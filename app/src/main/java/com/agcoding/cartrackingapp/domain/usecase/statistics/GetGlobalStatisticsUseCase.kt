package com.agcoding.cartrackingapp.domain.usecase.statistics

import com.agcoding.cartrackingapp.domain.model.CarStatistics
import com.agcoding.cartrackingapp.domain.model.GlobalStatistics
import com.agcoding.cartrackingapp.domain.model.MonthlyTrend
import com.agcoding.cartrackingapp.domain.repository.CarRepository
import com.agcoding.cartrackingapp.domain.repository.RefillRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

class GetGlobalStatisticsUseCase @Inject constructor(
    private val carRepository: CarRepository,
    private val refillRepository: RefillRepository,
    private val expenseRepository: com.agcoding.cartrackingapp.domain.repository.ExpenseRepository
) {
    operator fun invoke(): Flow<GlobalStatistics> {
        return combine(
            carRepository.getAllCars(),
            refillRepository.getAllRefills(),
            expenseRepository.getAllExpenses()
        ) { cars, refills, expenses ->
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

            // Find most efficient car
            val mostEfficientCar = cars.minByOrNull { it.averageConsumption }

            // Find most expensive car
            val mostExpensiveCar = cars.maxByOrNull { it.totalCost }

            // Calculate monthly trends
            val monthlyTrends = calculateMonthlyTrends(refills, expenses)

            // Calculate per-car statistics
            val perCarStatistics = cars.map { car ->
                val carRefills = refills.filter { it.carId == car.id }
                val carExpenses = expenses.filter { it.carId == car.id }
                val carServiceExpenses = carExpenses.filter { expense ->
                    serviceCategories.any { it.equals(expense.category, ignoreCase = true) }
                }
                val carOtherExpenses = carExpenses.filter { expense ->
                    !serviceCategories.any { it.equals(expense.category, ignoreCase = true) }
                }
                val carTotalCost = carRefills.sumOf { it.amountPaid } + carExpenses.sumOf { it.amount }
                val carTotalDistance = carRefills.sumOf { it.tripDistance }

                CarStatistics(
                    car = car,
                    averageConsumption = car.averageConsumption,
                    totalCost = carTotalCost,
                    totalDistance = carTotalDistance,
                    totalRefills = carRefills.size,
                    averagePricePerLiter = if (carRefills.sumOf { it.litersAdded } > 0) {
                        carRefills.sumOf { it.amountPaid } / carRefills.sumOf { it.litersAdded }
                    } else 0.0,
                    totalLiters = carRefills.sumOf { it.litersAdded },
                    recentRefills = carRefills.sortedByDescending { it.timestamp }.take(5),
                    recentExpenses = carExpenses.sortedByDescending { it.timestamp }.take(5),
                    totalExpensesCost = carExpenses.sumOf { it.amount },
                    serviceExpensesCost = carServiceExpenses.sumOf { it.amount },
                    otherExpensesCost = carOtherExpenses.sumOf { it.amount },
                    serviceExpenseCount = carServiceExpenses.size,
                    otherExpenseCount = carOtherExpenses.size,
                    costPerKilometer = if (carTotalDistance > 0) carTotalCost / carTotalDistance else 0.0
                )
            }.sortedBy { it.car.name }

            GlobalStatistics(
                totalCars = cars.size,
                totalRefills = refills.size,
                totalCost = totalCost,
                totalDistance = totalDistance,
                totalLiters = totalLiters,
                averageConsumption = averageConsumption,
                averagePricePerLiter = averagePricePerLiter,
                mostEfficientCar = mostEfficientCar,
                mostExpensiveCar = mostExpensiveCar,
                monthlyTrends = monthlyTrends,
                perCarStatistics = perCarStatistics,
                totalServiceExpenses = serviceExpensesCost,
                totalOtherExpenses = otherExpensesCost,
                totalExpensesCost = expensesCost,
                serviceExpenseCount = serviceExpenses.size,
                otherExpenseCount = otherExpenses.size,
                totalExpenseCount = expenses.size,
                costPerKilometer = costPerKm
            )
        }
    }

    private fun calculateMonthlyTrends(
        refills: List<com.agcoding.cartrackingapp.domain.model.FuelRefill>,
        expenses: List<com.agcoding.cartrackingapp.domain.model.Expense>
    ): List<MonthlyTrend> {
        val calendar = Calendar.getInstance()
        val monthFormat = SimpleDateFormat("MMM", Locale.getDefault())

        // Group refills by month
        val refillsByMonth = refills.groupBy { refill ->
            calendar.timeInMillis = refill.timestamp
            calendar.get(Calendar.YEAR) to calendar.get(Calendar.MONTH)
        }

        // Group expenses by month
        val expensesByMonth = expenses.groupBy { expense ->
            calendar.timeInMillis = expense.timestamp
            calendar.get(Calendar.YEAR) to calendar.get(Calendar.MONTH)
        }

        // Get all unique months from both refills and expenses
        val allMonths = (refillsByMonth.keys + expensesByMonth.keys).distinct()

        return allMonths.map { yearMonth ->
            val (year, month) = yearMonth
            calendar.set(year, month, 1)
            val monthName = monthFormat.format(calendar.time)

            val monthRefills = refillsByMonth[yearMonth] ?: emptyList()
            val monthExpenses = expensesByMonth[yearMonth] ?: emptyList()

            val refillCost = monthRefills.sumOf { it.amountPaid }
            val totalLiters = monthRefills.sumOf { it.litersAdded }
            val totalDistance = monthRefills.sumOf { it.tripDistance }
            val averageConsumption = if (totalDistance > 0) {
                (totalLiters / totalDistance) * 100.0
            } else 0.0

            val expenseCost = monthExpenses.sumOf { it.amount }

            MonthlyTrend(
                month = month,
                year = year,
                monthName = monthName,
                totalCost = refillCost,
                totalLiters = totalLiters,
                totalDistance = totalDistance,
                averageConsumption = averageConsumption,
                refillCount = monthRefills.size,
                expenseCount = monthExpenses.size,
                expenseCost = expenseCost,
                totalCombinedCost = refillCost + expenseCost
            )
        }
            .sortedByDescending { it.year * 100 + it.month }
    }
}
