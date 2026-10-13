package com.agcoding.cartrackingapp.cloud.domain.sync

import com.agcoding.cartrackingapp.cloud.domain.model.SyncProgress
import com.agcoding.cartrackingapp.cloud.domain.model.SyncStatus
import com.agcoding.cartrackingapp.cloud.domain.model.SyncSummary
import kotlinx.coroutines.flow.StateFlow

/**
 * The sync engine, as the rest of the app sees it.
 *
 * Everything outside the `cloud` package — the repositories that announce an
 * edit, the activity that syncs on resume, the screens that show a spinner —
 * talks to this and never to the implementation. That keeps the sync one
 * swappable piece rather than something threaded through the whole app.
 */
interface CloudSync {

    /**
     * Whether this engine actually moves data.
     *
     * False while the per-entity tables are still being designed: the account
     * works, but nothing is uploaded. The account screen reads it so it can
     * say so plainly instead of letting the user believe their cars and
     * refills are backed up when they are not.
     */
    val movesData: Boolean

    val status: StateFlow<SyncStatus>

    /** Non-null only while a sync is running, for the onboarding progress bar. */
    val progress: StateFlow<SyncProgress?>

    /**
     * Asks for a sync without waiting for one. Safe to call from anywhere, as
     * often as you like: calls that arrive while one is running are coalesced.
     *
     * @param localChange true when the caller just changed something on the
     *   phone, which is also what marks the account as having unsent edits.
     */
    fun requestSync(localChange: Boolean = false)

    /**
     * Runs one round and waits for it. Used by the screens that have to show
     * the result — onboarding, and the "Sync now" button.
     */
    suspend fun syncNow(): SyncSummary

    /**
     * Forgets every link between a local row and its row on the server, so the
     * next sync starts over from nothing. Done when signing out, and when a
     * different account signs in on the same phone.
     */
    suspend fun reset()
}
