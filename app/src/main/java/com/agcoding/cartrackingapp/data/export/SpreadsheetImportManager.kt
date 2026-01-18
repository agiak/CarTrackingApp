package com.agcoding.cartrackingapp.data.export

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.agcoding.cartrackingapp.domain.model.Car
import com.agcoding.cartrackingapp.domain.model.Expense
import com.agcoding.cartrackingapp.domain.model.FuelRefill
import com.agcoding.cartrackingapp.domain.repository.CarRepository
import com.agcoding.cartrackingapp.domain.repository.ExpenseRepository
import com.agcoding.cartrackingapp.domain.repository.RefillRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Result of spreadsheet import operation
 */
sealed class SpreadsheetImportResult {
    data class Success(
        val carsImported: Int,
        val refillsImported: Int,
        val expensesImported: Int,
        val warnings: List<String> = emptyList()
    ) : SpreadsheetImportResult()

    data class Error(val message: String) : SpreadsheetImportResult()

    data class PartialSuccess(
        val carsImported: Int,
        val refillsImported: Int,
        val expensesImported: Int,
        val errors: List<String>
    ) : SpreadsheetImportResult()
}

/**
 * Result of sample file generation
 */
sealed class SampleFileResult {
    data class Success(val filePath: String) : SampleFileResult()
    data class Error(val message: String) : SampleFileResult()
}

/**
 * Manager class for importing data from Excel/CSV spreadsheets
 * and generating sample template files
 */
