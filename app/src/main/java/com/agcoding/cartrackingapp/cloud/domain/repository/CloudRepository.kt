package com.agcoding.cartrackingapp.cloud.domain.repository

import com.agcoding.cartrackingapp.cloud.domain.model.CloudMode
import com.agcoding.cartrackingapp.cloud.domain.model.CloudSession
import com.agcoding.cartrackingapp.cloud.domain.model.CloudState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

/**
 * The account: who is signed in, and everything that changes that.
 *
 * **These throw rather than return a Result.** The rest of the app wraps its
 * failures in [com.agcoding.cartrackingapp.shared.domain.result.Result], but
 * the cloud package has its own small vocabulary of user-visible failures —
 * [com.agcoding.cartrackingapp.cloud.domain.model.CloudError] — and one
 * `catch (e: CloudException)` in the ViewModel maps every one of them to a
 * message. Threading those through a second error type would only lose detail.
 *
 * Everything suspending here does its work on IO.
 */
interface CloudRepository {

    val state: Flow<CloudState>

    val isOnline: StateFlow<Boolean>

    /** What the status card shows; see [CloudMode]. */
    val mode: Flow<CloudMode>

    /**
     * Creates the account and signs in. The email is confirmed server-side, so
     * there is no verification mail and no waiting.
     */
    suspend fun signUp(email: String, password: String, displayName: String)

    /**
     * Signs in with either the real password or a temporary one that was
     * emailed out. The user does not have to say which they are typing — and
     * if it was a temporary one the session comes back with
     * [CloudSession.mustChangePassword] set, which is what sends them to the
     * change-password screen.
     */
    suspend fun signIn(email: String, password: String)

    /**
     * Emails a temporary password. Succeeds even for an address with no
     * account, so this cannot be used to find out who is registered.
     *
     * @param language `el` or `en`, for the language of the mail.
     */
    suspend fun requestPasswordReset(email: String, language: String)

    suspend fun changePassword(newPassword: String)

    /**
     * @param wipeLocalData also clears this phone's cars, refills, expenses,
     *   trips and attachments. Refused unless a sync has actually completed at
     *   least once, because otherwise the data exists nowhere else.
     * @throws com.agcoding.cartrackingapp.cloud.domain.model.CloudException
     *   with [com.agcoding.cartrackingapp.cloud.domain.model.CloudError.UNSYNCED_CHANGES]
     *   when there are edits that have not reached the server.
     */
    suspend fun signOut(wipeLocalData: Boolean = false)

    /**
     * Deletes the account and, with it, every row it owns on the server.
     *
     * @param wipeLocalData also clears this phone. Off by default: deleting
     *   the account means going back to using the app offline, not throwing
     *   away the data.
     */
    suspend fun deleteAccount(wipeLocalData: Boolean = false)

    /** The name shown in the account screen, or null if none was given. */
    suspend fun displayName(): String?

    /** Remembers that the "create an account?" prompt has been answered. */
    suspend fun markAccountPromptAnswered()

    /**
     * The current session, with the access token refreshed if it is about to
     * expire. The sync calls this before each round.
     */
    suspend fun requireSession(): CloudSession
}
