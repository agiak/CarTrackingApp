package com.agcoding.cartrackingapp.domain.usecase.expense

import com.agcoding.cartrackingapp.domain.model.Car
import com.agcoding.cartrackingapp.domain.model.Expense
import com.agcoding.cartrackingapp.domain.repository.CarRepository
import com.agcoding.cartrackingapp.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Data class representing an expense with reminder information
 */
data class ExpenseReminder(
    val expense: Expense,
    val carName: String,
    val currentOdometer: Double,
    val remainingKm: Int? = null // Calculated: reminderMileage - currentOdometer
)

/**
 * Use case to get all expenses that have reminder settings enabled,
 * grouped by service type with only the most future reminder shown per type
 */
class GetExpensesWithRemindersUseCase @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val carRepository: CarRepository
) {
    operator fun invoke(): Flow<List<ExpenseReminder>> {
        return combine(
            expenseRepository.getAllExpenses(),
            carRepository.getAllCars()
        ) { expenses, cars ->
            val carMap = cars.associateBy { it.id }

            // Filter expenses that have at least one reminder set
            val expensesWithReminders = expenses.filter { expense ->
                expense.reminderDate != null || expense.reminderMileage != null
            }

            // Group by category (service type) and keep only the most future one
            val groupedByCategory = expensesWithReminders.groupBy { it.category }

            val result = groupedByCategory.mapNotNull { (_, expensesInCategory) ->
                // Find the expense with the most future reminder
                val mostFutureExpense = expensesInCategory.maxWithOrNull(
                    compareBy<Expense> { expense ->
                        // Primary sort: by reminder date (null dates are treated as minimum)
                        expense.reminderDate ?: Long.MIN_VALUE
                    }.thenBy { expense ->
                        // Secondary sort: by reminder mileage
                        expense.reminderMileage ?: Int.MIN_VALUE
                    }
                )

                mostFutureExpense?.let { expense ->
                    val car = carMap[expense.carId]
                    car?.let {
                        val remainingKm = expense.reminderMileage?.let { targetMileage ->
                            (targetMileage - car.currentOdometer.toInt()).coerceAtLeast(0)
                        }

                        ExpenseReminder(
                            expense = expense,
                            carName = car.name,
                            currentOdometer = car.currentOdometer,
                            remainingKm = remainingKm
                        )
                    }
                }
            }

            // Sort by nearest reminder (date first, then mileage)
            result.sortedWith(
                compareBy<ExpenseReminder> { reminder ->
                    reminder.expense.reminderDate ?: Long.MAX_VALUE
                }.thenBy { reminder ->
                    reminder.remainingKm ?: Int.MAX_VALUE
                }
            )
        }
    }
}