@Singleton
class SpreadsheetImportManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val carRepository: CarRepository,
    private val refillRepository: RefillRepository,
    private val expenseRepository: ExpenseRepository
) {
    companion object {
        // Sheet names
        const val SHEET_CARS = "Cars"
        const val SHEET_REFILLS = "Refills"
        const val SHEET_EXPENSES = "Expenses"

        // Cars column headers
        val CARS_HEADERS = listOf(
            "name", "license_plate", "current_odometer", "initial_odometer",
            "insurance_expiration_date", "kteo_expiration_date", "notes"
        )

        // Refills column headers
        val REFILLS_HEADERS = listOf(
            "car_license_plate", "date", "amount_paid", "liters_added",
            "trip_distance", "odometer_reading", "price_per_liter", "notes"
        )

        // Expenses column headers
        val EXPENSES_HEADERS = listOf(
            "car_license_plate", "date", "category", "amount", "notes",
            "reminder_date", "reminder_mileage"
        )

        // Date formats to try when parsing
        private val DATE_FORMATS = listOf(
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()),
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()),
            SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()),
            SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()),
            SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
        )
    }

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    /**
     * Import data from an Excel or CSV file
     * Data is ADDED to existing data, not replaced
     */
    suspend fun importFromSpreadsheet(uri: Uri): SpreadsheetImportResult = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return@withContext SpreadsheetImportResult.Error("Could not open file")

            val fileName = getFileName(uri)
            val isCSV = fileName?.endsWith(".csv", ignoreCase = true) == true

            if (isCSV) {
                importFromCSV(inputStream.bufferedReader())
            } else {
                val workbook = WorkbookFactory.create(inputStream)
                importFromExcel(workbook).also {
                    workbook.close()
                }
            }
        } catch (e: Exception) {
            SpreadsheetImportResult.Error("Import failed: ${e.message}")
        }
    }

    /**
     * Generate a sample Excel file with example data
     */
    suspend fun generateSampleFile(): SampleFileResult = withContext(Dispatchers.IO) {
        try {
            val workbook = XSSFWorkbook()

            // Create Cars sheet
            createCarsSheet(workbook)

            // Create Refills sheet
            createRefillsSheet(workbook)

            // Create Expenses sheet
            createExpensesSheet(workbook)

            // Generate filename
            val timestamp = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault()).format(Date())
            val fileName = "car_expenses_import_template_$timestamp.xlsx"

            // Save to Downloads
            val filePath = saveWorkbookToDownloads(workbook, fileName)
            workbook.close()

            SampleFileResult.Success(filePath)
        } catch (e: Exception) {
            SampleFileResult.Error("Failed to generate sample file: ${e.message}")
        }
    }

    private fun createCarsSheet(workbook: XSSFWorkbook) {
        val sheet = workbook.createSheet(SHEET_CARS)

        // Create header row
        val headerRow = sheet.createRow(0)
        CARS_HEADERS.forEachIndexed { index, header ->
            headerRow.createCell(index).setCellValue(header)
        }

        // Add sample data rows
        val sampleCars = listOf(
            listOf("Toyota Corolla", "ABC-1234", "75000", "50000", "2026-12-31", "2026-06-15", "Family car"),
            listOf("Honda Civic", "XYZ-5678", "52000", "30000", "2026-08-20", "2026-09-10", "Work commute"),
            listOf("VW Golf", "VWG-9012", "88000", "60000", "", "", "Weekend car")
        )

        sampleCars.forEachIndexed { rowIndex, carData ->
            val row = sheet.createRow(rowIndex + 1)
            carData.forEachIndexed { cellIndex, value ->
                row.createCell(cellIndex).setCellValue(value)
            }
        }

        // Set column widths (in units of 1/256th of a character width)
        // autoSizeColumn is not available on Android (requires AWT)
        val columnWidths = listOf(20, 15, 18, 18, 22, 22, 25) // character widths
        columnWidths.forEachIndexed { index, width ->
            sheet.setColumnWidth(index, width * 256)
        }
    }

    private fun createRefillsSheet(workbook: XSSFWorkbook) {
        val sheet = workbook.createSheet(SHEET_REFILLS)

        // Create header row
        val headerRow = sheet.createRow(0)
        REFILLS_HEADERS.forEachIndexed { index, header ->
            headerRow.createCell(index).setCellValue(header)
        }

        // Add sample data rows
        val sampleRefills = listOf(
            listOf("ABC-1234", "2026-01-15", "65.50", "40.5", "450", "75450", "1.62", "Shell station"),
            listOf("ABC-1234", "2026-01-08", "58.20", "36.0", "380", "75000", "1.62", ""),
            listOf("XYZ-5678", "2026-01-14", "45.00", "28.0", "320", "52320", "1.61", "BP station"),
            listOf("VWG-9012", "2026-01-12", "72.30", "45.0", "520", "88520", "1.61", "")
        )

        sampleRefills.forEachIndexed { rowIndex, refillData ->
            val row = sheet.createRow(rowIndex + 1)
            refillData.forEachIndexed { cellIndex, value ->
                row.createCell(cellIndex).setCellValue(value)
            }
        }

        // Set column widths (in units of 1/256th of a character width)
        val columnWidths = listOf(18, 12, 12, 12, 14, 18, 15, 20) // character widths
        columnWidths.forEachIndexed { index, width ->
            sheet.setColumnWidth(index, width * 256)
        }
    }

    private fun createExpensesSheet(workbook: XSSFWorkbook) {
        val sheet = workbook.createSheet(SHEET_EXPENSES)

        // Create header row
        val headerRow = sheet.createRow(0)
        EXPENSES_HEADERS.forEachIndexed { index, header ->
            headerRow.createCell(index).setCellValue(header)
        }

        // Add sample data rows
        val sampleExpenses = listOf(
            listOf("ABC-1234", "2026-01-10", "Oil change", "85.00", "Full synthetic oil", "2026-07-10", "80000"),
            listOf("ABC-1234", "2025-11-15", "Tire change", "450.00", "Winter tires", "", ""),
            listOf("XYZ-5678", "2026-01-05", "Car wash", "25.00", "", "", ""),
            listOf("VWG-9012", "2025-12-20", "Insurance", "680.00", "Annual payment", "2026-12-20", "")
        )

        sampleExpenses.forEachIndexed { rowIndex, expenseData ->
            val row = sheet.createRow(rowIndex + 1)
            expenseData.forEachIndexed { cellIndex, value ->
                row.createCell(cellIndex).setCellValue(value)
            }
        }

        // Set column widths (in units of 1/256th of a character width)
        val columnWidths = listOf(18, 12, 15, 10, 25, 14, 18) // character widths
        columnWidths.forEachIndexed { index, width ->
            sheet.setColumnWidth(index, width * 256)
        }
    }

    private suspend fun importFromExcel(workbook: Workbook): SpreadsheetImportResult {
        val errors = mutableListOf<String>()
        var carsImported = 0
        var refillsImported = 0
        var expensesImported = 0

        // Get existing cars for license plate lookup
        val existingCars = carRepository.getAllCars().first()
        val carsByLicensePlate = existingCars.associateBy { it.licensePlate.uppercase() }
        val newCarsByLicensePlate = mutableMapOf<String, Long>()

        // Import Cars sheet first
        workbook.getSheet(SHEET_CARS)?.let { sheet ->
            val result = importCarsFromSheet(sheet)
            carsImported = result.first
            errors.addAll(result.second)

            // Update car lookup with newly imported cars
            val updatedCars = carRepository.getAllCars().first()
            updatedCars.forEach { car ->
                newCarsByLicensePlate[car.licensePlate.uppercase()] = car.id
            }
        }

        // Merge existing and new cars for lookup
        val allCarsByLicensePlate = carsByLicensePlate.mapValues { it.value.id } + newCarsByLicensePlate

        // Import Refills sheet
        workbook.getSheet(SHEET_REFILLS)?.let { sheet ->
            val result = importRefillsFromSheet(sheet, allCarsByLicensePlate)
            refillsImported = result.first
            errors.addAll(result.second)
        }

        // Import Expenses sheet
        workbook.getSheet(SHEET_EXPENSES)?.let { sheet ->
            val result = importExpensesFromSheet(sheet, allCarsByLicensePlate)
            expensesImported = result.first
            errors.addAll(result.second)
        }

        return if (errors.isEmpty()) {
            SpreadsheetImportResult.Success(carsImported, refillsImported, expensesImported)
        } else if (carsImported > 0 || refillsImported > 0 || expensesImported > 0) {
            SpreadsheetImportResult.PartialSuccess(carsImported, refillsImported, expensesImported, errors)
        } else {
            SpreadsheetImportResult.Error("No data could be imported. Errors: ${errors.joinToString("; ")}")
        }
    }

    private suspend fun importCarsFromSheet(sheet: Sheet): Pair<Int, List<String>> {
        val errors = mutableListOf<String>()
        var imported = 0

        val headerRow = sheet.getRow(0) ?: return Pair(0, listOf("Cars sheet has no header row"))
        val headerMap = getHeaderMap(headerRow)

        for (rowIndex in 1..sheet.lastRowNum) {
            val row = sheet.getRow(rowIndex) ?: continue

            try {
                val name = getCellString(row, headerMap["name"]) ?: continue
                val licensePlate = getCellString(row, headerMap["license_plate"]) ?: continue
                val currentOdometer = getCellDouble(row, headerMap["current_odometer"]) ?: 0.0
                val initialOdometer = getCellDouble(row, headerMap["initial_odometer"]) ?: currentOdometer
                val insuranceDate = getCellDate(row, headerMap["insurance_expiration_date"])
                val kteoDate = getCellDate(row, headerMap["kteo_expiration_date"])

                // Check if car with this license plate already exists
                if (carRepository.isLicensePlateExists(licensePlate)) {
                    errors.add("Row ${rowIndex + 1}: Car with license plate '$licensePlate' already exists, skipped")
                    continue
                }

                val car = Car(
                    name = name,
                    licensePlate = licensePlate,
                    currentOdometer = currentOdometer,
                    initialOdometer = initialOdometer,
                    insuranceExpirationDate = insuranceDate,
                    kteoExpirationDate = kteoDate
                )

                carRepository.insertCar(car)
                imported++
            } catch (e: Exception) {
                errors.add("Row ${rowIndex + 1}: ${e.message}")
            }
        }

        return Pair(imported, errors)
    }

    private suspend fun importRefillsFromSheet(
        sheet: Sheet,
        carsByLicensePlate: Map<String, Long>
    ): Pair<Int, List<String>> {
        val errors = mutableListOf<String>()
        var imported = 0

        val headerRow = sheet.getRow(0) ?: return Pair(0, listOf("Refills sheet has no header row"))
        val headerMap = getHeaderMap(headerRow)

        for (rowIndex in 1..sheet.lastRowNum) {
            val row = sheet.getRow(rowIndex) ?: continue

            try {
                val licensePlate = getCellString(row, headerMap["car_license_plate"])?.uppercase()
                    ?: continue

                val carId = carsByLicensePlate[licensePlate]
                if (carId == null) {
                    errors.add("Row ${rowIndex + 1}: Car with license plate '$licensePlate' not found")
                    continue
                }

                val date = getCellDate(row, headerMap["date"]) ?: System.currentTimeMillis()
                val amountPaid = getCellDouble(row, headerMap["amount_paid"]) ?: continue
                val litersAdded = getCellDouble(row, headerMap["liters_added"]) ?: continue
                val tripDistance = getCellDouble(row, headerMap["trip_distance"]) ?: 0.0
                val odometerReading = getCellDouble(row, headerMap["odometer_reading"]) ?: 0.0
                val pricePerLiter = getCellDouble(row, headerMap["price_per_liter"])
                    ?: if (litersAdded > 0) amountPaid / litersAdded else 0.0
                val notes = getCellString(row, headerMap["notes"])

                // Calculate fuel consumption
                val consumption = if (tripDistance > 0 && litersAdded > 0) {
                    (litersAdded / tripDistance) * 100
                } else 0.0

                val refill = FuelRefill(
                    carId = carId,
                    amountPaid = amountPaid,
                    litersAdded = litersAdded,
                    tripDistance = tripDistance,
                    odometerReading = odometerReading,
                    fuelConsumption = consumption,
                    pricePerLiter = pricePerLiter,
                    timestamp = date,
                    notes = notes
                )

                refillRepository.insertRefill(refill)
                imported++

                // Update car's odometer if this refill's odometer is higher
                val car = carRepository.getCarById(carId).first()
                if (car != null && odometerReading > car.currentOdometer) {
                    carRepository.updateCar(car.copy(currentOdometer = odometerReading))
                }
            } catch (e: Exception) {
                errors.add("Row ${rowIndex + 1}: ${e.message}")
            }
        }

        return Pair(imported, errors)
    }

    private suspend fun importExpensesFromSheet(
        sheet: Sheet,
        carsByLicensePlate: Map<String, Long>
    ): Pair<Int, List<String>> {
        val errors = mutableListOf<String>()
        var imported = 0

        val headerRow = sheet.getRow(0) ?: return Pair(0, listOf("Expenses sheet has no header row"))
        val headerMap = getHeaderMap(headerRow)

        for (rowIndex in 1..sheet.lastRowNum) {
            val row = sheet.getRow(rowIndex) ?: continue

            try {
                val licensePlate = getCellString(row, headerMap["car_license_plate"])?.uppercase()
                    ?: continue

                val carId = carsByLicensePlate[licensePlate]
                if (carId == null) {
                    errors.add("Row ${rowIndex + 1}: Car with license plate '$licensePlate' not found")
                    continue
                }

                val date = getCellDate(row, headerMap["date"]) ?: System.currentTimeMillis()
                val category = getCellString(row, headerMap["category"]) ?: "Other"
                val amount = getCellDouble(row, headerMap["amount"]) ?: continue
                val notes = getCellString(row, headerMap["notes"])
                val reminderDate = getCellDate(row, headerMap["reminder_date"])
                val reminderMileage = getCellDouble(row, headerMap["reminder_mileage"])?.toInt()

                val expense = Expense(
                    carId = carId,
                    category = category,
                    amount = amount,
                    timestamp = date,
                    notes = notes,
                    reminderDate = reminderDate,
                    reminderMileage = reminderMileage
                )

                expenseRepository.insertExpense(expense)
                imported++
            } catch (e: Exception) {
                errors.add("Row ${rowIndex + 1}: ${e.message}")
            }
        }

        return Pair(imported, errors)
    }

    private suspend fun importFromCSV(reader: BufferedReader): SpreadsheetImportResult {
        // For CSV, we'll assume it contains refills or expenses based on headers
        // This is a simplified implementation - a more robust one would detect the content type

        val lines = reader.readLines()
        if (lines.isEmpty()) {
            return SpreadsheetImportResult.Error("CSV file is empty")
        }

        val headers = lines[0].split(",").map { it.trim().lowercase() }

        // Determine what type of data this CSV contains
        return when {
            headers.contains("license_plate") && headers.contains("current_odometer") -> {
                importCarsFromCSV(lines)
            }
            headers.contains("liters_added") || headers.contains("trip_distance") -> {
                importRefillsFromCSV(lines)
            }
            headers.contains("category") && headers.contains("amount") -> {
                importExpensesFromCSV(lines)
            }
            else -> SpreadsheetImportResult.Error("Could not determine CSV data type from headers")
        }
    }

    private suspend fun importCarsFromCSV(lines: List<String>): SpreadsheetImportResult {
        val errors = mutableListOf<String>()
        var imported = 0

        val headers = lines[0].split(",").map { it.trim().lowercase() }
        val headerMap = headers.mapIndexed { index, header -> header to index }.toMap()

        for (lineIndex in 1 until lines.size) {
            val values = parseCSVLine(lines[lineIndex])

            try {
                val name = values.getOrNull(headerMap["name"] ?: -1)?.takeIf { it.isNotBlank() } ?: continue
                val licensePlate = values.getOrNull(headerMap["license_plate"] ?: -1)?.takeIf { it.isNotBlank() } ?: continue
                val currentOdometer = values.getOrNull(headerMap["current_odometer"] ?: -1)?.toDoubleOrNull() ?: 0.0
                val initialOdometer = values.getOrNull(headerMap["initial_odometer"] ?: -1)?.toDoubleOrNull() ?: currentOdometer

                if (carRepository.isLicensePlateExists(licensePlate)) {
                    errors.add("Line ${lineIndex + 1}: Car with license plate '$licensePlate' already exists")
                    continue
                }

                val car = Car(
                    name = name,
                    licensePlate = licensePlate,
                    currentOdometer = currentOdometer,
                    initialOdometer = initialOdometer
                )

                carRepository.insertCar(car)
                imported++
            } catch (e: Exception) {
                errors.add("Line ${lineIndex + 1}: ${e.message}")
            }
        }

        return if (errors.isEmpty()) {
            SpreadsheetImportResult.Success(imported, 0, 0)
        } else {
            SpreadsheetImportResult.PartialSuccess(imported, 0, 0, errors)
        }
    }

    private suspend fun importRefillsFromCSV(lines: List<String>): SpreadsheetImportResult {
        val errors = mutableListOf<String>()
        var imported = 0

        val headers = lines[0].split(",").map { it.trim().lowercase() }
        val headerMap = headers.mapIndexed { index, header -> header to index }.toMap()

        val existingCars = carRepository.getAllCars().first()
        val carsByLicensePlate = existingCars.associateBy { it.licensePlate.uppercase() }

        for (lineIndex in 1 until lines.size) {
            val values = parseCSVLine(lines[lineIndex])

            try {
                val licensePlate = values.getOrNull(headerMap["car_license_plate"] ?: -1)
                    ?.takeIf { it.isNotBlank() }?.uppercase() ?: continue

                val carId = carsByLicensePlate[licensePlate]?.id
                if (carId == null) {
                    errors.add("Line ${lineIndex + 1}: Car with license plate '$licensePlate' not found")
                    continue
                }

                val amountPaid = values.getOrNull(headerMap["amount_paid"] ?: -1)?.toDoubleOrNull() ?: continue
                val litersAdded = values.getOrNull(headerMap["liters_added"] ?: -1)?.toDoubleOrNull() ?: continue
                val tripDistance = values.getOrNull(headerMap["trip_distance"] ?: -1)?.toDoubleOrNull() ?: 0.0
                val odometerReading = values.getOrNull(headerMap["odometer_reading"] ?: -1)?.toDoubleOrNull() ?: 0.0
                val dateStr = values.getOrNull(headerMap["date"] ?: -1)
                val date = parseDate(dateStr) ?: System.currentTimeMillis()
                val notes = values.getOrNull(headerMap["notes"] ?: -1)?.takeIf { it.isNotBlank() }

                val pricePerLiter = if (litersAdded > 0) amountPaid / litersAdded else 0.0
                val consumption = if (tripDistance > 0 && litersAdded > 0) {
                    (litersAdded / tripDistance) * 100
                } else 0.0

                val refill = FuelRefill(
                    carId = carId,
                    amountPaid = amountPaid,
                    litersAdded = litersAdded,
                    tripDistance = tripDistance,
                    odometerReading = odometerReading,
                    fuelConsumption = consumption,
                    pricePerLiter = pricePerLiter,
                    timestamp = date,
                    notes = notes
                )

                refillRepository.insertRefill(refill)
                imported++
            } catch (e: Exception) {
                errors.add("Line ${lineIndex + 1}: ${e.message}")
            }
        }

        return if (errors.isEmpty()) {
            SpreadsheetImportResult.Success(0, imported, 0)
        } else {
            SpreadsheetImportResult.PartialSuccess(0, imported, 0, errors)
        }
    }

    private suspend fun importExpensesFromCSV(lines: List<String>): SpreadsheetImportResult {
        val errors = mutableListOf<String>()
        var imported = 0

        val headers = lines[0].split(",").map { it.trim().lowercase() }
        val headerMap = headers.mapIndexed { index, header -> header to index }.toMap()

        val existingCars = carRepository.getAllCars().first()
        val carsByLicensePlate = existingCars.associateBy { it.licensePlate.uppercase() }

        for (lineIndex in 1 until lines.size) {
            val values = parseCSVLine(lines[lineIndex])

            try {
                val licensePlate = values.getOrNull(headerMap["car_license_plate"] ?: -1)
                    ?.takeIf { it.isNotBlank() }?.uppercase() ?: continue

                val carId = carsByLicensePlate[licensePlate]?.id
                if (carId == null) {
                    errors.add("Line ${lineIndex + 1}: Car with license plate '$licensePlate' not found")
                    continue
                }

                val category = values.getOrNull(headerMap["category"] ?: -1)?.takeIf { it.isNotBlank() } ?: "Other"
                val amount = values.getOrNull(headerMap["amount"] ?: -1)?.toDoubleOrNull() ?: continue
                val dateStr = values.getOrNull(headerMap["date"] ?: -1)
                val date = parseDate(dateStr) ?: System.currentTimeMillis()
                val notes = values.getOrNull(headerMap["notes"] ?: -1)?.takeIf { it.isNotBlank() }

                val expense = Expense(
                    carId = carId,
                    category = category,
                    amount = amount,
                    timestamp = date,
                    notes = notes
                )

                expenseRepository.insertExpense(expense)
                imported++
            } catch (e: Exception) {
                errors.add("Line ${lineIndex + 1}: ${e.message}")
            }
        }

        return if (errors.isEmpty()) {
            SpreadsheetImportResult.Success(0, 0, imported)
        } else {
            SpreadsheetImportResult.PartialSuccess(0, 0, imported, errors)
        }
    }

    // Helper functions

    private fun getHeaderMap(row: Row): Map<String, Int> {
        val map = mutableMapOf<String, Int>()
        for (cellIndex in 0 until row.lastCellNum) {
            val cell = row.getCell(cellIndex) ?: continue
            val header = cell.stringCellValue?.lowercase()?.trim() ?: continue
            map[header] = cellIndex
        }
        return map
    }

    private fun getCellString(row: Row, index: Int?): String? {
        if (index == null) return null
        val cell = row.getCell(index) ?: return null
        return when (cell.cellType) {
            CellType.STRING -> cell.stringCellValue?.takeIf { it.isNotBlank() }
            CellType.NUMERIC -> cell.numericCellValue.toString()
            else -> null
        }
    }

    private fun getCellDouble(row: Row, index: Int?): Double? {
        if (index == null) return null
        val cell = row.getCell(index) ?: return null
        return when (cell.cellType) {
            CellType.NUMERIC -> cell.numericCellValue
            CellType.STRING -> cell.stringCellValue?.toDoubleOrNull()
            else -> null
        }
    }

    private fun getCellDate(row: Row, index: Int?): Long? {
        if (index == null) return null
        val cell = row.getCell(index) ?: return null
        return when (cell.cellType) {
            CellType.NUMERIC -> {
                try {
                    cell.dateCellValue?.time
                } catch (e: Exception) {
                    null
                }
            }
            CellType.STRING -> parseDate(cell.stringCellValue)
            else -> null
        }
    }

    private fun parseDate(dateStr: String?): Long? {
        if (dateStr.isNullOrBlank()) return null
        for (format in DATE_FORMATS) {
            try {
                return format.parse(dateStr)?.time
            } catch (e: Exception) {
                continue
            }
        }
        return null
    }

    private fun parseCSVLine(line: String): List<String> {
        val result = mutableListOf<String>()
        var current = StringBuilder()
        var inQuotes = false

        for (char in line) {
            when {
                char == '"' -> inQuotes = !inQuotes
                char == ',' && !inQuotes -> {
                    result.add(current.toString().trim())
                    current = StringBuilder()
                }
                else -> current.append(char)
            }
        }
        result.add(current.toString().trim())

        return result
    }

    private fun getFileName(uri: Uri): String? {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        return cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (nameIndex >= 0) it.getString(nameIndex) else null
            } else null
        }
    }

    private fun saveWorkbookToDownloads(workbook: Workbook, fileName: String): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10+ - use MediaStore
            val contentValues = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                put(MediaStore.Downloads.MIME_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }

            val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                ?: throw Exception("Could not create file in Downloads")

            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                workbook.write(outputStream)
            } ?: throw Exception("Could not write to file")

            "Downloads/$fileName"
        } else {
            // Legacy - direct file access
            @Suppress("DEPRECATION")
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = java.io.File(downloadsDir, fileName)
            file.outputStream().use { outputStream ->
                workbook.write(outputStream)
            }
            file.absolutePath
        }
    }
}

