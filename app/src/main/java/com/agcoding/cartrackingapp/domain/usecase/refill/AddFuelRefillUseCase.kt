package com.agcoding.cartrackingapp.domain.usecase.refill

import com.agcoding.cartrackingapp.domain.model.FuelRefill
import com.agcoding.cartrackingapp.domain.model.Location
import com.agcoding.cartrackingapp.domain.repository.CarRepository
import com.agcoding.cartrackingapp.domain.repository.RefillRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class AddFuelRefillUseCase @Inject constructor(
    private val refillRepository: RefillRepository,
    private val carRepository: CarRepository
) {
    suspend operator fun invoke(
        carId: Long,
        amountPaid: Double,
        litersAdded: Double,
        tripDistance: Double,
        timestamp: Long = System.currentTimeMillis(),
        location: Location? = null,
        notes: String? = null
    ): Result<Long> {
        return try {
            // Validation
            if (amountPaid <= 0) {
                return Result.failure(IllegalArgumentException("Amount paid must be positive"))
            }
            if (litersAdded <= 0) {
                return Result.failure(IllegalArgumentException("Liters added must be positive"))
            }
            if (tripDistance < 0) {
                return Result.failure(IllegalArgumentException("Trip distance cannot be negative"))
            }

            // Get current car state
            val car = carRepository.getCarById(carId).first()
                ?: return Result.failure(IllegalArgumentException("Car not found"))

            // Calculate values
            val newOdometerReading = car.currentOdometer + tripDistance
            val fuelConsumption = if (tripDistance > 0) {
                (litersAdded / tripDistance) * 100.0
            } else 0.0
            val pricePerLiter = amountPaid / litersAdded

            // Create refill
            val refill = FuelRefill(
                carId = carId,
                amountPaid = amountPaid,
                litersAdded = litersAdded,
                tripDistance = tripDistance,
                odometerReading = newOdometerReading,
                fuelConsumption = fuelConsumption,
                pricePerLiter = pricePerLiter,
                location = location,
                timestamp = timestamp,
                notes = notes?.trim()
            )

            // Insert refill
            val refillId = refillRepository.insertRefill(refill)

            // Update car odometer
            val updatedCar = car.copy(
                currentOdometer = newOdometerReading,
                updatedAt = System.currentTimeMillis()
            )
            carRepository.updateCar(updatedCar)

            Result.success(refillId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

