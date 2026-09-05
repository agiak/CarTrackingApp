package com.agcoding.cartrackingapp.domain.usecase.statistics

import com.agcoding.cartrackingapp.domain.model.DateFilter
import com.agcoding.cartrackingapp.domain.model.FuelRefill
import com.agcoding.cartrackingapp.domain.model.MonthlyRefills
import com.agcoding.cartrackingapp.domain.model.RefillItem
import com.agcoding.cartrackingapp.domain.model.RefillsTrendData
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
 * Use case to get refills trend data for charts and statistics
 */
class GetRefillsTrendUseCase @Inject constructor(
    private val refillRepository: RefillRepository,
    private val carRepository: CarRepository
) {
    operator fun invoke(
        carId: Long? = null,
        dateFilter: DateFilter = DateFilter.None
    ): Flow<RefillsTrendData?> {
        val refillsFlow = if (carId != null) {
            refillRepository.getRefillsByCarId(carId)
        } else {
            refillRepository.getAllRefills()
        }

        return combine(refillsFlow, carRepository.getAllCars()) { refills, cars ->
            if (refills.isEmpty()) return@combine null

            // Chart only what the filter selected, at a granularity that matches it.
            val window = trendWindowFor(dateFilter, refills.map { it.timestamp })
                ?: return@combine null
            val dateRange = window.dateRange
            val bucketSize = window.bucket

            val filteredRefills = refills.filter { dateFilter.matches(it.timestamp) }

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

        val dateFormat = SimpleDateFormat("MMM d", Locale.getDefault())
        val monthOnlyFormat = SimpleDateFormat("MMM", Locale.getDefault())
        val yearFormat = SimpleDateFormat("yyyy", Locale.getDefault())

        val earliestTimestamp = refills.minOf { it.timestamp }
        val latestTimestamp = refills.maxOf { it.timestamp }

        val monthlyRefills = mutableListOf<MonthlyRefills>()

        // Month-and-longer buckets start on the 1st so a bucket labelled "March"
        // really covers March.
        var currentBucketStart = when (bucketSize) {
            com.agcoding.cartrackingapp.domain.model.AggregationBucket.MONTHLY,
            com.agcoding.cartrackingapp.domain.model.AggregationBucket.QUARTERLY,
            com.agcoding.cartrackingapp.domain.model.AggregationBucket.YEARLY -> startOfMonth(earliestTimestamp)
            else -> earliestTimestamp
        }
        val calendar = Calendar.getInstance()

        while (currentBucketStart <= latestTimestamp) {
            val bucketEnd = nextBucketStart(currentBucketStart, bucketSize)

            val bucketRefills = refills.filter { it.timestamp in currentBucketStart until bucketEnd }

            if (bucketRefills.isNotEmpty()) {
                val refillCount = bucketRefills.size
                val totalLiters = bucketRefills.sumOf { it.litersAdded }
                val totalCost = bucketRefills.sumOf { it.amountPaid }

                calendar.timeInMillis = currentBucketStart

                val label = when (bucketSize) {
                    com.agcoding.cartrackingapp.domain.model.AggregationBucket.DAILY -> dateFormat.format(calendar.time)
                    com.agcoding.cartrackingapp.domain.model.AggregationBucket.WEEKLY,
                    com.agcoding.cartrackingapp.domain.model.AggregationBucket.BI_WEEKLY -> {
                        val start = dateFormat.format(calendar.time)
                        val end = dateFormat.format(Date(bucketEnd - 1000L))
                        "$start - $end"
                    }
                    com.agcoding.cartrackingapp.domain.model.AggregationBucket.MONTHLY -> monthOnlyFormat.format(calendar.time)
                    com.agcoding.cartrackingapp.domain.model.AggregationBucket.QUARTERLY -> monthOnlyFormat.format(calendar.time)
                    com.agcoding.cartrackingapp.domain.model.AggregationBucket.YEARLY -> yearFormat.format(calendar.time)
                }

                monthlyRefills.add(
                    MonthlyRefills(
                        month = label,
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

