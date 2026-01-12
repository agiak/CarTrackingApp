package com.agcoding.cartrackingapp.domain.usecase.statistics

import com.agcoding.cartrackingapp.domain.model.CarStatistics
import com.agcoding.cartrackingapp.domain.repository.CarRepository
import com.agcoding.cartrackingapp.domain.repository.ExpenseRepository
import com.agcoding.cartrackingapp.domain.repository.RefillRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class GetCarStatisticsUseCase @Inject constructor(
    private val carRepository: CarRepository,
    private val refillRepository: RefillRepository,
    private val expenseRepository: ExpenseRepository
) {
    operator fun invoke(carId: Long): Flow<CarStatistics?> {
        return combine(
            carRepository.getCarById(carId),
            refillRepository.getRefillsByCarId(carId),
            expenseRepository.getExpensesByCarId(carId)
        ) { car, refills, expenses ->
            car?.let {
                val refillsCost = refills.sumOf { it.amountPaid }
                val expensesCost = expenses.sumOf { it.amount }
                val totalCost = refillsCost + expensesCost

                val totalDistance = refills.sumOf { it.tripDistance }
                val totalLiters = refills.sumOf { it.litersAdded }
                val averageConsumption = if (totalDistance > 0) {
                    (totalLiters / totalDistance) * 100.0
                } else 0.0
                val averagePricePerLiter = if (totalLiters > 0) {
                    refillsCost / totalLiters
                } else 0.0

                // Expense breakdown by category
                // For backward compatibility, group service-related categories together
                val serviceCategories = listOf("Service", "Small service", "Big service", "Oil change", "Tire change", "Repairs")
                val serviceExpenses = expenses.filter { expense ->
                    serviceCategories.any { it.equals(expense.category, ignoreCase = true) }
                }
                val otherExpenses = expenses.filter { expense ->
                    !serviceCategories.any { it.equals(expense.category, ignoreCase = true) }
                }
                val serviceExpensesCost = serviceExpenses.sumOf { it.amount }
                val otherExpensesCost = otherExpenses.sumOf { it.amount }
                val costPerKm = if (totalDistance > 0) totalCost / totalDistance else 0.0

                CarStatistics(
                    car = car,
                    averageConsumption = averageConsumption,
                    totalCost = totalCost,
                    totalDistance = totalDistance,
                    totalRefills = refills.size,
                    averagePricePerLiter = averagePricePerLiter,
                    totalLiters = totalLiters,
                    recentRefills = refills.take(10),
                    recentExpenses = expenses.sortedByDescending { it.timestamp }.take(10),
                    totalExpensesCost = expensesCost,
                    serviceExpensesCost = serviceExpensesCost,
                    otherExpensesCost = otherExpensesCost,
                    serviceExpenseCount = serviceExpenses.size,
                    otherExpenseCount = otherExpenses.size,
                    costPerKilometer = costPerKm
                )
            }
        }
    }
}

