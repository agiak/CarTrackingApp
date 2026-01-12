package com.agcoding.cartrackingapp.domain.usecase.statistics

import com.agcoding.cartrackingapp.domain.model.AggregationBucket
import com.agcoding.cartrackingapp.domain.model.ConsumptionDataPoint
import com.agcoding.cartrackingapp.domain.model.ConsumptionTrend
import com.agcoding.cartrackingapp.domain.model.ConsumptionTrendData
import com.agcoding.cartrackingapp.domain.model.DateRange
import com.agcoding.cartrackingapp.domain.model.FuelRefill
import com.agcoding.cartrackingapp.domain.model.TrendPeriod
import com.agcoding.cartrackingapp.domain.repository.RefillRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/**
 * Calculates consumption trend data with smart aggregation
 */
class GetConsumptionTrendUseCase @Inject constructor(
    private val refillRepository: RefillRepository
) {
    operator fun invoke(
        carId: Long? = null,
        period: TrendPeriod = TrendPeriod.ALL_TIME,
        customStartMillis: Long? = null,
        customEndMillis: Long? = null
    ): Flow<ConsumptionTrendData?> {
        return if (carId != null) {
            refillRepository.getRefillsByCarId(carId)
        } else {
            refillRepository.getAllRefills()
        }.map { refills ->
            if (refills.isEmpty()) return@map null

            // Determine date range
            val dateRange = calculateDateRange(period, customStartMillis, customEndMillis)

            // Filter refills by date range
            val filteredRefills = refills.filter { refill ->
                refill.timestamp >= dateRange.startMillis &&
                refill.timestamp <= dateRange.endMillis
            }.sortedBy { it.timestamp }

            if (filteredRefills.isEmpty()) return@map null
            if (filteredRefills.size < 2) return@map null // Need at least 2 refills for a trend

            // Calculate aggregation bucket size
            val totalDays = ((dateRange.endMillis - dateRange.startMillis) / (24 * 60 * 60 * 1000L)).toInt()
            val bucketSize = AggregationBucket.forDateRange(totalDays)

            // Aggregate refills into buckets
            val dataPoints = aggregateRefills(filteredRefills, bucketSize, dateRange)

            if (dataPoints.isEmpty()) return@map null

            // Calculate overall statistics
            val overallAverage = calculateWeightedAverage(filteredRefills)
            val consumptions = filteredRefills.map { it.fuelConsumption }
            val bestConsumption = consumptions.minOrNull() ?: 0.0
            val worstConsumption = consumptions.maxOrNull() ?: 0.0

            // Calculate trend direction
            val trend = calculateTrend(dataPoints)

            ConsumptionTrendData(
                dataPoints = dataPoints,
                overallAverage = overallAverage,
                bestConsumption = bestConsumption,
                worstConsumption = worstConsumption,
                trend = trend,
                totalRefills = filteredRefills.size,
                dateRange = dateRange
            )
        }
    }

    private fun calculateDateRange(
        period: TrendPeriod,
        customStartMillis: Long?,
        customEndMillis: Long?
    ): DateRange {
        val now = System.currentTimeMillis()

        return when (period) {
            TrendPeriod.CUSTOM -> {
                require(customStartMillis != null && customEndMillis != null)
                DateRange(customStartMillis, customEndMillis, "Custom Range")
            }
            TrendPeriod.ALL_TIME -> {
                DateRange(0L, now, "All Time")
            }
            else -> {
                val daysAgo = period.days
                val startMillis = now - (daysAgo * 24 * 60 * 60 * 1000L)
                DateRange(startMillis, now, period.label)
            }
        }
    }

    private fun aggregateRefills(
        refills: List<FuelRefill>,
        bucketSize: AggregationBucket,
        dateRange: DateRange
    ): List<ConsumptionDataPoint> {
        if (refills.isEmpty()) return emptyList()

        val bucketMillis = bucketSize.daysPerBucket * 24 * 60 * 60 * 1000L
        val dataPoints = mutableListOf<ConsumptionDataPoint>()

        // Create buckets
        var currentBucketStart = dateRange.startMillis
        val dateFormat = SimpleDateFormat(
            when (bucketSize) {
                AggregationBucket.DAILY -> "MMM d"
                AggregationBucket.WEEKLY -> "'Week' w"
                AggregationBucket.BI_WEEKLY -> "MMM d"
                AggregationBucket.MONTHLY -> "MMM yyyy"
            },
            Locale.getDefault()
        )

        while (currentBucketStart < dateRange.endMillis) {
            val bucketEnd = minOf(currentBucketStart + bucketMillis, dateRange.endMillis)

            // Get refills in this bucket
            val bucketRefills = refills.filter { refill ->
                refill.timestamp >= currentBucketStart && refill.timestamp < bucketEnd
            }

            if (bucketRefills.isNotEmpty()) {
                // Calculate weighted average consumption for this bucket
                val totalDistance = bucketRefills.sumOf { it.tripDistance }
                val totalLiters = bucketRefills.sumOf { it.litersAdded }

                val weightedAverage = if (totalDistance > 0) {
                    (totalLiters / totalDistance) * 100.0
                } else {
                    // Fallback to simple average if no distance data
                    bucketRefills.map { it.fuelConsumption }.average()
                }

                // Use middle of bucket as timestamp
                val bucketMiddle = currentBucketStart + (bucketMillis / 2)
                val label = dateFormat.format(Date(bucketMiddle))

                dataPoints.add(
                    ConsumptionDataPoint(
                        timestamp = bucketMiddle,
                        averageConsumption = weightedAverage,
                        refillCount = bucketRefills.size,
                        totalDistance = totalDistance,
                        label = label
                    )
                )
            }

            currentBucketStart = bucketEnd
        }

        return dataPoints
    }

    private fun calculateWeightedAverage(refills: List<FuelRefill>): Double {
        val totalDistance = refills.sumOf { it.tripDistance }
        val totalLiters = refills.sumOf { it.litersAdded }

        return if (totalDistance > 0) {
            (totalLiters / totalDistance) * 100.0
        } else {
            refills.map { it.fuelConsumption }.average()
        }
    }

    private fun calculateTrend(dataPoints: List<ConsumptionDataPoint>): ConsumptionTrend {
        if (dataPoints.size < 3) return ConsumptionTrend.STABLE

        // Use simple linear regression to determine trend
        val n = dataPoints.size
        val xValues = dataPoints.indices.map { it.toDouble() }
        val yValues = dataPoints.map { it.averageConsumption }

        val xMean = xValues.average()
        val yMean = yValues.average()

        val numerator = xValues.zip(yValues).sumOf { (x, y) ->
            (x - xMean) * (y - yMean)
        }
        val denominator = xValues.sumOf { x ->
            (x - xMean) * (x - xMean)
        }

        val slope = if (denominator != 0.0) numerator / denominator else 0.0

        // Determine trend based on slope and threshold
        // Threshold: 0.1 L/100km change per bucket is considered significant
        val threshold = 0.1

        return when {
            slope < -threshold -> ConsumptionTrend.IMPROVING // Consumption decreasing
            slope > threshold -> ConsumptionTrend.WORSENING // Consumption increasing
            else -> ConsumptionTrend.STABLE
        }
    }
}

