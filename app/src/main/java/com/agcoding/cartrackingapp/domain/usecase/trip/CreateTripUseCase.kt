package com.agcoding.cartrackingapp.domain.usecase.trip

import com.agcoding.cartrackingapp.domain.model.Trip
import com.agcoding.cartrackingapp.domain.repository.TripRepository
import com.agcoding.cartrackingapp.shared.domain.result.Result
import javax.inject.Inject

class CreateTripUseCase @Inject constructor(
    private val tripRepository: TripRepository,
) {
    suspend operator fun invoke(
        carId: Long,
        name: String,
        description: String?,
        refillIds: List<Long>,
    ): Result<Long> {
        val currentTime = System.currentTimeMillis()
        val trip = Trip(
            carId = carId,
            name = name.trim(),
            description = description?.trim(),
            createdAt = currentTime,
            updatedAt = currentTime,
        )

        return when (val insertResult = tripRepository.insertTrip(trip)) {
            is Result.Success -> {
                tripRepository.addRefillsToTrip(insertResult.data, refillIds)
                insertResult
            }
            is Result.Error -> insertResult
        }
    }
}
