package com.agcoding.cartrackingapp.data.local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.agcoding.cartrackingapp.data.local.database.entity.FuelRefillEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FuelRefillDao {

    @Query("SELECT * FROM fuel_refills WHERE carId = :carId AND deletedAt IS NULL ORDER BY timestamp DESC")
    fun getRefillsByCarId(carId: Long): Flow<List<FuelRefillEntity>>

    @Query("SELECT * FROM fuel_refills WHERE carId = :carId AND deletedAt IS NULL ORDER BY timestamp DESC")
    suspend fun getRefillsByCarIdSync(carId: Long): List<FuelRefillEntity>

    @Query("SELECT * FROM fuel_refills WHERE deletedAt IS NULL ORDER BY timestamp DESC")
    fun getAllRefills(): Flow<List<FuelRefillEntity>>

    @Query("SELECT * FROM fuel_refills WHERE id = :refillId AND deletedAt IS NULL")
    fun getRefillById(refillId: Long): Flow<FuelRefillEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRefill(refill: FuelRefillEntity): Long

    @Update
    suspend fun updateRefill(refill: FuelRefillEntity)

    @Delete
    suspend fun deleteRefill(refill: FuelRefillEntity)

    @Query("DELETE FROM fuel_refills WHERE id = :refillId")
    suspend fun deleteRefillById(refillId: Long)

    // Soft delete / trash methods
    @Query("UPDATE fuel_refills SET deletedAt = :timestamp WHERE id = :refillId")
    suspend fun softDeleteRefill(refillId: Long, timestamp: Long)

    @Query("UPDATE fuel_refills SET deletedAt = NULL WHERE id = :refillId")
    suspend fun restoreRefill(refillId: Long)

    @Query("SELECT * FROM fuel_refills WHERE deletedAt IS NOT NULL ORDER BY deletedAt DESC")
    suspend fun getDeletedRefills(): List<FuelRefillEntity>

    @Query("DELETE FROM fuel_refills WHERE id = :refillId AND deletedAt IS NOT NULL")
    suspend fun permanentlyDeleteRefill(refillId: Long)

    // Fuel stations & Top refills
    //
    // Every one of these takes the car selection as `allCars` plus `carIds`
    // rather than a nullable id: the screen's car filter is multi-select, and
    // `IN ()` on an empty list matches nothing, which would read as "you have
    // never refuelled" the moment somebody cleared the filter.
    //
    // Price per litre is recomputed as `amountPaid / litersAdded` instead of
    // being read from the stored `pricePerLiter`. That column is written by an
    // unguarded division, so a refill saved with zero litres holds an infinity
    // — and infinity sorts above every real price, permanently.

    @Query(
        """
        SELECT TRIM(COALESCE(locationName, '')) AS rawName,
               SUM(amountPaid) AS totalCost,
               SUM(litersAdded) AS totalLiters,
               COUNT(*) AS refillCount,
               MAX(timestamp) AS lastRefillAt
        FROM fuel_refills
        WHERE deletedAt IS NULL
          AND timestamp BETWEEN :startMillis AND :endMillis
          AND (:allCars = 1 OR carId IN (:carIds))
        GROUP BY rawName
        """
    )
    fun observeStationTotals(
        startMillis: Long,
        endMillis: Long,
        allCars: Boolean,
        carIds: List<Long>,
    ): Flow<List<StationTotalsRow>>

    @Query(
        """
        SELECT * FROM fuel_refills
        WHERE deletedAt IS NULL
          AND timestamp BETWEEN :startMillis AND :endMillis
          AND (:allCars = 1 OR carId IN (:carIds))
        ORDER BY amountPaid DESC, timestamp DESC
        LIMIT :limit
        """
    )
    fun observeTopRefillsByCost(
        startMillis: Long,
        endMillis: Long,
        allCars: Boolean,
        carIds: List<Long>,
        limit: Int,
    ): Flow<List<FuelRefillEntity>>

    @Query(
        """
        SELECT * FROM fuel_refills
        WHERE deletedAt IS NULL
          AND litersAdded > 0
          AND timestamp BETWEEN :startMillis AND :endMillis
          AND (:allCars = 1 OR carId IN (:carIds))
        ORDER BY (amountPaid / litersAdded) DESC, timestamp DESC
        LIMIT :limit
        """
    )
    fun observeTopRefillsByPricePerLiter(
        startMillis: Long,
        endMillis: Long,
        allCars: Boolean,
        carIds: List<Long>,
        limit: Int,
    ): Flow<List<FuelRefillEntity>>

    @Query(
        """
        SELECT * FROM fuel_refills
        WHERE deletedAt IS NULL
          AND timestamp BETWEEN :startMillis AND :endMillis
          AND (:allCars = 1 OR carId IN (:carIds))
        ORDER BY litersAdded DESC, timestamp DESC
        LIMIT :limit
        """
    )
    fun observeTopRefillsByLiters(
        startMillis: Long,
        endMillis: Long,
        allCars: Boolean,
        carIds: List<Long>,
        limit: Int,
    ): Flow<List<FuelRefillEntity>>

    /**
     * Every refill at one station, for the "Show refills" list.
     *
     * Matched on the whole stored string rather than on the parsed station
     * name, because SQLite cannot run the same splitting rules: the caller
     * passes all the spellings its grouping merged into one station.
     */
    @Query(
        """
        SELECT * FROM fuel_refills
        WHERE deletedAt IS NULL
          AND timestamp BETWEEN :startMillis AND :endMillis
          AND (:allCars = 1 OR carId IN (:carIds))
          AND TRIM(COALESCE(locationName, '')) IN (:rawNames)
        ORDER BY timestamp DESC
        """
    )
    fun observeRefillsAtStation(
        startMillis: Long,
        endMillis: Long,
        allCars: Boolean,
        carIds: List<Long>,
        rawNames: List<String>,
    ): Flow<List<FuelRefillEntity>>

    // Sync — trashed rows included on purpose; see CarDao.getAllForSync.

    @Query("SELECT * FROM fuel_refills ORDER BY id ASC")
    suspend fun getAllForSync(): List<FuelRefillEntity>

    @Query("DELETE FROM fuel_refills")
    suspend fun deleteAll()
}

