package com.agcoding.cartrackingapp.domain.model

/**
 * Represents yearly statistics for comparison
 */
data class YearlyData(
    val year: Int,
    val totalCost: Double,
    val totalDistance: Double,
    val totalFuelLiters: Double,
    val totalRefills: Int,
    val totalExpenses: Int,
    val monthlyCosts: List<MonthlyYearData>
) {
    val averageConsumption: Double
        get() = if (totalDistance > 0) (totalFuelLiters / totalDistance) * 100 else 0.0

    val costPerKm: Double
        get() = if (totalDistance > 0) totalCost / totalDistance else 0.0
}

/**
 * Monthly data for a specific year
 */
data class MonthlyYearData(
    val month: Int, // 1-12
    val monthName: String,
    val totalCost: Double,
    val fuelCost: Double,
    val expenseCost: Double,
    val distance: Double,
    val consumption: Double
)

/**
 * Comparison metric with calculated differences
 */
data class ComparisonMetric(
    val name: String,
    val year1Value: Double,
    val year2Value: Double,
    val unit: String,
    val year1FormattedValue: String,
    val year2FormattedValue: String
) {
    val absoluteDifference: Double
        get() = year2Value - year1Value

    val percentageChange: Double
        get() = if (year1Value > 0) ((year2Value - year1Value) / year1Value) * 100 else 0.0

    val isIncrease: Boolean
        get() = absoluteDifference > 0

    val isImprovement: Boolean
        get() = when (name) {
            "Average Consumption", "Cost per km" -> absoluteDifference < 0 // Lower is better
            else -> false // For cost/distance, no inherent "better"
        }
}

/**
 * Complete yearly comparison data
 */
data class YearlyComparisonData(
    val year1Data: YearlyData,
    val year2Data: YearlyData,
    val metrics: List<ComparisonMetric>
)

/**
 * Available years with data
 */
data class AvailableYear(
    val year: Int,
    val hasData: Boolean,
    val transactionCount: Int
)

