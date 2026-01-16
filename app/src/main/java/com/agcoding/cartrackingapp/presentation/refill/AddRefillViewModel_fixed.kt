package com.agcoding.cartrackingapp.presentation.refill

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agcoding.cartrackingapp.BuildConfig
import com.agcoding.cartrackingapp.data.local.LocationProvider
import com.agcoding.cartrackingapp.domain.usecase.refill.AddFuelRefillUseCase
import com.agcoding.cartrackingapp.domain.validation.RefillValidator
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class AddRefillViewModel @Inject constructor(
    private val addFuelRefillUseCase: AddFuelRefillUseCase,
    private val carRepository: com.agcoding.cartrackingapp.domain.repository.CarRepository,
    private val locationProvider: LocationProvider,
    @ApplicationContext private val context: android.content.Context,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private var carId: Long = 0L

    private val _uiState = MutableStateFlow(AddRefillUiState())
    val uiState: StateFlow<AddRefillUiState> = _uiState.asStateFlow()

    private val _showDatePicker = MutableStateFlow(false)
    val showDatePicker: StateFlow<Boolean> = _showDatePicker.asStateFlow()

    fun setCarId(id: Long) {
        carId = id

        // Load car's current odometer
        viewModelScope.launch {
            try {
                val car = carRepository.getCarById(carId).first()
                if (car != null) {
                    _uiState.value = _uiState.value.copy(
                        previousOdometer = car.currentOdometer,
                        odometer = car.currentOdometer.toInt().toString()
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

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
        val randomDistance = Random.nextInt(300, 700)

        // Generate random date within last 30 days
        val currentTimeMillis = System.currentTimeMillis()
        val daysInMillis = 24 * 60 * 60 * 1000L
        val randomDaysAgo = Random.nextInt(0, 300) // 0-300 days ago
        val randomDateMillis = currentTimeMillis - (randomDaysAgo * daysInMillis)

        // Calculate new odometer reading from previous + random distance
        val newOdometer = (_uiState.value.previousOdometer + randomDistance).toInt()

        _uiState.value = _uiState.value.copy(
            amountPaid = randomAmountPaid,
            litersAdded = randomLiters,
            odometer = newOdometer.toString(),
            selectedDateMillis = randomDateMillis
        )
    }

    fun updateAmountPaid(value: String) {
        val costError = RefillValidator.validateCost(context, value)
        _uiState.value = _uiState.value.copy(
            amountPaid = value,
            errorMessage = null,
            fieldErrors = if (costError != null) {
                _uiState.value.fieldErrors + ("cost" to costError)
            } else {
                _uiState.value.fieldErrors - "cost"
            }
        )
    }

    fun updateLitersAdded(value: String) {
        val litersError = RefillValidator.validateLiters(context, value)
        _uiState.value = _uiState.value.copy(
            litersAdded = value,
            errorMessage = null,
            fieldErrors = if (litersError != null) {
                _uiState.value.fieldErrors + ("liters" to litersError)
            } else {
                _uiState.value.fieldErrors - "liters"
            }
        )
    }

    fun updateTripDistance(value: String) {
        val distanceError = RefillValidator.validateDistance(context, value)
        _uiState.value = _uiState.value.copy(
            tripDistance = value,
            errorMessage = null,
            fieldErrors = if (distanceError != null) {
                _uiState.value.fieldErrors + ("distance" to distanceError)
            } else {
                _uiState.value.fieldErrors - "distance"
            }
        )
    }

    fun updateOdometer(value: String) {
        val odometerError = RefillValidator.validateOdometer(context, value, _uiState.value.previousOdometer)
        _uiState.value = _uiState.value.copy(
            odometer = value,
            errorMessage = null,
            fieldErrors = if (odometerError != null) {
                _uiState.value.fieldErrors + ("odometer" to odometerError)
            } else {
                _uiState.value.fieldErrors - "odometer"
            }
        )
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

        // Use RefillValidator for comprehensive validation
        val validationResult = RefillValidator.validateRefill(
            context = context,
            liters = state.litersAdded,
            cost = state.amountPaid,
            distance = state.tripDistance
        )

        if (!validationResult.isValid) {
            // Separate consumption error from field errors
            val consumptionError = validationResult.errors["consumption"]
            val fieldErrors = validationResult.errors.filterKeys { it != "consumption" }

            _uiState.value = state.copy(
                fieldErrors = fieldErrors,
                errorMessage = consumptionError ?: fieldErrors.values.firstOrNull()
            )
            return
        }

        // Parse validated values
        val amount = state.amountPaid.toDouble()
        val liters = state.litersAdded.toDouble()
        val distance = state.tripDistance.toDouble()

        viewModelScope.launch {
            _uiState.value = state.copy(isSaving = true, errorMessage = null, fieldErrors = emptyMap())

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
    val odometer: String = "",
    val previousOdometer: Double = 0.0,
    val notes: String = "",
    val selectedDateMillis: Long = System.currentTimeMillis(),
    val location: com.agcoding.cartrackingapp.domain.model.Location? = null,
    val isLoadingLocation: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val fieldErrors: Map<String, String> = emptyMap()
)
