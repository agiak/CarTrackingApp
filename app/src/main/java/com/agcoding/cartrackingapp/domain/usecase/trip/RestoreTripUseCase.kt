package com.agcoding.cartrackingapp.domain.usecase.trip

import com.agcoding.cartrackingapp.domain.repository.TripRepository
import com.agcoding.cartrackingapp.shared.domain.error.AppError
import com.agcoding.cartrackingapp.shared.domain.result.Result
import javax.inject.Inject

/**
 * Takes a delete back.
 *
 * The counterpart to [DeleteTripUseCase], which only marks the trip — its
 * refills are never touched — so undoing is clearing that one column and the
 * trip comes back with everything that was in it.
 */
class RestoreTripUseCase @Inject constructor(
    private val tripRepository: TripRepository,
) {
    suspend operator fun invoke(tripId: Long): Result<Unit> = try {
        tripRepository.restoreTrip(tripId)
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(AppError.DatabaseError(e))
    }
}
