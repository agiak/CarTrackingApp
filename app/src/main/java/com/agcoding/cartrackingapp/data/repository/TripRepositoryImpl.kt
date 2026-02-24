package com.agcoding.cartrackingapp.data.repository

import com.agcoding.cartrackingapp.data.local.database.dao.TripDao
import com.agcoding.cartrackingapp.data.mapper.toDomain
import com.agcoding.cartrackingapp.data.mapper.toEntity
import com.agcoding.cartrackingapp.domain.model.Trip
import com.agcoding.cartrackingapp.domain.repository.TripRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TripRepositoryImpl @Inject constructor(
    private val tripDao: TripDao
) : TripRepository {

    override fun getAllTrips(): Flow<List<Trip>> {
        return tripDao.getAllTripsWithRefills().map { tripsWithRefills ->
            tripsWithRefills.map { it.toDomain() }
        }
    }

    override fun getTripsByCarId(carId: Long): Flow<List<Trip>> {
        return tripDao.getTripsWithRefillsByCarId(carId).map { tripsWithRefills ->
            tripsWithRefills.map { it.toDomain() }
        }
    }

    override fun getRecentTripsByCarId(carId: Long, limit: Int): Flow<List<Trip>> {
        return tripDao.getRecentTripsWithRefillsByCarId(carId, limit).map { tripsWithRefills ->
            tripsWithRefills.map { it.toDomain() }
        }
    }

    override fun getTripById(tripId: Long): Flow<Trip?> {
        return tripDao.getTripWithRefills(tripId).map { tripWithRefills ->
            tripWithRefills?.toDomain()
        }
    }

    override fun getTripCountByCarId(carId: Long): Flow<Int> {
        return tripDao.getTripCountByCarId(carId)
    }

    override suspend fun insertTrip(trip: Trip): Result<Long> {
        return try {
            val tripId = tripDao.insertTrip(trip.toEntity())
            Result.success(tripId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateTrip(trip: Trip): Result<Unit> {
        return try {
            tripDao.updateTrip(trip.toEntity())
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteTrip(tripId: Long): Result<Unit> {
        return try {
            // First, remove all refills from the trip
            tripDao.removeAllRefillsFromTrip(tripId)
            // Then delete the trip
            tripDao.deleteTripById(tripId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addRefillsToTrip(tripId: Long, refillIds: List<Long>): Result<Unit> {
        return try {
            tripDao.addRefillsToTrip(tripId, refillIds)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun removeRefillsFromTrip(refillIds: List<Long>): Result<Unit> {
        return try {
            tripDao.removeRefillsFromTrip(refillIds)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

