package com.agcoding.cartrackingapp.cloud.push

import com.agcoding.cartrackingapp.cloud.data.remote.SupabaseClient
import com.agcoding.cartrackingapp.cloud.data.session.CloudSessionProvider
import com.agcoding.cartrackingapp.cloud.data.session.CloudSessionStore
import com.agcoding.cartrackingapp.cloud.domain.model.CloudSession
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

/**
 * Keeps the server's list of devices to push to in step with who is signed in
 * on this one.
 *
 * The row is keyed on the Firebase token, which belongs to the *installation*
 * rather than to the account. That is what makes a hand-me-down phone behave:
 * when a second account signs in, the upsert moves the row instead of adding
 * one, so the previous owner stops receiving notifications on a device that is
 * no longer theirs.
 *
 * Everything here is best effort. A push token that fails to register is worth
 * a log line and nothing more — the app's actual job is unaffected, and the
 * next sign-in or token rotation tries again.
 */
@Singleton
class PushTokenRegistrar @Inject constructor(
    private val client: SupabaseClient,
    private val sessions: CloudSessionProvider,
    private val store: CloudSessionStore,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val mutex = Mutex()

    /** What the last successful registration said, so it is not repeated. */
    @Volatile
    private var registeredUserId: String? = null

    @Volatile
    private var registeredToken: String? = null

    /** Called once from the Application. Safe to call again; it only adds a collector. */
    fun start() {
        scope.launch {
            store.state
                .map { it.session?.userId }
                .distinctUntilChanged()
                .collect { userId ->
                    if (userId == null) {
                        // Signing out clears this so the next sign-in on the
                        // same phone registers again rather than believing it
                        // already did.
                        registeredUserId = null
                    } else {
                        runCatching { registerCurrentToken() }
                            .onFailure { Timber.w(it, "Could not register for push") }
                    }
                }
        }
    }

    /** Firebase rotates tokens on its own schedule; the server has to follow. */
    fun onTokenRefreshed(token: String) {
        scope.launch {
            runCatching { register(token) }
                .onFailure { Timber.w(it, "Could not register refreshed push token") }
        }
    }

    /**
     * Must run *before* the session is cleared: deleting the row is an
     * authenticated call, and a moment later there is nothing to authenticate
     * it with. Getting this wrong does not fail loudly — it just means a
     * signed-out phone keeps buzzing.
     */
    suspend fun unregisterBeforeSignOut(session: CloudSession) {
        val token = currentToken() ?: return
        runCatching { client.unregisterDeviceToken(session, token) }
            .onFailure { Timber.w(it, "Could not unregister push token") }
        registeredUserId = null
        registeredToken = null
    }

    private suspend fun registerCurrentToken() {
        val token = currentToken() ?: return
        register(token)
    }

    private suspend fun register(token: String) = mutex.withLock {
        val session = sessions.valid() ?: return@withLock
        if (registeredUserId == session.userId && registeredToken == token) return@withLock
        client.registerDeviceToken(session, token)
        registeredUserId = session.userId
        registeredToken = token
    }

    /**
     * The current token, or null when Firebase cannot produce one — which is
     * ordinary on a device with no Play Services, so it is not an error.
     */
    private suspend fun currentToken(): String? = suspendCancellableCoroutine { continuation ->
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (continuation.isActive) {
                continuation.resume(if (task.isSuccessful) task.result else null)
            }
        }
    }
}
