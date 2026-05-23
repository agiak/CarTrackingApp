package com.agcoding.cartrackingapp.data.local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.agcoding.cartrackingapp.data.local.database.entity.TripEntity
import com.agcoding.cartrackingapp.data.local.database.entity.TripWithRefills
import kotlinx.coroutines.flow.Flow

@Dao
interface TripDao {

    @Query("SELECT * FROM trips WHERE carId = :carId AND deletedAt IS NULL ORDER BY createdAt DESC")
    fun getTripsByCarId(carId: Long): Flow<List<TripEntity>>

    @Query("SELECT * FROM trips WHERE carId = :carId AND deletedAt IS NULL ORDER BY createdAt DESC LIMIT :limit")
    fun getRecentTripsByCarId(carId: Long, limit: Int): Flow<List<TripEntity>>

    @Query("SELECT * FROM trips WHERE id = :tripId AND deletedAt IS NULL")
    fun getTripById(tripId: Long): Flow<TripEntity?>

    @Transaction
    @Query("SELECT * FROM trips WHERE deletedAt IS NULL ORDER BY createdAt DESC")
    fun getAllTripsWithRefills(): Flow<List<TripWithRefills>>

    @Transaction
    @Query("SELECT * FROM trips WHERE id = :tripId AND deletedAt IS NULL")
    fun getTripWithRefills(tripId: Long): Flow<TripWithRefills?>

    @Transaction
    @Query("SELECT * FROM trips WHERE carId = :carId AND deletedAt IS NULL ORDER BY createdAt DESC")
    fun getTripsWithRefillsByCarId(carId: Long): Flow<List<TripWithRefills>>

    @Transaction
    @Query("SELECT * FROM trips WHERE carId = :carId AND deletedAt IS NULL ORDER BY createdAt DESC LIMIT :limit")
    fun getRecentTripsWithRefillsByCarId(carId: Long, limit: Int): Flow<List<TripWithRefills>>

    @Query("SELECT COUNT(*) FROM trips WHERE carId = :carId AND deletedAt IS NULL")
    fun getTripCountByCarId(carId: Long): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrip(trip: TripEntity): Long

    @Update
    suspend fun updateTrip(trip: TripEntity)

    @Delete
    suspend fun deleteTrip(trip: TripEntity)

    @Query("DELETE FROM trips WHERE id = :tripId")
    suspend fun deleteTripById(tripId: Long)

    @Query("UPDATE fuel_refills SET tripId = :tripId WHERE id IN (:refillIds)")
    suspend fun addRefillsToTrip(tripId: Long, refillIds: List<Long>)

    @Query("UPDATE fuel_refills SET tripId = NULL WHERE id IN (:refillIds)")
    suspend fun removeRefillsFromTrip(refillIds: List<Long>)

    @Query("UPDATE fuel_refills SET tripId = NULL WHERE tripId = :tripId")
    suspend fun removeAllRefillsFromTrip(tripId: Long)

    // Soft delete / trash methods
    @Query("UPDATE trips SET deletedAt = :timestamp WHERE id = :tripId")
    suspend fun softDeleteTrip(tripId: Long, timestamp: Long)

    @Query("UPDATE trips SET deletedAt = NULL WHERE id = :tripId")
    suspend fun restoreTrip(tripId: Long)

    @Query("SELECT * FROM trips WHERE deletedAt IS NOT NULL ORDER BY deletedAt DESC")
    suspend fun getDeletedTrips(): List<TripEntity>

    @Query("DELETE FROM trips WHERE id = :tripId AND deletedAt IS NOT NULL")
    suspend fun permanentlyDeleteTrip(tripId: Long)
}

