package com.agcoding.cartrackingapp.domain.model

/**
 * Monthly refill data for the refills trend graph
 */
data class MonthlyRefills(
    val month: String,      // e.g., "Jan", "Feb"
    val year: Int,
    val refillCount: Int,
    val totalLiters: Double,
    val totalCost: Double,
    val timestamp: Long
)

/**
 * Recent refill item for the list
 */
data class RefillItem(
    val id: Long,
    val date: Long,
    val liters: Double,
    val cost: Double,
    val pricePerLiter: Double,
    val carName: String?
)

/**
 * Contains all data for the refills trend screen
 */
data class RefillsTrendData(
    val monthlyRefills: List<MonthlyRefills>,
    val totalRefills: Int,
    val averageRefillsPerMonth: Double,
    val highestMonthRefills: Int,
    val lowestMonthRefills: Int,
    val totalLiters: Double,
    val averageLitersPerRefill: Double,
    val recentRefills: List<RefillItem>,
    val dateRange: DateRange
)

