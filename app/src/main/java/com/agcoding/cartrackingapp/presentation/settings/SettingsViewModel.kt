package com.agcoding.cartrackingapp.presentation.settings

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.agcoding.cartrackingapp.BuildConfig
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.data.export.DataExportManager
import com.agcoding.cartrackingapp.data.export.ExportResult
import com.agcoding.cartrackingapp.data.export.ImportResult
import com.agcoding.cartrackingapp.data.export.SampleFileResult
import com.agcoding.cartrackingapp.data.export.SpreadsheetImportManager
import com.agcoding.cartrackingapp.data.export.SpreadsheetImportResult
import com.agcoding.cartrackingapp.data.preferences.AppLanguage
import com.agcoding.cartrackingapp.data.preferences.AppSettings
import com.agcoding.cartrackingapp.data.preferences.AppTheme
import com.agcoding.cartrackingapp.data.preferences.ColorPalette
import com.agcoding.cartrackingapp.data.preferences.ColorPalettePreferences
import com.agcoding.cartrackingapp.data.preferences.SettingsPreferences
import com.agcoding.cartrackingapp.data.preferences.ThemePreferences
import com.agcoding.cartrackingapp.domain.model.Car
import com.agcoding.cartrackingapp.domain.model.Expense
import com.agcoding.cartrackingapp.domain.model.FuelRefill
import com.agcoding.cartrackingapp.domain.model.NotificationHistoryItem
import com.agcoding.cartrackingapp.domain.repository.CarRepository
import com.agcoding.cartrackingapp.domain.repository.ExpenseRepository
import com.agcoding.cartrackingapp.domain.repository.NotificationHistoryRepository
import com.agcoding.cartrackingapp.domain.repository.RefillRepository
import com.agcoding.cartrackingapp.domain.repository.TripRepository
import com.agcoding.cartrackingapp.shared.domain.result.Result
import com.agcoding.cartrackingapp.util.NotificationHelper
import com.agcoding.cartrackingapp.util.StorageCheckResult
import com.agcoding.cartrackingapp.util.StorageUtil
import com.agcoding.cartrackingapp.worker.ReminderCheckWorker
import dagger.hilt.android.lifecycle.HiltViewModel
import timber.log.Timber
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import kotlin.random.Random

data class StorageInfo(
    val totalSize: Long = 0L,
    val dataSize: Long = 0L,
    val cacheSize: Long = 0L
) {
    fun formatSize(bytes: Long): String {
        return when {
            bytes < 1024 -> "$bytes B"
            bytes < 1024 * 1024 -> String.format("%.1f KB", bytes / 1024.0)
            bytes < 1024 * 1024 * 1024 -> String.format("%.1f MB", bytes / (1024.0 * 1024.0))
            else -> String.format("%.2f GB", bytes / (1024.0 * 1024.0 * 1024.0))
        }
    }

    val formattedTotalSize: String get() = formatSize(totalSize)
    val formattedDataSize: String get() = formatSize(dataSize)
    val formattedCacheSize: String get() = formatSize(cacheSize)
}

