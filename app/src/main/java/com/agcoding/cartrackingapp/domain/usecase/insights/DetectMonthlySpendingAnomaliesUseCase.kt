package com.agcoding.cartrackingapp.domain.usecase.insights

import com.agcoding.cartrackingapp.domain.model.Anomaly
import com.agcoding.cartrackingapp.domain.model.AnomalySeverity
import com.agcoding.cartrackingapp.domain.model.AnomalyType
import com.agcoding.cartrackingapp.domain.model.Expense
import com.agcoding.cartrackingapp.domain.model.FuelRefill
import com.agcoding.cartrackingapp.util.formatNumber
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.util.UUID
import javax.inject.Inject

/**
 * Detects monthly spending increases by comparing against 3-month rolling average.
 *
 * Detection Logic:
 * - Compute total spending per month (refills + expenses)
 * - Compare with previous 3-month average
 * - If >25% increase → MEDIUM
 * - If >40% increase → HIGH
 *
 * Grouped by carId for accurate per-vehicle analysis.
 */
class DetectMonthlySpendingAnomaliesUseCase @Inject constructor() {

    operator fun invoke(
        refills: List<FuelRefill>,
        expenses: List<Expense>
    ): List<Anomaly> {
        val anomalies = mutableListOf<Anomaly>()

        // Get all unique car IDs
        val carIds = (refills.map { it.carId } + expenses.map { it.carId }).distinct()

        carIds.forEach { carId ->
            // Filter transactions for this car
            val carRefills = refills.filter { it.carId == carId }
            val carExpenses = expenses.filter { it.carId == carId }

            // Calculate monthly spending
            val monthlySpending = calculateMonthlySpending(carRefills, carExpenses)

            if (monthlySpending.size < 4) return@forEach // Need at least 4 months of data

            // Only check the CURRENT and PREVIOUS month (not all historical months)
            val now = java.time.YearMonth.now()
            val lastMonth = now.minusMonths(1)
            val monthsToCheck = listOf(now, lastMonth)

            // Check each month against previous 3-month average
            monthlySpending.entries.sortedBy { it.key }.forEachIndexed { index, (yearMonth, spending) ->
                // Only check recent months
                if (yearMonth !in monthsToCheck) return@forEachIndexed

                if (index < 3) return@forEachIndexed // Skip first 3 months (no baseline)

                // Get previous 3 months
                val previousMonths = monthlySpending.entries
                    .sortedBy { it.key }
                    .subList(maxOf(0, index - 3), index)
                    .map { it.value }

                if (previousMonths.size < 3) return@forEachIndexed

                val avgPreviousSpending = previousMonths.average()

                // Check for increase (increased threshold to 30%)
                val percentageIncrease = ((spending - avgPreviousSpending) / avgPreviousSpending) * 100

                if (percentageIncrease > 30) {  // Increased from 25% to 30%
                    val severity = when {
                        percentageIncrease > 50 -> AnomalySeverity.HIGH  // Increased from 40%
                        else -> AnomalySeverity.MEDIUM
                    }

                    val detectedDate = yearMonth.atDay(1)

                    anomalies.add(
                        Anomaly(
                            id = UUID.randomUUID().toString(),
                            carId = carId,
                            type = AnomalyType.MONTHLY_SPENDING_INCREASE,
                            severity = severity,
                            title = "Monthly Spending Increased",
                            description = "Total spending in ${yearMonth.month.name} ${yearMonth.year} " +
                                    "(€${spending.formatNumber(2)}) is ${percentageIncrease.formatNumber(1)}% " +
                                    "higher than 3-month average (€${avgPreviousSpending.formatNumber(2)}). " +
                                    "Review fuel and maintenance expenses.",
                            detectedAt = detectedDate,
                            relatedTransactionId = null,
                            value = spending,
                            threshold = avgPreviousSpending * 1.25
                        )
                    )
                }
            }
        }

        return anomalies
    }

    private fun calculateMonthlySpending(
        refills: List<FuelRefill>,
        expenses: List<Expense>
    ): Map<YearMonth, Double> {
        val monthlyTotals = mutableMapOf<YearMonth, Double>()

        // Add refill costs
        refills.forEach { refill ->
            val yearMonth = YearMonth.from(
                LocalDate.ofInstant(
                    java.time.Instant.ofEpochMilli(refill.timestamp),
                    ZoneId.systemDefault()
                )
            )
            monthlyTotals[yearMonth] = monthlyTotals.getOrDefault(yearMonth, 0.0) + refill.amountPaid
        }

        // Add expense costs
        expenses.forEach { expense ->
            val yearMonth = YearMonth.from(
                LocalDate.ofInstant(
                    java.time.Instant.ofEpochMilli(expense.timestamp),
                    ZoneId.systemDefault()
                )
            )
            monthlyTotals[yearMonth] = monthlyTotals.getOrDefault(yearMonth, 0.0) + expense.amount
        }

        return monthlyTotals
    }
}

