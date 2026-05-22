package com.agcoding.cartrackingapp.domain.usecase.refill

import com.agcoding.cartrackingapp.domain.model.FuelRefill
import com.agcoding.cartrackingapp.domain.model.Location
import com.agcoding.cartrackingapp.domain.repository.RefillRepository
import com.agcoding.cartrackingapp.shared.domain.error.AppError
import com.agcoding.cartrackingapp.shared.domain.result.Result
import javax.inject.Inject

class UpdateRefillUseCase @Inject constructor(
    private val refillRepository: RefillRepository,
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
        notes: String?,
    ): Result<Unit> = try {
        val pricePerLiter = amountPaid / litersAdded
        val fuelConsumption = if (tripDistance > 0) (litersAdded / tripDistance) * 100.0 else 0.0

        refillRepository.updateRefill(
            FuelRefill(
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
                notes = notes,
            )
        )
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(AppError.DatabaseError(e))
    }
}
