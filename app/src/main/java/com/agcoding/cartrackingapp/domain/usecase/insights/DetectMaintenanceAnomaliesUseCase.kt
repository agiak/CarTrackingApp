package com.agcoding.cartrackingapp.domain.usecase.insights

import com.agcoding.cartrackingapp.domain.model.Anomaly
import com.agcoding.cartrackingapp.domain.model.AnomalySeverity
import com.agcoding.cartrackingapp.domain.model.AnomalyType
import com.agcoding.cartrackingapp.domain.model.Expense
import com.agcoding.cartrackingapp.util.formatNumber
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID
import javax.inject.Inject

/**
 * Detects maintenance expense outliers by comparing against historical averages.
 *
 * Detection Logic:
 * - Compute historical average maintenance cost
 * - If expense > average + 40% → HIGH severity anomaly
 *
 * Only analyzes maintenance-related expenses (excludes fuel, parking, etc.)
 */
class DetectMaintenanceAnomaliesUseCase @Inject constructor() {

    // Maintenance-related categories
    private val maintenanceCategories = setOf(
        "Service",
        "Tire Change",
        "Oil Change",
        "Small Service",
        "Big Service",
        "Repairs",
        "Συντήρηση",
        "Αλλαγή Ελαστικών",
        "Αλλαγή Λαδιών",
        "Μικρό Service",
        "Μεγάλο Service",
        "Επισκευές"
    )

    operator fun invoke(expenses: List<Expense>): List<Anomaly> {
        if (expenses.isEmpty()) return emptyList()

        val anomalies = mutableListOf<Anomaly>()

        // Filter maintenance expenses
        val maintenanceExpenses = expenses.filter { expense ->
            maintenanceCategories.any { category ->
                expense.category.equals(category, ignoreCase = true)
            }
        }

        if (maintenanceExpenses.isEmpty()) return emptyList()

        val sortedExpenses = maintenanceExpenses.sortedBy { it.timestamp }

        // Group by car
        val expensesByCar = sortedExpenses.groupBy { it.carId }

        expensesByCar.forEach { (carId, carExpenses) ->
            if (carExpenses.size < 3) return@forEach // Need historical data

            // Only check the MOST RECENT maintenance expense
            val currentExpense = carExpenses.lastOrNull() ?: return@forEach

            val currentDate = LocalDate.ofInstant(
                java.time.Instant.ofEpochMilli(currentExpense.timestamp),
                ZoneId.systemDefault()
            )

            // Only detect anomalies in recent expenses (last 60 days)
            val sixtyDaysAgo = LocalDate.now().minusDays(60)
            if (currentDate.isBefore(sixtyDaysAgo)) return@forEach

            // Get historical expenses (at least 2 months before current)
            val twoMonthsAgo = currentDate.minusMonths(2)
            val historicalExpenses = carExpenses.filter { expense ->
                val expenseDate = LocalDate.ofInstant(
                    java.time.Instant.ofEpochMilli(expense.timestamp),
                    ZoneId.systemDefault()
                )
                expenseDate.isBefore(twoMonthsAgo) && expense.id != currentExpense.id
            }

            if (historicalExpenses.size < 2) return@forEach

            // Calculate average historical maintenance cost
            val avgHistoricalCost = historicalExpenses
                .map { it.amount }
                .average()

            // Check for outlier (increased threshold to 50%)
            val currentCost = currentExpense.amount
            val percentageIncrease = ((currentCost - avgHistoricalCost) / avgHistoricalCost) * 100

            if (percentageIncrease > 50) {  // Increased from 40% to 50%
                anomalies.add(
                    Anomaly(
                        id = UUID.randomUUID().toString(),
                        carId = carId,
                        type = AnomalyType.MAINTENANCE_OUTLIER,
                        severity = AnomalySeverity.HIGH,
                        title = "Unusually High Maintenance Cost",
                        description = "${currentExpense.category} expense (€${currentCost.formatNumber(2)}) " +
                                "is ${percentageIncrease.formatNumber(1)}% higher than " +
                                "historical average (€${avgHistoricalCost.formatNumber(2)}). " +
                                "This may indicate a significant repair or service.",
                        detectedAt = currentDate,
                        relatedTransactionId = currentExpense.id,
                        value = currentCost,
                        threshold = avgHistoricalCost * 1.5
                    )
                )
            }
        }

        return anomalies
    }
}

