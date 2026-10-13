package com.agcoding.cartrackingapp.cloud.data.repository

import com.agcoding.cartrackingapp.cloud.data.local.LocalDataWiper
import com.agcoding.cartrackingapp.cloud.data.network.NetworkMonitor
import com.agcoding.cartrackingapp.cloud.data.remote.SupabaseClient
import com.agcoding.cartrackingapp.cloud.data.remote.SupabaseException
import com.agcoding.cartrackingapp.cloud.data.remote.toCloudException
import com.agcoding.cartrackingapp.cloud.data.session.CloudSessionStore
import com.agcoding.cartrackingapp.cloud.domain.model.CloudError
import com.agcoding.cartrackingapp.cloud.domain.model.CloudException
import com.agcoding.cartrackingapp.cloud.domain.model.CloudMode
import com.agcoding.cartrackingapp.cloud.domain.model.CloudSession
import com.agcoding.cartrackingapp.cloud.domain.model.CloudState
import com.agcoding.cartrackingapp.cloud.domain.model.SyncStatus
import com.agcoding.cartrackingapp.cloud.domain.repository.CloudRepository
import com.agcoding.cartrackingapp.cloud.domain.sync.CloudSync
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CloudRepositoryImpl @Inject constructor(
    private val client: SupabaseClient,
    private val store: CloudSessionStore,
    private val networkMonitor: NetworkMonitor,
    private val sync: CloudSync,
    private val localDataWiper: LocalDataWiper,
) : CloudRepository {

    override val state: Flow<CloudState> = store.state

    override val isOnline: StateFlow<Boolean> = networkMonitor.isOnline

    override val mode: Flow<CloudMode> =
        combine(store.state, networkMonitor.isOnline, sync.status) { state, online, status ->
            when {
                !state.isSignedIn -> CloudMode.OFFLINE_MODE
                status is SyncStatus.Syncing -> CloudMode.SYNCING
                // Checked before the error, because the usual reason a sync
                // failed is that the connection went away mid-way, and "no
                // internet" is the more useful of the two things to say.
                !online -> CloudMode.NO_INTERNET
                status is SyncStatus.Failed -> CloudMode.SYNC_ERROR
                else -> CloudMode.ONLINE
            }
        }

    /** Serialises token refreshes, so two parallel calls do not both refresh. */
    private val refreshMutex = Mutex()

    override suspend fun signUp(
        email: String,
        password: String,
        displayName: String,
    ) = onIo {
        val session = client.signUp(email.trim().lowercase(), password, displayName.trim())
        adoptSession(session)
    }

    override suspend fun signIn(email: String, password: String) = onIo {
        val address = email.trim().lowercase()
        val session = try {
            client.signIn(address, password)
        } catch (e: SupabaseException) {
            // Either it was the wrong password, or it was the temporary one
            // from the reset email — which is not a password yet, only a code
            // that can be exchanged for one. Try the exchange before giving up.
            if (e.toCloudException().error != CloudError.INVALID_CREDENTIALS) throw e
            if (!client.redeemTempPassword(address, password)) throw e
            client.signIn(address, password)
        }
        adoptSession(session)
    }

    override suspend fun requestPasswordReset(email: String, language: String) = onIo {
        client.requestTempPassword(email.trim().lowercase(), language)
    }

    override suspend fun changePassword(newPassword: String) = onIo {
        val session = requireSessionInternal()
        client.changePassword(session, newPassword)
        // The call also clears `must_change_password` server-side, but the
        // stored session still carries the old copy of the metadata, and that
        // is the copy the forced-change screen is watching.
        store.saveSession(session.copy(mustChangePassword = false))
    }

    override suspend fun signOut(wipeLocalData: Boolean) = onIo {
        val current = store.current()
        if (current.hasPendingChanges) {
            throw CloudException(CloudError.UNSYNCED_CHANGES)
        }
        if (wipeLocalData && current.lastSyncAt == null) {
            // Nothing has ever reached the server, so "it is safe, it is in
            // the cloud" would simply not be true.
            throw CloudException(CloudError.UNSYNCED_CHANGES)
        }

        current.session?.let { client.signOut(it.accessToken) }
        store.clearSession()
        sync.reset()
        if (wipeLocalData) localDataWiper.wipe()
    }

    override suspend fun deleteAccount(wipeLocalData: Boolean) = onIo {
        client.deleteAccount(requireSessionInternal())
        store.clearSession()
        sync.reset()
        if (wipeLocalData) localDataWiper.wipe()
    }

    override suspend fun displayName(): String? = onIo {
        client.displayName(requireSessionInternal())
    }

    override suspend fun markAccountPromptAnswered() {
        store.saveAccountPromptAnswered(true)
    }

    override suspend fun requireSession(): CloudSession = onIo { requireSessionInternal() }

    // ── plumbing ──────────────────────────────────────────────────────────

    /**
     * Records the session, and throws away the sync links if they were built
     * for somebody else. Those links map local row ids to another account's
     * UUIDs; reusing them would quietly push this phone's data into rows that
     * belong to a different user.
     */
    private suspend fun adoptSession(session: CloudSession) {
        val previousOwner = store.current().linksOwnerId
        if (previousOwner != null && previousOwner != session.userId) {
            sync.reset()
        }
        store.saveSession(session)
        store.saveLinksOwnerId(session.userId)
        store.saveAccountPromptAnswered(true)
    }

    private suspend fun requireSessionInternal(): CloudSession {
        val session = store.current().session
            ?: throw CloudException(CloudError.SESSION_EXPIRED)
        if (!session.expiresSoon()) return session

        return refreshMutex.withLock {
            // Another caller may have refreshed while this one waited.
            val latest = store.current().session
                ?: throw CloudException(CloudError.SESSION_EXPIRED)
            if (!latest.expiresSoon()) return@withLock latest

            val refreshed = try {
                client.refresh(latest.refreshToken)
            } catch (e: SupabaseException) {
                // A refresh token is only ever rejected for good: it was
                // revoked, or it has been used. Keeping it would leave the app
                // retrying forever, so the session goes.
                if (e.status == 400 || e.status == 401) {
                    store.clearSession()
                    throw CloudException(CloudError.SESSION_EXPIRED, e)
                }
                throw e
            }
            store.saveSession(refreshed)
            refreshed
        }
    }

    /**
     * Runs the block on IO and makes sure whatever comes out of it is a
     * [CloudException], so callers have exactly one thing to catch.
     */
    private suspend fun <T> onIo(block: suspend () -> T): T = withContext(Dispatchers.IO) {
        try {
            block()
        } catch (e: CancellationException) {
            // Leaving a screen cancels its coroutine. That is not a failure
            // and must not be reported to the user as one.
            throw e
        } catch (e: Exception) {
            throw e.toCloudException()
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
