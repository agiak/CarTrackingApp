package com.agcoding.cartrackingapp.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
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
    private object PreferencesKeys {
        val LAST_DISMISSED_DATE = stringPreferencesKey("last_dismissed_date")
    }

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    /**
     * Check if the banner was dismissed today
     */
    val isBannerDismissedToday: Flow<Boolean> = context.reminderBannerDataStore.data
        .map { preferences ->
            val lastDismissedDate = preferences[PreferencesKeys.LAST_DISMISSED_DATE]
            val today = dateFormat.format(Date())
            lastDismissedDate == today
        }

    /**
     * Mark the banner as dismissed for today
     */
    suspend fun dismissBannerForToday() {
        val today = dateFormat.format(Date())
        context.reminderBannerDataStore.edit { preferences ->
            preferences[PreferencesKeys.LAST_DISMISSED_DATE] = today
        }
    }
}
