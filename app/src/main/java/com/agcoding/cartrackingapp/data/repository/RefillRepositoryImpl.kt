package com.agcoding.cartrackingapp.data.repository

import com.agcoding.cartrackingapp.data.local.database.dao.FuelRefillDao
import com.agcoding.cartrackingapp.data.mapper.toDomain
import com.agcoding.cartrackingapp.data.mapper.toEntity
import com.agcoding.cartrackingapp.domain.model.FuelRefill
import com.agcoding.cartrackingapp.domain.repository.RefillRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class RefillRepositoryImpl @Inject constructor(
    private val refillDao: FuelRefillDao
) : RefillRepository {

    override fun getRefillsByCarId(carId: Long): Flow<List<FuelRefill>> {
        return refillDao.getRefillsByCarId(carId).map { refills ->
            refills.map { it.toDomain() }
        }
    }

    override fun getRefillById(refillId: Long): Flow<FuelRefill?> {
        return refillDao.getRefillById(refillId).map { it?.toDomain() }
    }

    override fun getAllRefills(): Flow<List<FuelRefill>> {
        return refillDao.getAllRefills().map { refills ->
            refills.map { it.toDomain() }
        }
    }

    override suspend fun insertRefill(refill: FuelRefill): Long {
        return refillDao.insertRefill(refill.toEntity())
    }

    override suspend fun updateRefill(refill: FuelRefill) {
        refillDao.updateRefill(refill.toEntity())
    }

    override suspend fun deleteRefill(refillId: Long) {
        refillDao.deleteRefillById(refillId)
    }

    override suspend fun softDeleteRefill(refillId: Long) {
        refillDao.softDeleteRefill(refillId, System.currentTimeMillis())
    }

    override suspend fun restoreRefill(refillId: Long) {
        refillDao.restoreRefill(refillId)
    }

    override suspend fun getDeletedRefills(): List<FuelRefill> {
        return refillDao.getDeletedRefills().map { it.toDomain() }
    }

    override suspend fun permanentlyDeleteRefill(refillId: Long) {
        refillDao.permanentlyDeleteRefill(refillId)
    }
}

