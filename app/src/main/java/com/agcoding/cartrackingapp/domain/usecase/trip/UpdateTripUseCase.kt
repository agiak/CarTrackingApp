package com.agcoding.cartrackingapp.domain.usecase.trip

import com.agcoding.cartrackingapp.domain.repository.TripRepository
import com.agcoding.cartrackingapp.shared.domain.error.AppError
import com.agcoding.cartrackingapp.shared.domain.result.Result
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

class UpdateTripUseCase @Inject constructor(
    private val tripRepository: TripRepository,
) {
    suspend operator fun invoke(
        tripId: Long,
        name: String,
        description: String?,
    ): Result<Unit> {
        val existingTrip = tripRepository.getTripById(tripId).firstOrNull()
            ?: return Result.Error(AppError.NotFound)

        return tripRepository.updateTrip(
            existingTrip.copy(
                name = name.trim(),
                description = description?.trim(),
                updatedAt = System.currentTimeMillis(),
            )
        )
    }
}
