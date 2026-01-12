package com.agcoding.cartrackingapp.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// Single DataStore instance at file level
private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "app_settings")

enum class AppTheme {
    SYSTEM,
    LIGHT,
    DARK
}

enum class AppLanguage(val displayName: String, val code: String) {
    ENGLISH("English", "en"),
    GREEK("Ελληνικά (Greek)", "el")
}

data class AppSettings(
    val theme: AppTheme = AppTheme.SYSTEM,
    val language: AppLanguage = AppLanguage.ENGLISH,
    val notificationsEnabled: Boolean = true
)

@Singleton
class SettingsPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val THEME = stringPreferencesKey("app_theme")
        val LANGUAGE = stringPreferencesKey("app_language")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
    }

    val settingsFlow: Flow<AppSettings> = context.settingsDataStore.data.map { preferences ->
        AppSettings(
            theme = preferences[Keys.THEME]?.let {
                try { AppTheme.valueOf(it) } catch (_: Exception) { AppTheme.SYSTEM }
            } ?: AppTheme.SYSTEM,
            language = preferences[Keys.LANGUAGE]?.let { code ->
                AppLanguage.entries.find { it.code == code }
            } ?: AppLanguage.ENGLISH,
            notificationsEnabled = preferences[Keys.NOTIFICATIONS_ENABLED] ?: true
        )
    }

    suspend fun updateTheme(theme: AppTheme) {
        context.settingsDataStore.edit { preferences ->
            preferences[Keys.THEME] = theme.name
        }
    }

    suspend fun updateLanguage(language: AppLanguage) {
        context.settingsDataStore.edit { preferences ->
            preferences[Keys.LANGUAGE] = language.code
        }
    }

    suspend fun updateNotificationsEnabled(enabled: Boolean) {
        context.settingsDataStore.edit { preferences ->
            preferences[Keys.NOTIFICATIONS_ENABLED] = enabled
        }
    }
}

