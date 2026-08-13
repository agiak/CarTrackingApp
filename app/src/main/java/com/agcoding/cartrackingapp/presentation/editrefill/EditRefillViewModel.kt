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
import com.agcoding.cartrackingapp.util.GeocodingUtil
import com.agcoding.cartrackingapp.util.parseLocalizedDouble
import com.agcoding.cartrackingapp.util.sanitizeDecimalInput
import com.agcoding.cartrackingapp.util.sanitizeIntInput
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
    val locationName: String = "",
    val isLoadingLocationName: Boolean = false,
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
                        amountPaid = refill.amountPaid.toString().replace('.', ','),
                        litersAdded = refill.litersAdded.toString().replace('.', ','),
                        tripDistance = refill.tripDistance.toString().replace('.', ','),
                        odometerReading = refill.odometerReading.toInt().toString(),
                        notes = refill.notes ?: "",
                        selectedDateMillis = refill.timestamp,
                        location = refill.location,
                        locationName = refill.locationName ?: "",
                        carId = refill.carId,
                        isLoading = false
                    )

                    // For older refills that have coordinates but no stored name,
                    // reverse-geocode once to prefill an editable name.
                    if (refill.locationName.isNullOrBlank() && refill.location != null) {
                        geocodeCurrentLocation()
                    }
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
        _uiState.value = _uiState.value.copy(amountPaid = sanitizeDecimalInput(value), errorMessage = null)
    }

    fun updateLitersAdded(value: String) {
        _uiState.value = _uiState.value.copy(litersAdded = sanitizeDecimalInput(value), errorMessage = null)
    }

    fun updateTripDistance(value: String) {
        _uiState.value = _uiState.value.copy(tripDistance = sanitizeDecimalInput(value), errorMessage = null)
    }

    fun updateOdometerReading(value: String) {
        _uiState.value = _uiState.value.copy(odometerReading = sanitizeIntInput(value), errorMessage = null)
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

    fun updateLocationName(value: String) {
        _uiState.value = _uiState.value.copy(locationName = value)
    }

    fun refreshLocation() {
        if (locationProvider.hasLocationPermission()) {
            viewModelScope.launch {
                _uiState.value = _uiState.value.copy(isLoadingLocationName = true)
                val location = locationProvider.getCurrentLocation()
                _uiState.value = _uiState.value.copy(location = location)
                // Re-fetching GPS refreshes the suggested name too.
                if (location != null) {
                    val name = GeocodingUtil.getAddressFromLocation(
                        context, location.latitude, location.longitude
                    )
                    _uiState.value = _uiState.value.copy(locationName = name ?: _uiState.value.locationName)
                }
                _uiState.value = _uiState.value.copy(isLoadingLocationName = false)
            }
        }
    }

    private fun geocodeCurrentLocation() {
        val location = _uiState.value.location ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingLocationName = true)
            val name = GeocodingUtil.getAddressFromLocation(
                context, location.latitude, location.longitude
            )
            // Only fill if the user hasn't typed something in the meantime.
            if (_uiState.value.locationName.isBlank() && name != null) {
                _uiState.value = _uiState.value.copy(locationName = name)
            }
            _uiState.value = _uiState.value.copy(isLoadingLocationName = false)
        }
    }

    fun saveRefill(onSuccess: () -> Unit) {
        val state = _uiState.value

        // Validation
        val amount = state.amountPaid.parseLocalizedDouble()
        val liters = state.litersAdded.parseLocalizedDouble()
        val distance = state.tripDistance.parseLocalizedDouble()
        val odometer = state.odometerReading.parseLocalizedDouble()

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
                locationName = state.locationName.takeIf { it.isNotBlank() },
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

