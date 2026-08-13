package com.agcoding.cartrackingapp.data.local.database

import androidx.room.InvalidationTracker
import com.agcoding.cartrackingapp.data.preferences.DataMetadataPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Records the time of the last data modification by observing writes to the
 * user-data tables (cars, refills, expenses). Room's invalidation tracker fires
 * once per committed transaction that touches these tables — for inserts,
 * updates and deletes alike — so the stored timestamp reflects the real "last
 * change" rather than the newest record's own date.
 *
 * Started once from the Application; the registered observer lives for the
 * process lifetime.
 */
@Singleton
class DatabaseChangeObserver @Inject constructor(
    private val database: CarDatabase,
    private val metadata: DataMetadataPreferences
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var started = false

    fun start() {
        if (started) return
        started = true

        val observer = object : InvalidationTracker.Observer(
            "cars", "fuel_refills", "expenses"
        ) {
            override fun onInvalidated(tables: Set<String>) {
                scope.launch {
                    metadata.setLastDataModifiedAt(System.currentTimeMillis())
                }
            }
        }
        database.invalidationTracker.addObserver(observer)
    }
}
