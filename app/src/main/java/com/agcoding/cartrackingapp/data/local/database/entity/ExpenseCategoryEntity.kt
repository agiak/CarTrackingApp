package com.agcoding.cartrackingapp.data.local.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "expense_categories",
    indices = [Index("name", unique = true)]
)
data class ExpenseCategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val isCustom: Boolean, // true for user-created, false for predefined
    val createdAt: Long = System.currentTimeMillis()
)

