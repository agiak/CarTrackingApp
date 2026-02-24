package com.agcoding.cartrackingapp.presentation.createtrip

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agcoding.cartrackingapp.domain.model.FuelRefill
import com.agcoding.cartrackingapp.domain.repository.RefillRepository
import com.agcoding.cartrackingapp.domain.usecase.trip.CreateTripUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateTripViewModel @Inject constructor(
    private val createTripUseCase: CreateTripUseCase,
    private val refillRepository: RefillRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val carId: Long = savedStateHandle.get<Long>("carId") ?: 0L

    private val _uiState = MutableStateFlow(CreateTripUiState())
    val uiState: StateFlow<CreateTripUiState> = _uiState.asStateFlow()

    init {
        loadAvailableRefills()
    }

    private fun loadAvailableRefills() {
        viewModelScope.launch {
            refillRepository.getRefillsByCarId(carId)
                .catch { e ->
                    _uiState.update { it.copy(error = e.message ?: "Failed to load refills") }
                }
                .collect { refills ->
                    // Filter out refills that are already in a trip
                    val availableRefills = refills.filter { it.tripId == null }
                    // Apply sorting
                    val sortedRefills = sortRefills(availableRefills, _uiState.value.sortOption)
                    _uiState.update { it.copy(availableRefills = sortedRefills, isLoading = false) }
                }
        }
    }

    fun setSortOption(option: RefillSortOption) {
        _uiState.update { state ->
            val sortedRefills = sortRefills(state.availableRefills, option)
            state.copy(sortOption = option, availableRefills = sortedRefills)
        }
    }

    private fun sortRefills(refills: List<FuelRefill>, sortOption: RefillSortOption): List<FuelRefill> {
        return when (sortOption) {
            RefillSortOption.MOST_RECENT -> refills.sortedByDescending { it.timestamp }
            RefillSortOption.OLDEST -> refills.sortedBy { it.timestamp }
        }
    }

    fun onTripNameChanged(name: String) {
        _uiState.update { it.copy(tripName = name, nameError = null) }
    }

    fun onTripDescriptionChanged(description: String) {
        _uiState.update { it.copy(tripDescription = description) }
    }

    fun toggleRefillSelection(refillId: Long) {
        _uiState.update { state ->
            val newSelection = if (refillId in state.selectedRefillIds) {
                state.selectedRefillIds - refillId
            } else {
                state.selectedRefillIds + refillId
            }
            state.copy(selectedRefillIds = newSelection, selectionError = null)
        }
    }

    fun createTrip(onSuccess: () -> Unit) {
        val state = _uiState.value

        // Validate
        var hasError = false
        if (state.tripName.isBlank()) {
            _uiState.update { it.copy(nameError = "Trip name is required") }
            hasError = true
        }
        if (state.selectedRefillIds.isEmpty()) {
            _uiState.update { it.copy(selectionError = "Please select at least one refill") }
            hasError = true
        }

        if (hasError) return

        _uiState.update { it.copy(isCreating = true) }

        viewModelScope.launch {
            createTripUseCase(
                carId = carId,
                name = state.tripName,
                description = state.tripDescription.ifBlank { null },
                refillIds = state.selectedRefillIds.toList()
            ).onSuccess {
                _uiState.update { it.copy(isCreating = false) }
                onSuccess()
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
                        isCreating = false,
                        error = error.message ?: "Failed to create trip"
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class CreateTripUiState(
    val isLoading: Boolean = true,
    val isCreating: Boolean = false,
    val availableRefills: List<FuelRefill> = emptyList(),
    val selectedRefillIds: Set<Long> = emptySet(),
    val tripName: String = "",
    val tripDescription: String = "",
    val nameError: String? = null,
    val selectionError: String? = null,
    val error: String? = null,
    val sortOption: RefillSortOption = RefillSortOption.MOST_RECENT
)

enum class RefillSortOption {
    MOST_RECENT,
    OLDEST
}

