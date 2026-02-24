package com.agcoding.cartrackingapp.domain.usecase.trip

import com.agcoding.cartrackingapp.domain.model.Trip
import com.agcoding.cartrackingapp.domain.repository.TripRepository
import javax.inject.Inject

class UpdateTripUseCase @Inject constructor(
    private val tripRepository: TripRepository
) {
    suspend operator fun invoke(
        tripId: Long,
        name: String,
        description: String?
    ): Result<Unit> {
        if (name.isBlank()) {
            return Result.failure(IllegalArgumentException("Trip name cannot be empty"))
        }

        // Get the existing trip first
        var existingTrip: Trip? = null
        tripRepository.getTripById(tripId).collect { trip ->
            existingTrip = trip
        }

        return existingTrip?.let { trip ->
            val updatedTrip = trip.copy(
                name = name.trim(),
                description = description?.trim(),
                updatedAt = System.currentTimeMillis()
            )
            tripRepository.updateTrip(updatedTrip)
        } ?: Result.failure(IllegalArgumentException("Trip not found"))
    }
}

