package com.agcoding.cartrackingapp.cloud.data.remote

import com.agcoding.cartrackingapp.cloud.domain.model.CloudSession
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.Locale
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The whole backend surface the app uses, as plain REST over OkHttp.
 *
 * There is no Supabase SDK here on purpose: the app needs a handful of calls,
 * and the SDK would drag in Ktor and kotlinx-serialization along with their
 * own Kotlin-version constraints. `org.json` is already in the platform.
 *
 * **Every method blocks.** They are meant to be called from a background
 * dispatcher — SyncManager and CloudRepository both do their work on IO.
 */
@Singleton
class SupabaseClient @Inject constructor() {

    private val http = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    // ── Auth ──────────────────────────────────────────────────────────────

    /**
     * Creates the account through the `signup` edge function — which marks the
     * email confirmed, so no verification mail is ever sent — then signs in.
     */
    fun signUp(email: String, password: String, displayName: String): CloudSession {
        call(
            method = "POST",
            path = "/functions/v1/signup",
            body = JSONObject()
                .put("email", email.normalizedEmail())
                .put("password", password)
                .put("display_name", displayName)
                .toString(),
        )
        return signIn(email, password)
    }

    fun signIn(email: String, password: String): CloudSession = parseSession(
        JSONObject(
            call(
                method = "POST",
                path = "/auth/v1/token?grant_type=password",
                body = JSONObject()
                    .put("email", email.normalizedEmail())
                    .put("password", password)
                    .toString(),
            )
        )
    )

    fun refresh(refreshToken: String): CloudSession = parseSession(
        JSONObject(
            call(
                method = "POST",
                path = "/auth/v1/token?grant_type=refresh_token",
                body = JSONObject().put("refresh_token", refreshToken).toString(),
            )
        )
    )

    /**
     * Best effort: the local session is cleared either way, so a logout that
     * fails because the token had already expired must not fail the sign-out.
     */
    fun signOut(accessToken: String) {
        runCatching { call("POST", "/auth/v1/logout", "{}", accessToken) }
    }

    /**
     * Sets a new password and clears `must_change_password`, which is what
     * releases the forced-change screen.
     */
    fun changePassword(session: CloudSession, newPassword: String) {
        val body = JSONObject()
            .put("password", newPassword)
            .put("data", JSONObject().put("must_change_password", false))
        call("PUT", "/auth/v1/user", body.toString(), session.accessToken)
    }

    /**
     * Asks for a temporary password by email. Always succeeds, even for an
     * address with no account — otherwise this endpoint would tell an attacker
     * which emails are registered.
     */
    fun requestTempPassword(email: String, language: String) {
        call(
            method = "POST",
            path = "/functions/v1/password-reset",
            body = JSONObject()
                .put("action", "request")
                .put("email", email.normalizedEmail())
                .put("lang", language)
                .toString(),
        )
    }

    /** @return true if the temporary password matched and is now the password. */
    fun redeemTempPassword(email: String, password: String): Boolean = try {
        call(
            method = "POST",
            path = "/functions/v1/password-reset",
            body = JSONObject()
                .put("action", "redeem")
                .put("email", email.normalizedEmail())
                .put("password", password)
                .toString(),
        )
        true
    } catch (e: SupabaseException) {
        // 400 is the function's "no, that code is not valid" answer, which is
        // an expected outcome here, not a failure worth propagating.
        if (e.status == 400) false else throw e
    }

    fun deleteAccount(session: CloudSession) {
        call("POST", "/functions/v1/delete-account", "{}", session.accessToken)
    }

    fun displayName(session: CloudSession): String? {
        val url = tableUrl("profiles")
            .addQueryParameter("user_id", "eq.${session.userId}")
            .addQueryParameter("select", "display_name")
            .build()
        val rows = JSONArray(request("GET", url, token = session.accessToken))
        if (rows.length() == 0) return null
        return rows.getJSONObject(0).optString("display_name").takeIf { it.isNotBlank() }
    }

    // ── Tables ────────────────────────────────────────────────────────────

    /**
     * Every row the user owns, soft-deleted ones included — the sync needs the
     * tombstones to learn about deletions made on another device.
     */
    fun selectAll(session: CloudSession, table: String, orderBy: String): List<JSONObject> {
        val all = mutableListOf<JSONObject>()
        var offset = 0
        while (true) {
            val url = tableUrl(table)
                .addQueryParameter("user_id", "eq.${session.userId}")
                .addQueryParameter("select", "*")
                .addQueryParameter("order", orderBy)
                .addQueryParameter("limit", SELECT_PAGE.toString())
                .addQueryParameter("offset", offset.toString())
                .build()
            val page = JSONArray(request("GET", url, token = session.accessToken))
            for (i in 0 until page.length()) all += page.getJSONObject(i)
            if (page.length() < SELECT_PAGE) break
            offset += SELECT_PAGE
        }
        return all
    }

    /**
     * Inserts and updates in one go, in batches so a first upload of thousands
     * of rows does not become one enormous request.
     *
     * @param onConflict the columns that decide insert-vs-update: `id` for the
     *   usual tables, `user_id,<key>` for the ones keyed by a natural key.
     * @param onProgress called with (rows done, rows total) after each batch.
     */
    fun upsert(
        session: CloudSession,
        table: String,
        rows: List<JSONObject>,
        onConflict: String,
        onProgress: (Int, Int) -> Unit = { _, _ -> },
    ) {
        if (rows.isEmpty()) return
        val url = tableUrl(table).addQueryParameter("on_conflict", onConflict).build()
        var done = 0
        rows.chunked(UPSERT_BATCH).forEach { batch ->
            val body = JSONArray().apply { batch.forEach { put(it) } }.toString()
            request(
                method = "POST",
                url = url,
                body = body,
                token = session.accessToken,
                prefer = "resolution=merge-duplicates,return=minimal",
            )
            done += batch.size
            onProgress(done, rows.size)
        }
    }

