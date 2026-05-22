package com.agcoding.cartrackingapp.presentation.tripslist

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agcoding.cartrackingapp.domain.model.Trip
import com.agcoding.cartrackingapp.domain.usecase.trip.DeleteTripUseCase
import com.agcoding.cartrackingapp.domain.usecase.trip.GetTripsByCarUseCase
import com.agcoding.cartrackingapp.shared.domain.result.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TripsListViewModel @Inject constructor(
    private val getTripsByCarUseCase: GetTripsByCarUseCase,
    private val deleteTripUseCase: DeleteTripUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val carId: Long = savedStateHandle.get<Long>("carId") ?: 0L

    private val _uiState = MutableStateFlow<TripsListUiState>(TripsListUiState.Loading)
    val uiState: StateFlow<TripsListUiState> = _uiState.asStateFlow()

    private val _showDeleteDialog = MutableStateFlow(false)
    val showDeleteDialog: StateFlow<Boolean> = _showDeleteDialog.asStateFlow()

    private val _tripToDelete = MutableStateFlow<Long?>(null)
    val tripToDelete: StateFlow<Long?> = _tripToDelete.asStateFlow()

    init {
        loadTrips()
    }

    private fun loadTrips() {
        viewModelScope.launch {
            getTripsByCarUseCase(carId)
                .catch { e ->
                    _uiState.value = TripsListUiState.Error(e.message ?: "Failed to load trips")
                }
                .collect { trips ->
                    _uiState.value = if (trips.isEmpty()) {
                        TripsListUiState.Empty
                    } else {
                        TripsListUiState.Success(trips)
                    }
                }
        }
    }

    fun showDeleteDialog(tripId: Long) {
        _tripToDelete.value = tripId
        _showDeleteDialog.value = true
    }

    fun hideDeleteDialog() {
        _showDeleteDialog.value = false
        _tripToDelete.value = null
    }

    fun deleteTrip() {
        val tripId = _tripToDelete.value ?: return
        viewModelScope.launch {
            when (deleteTripUseCase(tripId)) {
                is Result.Success, is Result.Error -> hideDeleteDialog()
            }
        }
    }
}

sealed class TripsListUiState {
    object Loading : TripsListUiState()
    object Empty : TripsListUiState()
    data class Success(val trips: List<Trip>) : TripsListUiState()
    data class Error(val message: String) : TripsListUiState()
}

