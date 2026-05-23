package com.agcoding.cartrackingapp.presentation.refillhistory

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agcoding.cartrackingapp.domain.model.FuelRefill
import com.agcoding.cartrackingapp.domain.model.Trip
import com.agcoding.cartrackingapp.domain.repository.CarRepository
import com.agcoding.cartrackingapp.domain.repository.RefillRepository
import com.agcoding.cartrackingapp.domain.repository.TripRepository
import com.agcoding.cartrackingapp.domain.usecase.refill.DeleteRefillUseCase
import com.agcoding.cartrackingapp.domain.usecase.trip.AddRefillsToTripUseCase
import com.agcoding.cartrackingapp.shared.domain.result.Result
import com.agcoding.cartrackingapp.shared.ui.utils.simpleMessage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RefillHistoryViewModel @Inject constructor(
    private val carRepository: CarRepository,
    private val refillRepository: RefillRepository,
    private val tripRepository: TripRepository,
    private val addRefillsToTripUseCase: AddRefillsToTripUseCase,
    private val deleteRefillUseCase: DeleteRefillUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val carId: Long = savedStateHandle.get<Long>("carId") ?: 0L

    private val _uiState = MutableStateFlow<RefillHistoryUiState>(RefillHistoryUiState.Loading)
    val uiState: StateFlow<RefillHistoryUiState> = _uiState.asStateFlow()

    private val _selectedSort = MutableStateFlow(RefillSortOption.MOST_RECENT)
    val selectedSort: StateFlow<RefillSortOption> = _selectedSort.asStateFlow()

    private val _startDate = MutableStateFlow<Long?>(null)
    val startDate: StateFlow<Long?> = _startDate.asStateFlow()

    private val _endDate = MutableStateFlow<Long?>(null)
    val endDate: StateFlow<Long?> = _endDate.asStateFlow()

    // Multi-select state
    private val _isSelectionMode = MutableStateFlow(false)
    val isSelectionMode: StateFlow<Boolean> = _isSelectionMode.asStateFlow()

    private val _selectedRefillIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedRefillIds: StateFlow<Set<Long>> = _selectedRefillIds.asStateFlow()

    // Available trips for this car
    private val _availableTrips = MutableStateFlow<List<Trip>>(emptyList())
    val availableTrips: StateFlow<List<Trip>> = _availableTrips.asStateFlow()

    // Trip names map (refillId -> tripName)
    private val _refillTripNames = MutableStateFlow<Map<Long, String>>(emptyMap())
    val refillTripNames: StateFlow<Map<Long, String>> = _refillTripNames.asStateFlow()

    init {
        loadRefillHistory()
    }

    private fun loadRefillHistory() {
        viewModelScope.launch {
            combine(
                carRepository.getCarById(carId),
                refillRepository.getRefillsByCarId(carId),
                tripRepository.getTripsByCarId(carId),
                _selectedSort,
                combine(_startDate, _endDate) { s, e -> s to e }
            ) { car, refills, trips, sortOption, (startDate, endDate) ->
                _availableTrips.value = trips

                val tripMap = mutableMapOf<Long, String>()
                trips.forEach { trip ->
                    trip.refills.forEach { refill ->
                        tripMap[refill.id] = trip.name
                    }
                }
                _refillTripNames.value = tripMap

                val hasDateFilter = startDate != null || endDate != null
                val filtered = refills.let {
                    var f = it
                    if (startDate != null) f = f.filter { r -> r.timestamp >= startDate }
                    if (endDate != null) f = f.filter { r -> r.timestamp <= endDate + 86_399_999L }
                    f
                }
                Triple(car, Pair(refills.isEmpty(), filtered), Pair(sortOption, hasDateFilter))
            }
                .catch { e ->
                    _uiState.value = RefillHistoryUiState.Error(e.message ?: "Unknown error")
                }
                .collect { (car, refillsInfo, sortInfo) ->
                    val (isOriginalEmpty, filtered) = refillsInfo
                    val (sortOption, hasDateFilter) = sortInfo

                    if (car == null) {
                        _uiState.value = RefillHistoryUiState.Error("Car not found")
                        return@collect
                    }

                    if (isOriginalEmpty) {
                        _uiState.value = RefillHistoryUiState.Empty
                        return@collect
                    }

                    if (filtered.isEmpty() && hasDateFilter) {
                        _uiState.value = RefillHistoryUiState.EmptyFilter
                        return@collect
                    }

                    val sortedRefills = sortRefills(filtered, sortOption)
                    _uiState.value = RefillHistoryUiState.Success(
                        carName = car.name,
                        refills = sortedRefills
                    )
                }
        }
    }

    fun setSortOption(option: RefillSortOption) { _selectedSort.value = option }
    fun setStartDate(date: Long?) { _startDate.value = date }
    fun setEndDate(date: Long?) { _endDate.value = date }
    fun clearDateFilter() { _startDate.value = null; _endDate.value = null }

    // Multi-select functions
    fun onRefillLongPress(refillId: Long) {
        // Don't allow selecting refills already in a trip
        if (refillId in _refillTripNames.value.keys) {
            return
        }

        if (!_isSelectionMode.value) {
            _isSelectionMode.value = true
            _selectedRefillIds.value = setOf(refillId)
        }
    }

    fun toggleRefillSelection(refillId: Long) {
        if (!_isSelectionMode.value) {
            // Not in selection mode, treat as normal click
            return
        }

        // Check if this refill is already in a trip
        if (refillId in _refillTripNames.value.keys) {
            // Refill is already in a trip, don't allow selection
            return
        }

        _selectedRefillIds.update { currentSet ->
            if (refillId in currentSet) {
                val newSet = currentSet - refillId
                // Exit selection mode if no items selected
                if (newSet.isEmpty()) {
                    _isSelectionMode.value = false
                }
                newSet
            } else {
                currentSet + refillId
            }
        }
    }

    fun clearSelection() {
        _isSelectionMode.value = false
        _selectedRefillIds.value = emptySet()
    }

    fun deleteSelectedRefills(onSuccess: (Int) -> Unit, onError: (String) -> Unit) {
        val ids = _selectedRefillIds.value.toList()
        if (ids.isEmpty()) return

        viewModelScope.launch {
            var deletedCount = 0
            var lastError: String? = null
            ids.forEach { id ->
                when (val result = deleteRefillUseCase(id)) {
                    is Result.Success -> deletedCount++
                    is Result.Error -> lastError = result.error.simpleMessage
                }
            }
            clearSelection()
            if (lastError != null && deletedCount == 0) {
                onError(lastError!!)
            } else {
                onSuccess(deletedCount)
            }
        }
    }

    fun addSelectedToTrip(tripId: Long, onSuccess: () -> Unit, onError: (String) -> Unit) {
        val selectedIds = _selectedRefillIds.value.toList()
        if (selectedIds.isEmpty()) {
            onError("No refills selected")
            return
        }

        viewModelScope.launch {
            when (val result = addRefillsToTripUseCase(tripId, selectedIds)) {
                is Result.Success -> { clearSelection(); onSuccess() }
                is Result.Error -> onError(result.error.simpleMessage)
            }
        }
    }

    private fun sortRefills(refills: List<FuelRefill>, sortOption: RefillSortOption): List<FuelRefill> {
        return when (sortOption) {
            RefillSortOption.MOST_RECENT -> refills.sortedByDescending { it.timestamp }
            RefillSortOption.OLDEST -> refills.sortedBy { it.timestamp }
            RefillSortOption.MOST_EXPENSIVE -> refills.sortedByDescending { it.amountPaid }
            RefillSortOption.LEAST_EXPENSIVE -> refills.sortedBy { it.amountPaid }
            RefillSortOption.BEST_CONSUMPTION -> refills.sortedBy { it.fuelConsumption }
            RefillSortOption.WORST_CONSUMPTION -> refills.sortedByDescending { it.fuelConsumption }
        }
    }
}

sealed class RefillHistoryUiState {
    object Loading : RefillHistoryUiState()
    object Empty : RefillHistoryUiState()
    object EmptyFilter : RefillHistoryUiState()
    data class Success(
        val carName: String,
        val refills: List<FuelRefill>
    ) : RefillHistoryUiState()
    data class Error(val message: String) : RefillHistoryUiState()
}
