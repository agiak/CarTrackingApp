package com.agcoding.cartrackingapp.data.preferences

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

private val Context.reminderBannerDataStore: DataStore<Preferences> by preferencesDataStore(name = "reminder_banner_preferences")

@Singleton
class ReminderBannerPreferences @Inject constructor(
    private val context: Context
) {
    companion object {
        private const val TAG = "ReminderBannerPrefs"
    }

    private object PreferencesKeys {
        val LAST_DISMISSED_DATE = stringPreferencesKey("last_dismissed_date")
        val DISMISSED_REMINDER_COUNT = intPreferencesKey("dismissed_reminder_count")
    }

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    /**
     * Check if the banner should be dismissed based on:
     * 1. Was dismissed today
     * 2. The number of reminders hasn't increased since dismissal
     */
    fun isBannerDismissed(currentReminderCount: Int): Flow<Boolean> {
        return context.reminderBannerDataStore.data.map { preferences ->
            val lastDismissedDate = preferences[PreferencesKeys.LAST_DISMISSED_DATE]
            val dismissedCount = preferences[PreferencesKeys.DISMISSED_REMINDER_COUNT] ?: 0
            val today = dateFormat.format(Date())

            // Banner is dismissed only if:
            // 1. It was dismissed today AND
            // 2. The reminder count hasn't increased
            val isDismissed = lastDismissedDate == today && currentReminderCount <= dismissedCount

            Log.d(TAG, "Banner dismissed check: lastDismissed=$lastDismissedDate, today=$today, " +
                    "dismissedCount=$dismissedCount, currentCount=$currentReminderCount, isDismissed=$isDismissed")
            isDismissed
        }
    }

    /**
     * Mark the banner as dismissed for today with the current reminder count
     */
    suspend fun dismissBannerForToday(reminderCount: Int) {
        val today = dateFormat.format(Date())
        Log.d(TAG, "Dismissing banner for today: $today with count: $reminderCount")
        context.reminderBannerDataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_DISMISSED_DATE] = today
            preferences[PreferencesKeys.DISMISSED_REMINDER_COUNT] = reminderCount
        }
    }
}
