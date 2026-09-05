package com.agcoding.cartrackingapp.domain.usecase.statistics

import com.agcoding.cartrackingapp.domain.model.CostCategory
import com.agcoding.cartrackingapp.domain.model.CostItem
import com.agcoding.cartrackingapp.domain.model.CostTrendData
import com.agcoding.cartrackingapp.domain.model.DateFilter
import com.agcoding.cartrackingapp.domain.model.ExpenseCategories
import com.agcoding.cartrackingapp.domain.model.MonthlyCost
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
        dateFilter: DateFilter = DateFilter.None
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

            val allTimestamps = (refills.map { it.timestamp } + expenses.map { it.timestamp })

            if (allTimestamps.isEmpty()) return@combine null

            // Chart only what the filter selected, at a granularity that matches it.
            val window = trendWindowFor(dateFilter, allTimestamps) ?: return@combine null
            val dateRange = window.dateRange
            val bucketSize = window.bucket

            val filteredRefills = refills.filter { dateFilter.matches(it.timestamp) }
            val filteredExpenses = expenses.filter { dateFilter.matches(it.timestamp) }

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

        val monthlyCosts = mutableListOf<MonthlyCost>()

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

