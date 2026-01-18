package com.agcoding.cartrackingapp.presentation.distancegraph

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agcoding.cartrackingapp.domain.model.Car
import com.agcoding.cartrackingapp.domain.model.DistanceTrendData
import com.agcoding.cartrackingapp.domain.model.TrendPeriod
import com.agcoding.cartrackingapp.domain.repository.CarRepository
import com.agcoding.cartrackingapp.domain.usecase.statistics.GetDistanceTrendUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DistanceGraphViewModel @Inject constructor(
    private val getDistanceTrendUseCase: GetDistanceTrendUseCase,
    private val carRepository: CarRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val carId: Long? = savedStateHandle.get<String>("carId")?.toLongOrNull()

    private val _uiState = MutableStateFlow<DistanceGraphUiState>(DistanceGraphUiState.Loading)
    val uiState: StateFlow<DistanceGraphUiState> = _uiState.asStateFlow()

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
                // If coming from single car, pre-select it
                if (carId != null) {
                    _selectedCarIds.value = setOf(carId)
                }
            } catch (e: Exception) {
                e.printStackTrace()
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
            _uiState.value = DistanceGraphUiState.Loading

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
                    val allCarData = mutableListOf<DistanceTrendData>()

                    for (selectedCarId in _selectedCarIds.value) {
                        getDistanceTrendUseCase(
                            carId = selectedCarId,
                            period = _selectedPeriod.value
                        ).first()?.let { data ->
                            allCarData.add(data)
                        }
                    }

                    if (allCarData.isEmpty()) {
                        _uiState.value = DistanceGraphUiState.NoData
                        return@launch
                    }

                    // Aggregate the data from all selected cars
                    val aggregatedData = aggregateDistanceData(allCarData)
                    _uiState.value = DistanceGraphUiState.Success(aggregatedData)
                } else {
                    // Single car or all cars - use standard flow
                    getDistanceTrendUseCase(
                        carId = carsToLoad,
                        period = _selectedPeriod.value
                    )
                        .catch { e ->
                            _uiState.value = DistanceGraphUiState.Error(e.message ?: "")
                        }
                        .collect { trendData ->
                            _uiState.value = if (trendData == null) {
                                DistanceGraphUiState.NoData
                            } else {
                                DistanceGraphUiState.Success(trendData)
                            }
                        }
                }
            } catch (e: Exception) {
                _uiState.value = DistanceGraphUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    private fun aggregateDistanceData(dataList: List<DistanceTrendData>): DistanceTrendData {
        // Sum up total distances from each car
        val totalDistance = dataList.sumOf { it.totalDistance }
        val totalTrips = dataList.sumOf { it.totalTrips }

        // Calculate average trip distance
        val averageTripDistance = if (totalTrips > 0) {
            totalDistance / totalTrips
        } else 0.0

        // Get longest and shortest trips across all cars
        val longestTrip = dataList.maxOfOrNull { it.longestTrip } ?: 0.0
        val shortestTrip = dataList.filter { it.shortestTrip > 0 }.minOfOrNull { it.shortestTrip } ?: 0.0

        // Combine and sort recent trips from all cars
        val allRecentTrips = dataList
            .flatMap { it.recentTrips }
            .sortedByDescending { it.timestamp }
            .take(20)

        // Aggregate monthly distances
        val monthlyDistancesMap = mutableMapOf<String, com.agcoding.cartrackingapp.domain.model.MonthlyDistance>()
        for (data in dataList) {
            for (monthly in data.monthlyDistances) {
                val key = "${monthly.month}-${monthly.year}"
                val existing = monthlyDistancesMap[key]
                if (existing != null) {
                    monthlyDistancesMap[key] = existing.copy(
                        distance = existing.distance + monthly.distance
                    )
                } else {
                    monthlyDistancesMap[key] = monthly
                }
            }
        }
        val aggregatedMonthlyDistances = monthlyDistancesMap.values
            .sortedBy { it.timestamp }
            .toList()

        // Aggregate data points
        val dataPointsMap = mutableMapOf<String, com.agcoding.cartrackingapp.domain.model.DistanceDataPoint>()
        for (data in dataList) {
            for (point in data.dataPoints) {
                val key = point.label
                val existing = dataPointsMap[key]
                if (existing != null) {
                    dataPointsMap[key] = existing.copy(
                        totalDistance = existing.totalDistance + point.totalDistance,
                        refillCount = existing.refillCount + point.refillCount
                    )
                } else {
                    dataPointsMap[key] = point
                }
            }
        }
        val aggregatedDataPoints = dataPointsMap.values
            .sortedBy { it.timestamp }
            .toList()

        // Use the date range from the first data (they should all have the same period)
        val dateRange = dataList.firstOrNull()?.dateRange ?: dataList.first().dateRange

        return DistanceTrendData(
            dataPoints = aggregatedDataPoints,
            totalDistance = totalDistance,
            averageTripDistance = averageTripDistance,
            longestTrip = longestTrip,
            shortestTrip = shortestTrip,
            totalTrips = totalTrips,
            recentTrips = allRecentTrips,
            dateRange = dateRange,
            monthlyDistances = aggregatedMonthlyDistances
        )
    }

    fun retry() {
        loadTrendData()
    }
}

sealed class DistanceGraphUiState {
    object Loading : DistanceGraphUiState()
    object NoData : DistanceGraphUiState()
    data class Success(val trendData: DistanceTrendData) : DistanceGraphUiState()
    data class Error(val message: String) : DistanceGraphUiState()
}
