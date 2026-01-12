package com.agcoding.cartrackingapp.domain.usecase.car

import com.agcoding.cartrackingapp.domain.model.Car
import com.agcoding.cartrackingapp.domain.repository.CarRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllCarsUseCase @Inject constructor(
    private val carRepository: CarRepository
) {
    operator fun invoke(): Flow<List<Car>> {
        return carRepository.getAllCars()
    }
}

