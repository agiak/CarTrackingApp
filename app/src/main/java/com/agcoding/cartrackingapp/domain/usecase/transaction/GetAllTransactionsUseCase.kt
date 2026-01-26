package com.agcoding.cartrackingapp.domain.usecase.transaction

import com.agcoding.cartrackingapp.domain.model.Expense
import com.agcoding.cartrackingapp.domain.model.FuelRefill
import com.agcoding.cartrackingapp.domain.repository.CarRepository
import com.agcoding.cartrackingapp.domain.repository.ExpenseRepository
import com.agcoding.cartrackingapp.domain.repository.RefillRepository
import com.agcoding.cartrackingapp.presentation.transactions.model.Transaction
import com.agcoding.cartrackingapp.presentation.transactions.model.TransactionType
import com.agcoding.cartrackingapp.presentation.transactions.model.TransactionWithData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

/**
 * Use case to retrieve all transactions (refills and expenses) across all cars
 */
class GetAllTransactionsUseCase @Inject constructor(
    private val refillRepository: RefillRepository,
    private val expenseRepository: ExpenseRepository,
    private val carRepository: CarRepository
) {
    operator fun invoke(): Flow<List<TransactionWithData>> {
        return combine(
            refillRepository.getAllRefills(),
            expenseRepository.getAllExpenses(),
            carRepository.getAllCars()
        ) { refills, expenses, cars ->
            val carMap = cars.associateBy { it.id }

            val refillTransactions = refills.map { refill ->
                TransactionWithData.RefillTransaction(
                    transaction = mapRefillToTransaction(refill, carMap[refill.carId]?.name ?: "Unknown"),
                    refill = refill,
                    carName = carMap[refill.carId]?.name ?: "Unknown"
                )
            }

            val expenseTransactions = expenses.map { expense ->
                TransactionWithData.ExpenseTransaction(
                    transaction = mapExpenseToTransaction(expense, carMap[expense.carId]?.name ?: "Unknown"),
                    expense = expense,
                    carName = carMap[expense.carId]?.name ?: "Unknown"
                )
            }

            (refillTransactions + expenseTransactions).sortedByDescending { it.transaction.timestamp }
        }
    }

    private fun mapRefillToTransaction(refill: FuelRefill, carName: String): Transaction {
        return Transaction(
            id = refill.id,
            carId = refill.carId,
            carName = carName,
            type = TransactionType.REFILL,
            amount = refill.amountPaid,
            timestamp = refill.timestamp,
            description = "${refill.litersAdded}L"
        )
    }

    private fun mapExpenseToTransaction(expense: Expense, carName: String): Transaction {
        return Transaction(
            id = expense.id,
            carId = expense.carId,
            carName = carName,
            type = TransactionType.EXPENSE,
            amount = expense.amount,
            timestamp = expense.timestamp,
            description = expense.category
        )
    }
}
