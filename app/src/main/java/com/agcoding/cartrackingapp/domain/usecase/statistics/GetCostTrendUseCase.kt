package com.agcoding.cartrackingapp.domain.usecase.statistics

import com.agcoding.cartrackingapp.domain.model.CostCategory
import com.agcoding.cartrackingapp.domain.model.CostItem
import com.agcoding.cartrackingapp.domain.model.CostTrendData
import com.agcoding.cartrackingapp.domain.model.DateRange
import com.agcoding.cartrackingapp.domain.model.MonthlyCost
import com.agcoding.cartrackingapp.domain.model.TrendPeriod
import com.agcoding.cartrackingapp.domain.repository.CarRepository
import com.agcoding.cartrackingapp.domain.repository.ExpenseRepository
import com.agcoding.cartrackingapp.domain.repository.RefillRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.text.SimpleDateFormat
import java.util.Calendar
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

            // Filter by date range
            val filteredRefills = refills.filter { it.timestamp in dateRange.startMillis..dateRange.endMillis }
            val filteredExpenses = expenses.filter { it.timestamp in dateRange.startMillis..dateRange.endMillis }

            // Calculate monthly costs
            val monthlyCosts = calculateMonthlyCosts(filteredRefills, filteredExpenses)

            // Calculate totals
            val totalFuelCost = filteredRefills.sumOf { it.amountPaid }
            val totalServiceCost = filteredExpenses.filter { it.category.equals("Service", true) }.sumOf { it.amount }
            val totalOtherCost = filteredExpenses.filter { !it.category.equals("Service", true) }.sumOf { it.amount }
            val totalCost = totalFuelCost + totalServiceCost + totalOtherCost

            // Cost by category
            val costByCategory = listOf(
                CostCategory("Fuel", totalFuelCost, if (totalCost > 0) totalFuelCost / totalCost * 100 else 0.0, 0xFF4CAF50.toInt()),
                CostCategory("Service", totalServiceCost, if (totalCost > 0) totalServiceCost / totalCost * 100 else 0.0, 0xFFFF9800.toInt()),
                CostCategory("Other", totalOtherCost, if (totalCost > 0) totalOtherCost / totalCost * 100 else 0.0, 0xFF2196F3.toInt())
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
                        description = "${String.format("%.1f", refill.litersAdded)} L",
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

    private fun calculateMonthlyCosts(refills: List<com.agcoding.cartrackingapp.domain.model.FuelRefill>,
                                     expenses: List<com.agcoding.cartrackingapp.domain.model.Expense>): List<MonthlyCost> {
        if (refills.isEmpty() && expenses.isEmpty()) return emptyList()

        val monthFormat = SimpleDateFormat("MMM", Locale.getDefault())
        val calendar = Calendar.getInstance()

        // Find earliest and latest timestamps
        val allTimestamps = (refills.map { it.timestamp } + expenses.map { it.timestamp })
        val earliestTimestamp = allTimestamps.minOrNull() ?: return emptyList()
        val latestTimestamp = allTimestamps.maxOrNull() ?: return emptyList()

        // Set calendar to first day of earliest month
        calendar.timeInMillis = earliestTimestamp
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        // Group by month
        val monthlyData = mutableMapOf<Pair<Int, Int>, Triple<Double, Double, Double>>()

        refills.forEach { refill ->
            calendar.timeInMillis = refill.timestamp
            val key = Pair(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH))
            val current = monthlyData.getOrDefault(key, Triple(0.0, 0.0, 0.0))
            monthlyData[key] = Triple(current.first + refill.amountPaid, current.second, current.third)
        }

        expenses.forEach { expense ->
            calendar.timeInMillis = expense.timestamp
            val key = Pair(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH))
            val current = monthlyData.getOrDefault(key, Triple(0.0, 0.0, 0.0))
            if (expense.category.equals("Service", true)) {
                monthlyData[key] = Triple(current.first, current.second + expense.amount, current.third)
            } else {
                monthlyData[key] = Triple(current.first, current.second, current.third + expense.amount)
            }
        }

        // Generate all months from earliest to latest
        val monthlyCosts = mutableListOf<MonthlyCost>()
        calendar.timeInMillis = earliestTimestamp
        calendar.set(Calendar.DAY_OF_MONTH, 1)

        val endCalendar = Calendar.getInstance()
        endCalendar.timeInMillis = latestTimestamp

        while (calendar.timeInMillis <= endCalendar.timeInMillis) {
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val monthKey = Pair(year, month)

            val costs = monthlyData.getOrDefault(monthKey, Triple(0.0, 0.0, 0.0))

            monthlyCosts.add(
                MonthlyCost(
                    month = monthFormat.format(calendar.time),
                    year = year,
                    totalCost = costs.first + costs.second + costs.third,
                    fuelCost = costs.first,
                    serviceCost = costs.second,
                    otherCost = costs.third,
                    timestamp = calendar.timeInMillis
                )
            )

            calendar.add(Calendar.MONTH, 1)
        }

        return monthlyCosts
    }
}

