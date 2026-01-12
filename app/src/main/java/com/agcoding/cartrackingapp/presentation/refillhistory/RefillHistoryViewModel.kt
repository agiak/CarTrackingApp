package com.agcoding.cartrackingapp.presentation.refillhistory

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agcoding.cartrackingapp.domain.model.FuelRefill
import com.agcoding.cartrackingapp.domain.repository.CarRepository
import com.agcoding.cartrackingapp.domain.repository.RefillRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RefillHistoryViewModel @Inject constructor(
    private val carRepository: CarRepository,
    private val refillRepository: RefillRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val carId: Long = savedStateHandle.get<Long>("carId") ?: 0L

    private val _uiState = MutableStateFlow<RefillHistoryUiState>(RefillHistoryUiState.Loading)
    val uiState: StateFlow<RefillHistoryUiState> = _uiState.asStateFlow()

    private val _selectedSort = MutableStateFlow(RefillSortOption.MOST_RECENT)
    val selectedSort: StateFlow<RefillSortOption> = _selectedSort.asStateFlow()

    init {
        loadRefillHistory()
    }

    private fun loadRefillHistory() {
        viewModelScope.launch {
            combine(
                carRepository.getCarById(carId),
                refillRepository.getRefillsByCarId(carId),
                _selectedSort
            ) { car, refills, sortOption ->
                Triple(car, refills, sortOption)
            }
                .catch { e ->
                    _uiState.value = RefillHistoryUiState.Error(e.message ?: "Unknown error")
                }
                .collect { (car, refills, sortOption) ->
                    if (car == null) {
                        _uiState.value = RefillHistoryUiState.Error("Car not found")
                        return@collect
                    }

                    if (refills.isEmpty()) {
                        _uiState.value = RefillHistoryUiState.Empty
                        return@collect
                    }

                    val sortedRefills = sortRefills(refills, sortOption)
                    _uiState.value = RefillHistoryUiState.Success(
                        carName = car.name,
                        refills = sortedRefills
                    )
                }
        }
    }

    fun setSortOption(option: RefillSortOption) {
        _selectedSort.value = option
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
    data class Success(
        val carName: String,
        val refills: List<FuelRefill>
    ) : RefillHistoryUiState()
    data class Error(val message: String) : RefillHistoryUiState()
}
