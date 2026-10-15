package com.agcoding.cartrackingapp.cloud.data.repository

import com.agcoding.cartrackingapp.cloud.data.local.LocalDataWiper
import com.agcoding.cartrackingapp.domain.repository.DatabaseStatsRepository
import com.agcoding.cartrackingapp.cloud.data.network.NetworkMonitor
import com.agcoding.cartrackingapp.cloud.data.remote.SupabaseClient
import com.agcoding.cartrackingapp.cloud.data.remote.SupabaseException
import com.agcoding.cartrackingapp.cloud.data.remote.toCloudException
import com.agcoding.cartrackingapp.cloud.data.session.CloudSessionProvider
import com.agcoding.cartrackingapp.cloud.data.session.CloudSessionStore
import com.agcoding.cartrackingapp.cloud.domain.model.CloudError
import com.agcoding.cartrackingapp.cloud.domain.model.CloudException
import com.agcoding.cartrackingapp.cloud.domain.model.CloudMode
import com.agcoding.cartrackingapp.cloud.domain.model.CloudSession
import com.agcoding.cartrackingapp.cloud.domain.model.CloudState
import com.agcoding.cartrackingapp.cloud.domain.model.SyncStatus
import com.agcoding.cartrackingapp.cloud.domain.repository.CloudRepository
import com.agcoding.cartrackingapp.cloud.domain.sync.CloudSync
import com.agcoding.cartrackingapp.cloud.push.PushTokenRegistrar
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CloudRepositoryImpl @Inject constructor(
    private val client: SupabaseClient,
    private val store: CloudSessionStore,
    private val networkMonitor: NetworkMonitor,
    private val sync: CloudSync,
    private val sessions: CloudSessionProvider,
    private val localDataWiper: LocalDataWiper,
    private val pushTokens: PushTokenRegistrar,
    private val databaseStats: DatabaseStatsRepository,
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

    /**
     * Leaves the account, and takes the account's data off the phone with it.
     *
     * The wipe is no longer optional. While signed in, what the phone holds is
     * a copy of the account — so a signed-out phone still showing it is not
     * "keeping your data", it is a stale copy that the next sign-in has to
     * reconcile, and which a different account signing in would see. Leaving
     * the account empties the phone and the user is asked again how they want
     * to continue, which is the same question a fresh install asks.
     *
     * The guard is what keeps this safe, and it only refuses when there is
     * something real to lose: unsent edits, or records on a phone that has
     * never completed a sync. An empty database has nothing to strand, so it
     * must not block the user from leaving.
     */
    override suspend fun signOut() = onIo {
        val current = store.current()
        if (current.hasPendingChanges) {
            throw CloudException(CloudError.UNSYNCED_CHANGES)
        }
        if (current.lastSyncAt == null && !databaseStats.observeStats().first().isEmpty) {
            // Nothing has ever reached the server, so "it is safe, it is in
            // the cloud" would simply not be true.
            throw CloudException(CloudError.UNSYNCED_CHANGES)
        }

        current.session?.let { session ->
            // Before the token goes: deleting the push registration is an
            // authenticated call, and a line later there is nothing left to
            // authenticate it with — the phone would keep buzzing for an
            // account it is no longer signed into.
            pushTokens.unregisterBeforeSignOut(session)
            client.signOut(session.accessToken)
        }
        store.clearSession()
        // Signing in sets this (see adoptSession), and leaving the account has
        // to take it back. Otherwise one sign-in answers "account, or no
        // account?" for the rest of the install: the user signs out, is not
        // signed in and is not asked either, and lands in the app with no data
        // and no way back to their records but to go looking in Settings.
        store.saveAccountPromptAnswered(false)
        sync.reset()
        localDataWiper.wipe()
    }

    /**
     * Deletes the account on the server, then empties the phone.
     *
     * Unconditional for the same reason as signing out, and more so: the rows
     * on the server are gone, so a local copy left behind is a copy of
     * something that no longer exists, holding links to rows that cannot come
     * back. Anyone who wants to keep their records exports them first — the
     * button is two taps away in Settings.
     */
    override suspend fun deleteAccount() = onIo {
        client.deleteAccount(requireSessionInternal())
        store.clearSession()
        store.saveAccountPromptAnswered(false)
        sync.reset()
        localDataWiper.wipe()
    }

    override suspend fun displayName(): String? = onIo {
        client.displayName(requireSessionInternal())
    }

    override suspend fun markAccountPromptAnswered() {
        store.saveAccountPromptAnswered(true)
        // Only ever called by "continue without an account", and that answer
        // settles the matter for a phone that used to have one: the onboarding
        // check treats "has synced before, and now holds nothing" as a phone
        // whose records are stranded on a server, and without this the user
        // would be asked the same question on every launch until they added
        // their first car.
        store.clearLastSyncAt()
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

    private suspend fun requireSessionInternal(): CloudSession = sessions.require()

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

