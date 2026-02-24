package com.agcoding.cartrackingapp.domain.model

data class TripStatistics(
    val trip: Trip,
    val totalDistance: Double,
    val totalFuelConsumed: Double,
    val totalCost: Double,
    val averageConsumption: Double,
    val refillCount: Int,
    val startDate: Long?,
    val endDate: Long?
)

