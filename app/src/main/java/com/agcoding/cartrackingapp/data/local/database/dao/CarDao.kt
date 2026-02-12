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

    @Query("SELECT * FROM cars ORDER BY id ASC")
    fun getAllCars(): Flow<List<CarEntity>>

    @Query("SELECT * FROM cars WHERE id = :carId")
    fun getCarById(carId: Long): Flow<CarEntity?>

    @Query("SELECT * FROM cars WHERE id = :carId")
    suspend fun getCarByIdSync(carId: Long): CarEntity?

    @Transaction
    @Query("SELECT * FROM cars WHERE id = :carId")
    fun getCarWithRefills(carId: Long): Flow<CarWithRefills?>

    @Transaction
    @Query("SELECT * FROM cars ORDER BY id ASC")
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
}
