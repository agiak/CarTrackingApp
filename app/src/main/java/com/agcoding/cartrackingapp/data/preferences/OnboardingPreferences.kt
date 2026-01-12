package com.agcoding.cartrackingapp.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.onboardingDataStore: DataStore<Preferences> by preferencesDataStore(name = "onboarding_preferences")

@Singleton
class OnboardingPreferences @Inject constructor(
    private val context: Context
) {
    private object PreferencesKeys {
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val PERMISSIONS_REQUESTED = booleanPreferencesKey("permissions_requested")
    }

    val isOnboardingCompleted: Flow<Boolean> = context.onboardingDataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.ONBOARDING_COMPLETED] ?: false
        }

    val arePermissionsRequested: Flow<Boolean> = context.onboardingDataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.PERMISSIONS_REQUESTED] ?: false
        }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        context.onboardingDataStore.edit { preferences ->
            preferences[PreferencesKeys.ONBOARDING_COMPLETED] = completed
        }
    }

    suspend fun setPermissionsRequested(requested: Boolean) {
        context.onboardingDataStore.edit { preferences ->
            preferences[PreferencesKeys.PERMISSIONS_REQUESTED] = requested
        }
    }
}

