package com.agcoding.cartrackingapp.domain.usecase.trip

import com.agcoding.cartrackingapp.domain.model.Trip
import com.agcoding.cartrackingapp.domain.repository.TripRepository
import javax.inject.Inject

class CreateTripUseCase @Inject constructor(
    private val tripRepository: TripRepository
) {
    suspend operator fun invoke(
        carId: Long,
        name: String,
        description: String?,
        refillIds: List<Long>
    ): Result<Long> {
        // Validate inputs
        if (name.isBlank()) {
            return Result.failure(IllegalArgumentException("Trip name cannot be empty"))
        }

        if (refillIds.isEmpty()) {
            return Result.failure(IllegalArgumentException("Trip must contain at least one refill"))
        }

        val currentTime = System.currentTimeMillis()
        val trip = Trip(
            carId = carId,
            name = name.trim(),
            description = description?.trim(),
            createdAt = currentTime,
            updatedAt = currentTime
        )

        return tripRepository.insertTrip(trip).onSuccess { tripId ->
            // Add refills to the trip
            tripRepository.addRefillsToTrip(tripId, refillIds)
        }
    }
}

