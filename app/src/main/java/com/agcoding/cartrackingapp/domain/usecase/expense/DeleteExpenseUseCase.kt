package com.agcoding.cartrackingapp.domain.usecase.expense

import com.agcoding.cartrackingapp.domain.repository.ExpenseRepository
import com.agcoding.cartrackingapp.shared.domain.error.AppError
import com.agcoding.cartrackingapp.shared.domain.result.Result
import javax.inject.Inject

class DeleteExpenseUseCase @Inject constructor(
    private val expenseRepository: ExpenseRepository,
) {
    suspend operator fun invoke(expenseId: Long): Result<Unit> = try {
        expenseRepository.softDeleteExpense(expenseId)
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(AppError.DatabaseError(e))
    }
}
