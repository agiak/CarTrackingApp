package com.agcoding.cartrackingapp.domain.model

import java.time.LocalDate

/**
 * Represents a detected financial anomaly in car expenses.
 * Anomalies are computed dynamically and NOT stored in the database.
 */
data class Anomaly(
    val id: String,
    val carId: Long,
    val type: AnomalyType,
    val severity: AnomalySeverity,
    val title: String,
    val description: String,
    val detectedAt: LocalDate,
    val relatedTransactionId: Long? = null,
    val value: Double? = null, // The actual value that triggered the anomaly
    val threshold: Double? = null, // The threshold that was exceeded
    val suggestedTripId: Long? = null // For MISSING_TRIP_REFILL: the trip to add the refill to
)

enum class AnomalyType {
    FUEL_PRICE_SPIKE,
    CONSUMPTION_SPIKE,
    MAINTENANCE_OUTLIER,
    MONTHLY_SPENDING_INCREASE,
    COST_PER_KM_DEVIATION,
    MISSING_TRIP_REFILL // New: Suggests adding an unassigned refill to a trip
}

enum class AnomalySeverity {
    LOW,
    MEDIUM,
    HIGH
}

