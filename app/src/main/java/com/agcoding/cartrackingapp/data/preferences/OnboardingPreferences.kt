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
        val ACCOUNT_STEP_PENDING = booleanPreferencesKey("account_step_pending")
    }

    val isOnboardingCompleted: Flow<Boolean> = context.onboardingDataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.ONBOARDING_COMPLETED] ?: false
        }

    val arePermissionsRequested: Flow<Boolean> = context.onboardingDataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.PERMISSIONS_REQUESTED] ?: false
        }

    /**
     * Set when a sign-out puts the user back in front of the account question,
     * and cleared only when they answer it.
     *
     * On disk rather than in memory because killing the app is not an answer:
     * someone who signs out and then closes the app comes back to a phone with
     * no account on it, and the question is still the first thing that needs
     * settling.
     */
    val isAccountStepPending: Flow<Boolean> = context.onboardingDataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.ACCOUNT_STEP_PENDING] ?: false
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

    suspend fun setAccountStepPending(pending: Boolean) {
        context.onboardingDataStore.edit { preferences ->
            preferences[PreferencesKeys.ACCOUNT_STEP_PENDING] = pending
        }
    }
}

