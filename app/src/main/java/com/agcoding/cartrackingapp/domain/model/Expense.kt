package com.agcoding.cartrackingapp.domain.model

import androidx.annotation.StringRes
import com.agcoding.cartrackingapp.R

/**
 * Predefined expense categories for suggestions.
 * Users can also create custom categories.
 */
object ExpenseCategories {
    @StringRes
    val predefinedResIds = listOf(
        R.string.expense_category_tire_change,
        R.string.expense_category_oil_change,
        R.string.expense_category_small_service,
        R.string.expense_category_big_service,
        R.string.expense_category_repairs,
        R.string.expense_category_accessories,
        R.string.expense_category_insurance,
        R.string.expense_category_registration,
        R.string.expense_category_parking,
        R.string.expense_category_toll,
        R.string.expense_category_car_wash,
        R.string.expense_category_other
    )

    // Keep for backward compatibility with existing code
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
    val notes: String? = null,
    // Service reminder fields (optional)
    val reminderDate: Long? = null, // Future date for service reminder
    val reminderMileage: Int? = null, // Mileage value for service reminder
    val reminderEnabled: Boolean = true // Whether notifications are enabled for this reminder
)

