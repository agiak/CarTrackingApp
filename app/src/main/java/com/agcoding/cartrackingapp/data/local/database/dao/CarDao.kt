package com.agcoding.cartrackingapp.data.local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.agcoding.cartrackingapp.data.local.database.entity.CarEntity
import com.agcoding.cartrackingapp.data.local.database.entity.CarWithRefills
import kotlinx.coroutines.flow.Flow

@Dao
interface CarDao {

    @Query("SELECT * FROM cars WHERE deletedAt IS NULL ORDER BY id ASC")
    fun getAllCars(): Flow<List<CarEntity>>

    @Query("SELECT * FROM cars WHERE id = :carId AND deletedAt IS NULL")
    fun getCarById(carId: Long): Flow<CarEntity?>

    @Query("SELECT * FROM cars WHERE id = :carId AND deletedAt IS NULL")
    suspend fun getCarByIdSync(carId: Long): CarEntity?

    @Transaction
    @Query("SELECT * FROM cars WHERE id = :carId AND deletedAt IS NULL")
    fun getCarWithRefills(carId: Long): Flow<CarWithRefills?>

    @Transaction
    @Query("SELECT * FROM cars WHERE deletedAt IS NULL ORDER BY id ASC")
    fun getAllCarsWithRefills(): Flow<List<CarWithRefills>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCar(car: CarEntity): Long

    @Update
    suspend fun updateCar(car: CarEntity)

    @Delete
    suspend fun deleteCar(car: CarEntity)

    @Query("DELETE FROM cars WHERE id = :carId")
    suspend fun deleteCarById(carId: Long)

    @Query("SELECT COUNT(*) FROM cars WHERE LOWER(licensePlate) = LOWER(:licensePlate) AND id != COALESCE(:excludeCarId, -1)")
    suspend fun countCarsWithLicensePlate(licensePlate: String, excludeCarId: Long?): Int

    // Default car methods
    @Query("SELECT * FROM cars WHERE isDefault = 1 AND deletedAt IS NULL LIMIT 1")
    fun getDefaultCar(): Flow<CarEntity?>

    @Query("SELECT * FROM cars WHERE isDefault = 1 AND deletedAt IS NULL LIMIT 1")
    suspend fun getDefaultCarSync(): CarEntity?

    @Query("UPDATE cars SET isDefault = 0")
    suspend fun clearAllDefaultFlags()

    @Query("UPDATE cars SET isDefault = CASE WHEN id = :carId THEN 1 ELSE 0 END")
    suspend fun setDefaultCar(carId: Long)

    // Soft delete / trash methods
    @Query("UPDATE cars SET deletedAt = :timestamp WHERE id = :carId")
    suspend fun softDeleteCar(carId: Long, timestamp: Long)

    @Query("UPDATE cars SET deletedAt = NULL WHERE id = :carId")
    suspend fun restoreCar(carId: Long)

    @Query("SELECT * FROM cars WHERE deletedAt IS NOT NULL ORDER BY deletedAt DESC")
    suspend fun getDeletedCars(): List<CarEntity>

    @Query("DELETE FROM cars WHERE id = :carId AND deletedAt IS NOT NULL")
    suspend fun permanentlyDeleteCar(carId: Long)
}
