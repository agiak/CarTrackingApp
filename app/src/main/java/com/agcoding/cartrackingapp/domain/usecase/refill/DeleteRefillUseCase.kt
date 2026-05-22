package com.agcoding.cartrackingapp.domain.usecase.refill

import com.agcoding.cartrackingapp.domain.repository.RefillRepository
import com.agcoding.cartrackingapp.shared.domain.error.AppError
import com.agcoding.cartrackingapp.shared.domain.result.Result
import javax.inject.Inject

class DeleteRefillUseCase @Inject constructor(
    private val refillRepository: RefillRepository,
) {
    suspend operator fun invoke(refillId: Long): Result<Unit> = try {
        refillRepository.deleteRefill(refillId)
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(AppError.DatabaseError(e))
    }
}
