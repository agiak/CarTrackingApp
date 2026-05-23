package com.agcoding.cartrackingapp.data.local.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cars")
data class CarEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    // Required fields
    val name: String,
    val licensePlate: String,
    val initialOdometer: Double,
    val currentOdometer: Double,

    // Default car setting
    val isDefault: Boolean = false,

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

    // Legacy fields (kept for backward compatibility)
    val tyreSize: String? = null,
    val licenseExpiration: String? = null,

    val deletedAt: Long? = null,
    val createdAt: Long,
    val updatedAt: Long
)


