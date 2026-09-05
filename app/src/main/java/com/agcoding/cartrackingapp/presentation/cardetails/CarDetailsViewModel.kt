package com.agcoding.cartrackingapp.presentation.cardetails

import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agcoding.cartrackingapp.domain.model.CarAttachment
import com.agcoding.cartrackingapp.domain.model.DateFilter
import com.agcoding.cartrackingapp.domain.model.PeriodStatistics
import com.agcoding.cartrackingapp.domain.model.filterByDate
import com.agcoding.cartrackingapp.domain.model.periodStatistics
import com.agcoding.cartrackingapp.domain.repository.CarRepository
import com.agcoding.cartrackingapp.domain.repository.TripRepository
import com.agcoding.cartrackingapp.domain.usecase.attachment.AddCarAttachmentUseCase
import com.agcoding.cartrackingapp.domain.usecase.attachment.DeleteCarAttachmentUseCase
import com.agcoding.cartrackingapp.domain.usecase.attachment.GetAttachmentFileUseCase
import com.agcoding.cartrackingapp.domain.usecase.attachment.GetCarAttachmentsUseCase
import com.agcoding.cartrackingapp.domain.usecase.attachment.RenameCarAttachmentUseCase
import com.agcoding.cartrackingapp.domain.usecase.car.DeleteCarUseCase
import com.agcoding.cartrackingapp.domain.usecase.car.UpdateCarUseCase
import com.agcoding.cartrackingapp.domain.usecase.statistics.GetCarStatisticsUseCase
import com.agcoding.cartrackingapp.domain.usecase.transaction.GetCarTransactionsUseCase
import com.agcoding.cartrackingapp.domain.usecase.trip.GetRecentTripsByCarUseCase
import com.agcoding.cartrackingapp.presentation.transactions.model.TransactionWithData
import com.agcoding.cartrackingapp.shared.domain.result.Result
import com.agcoding.cartrackingapp.util.parseLocalizedDouble
import com.agcoding.cartrackingapp.widget.QuickAddWidgetReceiver
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class CarDetailsViewModel @Inject constructor(
    private val getCarStatisticsUseCase: GetCarStatisticsUseCase,
    private val deleteCarUseCase: DeleteCarUseCase,
    private val updateCarUseCase: UpdateCarUseCase,
    private val carRepository: CarRepository,
    private val getCarAttachmentsUseCase: GetCarAttachmentsUseCase,
    private val addCarAttachmentUseCase: AddCarAttachmentUseCase,
    private val deleteCarAttachmentUseCase: DeleteCarAttachmentUseCase,
    private val renameCarAttachmentUseCase: RenameCarAttachmentUseCase,
    private val getAttachmentFileUseCase: GetAttachmentFileUseCase,
    private val getRecentTripsByCarUseCase: GetRecentTripsByCarUseCase,
    private val getCarTransactionsUseCase: GetCarTransactionsUseCase,
    private val tripRepository: TripRepository,
    savedStateHandle: SavedStateHandle,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val carId: Long = savedStateHandle.get<Long>("carId") ?: 0L

    private val _uiState = MutableStateFlow<CarDetailsUiState>(CarDetailsUiState.Loading)
    val uiState: StateFlow<CarDetailsUiState> = _uiState.asStateFlow()

    private val _showDeleteDialog = MutableStateFlow(false)
    val showDeleteDialog: StateFlow<Boolean> = _showDeleteDialog.asStateFlow()

    private val _showEditDialog = MutableStateFlow(false)
    val showEditDialog: StateFlow<Boolean> = _showEditDialog.asStateFlow()

    // Attachments state
    private val _attachments = MutableStateFlow<List<CarAttachment>>(emptyList())
    val attachments: StateFlow<List<CarAttachment>> = _attachments.asStateFlow()

    private val _attachmentToDelete = MutableStateFlow<CarAttachment?>(null)
    val attachmentToDelete: StateFlow<CarAttachment?> = _attachmentToDelete.asStateFlow()

    private val _attachmentToRename = MutableStateFlow<CarAttachment?>(null)
    val attachmentToRename: StateFlow<CarAttachment?> = _attachmentToRename.asStateFlow()

    private val _attachmentError = MutableStateFlow<String?>(null)
    val attachmentError: StateFlow<String?> = _attachmentError.asStateFlow()

    // Trips state
    private val _tripCount = MutableStateFlow(0)
    val tripCount: StateFlow<Int> = _tripCount.asStateFlow()

    private val _recentTrips = MutableStateFlow<List<com.agcoding.cartrackingapp.domain.model.Trip>>(emptyList())
    val recentTrips: StateFlow<List<com.agcoding.cartrackingapp.domain.model.Trip>> = _recentTrips.asStateFlow()

    // Map of refill ID to trip name for displaying trip badges
    private val _refillTripNames = MutableStateFlow<Map<Long, String>>(emptyMap())
    val refillTripNames: StateFlow<Map<Long, String>> = _refillTripNames.asStateFlow()

    // ---------------------------------------------------------------------
    // Unified transactions: refills and expenses in one filterable list, plus
    // statistics for whichever period the user picked.
    // ---------------------------------------------------------------------

    /** The period the screen is scoped to. Chosen from the statistics card. */
    private val _dateFilter = MutableStateFlow(DateFilter.None)
    val dateFilter: StateFlow<DateFilter> = _dateFilter.asStateFlow()

    /** Everything for this car, unfiltered — the source for the year picker and totals. */
    private val allTransactions: StateFlow<List<TransactionWithData>> =
        getCarTransactionsUseCase(carId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Years that actually have records, so the date sheet only offers useful choices. */
    val availableYears: StateFlow<List<Int>> = allTransactions
        .map { transactions -> DateFilter.availableYears(transactions.map { it.transaction.timestamp }) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /**
     * Newest first, scoped to the selected period. This screen only previews the most
     * recent entries — filtering by type and re-sorting live on the "see all" screen.
     */
    val transactions: StateFlow<List<TransactionWithData>> =
        combine(allTransactions, _dateFilter) { transactions, dateFilter ->
            transactions
                .filterByDate(dateFilter) { it.transaction.timestamp }
                .sortedByDescending { it.transaction.timestamp }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Totals for the selected period. */
    val periodStatistics: StateFlow<PeriodStatistics> =
        combine(allTransactions, _dateFilter) { transactions, dateFilter ->
            transactions.periodStatistics(dateFilter)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PeriodStatistics())

    fun setDateFilter(dateFilter: DateFilter) {
        _dateFilter.value = dateFilter.normalized
    }

    init {
        loadCarDetails()
        loadAttachments()
        loadTrips()
    }

    private fun loadCarDetails() {
        viewModelScope.launch {
            getCarStatisticsUseCase(carId)
                .catch { e ->
                    _uiState.value = CarDetailsUiState.Error(e.message ?: "Unknown error")
                }
                .collect { stats ->
                    _uiState.value = if (stats != null) {
                        CarDetailsUiState.Success(stats)
                    } else {
                        CarDetailsUiState.Error("Car not found")
                    }
                }
        }
    }

    fun showDeleteDialog() {
        _showDeleteDialog.value = true
    }

    fun hideDeleteDialog() {
        _showDeleteDialog.value = false
    }

    fun showEditDialog() {
        _showEditDialog.value = true
    }

    fun hideEditDialog() {
        _showEditDialog.value = false
    }

    fun updateCar(
        name: String,
        licensePlate: String,
        odometer: String,
        insuranceExpirationDate: Long?,
        kteoExpirationDate: Long?,
        emissionsCardExpirationDate: Long?,
        roadTaxAmount: Double?,
        roadTaxDueDate: Long?,
        lastServiceDate: Long?,
        lastTireChangeDate: Long?,
        tireBrand: String?,
        tireDimensions: String?,
        tireInstallationDate: Long?
    ) {
        viewModelScope.launch {
            val odometerValue = odometer.parseLocalizedDouble() ?: 0.0
            when (updateCarUseCase(
                carId = carId,
                name = name,
                licensePlate = licensePlate,
                currentOdometer = odometerValue,
                insuranceExpirationDate = insuranceExpirationDate,
                kteoExpirationDate = kteoExpirationDate,
                emissionsCardExpirationDate = emissionsCardExpirationDate,
                roadTaxAmount = roadTaxAmount,
                roadTaxDueDate = roadTaxDueDate,
                lastServiceDate = lastServiceDate,
                lastTireChangeDate = lastTireChangeDate,
                tireBrand = tireBrand,
                tireDimensions = tireDimensions,
                tireInstallationDate = tireInstallationDate,
            )) {
                is Result.Success -> hideEditDialog()
                is Result.Error -> Unit
            }
        }
    }

    fun deleteCar(onSuccess: () -> Unit) {
        viewModelScope.launch {
            when (deleteCarUseCase(carId)) {
                is Result.Success -> {
                    QuickAddWidgetReceiver.updateWidgets(context)
                    hideDeleteDialog()
                    onSuccess()
                }
                is Result.Error -> hideDeleteDialog()
            }
        }
    }

    fun setDefaultCar() {
        viewModelScope.launch {
            carRepository.setDefaultCar(carId)
        }
    }

    // Attachment functions

    private fun loadAttachments() {
        viewModelScope.launch {
            getCarAttachmentsUseCase(carId)
                .catch { e ->
                    _attachmentError.value = e.message ?: "Error loading attachments"
                }
                .collect { attachmentList ->
                    _attachments.value = attachmentList
                }
        }
    }

    private fun loadTrips() {
        viewModelScope.launch {
            // Load trip count
            tripRepository.getTripCountByCarId(carId)
                .catch { /* ignore errors */ }
                .collect { count ->
                    _tripCount.value = count
                }
        }

        viewModelScope.launch {
            // Load recent trips (for display)
            getRecentTripsByCarUseCase(carId, limit = 5)
                .catch { /* ignore errors */ }
                .collect { trips ->
                    _recentTrips.value = trips
                }
        }

        viewModelScope.launch {
            // Load all trips to build refill -> trip name map
            tripRepository.getTripsByCarId(carId)
                .catch { /* ignore errors */ }
                .collect { allTrips ->
                    val tripNamesMap = mutableMapOf<Long, String>()
                    allTrips.forEach { trip ->
                        trip.refills.forEach { refill ->
                            tripNamesMap[refill.id] = trip.name
                        }
                    }
                    _refillTripNames.value = tripNamesMap
                }
        }
    }

    fun addAttachment(
        uri: Uri,
        fileName: String,
        mimeType: String?,
        fileSizeBytes: Long
    ) {
        viewModelScope.launch {
            when (val result = addCarAttachmentUseCase(
                carId = carId,
                uri = uri,
                fileName = fileName,
                mimeType = mimeType,
                fileSizeBytes = fileSizeBytes
            )) {
                is AddCarAttachmentUseCase.Result.Success -> {
                    _attachmentError.value = null
                    // Attachments will auto-refresh through the flow
                }
                is AddCarAttachmentUseCase.Result.UnsupportedFileType -> {
                    _attachmentError.value = "Unsupported file type. Only PDF and images are allowed."
                }
                is AddCarAttachmentUseCase.Result.FileTooLarge -> {
                    val maxMB = result.maxSizeBytes / (1024 * 1024)
                    _attachmentError.value = "File is too large. Maximum size is ${maxMB}MB."
                }
                is AddCarAttachmentUseCase.Result.InvalidFile -> {
                    _attachmentError.value = "Invalid file."
                }
                is AddCarAttachmentUseCase.Result.Failed -> {
                    _attachmentError.value = "Failed to add attachment."
                }
            }
        }
    }

    fun showDeleteAttachmentDialog(attachment: CarAttachment) {
        _attachmentToDelete.value = attachment
    }

    fun hideDeleteAttachmentDialog() {
        _attachmentToDelete.value = null
    }

    fun deleteAttachment() {
        viewModelScope.launch {
            _attachmentToDelete.value?.let { attachment ->
                val success = deleteCarAttachmentUseCase(attachment)
                if (success) {
                    _attachmentError.value = null
                } else {
                    _attachmentError.value = "Failed to delete attachment."
                }
                hideDeleteAttachmentDialog()
            }
        }
    }

    fun showRenameAttachmentDialog(attachment: CarAttachment) {
        _attachmentToRename.value = attachment
    }

    fun hideRenameAttachmentDialog() {
        _attachmentToRename.value = null
    }

    fun renameAttachment(newFileName: String) {
        viewModelScope.launch {
            _attachmentToRename.value?.let { attachment ->
                val result = renameCarAttachmentUseCase(attachment, newFileName)
                if (result != null) {
                    _attachmentError.value = null
                } else {
                    _attachmentError.value = "Failed to rename attachment."
                }
                hideRenameAttachmentDialog()
            }
        }
    }

    suspend fun getAttachmentFile(attachment: CarAttachment): File? {
        return getAttachmentFileUseCase(attachment)
    }

    fun clearAttachmentError() {
        _attachmentError.value = null
    }
}
