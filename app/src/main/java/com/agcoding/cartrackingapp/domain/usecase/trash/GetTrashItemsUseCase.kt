package com.agcoding.cartrackingapp.domain.usecase.trash

import com.agcoding.cartrackingapp.domain.model.TrashItem
import com.agcoding.cartrackingapp.domain.repository.CarRepository
import com.agcoding.cartrackingapp.domain.repository.ExpenseRepository
import com.agcoding.cartrackingapp.domain.repository.RefillRepository
import com.agcoding.cartrackingapp.domain.repository.TripRepository
import javax.inject.Inject

class GetTrashItemsUseCase @Inject constructor(
    private val carRepository: CarRepository,
    private val refillRepository: RefillRepository,
    private val expenseRepository: ExpenseRepository,
    private val tripRepository: TripRepository,
) {
    suspend operator fun invoke(): List<TrashItem> {
        val deletedCars = carRepository.getDeletedCars()
        val carNameMap: Map<Long, String> = deletedCars.associate { it.id to it.name }

        val allTrashItems = mutableListOf<TrashItem>()

        deletedCars.forEach { car ->
            allTrashItems.add(TrashItem.CarItem(car, car.deletedAt ?: 0L))
        }

        refillRepository.getDeletedRefills().forEach { refill ->
            val carName = carNameMap[refill.carId] ?: "Car #${refill.carId}"
            allTrashItems.add(TrashItem.RefillItem(refill, carName, refill.deletedAt ?: 0L))
        }

        expenseRepository.getDeletedExpenses().forEach { expense ->
            val carName = carNameMap[expense.carId] ?: "Car #${expense.carId}"
            allTrashItems.add(TrashItem.ExpenseItem(expense, carName, expense.deletedAt ?: 0L))
        }

        tripRepository.getDeletedTrips().forEach { trip ->
            val carName = carNameMap[trip.carId] ?: "Car #${trip.carId}"
            allTrashItems.add(TrashItem.TripItem(trip, carName, trip.deletedAt ?: 0L))
        }

        return allTrashItems.sortedByDescending { it.deletedAt }
    }
}
