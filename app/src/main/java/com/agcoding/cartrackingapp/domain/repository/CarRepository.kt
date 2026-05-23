package com.agcoding.cartrackingapp.domain.repository

import com.agcoding.cartrackingapp.domain.model.Car
import kotlinx.coroutines.flow.Flow

interface CarRepository {
    fun getAllCars(): Flow<List<Car>>
    fun getCarById(carId: Long): Flow<Car?>
    suspend fun insertCar(car: Car): Long
    suspend fun updateCar(car: Car)
    suspend fun deleteCar(carId: Long)
    suspend fun isLicensePlateExists(licensePlate: String, excludeCarId: Long? = null): Boolean

    // Default car methods
    fun getDefaultCar(): Flow<Car?>
    suspend fun setDefaultCar(carId: Long)

    // Soft delete / trash methods
    suspend fun softDeleteCar(carId: Long)
    suspend fun restoreCar(carId: Long)
    suspend fun getDeletedCars(): List<Car>
    suspend fun permanentlyDeleteCar(carId: Long)
}

