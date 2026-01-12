package com.agcoding.cartrackingapp.domain.usecase.refill

import com.agcoding.cartrackingapp.domain.model.Car
import com.agcoding.cartrackingapp.domain.model.FuelRefill
import com.agcoding.cartrackingapp.domain.repository.CarRepository
import com.agcoding.cartrackingapp.domain.repository.RefillRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import javax.inject.Inject

data class RefillDetails(
    val refill: FuelRefill,
    val car: Car?
)

@OptIn(ExperimentalCoroutinesApi::class)
class GetRefillDetailsUseCase @Inject constructor(
    private val refillRepository: RefillRepository,
    private val carRepository: CarRepository
) {
    operator fun invoke(refillId: Long): Flow<RefillDetails?> {
        return refillRepository.getRefillById(refillId)
            .flatMapLatest { refill ->
                if (refill == null) {
                    flowOf(null)
                } else {
                    carRepository.getCarById(refill.carId)
                        .map { car -> RefillDetails(refill, car) }
                }
            }
    }
}

