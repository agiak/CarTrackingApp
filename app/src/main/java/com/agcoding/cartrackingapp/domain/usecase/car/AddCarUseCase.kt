package com.agcoding.cartrackingapp.domain.usecase.car

import android.content.Context
import com.agcoding.cartrackingapp.domain.model.Car
import com.agcoding.cartrackingapp.domain.repository.CarRepository
import com.agcoding.cartrackingapp.widget.QuickAddWidgetReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AddCarUseCase @Inject constructor(
    private val carRepository: CarRepository,
    @ApplicationContext private val context: Context
) {
    suspend operator fun invoke(
        name: String,
        licensePlate: String,
        currentOdometer: Double
    ): Result<Long> {
        return try {
            if (name.isBlank()) {
                return Result.failure(IllegalArgumentException("Car name cannot be empty"))
            }
            if (licensePlate.isBlank()) {
                return Result.failure(IllegalArgumentException("License plate cannot be empty"))
            }
            if (currentOdometer < 0) {
                return Result.failure(IllegalArgumentException("Odometer value must be positive"))
            }

            val car = Car(
                name = name.trim(),
                licensePlate = licensePlate.trim(),
                currentOdometer = currentOdometer,
                initialOdometer = currentOdometer,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            val id = carRepository.insertCar(car)

            // Update widgets when a car is added
            QuickAddWidgetReceiver.updateWidgets(context)

            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

