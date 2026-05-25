package com.agcoding.cartrackingapp.domain.model

/**
 * Represents a data point in the consumption trend graph
 */
data class ConsumptionDataPoint(
    val timestamp: Long, // Middle of the bucket
    val averageConsumption: Double, // Weighted average L/100km
    val refillCount: Int, // Number of refills in this bucket
    val totalDistance: Double, // Total distance covered in this bucket
    val label: String // Human-readable label (e.g., "Week 1", "Jan 2026")
)

/**
 * Contains all data needed to render the consumption trend graph
 */
data class ConsumptionTrendData(
    val dataPoints: List<ConsumptionDataPoint>,
    val overallAverage: Double,
    val bestConsumption: Double,
    val worstConsumption: Double,
    val trend: ConsumptionTrend, // Overall trend direction
    val totalRefills: Int,
    val dateRange: DateRange
)

enum class ConsumptionTrend {
    IMPROVING, // Consumption decreasing (getting better)
    WORSENING, // Consumption increasing (getting worse)
    STABLE // No significant change
}

data class DateRange(
    val startMillis: Long,
    val endMillis: Long,
    val label: String
)

enum class TrendPeriod(val labelResId: Int, val days: Int) {
    ALL_TIME(com.agcoding.cartrackingapp.R.string.period_all_time, -1),
    LAST_30_DAYS(com.agcoding.cartrackingapp.R.string.period_last_30_days, 30),
    LAST_60_DAYS(com.agcoding.cartrackingapp.R.string.period_last_60_days, 60),
    LAST_90_DAYS(com.agcoding.cartrackingapp.R.string.period_last_90_days, 90),
    LAST_YEAR(com.agcoding.cartrackingapp.R.string.period_last_year, 365),
    CUSTOM(com.agcoding.cartrackingapp.R.string.period_custom_range, 0);

    @Deprecated("Use labelResId with Context instead", ReplaceWith("context.getString(labelResId)"))
    val label: String
        get() = when (this) {
            ALL_TIME -> "All Time"
            LAST_30_DAYS -> "Last 30 Days"
            LAST_60_DAYS -> "Last 60 Days"
            LAST_90_DAYS -> "Last 90 Days"
            LAST_YEAR -> "Last Year"
            CUSTOM -> "Custom Range"
        }
}

/**
 * Determines the appropriate aggregation bucket size based on date range
 */
enum class AggregationBucket(val daysPerBucket: Int, val minDataPoints: Int) {
    DAILY(1, 7), // Min 7 days of data
    WEEKLY(7, 4), // Min 4 weeks of data
    BI_WEEKLY(14, 6), // Min ~3 months of data
    MONTHLY(30, 3), // Min 3 months of data
    QUARTERLY(90, 4), // Min 1 year of data
    YEARLY(365, 2); // Min 2 years of data

    companion object {
        fun forDateRange(totalDays: Int): AggregationBucket {
            return when {
                totalDays <= 45 -> DAILY
                totalDays <= 120 -> WEEKLY
                totalDays <= 365 -> BI_WEEKLY
                totalDays <= 730 -> MONTHLY
                totalDays <= 1825 -> QUARTERLY // Up to 5 years
                else -> YEARLY
            }
        }
    }
}

