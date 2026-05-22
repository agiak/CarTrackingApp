package com.agcoding.cartrackingapp.data.export

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import com.agcoding.cartrackingapp.data.export.SpreadsheetImportManager.Companion.REFILL_TOKEN_REGEX
import com.agcoding.cartrackingapp.data.export.SpreadsheetImportManager.Companion.REFILL_TOKEN_SEP
import com.agcoding.cartrackingapp.domain.model.Car
import com.agcoding.cartrackingapp.domain.model.Expense
import com.agcoding.cartrackingapp.domain.model.FuelRefill
import com.agcoding.cartrackingapp.domain.model.Trip
import com.agcoding.cartrackingapp.domain.repository.CarRepository
import com.agcoding.cartrackingapp.domain.repository.ExpenseRepository
import com.agcoding.cartrackingapp.domain.repository.RefillRepository
import com.agcoding.cartrackingapp.domain.repository.TripRepository
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.agcoding.cartrackingapp.shared.domain.result.Result
import javax.inject.Inject
import javax.inject.Singleton
import timber.log.Timber

/**
 * Result of spreadsheet import operation.
 *
 * Success / PartialSuccess now include [tripsImported] and [remindersConfigured]
 * so the UI can surface a richer summary to the user.
 */
sealed class SpreadsheetImportResult {
    data class Success(
        val carsImported: Int,
        val refillsImported: Int,
        val expensesImported: Int,
        val tripsImported: Int = 0,
        val remindersConfigured: Int = 0,
        val warnings: List<String> = emptyList()
    ) : SpreadsheetImportResult()

    data class Error(val message: String) : SpreadsheetImportResult()

