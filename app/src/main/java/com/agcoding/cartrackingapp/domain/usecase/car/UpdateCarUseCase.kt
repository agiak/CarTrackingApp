package com.agcoding.cartrackingapp.domain.usecase.car

import com.agcoding.cartrackingapp.domain.repository.CarRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class UpdateCarUseCase @Inject constructor(
    private val carRepository: CarRepository
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
        tireInstallationDate: Long? = null
    ): Result<Unit> {
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

            // Get existing car to preserve calculated fields
            val existingCar = carRepository.getCarById(carId).first()
                ?: return Result.failure(IllegalArgumentException("Car not found"))

            val updatedCar = existingCar.copy(
                name = name.trim(),
                licensePlate = licensePlate.trim(),
                currentOdometer = currentOdometer,
                // Insurance
                insuranceExpirationDate = insuranceExpirationDate,
                // Legal & Compliance
                kteoExpirationDate = kteoExpirationDate,
                emissionsCardExpirationDate = emissionsCardExpirationDate,
                roadTaxAmount = roadTaxAmount,
                roadTaxDueDate = roadTaxDueDate,
                // Maintenance
                lastServiceDate = lastServiceDate,
                lastTireChangeDate = lastTireChangeDate,
                // Tires
                tireBrand = tireBrand?.trim()?.takeIf { it.isNotBlank() },
                tireDimensions = tireDimensions?.trim()?.takeIf { it.isNotBlank() },
                tireInstallationDate = tireInstallationDate,
                updatedAt = System.currentTimeMillis()
            )

            carRepository.updateCar(updatedCar)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

