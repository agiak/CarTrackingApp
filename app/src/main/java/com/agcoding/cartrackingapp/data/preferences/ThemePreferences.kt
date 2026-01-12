package com.agcoding.cartrackingapp.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

enum class ThemeMode {
    SYSTEM, LIGHT, DARK
}

@Singleton
class ThemePreferences @Inject constructor(
    private val context: Context
) {
    private object PreferencesKeys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val IS_DARK_MODE_OVERRIDE = booleanPreferencesKey("is_dark_mode_override")
    }

    val themeModeFlow: Flow<ThemeMode> = context.dataStore.data
        .map { preferences ->
            val themeModeString = preferences[PreferencesKeys.THEME_MODE] ?: ThemeMode.SYSTEM.name
            try {
                ThemeMode.valueOf(themeModeString)
            } catch (e: IllegalArgumentException) {
                ThemeMode.SYSTEM
            }
        }

    val isDarkModeOverrideFlow: Flow<Boolean?> = context.dataStore.data
        .map { preferences ->
            if (preferences.contains(PreferencesKeys.IS_DARK_MODE_OVERRIDE)) {
                preferences[PreferencesKeys.IS_DARK_MODE_OVERRIDE]
            } else {
                null // null means follow system
            }
        }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME_MODE] = mode.name
        }
    }

    suspend fun setDarkModeOverride(isDark: Boolean?) {
        context.dataStore.edit { preferences ->
            if (isDark == null) {
                preferences.remove(PreferencesKeys.IS_DARK_MODE_OVERRIDE)
            } else {
                preferences[PreferencesKeys.IS_DARK_MODE_OVERRIDE] = isDark
            }
        }
    }
}

