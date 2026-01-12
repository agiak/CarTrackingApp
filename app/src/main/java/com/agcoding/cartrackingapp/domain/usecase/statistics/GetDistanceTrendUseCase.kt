package com.agcoding.cartrackingapp.domain.usecase.statistics

import com.agcoding.cartrackingapp.domain.model.AggregationBucket
import com.agcoding.cartrackingapp.domain.model.DateRange
import com.agcoding.cartrackingapp.domain.model.DistanceDataPoint
import com.agcoding.cartrackingapp.domain.model.DistanceTrendData
import com.agcoding.cartrackingapp.domain.model.FuelRefill
import com.agcoding.cartrackingapp.domain.model.MonthlyDistance
import com.agcoding.cartrackingapp.domain.model.TrendPeriod
import com.agcoding.cartrackingapp.domain.model.TripInfo
import com.agcoding.cartrackingapp.domain.repository.CarRepository
import com.agcoding.cartrackingapp.domain.repository.RefillRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/**
 * Calculates distance trend data with smart aggregation
 */
class GetDistanceTrendUseCase @Inject constructor(
    private val refillRepository: RefillRepository,
    private val carRepository: CarRepository
) {
    operator fun invoke(
        carId: Long? = null,
        period: TrendPeriod = TrendPeriod.ALL_TIME,
        customStartMillis: Long? = null,
        customEndMillis: Long? = null
    ): Flow<DistanceTrendData?> {
        val refillsFlow = if (carId != null) {
            refillRepository.getRefillsByCarId(carId)
        } else {
            refillRepository.getAllRefills()
        }

        return combine(refillsFlow, carRepository.getAllCars()) { refills, cars ->
            if (refills.isEmpty()) return@combine null

            // Determine date range
            val dateRange = calculateDateRange(period, customStartMillis, customEndMillis, refills)

            // Filter refills by date range
            val filteredRefills = refills.filter { refill ->
                refill.timestamp >= dateRange.startMillis &&
                refill.timestamp <= dateRange.endMillis
            }.sortedBy { it.timestamp }

            if (filteredRefills.isEmpty()) return@combine null

            // Calculate aggregation bucket size
            val totalDays = ((dateRange.endMillis - dateRange.startMillis) / (24 * 60 * 60 * 1000L)).toInt()
            val bucketSize = AggregationBucket.forDateRange(totalDays)

            // Aggregate refills into buckets for the line graph
            val dataPoints = aggregateRefills(filteredRefills, bucketSize, dateRange)

            // Calculate monthly distances for bar chart
            val monthlyDistances = calculateMonthlyDistances(filteredRefills)

            // Calculate overall statistics
            val totalDistance = filteredRefills.sumOf { it.tripDistance }
            val averageTripDistance = if (filteredRefills.isNotEmpty()) {
                totalDistance / filteredRefills.size
            } else 0.0

            val distances = filteredRefills.map { it.tripDistance }
            val longestTrip = distances.maxOrNull() ?: 0.0
            val shortestTrip = distances.filter { it > 0 }.minOrNull() ?: 0.0

            // Create car lookup map
            val carMap = cars.associateBy { it.id }

            // Generate color for each car
            val carColors = listOf(
                0xFF4CAF50.toInt(), // Green
                0xFFFF9800.toInt(), // Orange
                0xFF2196F3.toInt(), // Blue
                0xFF9C27B0.toInt(), // Purple
                0xFFE91E63.toInt(), // Pink
                0xFF00BCD4.toInt(), // Cyan
            )
            val carColorMap = cars.mapIndexed { index, car ->
                car.id to carColors[index % carColors.size]
            }.toMap()

            // Create recent trips list (most recent 20)
            val recentTrips = filteredRefills
                .sortedByDescending { it.timestamp }
                .take(20)
                .map { refill ->
                    val car = carMap[refill.carId]
                    TripInfo(
                        refillId = refill.id,
                        carId = refill.carId,
                        carName = car?.name ?: "Unknown",
                        timestamp = refill.timestamp,
                        distance = refill.tripDistance,
                        liters = refill.litersAdded,
                        carColor = carColorMap[refill.carId] ?: 0xFF4CAF50.toInt()
                    )
                }

            DistanceTrendData(
                dataPoints = dataPoints,
                totalDistance = totalDistance,
                averageTripDistance = averageTripDistance,
                longestTrip = longestTrip,
                shortestTrip = shortestTrip,
                totalTrips = filteredRefills.size,
                recentTrips = recentTrips,
                dateRange = dateRange,
                monthlyDistances = monthlyDistances
            )
        }
    }

    private fun calculateDateRange(
        period: TrendPeriod,
        customStartMillis: Long?,
        customEndMillis: Long?,
        refills: List<FuelRefill>
    ): DateRange {
        val now = System.currentTimeMillis()

        return when (period) {
            TrendPeriod.CUSTOM -> {
                require(customStartMillis != null && customEndMillis != null)
                DateRange(customStartMillis, customEndMillis, "Custom Range")
            }
            TrendPeriod.ALL_TIME -> {
                val earliest = refills.minOfOrNull { it.timestamp } ?: now
                DateRange(earliest, now, "All Time")
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
    ): List<DistanceDataPoint> {
        if (refills.isEmpty()) return emptyList()

        val bucketMillis = bucketSize.daysPerBucket * 24 * 60 * 60 * 1000L
        val dataPoints = mutableListOf<DistanceDataPoint>()

        val dateFormat = SimpleDateFormat(
            when (bucketSize) {
                AggregationBucket.DAILY -> "MMM d"
                AggregationBucket.WEEKLY -> "'Week' w"
                AggregationBucket.BI_WEEKLY -> "MMM d"
                AggregationBucket.MONTHLY -> "MMM yyyy"
            },
            Locale.getDefault()
        )

        var currentBucketStart = dateRange.startMillis

        while (currentBucketStart < dateRange.endMillis) {
            val bucketEnd = minOf(currentBucketStart + bucketMillis, dateRange.endMillis)

            val bucketRefills = refills.filter { refill ->
                refill.timestamp >= currentBucketStart && refill.timestamp < bucketEnd
            }

            if (bucketRefills.isNotEmpty()) {
                val totalDistance = bucketRefills.sumOf { it.tripDistance }
                val bucketMiddle = currentBucketStart + (bucketMillis / 2)
                val label = dateFormat.format(Date(bucketMiddle))

                dataPoints.add(
                    DistanceDataPoint(
                        timestamp = bucketMiddle,
                        totalDistance = totalDistance,
                        refillCount = bucketRefills.size,
                        label = label
                    )
                )
            }

            currentBucketStart = bucketEnd
        }

        return dataPoints
    }

    private fun calculateMonthlyDistances(refills: List<FuelRefill>): List<MonthlyDistance> {
        if (refills.isEmpty()) return emptyList()

        val monthFormat = SimpleDateFormat("MMM", Locale.getDefault())
        val calendar = Calendar.getInstance()

        // Group refills by month
        val monthlyGroups = refills.groupBy { refill ->
            calendar.timeInMillis = refill.timestamp
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            Pair(year, month)
        }

        return monthlyGroups.map { (yearMonth, monthRefills) ->
            calendar.set(yearMonth.first, yearMonth.second, 1)
            MonthlyDistance(
                month = monthFormat.format(calendar.time),
                year = yearMonth.first,
                distance = monthRefills.sumOf { it.tripDistance },
                timestamp = calendar.timeInMillis
            )
        }.sortedBy { it.timestamp }
    }
}

