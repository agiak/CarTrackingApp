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
 * Refills and expenses for a single car, merged into one chronological list.
 *
 * The car details screen and its "see all" screen show one unified list rather than
 * two separate ones, and each entry keeps its full domain model so the existing
 * refill/expense cards can render it unchanged.
 */
class GetCarTransactionsUseCase @Inject constructor(
    private val refillRepository: RefillRepository,
    private val expenseRepository: ExpenseRepository,
    private val carRepository: CarRepository
) {
    operator fun invoke(carId: Long): Flow<List<TransactionWithData>> {
        return combine(
            refillRepository.getRefillsByCarId(carId),
            expenseRepository.getExpensesByCarId(carId),
            carRepository.getCarById(carId)
        ) { refills, expenses, car ->
            val carName = car?.name.orEmpty()

            val refillTransactions = refills.map { refill ->
                TransactionWithData.RefillTransaction(
                    transaction = refill.toTransaction(carName),
                    refill = refill,
                    carName = carName
                )
            }

            val expenseTransactions = expenses.map { expense ->
                TransactionWithData.ExpenseTransaction(
                    transaction = expense.toTransaction(carName),
                    expense = expense,
                    carName = carName
                )
            }

            (refillTransactions + expenseTransactions)
                .sortedByDescending { it.transaction.timestamp }
        }
    }

    private fun FuelRefill.toTransaction(carName: String) = Transaction(
        id = id,
        carId = carId,
        carName = carName,
        type = TransactionType.REFILL,
        amount = amountPaid,
        timestamp = timestamp,
        description = "${litersAdded}L"
    )

    private fun Expense.toTransaction(carName: String) = Transaction(
        id = id,
        carId = carId,
        carName = carName,
        type = TransactionType.EXPENSE,
        amount = amount,
        timestamp = timestamp,
        description = category
    )
}
