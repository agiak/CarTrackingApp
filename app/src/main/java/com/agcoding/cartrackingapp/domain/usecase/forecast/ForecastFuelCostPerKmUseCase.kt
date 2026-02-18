package com.agcoding.cartrackingapp.domain.usecase.forecast

import com.agcoding.cartrackingapp.domain.forecast.HoltLinearSmoothingEngine
import com.agcoding.cartrackingapp.domain.model.ForecastResult
import com.agcoding.cartrackingapp.domain.model.FuelRefill
import com.agcoding.cartrackingapp.domain.model.MonthlyFuelMetric
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneId
import javax.inject.Inject

/**
 * Forecasts monthly fuel cost per kilometer using Holt's Linear Exponential Smoothing.
 *
 * This use case:
 * - Aggregates fuel refills by month
 * - Calculates cost per km for each month
 * - Applies Holt's forecasting algorithm
 * - Returns prediction for next month
 *
 * Handles edge cases:
 * - Missing months (treated sequentially)
 * - Extreme outliers (filtered)
 * - Insufficient data (returns low confidence)
 */
class ForecastFuelCostPerKmUseCase @Inject constructor(
    private val forecastEngine: HoltLinearSmoothingEngine
) {
    /**
     * Forecasts next month's fuel cost per km for a specific car.
     *
     * @param refills All fuel refills for the car, chronologically sorted
     * @return ForecastResult with prediction and confidence
     */
    operator fun invoke(refills: List<FuelRefill>): ForecastResult {
        if (refills.isEmpty()) {
            return ForecastResult(
                predictedNextValue = 0.0,
                trend = 0.0,
                confidence = 0.0,
                dataPointsUsed = 0
            )
        }

        // Aggregate refills by month
        val monthlyMetrics = aggregateByMonth(refills)

        if (monthlyMetrics.isEmpty()) {
            return ForecastResult(
                predictedNextValue = 0.0,
                trend = 0.0,
                confidence = 0.0,
                dataPointsUsed = 0
            )
        }

        // Extract cost per km values in chronological order
        val costPerKmValues = monthlyMetrics
            .sortedBy { it.month }
            .map { it.costPerKm }
            .filter { it > 0.0 }  // Filter invalid values

        if (costPerKmValues.isEmpty()) {
            return ForecastResult(
                predictedNextValue = 0.0,
                trend = 0.0,
                confidence = 0.0,
                dataPointsUsed = 0
            )
        }

        // Filter outliers (Case 5)
        val filteredValues = forecastEngine.filterOutliers(costPerKmValues)

        // Apply Holt's Linear Smoothing
        return forecastEngine.forecast(filteredValues)
    }

    /**
     * Aggregates fuel refills by month and calculates cost per km.
     *
     * Strategy for missing months (Case 6):
     * - Treats data sequentially without filling gaps
     * - Only includes months with actual refill data
     *
     * @param refills Fuel refills to aggregate
     * @return List of monthly metrics
     */
    private fun aggregateByMonth(refills: List<FuelRefill>): List<MonthlyFuelMetric> {
        val monthlyData = mutableMapOf<YearMonth, MutableList<FuelRefill>>()

        // Group refills by month
        refills.forEach { refill ->
            val instant = Instant.ofEpochMilli(refill.timestamp)
            val yearMonth = YearMonth.from(instant.atZone(ZoneId.systemDefault()))
            monthlyData.getOrPut(yearMonth) { mutableListOf() }.add(refill)
        }

        // Calculate monthly metrics
        return monthlyData.map { (yearMonth, monthRefills) ->
            val totalCost = monthRefills.sumOf { it.amountPaid }
            val totalDistance = monthRefills.sumOf { it.tripDistance }
            val totalLiters = monthRefills.sumOf { it.litersAdded }

            val costPerKm = if (totalDistance > 0) totalCost / totalDistance else 0.0
            val efficiency = if (totalDistance > 0) (totalLiters / totalDistance) * 100 else 0.0

            MonthlyFuelMetric(
                month = yearMonth,
                costPerKm = costPerKm,
                efficiency = efficiency
            )
        }.filter { it.costPerKm > 0 }  // Only include valid months
    }
}

