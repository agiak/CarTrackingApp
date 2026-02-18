package com.agcoding.cartrackingapp.presentation.cardetails

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agcoding.cartrackingapp.domain.model.CarAttachment
import com.agcoding.cartrackingapp.domain.repository.CarRepository
import com.agcoding.cartrackingapp.domain.usecase.attachment.AddCarAttachmentUseCase
import com.agcoding.cartrackingapp.domain.usecase.attachment.DeleteCarAttachmentUseCase
import com.agcoding.cartrackingapp.domain.usecase.attachment.GetAttachmentFileUseCase
import com.agcoding.cartrackingapp.domain.usecase.attachment.GetCarAttachmentsUseCase
import com.agcoding.cartrackingapp.domain.usecase.attachment.RenameCarAttachmentUseCase
import com.agcoding.cartrackingapp.domain.usecase.car.DeleteCarUseCase
import com.agcoding.cartrackingapp.domain.usecase.car.UpdateCarUseCase
import com.agcoding.cartrackingapp.domain.usecase.statistics.GetCarStatisticsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
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
    savedStateHandle: SavedStateHandle
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

    init {
        loadCarDetails()
        loadAttachments()
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
            val odometerValue = odometer.toDoubleOrNull() ?: 0.0
            updateCarUseCase(
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
                tireInstallationDate = tireInstallationDate
            ).onSuccess {
                hideEditDialog()
                // Car details will auto-refresh through the flow
            }.onFailure {
                // Handle error - could add error state
            }
        }
    }

    fun deleteCar(onSuccess: () -> Unit) {
        viewModelScope.launch {
            deleteCarUseCase(carId).onSuccess {
                hideDeleteDialog()
                onSuccess()
            }.onFailure {
                // Handle error
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
