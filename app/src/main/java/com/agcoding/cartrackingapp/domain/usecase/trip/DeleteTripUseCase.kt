package com.agcoding.cartrackingapp.domain.usecase.trip

import com.agcoding.cartrackingapp.domain.repository.TripRepository
import javax.inject.Inject

class DeleteTripUseCase @Inject constructor(
    private val tripRepository: TripRepository
) {
    suspend operator fun invoke(tripId: Long): Result<Unit> {
        return tripRepository.deleteTrip(tripId)
    }
}

