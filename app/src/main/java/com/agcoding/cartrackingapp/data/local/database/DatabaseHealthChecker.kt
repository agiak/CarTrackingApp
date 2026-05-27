package com.agcoding.cartrackingapp.data.local.database

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

sealed class DatabaseStatus {
    object Checking : DatabaseStatus()
    object Healthy : DatabaseStatus()
    object MigrationFailed : DatabaseStatus()
}

@Singleton
class DatabaseHealthChecker @Inject constructor(
    private val database: CarDatabase
) {
    private val _status = MutableStateFlow<DatabaseStatus>(DatabaseStatus.Checking)
    val status: StateFlow<DatabaseStatus> = _status.asStateFlow()

    suspend fun check() {
        _status.value = try {
            withContext(Dispatchers.IO) {
                database.openHelper.writableDatabase
            }
            DatabaseStatus.Healthy
        } catch (e: Exception) {
            Timber.e(e, "Database migration failed")
            DatabaseStatus.MigrationFailed
        }
    }
}
