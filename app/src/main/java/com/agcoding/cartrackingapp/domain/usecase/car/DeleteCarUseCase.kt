package com.agcoding.cartrackingapp.domain.usecase.car

import com.agcoding.cartrackingapp.domain.repository.CarRepository
import com.agcoding.cartrackingapp.shared.domain.error.AppError
import com.agcoding.cartrackingapp.shared.domain.result.Result
import javax.inject.Inject

class DeleteCarUseCase @Inject constructor(
    private val carRepository: CarRepository,
) {
    suspend operator fun invoke(carId: Long): Result<Unit> = try {
        carRepository.softDeleteCar(carId)
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(AppError.DatabaseError(e))
    }
}
