package com.agcoding.cartrackingapp.data.local.database.entity

import androidx.room.Embedded
import androidx.room.Relation

data class CarWithRefills(
    @Embedded val car: CarEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "carId"
    )
    val refills: List<FuelRefillEntity>
)

