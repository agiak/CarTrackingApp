package com.agcoding.cartrackingapp.presentation.notifications

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agcoding.cartrackingapp.domain.model.NotificationHistoryItem
import com.agcoding.cartrackingapp.domain.repository.ExpenseRepository
import com.agcoding.cartrackingapp.domain.repository.NotificationHistoryRepository
import com.agcoding.cartrackingapp.domain.usecase.expense.ExpenseReminder
import com.agcoding.cartrackingapp.domain.usecase.expense.GetExpensesWithRemindersUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * The screen holds two independent lists, which is why this is a data class
 * and not a sealed hierarchy any more: "no reminders, but three announcements
 * arrived this week" is a perfectly ordinary state, and a single `Empty`
 * branch could not express it without hiding one list or the other.
 */
data class NotificationsUiState(
    val isLoading: Boolean = true,
    val reminders: List<ExpenseReminder> = emptyList(),
    val received: List<NotificationHistoryItem> = emptyList(),
    val error: String? = null
) {
    val isEmpty: Boolean get() = reminders.isEmpty() && received.isEmpty()
}

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val getExpensesWithRemindersUseCase: GetExpensesWithRemindersUseCase,
    private val expenseRepository: ExpenseRepository,
    private val notificationHistoryRepository: NotificationHistoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationsUiState())
    val uiState: StateFlow<NotificationsUiState> = _uiState.asStateFlow()

    private var observeJob: Job? = null

    init {
        observe()
    }

    private fun observe() {
        // Cancelled first, so a retry replaces the previous collector instead
        // of running beside it and racing it to the same state.
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            combine(
                getExpensesWithRemindersUseCase(),
                notificationHistoryRepository.getRecentNotifications(RECENT_LIMIT)
            ) { reminders, received -> reminders to received }
                .catch { e ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = e.message ?: "Failed to load notifications"
                        )
                    }
                }
                .collect { (reminders, received) ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            reminders = reminders,
                            received = received,
                            error = null
                        )
                    }
                }
        }
    }

    fun toggleReminderEnabled(expenseId: Long, enabled: Boolean) {
        viewModelScope.launch {
            try {
                Log.d("NotificationsViewModel", "Toggle reminder $expenseId to enabled=$enabled")
                // Get the expense once, update reminderEnabled, and save
                val expense = expenseRepository.getExpenseById(expenseId).first()
                expense?.let {
                    Log.d("NotificationsViewModel", "Current state: reminderEnabled=${it.reminderEnabled}")
                    expenseRepository.updateExpense(it.copy(reminderEnabled = enabled))
                    Log.d("NotificationsViewModel", "Updated expense $expenseId to enabled=$enabled")
                }
            } catch (e: Exception) {
                Log.e("NotificationsViewModel", "Error toggling reminder", e)
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
        observe()
    }

    private companion object {
        /**
         * A preview, not the archive. The full list lives behind "see all",
         * which is also where it can be cleared.
         */
        const val RECENT_LIMIT = 10
    }
}
