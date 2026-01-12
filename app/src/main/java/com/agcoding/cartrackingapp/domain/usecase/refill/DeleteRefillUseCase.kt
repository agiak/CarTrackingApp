package com.agcoding.cartrackingapp.domain.usecase.refill

import com.agcoding.cartrackingapp.domain.repository.RefillRepository
import javax.inject.Inject

class DeleteRefillUseCase @Inject constructor(
    private val refillRepository: RefillRepository
) {
    suspend operator fun invoke(refillId: Long): Result<Unit> {
        return try {
            refillRepository.deleteRefill(refillId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

