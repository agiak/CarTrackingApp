package com.agcoding.cartrackingapp.presentation.carlist

import com.agcoding.cartrackingapp.domain.model.Car
import com.agcoding.cartrackingapp.domain.usecase.expense.ReminderInfo

sealed class CarListUiState {
    object Loading : CarListUiState()
    object Empty : CarListUiState()
    data class Success(val cars: List<Car>, val reminderInfo: ReminderInfo?) : CarListUiState()
    data class Error(val message: String) : CarListUiState()
}