package com.agcoding.cartrackingapp.domain.usecase.refill

import com.agcoding.cartrackingapp.domain.model.FuelRefill
import com.agcoding.cartrackingapp.domain.model.Location
import com.agcoding.cartrackingapp.domain.repository.CarRepository
import com.agcoding.cartrackingapp.domain.repository.RefillRepository
import com.agcoding.cartrackingapp.shared.domain.error.AppError
import com.agcoding.cartrackingapp.shared.domain.result.Result
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class AddFuelRefillUseCase @Inject constructor(
    private val refillRepository: RefillRepository,
    private val carRepository: CarRepository,
) {
    suspend operator fun invoke(
        carId: Long,
        amountPaid: Double,
        litersAdded: Double,
        tripDistance: Double,
        timestamp: Long = System.currentTimeMillis(),
        location: Location? = null,
        locationName: String? = null,
        notes: String? = null,
    ): Result<Long> = try {
        val car = carRepository.getCarById(carId).first()
            ?: return Result.Error(AppError.NotFound)

        // currentOdometer is now computed dynamically (initialOdometer + totalDistance),
        // so we use it directly for the historical odometerReading of this refill.
        val odometerReading = car.currentOdometer + tripDistance
        val fuelConsumption = if (tripDistance > 0) (litersAdded / tripDistance) * 100.0 else 0.0
        val pricePerLiter = amountPaid / litersAdded

        val refill = FuelRefill(
            carId = carId,
            amountPaid = amountPaid,
            litersAdded = litersAdded,
            tripDistance = tripDistance,
            odometerReading = odometerReading,
            fuelConsumption = fuelConsumption,
            pricePerLiter = pricePerLiter,
            location = location,
            locationName = locationName?.trim()?.takeIf { it.isNotBlank() },
            timestamp = timestamp,
            notes = notes?.trim(),
        )

        Result.Success(refillRepository.insertRefill(refill))
    } catch (e: Exception) {
        Result.Error(AppError.DatabaseError(e))
    }
}
