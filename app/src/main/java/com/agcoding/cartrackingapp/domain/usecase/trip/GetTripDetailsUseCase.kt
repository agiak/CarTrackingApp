package com.agcoding.cartrackingapp.domain.usecase.trip

import com.agcoding.cartrackingapp.domain.model.TripStatistics
import com.agcoding.cartrackingapp.domain.repository.TripRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetTripDetailsUseCase @Inject constructor(
    private val tripRepository: TripRepository
) {
    operator fun invoke(tripId: Long): Flow<TripStatistics?> {
        return tripRepository.getTripById(tripId).map { trip ->
            trip?.let { calculateTripStatistics(it) }
        }
    }

    private fun calculateTripStatistics(trip: com.agcoding.cartrackingapp.domain.model.Trip): TripStatistics {
        val refills = trip.refills
        val totalDistance = refills.sumOf { it.tripDistance }
        val totalFuelConsumed = refills.sumOf { it.litersAdded }
        val totalCost = refills.sumOf { it.amountPaid }
        val averageConsumption = if (totalDistance > 0) {
            (totalFuelConsumed / totalDistance) * 100
        } else 0.0
        val startDate = refills.minOfOrNull { it.timestamp }
        val endDate = refills.maxOfOrNull { it.timestamp }

        return TripStatistics(
            trip = trip,
            totalDistance = totalDistance,
            totalFuelConsumed = totalFuelConsumed,
            totalCost = totalCost,
            averageConsumption = averageConsumption,
            refillCount = refills.size,
            startDate = startDate,
            endDate = endDate
        )
    }
}

