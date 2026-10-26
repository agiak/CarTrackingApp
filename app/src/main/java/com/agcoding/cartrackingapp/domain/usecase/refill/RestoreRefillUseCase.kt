package com.agcoding.cartrackingapp.domain.usecase.refill

import com.agcoding.cartrackingapp.domain.repository.RefillRepository
import com.agcoding.cartrackingapp.shared.domain.error.AppError
import com.agcoding.cartrackingapp.shared.domain.result.Result
import javax.inject.Inject

/**
 * Takes a delete back.
 *
 * The counterpart to [DeleteRefillUseCase], which only ever marked the row:
 * undoing is clearing the same column, not writing the refill again, so
 * nothing about it — its id, its trip, its place in the odometer sequence —
 * has to be reconstructed.
 */
class RestoreRefillUseCase @Inject constructor(
    private val refillRepository: RefillRepository,
) {
    suspend operator fun invoke(refillId: Long): Result<Unit> = try {
        refillRepository.restoreRefill(refillId)
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(AppError.DatabaseError(e))
    }
}
