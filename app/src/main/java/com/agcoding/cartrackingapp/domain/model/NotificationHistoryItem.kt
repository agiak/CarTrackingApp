package com.agcoding.cartrackingapp.domain.model

/**
 * Where a notification came from.
 *
 * The distinction is worth keeping because the two are not the same kind of
 * thing to the person reading them: a reminder is about their own car and they
 * asked for it, an announcement is news from us and they did not.
 */
enum class NotificationKind {
    REMINDER,
    ANNOUNCEMENT
}

/**
 * Domain model representing a single entry in the notification history.
 *
 * Every notification the user actually receives — a reminder the app raised
 * itself, or an announcement pushed from the server — is persisted here, so
 * the notifications screen can show what arrived and not only what is coming.
 */
data class NotificationHistoryItem(
    val id: Long = 0,
    val title: String,
    val description: String,
    val timestamp: Long,
    val kind: NotificationKind = NotificationKind.REMINDER
)
