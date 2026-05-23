package com.agcoding.cartrackingapp.data.local.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "fuel_refills",
    foreignKeys = [
        ForeignKey(
            entity = CarEntity::class,
            parentColumns = ["id"],
            childColumns = ["carId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TripEntity::class,
            parentColumns = ["id"],
            childColumns = ["tripId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("carId"), Index("timestamp"), Index("tripId")]
)
data class FuelRefillEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val carId: Long,
    val amountPaid: Double,
    val litersAdded: Double,
    val tripDistance: Double,
    val odometerReading: Double,
    val fuelConsumption: Double,
    val pricePerLiter: Double,
    val latitude: Double?,
    val longitude: Double?,
    val timestamp: Long,
    val notes: String? = null,
    val tripId: Long? = null,
    val deletedAt: Long? = null
)

