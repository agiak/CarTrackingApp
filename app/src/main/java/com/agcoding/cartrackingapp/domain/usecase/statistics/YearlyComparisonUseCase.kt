package com.agcoding.cartrackingapp.domain.usecase.statistics

import com.agcoding.cartrackingapp.domain.model.AvailableYear
import com.agcoding.cartrackingapp.domain.model.ComparisonMetric
import com.agcoding.cartrackingapp.domain.model.MonthlyYearData
import com.agcoding.cartrackingapp.domain.model.YearlyComparisonData
import com.agcoding.cartrackingapp.domain.model.YearlyData
import com.agcoding.cartrackingapp.domain.repository.ExpenseRepository
import com.agcoding.cartrackingapp.domain.repository.RefillRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

/**
 * Use case for yearly comparison analytics
 * Handles aggregation and calculation of year-over-year metrics
 */
class YearlyComparisonUseCase @Inject constructor(
    private val refillRepository: RefillRepository,
    private val expenseRepository: ExpenseRepository
) {

    /**
     * Get available years with data
     */
    suspend fun getAvailableYears(): Flow<List<AvailableYear>> {
        return combine(
            refillRepository.getAllRefills(),
            expenseRepository.getAllExpenses()
        ) { refills, expenses ->
            val allTransactions = refills.map { it.timestamp } + expenses.map { it.timestamp }

            if (allTransactions.isEmpty()) return@combine emptyList()

            val calendar = Calendar.getInstance()
            val years = allTransactions.map { timestamp ->
                calendar.timeInMillis = timestamp
                calendar.get(Calendar.YEAR)
            }.distinct().sorted()

            years.map { year ->
                val yearTransactions = allTransactions.count { timestamp ->
                    calendar.timeInMillis = timestamp
                    calendar.get(Calendar.YEAR) == year
                }
                AvailableYear(year, yearTransactions > 0, yearTransactions)
            }
        }
    }

    /**
     * Get yearly comparison data
     */
    fun getYearlyComparison(
        year1: Int,
        year2: Int,
        carId: Long? = null
    ): Flow<YearlyComparisonData?> {
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

        return combine(refillsFlow, expensesFlow) { refills, expenses ->
            // Calculate data for year 1
            val year1Data = calculateYearlyData(year1, refills, expenses)

            // Calculate data for year 2
            val year2Data = calculateYearlyData(year2, refills, expenses)

            // Generate comparison metrics
            val metrics = generateComparisonMetrics(year1Data, year2Data)

            YearlyComparisonData(
                year1Data = year1Data,
                year2Data = year2Data,
                metrics = metrics
            )
        }
    }

    private fun calculateYearlyData(
        year: Int,
        refills: List<com.agcoding.cartrackingapp.domain.model.FuelRefill>,
        expenses: List<com.agcoding.cartrackingapp.domain.model.Expense>
    ): YearlyData {
        val calendar = Calendar.getInstance()

        // Filter data for the specific year
        val yearRefills = refills.filter { refill ->
            calendar.timeInMillis = refill.timestamp
            calendar.get(Calendar.YEAR) == year
        }

        val yearExpenses = expenses.filter { expense ->
            calendar.timeInMillis = expense.timestamp
            calendar.get(Calendar.YEAR) == year
        }

        // Calculate totals
        val totalFuelCost = yearRefills.sumOf { it.amountPaid }
        val totalExpenseCost = yearExpenses.sumOf { it.amount }
        val totalCost = totalFuelCost + totalExpenseCost
        val totalDistance = yearRefills.sumOf { it.tripDistance }
        val totalFuelLiters = yearRefills.sumOf { it.litersAdded }

        // Calculate monthly data
        val monthlyCosts = calculateMonthlyData(year, yearRefills, yearExpenses)

        return YearlyData(
            year = year,
            totalCost = totalCost,
            totalDistance = totalDistance,
            totalFuelLiters = totalFuelLiters,
            totalRefills = yearRefills.size,
            totalExpenses = yearExpenses.size,
            monthlyCosts = monthlyCosts
        )
    }

    private fun calculateMonthlyData(
        year: Int,
        refills: List<com.agcoding.cartrackingapp.domain.model.FuelRefill>,
        expenses: List<com.agcoding.cartrackingapp.domain.model.Expense>
    ): List<MonthlyYearData> {
        val calendar = Calendar.getInstance()
        val monthFormat = SimpleDateFormat("MMM", Locale.getDefault())
        val monthlyData = mutableListOf<MonthlyYearData>()

        for (month in 1..12) {
            val monthRefills = refills.filter { refill ->
                calendar.timeInMillis = refill.timestamp
                calendar.get(Calendar.YEAR) == year &&
                calendar.get(Calendar.MONTH) + 1 == month
            }

            val monthExpenses = expenses.filter { expense ->
                calendar.timeInMillis = expense.timestamp
                calendar.get(Calendar.YEAR) == year &&
                calendar.get(Calendar.MONTH) + 1 == month
            }

            val fuelCost = monthRefills.sumOf { it.amountPaid }
            val expenseCost = monthExpenses.sumOf { it.amount }
            val distance = monthRefills.sumOf { it.tripDistance }
            val liters = monthRefills.sumOf { it.litersAdded }
            val consumption = if (distance > 0) (liters / distance) * 100 else 0.0

            calendar.set(year, month - 1, 1)

            monthlyData.add(
                MonthlyYearData(
                    month = month,
                    monthName = monthFormat.format(calendar.time),
                    totalCost = fuelCost + expenseCost,
                    fuelCost = fuelCost,
                    expenseCost = expenseCost,
                    distance = distance,
                    consumption = consumption
                )
            )
        }

        return monthlyData
    }

    private fun generateComparisonMetrics(
        year1Data: YearlyData,
        year2Data: YearlyData
    ): List<ComparisonMetric> {
        return listOf(
            ComparisonMetric(
                name = "Total Cost",
                year1Value = year1Data.totalCost,
                year2Value = year2Data.totalCost,
                unit = "€",
                year1FormattedValue = "€${String.format(Locale.getDefault(), "%.2f", year1Data.totalCost)}",
                year2FormattedValue = "€${String.format(Locale.getDefault(), "%.2f", year2Data.totalCost)}"
            ),
            ComparisonMetric(
                name = "Total Distance",
                year1Value = year1Data.totalDistance,
                year2Value = year2Data.totalDistance,
                unit = "km",
                year1FormattedValue = "${String.format(Locale.getDefault(), "%.0f", year1Data.totalDistance)} km",
                year2FormattedValue = "${String.format(Locale.getDefault(), "%.0f", year2Data.totalDistance)} km"
            ),
            ComparisonMetric(
                name = "Average Consumption",
                year1Value = year1Data.averageConsumption,
                year2Value = year2Data.averageConsumption,
                unit = "L/100km",
                year1FormattedValue = "${String.format(Locale.getDefault(), "%.2f", year1Data.averageConsumption)} L/100km",
                year2FormattedValue = "${String.format(Locale.getDefault(), "%.2f", year2Data.averageConsumption)} L/100km"
            ),
            ComparisonMetric(
                name = "Cost per km",
                year1Value = year1Data.costPerKm,
                year2Value = year2Data.costPerKm,
                unit = "€/km",
                year1FormattedValue = "€${String.format(Locale.getDefault(), "%.3f", year1Data.costPerKm)}/km",
                year2FormattedValue = "€${String.format(Locale.getDefault(), "%.3f", year2Data.costPerKm)}/km"
            )
        )
    }
}

