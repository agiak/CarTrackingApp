package com.agcoding.cartrackingapp.data.local.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "expenses",
    foreignKeys = [
        ForeignKey(
            entity = CarEntity::class,
            parentColumns = ["id"],
            childColumns = ["carId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("carId"),
        Index("timestamp"),
        Index("category")
    ]
)
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val carId: Long,
    val category: String, // User-defined category (e.g., "Tire change", "Oil change", custom)
    val amount: Double,
    val timestamp: Long,
    val notes: String? = null,
    // Service reminder fields (optional)
    val reminderDate: Long? = null, // Future date for service reminder
    val reminderMileage: Int? = null, // Mileage value for service reminder
    val reminderEnabled: Boolean = true, // Whether notifications are enabled for this reminder
    val preExpiryNotificationSent: Boolean = false, // Whether pre-expiry notification was already sent
    val reminderDismissed: Boolean = false, // Whether user dismissed the reminder alert
    val deletedAt: Long? = null
)
