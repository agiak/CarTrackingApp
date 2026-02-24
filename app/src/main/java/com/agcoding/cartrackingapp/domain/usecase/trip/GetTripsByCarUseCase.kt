package com.agcoding.cartrackingapp.domain.usecase.trip

import com.agcoding.cartrackingapp.domain.model.Trip
import com.agcoding.cartrackingapp.domain.repository.TripRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTripsByCarUseCase @Inject constructor(
    private val tripRepository: TripRepository
) {
    operator fun invoke(carId: Long): Flow<List<Trip>> {
        return tripRepository.getTripsByCarId(carId)
    }
}

