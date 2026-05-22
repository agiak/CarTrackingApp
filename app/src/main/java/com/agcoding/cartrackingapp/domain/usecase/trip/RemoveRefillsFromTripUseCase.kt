package com.agcoding.cartrackingapp.domain.usecase.trip

import com.agcoding.cartrackingapp.domain.repository.TripRepository
import com.agcoding.cartrackingapp.shared.domain.result.Result
import javax.inject.Inject

class RemoveRefillsFromTripUseCase @Inject constructor(
    private val tripRepository: TripRepository,
) {
    suspend operator fun invoke(refillIds: List<Long>): Result<Unit> =
        tripRepository.removeRefillsFromTrip(refillIds)
}
