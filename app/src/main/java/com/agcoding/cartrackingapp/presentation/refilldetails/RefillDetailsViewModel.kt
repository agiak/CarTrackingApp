package com.agcoding.cartrackingapp.presentation.refilldetails

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agcoding.cartrackingapp.domain.model.Trip
import com.agcoding.cartrackingapp.domain.usecase.refill.DeleteRefillUseCase
import com.agcoding.cartrackingapp.domain.usecase.refill.GetRefillDetailsUseCase
import com.agcoding.cartrackingapp.domain.usecase.refill.RefillDetails
import com.agcoding.cartrackingapp.domain.usecase.trip.AddRefillsToTripUseCase
import com.agcoding.cartrackingapp.domain.usecase.trip.CreateTripUseCase
import com.agcoding.cartrackingapp.domain.usecase.trip.GetTripsByCarUseCase
import com.agcoding.cartrackingapp.domain.usecase.trip.RemoveRefillsFromTripUseCase
import com.agcoding.cartrackingapp.shared.domain.result.Result
import com.agcoding.cartrackingapp.util.GeocodingUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Outcome of a trip action, shown once as a snackbar and then cleared. */
sealed interface TripActionMessage {
    data class Added(val tripName: String) : TripActionMessage
    object Removed : TripActionMessage
    object Failed : TripActionMessage
}

sealed class RefillDetailsUiState {
    object Loading : RefillDetailsUiState()
    data class Success(
        val details: RefillDetails,
        val addressString: String? = null
    ) : RefillDetailsUiState()
    data class Error(val message: String) : RefillDetailsUiState()
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class RefillDetailsViewModel @Inject constructor(
    private val getRefillDetailsUseCase: GetRefillDetailsUseCase,
    private val deleteRefillUseCase: DeleteRefillUseCase,
    private val getTripsByCarUseCase: GetTripsByCarUseCase,
    private val addRefillsToTripUseCase: AddRefillsToTripUseCase,
    private val createTripUseCase: CreateTripUseCase,
    private val removeRefillsFromTripUseCase: RemoveRefillsFromTripUseCase,
    @ApplicationContext private val context: Context,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val refillId: Long = savedStateHandle.get<Long>("refillId") ?: 0L

    private val _uiState = MutableStateFlow<RefillDetailsUiState>(RefillDetailsUiState.Loading)
    val uiState: StateFlow<RefillDetailsUiState> = _uiState.asStateFlow()

    private val _showDeleteDialog = MutableStateFlow(false)
    val showDeleteDialog: StateFlow<Boolean> = _showDeleteDialog.asStateFlow()

    // ---------------------------------------------------------------------
    // Trip membership: a refill belongs to at most one trip, and only to a
    // trip of its own car.
    // ---------------------------------------------------------------------

    /** Known once the refill has loaded; until then there are no trips to offer. */
    private val _carId = MutableStateFlow<Long?>(null)

    val trips: StateFlow<List<Trip>> = _carId
        .flatMapLatest { carId ->
            if (carId == null) flowOf(emptyList()) else getTripsByCarUseCase(carId)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** The trip this refill is part of, or null when it is not in one. */
    val currentTrip: StateFlow<Trip?> = combine(trips, _uiState) { trips, state ->
        val tripId = (state as? RefillDetailsUiState.Success)?.details?.refill?.tripId
        trips.firstOrNull { it.id == tripId }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val _tripMessage = MutableStateFlow<TripActionMessage?>(null)
    val tripMessage: StateFlow<TripActionMessage?> = _tripMessage.asStateFlow()

    init {
        loadRefillDetails()
    }

    private fun loadRefillDetails() {
        viewModelScope.launch {
            _uiState.value = RefillDetailsUiState.Loading

            getRefillDetailsUseCase(refillId)
                .catch { e ->
                    _uiState.value = RefillDetailsUiState.Error(
                        e.message ?: "Failed to load refill details"
                    )
                }
                .collect { details ->
                    if (details != null) {
                        _carId.value = details.refill.carId
                        val storedName = details.refill.locationName
                        _uiState.value = RefillDetailsUiState.Success(details, storedName)

                        // Prefer the persisted (user-editable) name. Only reverse-geocode
                        // as a fallback for older refills that have coordinates but no name.
                        if (storedName.isNullOrBlank()) {
                            details.refill.location?.let { location ->
                                fetchAddress(location.latitude, location.longitude, details)
                            }
                        }
                    } else {
                        _uiState.value = RefillDetailsUiState.Error("Refill not found")
                    }
                }
        }
    }

    private fun fetchAddress(latitude: Double, longitude: Double, details: RefillDetails) {
        viewModelScope.launch {
            val address = GeocodingUtil.getAddressFromLocation(context, latitude, longitude)
            _uiState.value = RefillDetailsUiState.Success(details, address)
        }
    }

    fun showDeleteDialog() {
        _showDeleteDialog.value = true
    }

    fun hideDeleteDialog() {
        _showDeleteDialog.value = false
    }

    fun deleteRefill(onSuccess: () -> Unit) {
        viewModelScope.launch {
            when (deleteRefillUseCase(refillId)) {
                is Result.Success -> { hideDeleteDialog(); onSuccess() }
                is Result.Error -> hideDeleteDialog()
            }
        }
    }

    /** Moves the refill into [trip]; reassigning from another trip is the same write. */
    fun assignToTrip(trip: Trip) {
        viewModelScope.launch {
            _tripMessage.value = when (addRefillsToTripUseCase(trip.id, listOf(refillId))) {
                is Result.Success -> TripActionMessage.Added(trip.name)
                is Result.Error -> TripActionMessage.Failed
            }
        }
    }

    /** Creates a trip for this refill's car and puts the refill in it. */
    fun createTripAndAssign(name: String, description: String?) {
        viewModelScope.launch {
            val carId = _carId.value
            if (carId == null) {
                _tripMessage.value = TripActionMessage.Failed
                return@launch
            }
            _tripMessage.value = when (
                createTripUseCase(carId, name, description?.takeIf { it.isNotBlank() }, listOf(refillId))
            ) {
                is Result.Success -> TripActionMessage.Added(name.trim())
                is Result.Error -> TripActionMessage.Failed
            }
        }
    }

    fun removeFromTrip() {
        viewModelScope.launch {
            _tripMessage.value = when (removeRefillsFromTripUseCase(listOf(refillId))) {
                is Result.Success -> TripActionMessage.Removed
                is Result.Error -> TripActionMessage.Failed
            }
        }
    }

    fun clearTripMessage() {
        _tripMessage.value = null
    }

    fun retry() {
        loadRefillDetails()
    }
}

