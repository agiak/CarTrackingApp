package com.agcoding.cartrackingapp.data.export

import android.content.ContentResolver
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.agcoding.cartrackingapp.BuildConfig
import com.agcoding.cartrackingapp.domain.model.Car
import com.agcoding.cartrackingapp.domain.model.Expense
import com.agcoding.cartrackingapp.domain.model.FuelRefill
import com.agcoding.cartrackingapp.domain.repository.CarRepository
import com.agcoding.cartrackingapp.data.preferences.DataMetadataPreferences
import com.agcoding.cartrackingapp.domain.repository.ExpenseRepository
import com.agcoding.cartrackingapp.domain.repository.RefillRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.xssf.usermodel.XSSFCellStyle
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.ByteArrayOutputStream
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
    private val expenseRepository: ExpenseRepository,
    private val dataMetadataPreferences: DataMetadataPreferences
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
                lastDataModifiedAt = dataMetadataPreferences.lastDataModifiedAt.first(),
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
     * Export all app data to an Excel (.xlsx) file in the Downloads folder.
     * Creates three sheets: Cars, Fuel Refills, Expenses.
     */
    suspend fun exportToExcel(): ExportResult = withContext(Dispatchers.IO) {
        try {
            val cars = carRepository.getAllCars().first()
            val allRefills = mutableListOf<FuelRefill>()
            val allExpenses = mutableListOf<Expense>()
            for (car in cars) {
                allRefills.addAll(refillRepository.getRefillsByCarId(car.id).first())
                allExpenses.addAll(expenseRepository.getExpensesByCarId(car.id).first())
            }

            val lastModified = dataMetadataPreferences.lastDataModifiedAt.first()
            val bytes = buildExcelBytes(cars, allRefills, allExpenses, lastDataModifiedAt = lastModified)
            // Stable file name so each export REPLACES the previous one in Downloads
            // instead of creating a new timestamped copy every time.
            val fileName = "car_expenses.xlsx"
            val filePath = saveToDownloadsAsBytes(
                fileName, bytes,
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            )
            ExportResult.Success(filePath)
        } catch (e: Exception) {
            ExportResult.Error(e.message ?: "Unknown error during Excel export")
        }
    }

    internal fun buildExcelBytes(
        cars: List<Car>,
        refills: List<FuelRefill>,
        expenses: List<Expense>,
        customCategories: List<String> = emptyList(),
        lastDataModifiedAt: Long? = null
    ): ByteArray {
        val carMap = cars.associateBy { it.id }
        val workbook = XSSFWorkbook()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val headerStyle = (workbook.createCellStyle() as XSSFCellStyle).apply {
            fillForegroundColor = IndexedColors.GREY_25_PERCENT.index
            fillPattern = FillPatternType.SOLID_FOREGROUND
            setFont(workbook.createFont().apply { bold = true })
        }
        buildInfoSheet(workbook, headerStyle, dateTimeFormat, lastDataModifiedAt)
        buildCarsSheet(workbook, cars, headerStyle, dateFormat)
        buildRefillsSheet(workbook, refills, carMap, headerStyle, dateFormat)
        buildExpensesSheet(workbook, expenses, carMap, headerStyle, dateFormat)
        if (customCategories.isNotEmpty()) buildCategoriesSheet(workbook, customCategories, headerStyle)
        return ByteArrayOutputStream().also { workbook.write(it) }.toByteArray()
            .also { workbook.close() }
    }

    /**
     * A small "Info" sheet at the front of the workbook with export metadata,
     * including when the data was last modified.
     */
    private fun buildInfoSheet(
        workbook: XSSFWorkbook,
        headerStyle: XSSFCellStyle,
        dateTimeFormat: SimpleDateFormat,
        lastDataModifiedAt: Long?
    ) {
        val sheet = workbook.createSheet("Info")
        val headerRow = sheet.createRow(0)
        listOf("field", "value").forEachIndexed { i, title ->
            headerRow.createCell(i).also {
                it.setCellValue(title)
                it.cellStyle = headerStyle
            }
        }
        val rows = listOf(
            "app_version" to BuildConfig.VERSION_NAME,
            "exported_at" to dateTimeFormat.format(Date()),
            "last_data_modification" to (lastDataModifiedAt?.let { dateTimeFormat.format(Date(it)) } ?: "")
        )
        rows.forEachIndexed { idx, (field, value) ->
            val row = sheet.createRow(idx + 1)
            row.createCell(0).setCellValue(field)
            row.createCell(1).setCellValue(value)
        }
        sheet.setColumnWidth(0, 24 * 256)
        sheet.setColumnWidth(1, 24 * 256)
    }

    private fun buildCarsSheet(
        workbook: XSSFWorkbook,
        cars: List<Car>,
        headerStyle: XSSFCellStyle,
        dateFormat: SimpleDateFormat
    ) {
        val sheet = workbook.createSheet("Cars")
        val headers = listOf(
            "name", "license_plate", "current_odometer", "initial_odometer",
            "insurance_expiration_date", "kteo_expiration_date", "notes"
        )
        val headerRow = sheet.createRow(0)
        headers.forEachIndexed { i, title ->
            headerRow.createCell(i).also {
                it.setCellValue(title)
                it.cellStyle = headerStyle
            }
        }
        cars.forEachIndexed { rowIdx, car ->
            val row = sheet.createRow(rowIdx + 1)
            row.createCell(0).setCellValue(car.name)
            row.createCell(1).setCellValue(car.licensePlate)
            row.createCell(2).setCellValue(car.currentOdometer)
            row.createCell(3).setCellValue(car.initialOdometer)
            row.createCell(4).setCellValue(car.insuranceExpirationDate?.let { dateFormat.format(Date(it)) } ?: "")
            row.createCell(5).setCellValue(car.kteoExpirationDate?.let { dateFormat.format(Date(it)) } ?: "")
            row.createCell(6).setCellValue("")
        }
        listOf(20, 15, 18, 18, 24, 24, 20).map { it * 256 }
            .forEachIndexed { i, w -> sheet.setColumnWidth(i, w) }
    }

    private fun buildRefillsSheet(
        workbook: XSSFWorkbook,
        refills: List<FuelRefill>,
        carMap: Map<Long, Car>,
        headerStyle: XSSFCellStyle,
        dateFormat: SimpleDateFormat
    ) {
        val sheet = workbook.createSheet("Refills")
        val headers = listOf(
            "car_license_plate", "date", "amount_paid", "liters_added",
            "trip_distance", "odometer_reading", "price_per_liter", "notes"
        )
        val headerRow = sheet.createRow(0)
        headers.forEachIndexed { i, title ->
            headerRow.createCell(i).also {
                it.setCellValue(title)
                it.cellStyle = headerStyle
            }
        }
        refills.sortedBy { it.timestamp }.forEachIndexed { rowIdx, refill ->
            val car = carMap[refill.carId]
            val row = sheet.createRow(rowIdx + 1)
            row.createCell(0).setCellValue(car?.licensePlate ?: "")
            row.createCell(1).setCellValue(dateFormat.format(Date(refill.timestamp)))
            row.createCell(2).setCellValue(refill.amountPaid)
            row.createCell(3).setCellValue(refill.litersAdded)
            row.createCell(4).setCellValue(refill.tripDistance)
            row.createCell(5).setCellValue(refill.odometerReading)
            row.createCell(6).setCellValue(refill.pricePerLiter)
            row.createCell(7).setCellValue(refill.notes ?: "")
        }
        listOf(18, 12, 12, 12, 14, 18, 15, 20).map { it * 256 }
            .forEachIndexed { i, w -> sheet.setColumnWidth(i, w) }
    }

    private fun buildExpensesSheet(
        workbook: XSSFWorkbook,
        expenses: List<Expense>,
        carMap: Map<Long, Car>,
        headerStyle: XSSFCellStyle,
        dateFormat: SimpleDateFormat
    ) {
        val sheet = workbook.createSheet("Expenses")
        val headers = listOf(
            "car_license_plate", "date", "category", "amount", "notes",
            "reminder_date", "reminder_mileage"
        )
        val headerRow = sheet.createRow(0)
        headers.forEachIndexed { i, title ->
            headerRow.createCell(i).also {
                it.setCellValue(title)
                it.cellStyle = headerStyle
            }
        }
        expenses.sortedBy { it.timestamp }.forEachIndexed { rowIdx, expense ->
            val car = carMap[expense.carId]
            val row = sheet.createRow(rowIdx + 1)
            row.createCell(0).setCellValue(car?.licensePlate ?: "")
            row.createCell(1).setCellValue(dateFormat.format(Date(expense.timestamp)))
            row.createCell(2).setCellValue(expense.category)
            row.createCell(3).setCellValue(expense.amount)
            row.createCell(4).setCellValue(expense.notes ?: "")
            row.createCell(5).setCellValue(expense.reminderDate?.let { dateFormat.format(Date(it)) } ?: "")
            if (expense.reminderMileage != null && expense.reminderMileage > 0) {
                row.createCell(6).setCellValue(expense.reminderMileage.toDouble())
            } else {
                row.createCell(6).setCellValue("")
            }
        }
        listOf(18, 12, 15, 10, 22, 14, 15).map { it * 256 }
            .forEachIndexed { i, w -> sheet.setColumnWidth(i, w) }
    }

    private fun buildCategoriesSheet(
        workbook: XSSFWorkbook,
        categories: List<String>,
        headerStyle: XSSFCellStyle
    ) {
        val sheet = workbook.createSheet("Categories")
        sheet.createRow(0).createCell(0).also {
            it.setCellValue("name")
            it.cellStyle = headerStyle
        }
        categories.forEachIndexed { ri, name ->
            sheet.createRow(ri + 1).createCell(0).setCellValue(name)
        }
        sheet.setColumnWidth(0, 20 * 256)
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

    private fun saveToDownloadsAsBytes(fileName: String, bytes: ByteArray, mimeType: String): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = context.contentResolver
            // Reuse an existing Download with the same name so the export REPLACES the
            // previous file instead of MediaStore creating "car_expenses (1).xlsx", etc.
            val existingUri = findDownloadUriByName(resolver, fileName)
            val uri = existingUri ?: run {
                val contentValues = ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                    put(MediaStore.Downloads.MIME_TYPE, mimeType)
                    put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                    put(MediaStore.Downloads.IS_PENDING, 1)
                }
                resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                    ?: throw Exception("Could not create file in Downloads")
            }
            // "wt" truncates first, so overwriting a smaller file leaves no stale bytes.
            resolver.openOutputStream(uri, "wt")?.use { it.write(bytes) }
                ?: throw Exception("Could not write to file")
            if (existingUri == null) {
                val done = ContentValues().apply { put(MediaStore.Downloads.IS_PENDING, 0) }
                resolver.update(uri, done, null, null)
            }
            "Downloads/$fileName"
        } else {
            @Suppress("DEPRECATION")
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadsDir, fileName)
            file.writeBytes(bytes) // truncates/overwrites any existing file with this name
            file.absolutePath
        }
    }

    /**
     * Finds an existing file in the public Downloads collection by its display name,
     * so a re-export can overwrite it in place. Returns null if none exists.
     */
    private fun findDownloadUriByName(resolver: ContentResolver, fileName: String): Uri? {
        val collection = MediaStore.Downloads.EXTERNAL_CONTENT_URI
        val projection = arrayOf(MediaStore.Downloads._ID)
        val selection = "${MediaStore.Downloads.DISPLAY_NAME} = ?"
        val selectionArgs = arrayOf(fileName)
        resolver.query(collection, projection, selection, selectionArgs, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Downloads._ID))
                return ContentUris.withAppendedId(collection, id)
            }
        }
        return null
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

