package com.agcoding.cartrackingapp.cloud.domain.model

/**
 * A signed-in Supabase session, as it is held in memory and in DataStore.
 *
 * [expiresAt] is the epoch **second** the access token stops being accepted —
 * the unit Supabase itself uses in the token response — so it is compared
 * against `System.currentTimeMillis() / 1000`, not against milliseconds.
 */
data class CloudSession(
    val accessToken: String,
    val refreshToken: String,
    val expiresAt: Long,
    val userId: String,
    val email: String,
    /**
     * Set when the user signed in with a temporary password. The app blocks on
     * a change-password screen until they pick a real one.
     */
    val mustChangePassword: Boolean = false,
)

/** Everything persisted about the account, as one value. */
data class CloudState(
    val session: CloudSession? = null,
    /** Epoch millis of the last successful sync, or null if it never ran. */
    val lastSyncAt: Long? = null,
    /** Local edits that have not reached the server yet. Survives a restart. */
    val hasPendingChanges: Boolean = false,
    /** Which user the rows in `sync_links` belong to; see CloudSessionStore. */
    val linksOwnerId: String? = null,
    /** True once the user has answered the "create an account?" prompt. */
    val accountPromptAnswered: Boolean = false,
) {
    val isSignedIn: Boolean get() = session != null
}

sealed interface SyncStatus {
    data object Idle : SyncStatus
    data object Syncing : SyncStatus
    data class Failed(val message: String) : SyncStatus
}

/**
 * What the status card shows. Derived from session, sync status, connectivity
 * and pending changes together — no single one of those is enough on its own.
 */
enum class CloudMode {
    /** No account. Everything works, nothing leaves the phone. */
    OFFLINE_MODE,
    ONLINE,
    SYNCING,
    NO_INTERNET,
    SYNC_ERROR,
}

/**
 * The named steps of a sync, in order. The onboarding progress bar names the
 * one it is on, so a first upload reads as progress rather than as a frozen
 * screen — and so a long step looks like work rather than like a hang.
 */
enum class SyncStage {
    CONNECTING,
    CHECKING,
    CARS,
    TRIPS,
    REFILLS,
    EXPENSES,
    CATEGORIES,
    FINISHING,
    DONE,
}

data class SyncProgress(val stage: SyncStage, val fraction: Float)

/** What one round of syncing did, for the "Done!" screen. */
data class SyncSummary(
    val cars: Int = 0,
    val trips: Int = 0,
    val refills: Int = 0,
    val expenses: Int = 0,
    val categories: Int = 0,
    val received: Int = 0,
) {
    val pushed: Int get() = cars + trips + refills + expenses + categories

    val changedAnything: Boolean get() = pushed > 0 || received > 0
}
