package com.agcoding.cartrackingapp.data.mapper

import com.agcoding.cartrackingapp.data.local.database.entity.ExpenseEntity
import com.agcoding.cartrackingapp.domain.model.Expense

fun ExpenseEntity.toDomain(): Expense {
    return Expense(
        id = id,
        carId = carId,
        category = category,
        amount = amount,
        timestamp = timestamp,
        notes = notes,
        reminderDate = reminderDate,
        reminderMileage = reminderMileage,
        reminderEnabled = reminderEnabled,
        preExpiryNotificationSent = preExpiryNotificationSent,
        reminderDismissed = reminderDismissed
    )
}

fun Expense.toEntity(): ExpenseEntity {
    return ExpenseEntity(
        id = id,
        carId = carId,
        category = category,
        amount = amount,
        timestamp = timestamp,
        notes = notes,
        reminderDate = reminderDate,
        reminderMileage = reminderMileage,
        reminderEnabled = reminderEnabled,
        preExpiryNotificationSent = preExpiryNotificationSent,
        reminderDismissed = reminderDismissed
    )
}

