package com.agcoding.cartrackingapp.data.export

import com.agcoding.cartrackingapp.domain.model.Car
import com.agcoding.cartrackingapp.domain.model.Expense
import com.agcoding.cartrackingapp.domain.model.FuelRefill
import com.agcoding.cartrackingapp.domain.model.Location
import kotlinx.serialization.Serializable

/**
 * Schema version for export file format.
 * Increment this when the export structure changes.
 */
const val EXPORT_SCHEMA_VERSION = 1

/**
 * Root data structure for export/import operations.
 * Contains all user data in a single JSON file.
 */
@Serializable
data class AppDataExport(
    val schemaVersion: Int = EXPORT_SCHEMA_VERSION,
    val exportDate: Long = System.currentTimeMillis(),
    val appVersion: String = "",
    val data: ExportedData
)

@Serializable
data class ExportedData(
    val cars: List<ExportedCar> = emptyList(),
    val refills: List<ExportedRefill> = emptyList(),
    val expenses: List<ExportedExpense> = emptyList()
)

/**
 * Exported car data - maps to Car domain model
 */
@Serializable
data class ExportedCar(
    val id: Long,
    val name: String,
    val licensePlate: String,
    val currentOdometer: Double,
    val initialOdometer: Double,
    // Insurance Information
    val insuranceExpirationDate: Long? = null,
    // Legal & Compliance Information
    val kteoExpirationDate: Long? = null,
    val emissionsCardExpirationDate: Long? = null,
    val roadTaxAmount: Double? = null,
    val roadTaxDueDate: Long? = null,
    // Maintenance History
    val lastServiceDate: Long? = null,
    val lastTireChangeDate: Long? = null,
    // Tires Information
    val tireBrand: String? = null,
    val tireDimensions: String? = null,
    val tireInstallationDate: Long? = null,
    // Legacy fields
    val tyreSize: String? = null,
    val licenseExpiration: String? = null,
    // Statistics
    val averageConsumption: Double = 0.0,
    val totalRefills: Int = 0,
    val totalCost: Double = 0.0,
    val totalDistance: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Exported fuel refill data - maps to FuelRefill domain model
 */
@Serializable
data class ExportedRefill(
    val id: Long,
    val carId: Long,
    val amountPaid: Double,
    val litersAdded: Double,
    val tripDistance: Double,
    val odometerReading: Double,
    val fuelConsumption: Double,
    val pricePerLiter: Double,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val timestamp: Long,
    val notes: String? = null
)

/**
 * Exported expense data - maps to Expense domain model
 */
@Serializable
data class ExportedExpense(
    val id: Long,
    val carId: Long,
    val category: String,
    val amount: Double,
    val timestamp: Long,
    val notes: String? = null
)

// Extension functions to convert between domain models and export models

fun Car.toExported() = ExportedCar(
    id = id,
    name = name,
    licensePlate = licensePlate,
    currentOdometer = currentOdometer,
    initialOdometer = initialOdometer,
    insuranceExpirationDate = insuranceExpirationDate,
    kteoExpirationDate = kteoExpirationDate,
    emissionsCardExpirationDate = emissionsCardExpirationDate,
    roadTaxAmount = roadTaxAmount,
    roadTaxDueDate = roadTaxDueDate,
    lastServiceDate = lastServiceDate,
    lastTireChangeDate = lastTireChangeDate,
    tireBrand = tireBrand,
    tireDimensions = tireDimensions,
    tireInstallationDate = tireInstallationDate,
    tyreSize = tyreSize,
    licenseExpiration = licenseExpiration,
    averageConsumption = averageConsumption,
    totalRefills = totalRefills,
    totalCost = totalCost,
    totalDistance = totalDistance,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun ExportedCar.toDomain() = Car(
    id = id,
    name = name,
    licensePlate = licensePlate,
    currentOdometer = currentOdometer,
    initialOdometer = initialOdometer,
    insuranceExpirationDate = insuranceExpirationDate,
    kteoExpirationDate = kteoExpirationDate,
    emissionsCardExpirationDate = emissionsCardExpirationDate,
    roadTaxAmount = roadTaxAmount,
    roadTaxDueDate = roadTaxDueDate,
    lastServiceDate = lastServiceDate,
    lastTireChangeDate = lastTireChangeDate,
    tireBrand = tireBrand,
    tireDimensions = tireDimensions,
    tireInstallationDate = tireInstallationDate,
    tyreSize = tyreSize,
    licenseExpiration = licenseExpiration,
    averageConsumption = averageConsumption,
    totalRefills = totalRefills,
    totalCost = totalCost,
    totalDistance = totalDistance,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun FuelRefill.toExported() = ExportedRefill(
    id = id,
    carId = carId,
    amountPaid = amountPaid,
    litersAdded = litersAdded,
    tripDistance = tripDistance,
    odometerReading = odometerReading,
    fuelConsumption = fuelConsumption,
    pricePerLiter = pricePerLiter,
    latitude = location?.latitude,
    longitude = location?.longitude,
    timestamp = timestamp,
    notes = notes
)

fun ExportedRefill.toDomain() = FuelRefill(
    id = id,
    carId = carId,
    amountPaid = amountPaid,
    litersAdded = litersAdded,
    tripDistance = tripDistance,
    odometerReading = odometerReading,
    fuelConsumption = fuelConsumption,
    pricePerLiter = pricePerLiter,
    location = if (latitude != null && longitude != null) Location(latitude, longitude) else null,
    timestamp = timestamp,
    notes = notes
)

fun Expense.toExported() = ExportedExpense(
    id = id,
    carId = carId,
    category = category,
    amount = amount,
    timestamp = timestamp,
    notes = notes
)

fun ExportedExpense.toDomain() = Expense(
    id = id,
    carId = carId,
    category = category,
    amount = amount,
    timestamp = timestamp,
    notes = notes
)

