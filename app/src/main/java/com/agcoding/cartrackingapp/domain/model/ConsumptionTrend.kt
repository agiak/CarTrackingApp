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

enum class TrendPeriod(val label: String, val days: Int) {
    ALL_TIME("All Time", -1),
    LAST_30_DAYS("Last 30 Days", 30),
    LAST_60_DAYS("Last 60 Days", 60),
    LAST_90_DAYS("Last 90 Days", 90),
    LAST_YEAR("Last Year", 365),
    CUSTOM("Custom Range", 0)
}

/**
 * Determines the appropriate aggregation bucket size based on date range
 */
enum class AggregationBucket(val daysPerBucket: Int, val minDataPoints: Int) {
    DAILY(1, 7), // Min 7 days of data
    WEEKLY(7, 4), // Min 4 weeks of data
    BI_WEEKLY(14, 6), // Min ~3 months of data
    MONTHLY(30, 3); // Min 3 months of data

    companion object {
        fun forDateRange(totalDays: Int): AggregationBucket {
            return when {
                totalDays <= 30 -> DAILY
                totalDays <= 90 -> WEEKLY
                totalDays <= 365 -> BI_WEEKLY
                else -> MONTHLY
            }
        }
    }
}

