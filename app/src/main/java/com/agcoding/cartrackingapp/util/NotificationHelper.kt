package com.agcoding.cartrackingapp.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.agcoding.cartrackingapp.MainActivity
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.domain.model.NotificationHistoryItem
import com.agcoding.cartrackingapp.domain.repository.NotificationHistoryRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Central helper for creating and showing notifications.
 *
 * Responsibilities:
 * - Creates the notification channel on first use.
 * - Provides [showPreExpiryNotification] for real reminder notifications.
 * - Provides [showTestNotification] so developers can instantly verify the
 *   full notification pipeline from Developer Options.
 * - Exposes [areNotificationsEnabled] which checks both the system-level
 *   notification permission (Android 13+) and the channel enabled state.
 * - Persists every sent notification in [NotificationHistoryRepository]
 *   so the user can review past notifications inside the app.
 */
@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val notificationHistoryRepository: NotificationHistoryRepository
) {
    companion object {
        const val CHANNEL_ID_REMINDERS = "car_reminders"
        const val NOTIFICATION_ID_REMINDER_BASE = 1000
        private const val NOTIFICATION_ID_TEST = 9999
        private const val TAG = "NotificationHelper"
    }

    /** Dedicated scope for fire-and-forget persistence – never blocks the caller. */
    private val persistenceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        val reminderChannel = NotificationChannel(
            CHANNEL_ID_REMINDERS,
            context.getString(R.string.notification_channel_reminders),
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = context.getString(R.string.notification_channel_reminders_desc)
            enableVibration(true)
            setShowBadge(true)
        }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(reminderChannel)
    }

    /**
     * Show a real pre-expiry notification for an expense reminder.
     */
    fun showPreExpiryNotification(
        expenseId: Long,
        category: String,
        message: String
    ) {
        Log.d(TAG, "Attempting to show notification for expense $expenseId: $category")

        val title = context.getString(R.string.notification_reminder_title, category)

        // Create intent to open app and navigate to the specific expense reminder
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("expense_id", expenseId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            expenseId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_REMINDERS)
            .setSmallIcon(R.drawable.ic_notification) // app icon – falls back below
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            val notificationId = NOTIFICATION_ID_REMINDER_BASE + expenseId.toInt()
            NotificationManagerCompat.from(context).notify(notificationId, notification)
            Log.d(TAG, "Notification shown successfully with ID: $notificationId")

            // Persist to notification history
            persistNotification(title, message)
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException: Notification permission not granted", e)
        } catch (e: Exception) {
            Log.e(TAG, "Error showing notification", e)
        }
    }

    /**
     * Send a test notification immediately. Uses the same channel and builder
     * as real reminders so the developer can verify the full pipeline.
     *
     * @return `true` if the notification was posted, `false` if permissions are missing.
     */
    fun showTestNotification(): Boolean {
        if (!areNotificationsEnabled()) {
            Log.w(TAG, "Cannot show test notification – notifications disabled")
            return false
        }

        val title = context.getString(R.string.notification_reminder_title, "Test")
        val message = "This is a test notification from Developer Options. If you see this, the notification pipeline is working correctly!"

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, NOTIFICATION_ID_TEST, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_REMINDERS)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText("This is a test notification from Developer Options.")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(message)
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        return try {
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID_TEST, notification)
            Log.d(TAG, "Test notification posted (id=$NOTIFICATION_ID_TEST)")

            // Persist to notification history
            persistNotification(title, message)
            true
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException: notification permission not granted", e)
            false
        }
    }

    fun areNotificationsEnabled(): Boolean {
        val enabled = NotificationManagerCompat.from(context).areNotificationsEnabled()
        Log.d(TAG, "Notifications enabled: $enabled")
        return enabled
    }

    /**
     * Persists the notification title and description in the local database
     * so the user can later review it in the Notification History screen.
     */
    private fun persistNotification(title: String, description: String) {
        persistenceScope.launch {
            try {
                notificationHistoryRepository.insertNotification(
                    NotificationHistoryItem(
                        title = title,
                        description = description,
                        timestamp = System.currentTimeMillis()
                    )
                )
                Log.d(TAG, "Notification persisted to history: $title")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to persist notification to history", e)
            }
        }
    }
}
