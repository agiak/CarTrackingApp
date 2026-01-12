package com.agcoding.cartrackingapp.data.repository

import com.agcoding.cartrackingapp.data.local.database.dao.ExpenseDao
import com.agcoding.cartrackingapp.data.mapper.toDomain
import com.agcoding.cartrackingapp.data.mapper.toEntity
import com.agcoding.cartrackingapp.domain.model.Expense
import com.agcoding.cartrackingapp.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ExpenseRepositoryImpl @Inject constructor(
    private val expenseDao: ExpenseDao
) : ExpenseRepository {

    override fun getExpensesByCarId(carId: Long): Flow<List<Expense>> {
        return expenseDao.getExpensesByCarId(carId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getExpenseById(expenseId: Long): Flow<Expense?> {
        return expenseDao.getExpenseById(expenseId).map { it?.toDomain() }
    }

    override fun getAllExpenses(): Flow<List<Expense>> {
        return expenseDao.getAllExpenses().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertExpense(expense: Expense): Long {
        return expenseDao.insertExpense(expense.toEntity())
    }

    override suspend fun updateExpense(expense: Expense) {
        expenseDao.updateExpense(expense.toEntity())
    }

    override suspend fun deleteExpense(expenseId: Long) {
        expenseDao.deleteExpenseById(expenseId)
    }
}

