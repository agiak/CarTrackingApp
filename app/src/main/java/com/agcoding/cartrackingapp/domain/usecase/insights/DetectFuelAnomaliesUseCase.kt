package com.agcoding.cartrackingapp.domain.usecase.insights

import com.agcoding.cartrackingapp.domain.model.Anomaly
import com.agcoding.cartrackingapp.domain.model.AnomalySeverity
import com.agcoding.cartrackingapp.domain.model.AnomalyType
import com.agcoding.cartrackingapp.domain.model.FuelRefill
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID
import javax.inject.Inject

/**
 * Detects fuel price anomalies by comparing current prices against historical averages.
 *
 * Detection Logic:
 * - Computes average fuel price per liter over previous 3-6 months
 * - If current price > average + 20% → anomaly
 *
 * Severity:
 * - 20-30% increase → MEDIUM
 * - >30% increase → HIGH
 */
class DetectFuelAnomaliesUseCase @Inject constructor() {

    operator fun invoke(refills: List<FuelRefill>): List<Anomaly> {
        if (refills.isEmpty()) return emptyList()

        val anomalies = mutableListOf<Anomaly>()
        val sortedRefills = refills.sortedBy { it.timestamp }

        // Group by car
        val refillsByCar = sortedRefills.groupBy { it.carId }

        refillsByCar.forEach { (carId, carRefills) ->
            // Only check the MOST RECENT refill for each car
            val currentRefill = carRefills.lastOrNull() ?: return@forEach

            val currentDate = LocalDate.ofInstant(
                java.time.Instant.ofEpochMilli(currentRefill.timestamp),
                ZoneId.systemDefault()
            )

            // Only detect anomalies in recent refills (last 30 days)
            val thirtyDaysAgo = LocalDate.now().minusDays(30)
            if (currentDate.isBefore(thirtyDaysAgo)) return@forEach

            // Get historical refills (3-6 months before current refill, excluding very recent ones)
            val sixMonthsAgo = currentDate.minusMonths(6)
            val oneMonthAgo = currentDate.minusMonths(1)

            val historicalRefills = carRefills.filter { refill ->
                val refillDate = LocalDate.ofInstant(
                    java.time.Instant.ofEpochMilli(refill.timestamp),
                    ZoneId.systemDefault()
                )
                refillDate.isAfter(sixMonthsAgo) &&
                refillDate.isBefore(oneMonthAgo) &&
                refill.id != currentRefill.id
            }

            // Need at least 4 historical refills for meaningful comparison
            if (historicalRefills.size < 4) return@forEach

            // Calculate average historical price per liter
            val avgHistoricalPrice = historicalRefills
                .map { it.pricePerLiter }
                .average()

            // Check for price spike (increased threshold to 25% for more conservative detection)
            val currentPrice = currentRefill.pricePerLiter
            val percentageIncrease = ((currentPrice - avgHistoricalPrice) / avgHistoricalPrice) * 100

            if (percentageIncrease > 25) {  // Increased from 20% to 25%
                val severity = when {
                    percentageIncrease > 40 -> AnomalySeverity.HIGH  // Increased from 30%
                    else -> AnomalySeverity.MEDIUM
                }

                anomalies.add(
                    Anomaly(
                        id = UUID.randomUUID().toString(),
                        carId = carId,
                        type = AnomalyType.FUEL_PRICE_SPIKE,
                        severity = severity,
                        title = "Fuel Price Spike Detected",
                        description = "Fuel price increased by ${String.format("%.1f", percentageIncrease)}% " +
                                "compared to 6-month average (€${String.format("%.3f", avgHistoricalPrice)}/L). " +
                                "Current: €${String.format("%.3f", currentPrice)}/L",
                        detectedAt = currentDate,
                        relatedTransactionId = currentRefill.id,
                        value = currentPrice,
                        threshold = avgHistoricalPrice * 1.25
                    )
                )
            }
        }

        return anomalies
    }
}

