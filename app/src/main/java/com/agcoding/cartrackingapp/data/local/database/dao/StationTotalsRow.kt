package com.agcoding.cartrackingapp.data.local.database.dao

/**
 * One row of [FuelRefillDao.observeStationTotals]: every refill that recorded
 * the same `locationName`, added up.
 *
 * This is deliberately *not* one row per station. SQLite groups on the exact
 * bytes, so "Shell" and "shell " land in two rows, and the string still holds a
 * name and an address together. Turning these into stations — folding case,
 * splitting off the address — happens in `GetFuelStationStatsUseCase`, where
 * the parsing rules live. Sums, counts and litres all survive being added up a
 * second time, so pre-aggregating here still saves carrying every refill row
 * into memory.
 */
data class StationTotalsRow(
    /** Trimmed `locationName`; empty for refills that recorded no place. */
    val rawName: String,
    val totalCost: Double,
    val totalLiters: Double,
    val refillCount: Int,
    val lastRefillAt: Long,
)
