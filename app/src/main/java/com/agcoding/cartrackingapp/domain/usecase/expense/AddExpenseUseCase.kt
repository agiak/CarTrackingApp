package com.agcoding.cartrackingapp.domain.usecase.expense

import android.content.Context
import com.agcoding.cartrackingapp.domain.model.Expense
import com.agcoding.cartrackingapp.domain.repository.ExpenseRepository
import com.agcoding.cartrackingapp.widget.QuickAddWidgetReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Use case for adding an expense with validation and widget updates
 * Ensures consistency across the app (main screens and widget quick entry)
 */
class AddExpenseUseCase @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    @ApplicationContext private val context: Context
) {
    suspend operator fun invoke(
        carId: Long,
        category: String,
        amount: Double,
        timestamp: Long = System.currentTimeMillis(),
        notes: String? = null
    ): Result<Long> {
        return try {
            // Validation
            if (amount <= 0) {
                return Result.failure(IllegalArgumentException("Amount must be positive"))
            }
            if (category.isBlank()) {
                return Result.failure(IllegalArgumentException("Category is required"))
            }

            // Create expense
            val expense = Expense(
                carId = carId,
                category = category,
                amount = amount,
                timestamp = timestamp,
                notes = notes?.trim()
            )

            // Insert expense
            val expenseId = expenseRepository.insertExpense(expense)

            // Update widgets to show latest transaction
            QuickAddWidgetReceiver.updateWidgets(context)

            Result.success(expenseId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

