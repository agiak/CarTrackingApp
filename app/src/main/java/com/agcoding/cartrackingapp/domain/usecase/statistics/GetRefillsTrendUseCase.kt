package com.agcoding.cartrackingapp.domain.usecase.statistics

import com.agcoding.cartrackingapp.domain.model.DateRange
import com.agcoding.cartrackingapp.domain.model.FuelRefill
import com.agcoding.cartrackingapp.domain.model.MonthlyRefills
import com.agcoding.cartrackingapp.domain.model.RefillItem
import com.agcoding.cartrackingapp.domain.model.RefillsTrendData
import com.agcoding.cartrackingapp.domain.model.TrendPeriod
import com.agcoding.cartrackingapp.domain.repository.CarRepository
import com.agcoding.cartrackingapp.domain.repository.RefillRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

/**
 * Use case to get refills trend data for charts and statistics
 */
class GetRefillsTrendUseCase @Inject constructor(
    private val refillRepository: RefillRepository,
    private val carRepository: CarRepository
) {
    operator fun invoke(
        carId: Long? = null,
        period: TrendPeriod = TrendPeriod.ALL_TIME
    ): Flow<RefillsTrendData?> {
        val refillsFlow = if (carId != null) {
            refillRepository.getRefillsByCarId(carId)
        } else {
            refillRepository.getAllRefills()
        }

        return combine(refillsFlow, carRepository.getAllCars()) { refills, cars ->
            if (refills.isEmpty()) return@combine null

            // Determine date range
            val now = System.currentTimeMillis()
            val earliest = refills.minOfOrNull { it.timestamp } ?: now

            val dateRange = when (period) {
                TrendPeriod.LAST_30_DAYS -> DateRange(now - 30L * 24 * 60 * 60 * 1000, now, "Last 30 Days")
                TrendPeriod.LAST_60_DAYS -> DateRange(now - 60L * 24 * 60 * 60 * 1000, now, "Last 60 Days")
                TrendPeriod.LAST_90_DAYS -> DateRange(now - 90L * 24 * 60 * 60 * 1000, now, "Last 90 Days")
                TrendPeriod.LAST_YEAR -> DateRange(now - 365L * 24 * 60 * 60 * 1000, now, "Last Year")
                TrendPeriod.ALL_TIME -> DateRange(earliest, now, "All Time")
                TrendPeriod.CUSTOM -> DateRange(earliest, now, "Custom Range")
            }

            val totalDays = ((dateRange.endMillis - dateRange.startMillis) / (24 * 60 * 60 * 1000L)).toInt()
            val bucketSize = com.agcoding.cartrackingapp.domain.model.AggregationBucket.forDateRange(totalDays)

            // Filter by date range
            val filteredRefills = refills.filter { it.timestamp in dateRange.startMillis..dateRange.endMillis }

            if (filteredRefills.isEmpty()) return@combine null

            // Calculate monthly refills
            val monthlyRefills = calculateMonthlyRefills(filteredRefills, bucketSize)

            // Calculate statistics
            val totalRefills = filteredRefills.size
            val averageRefillsPerMonth = if (monthlyRefills.isNotEmpty()) {
                monthlyRefills.map { it.refillCount }.average()
            } else 0.0

            val highestMonthRefills = monthlyRefills.maxOfOrNull { it.refillCount } ?: 0
            val lowestMonthRefills = monthlyRefills.filter { it.refillCount > 0 }.minOfOrNull { it.refillCount } ?: 0

            val totalLiters = filteredRefills.sumOf { it.litersAdded }
            val averageLitersPerRefill = if (totalRefills > 0) totalLiters / totalRefills else 0.0

            // Recent refills
            val carMap = cars.associateBy { it.id }
            val recentRefills = filteredRefills
                .sortedByDescending { it.timestamp }
                .take(20)
                .map { refill ->
                    RefillItem(
                        id = refill.id,
                        date = refill.timestamp,
                        liters = refill.litersAdded,
                        cost = refill.amountPaid,
                        pricePerLiter = if (refill.litersAdded > 0) refill.amountPaid / refill.litersAdded else 0.0,
                        carName = carMap[refill.carId]?.name
                    )
                }

            RefillsTrendData(
                monthlyRefills = monthlyRefills,
                totalRefills = totalRefills,
                averageRefillsPerMonth = averageRefillsPerMonth,
                highestMonthRefills = highestMonthRefills,
                lowestMonthRefills = lowestMonthRefills,
                totalLiters = totalLiters,
                averageLitersPerRefill = averageLitersPerRefill,
                recentRefills = recentRefills.take(10),
                dateRange = dateRange
            )
        }
    }

    private fun calculateMonthlyRefills(
        refills: List<FuelRefill>,
        bucketSize: com.agcoding.cartrackingapp.domain.model.AggregationBucket
    ): List<MonthlyRefills> {
        if (refills.isEmpty()) return emptyList()

        val dateFormat = SimpleDateFormat(
            when (bucketSize) {
                com.agcoding.cartrackingapp.domain.model.AggregationBucket.DAILY -> "MMM d"
                com.agcoding.cartrackingapp.domain.model.AggregationBucket.WEEKLY -> "'Week' w"
                com.agcoding.cartrackingapp.domain.model.AggregationBucket.BI_WEEKLY -> "MMM d"
                com.agcoding.cartrackingapp.domain.model.AggregationBucket.MONTHLY -> "MMM"
                com.agcoding.cartrackingapp.domain.model.AggregationBucket.QUARTERLY -> "MMM"
                com.agcoding.cartrackingapp.domain.model.AggregationBucket.YEARLY -> "yyyy"
            },
            Locale.getDefault()
        )

        val earliestTimestamp = refills.minOf { it.timestamp }
        val latestTimestamp = refills.maxOf { it.timestamp }

        val bucketMillis = bucketSize.daysPerBucket * 24 * 60 * 60 * 1000L
        val monthlyRefills = mutableListOf<MonthlyRefills>()

        var currentBucketStart = earliestTimestamp
        val calendar = Calendar.getInstance()

        while (currentBucketStart <= latestTimestamp) {
            val bucketEnd = currentBucketStart + bucketMillis

            val bucketRefills = refills.filter { it.timestamp in currentBucketStart until bucketEnd }

            if (bucketRefills.isNotEmpty()) {
                val refillCount = bucketRefills.size
                val totalLiters = bucketRefills.sumOf { it.litersAdded }
                val totalCost = bucketRefills.sumOf { it.amountPaid }

                calendar.timeInMillis = currentBucketStart
                monthlyRefills.add(
                    MonthlyRefills(
                        month = dateFormat.format(calendar.time),
                        year = calendar.get(Calendar.YEAR),
                        refillCount = refillCount,
                        totalLiters = totalLiters,
                        totalCost = totalCost,
                        timestamp = currentBucketStart
                    )
                )
            }

            currentBucketStart = bucketEnd
        }

        return monthlyRefills
    }
}

