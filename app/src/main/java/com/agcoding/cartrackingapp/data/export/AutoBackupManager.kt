package com.agcoding.cartrackingapp.data.export

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.room.InvalidationTracker
import com.agcoding.cartrackingapp.data.local.database.CarDatabase
import com.agcoding.cartrackingapp.domain.model.FuelRefill
import com.agcoding.cartrackingapp.domain.model.Expense
import com.agcoding.cartrackingapp.domain.repository.CarRepository
import com.agcoding.cartrackingapp.domain.repository.ExpenseRepository
import com.agcoding.cartrackingapp.domain.repository.RefillRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import timber.log.Timber
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

const val AUTO_BACKUP_FILENAME = "car_expenses_auto_backup.xlsx"
private val WATCHED_TABLES = arrayOf("cars", "fuel_refills", "expenses", "expense_categories")
private const val DEBOUNCE_MS = 3000L

@Singleton
class AutoBackupManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val database: CarDatabase,
    private val dataExportManager: DataExportManager,
    private val carRepository: CarRepository,
    private val refillRepository: RefillRepository,
    private val expenseRepository: ExpenseRepository
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val trigger = MutableSharedFlow<Unit>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    fun start() {
        scope.launch {
            trigger
                .debounce(DEBOUNCE_MS)
                .collect { runBackup() }
        }

        database.invalidationTracker.addObserver(object : InvalidationTracker.Observer(WATCHED_TABLES) {
            override fun onInvalidated(tables: Set<String>) {
                scope.launch { trigger.emit(Unit) }
            }
        })
    }

    private suspend fun runBackup() {
        try {
            val cars = carRepository.getAllCars().first()
            val allRefills = mutableListOf<FuelRefill>()
            val allExpenses = mutableListOf<Expense>()
            for (car in cars) {
                allRefills.addAll(refillRepository.getRefillsByCarId(car.id).first())
                allExpenses.addAll(expenseRepository.getExpensesByCarId(car.id).first())
            }
            val customCategories = database.expenseCategoryDao()
                .getCustomCategories()
                .first()
                .map { it.name }

            val bytes = dataExportManager.buildExcelBytes(cars, allRefills, allExpenses, customCategories)
            saveReplacing(bytes)
            Timber.d("Auto backup completed (${cars.size} cars, ${allRefills.size} refills, ${allExpenses.size} expenses)")
        } catch (e: Exception) {
            Timber.e(e, "Auto backup failed")
        }
    }

    private fun saveReplacing(bytes: ByteArray) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = context.contentResolver
            findExistingUri(resolver)?.let { resolver.delete(it, null, null) }
            val cv = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, AUTO_BACKUP_FILENAME)
                put(MediaStore.Downloads.MIME_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                put(MediaStore.Downloads.IS_PENDING, 1)
            }
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, cv)
                ?: throw Exception("Could not create backup file")
            resolver.openOutputStream(uri)?.use { it.write(bytes) }
                ?: throw Exception("Could not write backup file")
            cv.clear()
            cv.put(MediaStore.Downloads.IS_PENDING, 0)
            resolver.update(uri, cv, null, null)
        } else {
            @Suppress("DEPRECATION")
            File(
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                AUTO_BACKUP_FILENAME
            ).writeBytes(bytes)
        }
    }

    private fun findExistingUri(resolver: android.content.ContentResolver): Uri? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return null
        val projection = arrayOf(MediaStore.Downloads._ID)
        val selection = "${MediaStore.Downloads.DISPLAY_NAME} = ?"
        return resolver.query(
            MediaStore.Downloads.EXTERNAL_CONTENT_URI,
            projection, selection, arrayOf(AUTO_BACKUP_FILENAME), null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Downloads._ID))
                Uri.withAppendedPath(MediaStore.Downloads.EXTERNAL_CONTENT_URI, id.toString())
            } else null
        }
    }
}
