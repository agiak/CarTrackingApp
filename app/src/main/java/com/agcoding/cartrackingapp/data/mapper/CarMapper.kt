package com.agcoding.cartrackingapp.data.mapper

import com.agcoding.cartrackingapp.data.local.database.entity.CarEntity
import com.agcoding.cartrackingapp.domain.model.Car

fun CarEntity.toDomain(
    averageConsumption: Double = 0.0,
    totalRefills: Int = 0,
    totalCost: Double = 0.0,
    totalDistance: Double = 0.0
): Car {
    return Car(
        id = id,
        name = name,
        licensePlate = licensePlate,
        currentOdometer = initialOdometer + totalDistance,
        initialOdometer = initialOdometer,
        isDefault = isDefault,
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
        tireBrand = tireBrand,
        tireDimensions = tireDimensions,
        tireInstallationDate = tireInstallationDate,
        // Legacy
        tyreSize = tyreSize,
        licenseExpiration = licenseExpiration,
        // Statistics
        averageConsumption = averageConsumption,
        totalRefills = totalRefills,
        totalCost = totalCost,
        totalDistance = totalDistance,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun Car.toEntity(): CarEntity {
    return CarEntity(
        id = id,
        name = name,
        licensePlate = licensePlate,
        currentOdometer = currentOdometer,
        initialOdometer = initialOdometer,
        isDefault = isDefault,
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
        tireBrand = tireBrand,
        tireDimensions = tireDimensions,
        tireInstallationDate = tireInstallationDate,
        // Legacy
        tyreSize = tyreSize,
        licenseExpiration = licenseExpiration,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

