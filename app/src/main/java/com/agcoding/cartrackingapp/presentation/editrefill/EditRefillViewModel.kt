package com.agcoding.cartrackingapp.presentation.editrefill

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agcoding.cartrackingapp.data.local.LocationProvider
import com.agcoding.cartrackingapp.domain.model.Location
import com.agcoding.cartrackingapp.domain.usecase.refill.GetRefillDetailsUseCase
import com.agcoding.cartrackingapp.domain.usecase.refill.UpdateRefillUseCase
import com.agcoding.cartrackingapp.shared.domain.result.Result
import com.agcoding.cartrackingapp.shared.ui.utils.simpleMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EditRefillUiState(
    val amountPaid: String = "",
    val litersAdded: String = "",
    val tripDistance: String = "",
    val odometerReading: String = "",
    val notes: String = "",
    val selectedDateMillis: Long = System.currentTimeMillis(),
    val location: Location? = null,
    val carId: Long = 0L,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class EditRefillViewModel @Inject constructor(
    private val getRefillDetailsUseCase: GetRefillDetailsUseCase,
    private val updateRefillUseCase: UpdateRefillUseCase,
    private val locationProvider: LocationProvider,
    @ApplicationContext private val context: Context,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val refillId: Long = savedStateHandle.get<Long>("refillId") ?: 0L

    private val _uiState = MutableStateFlow(EditRefillUiState())
    val uiState: StateFlow<EditRefillUiState> = _uiState.asStateFlow()

    private val _showDatePicker = MutableStateFlow(false)
    val showDatePicker: StateFlow<Boolean> = _showDatePicker.asStateFlow()

    init {
        loadRefillData()
    }

    private fun loadRefillData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                val details = getRefillDetailsUseCase(refillId).firstOrNull()
                if (details != null) {
                    val refill = details.refill
                    _uiState.value = EditRefillUiState(
                        amountPaid = refill.amountPaid.toString(),
                        litersAdded = refill.litersAdded.toString(),
                        tripDistance = refill.tripDistance.toString(),
                        odometerReading = refill.odometerReading.toString(),
                        notes = refill.notes ?: "",
                        selectedDateMillis = refill.timestamp,
                        location = refill.location,
                        carId = refill.carId,
                        isLoading = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = "Refill not found"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Failed to load refill"
                )
            }
        }
    }

    fun updateAmountPaid(value: String) {
        // Only allow digits and one decimal point
        if (value.isEmpty() || value.matches(Regex("^\\d*\\.?\\d*$"))) {
            _uiState.value = _uiState.value.copy(amountPaid = value, errorMessage = null)
        }
    }

    fun updateLitersAdded(value: String) {
        // Only allow digits and one decimal point
        if (value.isEmpty() || value.matches(Regex("^\\d*\\.?\\d*$"))) {
            _uiState.value = _uiState.value.copy(litersAdded = value, errorMessage = null)
        }
    }

    fun updateTripDistance(value: String) {
        // Only allow digits and one decimal point
        if (value.isEmpty() || value.matches(Regex("^\\d*\\.?\\d*$"))) {
            _uiState.value = _uiState.value.copy(tripDistance = value, errorMessage = null)
        }
    }

    fun updateOdometerReading(value: String) {
        // Only allow digits (no decimal for odometer)
        if (value.isEmpty() || value.matches(Regex("^\\d+$"))) {
            _uiState.value = _uiState.value.copy(odometerReading = value, errorMessage = null)
        }
    }

    fun updateNotes(value: String) {
        _uiState.value = _uiState.value.copy(notes = value)
    }

    fun showDatePicker() {
        _showDatePicker.value = true
    }

    fun hideDatePicker() {
        _showDatePicker.value = false
    }

    fun updateDate(dateMillis: Long) {
        _uiState.value = _uiState.value.copy(selectedDateMillis = dateMillis)
        hideDatePicker()
    }

    fun refreshLocation() {
        if (locationProvider.hasLocationPermission()) {
            viewModelScope.launch {
                val location = locationProvider.getCurrentLocation()
                _uiState.value = _uiState.value.copy(location = location)
            }
        }
    }

    fun saveRefill(onSuccess: () -> Unit) {
        val state = _uiState.value

        // Validation
        val amount = state.amountPaid.toDoubleOrNull()
        val liters = state.litersAdded.toDoubleOrNull()
        val distance = state.tripDistance.toDoubleOrNull()
        val odometer = state.odometerReading.toDoubleOrNull()

        if (amount == null || amount <= 0) {
            _uiState.value = state.copy(errorMessage = "Please enter a valid amount")
            return
        }

        if (liters == null || liters <= 0) {
            _uiState.value = state.copy(errorMessage = "Please enter valid liters")
            return
        }

        if (distance == null || distance < 0) {
            _uiState.value = state.copy(errorMessage = "Please enter valid distance")
            return
        }

        if (odometer == null || odometer < 0) {
            _uiState.value = state.copy(errorMessage = "Please enter valid odometer reading")
            return
        }

        viewModelScope.launch {
            _uiState.value = state.copy(isSaving = true, errorMessage = null)

            when (val result = updateRefillUseCase(
                refillId = refillId,
                carId = state.carId,
                amountPaid = amount,
                litersAdded = liters,
                tripDistance = distance,
                odometerReading = odometer,
                timestamp = state.selectedDateMillis,
                location = state.location,
                notes = state.notes.takeIf { it.isNotBlank() },
            )) {
                is Result.Success -> {
                    _uiState.value = state.copy(isSaving = false)
                    onSuccess()
                }
                is Result.Error -> _uiState.value = state.copy(
                    isSaving = false,
                    errorMessage = result.error.simpleMessage,
                )
            }
        }
    }
}

