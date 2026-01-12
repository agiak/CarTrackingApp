package com.agcoding.cartrackingapp.domain.model

/**
 * Predefined expense categories for suggestions.
 * Users can also create custom categories.
 */
object ExpenseCategories {
    val predefined = listOf(
        "Tire change",
        "Oil change",
        "Small service",
        "Big service",
        "Repairs",
        "Accessories",
        "Insurance",
        "Registration",
        "Parking",
        "Toll",
        "Car wash",
        "Other"
    )
}

data class Expense(
    val id: Long = 0,
    val carId: Long,
    val category: String, // Flexible user-defined category
    val amount: Double,
    val timestamp: Long,
    val notes: String? = null
)

