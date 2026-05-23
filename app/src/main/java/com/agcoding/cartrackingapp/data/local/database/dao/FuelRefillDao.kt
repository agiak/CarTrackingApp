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
}

