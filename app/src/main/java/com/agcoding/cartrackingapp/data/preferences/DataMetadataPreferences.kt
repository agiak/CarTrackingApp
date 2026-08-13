package com.agcoding.cartrackingapp.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataMetadataDataStore: DataStore<Preferences> by
    preferencesDataStore(name = "data_metadata_preferences")

/**
 * Small metadata store about the app's data (as opposed to user settings).
 * Currently tracks when the database was last modified so it can be shown in
 * the About screen and embedded in exported backups.
 */
@Singleton
class DataMetadataPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val LAST_DATA_MODIFIED_AT = longPreferencesKey("last_data_modified_at")
    }

    /** Epoch millis of the last data change, or null if nothing has changed yet. */
    val lastDataModifiedAt: Flow<Long?> = context.dataMetadataDataStore.data
        .map { preferences -> preferences[Keys.LAST_DATA_MODIFIED_AT] }

    suspend fun setLastDataModifiedAt(timestamp: Long) {
        context.dataMetadataDataStore.edit { preferences ->
            preferences[Keys.LAST_DATA_MODIFIED_AT] = timestamp
        }
    }
}
