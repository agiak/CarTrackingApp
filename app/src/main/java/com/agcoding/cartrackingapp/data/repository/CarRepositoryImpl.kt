package com.agcoding.cartrackingapp.data.repository

import com.agcoding.cartrackingapp.data.local.database.dao.CarDao
import com.agcoding.cartrackingapp.data.local.database.dao.FuelRefillDao
import com.agcoding.cartrackingapp.data.mapper.toDomain
import com.agcoding.cartrackingapp.data.mapper.toEntity
import com.agcoding.cartrackingapp.domain.model.Car
import com.agcoding.cartrackingapp.domain.repository.CarRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CarRepositoryImpl @Inject constructor(
    private val carDao: CarDao,
    private val refillDao: FuelRefillDao
) : CarRepository {

    override fun getAllCars(): Flow<List<Car>> {
        return carDao.getAllCarsWithRefills().map { carsWithRefills ->
            carsWithRefills.map { carWithRefills ->
                val refills = carWithRefills.refills
                val totalCost = refills.sumOf { it.amountPaid }
                val totalDistance = refills.sumOf { it.tripDistance }
                val totalLiters = refills.sumOf { it.litersAdded }
                val averageConsumption = if (totalDistance > 0) {
                    (totalLiters / totalDistance) * 100.0
                } else 0.0

                carWithRefills.car.toDomain(
                    averageConsumption = averageConsumption,
                    totalRefills = refills.size,
                    totalCost = totalCost,
                    totalDistance = totalDistance
                )
            }
        }
    }

    override fun getCarById(carId: Long): Flow<Car?> {
        return carDao.getCarWithRefills(carId).map { carWithRefills ->
            carWithRefills?.let {
                val refills = it.refills
                val totalCost = refills.sumOf { refill -> refill.amountPaid }
                val totalDistance = refills.sumOf { refill -> refill.tripDistance }
                val totalLiters = refills.sumOf { refill -> refill.litersAdded }
                val averageConsumption = if (totalDistance > 0) {
                    (totalLiters / totalDistance) * 100.0
                } else 0.0

                it.car.toDomain(
                    averageConsumption = averageConsumption,
                    totalRefills = refills.size,
                    totalCost = totalCost,
                    totalDistance = totalDistance
                )
            }
        }
    }

    override suspend fun insertCar(car: Car): Long {
        return carDao.insertCar(car.toEntity())
    }

    override suspend fun updateCar(car: Car) {
        carDao.updateCar(car.toEntity())
    }

    override suspend fun deleteCar(carId: Long) {
        carDao.deleteCarById(carId)
    }
}