    data class PartialSuccess(
        val carsImported: Int,
        val refillsImported: Int,
        val expensesImported: Int,
        val tripsImported: Int = 0,
        val remindersConfigured: Int = 0,
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
 * and generating sample template files.
 *
 * ## Sheet order matters
 * When a workbook is imported the sheets are processed in this order:
 *  1. **Cars** – cars must exist before refills/expenses can reference them.
 *  2. **Refills** – refills are persisted and their generated IDs are cached by a
 *     stable lookup key (license_plate + date + amount) so the Trips sheet can
 *     reference them.
 *  3. **Expenses** – standard expense rows; may include optional reminder columns.
 *  4. **Trips** – each trip row lists a car and a comma-separated list of refill
 *     references (by lookup key); the importer resolves the IDs and calls
 *     [TripRepository.addRefillsToTrip].
 *
 * ## Backward compatibility
 * All new columns (trip_name, reminder_interval_days, reminder_interval_weeks,
 * reminder_interval_months, reminder_distance_km, trip_reference) are **optional**.
 * Files that do not contain them are imported exactly as before.
 *
 * ## Reminder handling
 * Expenses may carry reminder configuration in two mutually exclusive ways:
 * - **Time-based** – `reminder_interval_days`, `reminder_interval_weeks`, or
 *   `reminder_interval_months` specifies how many days/weeks/months after the
 *   expense date the reminder should fire. The computed epoch timestamp is stored
 *   in [Expense.reminderDate].
 * - **Distance-based** – `reminder_distance_km` specifies how many km *from now*
 *   (current odometer + offset) the reminder should fire. The target odometer value
 *   is stored in [Expense.reminderMileage].
 * - **Legacy explicit date** – the old `reminder_date` column (absolute date) is
 *   still supported and takes precedence when present.
 *
 * A non-null [Expense.reminderDate] or [Expense.reminderMileage] means a reminder
 * was configured, so [remindersConfigured] is incremented for each such expense.
 */
@Singleton
class SpreadsheetImportManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val carRepository: CarRepository,
    private val refillRepository: RefillRepository,
    private val expenseRepository: ExpenseRepository,
    private val tripRepository: TripRepository
) {
    companion object {
        // Sheet names
        const val SHEET_CARS = "Cars"
        const val SHEET_REFILLS = "Refills"
        const val SHEET_EXPENSES = "Expenses"
        const val SHEET_TRIPS = "Trips"

        // Cars column headers
        val CARS_HEADERS = listOf(
            "name", "license_plate", "current_odometer", "initial_odometer",
            "insurance_expiration_date", "kteo_expiration_date", "notes"
        )

        // Refills column headers (trip_reference is new – optional)
        val REFILLS_HEADERS = listOf(
            "car_license_plate", "date", "amount_paid", "liters_added",
            "trip_distance", "odometer_reading", "price_per_liter", "notes",
            "trip_reference"
        )

        // Expenses column headers
        // reminder_date          – existing, absolute date (backward compat)
        // reminder_mileage       – existing, absolute target odometer (backward compat)
        // reminder_interval_days / _weeks / _months – NEW time-based interval
        // reminder_distance_km   – NEW distance-based offset from current odometer
        val EXPENSES_HEADERS = listOf(
            "car_license_plate", "date", "category", "amount", "notes",
            "reminder_date", "reminder_mileage",
            "reminder_interval_days", "reminder_interval_weeks", "reminder_interval_months",
            "reminder_distance_km"
        )

        // Trips column headers (all new)
        val TRIPS_HEADERS = listOf(
            "car_license_plate", "trip_name", "notes",
            "refill_references"   // comma-separated: <license_plate>|<date>|<amount>
        )

        // Date formats to try when parsing
        private val DATE_FORMATS = listOf(
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()),
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()),
            SimpleDateFormat("MM/dd/yyyy", Locale.getDefault()),
            SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()),
            SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
        )

        /** Separator used inside a refill_references cell */
        private const val REFILL_REF_SEP = ";"
        /** Separator used within a single refill reference token.
         *  NOTE: Do NOT use this in split() directly – | is a regex metacharacter.
         *  Use [REFILL_TOKEN_REGEX] for splitting. */
        private const val REFILL_TOKEN_SEP = "|"
        /** Literal (non-regex) pattern for splitting a refill token on [REFILL_TOKEN_SEP]. */
        private val REFILL_TOKEN_REGEX = Regex("""\|""")
    }

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // ─────────────────────────────────────────────────────────────────────────
    // Public API
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Import data from an Excel or CSV file.
     * Data is **added** to existing data, not replaced.
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
     * Generate a sample Excel file with example data.
     *
     * The generated file contains four sheets:
     * - Cars
     * - Refills (including an optional trip_reference column)
     * - Expenses (including reminder columns for both time- and distance-based reminders)
     * - Trips (showing how to group refills into a trip)
     */
    suspend fun generateSampleFile(): SampleFileResult = withContext(Dispatchers.IO) {
        try {
            val workbook = XSSFWorkbook()

            createCarsSheet(workbook)
            createRefillsSheet(workbook)
            createExpensesSheet(workbook)
            createTripsSheet(workbook)

            val timestamp = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault()).format(Date())
            val fileName = "car_expenses_import_template_$timestamp.xlsx"

            val filePath = saveWorkbookToDownloads(workbook, fileName)
            workbook.close()

            SampleFileResult.Success(filePath)
        } catch (e: Exception) {
            Timber.e(e, "Failed to generate sample file")
            SampleFileResult.Error("Failed to generate sample file: ${e.message}")
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Sample sheet builders
    // ─────────────────────────────────────────────────────────────────────────

    private fun createCarsSheet(workbook: XSSFWorkbook) {
        val sheet = workbook.createSheet(SHEET_CARS)
        val headerRow = sheet.createRow(0)
        CARS_HEADERS.forEachIndexed { i, h -> headerRow.createCell(i).setCellValue(h) }

        val rows = listOf(
            listOf("Toyota Corolla", "ABC-1234", "75000", "50000", "2026-12-31", "2026-06-15", "Family car"),
            listOf("Honda Civic",    "XYZ-5678", "52000", "30000", "2026-08-20", "2026-09-10", "Work commute"),
            listOf("VW Golf",        "VWG-9012", "88000", "60000", "",           "",           "Weekend car")
        )
        rows.forEachIndexed { ri, row ->
            val r = sheet.createRow(ri + 1)
            row.forEachIndexed { ci, v -> r.createCell(ci).setCellValue(v) }
        }
        listOf(20, 15, 18, 18, 22, 22, 25).forEachIndexed { i, w -> sheet.setColumnWidth(i, w * 256) }
    }

    private fun createRefillsSheet(workbook: XSSFWorkbook) {
        val sheet = workbook.createSheet(SHEET_REFILLS)
        val headerRow = sheet.createRow(0)
        REFILLS_HEADERS.forEachIndexed { i, h -> headerRow.createCell(i).setCellValue(h) }

        // trip_reference format: <license_plate>|<date>|<amount_paid>
        // Leave empty when the refill is not part of a trip.
        val rows = listOf(
            // car_lp      date          amount  liters  dist   odo     ppl    notes          trip_reference
            listOf("ABC-1234", "2026-01-15", "65.50", "40.5", "450", "75450", "1.62", "Shell station", "ABC-1234|2026-01-15|65.50"),
            listOf("ABC-1234", "2026-01-08", "58.20", "36.0", "380", "75000", "1.62", "",              "ABC-1234|2026-01-08|58.20"),
            listOf("XYZ-5678", "2026-01-14", "45.00", "28.0", "320", "52320", "1.61", "BP station",   ""),
            listOf("VWG-9012", "2026-01-12", "72.30", "45.0", "520", "88520", "1.61", "",              "")
        )
        rows.forEachIndexed { ri, row ->
            val r = sheet.createRow(ri + 1)
            row.forEachIndexed { ci, v -> r.createCell(ci).setCellValue(v) }
        }
        listOf(18, 12, 12, 12, 14, 18, 15, 20, 28).forEachIndexed { i, w -> sheet.setColumnWidth(i, w * 256) }
    }

    private fun createExpensesSheet(workbook: XSSFWorkbook) {
        val sheet = workbook.createSheet(SHEET_EXPENSES)
        val headerRow = sheet.createRow(0)
        EXPENSES_HEADERS.forEachIndexed { i, h -> headerRow.createCell(i).setCellValue(h) }

        // Demonstrates all reminder styles:
        //  row 1 – legacy absolute reminder_date + reminder_mileage
        //  row 2 – time-based: reminder_interval_months
        //  row 3 – distance-based: reminder_distance_km
        //  row 4 – no reminder
        val rows = listOf(
            // lp       date        category     amount  notes              rem_date      rem_mi   rem_days  rem_weeks  rem_months  rem_dist_km
            listOf("ABC-1234", "2026-01-10", "Oil change",  "85.00", "Full synthetic", "2026-07-10", "80000", "",  "",  "",  ""),
            listOf("ABC-1234", "2025-11-15", "Tire change", "450.00","Winter tires",  "",           "",      "",  "",  "6", ""),
            listOf("XYZ-5678", "2026-01-05", "Big service", "320.00","60k service",   "",           "",      "",  "",  "",  "10000"),
            listOf("VWG-9012", "2025-12-20", "Insurance",   "680.00","Annual payment","2026-12-20", "",      "",  "",  "",  "")
        )
        rows.forEachIndexed { ri, row ->
            val r = sheet.createRow(ri + 1)
            row.forEachIndexed { ci, v -> r.createCell(ci).setCellValue(v) }
        }
        listOf(18, 12, 15, 10, 22, 14, 15, 14, 15, 15, 18).forEachIndexed { i, w -> sheet.setColumnWidth(i, w * 256) }
    }

    private fun createTripsSheet(workbook: XSSFWorkbook) {
        val sheet = workbook.createSheet(SHEET_TRIPS)
        val headerRow = sheet.createRow(0)
        TRIPS_HEADERS.forEachIndexed { i, h -> headerRow.createCell(i).setCellValue(h) }

        // refill_references is a semicolon-separated list of tokens:
        //   <license_plate>|<date>|<amount_paid>
        // Each token uniquely identifies a refill already defined in the Refills sheet.
        val rows = listOf(
            listOf("ABC-1234", "January Road Trip", "Two-fill weekend drive",
                "ABC-1234|2026-01-08|58.20;ABC-1234|2026-01-15|65.50")
        )
        rows.forEachIndexed { ri, row ->
            val r = sheet.createRow(ri + 1)
            row.forEachIndexed { ci, v -> r.createCell(ci).setCellValue(v) }
        }
        listOf(18, 25, 30, 50).forEachIndexed { i, w -> sheet.setColumnWidth(i, w * 256) }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Excel import
    // ─────────────────────────────────────────────────────────────────────────

    private suspend fun importFromExcel(workbook: Workbook): SpreadsheetImportResult {
        val errors = mutableListOf<String>()
        var carsImported = 0
        var refillsImported = 0
        var expensesImported = 0
        var tripsImported = 0
        var remindersConfigured = 0

        // ── 1. Cars ──────────────────────────────────────────────────────────
        val existingCars = carRepository.getAllCars().first()
        val carsByLicensePlate = existingCars.associateBy { it.licensePlate.uppercase() }
        val newCarsByLicensePlate = mutableMapOf<String, Long>()

        workbook.getSheet(SHEET_CARS)?.let { sheet ->
            val result = importCarsFromSheet(sheet)
            carsImported = result.first
            errors.addAll(result.second)

            val updatedCars = carRepository.getAllCars().first()
            updatedCars.forEach { car ->
                newCarsByLicensePlate[car.licensePlate.uppercase()] = car.id
            }
        }

        val allCarsByLicensePlate: Map<String, Long> =
            carsByLicensePlate.mapValues { it.value.id } + newCarsByLicensePlate

        // ── 2. Refills ───────────────────────────────────────────────────────
        // refillKeyToId is populated while inserting refills so the Trips sheet
        // can resolve references by the composite key  lp|date|amount.
        val refillKeyToId = mutableMapOf<String, Long>()

        workbook.getSheet(SHEET_REFILLS)?.let { sheet ->
            val result = importRefillsFromSheet(sheet, allCarsByLicensePlate, refillKeyToId)
            refillsImported = result.first
            errors.addAll(result.second)
        }

        // ── 3. Expenses ──────────────────────────────────────────────────────
        workbook.getSheet(SHEET_EXPENSES)?.let { sheet ->
            val result = importExpensesFromSheet(sheet, allCarsByLicensePlate)
            expensesImported = result.first
            remindersConfigured = result.third
            errors.addAll(result.second)
        }

        // ── 4. Trips ─────────────────────────────────────────────────────────
        workbook.getSheet(SHEET_TRIPS)?.let { sheet ->
            val result = importTripsFromSheet(sheet, allCarsByLicensePlate, refillKeyToId)
            tripsImported = result.first
            errors.addAll(result.second)
        }

        return when {
            errors.isEmpty() -> SpreadsheetImportResult.Success(
                carsImported, refillsImported, expensesImported, tripsImported, remindersConfigured
            )
            carsImported > 0 || refillsImported > 0 || expensesImported > 0 || tripsImported > 0 ->
                SpreadsheetImportResult.PartialSuccess(
                    carsImported, refillsImported, expensesImported, tripsImported, remindersConfigured, errors
                )
            else -> SpreadsheetImportResult.Error(
                "No data could be imported. Errors: ${errors.joinToString("; ")}"
            )
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Sheet-level importers
    // ─────────────────────────────────────────────────────────────────────────

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

                if (carRepository.isLicensePlateExists(licensePlate)) {
                    errors.add("Row ${rowIndex + 1}: Car with license plate '$licensePlate' already exists, skipped")
                    continue
                }

                carRepository.insertCar(
                    Car(
                        name = name,
                        licensePlate = licensePlate,
                        currentOdometer = currentOdometer,
                        initialOdometer = initialOdometer,
                        insuranceExpirationDate = insuranceDate,
                        kteoExpirationDate = kteoDate
                    )
                )
                imported++
            } catch (e: Exception) {
                errors.add("Row ${rowIndex + 1}: ${e.message}")
            }
        }
        return Pair(imported, errors)
    }

    /**
     * Import refills sheet.
     *
     * [refillKeyToId] is an **output** parameter: it is populated with
     * `"<LP_UPPER>|<yyyy-MM-dd>|<amount>" → persisted_refill_id` entries so the
     * Trips sheet importer can resolve refill references.
     */
    private suspend fun importRefillsFromSheet(
        sheet: Sheet,
        carsByLicensePlate: Map<String, Long>,
        refillKeyToId: MutableMap<String, Long>
    ): Pair<Int, List<String>> {
        val errors = mutableListOf<String>()
        var imported = 0

        val headerRow = sheet.getRow(0) ?: return Pair(0, listOf("Refills sheet has no header row"))
        val headerMap = getHeaderMap(headerRow)

        for (rowIndex in 1..sheet.lastRowNum) {
            val row = sheet.getRow(rowIndex) ?: continue
            try {
                val licensePlate = getCellString(row, headerMap["car_license_plate"])?.uppercase() ?: continue

                val carId = carsByLicensePlate[licensePlate]
                if (carId == null) {
                    errors.add("Row ${rowIndex + 1}: Car '$licensePlate' not found")
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

                val newId = refillRepository.insertRefill(refill)
                imported++

                // Register in lookup map (primary key = lp|date|amount)
                val dateKey = dateFormat.format(Date(date))
                val lookupKey = buildRefillKey(licensePlate, dateKey, amountPaid)
                refillKeyToId[lookupKey] = newId

                // Also check trip_reference column for inline assignment
                getCellString(row, headerMap["trip_reference"])?.let { ref ->
                    // We cannot assign to a trip here because the trip may not exist yet.
                    // The key is registered above and will be resolved when the Trips sheet
                    // is processed.  This column acts purely as a documentation/hint.
                }

                // Update car's odometer
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

    /**
     * Import expenses sheet.
     *
     * Returns a [Triple] of (imported count, errors, reminders configured count).
     *
     * Reminder resolution priority (first match wins):
     * 1. `reminder_date` – absolute date string → [Expense.reminderDate]
     * 2. `reminder_mileage` – absolute target odometer → [Expense.reminderMileage]
     * 3. `reminder_interval_months` / `_weeks` / `_days` → computed date offset from expense date
     * 4. `reminder_distance_km` → current car odometer + km offset → [Expense.reminderMileage]
     */
    private suspend fun importExpensesFromSheet(
        sheet: Sheet,
        carsByLicensePlate: Map<String, Long>
    ): Triple<Int, List<String>, Int> {
        val errors = mutableListOf<String>()
        var imported = 0
        var remindersConfigured = 0

        val headerRow = sheet.getRow(0) ?: return Triple(0, listOf("Expenses sheet has no header row"), 0)
        val headerMap = getHeaderMap(headerRow)

        for (rowIndex in 1..sheet.lastRowNum) {
            val row = sheet.getRow(rowIndex) ?: continue
            try {
                val licensePlate = getCellString(row, headerMap["car_license_plate"])?.uppercase() ?: continue

                val carId = carsByLicensePlate[licensePlate]
                if (carId == null) {
                    errors.add("Row ${rowIndex + 1}: Car '$licensePlate' not found")
                    continue
                }

                val date = getCellDate(row, headerMap["date"]) ?: System.currentTimeMillis()
                val category = getCellString(row, headerMap["category"]) ?: "Other"
                val amount = getCellDouble(row, headerMap["amount"]) ?: continue
                val notes = getCellString(row, headerMap["notes"])

                // ── Reminder resolution ──────────────────────────────────────
                var reminderDate: Long? = null
                var reminderMileage: Int? = null

                // 1) Legacy absolute date
                val legacyDate = getCellDate(row, headerMap["reminder_date"])
                // 2) Legacy absolute mileage
                val legacyMileage = getCellDouble(row, headerMap["reminder_mileage"])?.toInt()

                // 3) Time-based interval (months > weeks > days priority)
                val intervalMonths = getCellDouble(row, headerMap["reminder_interval_months"])?.toInt()
                val intervalWeeks = getCellDouble(row, headerMap["reminder_interval_weeks"])?.toInt()
                val intervalDays = getCellDouble(row, headerMap["reminder_interval_days"])?.toInt()

                // 4) Distance offset from current odometer
                val distanceKm = getCellDouble(row, headerMap["reminder_distance_km"])?.toInt()

                when {
                    legacyDate != null -> {
                        reminderDate = legacyDate
                    }
                    legacyMileage != null && legacyMileage > 0 -> {
                        reminderMileage = legacyMileage
                    }
                    intervalMonths != null && intervalMonths > 0 -> {
                        val cal = java.util.Calendar.getInstance()
                        cal.timeInMillis = date
                        cal.add(java.util.Calendar.MONTH, intervalMonths)
                        reminderDate = cal.timeInMillis
                    }
                    intervalWeeks != null && intervalWeeks > 0 -> {
                        val cal = java.util.Calendar.getInstance()
                        cal.timeInMillis = date
                        cal.add(java.util.Calendar.WEEK_OF_YEAR, intervalWeeks)
                        reminderDate = cal.timeInMillis
                    }
                    intervalDays != null && intervalDays > 0 -> {
                        reminderDate = date + intervalDays * 24L * 60 * 60 * 1000
                    }
                    distanceKm != null && distanceKm > 0 -> {
                        val car = carRepository.getCarById(carId).first()
                        val currentOdo = car?.currentOdometer?.toInt() ?: 0
                        reminderMileage = currentOdo + distanceKm
                    }
                }

                val hasReminder = reminderDate != null || reminderMileage != null

                val expense = Expense(
                    carId = carId,
                    category = category,
                    amount = amount,
                    timestamp = date,
                    notes = notes,
                    reminderDate = reminderDate,
                    reminderMileage = reminderMileage,
                    reminderEnabled = hasReminder,
                    reminderDismissed = false,
                    preExpiryNotificationSent = false
                )

                expenseRepository.insertExpense(expense)
                imported++
                if (hasReminder) remindersConfigured++
            } catch (e: Exception) {
                errors.add("Row ${rowIndex + 1}: ${e.message}")
            }
        }
        return Triple(imported, errors, remindersConfigured)
    }

    /**
     * Import trips sheet.
     *
     * Each row creates one [Trip] for the specified car and links the listed
     * refills.  Refills are identified by the composite key
     * `<license_plate>|<date>|<amount_paid>` produced during [importRefillsFromSheet].
     *
     * A refill that cannot be resolved is reported as a warning and skipped; the
     * trip is still created with any successfully resolved refills (provided at
     * least one resolves).
     *
     * A refill that is already assigned to another trip (non-null [FuelRefill.tripId])
     * is also skipped with a warning.
     */
    private suspend fun importTripsFromSheet(
        sheet: Sheet,
        carsByLicensePlate: Map<String, Long>,
        refillKeyToId: Map<String, Long>
    ): Pair<Int, List<String>> {
        val errors = mutableListOf<String>()
        var imported = 0

        val headerRow = sheet.getRow(0) ?: return Pair(0, listOf("Trips sheet has no header row"))
        val headerMap = getHeaderMap(headerRow)

        val now = System.currentTimeMillis()

        for (rowIndex in 1..sheet.lastRowNum) {
            val row = sheet.getRow(rowIndex) ?: continue
            try {
                val licensePlate = getCellString(row, headerMap["car_license_plate"])?.uppercase()
                if (licensePlate.isNullOrBlank()) {
                    errors.add("Trips row ${rowIndex + 1}: car_license_plate is required")
                    continue
                }

                val carId = carsByLicensePlate[licensePlate]
                if (carId == null) {
                    errors.add("Trips row ${rowIndex + 1}: Car '$licensePlate' not found")
                    continue
                }

                val tripName = getCellString(row, headerMap["trip_name"])?.takeIf { it.isNotBlank() }
                    ?: "Trip ${rowIndex}"
                val tripNotes = getCellString(row, headerMap["notes"])

                // Resolve refill references
                val refillRefsRaw = getCellString(row, headerMap["refill_references"]) ?: ""
                val resolvedRefillIds = mutableListOf<Long>()

                if (refillRefsRaw.isNotBlank()) {
                    refillRefsRaw.split(REFILL_REF_SEP)
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }
                        .forEach { token ->
                            val parts = token.split(REFILL_TOKEN_REGEX)
                            if (parts.size < 3) {
                                errors.add("Trips row ${rowIndex + 1}: invalid refill reference '$token' (expected lp|date|amount)")
                                return@forEach
                            }
                            val refLp = parts[0].trim().uppercase()
                            val refDate = parts[1].trim()
                            val refAmount = parts[2].trim().toDoubleOrNull()

                            if (refAmount == null) {
                                errors.add("Trips row ${rowIndex + 1}: invalid amount in reference '$token'")
                                return@forEach
                            }

                            val key = buildRefillKey(refLp, refDate, refAmount)
                            val refillId = refillKeyToId[key]
                            if (refillId == null) {
                                errors.add("Trips row ${rowIndex + 1}: refill '$token' not found – skipped")
                                return@forEach
                            }

                            // Check if already in a trip
                            val existingRefill = refillRepository.getRefillById(refillId).first()
                            if (existingRefill?.tripId != null) {
                                errors.add("Trips row ${rowIndex + 1}: refill '$token' already belongs to trip ${existingRefill.tripId} – skipped")
                                return@forEach
                            }

                            resolvedRefillIds.add(refillId)
                        }
                }

                // A trip with no resolvable refills is still created (matches UX behaviour)
                val trip = Trip(
                    carId = carId,
                    name = tripName,
                    description = tripNotes,
                    createdAt = now,
                    updatedAt = now
                )

                val insertResult = tripRepository.insertTrip(trip)
                when (insertResult) {
                    is Result.Success -> {
                        if (resolvedRefillIds.isNotEmpty()) {
                            when (val linkResult = tripRepository.addRefillsToTrip(insertResult.data, resolvedRefillIds)) {
                                is Result.Success -> Unit
                                is Result.Error -> errors.add("Trips row ${rowIndex + 1}: failed to link refills – ${linkResult.error}")
                            }
                        }
                        imported++
                    }
                    is Result.Error -> errors.add("Trips row ${rowIndex + 1}: failed to create trip – ${insertResult.error}")
                }
            } catch (e: Exception) {
                errors.add("Trips row ${rowIndex + 1}: ${e.message}")
            }
        }
        return Pair(imported, errors)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CSV import (legacy – Trips not yet supported in CSV)
    // ─────────────────────────────────────────────────────────────────────────

    private suspend fun importFromCSV(reader: BufferedReader): SpreadsheetImportResult {
        val lines = reader.readLines()
        if (lines.isEmpty()) return SpreadsheetImportResult.Error("CSV file is empty")

        val headers = lines[0].split(",").map { it.trim().lowercase() }

        return when {
            headers.contains("license_plate") && headers.contains("current_odometer") ->
                importCarsFromCSV(lines)
            headers.contains("liters_added") || headers.contains("trip_distance") ->
                importRefillsFromCSV(lines)
            headers.contains("category") && headers.contains("amount") ->
                importExpensesFromCSV(lines)
            else -> SpreadsheetImportResult.Error("Could not determine CSV data type from headers")
        }
    }

    private suspend fun importCarsFromCSV(lines: List<String>): SpreadsheetImportResult {
        val errors = mutableListOf<String>()
        var imported = 0
        val headers = lines[0].split(",").map { it.trim().lowercase() }
        val headerMap = headers.mapIndexed { i, h -> h to i }.toMap()

        for (lineIndex in 1 until lines.size) {
            val values = parseCSVLine(lines[lineIndex])
            try {
                val name = values.getOrNull(headerMap["name"] ?: -1)?.takeIf { it.isNotBlank() } ?: continue
                val licensePlate = values.getOrNull(headerMap["license_plate"] ?: -1)?.takeIf { it.isNotBlank() } ?: continue
                val currentOdometer = values.getOrNull(headerMap["current_odometer"] ?: -1)?.toDoubleOrNull() ?: 0.0
                val initialOdometer = values.getOrNull(headerMap["initial_odometer"] ?: -1)?.toDoubleOrNull() ?: currentOdometer

                if (carRepository.isLicensePlateExists(licensePlate)) {
                    errors.add("Line ${lineIndex + 1}: Car '$licensePlate' already exists")
                    continue
                }
                carRepository.insertCar(Car(name = name, licensePlate = licensePlate, currentOdometer = currentOdometer, initialOdometer = initialOdometer))
                imported++
            } catch (e: Exception) {
                errors.add("Line ${lineIndex + 1}: ${e.message}")
            }
        }
        return if (errors.isEmpty()) SpreadsheetImportResult.Success(imported, 0, 0)
        else SpreadsheetImportResult.PartialSuccess(imported, 0, 0, errors = errors)
    }

    private suspend fun importRefillsFromCSV(lines: List<String>): SpreadsheetImportResult {
        val errors = mutableListOf<String>()
        var imported = 0
        val headers = lines[0].split(",").map { it.trim().lowercase() }
        val headerMap = headers.mapIndexed { i, h -> h to i }.toMap()

        val existingCars = carRepository.getAllCars().first()
        val carsByLicensePlate = existingCars.associateBy { it.licensePlate.uppercase() }

        for (lineIndex in 1 until lines.size) {
            val values = parseCSVLine(lines[lineIndex])
            try {
                val licensePlate = values.getOrNull(headerMap["car_license_plate"] ?: -1)?.takeIf { it.isNotBlank() }?.uppercase() ?: continue
                val carId = carsByLicensePlate[licensePlate]?.id
                if (carId == null) { errors.add("Line ${lineIndex + 1}: Car '$licensePlate' not found"); continue }

                val amountPaid = values.getOrNull(headerMap["amount_paid"] ?: -1)?.toDoubleOrNull() ?: continue
                val litersAdded = values.getOrNull(headerMap["liters_added"] ?: -1)?.toDoubleOrNull() ?: continue
                val tripDistance = values.getOrNull(headerMap["trip_distance"] ?: -1)?.toDoubleOrNull() ?: 0.0
                val odometerReading = values.getOrNull(headerMap["odometer_reading"] ?: -1)?.toDoubleOrNull() ?: 0.0
                val dateStr = values.getOrNull(headerMap["date"] ?: -1)
                val date = parseDate(dateStr) ?: System.currentTimeMillis()
                val notes = values.getOrNull(headerMap["notes"] ?: -1)?.takeIf { it.isNotBlank() }
                val pricePerLiter = if (litersAdded > 0) amountPaid / litersAdded else 0.0
                val consumption = if (tripDistance > 0 && litersAdded > 0) (litersAdded / tripDistance) * 100 else 0.0

                refillRepository.insertRefill(FuelRefill(carId = carId, amountPaid = amountPaid, litersAdded = litersAdded, tripDistance = tripDistance, odometerReading = odometerReading, fuelConsumption = consumption, pricePerLiter = pricePerLiter, timestamp = date, notes = notes))
                imported++
            } catch (e: Exception) {
                errors.add("Line ${lineIndex + 1}: ${e.message}")
            }
        }
        return if (errors.isEmpty()) SpreadsheetImportResult.Success(0, imported, 0)
        else SpreadsheetImportResult.PartialSuccess(0, imported, 0, errors = errors)
    }

    private suspend fun importExpensesFromCSV(lines: List<String>): SpreadsheetImportResult {
        val errors = mutableListOf<String>()
        var imported = 0
        val headers = lines[0].split(",").map { it.trim().lowercase() }
        val headerMap = headers.mapIndexed { i, h -> h to i }.toMap()

        val existingCars = carRepository.getAllCars().first()
        val carsByLicensePlate = existingCars.associateBy { it.licensePlate.uppercase() }

        for (lineIndex in 1 until lines.size) {
            val values = parseCSVLine(lines[lineIndex])
            try {
                val licensePlate = values.getOrNull(headerMap["car_license_plate"] ?: -1)?.takeIf { it.isNotBlank() }?.uppercase() ?: continue
                val carId = carsByLicensePlate[licensePlate]?.id
                if (carId == null) { errors.add("Line ${lineIndex + 1}: Car '$licensePlate' not found"); continue }

                val category = values.getOrNull(headerMap["category"] ?: -1)?.takeIf { it.isNotBlank() } ?: "Other"
                val amount = values.getOrNull(headerMap["amount"] ?: -1)?.toDoubleOrNull() ?: continue
                val dateStr = values.getOrNull(headerMap["date"] ?: -1)
                val date = parseDate(dateStr) ?: System.currentTimeMillis()
                val notes = values.getOrNull(headerMap["notes"] ?: -1)?.takeIf { it.isNotBlank() }

                expenseRepository.insertExpense(Expense(carId = carId, category = category, amount = amount, timestamp = date, notes = notes))
                imported++
            } catch (e: Exception) {
                errors.add("Line ${lineIndex + 1}: ${e.message}")
            }
        }
        return if (errors.isEmpty()) SpreadsheetImportResult.Success(0, 0, imported)
        else SpreadsheetImportResult.PartialSuccess(0, 0, imported, errors = errors)
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Build a stable lookup key for a refill:
     * `<LP_UPPER>|<yyyy-MM-dd>|<amount_formatted_2dp>`
     */
    private fun buildRefillKey(licensePlate: String, dateStr: String, amount: Double): String =
        "${licensePlate.uppercase()}${REFILL_TOKEN_SEP}$dateStr${REFILL_TOKEN_SEP}${String.format(Locale.US, "%.2f", amount)}"

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
            CellType.NUMERIC -> try { cell.dateCellValue?.time } catch (e: Exception) { null }
            CellType.STRING -> parseDate(cell.stringCellValue)
            else -> null
        }
    }

    private fun parseDate(dateStr: String?): Long? {
        if (dateStr.isNullOrBlank()) return null
        for (format in DATE_FORMATS) {
            try { return format.parse(dateStr)?.time } catch (_: Exception) { }
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
                char == ',' && !inQuotes -> { result.add(current.toString().trim()); current = StringBuilder() }
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
            val contentValues = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                put(MediaStore.Downloads.MIME_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            val uri = context.contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                ?: throw Exception("Could not create file in Downloads")
            context.contentResolver.openOutputStream(uri)?.use { workbook.write(it) }
                ?: throw Exception("Could not write to file")
            "Downloads/$fileName"
        } else {
            @Suppress("DEPRECATION")
            val file = java.io.File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName)
            file.outputStream().use { workbook.write(it) }
            file.absolutePath
        }
    }
}
