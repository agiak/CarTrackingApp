package com.agcoding.cartrackingapp.domain.usecase.car

import com.agcoding.cartrackingapp.domain.model.Car
import com.agcoding.cartrackingapp.domain.repository.CarRepository
import com.agcoding.cartrackingapp.shared.domain.error.AppError
import com.agcoding.cartrackingapp.shared.domain.result.Result
import javax.inject.Inject

class AddCarUseCase @Inject constructor(
    private val carRepository: CarRepository,
) {
    suspend operator fun invoke(
        name: String,
        licensePlate: String,
        currentOdometer: Double,
    ): Result<Long> = try {
        val car = Car(
            name = name.trim(),
            licensePlate = licensePlate.trim(),
            currentOdometer = currentOdometer,
            initialOdometer = currentOdometer,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
        )
        Result.Success(carRepository.insertCar(car))
    } catch (e: Exception) {
        Result.Error(AppError.DatabaseError(e))
    }
}
