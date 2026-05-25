package com.agcoding.cartrackingapp.presentation.costgraph

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agcoding.cartrackingapp.domain.model.Car
import com.agcoding.cartrackingapp.domain.model.CostTrendData
import com.agcoding.cartrackingapp.domain.model.TrendPeriod
import com.agcoding.cartrackingapp.domain.repository.CarRepository
import com.agcoding.cartrackingapp.domain.usecase.statistics.GetCostTrendUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CostGraphViewModel @Inject constructor(
    private val getCostTrendUseCase: GetCostTrendUseCase,
    private val carRepository: CarRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val carId: Long? = savedStateHandle.get<String>("carId")?.toLongOrNull()

    private val _uiState = MutableStateFlow<CostGraphUiState>(CostGraphUiState.Loading)
    val uiState: StateFlow<CostGraphUiState> = _uiState.asStateFlow()

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
                android.util.Log.e("CostGraphVM", "Unexpected error", e)
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
            _uiState.value = CostGraphUiState.Loading

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
                    val allCarData = mutableListOf<CostTrendData>()

                    for (selectedCarId in _selectedCarIds.value) {
                        getCostTrendUseCase(
                            carId = selectedCarId,
                            period = _selectedPeriod.value
                        ).first()?.let { data ->
                            allCarData.add(data)
                        }
                    }

                    if (allCarData.isEmpty()) {
                        _uiState.value = CostGraphUiState.NoData
                        return@launch
                    }

                    // Aggregate the data from all selected cars
                    val aggregatedData = aggregateCostData(allCarData)
                    _uiState.value = CostGraphUiState.Success(aggregatedData)
                } else {
                    // Single car or all cars - use standard flow
                    getCostTrendUseCase(
                        carId = carsToLoad,
                        period = _selectedPeriod.value
                    )
                        .catch { e ->
                            _uiState.value = CostGraphUiState.Error(e.message ?: "Unknown error")
                        }
                        .collect { trendData ->
                            _uiState.value = if (trendData == null) {
                                CostGraphUiState.NoData
                            } else {
                                CostGraphUiState.Success(trendData)
                            }
                        }
                }
            } catch (e: Exception) {
                _uiState.value = CostGraphUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    private fun aggregateCostData(dataList: List<CostTrendData>): CostTrendData {
        // Sum up total costs from each car
        val totalCost = dataList.sumOf { it.totalCost }

        // Combine and sort recent expenses from all cars
        val allRecentExpenses = dataList
            .flatMap { it.recentExpenses }
            .sortedByDescending { it.date }
            .take(10)

        // Aggregate monthly costs
        val monthlyCostsMap = mutableMapOf<String, com.agcoding.cartrackingapp.domain.model.MonthlyCost>()
        for (data in dataList) {
            for (monthly in data.monthlyCosts) {
                val key = "${monthly.month}-${monthly.year}"
                val existing = monthlyCostsMap[key]
                if (existing != null) {
                    monthlyCostsMap[key] = existing.copy(
                        totalCost = existing.totalCost + monthly.totalCost,
                        fuelCost = existing.fuelCost + monthly.fuelCost,
                        serviceCost = existing.serviceCost + monthly.serviceCost,
                        otherCost = existing.otherCost + monthly.otherCost
                    )
                } else {
                    monthlyCostsMap[key] = monthly
                }
            }
        }
        val aggregatedMonthlyCosts = monthlyCostsMap.values
            .sortedBy { it.timestamp }
            .toList()

        // Calculate monthly statistics
        val avgMonthlyCost = if (aggregatedMonthlyCosts.isNotEmpty()) {
            aggregatedMonthlyCosts.map { it.totalCost }.average()
        } else 0.0

        val highestMonthCost = aggregatedMonthlyCosts.maxOfOrNull { it.totalCost } ?: 0.0
        val lowestMonthCost = aggregatedMonthlyCosts.filter { it.totalCost > 0 }
            .minOfOrNull { it.totalCost } ?: 0.0

        // Aggregate cost by category
        val categoryMap = mutableMapOf<String, Double>()
        for (data in dataList) {
            for (category in data.costByCategory) {
                categoryMap[category.name] = (categoryMap[category.name] ?: 0.0) + category.amount
            }
        }

        // Recalculate percentages based on new total
        val aggregatedCategories = categoryMap.map { (name, amount) ->
            val percentage = if (totalCost > 0) (amount / totalCost) * 100 else 0.0
            // Get color from first matching category
            val color = dataList.flatMap { it.costByCategory }
                .firstOrNull { it.name == name }?.color ?: 0
            com.agcoding.cartrackingapp.domain.model.CostCategory(
                name = name,
                amount = amount,
                percentage = percentage,
                color = color
            )
        }.sortedByDescending { it.amount }

        // Use the date range from the first data (they should all have the same period)
        val dateRange = dataList.firstOrNull()?.dateRange ?: dataList.first().dateRange

        return CostTrendData(
            monthlyCosts = aggregatedMonthlyCosts,
            totalCost = totalCost,
            averageMonthlyCost = avgMonthlyCost,
            highestMonthCost = highestMonthCost,
            lowestMonthCost = lowestMonthCost,
            costByCategory = aggregatedCategories,
            recentExpenses = allRecentExpenses,
            dateRange = dateRange
        )
    }

    fun retry() {
        loadTrendData()
    }
}

sealed class CostGraphUiState {
    object Loading : CostGraphUiState()
    object NoData : CostGraphUiState()
    data class Success(val trendData: CostTrendData) : CostGraphUiState()
    data class Error(val message: String) : CostGraphUiState()
}
