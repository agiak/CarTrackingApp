package com.agcoding.cartrackingapp.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.agcoding.cartrackingapp.MainActivity
import com.agcoding.cartrackingapp.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val CHANNEL_ID_REMINDERS = "car_reminders"
        const val NOTIFICATION_ID_REMINDER_BASE = 1000
        private const val TAG = "NotificationHelper"
    }

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
        }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(reminderChannel)
    }

    fun showPreExpiryNotification(
        expenseId: Long,
        category: String,
        message: String
    ) {
        Log.d(TAG, "Attempting to show notification for expense $expenseId: $category")

        // Create intent to open app when notification is tapped
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            expenseId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_REMINDERS)
            .setSmallIcon(android.R.drawable.ic_dialog_info) // Using system icon for now
            .setContentTitle(context.getString(R.string.notification_reminder_title, category))
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
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException: Notification permission not granted", e)
        } catch (e: Exception) {
            Log.e(TAG, "Error showing notification", e)
        }
    }

    fun areNotificationsEnabled(): Boolean {
        val enabled = NotificationManagerCompat.from(context).areNotificationsEnabled()
        Log.d(TAG, "Notifications enabled: $enabled")
        return enabled
    }
}
