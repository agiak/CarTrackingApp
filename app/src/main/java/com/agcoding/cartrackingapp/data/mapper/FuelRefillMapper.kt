package com.agcoding.cartrackingapp.data.mapper

import com.agcoding.cartrackingapp.data.local.database.entity.FuelRefillEntity
import com.agcoding.cartrackingapp.domain.model.FuelRefill
import com.agcoding.cartrackingapp.domain.model.Location

fun FuelRefillEntity.toDomain(): FuelRefill {
    return FuelRefill(
        id = id,
        carId = carId,
        amountPaid = amountPaid,
        litersAdded = litersAdded,
        tripDistance = tripDistance,
        odometerReading = odometerReading,
        fuelConsumption = fuelConsumption,
        pricePerLiter = pricePerLiter,
        location = if (latitude != null && longitude != null) {
            Location(latitude, longitude)
        } else null,
        locationName = locationName,
        timestamp = timestamp,
        notes = notes,
        tripId = tripId,
        deletedAt = deletedAt
    )
}

fun FuelRefill.toEntity(): FuelRefillEntity {
    return FuelRefillEntity(
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
        locationName = locationName,
        timestamp = timestamp,
        notes = notes,
        tripId = tripId,
        deletedAt = deletedAt
    )
}

