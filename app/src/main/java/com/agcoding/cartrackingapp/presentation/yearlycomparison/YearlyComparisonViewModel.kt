package com.agcoding.cartrackingapp.presentation.yearlycomparison

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agcoding.cartrackingapp.domain.model.AvailableYear
import com.agcoding.cartrackingapp.domain.model.Car
import com.agcoding.cartrackingapp.domain.model.YearlyComparisonData
import com.agcoding.cartrackingapp.domain.repository.CarRepository
import com.agcoding.cartrackingapp.domain.usecase.statistics.YearlyComparisonUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class YearlyComparisonViewModel @Inject constructor(
    private val yearlyComparisonUseCase: YearlyComparisonUseCase,
    private val carRepository: CarRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<YearlyComparisonUiState>(YearlyComparisonUiState.Loading)
    val uiState: StateFlow<YearlyComparisonUiState> = _uiState.asStateFlow()

    private val _availableYears = MutableStateFlow<List<AvailableYear>>(emptyList())
    val availableYears: StateFlow<List<AvailableYear>> = _availableYears.asStateFlow()

    private val _selectedYear1 = MutableStateFlow<Int?>(null)
    val selectedYear1: StateFlow<Int?> = _selectedYear1.asStateFlow()

    private val _selectedYear2 = MutableStateFlow<Int?>(null)
    val selectedYear2: StateFlow<Int?> = _selectedYear2.asStateFlow()

    private val _showYear1Selector = MutableStateFlow(false)
    val showYear1Selector: StateFlow<Boolean> = _showYear1Selector.asStateFlow()

    private val _showYear2Selector = MutableStateFlow(false)
    val showYear2Selector: StateFlow<Boolean> = _showYear2Selector.asStateFlow()

    private val _availableCars = MutableStateFlow<List<Car>>(emptyList())
    val availableCars: StateFlow<List<Car>> = _availableCars.asStateFlow()

    // Empty set = all cars
    private val _selectedCarIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedCarIds: StateFlow<Set<Long>> = _selectedCarIds.asStateFlow()

    private var comparisonJob: Job? = null
    private var yearsJob: Job? = null

    init {
        loadCars()
        loadAvailableYears()
    }

    private fun loadCars() {
        viewModelScope.launch {
            carRepository.getAllCars()
                .catch { }
                .collect { cars -> _availableCars.value = cars }
        }
    }

    fun toggleCar(carId: Long) {
        val current = _selectedCarIds.value
        _selectedCarIds.value = if (carId in current) current - carId else current + carId
        _selectedYear1.value = null
        _selectedYear2.value = null
        loadAvailableYears()
    }

    fun clearCarFilter() {
        if (_selectedCarIds.value.isEmpty()) return
        _selectedCarIds.value = emptySet()
        _selectedYear1.value = null
        _selectedYear2.value = null
        loadAvailableYears()
    }

    private fun loadAvailableYears() {
        yearsJob?.cancel()
        yearsJob = viewModelScope.launch {
            try {
                yearlyComparisonUseCase.getAvailableYears(_selectedCarIds.value)
                    .catch { e ->
                        _uiState.value = YearlyComparisonUiState.Error(e.message ?: "Unknown error")
                    }
                    .collect { years ->
                        _availableYears.value = years

                        if (years.size >= 2) {
                            val currentYear = Calendar.getInstance().get(Calendar.YEAR)
                            val availableYearsList = years.map { it.year }.sorted()

                            val year2 = availableYearsList.lastOrNull { it <= currentYear } ?: availableYearsList.last()
                            val year1 = availableYearsList.lastOrNull { it < year2 } ?: availableYearsList.first()

                            _selectedYear1.value = year1
                            _selectedYear2.value = year2

                            loadComparison()
                        } else {
                            _uiState.value = YearlyComparisonUiState.InsufficientData
                        }
                    }
            } catch (e: Exception) {
                _uiState.value = YearlyComparisonUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun selectYear1(year: Int) {
        if (year != _selectedYear2.value) {
            _selectedYear1.value = year
            _showYear1Selector.value = false
            loadComparison()
        }
    }

    fun selectYear2(year: Int) {
        if (year != _selectedYear1.value) {
            _selectedYear2.value = year
            _showYear2Selector.value = false
            loadComparison()
        }
    }

    fun showYear1Selector() { _showYear1Selector.value = true }
    fun hideYear1Selector() { _showYear1Selector.value = false }
    fun showYear2Selector() { _showYear2Selector.value = true }
    fun hideYear2Selector() { _showYear2Selector.value = false }

    private fun loadComparison() {
        val year1 = _selectedYear1.value ?: return
        val year2 = _selectedYear2.value ?: return

        comparisonJob?.cancel()
        comparisonJob = viewModelScope.launch {
            // Only show loading spinner on initial load — keep existing data visible during refresh
            if (_uiState.value !is YearlyComparisonUiState.Success) {
                _uiState.value = YearlyComparisonUiState.Loading
            }

            try {
                yearlyComparisonUseCase.getYearlyComparison(year1, year2, _selectedCarIds.value)
                    .catch { e ->
                        _uiState.value = YearlyComparisonUiState.Error(e.message ?: "Unknown error")
                    }
                    .collect { comparisonData ->
                        _uiState.value = if (comparisonData == null) {
                            YearlyComparisonUiState.InsufficientData
                        } else {
                            YearlyComparisonUiState.Success(comparisonData)
                        }
                    }
            } catch (e: Exception) {
                _uiState.value = YearlyComparisonUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}

sealed class YearlyComparisonUiState {
    object Loading : YearlyComparisonUiState()
    object InsufficientData : YearlyComparisonUiState()
    data class Success(val data: YearlyComparisonData) : YearlyComparisonUiState()
    data class Error(val message: String) : YearlyComparisonUiState()
}
