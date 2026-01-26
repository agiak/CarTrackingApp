package com.agcoding.cartrackingapp.presentation.transactions.model

import com.agcoding.cartrackingapp.domain.model.Expense
import com.agcoding.cartrackingapp.domain.model.FuelRefill

/**
 * UI model representing a transaction (either a refill or an expense)
 * Used to display unified transaction list
 */
data class Transaction(
    val id: Long,
    val carId: Long,
    val carName: String,
    val type: TransactionType,
    val amount: Double,
    val timestamp: Long,
    val description: String? = null
)

enum class TransactionType {
    REFILL,
    EXPENSE
}

/**
 * Wrapper that holds both the transaction summary and the full domain model
 * This allows us to reuse existing RefillItemCard and ExpenseItemCard components
 */
sealed class TransactionWithData {
    abstract val transaction: Transaction

    data class RefillTransaction(
        override val transaction: Transaction,
        val refill: FuelRefill,
        val carName: String
    ) : TransactionWithData()

    data class ExpenseTransaction(
        override val transaction: Transaction,
        val expense: Expense,
        val carName: String
    ) : TransactionWithData()
}

