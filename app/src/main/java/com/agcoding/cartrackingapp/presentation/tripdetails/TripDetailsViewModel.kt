package com.agcoding.cartrackingapp.presentation.tripdetails

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agcoding.cartrackingapp.domain.model.FuelRefill
import com.agcoding.cartrackingapp.domain.model.TripStatistics
import com.agcoding.cartrackingapp.domain.repository.RefillRepository
import com.agcoding.cartrackingapp.domain.usecase.trip.AddRefillsToTripUseCase
import com.agcoding.cartrackingapp.domain.usecase.trip.DeleteTripUseCase
import com.agcoding.cartrackingapp.domain.usecase.trip.GetTripDetailsUseCase
import com.agcoding.cartrackingapp.domain.usecase.trip.RemoveRefillsFromTripUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TripDetailsViewModel @Inject constructor(
    private val getTripDetailsUseCase: GetTripDetailsUseCase,
    private val deleteTripUseCase: DeleteTripUseCase,
    private val addRefillsToTripUseCase: AddRefillsToTripUseCase,
    private val removeRefillsFromTripUseCase: RemoveRefillsFromTripUseCase,
    private val refillRepository: RefillRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val tripId: Long = savedStateHandle.get<Long>("tripId") ?: 0L
    private var carId: Long = 0L

    private val _uiState = MutableStateFlow<TripDetailsUiState>(TripDetailsUiState.Loading)
    val uiState: StateFlow<TripDetailsUiState> = _uiState.asStateFlow()

    private val _showDeleteDialog = MutableStateFlow(false)
    val showDeleteDialog: StateFlow<Boolean> = _showDeleteDialog.asStateFlow()

    private val _showAddRefillsDialog = MutableStateFlow(false)
    val showAddRefillsDialog: StateFlow<Boolean> = _showAddRefillsDialog.asStateFlow()

    private val _availableRefills = MutableStateFlow<List<FuelRefill>>(emptyList())
    val availableRefills: StateFlow<List<FuelRefill>> = _availableRefills.asStateFlow()

    private val _selectedRefillIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedRefillIds: StateFlow<Set<Long>> = _selectedRefillIds.asStateFlow()

    init {
        loadTripDetails()
    }

    private fun loadTripDetails() {
        viewModelScope.launch {
            getTripDetailsUseCase(tripId)
                .catch { e ->
                    _uiState.value = TripDetailsUiState.Error(e.message ?: "Failed to load trip details")
                }
                .collect { tripStats ->
                    _uiState.value = if (tripStats != null) {
                        carId = tripStats.trip.carId
                        TripDetailsUiState.Success(tripStats)
                    } else {
                        TripDetailsUiState.Error("Trip not found")
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

    fun deleteTrip(onSuccess: () -> Unit) {
        viewModelScope.launch {
            deleteTripUseCase(tripId).onSuccess {
                hideDeleteDialog()
                onSuccess()
            }.onFailure {
                hideDeleteDialog()
            }
        }
    }

    fun showAddRefillsDialog() {
        // Load available refills (those not in any trip)
        viewModelScope.launch {
            refillRepository.getRefillsByCarId(carId)
                .catch { /* ignore */ }
                .collect { allRefills ->
                    // Filter to only show refills not in any trip
                    val available = allRefills.filter { it.tripId == null }
                    _availableRefills.value = available
                    _showAddRefillsDialog.value = true
                }
        }
    }

    fun hideAddRefillsDialog() {
        _showAddRefillsDialog.value = false
        _selectedRefillIds.value = emptySet()
    }

    fun toggleRefillSelection(refillId: Long) {
        _selectedRefillIds.value = if (refillId in _selectedRefillIds.value) {
            _selectedRefillIds.value - refillId
        } else {
            _selectedRefillIds.value + refillId
        }
    }

    fun addSelectedRefills(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val selectedIds = _selectedRefillIds.value.toList()
        if (selectedIds.isEmpty()) {
            onError("No refills selected")
            return
        }

        viewModelScope.launch {
            addRefillsToTripUseCase(tripId, selectedIds).onSuccess {
                hideAddRefillsDialog()
                onSuccess()
            }.onFailure { error ->
                onError(error.message ?: "Failed to add refills")
            }
        }
    }

    fun removeRefill(refillId: Long, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            removeRefillsFromTripUseCase(listOf(refillId)).onSuccess {
                onSuccess()
            }.onFailure { error ->
                onError(error.message ?: "Failed to remove refill")
            }
        }
    }
}

sealed class TripDetailsUiState {
    object Loading : TripDetailsUiState()
    data class Success(val tripStatistics: TripStatistics) : TripDetailsUiState()
    data class Error(val message: String) : TripDetailsUiState()
}

