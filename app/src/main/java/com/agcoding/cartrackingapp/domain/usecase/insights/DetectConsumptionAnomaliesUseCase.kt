package com.agcoding.cartrackingapp.domain.usecase.insights

import com.agcoding.cartrackingapp.domain.model.Anomaly
import com.agcoding.cartrackingapp.domain.model.AnomalySeverity
import com.agcoding.cartrackingapp.domain.model.AnomalyType
import com.agcoding.cartrackingapp.domain.model.FuelRefill
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID
import javax.inject.Inject
import kotlin.math.abs

/**
 * Detects fuel consumption anomalies by comparing current consumption against rolling average.
 *
 * Detection Logic:
 * - Compare fuel consumption (L/100km) against rolling historical average
 * - If deviation >15% → LOW
 * - If deviation >25% → HIGH
 *
 * Handles edge cases:
 * - Zero distance (invalid consumption)
 * - Insufficient historical data
 */
class DetectConsumptionAnomaliesUseCase @Inject constructor() {

    operator fun invoke(refills: List<FuelRefill>): List<Anomaly> {
        if (refills.isEmpty()) return emptyList()

        val anomalies = mutableListOf<Anomaly>()
        val sortedRefills = refills.sortedBy { it.timestamp }

        // Group by car
        val refillsByCar = sortedRefills.groupBy { it.carId }

        refillsByCar.forEach { (carId, carRefills) ->
            // Filter refills with valid consumption data
            val validRefills = carRefills.filter {
                it.fuelConsumption > 0 && it.tripDistance > 0
            }

            if (validRefills.size < 5) return@forEach // Need enough data

            // Only check the MOST RECENT valid refill
            val currentRefill = validRefills.lastOrNull() ?: return@forEach

            val currentDate = LocalDate.ofInstant(
                java.time.Instant.ofEpochMilli(currentRefill.timestamp),
                ZoneId.systemDefault()
            )

            // Only detect anomalies in recent refills (last 30 days)
            val thirtyDaysAgo = LocalDate.now().minusDays(30)
            if (currentDate.isBefore(thirtyDaysAgo)) return@forEach

            // Get historical refills (previous 6 months, excluding very recent)
            val sixMonthsAgo = currentDate.minusMonths(6)
            val oneMonthAgo = currentDate.minusMonths(1)

            val historicalRefills = validRefills.filter { refill ->
                val refillDate = LocalDate.ofInstant(
                    java.time.Instant.ofEpochMilli(refill.timestamp),
                    ZoneId.systemDefault()
                )
                refillDate.isAfter(sixMonthsAgo) &&
                refillDate.isBefore(oneMonthAgo) &&
                refill.id != currentRefill.id
            }

            if (historicalRefills.size < 4) return@forEach

            // Calculate average historical consumption
            val avgHistoricalConsumption = historicalRefills
                .map { it.fuelConsumption }
                .average()

            // Check for consumption spike (increased threshold to 20%)
            val currentConsumption = currentRefill.fuelConsumption
            val deviation = abs(currentConsumption - avgHistoricalConsumption)
            val percentageDeviation = (deviation / avgHistoricalConsumption) * 100

            if (percentageDeviation > 20) {  // Increased from 15% to 20%
                val severity = when {
                    percentageDeviation > 35 -> AnomalySeverity.HIGH  // Increased from 25%
                    else -> AnomalySeverity.LOW
                }

                val direction = if (currentConsumption > avgHistoricalConsumption) "increased" else "decreased"

                anomalies.add(
                    Anomaly(
                        id = UUID.randomUUID().toString(),
                        carId = carId,
                        type = AnomalyType.CONSUMPTION_SPIKE,
                        severity = severity,
                        title = "Unusual Fuel Consumption",
                        description = "Fuel consumption $direction by ${String.format("%.1f", percentageDeviation)}% " +
                                "compared to 6-month average (${String.format("%.2f", avgHistoricalConsumption)} L/100km). " +
                                "Current: ${String.format("%.2f", currentConsumption)} L/100km",
                        detectedAt = currentDate,
                        relatedTransactionId = currentRefill.id,
                        value = currentConsumption,
                        threshold = avgHistoricalConsumption
                    )
                )
            }
        }

        return anomalies
    }
}

