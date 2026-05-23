package com.agcoding.cartrackingapp.data.mapper

import com.agcoding.cartrackingapp.data.local.database.entity.TripEntity
import com.agcoding.cartrackingapp.data.local.database.entity.TripWithRefills
import com.agcoding.cartrackingapp.domain.model.Trip

fun TripEntity.toDomain(): Trip {
    return Trip(
        id = id,
        carId = carId,
        name = name,
        description = description,
        createdAt = createdAt,
        updatedAt = updatedAt,
        refills = emptyList(),
        deletedAt = deletedAt
    )
}

fun TripWithRefills.toDomain(): Trip {
    return Trip(
        id = trip.id,
        carId = trip.carId,
        name = trip.name,
        description = trip.description,
        createdAt = trip.createdAt,
        updatedAt = trip.updatedAt,
        refills = refills.map { it.toDomain() }
    )
}

fun Trip.toEntity(): TripEntity {
    return TripEntity(
        id = id,
        carId = carId,
        name = name,
        description = description,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

