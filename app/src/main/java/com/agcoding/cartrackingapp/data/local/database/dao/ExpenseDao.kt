package com.agcoding.cartrackingapp.data.local.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.agcoding.cartrackingapp.data.local.database.entity.ExpenseEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {

    @Query("SELECT * FROM expenses WHERE carId = :carId AND deletedAt IS NULL ORDER BY timestamp DESC")
    fun getExpensesByCarId(carId: Long): Flow<List<ExpenseEntity>>

    @Query("SELECT * FROM expenses WHERE id = :expenseId AND deletedAt IS NULL")
    fun getExpenseById(expenseId: Long): Flow<ExpenseEntity?>

    @Query("SELECT * FROM expenses WHERE deletedAt IS NULL ORDER BY timestamp DESC")
    fun getAllExpenses(): Flow<List<ExpenseEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: ExpenseEntity): Long

    @Update
    suspend fun updateExpense(expense: ExpenseEntity)

    @Delete
    suspend fun deleteExpense(expense: ExpenseEntity)

    @Query("DELETE FROM expenses WHERE id = :expenseId")
    suspend fun deleteExpenseById(expenseId: Long)

    // Soft delete / trash methods
    @Query("UPDATE expenses SET deletedAt = :timestamp WHERE id = :expenseId")
    suspend fun softDeleteExpense(expenseId: Long, timestamp: Long)

    @Query("UPDATE expenses SET deletedAt = NULL WHERE id = :expenseId")
    suspend fun restoreExpense(expenseId: Long)

    @Query("SELECT * FROM expenses WHERE deletedAt IS NOT NULL ORDER BY deletedAt DESC")
    suspend fun getDeletedExpenses(): List<ExpenseEntity>

    @Query("DELETE FROM expenses WHERE id = :expenseId AND deletedAt IS NOT NULL")
    suspend fun permanentlyDeleteExpense(expenseId: Long)
}