data class SettingsUiState(
    val appSettings: AppSettings = AppSettings(),
    val appVersion: String = BuildConfig.VERSION_NAME,
    val storageInfo: StorageInfo = StorageInfo(),
    val isDebugMode: Boolean = BuildConfig.DEBUG,
    val isGeneratingData: Boolean = false,
    val dataGenerationSuccess: Boolean = false,
    // Export/Import state
    val isExporting: Boolean = false,
    val isExportingExcel: Boolean = false,
    val isImporting: Boolean = false,
    val exportSuccess: String? = null,
    val exportExcelSuccess: String? = null,
    val importSuccess: String? = null,
    val exportError: String? = null,
    val exportExcelError: String? = null,
    val importError: String? = null,
    // Spreadsheet import state
    val isSpreadsheetImporting: Boolean = false,
    val isGeneratingSampleFile: Boolean = false,
    val spreadsheetImportSuccess: String? = null,
    val spreadsheetImportError: String? = null,
    val sampleFileSuccess: String? = null,
    val sampleFileError: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val themePreferences: ThemePreferences,
    private val settingsPreferences: SettingsPreferences,
    val colorPalettePreferences: ColorPalettePreferences,
    private val dataExportManager: DataExportManager,
    private val spreadsheetImportManager: SpreadsheetImportManager,
    private val carRepository: CarRepository,
    private val refillRepository: RefillRepository,
    private val expenseRepository: ExpenseRepository,
    private val tripRepository: TripRepository,
    private val notificationHistoryRepository: NotificationHistoryRepository,
    private val notificationHelper: NotificationHelper,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()


    // Sample data configuration - exactly 3 cars
    // Toyota Corolla = heavy trip user (8-9 trips/year) to simulate power usage
    // Honda Civic    = moderate user (4-6 trips/year)
    // VW Golf        = light user (1-3 trips/year)
    private val sampleCars = listOf(
        SampleCarConfig("Toyota Corolla", "ABC-1234", 0.0, tripsPerYear = 8..9),
        SampleCarConfig("Honda Civic",    "XYZ-5678", 0.0, tripsPerYear = 4..6),
        SampleCarConfig("Volkswagen Golf","VWG-9012", 0.0, tripsPerYear = 1..3)
    )

    private val expenseCategories = listOf(
        "Tire change" to (200.0..600.0),
        "Oil change" to (60.0..150.0),
        "Small service" to (100.0..300.0),
        "Big service" to (400.0..1200.0),
        "Repairs" to (150.0..800.0),
        "Car wash" to (15.0..50.0),
        "Insurance" to (400.0..800.0),
        "Road tax" to (100.0..300.0),
        "Parking" to (50.0..200.0),
        "Accessories" to (30.0..250.0),
        "Battery" to (80.0..200.0),
        "Brakes" to (200.0..500.0),
        "Windshield wipers" to (20.0..60.0),
        "Air filter" to (25.0..80.0),
        "Inspection (KTEO)" to (80.0..150.0)
    )

    init {
        // Load saved settings
        viewModelScope.launch {
            settingsPreferences.settingsFlow.collect { settings ->
                _uiState.value = _uiState.value.copy(appSettings = settings)
                // Also sync theme with ThemePreferences for backward compatibility
                val isDark = when (settings.theme) {
                    AppTheme.DARK -> true
                    AppTheme.LIGHT -> false
                    AppTheme.SYSTEM -> null
                }
                themePreferences.setDarkModeOverride(isDark)
            }
        }

        // Calculate storage size
        calculateStorageSize()
    }

    private fun calculateStorageSize() {
        viewModelScope.launch {
            val storageInfo = withContext(Dispatchers.IO) {
                val dataDir = context.dataDir
                val externalCacheDir = context.externalCacheDir
                val externalFilesDir = context.getExternalFilesDir(null)

                // Calculate cache size using StorageUtil (internal cache)
                val internalCacheSize = StorageUtil.getCacheSize(context)
                val externalCacheSize = externalCacheDir?.let { getFolderSize(it) } ?: 0L
                val totalCacheSize = internalCacheSize + externalCacheSize

                // Calculate total data directory size
                val totalDataDirSize = getFolderSize(dataDir)

                // Calculate external files size
                val externalFilesSize = externalFilesDir?.let { getFolderSize(it) } ?: 0L

                // User data = total data dir - internal cache + external files
                // This gives us just the databases, preferences, and files (excluding cache)
                val userDataSize = (totalDataDirSize - internalCacheSize) + externalFilesSize

                // Total app storage = user data + cache
                val totalSize = userDataSize + totalCacheSize

                StorageInfo(
                    totalSize = totalSize,
                    dataSize = userDataSize,
                    cacheSize = totalCacheSize
                )
            }
            _uiState.value = _uiState.value.copy(storageInfo = storageInfo)
        }
    }

    private fun getFolderSize(dir: File): Long {
        var size = 0L
        if (dir.exists() && dir.isDirectory) {
            dir.listFiles()?.forEach { file ->
                size += if (file.isDirectory) {
                    getFolderSize(file)
                } else {
                    file.length()
                }
            }
        }
        return size
    }

    fun refreshStorageSize() {
        calculateStorageSize()
    }

    fun updateTheme(theme: AppTheme) {
        viewModelScope.launch {
            settingsPreferences.updateTheme(theme)
            // Also sync with ThemePreferences for backward compatibility
            val isDark = when (theme) {
                AppTheme.DARK -> true
                AppTheme.LIGHT -> false
                AppTheme.SYSTEM -> null
            }
            themePreferences.setDarkModeOverride(isDark)
        }
    }

    fun toggleDarkMode(enabled: Boolean) {
        val theme = if (enabled) AppTheme.DARK else AppTheme.LIGHT
        updateTheme(theme)
    }

    fun updateColorPalette(palette: ColorPalette) {
        viewModelScope.launch {
            colorPalettePreferences.setColorPalette(palette)
        }
    }


    fun updateLanguage(language: AppLanguage) {
        viewModelScope.launch {
            // Save the language preference first
            settingsPreferences.updateLanguage(language)

            // Apply the locale immediately - this will recreate the activity
            withContext(Dispatchers.Main) {
                val localeList = LocaleListCompat.forLanguageTags(language.code)
                AppCompatDelegate.setApplicationLocales(localeList)
            }
        }
    }

    fun updateLLMModel(model: com.agcoding.cartrackingapp.domain.model.LLMModel) {
        viewModelScope.launch {
            settingsPreferences.updateLLMModel(model)
            Timber.d("LLM model updated to: ${model.displayName} (${model.modelId})")
        }
    }

    fun updateNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsPreferences.updateNotificationsEnabled(enabled)
        }
    }

    fun updateForecastingEnabled(enabled: Boolean) {
        viewModelScope.launch {
            settingsPreferences.updateForecastingEnabled(enabled)
        }
    }

    // ==================== Data Export/Import ====================

    fun exportData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isExporting = true,
                exportSuccess = null,
                exportError = null
            )

            // Pre-flight storage check
            try {
                // Get current data counts
                val carCount = carRepository.getAllCars().first().size
                val refillCount = refillRepository.getAllRefills().first().size
                val expenseCount = expenseRepository.getAllExpenses().first().size

                // Estimate export size
                val estimatedSize =
                    StorageUtil.estimateExportSize(carCount, refillCount, expenseCount)

                // Check if sufficient storage available
                when (val storageCheck = StorageUtil.checkStorageSpace(context, estimatedSize)) {
                    is StorageCheckResult.Insufficient -> {
                        _uiState.value = _uiState.value.copy(
                            isExporting = false,
                            exportError = storageCheck.toUserMessage(context)
                        )
                        return@launch
                    }

                    is StorageCheckResult.Unavailable -> {
                        _uiState.value = _uiState.value.copy(
                            isExporting = false,
                            exportError = "Storage unavailable. Please check your device storage."
                        )
                        return@launch
                    }

                    is StorageCheckResult.Sufficient -> {
                        // Proceed with export
                    }
                }
            } catch (e: Exception) {
                // If storage check fails, log but continue (fail gracefully)
                android.util.Log.e("SettingsVM", "Storage check failed", e)
            }

            // Proceed with actual export
            when (val result = dataExportManager.exportData()) {
                is ExportResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isExporting = false,
                        exportSuccess = result.filePath
                    )
                    // Delay to allow file system to update, then refresh storage size
                    delay(300)
                    calculateStorageSize()
                }

                is ExportResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isExporting = false,
                        exportError = result.message
                    )
                }
            }
        }
    }

    fun exportToExcel() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isExportingExcel = true,
                exportExcelSuccess = null,
                exportExcelError = null
            )
            when (val result = dataExportManager.exportToExcel()) {
                is ExportResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isExportingExcel = false,
                        exportExcelSuccess = result.filePath
                    )
                }
                is ExportResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isExportingExcel = false,
                        exportExcelError = result.message
                    )
                }
            }
        }
    }

    fun importData(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isImporting = true,
                importSuccess = null,
                importError = null
            )

            when (val result = dataExportManager.importData(uri)) {
                is ImportResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isImporting = false,
                        importSuccess = "Imported ${result.carsImported} cars, ${result.refillsImported} refills, ${result.expensesImported} expenses"
                    )
                    // Delay to allow file system to update, then refresh storage size
                    delay(500)
                    calculateStorageSize()
                }

                is ImportResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isImporting = false,
                        importError = result.message
                    )
                }
            }
        }
    }

    /**
     * Unified import entry point. The user picks a single file and we route it to
     * the right importer based on its type: JSON backups go to [importData], while
     * Excel/CSV spreadsheets go to [importFromSpreadsheet]. This lets the UI expose
     * a single "Import" action instead of asking the user to pick a format first.
     */
    fun importFromFile(uri: Uri) {
        when (detectImportFileType(uri)) {
            ImportFileType.JSON -> importData(uri)
            ImportFileType.SPREADSHEET -> importFromSpreadsheet(uri)
            ImportFileType.UNKNOWN -> {
                _uiState.value = _uiState.value.copy(
                    isImporting = false,
                    isSpreadsheetImporting = false,
                    importError = context.getString(R.string.import_unsupported_file)
                )
            }
        }
    }

    private enum class ImportFileType { JSON, SPREADSHEET, UNKNOWN }

    /**
     * Detects the import file type. The file name/extension is the most reliable
     * signal (many providers report generic MIME types), so we check it first and
     * fall back to the content resolver's MIME type.
     */
    private fun detectImportFileType(uri: Uri): ImportFileType {
        val name = queryDisplayName(uri)?.lowercase()
        when {
            name == null -> Unit
            name.endsWith(".json") -> return ImportFileType.JSON
            name.endsWith(".xlsx") || name.endsWith(".xls") || name.endsWith(".csv") ->
                return ImportFileType.SPREADSHEET
        }

        val mimeType = context.contentResolver.getType(uri)?.lowercase()
        return when {
            mimeType == null -> ImportFileType.UNKNOWN
            mimeType.contains("json") -> ImportFileType.JSON
            mimeType.contains("spreadsheet") || mimeType.contains("excel") ||
                mimeType.contains("csv") || mimeType.contains("comma-separated") ->
                ImportFileType.SPREADSHEET
            else -> ImportFileType.UNKNOWN
        }
    }

    private fun queryDisplayName(uri: Uri): String? {
        return try {
            context.contentResolver.query(
                uri,
                arrayOf(OpenableColumns.DISPLAY_NAME),
                null,
                null,
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (index >= 0) cursor.getString(index) else null
                } else {
                    null
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to resolve display name for import uri")
            null
        }
    }

    fun clearAllData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isImporting = true) // Reuse for loading state

            dataExportManager.clearAllData()

            _uiState.value = _uiState.value.copy(
                isImporting = false,
                importSuccess = "All data cleared successfully"
            )
            // Delay to allow file system to update, then refresh storage size
            delay(500)
            calculateStorageSize()
        }
    }

    fun resetExportImportState() {
        _uiState.value = _uiState.value.copy(
            exportSuccess = null,
            exportExcelSuccess = null,
            exportError = null,
            exportExcelError = null,
            importSuccess = null,
            importError = null,
            spreadsheetImportSuccess = null,
            spreadsheetImportError = null,
            sampleFileSuccess = null,
            sampleFileError = null
        )
    }

    // ==================== Spreadsheet Import ====================

    fun importFromSpreadsheet(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isSpreadsheetImporting = true,
                spreadsheetImportSuccess = null,
                spreadsheetImportError = null
            )

            when (val result = spreadsheetImportManager.importFromSpreadsheet(uri)) {
                is SpreadsheetImportResult.Success -> {
                    val message = buildString {
                        append("Successfully imported: ")
                        val parts = mutableListOf<String>()
                        if (result.carsImported > 0) parts.add("${result.carsImported} cars")
                        if (result.refillsImported > 0) parts.add("${result.refillsImported} refills")
                        if (result.expensesImported > 0) parts.add("${result.expensesImported} expenses")
                        if (result.tripsImported > 0) parts.add("${result.tripsImported} trips")
                        if (result.remindersConfigured > 0) parts.add("${result.remindersConfigured} reminders")
                        append(parts.joinToString(", "))
                    }
                    _uiState.value = _uiState.value.copy(
                        isSpreadsheetImporting = false,
                        spreadsheetImportSuccess = message
                    )
                    delay(500)
                    calculateStorageSize()
                }

                is SpreadsheetImportResult.PartialSuccess -> {
                    val message = buildString {
                        append("Partially imported: ")
                        val parts = mutableListOf<String>()
                        if (result.carsImported > 0) parts.add("${result.carsImported} cars")
                        if (result.refillsImported > 0) parts.add("${result.refillsImported} refills")
                        if (result.expensesImported > 0) parts.add("${result.expensesImported} expenses")
                        if (result.tripsImported > 0) parts.add("${result.tripsImported} trips")
                        if (result.remindersConfigured > 0) parts.add("${result.remindersConfigured} reminders")
                        append(parts.joinToString(", "))
                        append(". Some rows had errors.")
                    }
                    _uiState.value = _uiState.value.copy(
                        isSpreadsheetImporting = false,
                        spreadsheetImportSuccess = message
                    )
                    delay(500)
                    calculateStorageSize()
                }

                is SpreadsheetImportResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isSpreadsheetImporting = false,
                        spreadsheetImportError = result.message
                    )
                }
            }
        }
    }

    fun generateSampleSpreadsheet() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isGeneratingSampleFile = true,
                sampleFileSuccess = null,
                sampleFileError = null
            )

            when (val result = spreadsheetImportManager.generateSampleFile()) {
                is SampleFileResult.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isGeneratingSampleFile = false,
                        sampleFileSuccess = result.filePath
                    )
                }

                is SampleFileResult.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isGeneratingSampleFile = false,
                        sampleFileError = result.message
                    )
                }
            }
        }
    }

    // ==================== Sample Data Generation ====================

    fun generateSampleData(onSuccess: () -> Unit, onError: (String) -> Unit) {
        if (!BuildConfig.DEBUG) {
            onError("Sample data generation is only available in debug mode")
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isGeneratingData = true)

                withContext(Dispatchers.IO) {
                    generateAllSampleData()
                }

                _uiState.value = _uiState.value.copy(
                    isGeneratingData = false,
                    dataGenerationSuccess = true
                )

                // Delay to allow file system to update, then refresh storage size
                delay(500)
                calculateStorageSize()

                onSuccess()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isGeneratingData = false)
                onError(e.message ?: "Failed to generate sample data")
            }
        }
    }

    private suspend fun generateAllSampleData() {
        val random = Random(System.currentTimeMillis())
        val now = System.currentTimeMillis()
        val sevenYearsAgo = now - (7L * 365 * 24 * 60 * 60 * 1000)

        for (carConfig in sampleCars) {
            // Total distance = sum of random yearly mileage (8 000 – 25 000 km/year × 7 years)
            var totalDistance = 0.0
            for (ignored in 0 until 7) {
                totalDistance += random.nextInt(8000, 25001).toDouble()
            }

            val car = Car(
                name = carConfig.name,
                licensePlate = carConfig.licensePlate,
                currentOdometer = totalDistance,
                initialOdometer = 0.0
            )
            val carId = carRepository.insertCar(car)

            // Plan trip windows first, then generate ALL refills (trip + regular) together
            generateRefillsAndTripsForCar(
                carId = carId,
                totalDistance = totalDistance,
                startTime = sevenYearsAgo,
                endTime = now,
                tripsPerYear = carConfig.tripsPerYear,
                random = random
            )

            // Generate a realistic number of expenses per car over 7 years
            // (~7–20/year). The previous 300–10 000 range produced tens of
            // thousands of expenses and multi-million total costs.
            val numberOfExpenses = random.nextInt(50, 141)
            generateExpensesForCar(
                carId = carId,
                startTime = sevenYearsAgo,
                endTime = now,
                random = random,
                targetNumberOfExpenses = numberOfExpenses,
                currentOdometer = totalDistance.toInt()
            )
        }

        // Generate sample notification history entries
        generateSampleNotifications(
            startTime = sevenYearsAgo,
            endTime = now,
            random = random
        )
    }

    /**
     * Core generation function.
     *
     * Strategy
     * ─────────
     * 1. For every calendar year in the 7-year window, decide HOW MANY trips to create and
     *    WHEN (random anchor timestamps spread across the year, kept at least 21 days apart).
     * 2. Build a flat list of ALL refill timestamps:
     *      • Trip refills: 2-4 per trip, each 2-5 days after the previous, starting at the anchor.
     *      • Regular refills: fill the remaining timeline so that every ~35-55 days there is a
     *        refill (mimicking real-world fuelling that is not part of a trip).
     * 3. Sort every timestamp, assign progressive odometer readings, insert FuelRefill rows.
     * 4. Insert Trip rows and link their refill IDs.
     *
     * This guarantees:
     *   • Every trip cluster stays within 14 days  ✓
     *   • Refills inside a trip are chronologically ordered with realistic mileage  ✓
     *   • Trips never overlap  ✓
     *   • The heavy-user car (Toyota) gets 8-9 trips/year  ✓
     */
    private suspend fun generateRefillsAndTripsForCar(
        carId: Long,
        totalDistance: Double,
        startTime: Long,
        endTime: Long,
        tripsPerYear: IntRange,
        random: Random
    ) {
        val cal = java.util.Calendar.getInstance()

        cal.timeInMillis = startTime
        val firstYear = cal.get(java.util.Calendar.YEAR)
        cal.timeInMillis = endTime
        val lastYear = cal.get(java.util.Calendar.YEAR)

        val dayMs     = 24L * 60 * 60 * 1000
        val twoWeekMs = 14L * dayMs

        val tripNamePool = listOf(
            "Road Trip", "Weekend Getaway", "Business Trip", "Holiday Drive",
            "City Tour", "Mountain Drive", "Coastal Ride", "Cross-Country Trip"
        )
        val sdf = java.text.SimpleDateFormat("d MMM yyyy", java.util.Locale.ENGLISH)

        // ── Step 1: plan trip windows ─────────────────────────────────────────
        // A "trip window" is [anchorTime .. anchorTime + (refillCount-1)*5 days]
        data class TripWindow(
            val anchorMs: Long,
            val refillCount: Int,
            val name: String,
            val description: String
        )

        val tripWindows = mutableListOf<TripWindow>()
        var tripCounter = 1

        for (year in firstYear..lastYear) {
            val targetTrips = random.nextInt(tripsPerYear.first, tripsPerYear.last + 1)

            cal.set(year, java.util.Calendar.JANUARY, 1, 0, 0, 0)
            cal.set(java.util.Calendar.MILLISECOND, 0)
            val yearStartMs = maxOf(cal.timeInMillis, startTime)

            cal.set(year, java.util.Calendar.DECEMBER, 31, 23, 59, 59)
            cal.set(java.util.Calendar.MILLISECOND, 999)
            val yearEndMs = minOf(cal.timeInMillis, endTime)

            val yearRangeMs = yearEndMs - yearStartMs
            if (yearRangeMs < twoWeekMs) continue          // not enough room

            // Divide the year into equal slots; place one trip anchor in each slot
            val slotMs = yearRangeMs / targetTrips
            var lastWindowEndMs = yearStartMs - 1L          // track previous window end

            for (slot in 0 until targetTrips) {
                val slotStart = yearStartMs + slot * slotMs
                val slotEnd   = slotStart + slotMs - 1

                // Anchor must be at least 7 days after the previous trip ended
                val earliestAnchor = maxOf(slotStart, lastWindowEndMs + 7 * dayMs)
                if (earliestAnchor + twoWeekMs > slotEnd) continue   // slot too tight

                val refillCount = random.nextInt(2, 5)                // 2–4
                val maxWindowMs = (refillCount - 1) * 5 * dayMs       // worst case: 15 days at 5d gap

                // Random anchor within the slot so the full window still fits
                val anchorRange = (slotEnd - maxWindowMs - earliestAnchor).coerceAtLeast(1L)
                val anchorMs    = earliestAnchor + random.nextLong(0, anchorRange)

                val windowEndMs = anchorMs + maxWindowMs
                val name        = "${tripNamePool[random.nextInt(tripNamePool.size)]} #$tripCounter"
                val desc        = "Generated trip with $refillCount refills from " +
                        "${sdf.format(java.util.Date(anchorMs))} to ${sdf.format(java.util.Date(windowEndMs))}"

                tripWindows.add(TripWindow(anchorMs, refillCount, name, desc))
                lastWindowEndMs = windowEndMs
                tripCounter++
            }
        }

        // ── Step 2: build ALL refill timestamps ───────────────────────────────
        // Each entry: Pair(timestamp, isTripRefill)
        // We will later stitch together the full sorted list.

        // 2a. Trip refill timestamps (key = tripWindow index → list of timestamps)
        val tripRefillTimestamps = mutableListOf<MutableList<Long>>()
        for (tw in tripWindows) {
            val timestamps = mutableListOf<Long>()
            var t = tw.anchorMs
            for (r in 0 until tw.refillCount) {
                timestamps.add(t)
                if (r < tw.refillCount - 1) {
                    // gap between successive refills: 2-5 days
                    t += (2 + random.nextInt(4)) * dayMs
                }
            }
            tripRefillTimestamps.add(timestamps)
        }

        // Flatten the set of timestamps already occupied by trips
        val occupiedTimestamps = tripRefillTimestamps.flatten().toSet()

        // 2b. Regular (non-trip) refill timestamps
        // Space them roughly every 35-55 days across the full timeline, skipping any slot
        // that falls within an active trip window (±3 days of a trip refill).
        val regularTimestamps = mutableListOf<Long>()
        var cursor = startTime
        while (cursor < endTime) {
            val gap = (35 + random.nextInt(21)) * dayMs   // 35-55 days
            cursor += gap
            if (cursor >= endTime) break

            // Skip if this timestamp is too close to any trip refill (within 3 days)
            val tooClose = occupiedTimestamps.any { kotlin.math.abs(it - cursor) < 3 * dayMs }
            if (!tooClose) {
                regularTimestamps.add(cursor)
            }
        }

        // ── Step 3: merge, sort, assign odometer, insert refills ──────────────
        // Build a unified sorted list of (timestamp, isTrip, tripWindowIdx, posInTrip)
        data class RefillSlot(
            val timestamp: Long,
            val tripWindowIdx: Int,   // -1 = regular refill
            val posInTrip: Int        // 0-based position within the trip, -1 if regular
        )

        val allSlots = mutableListOf<RefillSlot>()
        for ((idx, tsList) in tripRefillTimestamps.withIndex()) {
            for ((pos, ts) in tsList.withIndex()) {
                allSlots.add(RefillSlot(ts, idx, pos))
            }
        }
        for (ts in regularTimestamps) {
            allSlots.add(RefillSlot(ts, -1, -1))
        }
        allSlots.sortBy { it.timestamp }

        val totalSlots     = allSlots.size
        val distPerSlot    = if (totalSlots > 0) totalDistance / totalSlots else totalDistance
        var odometer       = 0.0

        // Map: tripWindowIdx → list of (insertedRefillId, posInTrip)
        val insertedTripRefills = mutableMapOf<Int, MutableList<Pair<Long, Int>>>()

        for (slot in allSlots) {
            val variance     = distPerSlot * 0.3
            val tripDistance = (distPerSlot + random.nextDouble(-variance, variance)).coerceAtLeast(50.0)
            odometer        += tripDistance

            val consumption  = random.nextDouble(5.0, 12.0)
            val liters       = (consumption * tripDistance) / 100.0
            val ppl          = random.nextDouble(1.65, 2.40)
            val amount       = liters * ppl

            val refill = FuelRefill(
                carId            = carId,
                amountPaid       = Math.round(amount       * 100) / 100.0,
                litersAdded      = Math.round(liters       * 100) / 100.0,
                tripDistance     = Math.round(tripDistance * 10 ) / 10.0,
                odometerReading  = Math.round(odometer     * 10 ) / 10.0,
                fuelConsumption  = Math.round(consumption  * 100) / 100.0,
                pricePerLiter    = Math.round(ppl          * 1000) / 1000.0,
                timestamp        = slot.timestamp,
                notes            = null
            )

            val refillId = refillRepository.insertRefill(refill)

            if (slot.tripWindowIdx >= 0) {
                insertedTripRefills
                    .getOrPut(slot.tripWindowIdx) { mutableListOf() }
                    .add(Pair(refillId, slot.posInTrip))
            }
        }

        // ── Step 4: insert Trip rows and link refill IDs ──────────────────────
        for ((idx, tw) in tripWindows.withIndex()) {
            val linkedRefills = insertedTripRefills[idx]
                ?.sortedBy { it.second }   // order by posInTrip
                ?.map { it.first }         // extract IDs
                ?: continue

            if (linkedRefills.size < 2) continue  // shouldn't happen, but guard anyway

            val trip = com.agcoding.cartrackingapp.domain.model.Trip(
                carId       = carId,
                name        = tw.name,
                description = tw.description,
                createdAt   = tw.anchorMs,
                updatedAt   = tw.anchorMs + (tw.refillCount - 1) * 5L * dayMs
            )

            when (val insertResult = tripRepository.insertTrip(trip)) {
                is Result.Success -> tripRepository.addRefillsToTrip(insertResult.data, linkedRefills)
                is Result.Error -> Timber.e("Failed to insert trip: ${insertResult.error}")
            }
        }
    }

    private suspend fun generateExpensesForCar(
        carId: Long,
        startTime: Long,
        endTime: Long,
        random: Random,
        targetNumberOfExpenses: Int,
        currentOdometer: Int
    ) {
        val timeRange = endTime - startTime
        val now = System.currentTimeMillis()

        val usedCategories = mutableSetOf<String>()
        val generatedExpenses = mutableListOf<Expense>()

        // Service categories that should have reminders
        val serviceCategories = listOf("Oil change", "Big service", "Small service", "Tire change", "Brakes")

        // First, ensure every category is used at least once (15 categories)
        for ((category, costRange) in expenseCategories) {
            val cost = random.nextDouble(costRange.start, costRange.endInclusive)
            val expenseTime = startTime + random.nextLong(0, timeRange)

            // Determine if this expense should have reminders
            val shouldHaveReminder = category in serviceCategories && random.nextBoolean()

            val reminderDate = if (shouldHaveReminder && random.nextBoolean()) {
                // Add reminder date 1-6 months in the future
                now + random.nextLong(30L * 24 * 60 * 60 * 1000, 180L * 24 * 60 * 60 * 1000)
            } else null

            val reminderMileage = if (shouldHaveReminder && random.nextBoolean()) {
                // Add reminder mileage 3,000-10,000 km in the future
                currentOdometer + random.nextInt(3000, 10001)
            } else null

            generatedExpenses.add(
                Expense(
                    carId = carId,
                    category = category,
                    amount = Math.round(cost * 100) / 100.0,
                    timestamp = expenseTime,
                    notes = null,
                    reminderDate = reminderDate,
                    reminderMileage = reminderMileage
                )
            )

            usedCategories.add(category)
        }

        // Continue adding expenses until we reach the target number
        val remainingExpenses = targetNumberOfExpenses - expenseCategories.size
        for (i in 0 until remainingExpenses) {
            // Pick a random expense category
            val (category, costRange) = expenseCategories[random.nextInt(expenseCategories.size)]

            // Random cost within the range
            val cost = random.nextDouble(costRange.start, costRange.endInclusive)

            // Random timestamp within the time range, distributed evenly
            val expenseTime = startTime + random.nextLong(0, timeRange)

            // Occasionally add reminders to service categories
            val shouldHaveReminder = category in serviceCategories && random.nextInt(100) < 30 // 30% chance

            val reminderDate = if (shouldHaveReminder && random.nextBoolean()) {
                // Add reminder date 1-6 months in the future
                now + random.nextLong(30L * 24 * 60 * 60 * 1000, 180L * 24 * 60 * 60 * 1000)
            } else null

            val reminderMileage = if (shouldHaveReminder && random.nextBoolean()) {
                // Add reminder mileage 3,000-10,000 km in the future
                currentOdometer + random.nextInt(3000, 10001)
            } else null

            generatedExpenses.add(
                Expense(
                    carId = carId,
                    category = category,
                    amount = Math.round(cost * 100) / 100.0,
                    timestamp = expenseTime,
                    notes = null,
                    reminderDate = reminderDate,
                    reminderMileage = reminderMileage
                )
            )
        }

        // Add a few guaranteed upcoming reminders for better testing
        val guaranteedReminders = listOf(
            Triple("Oil change", 7, 5000),  // 7 days, 5000 km
            Triple("Big service", 45, 8000), // 45 days, 8000 km
            Triple("Tire change", 90, 12000) // 90 days, 12000 km
        )

        guaranteedReminders.forEach { (category, daysInFuture, kmInFuture) ->
            val costRange = expenseCategories.find { it.first == category }?.second
                ?: (100.0..300.0)
            val cost = random.nextDouble(costRange.start, costRange.endInclusive)

            // Past expense timestamp (2-6 months ago)
            val pastExpenseTime = now - random.nextLong(60L * 24 * 60 * 60 * 1000, 180L * 24 * 60 * 60 * 1000)

            generatedExpenses.add(
                Expense(
                    carId = carId,
                    category = category,
                    amount = Math.round(cost * 100) / 100.0,
                    timestamp = pastExpenseTime,
                    notes = "Due for next service",
                    reminderDate = now + (daysInFuture.toLong() * 24 * 60 * 60 * 1000),
                    reminderMileage = currentOdometer + kmInFuture
                )
            )
        }

        // Add pre-expiry test cases (within notification thresholds)
        // These will trigger pre-expiry notifications
        val preExpiryTestCases = listOf(
            // Date-based: expires tomorrow (within 1 day)
            Triple("Small service", 1, null),
            // Date-based: expires in 2 days (also within 1-day threshold now)
            Triple("Brakes", 2, null),
            // Mileage-based: within 500 km
            Triple("Oil change", null, 300),
            // Mileage-based: within 2000 km (not yet eligible for 500-km threshold)
            Triple("Big service", null, 1800),
            // Both date and mileage
            Triple("Tire change", 1, 400)
        )

        preExpiryTestCases.forEach { (category, daysInFuture, kmInFuture) ->
            val costRange = expenseCategories.find { it.first == category }?.second
                ?: (100.0..300.0)
            val cost = random.nextDouble(costRange.start, costRange.endInclusive)

            // Past expense timestamp (1-3 months ago)
            val pastExpenseTime = now - random.nextLong(30L * 24 * 60 * 60 * 1000, 90L * 24 * 60 * 60 * 1000)

            generatedExpenses.add(
                Expense(
                    carId = carId,
                    category = category,
                    amount = Math.round(cost * 100) / 100.0,
                    timestamp = pastExpenseTime,
                    notes = "Pre-expiry test case",
                    reminderDate = daysInFuture?.let { now + (it.toLong() * 24 * 60 * 60 * 1000) },
                    reminderMileage = kmInFuture?.let { currentOdometer + it },
                    reminderEnabled = true,
                    preExpiryNotificationSent = false
                )
            )
        }

        // Add OVERDUE test cases – these reminders are already past due and
        // should definitely fire a notification on the very first worker run.
        val overdueTestCases = listOf(
            // Date-based: overdue by 2 days
            Triple("Insurance", -2, null),
            // Date-based: overdue by 5 hours (just barely past)
            Triple("Registration", 0, null), // 0 means "today" → will be slightly in the past by the time worker runs
            // Mileage already exceeded by 100 km
            Triple("Car wash", null, -100)
        )

        overdueTestCases.forEach { (category, daysInFuture, kmOffset) ->
            val costRange = expenseCategories.find { it.first == category }?.second
                ?: (50.0..200.0)
            val cost = random.nextDouble(costRange.start, costRange.endInclusive)
            val pastExpenseTime = now - random.nextLong(30L * 24 * 60 * 60 * 1000, 90L * 24 * 60 * 60 * 1000)

            generatedExpenses.add(
                Expense(
                    carId = carId,
                    category = category,
                    amount = Math.round(cost * 100) / 100.0,
                    timestamp = pastExpenseTime,
                    notes = "Overdue test case – should trigger immediately",
                    reminderDate = daysInFuture?.let { now + (it.toLong() * 24 * 60 * 60 * 1000) },
                    reminderMileage = kmOffset?.let { (currentOdometer + it).coerceAtLeast(0) },
                    reminderEnabled = true,
                    preExpiryNotificationSent = false
                )
            )
        }

        // Sort by timestamp and insert
        generatedExpenses.sortedBy { it.timestamp }.forEach { expense ->
            expenseRepository.insertExpense(expense)
        }
    }

    /**
     * Generates a realistic set of sample notification history entries spread across the
     * given time range. The notifications simulate real reminder events a user would have
     * received while using the app (overdue services, upcoming mileage, etc.).
     *
     * Notification types generated:
     *  - Date-based: service is due tomorrow / already overdue
     *  - Mileage-based: approaching target odometer
     *  - Combined: both date and mileage thresholds close
     *  - Test notifications (a handful, recent)
     */
    private suspend fun generateSampleNotifications(
        startTime: Long,
        endTime: Long,
        random: Random
    ) {
        val timeRange = endTime - startTime

        // Templates: Pair(title pattern, message pattern) keyed by category name
        val notificationTemplates = listOf(
            Triple("Oil Change Reminder",          "Your Oil change is due tomorrow (%s)",                         "oil"),
            Triple("Oil Change Reminder",          "Your Oil change is overdue! It was due on %s",                "oil"),
            Triple("Oil Change Reminder",          "Your Oil change is due soon (within %d km)",                  "oil_km"),
            Triple("Tire Change Reminder",         "Your Tire change is due tomorrow (%s)",                       "tire"),
            Triple("Tire Change Reminder",         "Your Tire change is overdue! It was due on %s",              "tire"),
            Triple("Tire Change Reminder",         "Your Tire change is due soon (within %d km)",                "tire_km"),
            Triple("Big Service Reminder",         "Your Big service is due tomorrow (%s)",                      "service"),
            Triple("Big Service Reminder",         "Your Big service is overdue! It was due on %s",              "service"),
            Triple("Big Service Reminder",         "Your Big service is due soon (within %d km)",                "service_km"),
            Triple("Small Service Reminder",       "Your Small service is due tomorrow (%s)",                    "small"),
            Triple("Small Service Reminder",       "Your Small service is overdue! It was due on %s",            "small"),
            Triple("Insurance Reminder",           "Your Insurance is due tomorrow (%s)",                        "insurance"),
            Triple("Insurance Reminder",           "Your Insurance renewal is overdue! It was due on %s",        "insurance"),
            Triple("Brake Inspection Reminder",    "Your Brakes are due soon (within %d km)",                    "brakes_km"),
            Triple("Brake Inspection Reminder",    "Your Brakes inspection is overdue! It was due on %s",        "brakes"),
            Triple("Inspection (KTEO) Reminder",   "Your Inspection (KTEO) is due tomorrow (%s)",                "kteo"),
            Triple("Inspection (KTEO) Reminder",   "Your Inspection (KTEO) is overdue! It was due on %s",        "kteo"),
            Triple("Battery Check Reminder",       "Your Battery check is due soon (within %d km)",              "battery_km"),
            Triple("Air Filter Reminder",          "Your Air filter replacement is due soon (within %d km)",     "airfilter_km"),
            Triple("Road Tax Reminder",            "Your Road tax payment is due tomorrow (%s)",                  "tax"),
            Triple("Road Tax Reminder",            "Your Road tax payment is overdue! It was due on %s",         "tax"),
            Triple("Test Reminder",                "This is a test notification from Developer Options.",        "test"),
            Triple("Test Reminder",                "This is a test notification from Developer Options. If you see this, the notification pipeline is working correctly!", "test")
        )

        val dateFormat = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())

        // Build a realistic set: ~40-80 entries spread across the 7-year window
        val targetCount = random.nextInt(40, 81)
        val notificationsToInsert = mutableListOf<NotificationHistoryItem>()

        repeat(targetCount) {
            val template = notificationTemplates[random.nextInt(notificationTemplates.size)]
            val (title, messageTemplate, kind) = template

            val timestamp = startTime + (random.nextLong(0, timeRange))

            // Fill in the message placeholder depending on template kind
            val message = when {
                kind.endsWith("_km") -> {
                    val km = (random.nextInt(1, 11)) * 50  // 50-500 km remaining
                    String.format(messageTemplate, km)
                }
                kind == "test" -> messageTemplate
                else -> {
                    // Date-based — use the notification's own timestamp as the "due date"
                    val dueDateOffset = (-3..3).random() * 24L * 60 * 60 * 1000  // ±3 days from notification
                    val dueDate = timestamp + dueDateOffset
                    String.format(messageTemplate, dateFormat.format(java.util.Date(dueDate)))
                }
            }

            notificationsToInsert.add(
                NotificationHistoryItem(
                    title = title,
                    description = message,
                    timestamp = timestamp
                )
            )
        }

        // Additionally, always include a few guaranteed recent entries (last 30 days)
        // so the user immediately sees something useful in the history screen.
        val recentTemplates = listOf(
            NotificationHistoryItem(
                title = "Oil Change Reminder",
                description = "Your Oil change is due tomorrow (${dateFormat.format(java.util.Date(endTime + 24L * 60 * 60 * 1000))})",
                timestamp = endTime - 2L * 24 * 60 * 60 * 1000   // 2 days ago
            ),
            NotificationHistoryItem(
                title = "Tire Change Reminder",
                description = "Your Tire change is due soon (within 300 km)",
                timestamp = endTime - 5L * 24 * 60 * 60 * 1000   // 5 days ago
            ),
            NotificationHistoryItem(
                title = "Insurance Reminder",
                description = "Your Insurance renewal is overdue! It was due on ${dateFormat.format(java.util.Date(endTime - 3L * 24 * 60 * 60 * 1000))}",
                timestamp = endTime - 1L * 24 * 60 * 60 * 1000   // yesterday
            ),
            NotificationHistoryItem(
                title = "Big Service Reminder",
                description = "Your Big service is overdue! It was due on ${dateFormat.format(java.util.Date(endTime - 7L * 24 * 60 * 60 * 1000))}",
                timestamp = endTime - 3L * 60 * 60 * 1000        // 3 hours ago
            ),
            NotificationHistoryItem(
                title = "Test Reminder",
                description = "This is a test notification from Developer Options. If you see this, the notification pipeline is working correctly!",
                timestamp = endTime - 30L * 60 * 1000             // 30 minutes ago
            )
        )

        // Insert all entries sorted by timestamp (oldest first → DB ordering is DESC anyway)
        (notificationsToInsert + recentTemplates)
            .sortedBy { it.timestamp }
            .forEach { item ->
                notificationHistoryRepository.insertNotification(item)
            }
    }

    fun resetDataGenerationSuccess() {
        _uiState.value = _uiState.value.copy(dataGenerationSuccess = false)
    }

    /**
     * Immediately fire a test notification via [NotificationHelper].
     * Does **not** go through WorkManager – the notification appears within
     * milliseconds so the developer can confirm the pipeline end-to-end.
     */
    fun sendTestNotification(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val sent = notificationHelper.showTestNotification()
        if (sent) {
            onSuccess()
        } else {
            onError("Notification permission is not granted. Please enable it in system settings.")
        }
    }

    /**
     * Manually trigger the reminder check worker for testing/debugging.
     * The worker runs in the background and will send notifications for
     * any expenses whose reminder thresholds are met.
     */
    fun triggerReminderCheck(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                Timber.d("Manually triggering reminder check worker")

                val workRequest = OneTimeWorkRequestBuilder<ReminderCheckWorker>()
                    .build()

                WorkManager.getInstance(context).enqueue(workRequest)

                onSuccess()
                Timber.d("Reminder check worker enqueued successfully")
            } catch (e: Exception) {
                Timber.e(e, "Failed to trigger reminder check")
                onError(e.message ?: "Failed to trigger reminder check")
            }
        }
    }

    /**
     * Reset the [Expense.preExpiryNotificationSent] flag on every expense that
     * has an active reminder. This allows the same reminders to fire again the
     * next time the worker runs – useful for repeated testing.
     */
    fun resetNotificationFlags(onSuccess: (Int) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val allExpenses = expenseRepository.getAllExpenses().first()
                var resetCount = 0
                allExpenses.forEach { expense ->
                    if (expense.preExpiryNotificationSent &&
                        (expense.reminderDate != null || expense.reminderMileage != null)
                    ) {
                        expenseRepository.updateExpense(
                            expense.copy(preExpiryNotificationSent = false)
                        )
                        resetCount++
                    }
                }
                Timber.d("Reset preExpiryNotificationSent on $resetCount expenses")
                onSuccess(resetCount)
            } catch (e: Exception) {
                Timber.e(e, "Failed to reset notification flags")
                onError(e.message ?: "Failed to reset notification flags")
            }
        }
    }

    private data class SampleCarConfig(
        val name: String,
        val licensePlate: String,
        val currentOdometer: Double,
        /** Target number of trips to generate per calendar year. */
        val tripsPerYear: IntRange = 1..5
    )
}

