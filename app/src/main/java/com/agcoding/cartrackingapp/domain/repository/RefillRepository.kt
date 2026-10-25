package com.agcoding.cartrackingapp.domain.repository

import com.agcoding.cartrackingapp.domain.model.FuelRefill
import com.agcoding.cartrackingapp.domain.model.StationNameTotals
import com.agcoding.cartrackingapp.domain.model.TopRefillSort
import kotlinx.coroutines.flow.Flow

interface RefillRepository {
    fun getRefillsByCarId(carId: Long): Flow<List<FuelRefill>>
    fun getRefillById(refillId: Long): Flow<FuelRefill?>
    fun getAllRefills(): Flow<List<FuelRefill>>

    // Fuel stations & Top refills.
    //
    // These aggregate in SQL rather than loading every refill and folding it in
    // memory: the screen asks for one period at a time, and the history it is
    // asked about is the whole history. `carIds` empty means every car — the
    // screen's filter is multi-select and clearing it means "no narrowing", not
    // "no cars".

    /** Totals per stored location string; several rows may be one station. */
    fun observeStationTotals(
        window: LongRange,
        carIds: Set<Long>,
    ): Flow<List<StationNameTotals>>

    /** The [limit] highest refills by [sort], newest first within a tie. */
    fun observeTopRefills(
        window: LongRange,
        carIds: Set<Long>,
        sort: TopRefillSort,
        limit: Int,
    ): Flow<List<FuelRefill>>

    /** Every refill whose stored location is one of [rawNames], newest first. */
    fun observeRefillsAtStation(
        window: LongRange,
        carIds: Set<Long>,
        rawNames: Set<String>,
    ): Flow<List<FuelRefill>>
    suspend fun insertRefill(refill: FuelRefill): Long
    suspend fun updateRefill(refill: FuelRefill)
    suspend fun deleteRefill(refillId: Long)

    // Soft delete / trash methods
    suspend fun softDeleteRefill(refillId: Long)
    suspend fun restoreRefill(refillId: Long)
    suspend fun getDeletedRefills(): List<FuelRefill>
    suspend fun permanentlyDeleteRefill(refillId: Long)
}

