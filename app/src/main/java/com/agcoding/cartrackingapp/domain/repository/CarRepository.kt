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
}

