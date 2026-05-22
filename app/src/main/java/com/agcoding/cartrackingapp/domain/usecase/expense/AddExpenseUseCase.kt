package com.agcoding.cartrackingapp.domain.usecase.expense

import com.agcoding.cartrackingapp.domain.model.Expense
import com.agcoding.cartrackingapp.domain.repository.ExpenseRepository
import com.agcoding.cartrackingapp.shared.domain.error.AppError
import com.agcoding.cartrackingapp.shared.domain.result.Result
import javax.inject.Inject

class AddExpenseUseCase @Inject constructor(
    private val expenseRepository: ExpenseRepository,
) {
    suspend operator fun invoke(
        carId: Long,
        category: String,
        amount: Double,
        timestamp: Long = System.currentTimeMillis(),
        notes: String? = null,
    ): Result<Long> = try {
        val expenseId = expenseRepository.insertExpense(
            Expense(
                carId = carId,
                category = category,
                amount = amount,
                timestamp = timestamp,
                notes = notes?.trim(),
            )
        )
        Result.Success(expenseId)
    } catch (e: Exception) {
        Result.Error(AppError.DatabaseError(e))
    }
}
