package com.agcoding.cartrackingapp.domain.usecase.statistics

import com.agcoding.cartrackingapp.domain.model.AggregationBucket
import com.agcoding.cartrackingapp.domain.model.ConsumptionDataPoint
import com.agcoding.cartrackingapp.domain.model.ConsumptionTrend
import com.agcoding.cartrackingapp.domain.model.ConsumptionTrendData
import com.agcoding.cartrackingapp.domain.model.DateFilter
import com.agcoding.cartrackingapp.domain.model.DateRange
import com.agcoding.cartrackingapp.domain.model.FuelRefill
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
        dateFilter: DateFilter = DateFilter.None
    ): Flow<ConsumptionTrendData?> {
        return if (carId != null) {
            refillRepository.getRefillsByCarId(carId)
        } else {
            refillRepository.getAllRefills()
        }.map { refills ->
            if (refills.isEmpty()) return@map null

            // Chart only what the filter selected, at a granularity that matches it.
            val window = trendWindowFor(dateFilter, refills.map { it.timestamp })
                ?: return@map null
            val dateRange = window.dateRange

            val filteredRefills = refills
                .filter { dateFilter.matches(it.timestamp) }
                .sortedBy { it.timestamp }

            if (filteredRefills.size < 2) return@map null // Need at least 2 refills for a trend

            // Aggregate refills into buckets
            val dataPoints = aggregateRefills(filteredRefills, window.bucket, dateRange)

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

    private fun aggregateRefills(
        refills: List<FuelRefill>,
        bucketSize: AggregationBucket,
        dateRange: DateRange
    ): List<ConsumptionDataPoint> {
        if (refills.isEmpty()) return emptyList()

        val dataPoints = mutableListOf<ConsumptionDataPoint>()

        // Create buckets
        var currentBucketStart = dateRange.startMillis
        val dateFormat = SimpleDateFormat("MMM d", Locale.getDefault())
        val yearFormat = SimpleDateFormat("yyyy", Locale.getDefault())
        val monthYearFormat = SimpleDateFormat("MMM yyyy", Locale.getDefault())

        while (currentBucketStart < dateRange.endMillis) {
            val nextStart = nextBucketStart(currentBucketStart, bucketSize)
            val bucketEnd = minOf(nextStart, dateRange.endMillis)

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
                val bucketMiddle = currentBucketStart + (bucketEnd - currentBucketStart) / 2

                val label = when (bucketSize) {
                    AggregationBucket.DAILY -> dateFormat.format(Date(bucketMiddle))
                    AggregationBucket.WEEKLY, AggregationBucket.BI_WEEKLY -> {
                        val start = dateFormat.format(Date(currentBucketStart))
                        val end = dateFormat.format(Date(bucketEnd - 1000L))
                        "$start - $end"
                    }
                    AggregationBucket.MONTHLY, AggregationBucket.QUARTERLY -> monthYearFormat.format(Date(bucketMiddle))
                    AggregationBucket.YEARLY -> yearFormat.format(Date(bucketMiddle))
                }

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

            currentBucketStart = nextStart
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

