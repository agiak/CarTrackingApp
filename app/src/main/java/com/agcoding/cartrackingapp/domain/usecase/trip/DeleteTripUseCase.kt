package com.agcoding.cartrackingapp.domain.usecase.trip

import com.agcoding.cartrackingapp.domain.repository.TripRepository
import com.agcoding.cartrackingapp.shared.domain.error.AppError
import com.agcoding.cartrackingapp.shared.domain.result.Result
import javax.inject.Inject

class DeleteTripUseCase @Inject constructor(
    private val tripRepository: TripRepository,
) {
    suspend operator fun invoke(tripId: Long): Result<Unit> = try {
        tripRepository.softDeleteTrip(tripId)
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(AppError.DatabaseError(e))
    }
}
