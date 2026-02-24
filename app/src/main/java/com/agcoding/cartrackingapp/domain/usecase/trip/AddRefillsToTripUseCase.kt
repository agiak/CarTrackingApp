package com.agcoding.cartrackingapp.domain.usecase.trip

import com.agcoding.cartrackingapp.domain.repository.TripRepository
import javax.inject.Inject

class AddRefillsToTripUseCase @Inject constructor(
    private val tripRepository: TripRepository
) {
    suspend operator fun invoke(tripId: Long, refillIds: List<Long>): Result<Unit> {
        if (refillIds.isEmpty()) {
            return Result.failure(IllegalArgumentException("No refills selected"))
        }
        return tripRepository.addRefillsToTrip(tripId, refillIds)
    }
}

