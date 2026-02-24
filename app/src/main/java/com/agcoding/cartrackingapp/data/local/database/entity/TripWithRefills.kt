package com.agcoding.cartrackingapp.data.local.database.entity

import androidx.room.Embedded
import androidx.room.Relation

data class TripWithRefills(
    @Embedded val trip: TripEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "tripId"
    )
    val refills: List<FuelRefillEntity>
)

