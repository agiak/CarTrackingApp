package com.agcoding.cartrackingapp.presentation.carcomparison

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agcoding.cartrackingapp.domain.model.Car
import com.agcoding.cartrackingapp.domain.model.CarComparisonResult
import com.agcoding.cartrackingapp.domain.model.MultiCarComparisonResult
import com.agcoding.cartrackingapp.domain.repository.CarRepository
import com.agcoding.cartrackingapp.domain.usecase.comparison.CalculateCarComparisonUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for Car Comparison screen
 */
@HiltViewModel
class CarComparisonViewModel @Inject constructor(
    private val carRepository: CarRepository,
    private val calculateCarComparisonUseCase: CalculateCarComparisonUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow<CarComparisonUiState>(CarComparisonUiState.Loading)
    val uiState: StateFlow<CarComparisonUiState> = _uiState.asStateFlow()

    private val _availableCars = MutableStateFlow<List<Car>>(emptyList())
    val availableCars: StateFlow<List<Car>> = _availableCars.asStateFlow()

    private val _selectedCar1 = MutableStateFlow<Car?>(null)
    val selectedCar1: StateFlow<Car?> = _selectedCar1.asStateFlow()

    private val _selectedCar2 = MutableStateFlow<Car?>(null)
    val selectedCar2: StateFlow<Car?> = _selectedCar2.asStateFlow()

    private val _comparisonMode = MutableStateFlow(ComparisonMode.ALL_CARS)
    val comparisonMode: StateFlow<ComparisonMode> = _comparisonMode.asStateFlow()

    init {
        loadAvailableCars()
    }

    private fun loadAvailableCars() {
        viewModelScope.launch {
            try {
                val cars = carRepository.getAllCars().first()
                _availableCars.value = cars

                if (cars.size < 2) {
                    _uiState.value = CarComparisonUiState.InsufficientCars
                } else {
                    // Default to all cars comparison
                    compareAllCars()
                }
            } catch (e: Exception) {
                _uiState.value = CarComparisonUiState.Error(e.message ?: "Failed to load cars")
            }
        }
    }

    fun setComparisonMode(mode: ComparisonMode) {
        _comparisonMode.value = mode
        when (mode) {
            ComparisonMode.ALL_CARS -> compareAllCars()
            ComparisonMode.TWO_CARS -> {
                // Wait for car selection
                _uiState.value = CarComparisonUiState.SelectingCars
            }
        }
    }

    fun selectCar1(car: Car) {
        _selectedCar1.value = car
        checkAndCompareTwoCars()
    }

    fun selectCar2(car: Car) {
        _selectedCar2.value = car
        checkAndCompareTwoCars()
    }

    private fun checkAndCompareTwoCars() {
        val car1 = _selectedCar1.value
        val car2 = _selectedCar2.value

        if (car1 != null && car2 != null && car1.id != car2.id) {
            compareTwoCars(car1.id, car2.id)
        }
    }

    fun compareAllCars() {
        viewModelScope.launch {
            try {
                _uiState.value = CarComparisonUiState.Loading
                val result = calculateCarComparisonUseCase.compareAllCars()

                if (result.cars.isEmpty()) {
                    _uiState.value = CarComparisonUiState.InsufficientData
                } else {
                    _uiState.value = CarComparisonUiState.AllCarsComparison(result)
                }
            } catch (e: Exception) {
                _uiState.value = CarComparisonUiState.Error(e.message ?: "Comparison failed")
            }
        }
    }

    fun compareTwoCars(car1Id: Long, car2Id: Long) {
        viewModelScope.launch {
            try {
                _uiState.value = CarComparisonUiState.Loading
                val result = calculateCarComparisonUseCase.compareTwoCars(car1Id, car2Id)

                if (result.car1.hasInsufficientData || result.car2.hasInsufficientData) {
                    _uiState.value = CarComparisonUiState.InsufficientData
                } else {
                    _uiState.value = CarComparisonUiState.TwoCarsComparison(result)
                }
            } catch (e: Exception) {
                _uiState.value = CarComparisonUiState.Error(e.message ?: "Comparison failed")
            }
        }
    }

    fun retry() {
        when (_comparisonMode.value) {
            ComparisonMode.ALL_CARS -> compareAllCars()
            ComparisonMode.TWO_CARS -> {
                val car1 = _selectedCar1.value
                val car2 = _selectedCar2.value
                if (car1 != null && car2 != null) {
                    compareTwoCars(car1.id, car2.id)
                }
            }
        }
    }
}

/**
 * UI state for car comparison
 */
sealed class CarComparisonUiState {
    object Loading : CarComparisonUiState()
    object SelectingCars : CarComparisonUiState()
    object InsufficientCars : CarComparisonUiState()
    object InsufficientData : CarComparisonUiState()
    data class Error(val message: String) : CarComparisonUiState()
    data class TwoCarsComparison(val result: CarComparisonResult) : CarComparisonUiState()
    data class AllCarsComparison(val result: MultiCarComparisonResult) : CarComparisonUiState()
}

/**
 * Comparison mode
 */
enum class ComparisonMode {
    ALL_CARS,
    TWO_CARS
}

