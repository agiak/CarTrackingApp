package com.agcoding.cartrackingapp.domain.model

/**
 * Domain model representing a single entry in the notification history.
 *
 * Every time the app fires a notification (pre-expiry, overdue, test, …)
 * a [NotificationHistoryItem] is persisted so the user can review past
 * notifications inside Settings → Notifications → Notification History.
 */
data class NotificationHistoryItem(
    val id: Long = 0,
    val title: String,
    val description: String,
    val timestamp: Long
)

