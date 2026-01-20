package com.agcoding.cartrackingapp.presentation.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agcoding.cartrackingapp.domain.repository.ExpenseRepository
import com.agcoding.cartrackingapp.domain.usecase.expense.ExpenseReminder
import com.agcoding.cartrackingapp.domain.usecase.expense.GetExpensesWithRemindersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class NotificationsUiState {
    object Loading : NotificationsUiState()
    object Empty : NotificationsUiState()
    data class Success(val reminders: List<ExpenseReminder>) : NotificationsUiState()
    data class Error(val message: String) : NotificationsUiState()
}

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val getExpensesWithRemindersUseCase: GetExpensesWithRemindersUseCase,
    private val expenseRepository: ExpenseRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<NotificationsUiState>(NotificationsUiState.Loading)
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    init {
        loadReminders()
    }

    private fun loadReminders() {
        viewModelScope.launch {
            _uiState.value = NotificationsUiState.Loading

            getExpensesWithRemindersUseCase()
                .catch { e ->
                    _uiState.value = NotificationsUiState.Error(
                        e.message ?: "Failed to load reminders"
                    )
                }
                .collect { reminders ->
                    _uiState.value = if (reminders.isEmpty()) {
                        NotificationsUiState.Empty
                    } else {
                        NotificationsUiState.Success(reminders)
                    }
                }
        }
    }

    fun toggleReminderEnabled(expenseId: Long, enabled: Boolean) {
        viewModelScope.launch {
            try {
                // Get the expense once, update reminderEnabled, and save
                val expense = expenseRepository.getExpenseById(expenseId).first()
                expense?.let {
                    expenseRepository.updateExpense(it.copy(reminderEnabled = enabled))
                }
            } catch (e: Exception) {
                // Handle error if needed
            }
        }
    }

    fun dismissReminder(expenseId: Long) {
        viewModelScope.launch {
            try {
                val expense = expenseRepository.getExpenseById(expenseId).first()
                expense?.let {
                    expenseRepository.updateExpense(it.copy(reminderDismissed = true))
                }
            } catch (e: Exception) {
                // Handle error if needed
            }
        }
    }

    fun retry() {
        loadReminders()
    }
}

