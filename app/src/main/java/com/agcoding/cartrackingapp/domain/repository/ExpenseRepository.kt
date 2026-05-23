package com.agcoding.cartrackingapp.domain.repository

import com.agcoding.cartrackingapp.domain.model.Expense
import kotlinx.coroutines.flow.Flow

interface ExpenseRepository {
    fun getExpensesByCarId(carId: Long): Flow<List<Expense>>
    fun getExpenseById(expenseId: Long): Flow<Expense?>
    fun getAllExpenses(): Flow<List<Expense>>
    suspend fun insertExpense(expense: Expense): Long
    suspend fun updateExpense(expense: Expense)
    suspend fun deleteExpense(expenseId: Long)

    // Soft delete / trash methods
    suspend fun softDeleteExpense(expenseId: Long)
    suspend fun restoreExpense(expenseId: Long)
    suspend fun getDeletedExpenses(): List<Expense>
    suspend fun permanentlyDeleteExpense(expenseId: Long)
}

