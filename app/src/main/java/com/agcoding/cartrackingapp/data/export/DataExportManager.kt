package com.agcoding.cartrackingapp.data.export

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.agcoding.cartrackingapp.BuildConfig
import com.agcoding.cartrackingapp.domain.repository.CarRepository
import com.agcoding.cartrackingapp.domain.repository.ExpenseRepository
import com.agcoding.cartrackingapp.domain.repository.RefillRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

sealed class ExportResult {
    data class Success(val filePath: String) : ExportResult()
    data class Error(val message: String) : ExportResult()
}

sealed class ImportResult {
    data class Success(val carsImported: Int, val refillsImported: Int, val expensesImported: Int) : ImportResult()
    data class Error(val message: String) : ImportResult()
}

@Singleton
class DataExportManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val carRepository: CarRepository,
    private val refillRepository: RefillRepository,
    private val expenseRepository: ExpenseRepository
) {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    /**
     * Export all app data to a JSON file in the Downloads folder
     */
    suspend fun exportData(): ExportResult = withContext(Dispatchers.IO) {
        try {
            // Collect all data
            val cars = carRepository.getAllCars().first()
            val allRefills = mutableListOf<com.agcoding.cartrackingapp.domain.model.FuelRefill>()
            val allExpenses = mutableListOf<com.agcoding.cartrackingapp.domain.model.Expense>()

            // Get refills and expenses for each car
            for (car in cars) {
                val refills = refillRepository.getRefillsByCarId(car.id).first()
                allRefills.addAll(refills)

                val expenses = expenseRepository.getExpensesByCarId(car.id).first()
                allExpenses.addAll(expenses)
            }

            // Create export object
            val exportData = AppDataExport(
                schemaVersion = EXPORT_SCHEMA_VERSION,
                exportDate = System.currentTimeMillis(),
                appVersion = BuildConfig.VERSION_NAME,
                data = ExportedData(
                    cars = cars.map { it.toExported() },
                    refills = allRefills.map { it.toExported() },
                    expenses = allExpenses.map { it.toExported() }
                )
            )

            // Convert to JSON
            val jsonString = json.encodeToString(exportData)

            // Generate filename with timestamp
            val timestamp = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault()).format(Date())
            val fileName = "car_expenses_backup_$timestamp.json"

            // Save to Downloads folder
            val filePath = saveToDownloads(fileName, jsonString)

            ExportResult.Success(filePath)
        } catch (e: Exception) {
            ExportResult.Error(e.message ?: "Unknown error during export")
        }
    }

    /**
     * Import data from a JSON file URI
     * This will REPLACE all existing data with the imported data
     */
    suspend fun importData(uri: Uri): ImportResult = withContext(Dispatchers.IO) {
        try {
            // Read file content
            val inputStream: InputStream = context.contentResolver.openInputStream(uri)
                ?: return@withContext ImportResult.Error("Could not open file")

            val jsonString = inputStream.bufferedReader().use { it.readText() }
            inputStream.close()

            // Parse JSON
            val exportData = try {
                json.decodeFromString<AppDataExport>(jsonString)
            } catch (e: Exception) {
                return@withContext ImportResult.Error("Invalid file format: ${e.message}")
            }

            // Validate schema version
            if (exportData.schemaVersion > EXPORT_SCHEMA_VERSION) {
                return@withContext ImportResult.Error(
                    "This backup was created with a newer version of the app. " +
                    "Please update the app to import this file."
                )
            }

            // Clear existing data first (REPLACE mode)
            clearAllData()

            // Import cars first (to establish IDs)
            val oldToNewCarIdMap = mutableMapOf<Long, Long>()
            for (exportedCar in exportData.data.cars) {
                val car = exportedCar.toDomain().copy(id = 0) // Reset ID for insertion
                val newId = carRepository.insertCar(car)
                oldToNewCarIdMap[exportedCar.id] = newId
            }

            // Import refills with updated car IDs
            var refillsImported = 0
            for (exportedRefill in exportData.data.refills) {
                val newCarId = oldToNewCarIdMap[exportedRefill.carId]
                if (newCarId != null) {
                    val refill = exportedRefill.toDomain().copy(
                        id = 0, // Reset ID for insertion
                        carId = newCarId
                    )
                    refillRepository.insertRefill(refill)
                    refillsImported++
                }
            }

            // Import expenses with updated car IDs
            var expensesImported = 0
            for (exportedExpense in exportData.data.expenses) {
                val newCarId = oldToNewCarIdMap[exportedExpense.carId]
                if (newCarId != null) {
                    val expense = exportedExpense.toDomain().copy(
                        id = 0, // Reset ID for insertion
                        carId = newCarId
                    )
                    expenseRepository.insertExpense(expense)
                    expensesImported++
                }
            }

            ImportResult.Success(
                carsImported = exportData.data.cars.size,
                refillsImported = refillsImported,
                expensesImported = expensesImported
            )
        } catch (e: Exception) {
            ImportResult.Error(e.message ?: "Unknown error during import")
        }
    }

    /**
     * Clear all existing data from the database
     */
    suspend fun clearAllData() {
        // Get all cars and delete them (cascade should handle refills and expenses)
        val cars = carRepository.getAllCars().first()
        for (car in cars) {
            carRepository.deleteCar(car.id)
        }
    }

    /**
     * Save content to Downloads folder using MediaStore (for Android 10+) or direct file access
     */
    private fun saveToDownloads(fileName: String, content: String): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10+ - Use MediaStore
            val contentValues = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                put(MediaStore.Downloads.MIME_TYPE, "application/json")
                put(MediaStore.Downloads.IS_PENDING, 1)
            }

            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                ?: throw Exception("Could not create file in Downloads")

            resolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(content.toByteArray())
            } ?: throw Exception("Could not write to file")

            contentValues.clear()
            contentValues.put(MediaStore.Downloads.IS_PENDING, 0)
            resolver.update(uri, contentValues, null, null)

            "Downloads/$fileName"
        } else {
            // Android 9 and below - Direct file access
            @Suppress("DEPRECATION")
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadsDir, fileName)
            file.writeText(content)
            file.absolutePath
        }
    }
}

