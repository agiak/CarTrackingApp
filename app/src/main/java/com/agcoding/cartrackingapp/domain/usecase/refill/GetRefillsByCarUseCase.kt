package com.agcoding.cartrackingapp.domain.usecase.refill

import com.agcoding.cartrackingapp.domain.model.FuelRefill
import com.agcoding.cartrackingapp.domain.repository.RefillRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetRefillsByCarUseCase @Inject constructor(
    private val refillRepository: RefillRepository
) {
    operator fun invoke(carId: Long): Flow<List<FuelRefill>> {
        return refillRepository.getRefillsByCarId(carId)
    }
}

