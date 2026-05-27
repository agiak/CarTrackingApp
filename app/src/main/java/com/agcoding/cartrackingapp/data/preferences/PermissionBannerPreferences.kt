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

private val Context.permissionBannerDataStore: DataStore<Preferences>
    by preferencesDataStore(name = "permission_banner_preferences")

@Singleton
class PermissionBannerPreferences @Inject constructor(
    private val context: Context
) {
    private object Keys {
        val BANNER_DISMISSED = booleanPreferencesKey("banner_dismissed")
    }

    val isBannerDismissed: Flow<Boolean> =
        context.permissionBannerDataStore.data.map { it[Keys.BANNER_DISMISSED] ?: false }

    suspend fun dismissBanner() {
        context.permissionBannerDataStore.edit { it[Keys.BANNER_DISMISSED] = true }
    }
}
