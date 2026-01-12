package com.agcoding.cartrackingapp.presentation.refill

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agcoding.cartrackingapp.BuildConfig
import com.agcoding.cartrackingapp.data.local.LocationProvider
import com.agcoding.cartrackingapp.domain.usecase.refill.AddFuelRefillUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class AddRefillViewModel @Inject constructor(
    private val addFuelRefillUseCase: AddFuelRefillUseCase,
    private val locationProvider: LocationProvider,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private var carId: Long = 0L

    private val _uiState = MutableStateFlow(AddRefillUiState())
    val uiState: StateFlow<AddRefillUiState> = _uiState.asStateFlow()

    private val _showDatePicker = MutableStateFlow(false)
    val showDatePicker: StateFlow<Boolean> = _showDatePicker.asStateFlow()

    fun setCarId(id: Long) {
        carId = id

        // Populate with random values in debug mode
        if (BuildConfig.DEBUG) {
            populateRandomValues()
        }

        if (carId > 0 && locationProvider.hasLocationPermission()) {
            fetchLocation()
        }
    }

    private fun populateRandomValues() {
        val randomAmountPaid = Random.nextInt(30, 100).toString() + "." + Random.nextInt(0, 99).toString().padStart(2, '0')
        val randomLiters = Random.nextInt(25, 60).toString() + "." + Random.nextInt(0, 9).toString()
        val randomDistance = Random.nextInt(300, 700).toString()

        // Generate random date within last 30 days
        val currentTimeMillis = System.currentTimeMillis()
        val daysInMillis = 24 * 60 * 60 * 1000L
        val randomDaysAgo = Random.nextInt(0, 300) // 0-30 days ago
        val randomDateMillis = currentTimeMillis - (randomDaysAgo * daysInMillis)

        _uiState.value = _uiState.value.copy(
            amountPaid = randomAmountPaid,
            litersAdded = randomLiters,
            tripDistance = randomDistance,
            selectedDateMillis = randomDateMillis
        )
    }

    fun updateAmountPaid(value: String) {
        _uiState.value = _uiState.value.copy(amountPaid = value, errorMessage = null)
    }

    fun updateLitersAdded(value: String) {
        _uiState.value = _uiState.value.copy(litersAdded = value, errorMessage = null)
    }

    fun updateTripDistance(value: String) {
        _uiState.value = _uiState.value.copy(tripDistance = value, errorMessage = null)
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

    private fun fetchLocation() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoadingLocation = true)
            val location = locationProvider.getCurrentLocation()
            _uiState.value = _uiState.value.copy(
                location = location,
                isLoadingLocation = false
            )
        }
    }

    fun saveRefill(onSuccess: () -> Unit) {
        val state = _uiState.value

        // Validation
        val amount = state.amountPaid.toDoubleOrNull()
        val liters = state.litersAdded.toDoubleOrNull()
        val distance = state.tripDistance.toDoubleOrNull()

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

        viewModelScope.launch {
            _uiState.value = state.copy(isSaving = true, errorMessage = null)

            addFuelRefillUseCase(
                carId = carId,
                amountPaid = amount,
                litersAdded = liters,
                tripDistance = distance,
                timestamp = state.selectedDateMillis,
                location = state.location,
                notes = state.notes.takeIf { it.isNotBlank() }
            ).onSuccess {
                _uiState.value = state.copy(isSaving = false)
                resetForm()
                onSuccess()
            }.onFailure { e ->
                _uiState.value = state.copy(
                    isSaving = false,
                    errorMessage = e.message ?: "Failed to save refill"
                )
            }
        }
    }

    fun resetForm() {
        _uiState.value = AddRefillUiState()
        _showDatePicker.value = false
    }
}

data class AddRefillUiState(
    val amountPaid: String = "",
    val litersAdded: String = "",
    val tripDistance: String = "",
    val notes: String = "",
    val selectedDateMillis: Long = System.currentTimeMillis(),
    val location: com.agcoding.cartrackingapp.domain.model.Location? = null,
    val isLoadingLocation: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null
)

