package com.agcoding.cartrackingapp.domain.usecase.insights

import com.agcoding.cartrackingapp.domain.model.Anomaly
import com.agcoding.cartrackingapp.domain.model.AnomalySeverity
import com.agcoding.cartrackingapp.domain.model.AnomalyType
import com.agcoding.cartrackingapp.domain.model.FuelRefill
import com.agcoding.cartrackingapp.domain.model.Trip
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import javax.inject.Inject

/**
 * Detects refills that likely belong to existing trips but haven't been added.
 *
 * Detection Logic:
 * - A refill is suggested if it's NOT assigned to any trip AND:
 *   1. Falls between two refills of the same trip (date-wise), OR
 *   2. Falls within 10 days before/after the first/last refill of a trip
 *
 * The 10-day threshold prevents suggesting refills from completely different
 * time periods (e.g., suggesting a December refill for an October trip).
 *
 * Only suggests refills for the same car as the trip.
 */
class DetectMissingTripRefillsUseCase @Inject constructor() {

    /**
     * Detect unassigned refills that likely belong to existing trips.
     *
     * @param allRefills All refills across all cars
     * @param allTrips All trips with their refills
     * @return List of anomalies suggesting to add refills to trips
     */
    operator fun invoke(
        allRefills: List<FuelRefill>,
        allTrips: List<Trip>
    ): List<Anomaly> {
        val anomalies = mutableListOf<Anomaly>()

        // Get unassigned refills (not in any trip)
        val unassignedRefills = allRefills.filter { it.tripId == null }

        // For each trip, check if there are unassigned refills that should belong to it
        allTrips.forEach { trip ->
            // Skip trips with no refills
            if (trip.refills.isEmpty()) return@forEach

            // Get refills of this trip sorted by timestamp
            val tripRefills = trip.refills.sortedBy { it.timestamp }
            val firstRefillTime = tripRefills.first().timestamp
            val lastRefillTime = tripRefills.last().timestamp

            // Find unassigned refills for the same car
            val candidateRefills = unassignedRefills.filter { it.carId == trip.carId }

            candidateRefills.forEach { refill ->
                val suggestion = detectIfRefillBelongsToTrip(
                    refill = refill,
                    trip = trip,
                    firstRefillTime = firstRefillTime,
                    lastRefillTime = lastRefillTime
                )

                if (suggestion != null) {
                    anomalies.add(suggestion)
                }
            }
        }

        return anomalies
    }

    private fun detectIfRefillBelongsToTrip(
        refill: FuelRefill,
        trip: Trip,
        firstRefillTime: Long,
        lastRefillTime: Long
    ): Anomaly? {
        val refillTime = refill.timestamp

        // Check if refill falls between trip refills
        if (refillTime > firstRefillTime && refillTime < lastRefillTime) {
            // Refill is between the first and last refill of the trip
            return createAnomaly(
                refill = refill,
                trip = trip,
                reason = "between other refills of this trip"
            )
        }

        // Check if refill is close to trip boundaries (within 10 days maximum)
        // This prevents suggesting refills from completely different time periods
        val maxThresholdDays = 10L
        val thresholdMillis = ChronoUnit.DAYS.duration.toMillis() * maxThresholdDays

        // Check if close to start (within 10 days before first refill)
        if (refillTime < firstRefillTime) {
            val daysDifference = (firstRefillTime - refillTime) / ChronoUnit.DAYS.duration.toMillis()

            // Only suggest if within 10 days AND not too far in the past
            if (daysDifference <= maxThresholdDays) {
                return createAnomaly(
                    refill = refill,
                    trip = trip,
                    reason = "close to the start of this trip ($daysDifference days before)"
                )
            }
        }

        // Check if close to end (within 10 days after last refill)
        if (refillTime > lastRefillTime) {
            val daysDifference = (refillTime - lastRefillTime) / ChronoUnit.DAYS.duration.toMillis()

            // Only suggest if within 10 days AND not too far in the future
            if (daysDifference <= maxThresholdDays) {
                return createAnomaly(
                    refill = refill,
                    trip = trip,
                    reason = "close to the end of this trip ($daysDifference days after)"
                )
            }
        }

        return null
    }

    private fun createAnomaly(
        refill: FuelRefill,
        trip: Trip,
        reason: String
    ): Anomaly {
        val refillDate = Instant.ofEpochMilli(refill.timestamp)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()

        return Anomaly(
            id = "missing_trip_refill_${refill.id}_${trip.id}",
            carId = refill.carId,
            type = AnomalyType.MISSING_TRIP_REFILL,
            severity = AnomalySeverity.LOW,
            title = "Possible Missing Refill in Trip",
            description = "Refill from ${formatDate(refillDate)} appears to belong to trip \"${trip.name}\" (${reason})",
            detectedAt = LocalDate.now(),
            relatedTransactionId = refill.id,
            suggestedTripId = trip.id
        )
    }

    private fun formatDate(date: LocalDate): String {
        val month = date.month.toString().lowercase().replaceFirstChar { it.uppercase() }
        return "${month.substring(0, 3)} ${date.dayOfMonth}, ${date.year}"
    }
}

