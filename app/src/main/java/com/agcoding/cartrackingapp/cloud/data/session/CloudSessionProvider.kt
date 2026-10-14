package com.agcoding.cartrackingapp.cloud.data.session

import com.agcoding.cartrackingapp.cloud.data.remote.SupabaseClient
import com.agcoding.cartrackingapp.cloud.data.remote.SupabaseException
import com.agcoding.cartrackingapp.cloud.domain.model.CloudError
import com.agcoding.cartrackingapp.cloud.domain.model.CloudException
import com.agcoding.cartrackingapp.cloud.domain.model.CloudSession
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Hands out an access token that is good for at least another couple of
 * minutes, refreshing it when it is not.
 *
 * There is exactly one of these because a Supabase refresh token is single
 * use: it is exchanged for a new pair and immediately invalidated. Two
 * refreshes racing each other would mean the slower one presenting a token the
 * server has already burned, and the user being signed out for no reason. Both
 * the repository and the sync go through here, behind one mutex, so that
 * cannot happen.
 */
@Singleton
class CloudSessionProvider @Inject constructor(
    private val client: SupabaseClient,
    private val store: CloudSessionStore,
) {
    private val mutex = Mutex()

    /** @throws CloudException [CloudError.SESSION_EXPIRED] when signed out. */
    suspend fun require(): CloudSession =
        valid() ?: throw CloudException(CloudError.SESSION_EXPIRED)

    /** The current session, or null when there is no usable one. */
    suspend fun valid(): CloudSession? {
        val session = store.current().session ?: return null
        if (!session.expiresSoon()) return session

        return mutex.withLock {
            // Another caller may have refreshed while this one waited.
            val latest = store.current().session ?: return@withLock null
            if (!latest.expiresSoon()) return@withLock latest

            val refreshed = try {
                client.refresh(latest.refreshToken)
            } catch (e: SupabaseException) {
                // A refresh token is only ever rejected for good: it was
                // revoked, or it has already been used. Keeping it would leave
                // the app retrying forever, so the session goes.
                if (e.status in 400..499) {
                    store.clearSession()
                    throw CloudException(CloudError.SESSION_EXPIRED, e)
                }
                throw e
            }
            store.saveSession(refreshed)
            refreshed
        }
    }

}

/**
 * Refresh this long before the token actually expires, so a sync that takes a
 * while does not have it die underneath it.
 */
private const val REFRESH_MARGIN_SECONDS = 120L

private fun CloudSession.expiresSoon(): Boolean =
    expiresAt - System.currentTimeMillis() / 1000 < REFRESH_MARGIN_SECONDS
