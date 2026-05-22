package com.agcoding.cartrackingapp.domain.usecase.car

import com.agcoding.cartrackingapp.domain.repository.CarRepository
import com.agcoding.cartrackingapp.shared.domain.error.AppError
import com.agcoding.cartrackingapp.shared.domain.result.Result
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class UpdateCarUseCase @Inject constructor(
    private val carRepository: CarRepository,
) {
    suspend operator fun invoke(
        carId: Long,
        name: String,
        licensePlate: String,
        currentOdometer: Double,
        insuranceExpirationDate: Long? = null,
        kteoExpirationDate: Long? = null,
        emissionsCardExpirationDate: Long? = null,
        roadTaxAmount: Double? = null,
        roadTaxDueDate: Long? = null,
        lastServiceDate: Long? = null,
        lastTireChangeDate: Long? = null,
        tireBrand: String? = null,
        tireDimensions: String? = null,
        tireInstallationDate: Long? = null,
    ): Result<Unit> = try {
        val existingCar = carRepository.getCarById(carId).first()
            ?: return Result.Error(AppError.NotFound)

        val updatedCar = existingCar.copy(
            name = name.trim(),
            licensePlate = licensePlate.trim(),
            currentOdometer = currentOdometer,
            insuranceExpirationDate = insuranceExpirationDate,
            kteoExpirationDate = kteoExpirationDate,
            emissionsCardExpirationDate = emissionsCardExpirationDate,
            roadTaxAmount = roadTaxAmount,
            roadTaxDueDate = roadTaxDueDate,
            lastServiceDate = lastServiceDate,
            lastTireChangeDate = lastTireChangeDate,
            tireBrand = tireBrand?.trim()?.takeIf { it.isNotBlank() },
            tireDimensions = tireDimensions?.trim()?.takeIf { it.isNotBlank() },
            tireInstallationDate = tireInstallationDate,
            updatedAt = System.currentTimeMillis(),
        )
        carRepository.updateCar(updatedCar)
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(AppError.DatabaseError(e))
    }
}
