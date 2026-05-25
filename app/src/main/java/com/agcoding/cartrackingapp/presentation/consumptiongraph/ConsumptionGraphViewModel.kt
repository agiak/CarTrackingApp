package com.agcoding.cartrackingapp.presentation.consumptiongraph

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agcoding.cartrackingapp.domain.model.Car
import com.agcoding.cartrackingapp.domain.model.ConsumptionTrendData
import com.agcoding.cartrackingapp.domain.model.TrendPeriod
import com.agcoding.cartrackingapp.domain.repository.CarRepository
import com.agcoding.cartrackingapp.domain.usecase.statistics.GetConsumptionTrendUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConsumptionGraphViewModel @Inject constructor(
    private val getConsumptionTrendUseCase: GetConsumptionTrendUseCase,
    private val carRepository: CarRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val carId: Long? = savedStateHandle.get<String>("carId")?.toLongOrNull()

    private val _uiState = MutableStateFlow<ConsumptionGraphUiState>(ConsumptionGraphUiState.Loading)
    val uiState: StateFlow<ConsumptionGraphUiState> = _uiState.asStateFlow()

    private val _selectedPeriod = MutableStateFlow(TrendPeriod.ALL_TIME)
    val selectedPeriod: StateFlow<TrendPeriod> = _selectedPeriod.asStateFlow()

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
                }
            } catch (e: Exception) {
                android.util.Log.e("ConsumptionGraphVM", "Unexpected error", e)
            }
        }
    }

    fun selectPeriod(period: TrendPeriod) {
        _selectedPeriod.value = period
        _showPeriodSelector.value = false
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
            _uiState.value = ConsumptionGraphUiState.Loading

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
                    val allCarData = mutableListOf<ConsumptionTrendData>()

                    for (selectedCarId in _selectedCarIds.value) {
                        getConsumptionTrendUseCase(
                            carId = selectedCarId,
                            period = _selectedPeriod.value
                        ).first()?.let { data ->
                            allCarData.add(data)
                        }
                    }

                    if (allCarData.isEmpty()) {
                        _uiState.value = ConsumptionGraphUiState.NoData
                        return@launch
                    }

                    // Aggregate the data from all selected cars
                    val aggregatedData = aggregateConsumptionData(allCarData)
                    _uiState.value = ConsumptionGraphUiState.Success(aggregatedData)
                } else {
                    // Single car or all cars - use standard flow
                    getConsumptionTrendUseCase(
                        carId = carsToLoad,
                        period = _selectedPeriod.value
                    )
                        .catch { e ->
                            _uiState.value = ConsumptionGraphUiState.Error(e.message ?: "Failed to load consumption trend")
                        }
                        .collect { trendData ->
                            _uiState.value = if (trendData == null) {
                                ConsumptionGraphUiState.NoData
                            } else {
                                ConsumptionGraphUiState.Success(trendData)
                            }
                        }
                }
            } catch (e: Exception) {
                _uiState.value = ConsumptionGraphUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    private fun aggregateConsumptionData(dataList: List<ConsumptionTrendData>): ConsumptionTrendData {
        // For consumption, we need to calculate weighted average
        // Total refills and total distance across all cars
        val totalRefills = dataList.sumOf { it.totalRefills }

        // Calculate weighted average consumption
        // Sum up (consumption * refillCount) and divide by total refills
        val weightedSum = dataList.sumOf { it.overallAverage * it.totalRefills }
        val overallAverage = if (totalRefills > 0) weightedSum / totalRefills else 0.0

        // Get best and worst consumption across all cars
        val bestConsumption = dataList.filter { it.bestConsumption > 0 }.minOfOrNull { it.bestConsumption } ?: 0.0
        val worstConsumption = dataList.maxOfOrNull { it.worstConsumption } ?: 0.0

        // Determine overall trend
        val trend = when {
            dataList.count { it.trend == com.agcoding.cartrackingapp.domain.model.ConsumptionTrend.IMPROVING } >
            dataList.count { it.trend == com.agcoding.cartrackingapp.domain.model.ConsumptionTrend.WORSENING } ->
                com.agcoding.cartrackingapp.domain.model.ConsumptionTrend.IMPROVING
            dataList.count { it.trend == com.agcoding.cartrackingapp.domain.model.ConsumptionTrend.WORSENING } >
            dataList.count { it.trend == com.agcoding.cartrackingapp.domain.model.ConsumptionTrend.IMPROVING } ->
                com.agcoding.cartrackingapp.domain.model.ConsumptionTrend.WORSENING
            else -> com.agcoding.cartrackingapp.domain.model.ConsumptionTrend.STABLE
        }

        // Aggregate data points by label (time bucket)
        val dataPointsMap = mutableMapOf<String, com.agcoding.cartrackingapp.domain.model.ConsumptionDataPoint>()
        for (data in dataList) {
            for (point in data.dataPoints) {
                val key = point.label
                val existing = dataPointsMap[key]
                if (existing != null) {
                    // Weighted average for consumption
                    val totalCount = existing.refillCount + point.refillCount
                    val weightedAvg = if (totalCount > 0) {
                        (existing.averageConsumption * existing.refillCount +
                         point.averageConsumption * point.refillCount) / totalCount
                    } else 0.0
                    dataPointsMap[key] = existing.copy(
                        averageConsumption = weightedAvg,
                        refillCount = totalCount,
                        totalDistance = existing.totalDistance + point.totalDistance
                    )
                } else {
                    dataPointsMap[key] = point
                }
            }
        }
        val aggregatedDataPoints = dataPointsMap.values
            .sortedBy { it.timestamp }
            .toList()

        // Use the date range from the first data
        val dateRange = dataList.firstOrNull()?.dateRange ?: dataList.first().dateRange

        return ConsumptionTrendData(
            dataPoints = aggregatedDataPoints,
            overallAverage = overallAverage,
            bestConsumption = bestConsumption,
            worstConsumption = worstConsumption,
            trend = trend,
            totalRefills = totalRefills,
            dateRange = dateRange
        )
    }

    fun retry() {
        loadTrendData()
    }
}

sealed class ConsumptionGraphUiState {
    object Loading : ConsumptionGraphUiState()
    object NoData : ConsumptionGraphUiState()
    data class Success(val trendData: ConsumptionTrendData) : ConsumptionGraphUiState()
    data class Error(val message: String) : ConsumptionGraphUiState()
}

