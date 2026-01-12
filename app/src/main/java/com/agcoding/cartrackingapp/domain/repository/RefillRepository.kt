package com.agcoding.cartrackingapp.domain.repository

import com.agcoding.cartrackingapp.domain.model.FuelRefill
import kotlinx.coroutines.flow.Flow

interface RefillRepository {
    fun getRefillsByCarId(carId: Long): Flow<List<FuelRefill>>
    fun getRefillById(refillId: Long): Flow<FuelRefill?>
    fun getAllRefills(): Flow<List<FuelRefill>>
    suspend fun insertRefill(refill: FuelRefill): Long
    suspend fun updateRefill(refill: FuelRefill)
    suspend fun deleteRefill(refillId: Long)
}

