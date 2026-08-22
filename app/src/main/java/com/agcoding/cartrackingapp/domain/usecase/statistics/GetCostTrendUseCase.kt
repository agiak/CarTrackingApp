package com.agcoding.cartrackingapp.domain.usecase.statistics

import com.agcoding.cartrackingapp.domain.model.CostCategory
import com.agcoding.cartrackingapp.domain.model.CostItem
import com.agcoding.cartrackingapp.domain.model.CostTrendData
import com.agcoding.cartrackingapp.domain.model.DateRange
import com.agcoding.cartrackingapp.domain.model.ExpenseCategories
import com.agcoding.cartrackingapp.domain.model.MonthlyCost
import com.agcoding.cartrackingapp.domain.model.TrendPeriod
import com.agcoding.cartrackingapp.domain.repository.CarRepository
import com.agcoding.cartrackingapp.domain.repository.ExpenseRepository
import com.agcoding.cartrackingapp.domain.repository.RefillRepository
import com.agcoding.cartrackingapp.util.formatNumber
import com.agcoding.cartrackingapp.util.safeDivide
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/**
 * Use case to get cost trend data for charts and statistics
 */
class GetCostTrendUseCase @Inject constructor(
    private val refillRepository: RefillRepository,
    private val expenseRepository: ExpenseRepository,
    private val carRepository: CarRepository
) {
    operator fun invoke(
        carId: Long? = null,
        period: TrendPeriod = TrendPeriod.ALL_TIME
    ): Flow<CostTrendData?> {
        val refillsFlow = if (carId != null) {
            refillRepository.getRefillsByCarId(carId)
        } else {
            refillRepository.getAllRefills()
        }

        val expensesFlow = if (carId != null) {
            expenseRepository.getExpensesByCarId(carId)
        } else {
            expenseRepository.getAllExpenses()
        }

        return combine(refillsFlow, expensesFlow, carRepository.getAllCars()) { refills, expenses, cars ->
            if (refills.isEmpty() && expenses.isEmpty()) return@combine null

            // Determine date range
            val now = System.currentTimeMillis()
            val allTimestamps = (refills.map { it.timestamp } + expenses.map { it.timestamp })

            if (allTimestamps.isEmpty()) return@combine null

            val earliest = allTimestamps.minOrNull() ?: now

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
            val filteredExpenses = expenses.filter { it.timestamp in dateRange.startMillis..dateRange.endMillis }

            // Calculate monthly costs
            val monthlyCosts = calculateMonthlyCosts(filteredRefills, filteredExpenses, bucketSize)

            // Calculate totals
            val totalFuelCost = filteredRefills.sumOf { it.amountPaid }
            val totalServiceCost = filteredExpenses.filter { ExpenseCategories.isServiceCategory(it.category) }.sumOf { it.amount }
            val totalOtherCost = filteredExpenses.filter { !ExpenseCategories.isServiceCategory(it.category) }.sumOf { it.amount }
            val totalCost = totalFuelCost + totalServiceCost + totalOtherCost

            // Cost by category
            val costByCategory = listOf(
                CostCategory("Fuel", totalFuelCost, totalFuelCost.safeDivide(totalCost) * 100, 0xFF4CAF50.toInt()),
                CostCategory("Service", totalServiceCost, totalServiceCost.safeDivide(totalCost) * 100, 0xFFFF9800.toInt()),
                CostCategory("Other", totalOtherCost, totalOtherCost.safeDivide(totalCost) * 100, 0xFF2196F3.toInt())
            ).filter { it.amount > 0 }

            // Recent expenses (combined refills and expenses)
            val carMap = cars.associateBy { it.id }
            val recentExpenses = mutableListOf<CostItem>()

            filteredRefills.forEach { refill ->
                recentExpenses.add(
                    CostItem(
                        id = refill.id,
                        date = refill.timestamp,
                        category = "Fuel",
                        description = "${refill.litersAdded.formatNumber(1)} L",
                        amount = refill.amountPaid,
                        carName = carMap[refill.carId]?.name
                    )
                )
            }

            filteredExpenses.forEach { expense ->
                recentExpenses.add(
                    CostItem(
                        id = expense.id,
                        date = expense.timestamp,
                        category = expense.category,
                        description = expense.notes ?: expense.category,
                        amount = expense.amount,
                        carName = carMap[expense.carId]?.name
                    )
                )
            }

            val sortedExpenses = recentExpenses.sortedByDescending { it.date }.take(20)

            // Calculate statistics
            val averageMonthlyCost = if (monthlyCosts.isNotEmpty()) {
                monthlyCosts.map { it.totalCost }.average()
            } else 0.0

            val highestMonthCost = monthlyCosts.maxOfOrNull { it.totalCost } ?: 0.0
            val lowestMonthCost = monthlyCosts.filter { it.totalCost > 0 }.minOfOrNull { it.totalCost } ?: 0.0

            CostTrendData(
                monthlyCosts = monthlyCosts,
                totalCost = totalCost,
                averageMonthlyCost = averageMonthlyCost,
                highestMonthCost = highestMonthCost,
                lowestMonthCost = lowestMonthCost,
                costByCategory = costByCategory,
                recentExpenses = sortedExpenses.take(10),
                dateRange = dateRange
            )
        }
    }

    private fun calculateMonthlyCosts(
        refills: List<com.agcoding.cartrackingapp.domain.model.FuelRefill>,
        expenses: List<com.agcoding.cartrackingapp.domain.model.Expense>,
        bucketSize: com.agcoding.cartrackingapp.domain.model.AggregationBucket
    ): List<MonthlyCost> {
        if (refills.isEmpty() && expenses.isEmpty()) return emptyList()

        val dateFormat = SimpleDateFormat("MMM d", Locale.getDefault())
        val monthOnlyFormat = SimpleDateFormat("MMM", Locale.getDefault())
        val yearFormat = SimpleDateFormat("yyyy", Locale.getDefault())

        val allTimestamps = (refills.map { it.timestamp } + expenses.map { it.timestamp })
        val earliestTimestamp = allTimestamps.minOrNull() ?: return emptyList()
        val latestTimestamp = allTimestamps.maxOrNull() ?: return emptyList()

        val bucketMillis = bucketSize.daysPerBucket * 24 * 60 * 60 * 1000L
        val monthlyCosts = mutableListOf<MonthlyCost>()

        var currentBucketStart = earliestTimestamp
        val calendar = Calendar.getInstance()

        while (currentBucketStart <= latestTimestamp) {
            val bucketEnd = currentBucketStart + bucketMillis

            val bucketRefills = refills.filter { it.timestamp in currentBucketStart until bucketEnd }
            val bucketExpenses = expenses.filter { it.timestamp in currentBucketStart until bucketEnd }

            if (bucketRefills.isNotEmpty() || bucketExpenses.isNotEmpty()) {
                val fuelCost = bucketRefills.sumOf { it.amountPaid }
                val serviceCost = bucketExpenses.filter { ExpenseCategories.isServiceCategory(it.category) }.sumOf { it.amount }
                val otherCost = bucketExpenses.filter { !ExpenseCategories.isServiceCategory(it.category) }.sumOf { it.amount }

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

                monthlyCosts.add(
                    MonthlyCost(
                        month = label,
                        year = calendar.get(Calendar.YEAR),
                        totalCost = fuelCost + serviceCost + otherCost,
                        fuelCost = fuelCost,
                        serviceCost = serviceCost,
                        otherCost = otherCost,
                        timestamp = currentBucketStart
                    )
                )
            }

            currentBucketStart = bucketEnd
        }

        return monthlyCosts
    }
}

