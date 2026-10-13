package com.agcoding.cartrackingapp.cloud.data.session

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.agcoding.cartrackingapp.cloud.domain.model.CloudSession
import com.agcoding.cartrackingapp.cloud.domain.model.CloudState
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.cloudDataStore: DataStore<Preferences> by preferencesDataStore(name = "cloud_prefs")

/**
 * Everything about the account that has to outlive the process: the tokens,
 * when the last sync finished, and whether there are local edits waiting.
 *
 * Kept apart from the other preferences files because signing out wipes this
 * one wholesale while the device settings — language, theme — must survive.
 */
@Singleton
class CloudSessionStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private object Keys {
        val ACCESS_TOKEN = stringPreferencesKey("access_token")
        val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        val EXPIRES_AT = longPreferencesKey("expires_at")
        val USER_ID = stringPreferencesKey("user_id")
        val EMAIL = stringPreferencesKey("email")
        val MUST_CHANGE_PASSWORD = booleanPreferencesKey("must_change_password")

        val LAST_SYNC_AT = longPreferencesKey("last_sync_at")
        val PENDING_CHANGES = booleanPreferencesKey("pending_changes")

        /**
         * Which user the rows in `sync_links` were built for. If a different
         * account signs in on this phone, the links point at another user's
         * UUIDs and have to be thrown away rather than reused.
         */
        val LINKS_OWNER_ID = stringPreferencesKey("links_owner_id")

        val ACCOUNT_PROMPT_ANSWERED = booleanPreferencesKey("account_prompt_answered")
    }

    val state: Flow<CloudState> = context.cloudDataStore.data.map { it.toCloudState() }

    suspend fun current(): CloudState = state.first()

    suspend fun saveSession(session: CloudSession) {
        context.cloudDataStore.edit { prefs ->
            prefs[Keys.ACCESS_TOKEN] = session.accessToken
            prefs[Keys.REFRESH_TOKEN] = session.refreshToken
            prefs[Keys.EXPIRES_AT] = session.expiresAt
            prefs[Keys.USER_ID] = session.userId
            prefs[Keys.EMAIL] = session.email
            prefs[Keys.MUST_CHANGE_PASSWORD] = session.mustChangePassword
        }
    }

    /** Drops the tokens but keeps lastSyncAt and the prompt answer. */
    suspend fun clearSession() {
        context.cloudDataStore.edit { prefs ->
            prefs.remove(Keys.ACCESS_TOKEN)
            prefs.remove(Keys.REFRESH_TOKEN)
            prefs.remove(Keys.EXPIRES_AT)
            prefs.remove(Keys.USER_ID)
            prefs.remove(Keys.EMAIL)
            prefs.remove(Keys.MUST_CHANGE_PASSWORD)
            prefs.remove(Keys.LINKS_OWNER_ID)
            prefs.remove(Keys.PENDING_CHANGES)
        }
    }

    suspend fun saveLastSyncAt(epochMillis: Long) {
        context.cloudDataStore.edit { it[Keys.LAST_SYNC_AT] = epochMillis }
    }

    suspend fun savePendingChanges(pending: Boolean) {
        context.cloudDataStore.edit { it[Keys.PENDING_CHANGES] = pending }
    }

    suspend fun saveLinksOwnerId(userId: String) {
        context.cloudDataStore.edit { it[Keys.LINKS_OWNER_ID] = userId }
    }

    suspend fun saveAccountPromptAnswered(answered: Boolean) {
        context.cloudDataStore.edit { it[Keys.ACCOUNT_PROMPT_ANSWERED] = answered }
    }

    private fun Preferences.toCloudState(): CloudState {
        val accessToken = this[Keys.ACCESS_TOKEN]
        val refreshToken = this[Keys.REFRESH_TOKEN]
        val userId = this[Keys.USER_ID]
        // A half-written session is no session: all three are needed to make
        // any call at all, so treat anything less as signed out.
        val session = if (accessToken != null && refreshToken != null && userId != null) {
            CloudSession(
                accessToken = accessToken,
                refreshToken = refreshToken,
                expiresAt = this[Keys.EXPIRES_AT] ?: 0L,
                userId = userId,
                email = this[Keys.EMAIL].orEmpty(),
                mustChangePassword = this[Keys.MUST_CHANGE_PASSWORD] ?: false,
            )
        } else {
            null
        }
        return CloudState(
            session = session,
            lastSyncAt = this[Keys.LAST_SYNC_AT],
            hasPendingChanges = this[Keys.PENDING_CHANGES] ?: false,
            linksOwnerId = this[Keys.LINKS_OWNER_ID],
            accountPromptAnswered = this[Keys.ACCOUNT_PROMPT_ANSWERED] ?: false,
        )
    }
}
