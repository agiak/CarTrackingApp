package com.agcoding.cartrackingapp.domain.model

data class FuelRefill(
    val id: Long = 0,
    val carId: Long,
    val amountPaid: Double,
    val litersAdded: Double,
    val tripDistance: Double,
    val odometerReading: Double,
    val fuelConsumption: Double,
    val pricePerLiter: Double,
    val location: Location? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val notes: String? = null,
    val tripId: Long? = null,
    val deletedAt: Long? = null
)

