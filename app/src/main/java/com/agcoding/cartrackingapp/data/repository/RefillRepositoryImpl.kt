package com.agcoding.cartrackingapp.data.repository

import com.agcoding.cartrackingapp.data.local.database.dao.FuelRefillDao
import com.agcoding.cartrackingapp.data.mapper.toDomain
import com.agcoding.cartrackingapp.data.mapper.toEntity
import com.agcoding.cartrackingapp.domain.model.FuelRefill
import com.agcoding.cartrackingapp.domain.model.StationNameTotals
import com.agcoding.cartrackingapp.domain.model.TopRefillSort
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

    override fun observeStationTotals(
        window: LongRange,
        carIds: Set<Long>,
    ): Flow<List<StationNameTotals>> = refillDao.observeStationTotals(
        startMillis = window.first,
        endMillis = window.last,
        allCars = carIds.isEmpty(),
        // Room still binds the list even when `allCars` short-circuits the
        // comparison, and it refuses to bind an empty one.
        carIds = carIds.toList().ifEmpty { listOf(NO_CAR) },
    ).map { rows ->
        rows.map { row ->
            StationNameTotals(
                rawName = row.rawName,
                totalCost = row.totalCost,
                totalLiters = row.totalLiters,
                refillCount = row.refillCount,
                lastRefillAt = row.lastRefillAt,
            )
        }
    }

    override fun observeTopRefills(
        window: LongRange,
        carIds: Set<Long>,
        sort: TopRefillSort,
        limit: Int,
    ): Flow<List<FuelRefill>> {
        val allCars = carIds.isEmpty()
        val ids = carIds.toList().ifEmpty { listOf(NO_CAR) }
        val rows = when (sort) {
            TopRefillSort.TOTAL_COST ->
                refillDao.observeTopRefillsByCost(window.first, window.last, allCars, ids, limit)
            TopRefillSort.PRICE_PER_LITER ->
                refillDao.observeTopRefillsByPricePerLiter(window.first, window.last, allCars, ids, limit)
            TopRefillSort.LITERS ->
                refillDao.observeTopRefillsByLiters(window.first, window.last, allCars, ids, limit)
        }
        return rows.map { list -> list.map { it.toDomain() } }
    }

    override fun observeRefillsAtStation(
        window: LongRange,
        carIds: Set<Long>,
        rawNames: Set<String>,
    ): Flow<List<FuelRefill>> = refillDao.observeRefillsAtStation(
        startMillis = window.first,
        endMillis = window.last,
        allCars = carIds.isEmpty(),
        carIds = carIds.toList().ifEmpty { listOf(NO_CAR) },
        // "" is a real name here — it is the bucket for refills with no
        // location — so an empty request cannot fall back to it.
        rawNames = rawNames.toList().ifEmpty { listOf(NO_NAME) },
    ).map { list -> list.map { it.toDomain() } }

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

    private companion object {
        /** Car ids are positive, so this matches nothing — a placeholder, never a filter. */
        const val NO_CAR = -1L

        /** A NUL byte cannot be typed into the location field, so this matches nothing. */
        const val NO_NAME = "\u0000"
    }
}

