package com.agcoding.cartrackingapp.presentation.cardetails

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agcoding.cartrackingapp.domain.usecase.car.DeleteCarUseCase
import com.agcoding.cartrackingapp.domain.usecase.car.UpdateCarUseCase
import com.agcoding.cartrackingapp.domain.usecase.statistics.GetCarStatisticsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CarDetailsViewModel @Inject constructor(
    private val getCarStatisticsUseCase: GetCarStatisticsUseCase,
    private val deleteCarUseCase: DeleteCarUseCase,
    private val updateCarUseCase: UpdateCarUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val carId: Long = savedStateHandle.get<Long>("carId") ?: 0L

    private val _uiState = MutableStateFlow<CarDetailsUiState>(CarDetailsUiState.Loading)
    val uiState: StateFlow<CarDetailsUiState> = _uiState.asStateFlow()

    private val _showDeleteDialog = MutableStateFlow(false)
    val showDeleteDialog: StateFlow<Boolean> = _showDeleteDialog.asStateFlow()

    private val _showEditDialog = MutableStateFlow(false)
    val showEditDialog: StateFlow<Boolean> = _showEditDialog.asStateFlow()

    init {
        loadCarDetails()
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
}


