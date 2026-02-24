package com.agcoding.cartrackingapp.domain.repository

import com.agcoding.cartrackingapp.domain.model.Trip
import kotlinx.coroutines.flow.Flow

interface TripRepository {
    fun getAllTrips(): Flow<List<Trip>>
    fun getTripsByCarId(carId: Long): Flow<List<Trip>>
    fun getRecentTripsByCarId(carId: Long, limit: Int): Flow<List<Trip>>
    fun getTripById(tripId: Long): Flow<Trip?>
    fun getTripCountByCarId(carId: Long): Flow<Int>
    suspend fun insertTrip(trip: Trip): Result<Long>
    suspend fun updateTrip(trip: Trip): Result<Unit>
    suspend fun deleteTrip(tripId: Long): Result<Unit>
    suspend fun addRefillsToTrip(tripId: Long, refillIds: List<Long>): Result<Unit>
    suspend fun removeRefillsFromTrip(refillIds: List<Long>): Result<Unit>
}

