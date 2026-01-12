package com.agcoding.cartrackingapp.domain.usecase.refill

import com.agcoding.cartrackingapp.domain.model.FuelRefill
import com.agcoding.cartrackingapp.domain.model.Location
import com.agcoding.cartrackingapp.domain.repository.RefillRepository
import javax.inject.Inject

class UpdateRefillUseCase @Inject constructor(
    private val refillRepository: RefillRepository
) {
    suspend operator fun invoke(
        refillId: Long,
        carId: Long,
        amountPaid: Double,
        litersAdded: Double,
        tripDistance: Double,
        odometerReading: Double,
        timestamp: Long,
        location: Location?,
        notes: String?
    ): Result<Unit> {
        return try {
            val pricePerLiter = amountPaid / litersAdded
            val fuelConsumption = if (tripDistance > 0) {
                (litersAdded / tripDistance) * 100.0
            } else 0.0

            val refill = FuelRefill(
                id = refillId,
                carId = carId,
                amountPaid = amountPaid,
                litersAdded = litersAdded,
                tripDistance = tripDistance,
                odometerReading = odometerReading,
                fuelConsumption = fuelConsumption,
                pricePerLiter = pricePerLiter,
                location = location,
                timestamp = timestamp,
                notes = notes
            )

            refillRepository.updateRefill(refill)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

