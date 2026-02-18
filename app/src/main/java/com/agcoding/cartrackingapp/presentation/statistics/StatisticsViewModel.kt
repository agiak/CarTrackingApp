package com.agcoding.cartrackingapp.presentation.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agcoding.cartrackingapp.data.preferences.SettingsPreferences
import com.agcoding.cartrackingapp.domain.model.ForecastResult
import com.agcoding.cartrackingapp.domain.model.FuelRefill
import com.agcoding.cartrackingapp.domain.model.GlobalStatistics
import com.agcoding.cartrackingapp.domain.repository.CarRepository
import com.agcoding.cartrackingapp.domain.repository.RefillRepository
import com.agcoding.cartrackingapp.domain.usecase.forecast.ForecastFuelCostPerKmUseCase
import com.agcoding.cartrackingapp.domain.usecase.forecast.ForecastFuelEfficiencyUseCase
import com.agcoding.cartrackingapp.domain.usecase.statistics.GetGlobalStatisticsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val getGlobalStatisticsUseCase: GetGlobalStatisticsUseCase,
    private val forecastCostPerKmUseCase: ForecastFuelCostPerKmUseCase,
    private val forecastEfficiencyUseCase: ForecastFuelEfficiencyUseCase,
    private val carRepository: CarRepository,
    private val refillRepository: RefillRepository,
    private val settingsPreferences: SettingsPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow<StatisticsUiState>(StatisticsUiState.Loading)
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    private val _forecastUiState = MutableStateFlow<List<FuelForecastUiState>>(emptyList())
    val forecastUiState: StateFlow<List<FuelForecastUiState>> = _forecastUiState.asStateFlow()

    private val _forecastingEnabled = MutableStateFlow(true)
    val forecastingEnabled: StateFlow<Boolean> = _forecastingEnabled.asStateFlow()

    init {
        loadStatistics()
        observeForecastingSettings()
        loadForecasts()
    }

    private fun loadStatistics() {
        viewModelScope.launch {
            getGlobalStatisticsUseCase()
                .catch { e ->
                    _uiState.value = StatisticsUiState.Error(e.message ?: "Unknown error")
                }
                .collect { statistics ->
                    _uiState.value = StatisticsUiState.Success(statistics)
                }
        }
    }

    private fun observeForecastingSettings() {
        viewModelScope.launch {
            settingsPreferences.settingsFlow
                .collect { settings ->
                    _forecastingEnabled.value = settings.forecastingEnabled
                }
        }
    }

    private fun loadForecasts() {
        viewModelScope.launch {
            // Observe both cars and refills, recompute when either changes
            combine(
                carRepository.getAllCars(),
                refillRepository.getAllRefills(),
                settingsPreferences.settingsFlow
            ) { cars, refills, settings ->
                Triple(cars, refills, settings.forecastingEnabled)
            }.collect { (cars, refills, enabled) ->
                if (!enabled || cars.isEmpty()) {
                    _forecastUiState.value = emptyList()
                    return@collect
                }

                // Compute forecasts on background dispatcher
                val forecasts = withContext(Dispatchers.Default) {
                    cars.map { car ->
                        val carRefills = refills.filter { it.carId == car.id }
                            .sortedBy { it.timestamp }

                        val costPerKmForecast = forecastCostPerKmUseCase(carRefills)
                        val efficiencyForecast = forecastEfficiencyUseCase(carRefills)

                        // Calculate car-specific insights
                        val insights = calculateCarInsights(carRefills, efficiencyForecast)

                        FuelForecastUiState(
                            carId = car.id,
                            carName = car.name,
                            costPerKmForecast = costPerKmForecast,
                            efficiencyForecast = efficiencyForecast,
                            carInsights = insights
                        )
                    }.filter { it.hasSufficientData || it.showLowDataWarning }
                }

                // Hide feature if ALL cars have very low confidence (<20%)
                val hasAnyReliableData = forecasts.any { forecast ->
                    val costConfidence = forecast.costPerKmForecast?.confidence ?: 0.0
                    val efficiencyConfidence = forecast.efficiencyForecast?.confidence ?: 0.0
                    val maxConfidence = maxOf(costConfidence, efficiencyConfidence)
                    maxConfidence >= 0.20 // At least 20% confidence
                }

                _forecastUiState.value = if (hasAnyReliableData) forecasts else emptyList()
            }
        }
    }

    /**
     * Analyze car refill patterns to generate personalized insights.
     */
    private fun calculateCarInsights(
        refills: List<FuelRefill>,
        efficiencyForecast: ForecastResult?
    ): CarSpecificInsights {
        if (refills.size < 3) return CarSpecificInsights()

        // Group refills by month
        val refillsByMonth = refills.groupBy {
            val date = java.time.Instant.ofEpochMilli(it.timestamp)
                .atZone(java.time.ZoneId.systemDefault())
                .toLocalDate()
            date.month.value
        }

        // Calculate average efficiency per month
        val monthlyEfficiency = refillsByMonth.mapValues { (_, monthRefills) ->
            val validRefills = monthRefills.filter { it.tripDistance > 0 && it.litersAdded > 0 }
            if (validRefills.isEmpty()) return@mapValues null

            val totalDistance = validRefills.sumOf { it.tripDistance }
            val totalLiters = validRefills.sumOf { it.litersAdded }
            (totalLiters / totalDistance) * 100
        }.filterValues { it != null }.mapValues { it.value!! }

        if (monthlyEfficiency.isEmpty()) return CarSpecificInsights()

        // Summer months: June, July, August (6, 7, 8)
        val summerMonths = listOf(6, 7, 8)
        val winterMonths = listOf(12, 1, 2)

        val summerEfficiency = monthlyEfficiency.filterKeys { it in summerMonths }.values
        val winterEfficiency = monthlyEfficiency.filterKeys { it in winterMonths }.values
        val otherMonthsEfficiency = monthlyEfficiency.filterKeys { it !in summerMonths && it !in winterMonths }.values

        val avgSummer = if (summerEfficiency.isNotEmpty()) summerEfficiency.average() else null
        val avgWinter = if (winterEfficiency.isNotEmpty()) winterEfficiency.average() else null
        val avgOther = if (otherMonthsEfficiency.isNotEmpty()) otherMonthsEfficiency.average() else null

        // Detect seasonal issues (>10% worse than other months)
        val hasSummerIssue = avgSummer != null && avgOther != null && avgSummer > avgOther * 1.1
        val hasWinterIssue = avgWinter != null && avgOther != null && avgWinter > avgOther * 1.1

        // Weather stable if all seasons are within 5% of each other
        val isWeatherStable = if (avgSummer != null && avgWinter != null && avgOther != null) {
            val max = maxOf(avgSummer, avgWinter, avgOther)
            val min = minOf(avgSummer, avgWinter, avgOther)
            (max - min) / min < 0.05
        } else false

        // Calculate monthly variation
        val avgEfficiency = monthlyEfficiency.values.average()
        val variation = if (monthlyEfficiency.size > 1) {
            monthlyEfficiency.values.map { kotlin.math.abs(it - avgEfficiency) / avgEfficiency }.average()
        } else 0.0

        // Trend detection
        val trend = efficiencyForecast?.trend ?: 0.0
        val hasIncreasingTrend = trend > 0.001
        val hasImprovingTrend = trend < -0.001

        return CarSpecificInsights(
            hasSummerConsumptionIssue = hasSummerIssue,
            hasWinterConsumptionIssue = hasWinterIssue,
            isWeatherStable = isWeatherStable,
            hasIncreasingTrend = hasIncreasingTrend,
            hasImprovingTrend = hasImprovingTrend,
            avgMonthlyVariation = variation
        )
    }
}

sealed class StatisticsUiState {
    object Loading : StatisticsUiState()
    data class Success(val statistics: GlobalStatistics) : StatisticsUiState()
    data class Error(val message: String) : StatisticsUiState()
}