    /**
     * Marks rows deleted rather than removing them, so the other devices find
     * out. A hard delete would simply look like "a row I have not seen yet"
     * and the sync would helpfully put it back.
     */
    fun softDelete(
        session: CloudSession,
        table: String,
        keyColumn: String,
        ids: List<String>,
        deletedAt: String,
    ) {
        if (ids.isEmpty()) return
        val body = JSONObject().put("deleted_at", deletedAt).toString()
        ids.chunked(DELETE_BATCH).forEach { batch ->
            val url = tableUrl(table)
                .addQueryParameter("user_id", "eq.${session.userId}")
                .addQueryParameter(keyColumn, batch.joinToString(",", "in.(", ")") { quoted(it) })
                .build()
            request(
                method = "PATCH",
                url = url,
                body = body,
                token = session.accessToken,
                prefer = "return=minimal",
            )
        }
    }

    // ── plumbing ──────────────────────────────────────────────────────────

    /**
     * The address as the account is actually stored under.
     *
     * The `signup` edge function trims and lower-cases before creating the
     * user, so anything that does not do the same here is looking for an
     * account under a name nobody has. A keyboard that capitalises the first
     * letter, or an autofill that hands over a trailing space, would otherwise
     * create `Me@x.gr` and then fail to sign into it — with "wrong password"
     * as the only explanation offered.
     *
     * [java.util.Locale.ROOT] because in Turkish locales `I` lower-cases to a
     * dotless `ı`, which would turn a valid address into a different one on
     * exactly the phones least likely to be testing this.
     */
    private fun String.normalizedEmail(): String = trim().lowercase(Locale.ROOT)

    /**
     * One value of a PostgREST `in.(…)` list.
     *
     * The quotes are what let a value contain a comma or a parenthesis, so a
     * value that contains a quote of its own has to escape it — and the
     * backslash that does the escaping has to be escaped first. Without this,
     * deleting a category called `Service "A"` sends a filter the server
     * cannot parse, and every sync from then on fails on the same row.
     */
    private fun quoted(value: String): String =
        "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\""

    private fun tableUrl(table: String): HttpUrl.Builder =
        (SupabaseConfig.URL + "/rest/v1/" + table).toHttpUrl().newBuilder()

    private fun call(
        method: String,
        path: String,
        body: String? = null,
        token: String? = null,
        prefer: String? = null,
    ): String = request(method, (SupabaseConfig.URL + path).toHttpUrl(), body, token, prefer)

    private fun request(
        method: String,
        url: HttpUrl,
        body: String? = null,
        token: String? = null,
        prefer: String? = null,
    ): String {
        // OkHttp insists on a body for POST/PUT/PATCH even when there is
        // nothing to send, and refuses one for GET.
        val payload = (body ?: if (method == "GET" || method == "DELETE") null else "")
            ?.toRequestBody(JSON_MEDIA_TYPE)
        val request = Request.Builder()
            .url(url)
            .header("apikey", SupabaseConfig.PUBLISHABLE_KEY)
            .apply {
                // Unauthenticated calls still need a bearer token for the
                // gateway; the publishable key doubles as the anon one.
                header("Authorization", "Bearer " + (token ?: SupabaseConfig.PUBLISHABLE_KEY))
                if (prefer != null) header("Prefer", prefer)
            }
            .method(method, payload)
            .build()

        http.newCall(request).execute().use { response ->
            val text = response.body?.string().orEmpty()
            if (!response.isSuccessful) throw parseError(response.code, text)
            return text
        }
    }

    private fun parseSession(json: JSONObject): CloudSession {
        val user = json.getJSONObject("user")
        val expiresAt = json.optLong("expires_at", 0L).takeIf { it > 0L }
            ?: (System.currentTimeMillis() / 1000 + json.optLong("expires_in", 3600L))
        return CloudSession(
            accessToken = json.getString("access_token"),
            refreshToken = json.getString("refresh_token"),
            expiresAt = expiresAt,
            userId = user.getString("id"),
            email = user.optString("email"),
            mustChangePassword = user.optJSONObject("user_metadata")
                ?.optBoolean("must_change_password", false) ?: false,
        )
    }

    companion object {
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

        private const val SELECT_PAGE = 1000
        private const val UPSERT_BATCH = 200
        private const val DELETE_BATCH = 100

        /**
         * The three services answer with three different error shapes: Auth
         * uses `{msg, error_code}` or `{error, error_description}`, PostgREST
         * and the edge functions use `{message, code}`. Try them all rather
         * than guessing from the path.
         */
        internal fun parseError(status: Int, body: String): SupabaseException {
            val json = runCatching { JSONObject(body) }.getOrNull()
                ?: return SupabaseException(status, body.ifBlank { "HTTP $status" })
            val message = listOf("msg", "error_description", "message", "error")
                .firstNotNullOfOrNull { key -> json.optString(key).takeIf { it.isNotBlank() } }
                ?: "HTTP $status"
            val code = listOf("error_code", "code", "error")
                .firstNotNullOfOrNull { key ->
                    json.opt(key)?.toString()?.takeIf { it.isNotBlank() && it != "null" }
                }
            return SupabaseException(status, message, code)
        }
    }
}

class SupabaseException(
    val status: Int,
    message: String,
    val code: String? = null,
) : IOException(message)
