package com.agcoding.cartrackingapp.domain.model

data class Trip(
    val id: Long = 0,
    val carId: Long,
    val name: String,
    val description: String? = null,
    val createdAt: Long,
    val updatedAt: Long,
    val refills: List<FuelRefill> = emptyList(),
    val deletedAt: Long? = null
)

