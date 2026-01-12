package com.agcoding.cartrackingapp.domain.usecase.car

import com.agcoding.cartrackingapp.domain.repository.CarRepository
import javax.inject.Inject

class DeleteCarUseCase @Inject constructor(
    private val carRepository: CarRepository
) {
    suspend operator fun invoke(carId: Long): Result<Unit> {
        return try {
            carRepository.deleteCar(carId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

