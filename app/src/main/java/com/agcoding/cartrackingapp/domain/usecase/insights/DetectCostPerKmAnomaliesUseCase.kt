package com.agcoding.cartrackingapp.domain.usecase.insights

import com.agcoding.cartrackingapp.domain.model.Anomaly
import com.agcoding.cartrackingapp.domain.model.AnomalySeverity
import com.agcoding.cartrackingapp.domain.model.AnomalyType
import com.agcoding.cartrackingapp.domain.model.Expense
import com.agcoding.cartrackingapp.domain.model.FuelRefill
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.util.UUID
import javax.inject.Inject

/**
 * Detects cost per kilometer deviations by comparing current month against historical average.
 *
 * Detection Logic:
 * - Compute historical cost/km (total spending / total distance)
 * - Compare current month cost/km
 * - If deviation >20% → anomaly
 *
 * Handles edge cases:
 * - Low mileage months (< 50 km ignored to avoid false positives)
 * - Division by zero
 * - Insufficient historical data
 */
class DetectCostPerKmAnomaliesUseCase @Inject constructor() {

    private val MIN_DISTANCE_THRESHOLD = 50.0 // km - ignore months with very low mileage

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

            // Calculate monthly cost/km
            val monthlyCostPerKm = calculateMonthlyCostPerKm(carRefills, carExpenses)

            if (monthlyCostPerKm.size < 3) return@forEach // Need historical data

            // Only check the CURRENT and PREVIOUS month
            val now = java.time.YearMonth.now()
            val lastMonth = now.minusMonths(1)
            val monthsToCheck = listOf(now, lastMonth)

            // Check each month
            monthlyCostPerKm.entries.sortedBy { it.key }.forEachIndexed { index, (yearMonth, monthData) ->
                // Only check recent months
                if (yearMonth !in monthsToCheck) return@forEachIndexed

                // Skip if distance too low (unreliable cost/km)
                if (monthData.distance < MIN_DISTANCE_THRESHOLD) return@forEachIndexed

                // Calculate historical average (all months before current, with sufficient distance)
                val historicalMonths = monthlyCostPerKm.entries
                    .sortedBy { it.key }
                    .subList(0, index)
                    .filter { it.value.distance >= MIN_DISTANCE_THRESHOLD }
                    .map { it.value.costPerKm }

                if (historicalMonths.size < 2) return@forEachIndexed

                val avgHistoricalCostPerKm = historicalMonths.average()
                val currentCostPerKm = monthData.costPerKm

                // Check for deviation (increased threshold to 25%)
                val percentageDeviation = kotlin.math.abs(
                    ((currentCostPerKm - avgHistoricalCostPerKm) / avgHistoricalCostPerKm) * 100
                )

                if (percentageDeviation > 25) {  // Increased from 20% to 25%
                    val severity = when {
                        percentageDeviation > 45 -> AnomalySeverity.HIGH  // Increased from 35%
                        percentageDeviation > 35 -> AnomalySeverity.MEDIUM  // Increased from 25%
                        else -> AnomalySeverity.LOW
                    }

                    val direction = if (currentCostPerKm > avgHistoricalCostPerKm) "increased" else "decreased"
                    val detectedDate = yearMonth.atDay(1)

                    anomalies.add(
                        Anomaly(
                            id = UUID.randomUUID().toString(),
                            carId = carId,
                            type = AnomalyType.COST_PER_KM_DEVIATION,
                            severity = severity,
                            title = "Cost per Kilometer Deviation",
                            description = "Cost per km in ${yearMonth.month.name} ${yearMonth.year} " +
                                    "$direction by ${String.format("%.1f", percentageDeviation)}%. " +
                                    "Current: €${String.format("%.3f", currentCostPerKm)}/km vs " +
                                    "historical avg: €${String.format("%.3f", avgHistoricalCostPerKm)}/km " +
                                    "(${String.format("%.0f", monthData.distance)} km traveled)",
                            detectedAt = detectedDate,
                            relatedTransactionId = null,
                            value = currentCostPerKm,
                            threshold = avgHistoricalCostPerKm
                        )
                    )
                }
            }
        }

        return anomalies
    }

    private data class MonthCostData(
        val costPerKm: Double,
        val distance: Double,
        val totalCost: Double
    )

    private fun calculateMonthlyCostPerKm(
        refills: List<FuelRefill>,
        expenses: List<Expense>
    ): Map<YearMonth, MonthCostData> {
        val monthlyData = mutableMapOf<YearMonth, Pair<Double, Double>>() // cost, distance

        // Add refill costs and distances
        refills.forEach { refill ->
            val yearMonth = YearMonth.from(
                LocalDate.ofInstant(
                    java.time.Instant.ofEpochMilli(refill.timestamp),
                    ZoneId.systemDefault()
                )
            )
            val current = monthlyData.getOrDefault(yearMonth, Pair(0.0, 0.0))
            monthlyData[yearMonth] = Pair(
                current.first + refill.amountPaid,
                current.second + refill.tripDistance
            )
        }

        // Add expense costs (but not distance)
        expenses.forEach { expense ->
            val yearMonth = YearMonth.from(
                LocalDate.ofInstant(
                    java.time.Instant.ofEpochMilli(expense.timestamp),
                    ZoneId.systemDefault()
                )
            )
            val current = monthlyData.getOrDefault(yearMonth, Pair(0.0, 0.0))
            monthlyData[yearMonth] = Pair(
                current.first + expense.amount,
                current.second
            )
        }

        // Calculate cost per km
        return monthlyData.mapValues { (_, data) ->
            val (totalCost, totalDistance) = data
            val costPerKm = if (totalDistance > 0) totalCost / totalDistance else 0.0
            MonthCostData(
                costPerKm = costPerKm,
                distance = totalDistance,
                totalCost = totalCost
            )
        }
    }
}

