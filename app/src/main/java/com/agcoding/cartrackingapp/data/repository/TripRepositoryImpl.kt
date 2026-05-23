package com.agcoding.cartrackingapp.data.repository

import com.agcoding.cartrackingapp.data.local.database.dao.TripDao
import com.agcoding.cartrackingapp.data.mapper.toDomain
import com.agcoding.cartrackingapp.data.mapper.toEntity
import com.agcoding.cartrackingapp.domain.model.Trip
import com.agcoding.cartrackingapp.domain.repository.TripRepository
import com.agcoding.cartrackingapp.shared.domain.error.AppError
import com.agcoding.cartrackingapp.shared.domain.result.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TripRepositoryImpl @Inject constructor(
    private val tripDao: TripDao
) : TripRepository {

    override fun getAllTrips(): Flow<List<Trip>> =
        tripDao.getAllTripsWithRefills().map { it.map { tw -> tw.toDomain() } }

    override fun getTripsByCarId(carId: Long): Flow<List<Trip>> =
        tripDao.getTripsWithRefillsByCarId(carId).map { it.map { tw -> tw.toDomain() } }

    override fun getRecentTripsByCarId(carId: Long, limit: Int): Flow<List<Trip>> =
        tripDao.getRecentTripsWithRefillsByCarId(carId, limit).map { it.map { tw -> tw.toDomain() } }

    override fun getTripById(tripId: Long): Flow<Trip?> =
        tripDao.getTripWithRefills(tripId).map { it?.toDomain() }

    override fun getTripCountByCarId(carId: Long): Flow<Int> =
        tripDao.getTripCountByCarId(carId)

    override suspend fun insertTrip(trip: Trip): Result<Long> = try {
        Result.Success(tripDao.insertTrip(trip.toEntity()))
    } catch (e: Exception) {
        Result.Error(AppError.DatabaseError(e))
    }

    override suspend fun updateTrip(trip: Trip): Result<Unit> = try {
        tripDao.updateTrip(trip.toEntity())
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(AppError.DatabaseError(e))
    }

    override suspend fun deleteTrip(tripId: Long): Result<Unit> = try {
        tripDao.removeAllRefillsFromTrip(tripId)
        tripDao.deleteTripById(tripId)
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(AppError.DatabaseError(e))
    }

    override suspend fun addRefillsToTrip(tripId: Long, refillIds: List<Long>): Result<Unit> = try {
        tripDao.addRefillsToTrip(tripId, refillIds)
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(AppError.DatabaseError(e))
    }

    override suspend fun removeRefillsFromTrip(refillIds: List<Long>): Result<Unit> = try {
        tripDao.removeRefillsFromTrip(refillIds)
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(AppError.DatabaseError(e))
    }

    override suspend fun softDeleteTrip(tripId: Long) {
        tripDao.softDeleteTrip(tripId, System.currentTimeMillis())
    }

    override suspend fun restoreTrip(tripId: Long) {
        tripDao.restoreTrip(tripId)
    }

    override suspend fun getDeletedTrips(): List<Trip> {
        return tripDao.getDeletedTrips().map { it.toDomain() }
    }

    override suspend fun permanentlyDeleteTrip(tripId: Long) {
        tripDao.permanentlyDeleteTrip(tripId)
    }
}
