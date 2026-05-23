package com.agcoding.cartrackingapp.data.repository

import com.agcoding.cartrackingapp.data.local.database.dao.CarDao
import com.agcoding.cartrackingapp.data.local.database.dao.FuelRefillDao
import com.agcoding.cartrackingapp.data.mapper.toDomain
import com.agcoding.cartrackingapp.data.mapper.toEntity
import com.agcoding.cartrackingapp.domain.model.Car
import com.agcoding.cartrackingapp.domain.repository.CarRepository
import com.agcoding.cartrackingapp.util.calculateConsumption
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
                val refills = carWithRefills.refills.filter { it.deletedAt == null }
                val totalCost = refills.sumOf { it.amountPaid }
                val totalDistance = refills.sumOf { it.tripDistance }
                val totalLiters = refills.sumOf { it.litersAdded }
                val averageConsumption = calculateConsumption(totalLiters, totalDistance)

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
                val refills = it.refills.filter { refill -> refill.deletedAt == null }
                val totalCost = refills.sumOf { refill -> refill.amountPaid }
                val totalDistance = refills.sumOf { refill -> refill.tripDistance }
                val totalLiters = refills.sumOf { refill -> refill.litersAdded }
                val averageConsumption = calculateConsumption(totalLiters, totalDistance)

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

    override suspend fun isLicensePlateExists(licensePlate: String, excludeCarId: Long?): Boolean {
        return carDao.countCarsWithLicensePlate(licensePlate, excludeCarId) > 0
    }

    override fun getDefaultCar(): Flow<Car?> {
        return carDao.getDefaultCar().map { carEntity ->
            carEntity?.let {
                // Get refills for this car to calculate statistics (exclude soft-deleted)
                val refills = refillDao.getRefillsByCarIdSync(it.id).filter { refill -> refill.deletedAt == null }
                val totalCost = refills.sumOf { refill -> refill.amountPaid }
                val totalDistance = refills.sumOf { refill -> refill.tripDistance }
                val totalLiters = refills.sumOf { refill -> refill.litersAdded }
                val averageConsumption = calculateConsumption(totalLiters, totalDistance)

                it.toDomain(
                    averageConsumption = averageConsumption,
                    totalRefills = refills.size,
                    totalCost = totalCost,
                    totalDistance = totalDistance
                )
            }
        }
    }

    override suspend fun setDefaultCar(carId: Long) {
        carDao.setDefaultCar(carId)
    }

    override suspend fun softDeleteCar(carId: Long) {
        carDao.softDeleteCar(carId, System.currentTimeMillis())
    }

    override suspend fun restoreCar(carId: Long) {
        carDao.restoreCar(carId)
    }

    override suspend fun getDeletedCars(): List<Car> {
        return carDao.getDeletedCars().map { entity ->
            entity.toDomain()
        }
    }

    override suspend fun permanentlyDeleteCar(carId: Long) {
        carDao.permanentlyDeleteCar(carId)
    }
}
