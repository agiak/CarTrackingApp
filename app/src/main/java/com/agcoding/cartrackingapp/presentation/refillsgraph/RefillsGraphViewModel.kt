package com.agcoding.cartrackingapp.presentation.refillsgraph

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agcoding.cartrackingapp.domain.model.Car
import com.agcoding.cartrackingapp.domain.model.RefillsTrendData
import com.agcoding.cartrackingapp.domain.model.DateFilter
import com.agcoding.cartrackingapp.domain.repository.CarRepository
import com.agcoding.cartrackingapp.domain.usecase.statistics.GetAvailableYearsUseCase
import com.agcoding.cartrackingapp.domain.usecase.statistics.GetRefillsTrendUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RefillsGraphViewModel @Inject constructor(
    private val getRefillsTrendUseCase: GetRefillsTrendUseCase,
    private val carRepository: CarRepository,
    getAvailableYearsUseCase: GetAvailableYearsUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val carId: Long? = savedStateHandle.get<String>("carId")?.toLongOrNull()

    private val _uiState = MutableStateFlow<RefillsGraphUiState>(RefillsGraphUiState.Loading)
    val uiState: StateFlow<RefillsGraphUiState> = _uiState.asStateFlow()

    /** The shared year/month filter, same control as everywhere else in the app. */
    private val _dateFilter = MutableStateFlow(DateFilter.None)
    val dateFilter: StateFlow<DateFilter> = _dateFilter.asStateFlow()

    val availableYears: StateFlow<List<Int>> = getAvailableYearsUseCase(carId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _showPeriodSelector = MutableStateFlow(false)
    val showPeriodSelector: StateFlow<Boolean> = _showPeriodSelector.asStateFlow()

    private val _allCars = MutableStateFlow<List<Car>>(emptyList())
    val allCars: StateFlow<List<Car>> = _allCars.asStateFlow()

    private val _selectedCarIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedCarIds: StateFlow<Set<Long>> = _selectedCarIds.asStateFlow()

    private val _showCarFilter = MutableStateFlow(false)
    val showCarFilter: StateFlow<Boolean> = _showCarFilter.asStateFlow()

    init {
        loadCars()
        loadTrendData()
    }

    private fun loadCars() {
        viewModelScope.launch {
            try {
                val cars = carRepository.getAllCars().first()
                _allCars.value = cars
                if (carId != null) {
                    _selectedCarIds.value = setOf(carId)
                } else {
                    // The screen opens showing every car, so the filter has to say so
                    // too. Leaving this empty meant the filter sheet rendered every
                    // checkbox unticked while the chart was showing all of them.
                    // An empty set and a full set load the same data (see loadTrendData).
                    _selectedCarIds.value = cars.map { it.id }.toSet()
                }
            } catch (e: Exception) {
                android.util.Log.e("RefillsGraphVM", "Unexpected error", e)
            }
        }
    }

    fun setDateFilter(dateFilter: DateFilter) {
        _dateFilter.value = dateFilter.normalized
        loadTrendData()
    }

    fun showPeriodSelector() {
        _showPeriodSelector.value = true
    }

    fun hidePeriodSelector() {
        _showPeriodSelector.value = false
    }

    fun showCarFilter() {
        _showCarFilter.value = true
    }

    fun hideCarFilter() {
        _showCarFilter.value = false
    }

    fun toggleCarSelection(carId: Long, selected: Boolean) {
        _selectedCarIds.value = if (selected) {
            _selectedCarIds.value + carId
        } else {
            _selectedCarIds.value - carId
        }
    }

    fun applyCarFilter() {
        _showCarFilter.value = false
        loadTrendData()
    }

    private fun loadTrendData() {
        viewModelScope.launch {
            _uiState.value = RefillsGraphUiState.Loading

            try {
                // Determine which cars to load
                val carsToLoad = when {
                    // No selection or all cars selected -> load all (null)
                    _selectedCarIds.value.isEmpty() || _selectedCarIds.value.size == _allCars.value.size -> null
                    // One car selected -> load just that car
                    _selectedCarIds.value.size == 1 -> _selectedCarIds.value.first()
                    // Multiple cars selected -> need to aggregate
                    else -> null // Will handle aggregation below
                } ?: carId

                // If we have multiple specific cars selected (not all), aggregate their data
                if (_selectedCarIds.value.size > 1 && _selectedCarIds.value.size < _allCars.value.size) {
                    // Fetch data for each selected car and aggregate
                    val allCarData = mutableListOf<RefillsTrendData>()

                    for (selectedCarId in _selectedCarIds.value) {
                        getRefillsTrendUseCase(
                            carId = selectedCarId,
                            dateFilter = _dateFilter.value
                        ).first()?.let { data ->
                            allCarData.add(data)
                        }
                    }

                    if (allCarData.isEmpty()) {
                        _uiState.value = RefillsGraphUiState.NoData
                        return@launch
                    }

                    // Aggregate the data from all selected cars
                    val aggregatedData = aggregateRefillsData(allCarData)
                    _uiState.value = RefillsGraphUiState.Success(aggregatedData)
                } else {
                    // Single car or all cars - use standard flow
                    getRefillsTrendUseCase(
                        carId = carsToLoad,
                        dateFilter = _dateFilter.value
                    )
                        .catch { e ->
                            _uiState.value = RefillsGraphUiState.Error(e.message ?: "Unknown error")
                        }
                        .collect { trendData ->
                            _uiState.value = if (trendData == null) {
                                RefillsGraphUiState.NoData
                            } else {
                                RefillsGraphUiState.Success(trendData)
                            }
                        }
                }
            } catch (e: Exception) {
                _uiState.value = RefillsGraphUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    private fun aggregateRefillsData(dataList: List<RefillsTrendData>): RefillsTrendData {
        // Sum up totals from each car
        val totalRefills = dataList.sumOf { it.totalRefills }
        val totalLiters = dataList.sumOf { it.totalLiters }

        // Calculate averages
        val averageLitersPerRefill = if (totalRefills > 0) {
            totalLiters / totalRefills
        } else 0.0

        // Combine and sort recent refills from all cars
        val allRecentRefills = dataList
            .flatMap { it.recentRefills }
            .sortedByDescending { it.date }
            .take(10)

        // Aggregate monthly refills
        val monthlyRefillsMap = mutableMapOf<String, com.agcoding.cartrackingapp.domain.model.MonthlyRefills>()
        for (data in dataList) {
            for (monthly in data.monthlyRefills) {
                val key = "${monthly.month}-${monthly.year}"
                val existing = monthlyRefillsMap[key]
                if (existing != null) {
                    monthlyRefillsMap[key] = existing.copy(
                        refillCount = existing.refillCount + monthly.refillCount,
                        totalLiters = existing.totalLiters + monthly.totalLiters,
                        totalCost = existing.totalCost + monthly.totalCost
                    )
                } else {
                    monthlyRefillsMap[key] = monthly
                }
            }
        }
        val aggregatedMonthlyRefills = monthlyRefillsMap.values
            .sortedBy { it.timestamp }
            .toList()

        // Calculate monthly statistics
        val avgRefillsPerMonth = if (aggregatedMonthlyRefills.isNotEmpty()) {
            aggregatedMonthlyRefills.map { it.refillCount }.average()
        } else 0.0

        val highestMonthRefills = aggregatedMonthlyRefills.maxOfOrNull { it.refillCount } ?: 0
        val lowestMonthRefills = aggregatedMonthlyRefills.filter { it.refillCount > 0 }
            .minOfOrNull { it.refillCount } ?: 0

        // Use the date range from the first data (they should all have the same period)
        val dateRange = dataList.firstOrNull()?.dateRange ?: dataList.first().dateRange

        return RefillsTrendData(
            monthlyRefills = aggregatedMonthlyRefills,
            totalRefills = totalRefills,
            averageRefillsPerMonth = avgRefillsPerMonth,
            highestMonthRefills = highestMonthRefills,
            lowestMonthRefills = lowestMonthRefills,
            totalLiters = totalLiters,
            averageLitersPerRefill = averageLitersPerRefill,
            recentRefills = allRecentRefills,
            dateRange = dateRange
        )
    }

    fun retry() {
        loadTrendData()
    }
}

sealed class RefillsGraphUiState {
    object Loading : RefillsGraphUiState()
    object NoData : RefillsGraphUiState()
    data class Success(val trendData: RefillsTrendData) : RefillsGraphUiState()
    data class Error(val message: String) : RefillsGraphUiState()
}

