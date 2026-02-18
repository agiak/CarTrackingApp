package com.agcoding.cartrackingapp.presentation.statistics

import com.agcoding.cartrackingapp.domain.model.ForecastResult

/**
 * UI State for Fuel Forecasting feature.
 *
 * Represents forecast data per car in a clean, composable-friendly structure.
 */
data class FuelForecastUiState(
    val carId: Long,
    val carName: String,
    val costPerKmForecast: ForecastResult?,
    val efficiencyForecast: ForecastResult?,
    val carInsights: CarSpecificInsights = CarSpecificInsights()
) {
    val hasSufficientData: Boolean
        get() = costPerKmForecast?.isReliable == true || efficiencyForecast?.isReliable == true

    val showLowDataWarning: Boolean
        get() = (costPerKmForecast?.dataPointsUsed ?: 0) < 3 &&
                (efficiencyForecast?.dataPointsUsed ?: 0) < 3
}

/**
 * Car-specific insights derived from historical data patterns.
 */
data class CarSpecificInsights(
    val hasSummerConsumptionIssue: Boolean = false,
    val hasWinterConsumptionIssue: Boolean = false,
    val isWeatherStable: Boolean = false,
    val hasIncreasingTrend: Boolean = false,
    val hasImprovingTrend: Boolean = false,
    val avgMonthlyVariation: Double = 0.0
)
