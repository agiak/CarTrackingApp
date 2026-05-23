package com.agcoding.cartrackingapp.domain.model

data class Car(
    val id: Long = 0,
    // Required fields
    val name: String,
    val licensePlate: String,
    val currentOdometer: Double,
    val initialOdometer: Double,

    // Default car setting
    val isDefault: Boolean = false,

    // Insurance Information
    val insuranceExpirationDate: Long? = null,

    // Legal & Compliance Information
    val kteoExpirationDate: Long? = null, // KTEO expiration date
    val emissionsCardExpirationDate: Long? = null,
    val roadTaxAmount: Double? = null,
    val roadTaxDueDate: Long? = null,

    // Maintenance History
    val lastServiceDate: Long? = null,
    val lastTireChangeDate: Long? = null,

    // Tires Information
    val tireBrand: String? = null,
    val tireDimensions: String? = null, // e.g., "205/55 R16"
    val tireInstallationDate: Long? = null, // Date when current tires were installed

    // Legacy fields (kept for backward compatibility)
    val tyreSize: String? = null,
    val licenseExpiration: String? = null,

    // Statistics
    val averageConsumption: Double = 0.0,
    val totalRefills: Int = 0,
    val totalCost: Double = 0.0,
    val totalDistance: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val deletedAt: Long? = null
)


