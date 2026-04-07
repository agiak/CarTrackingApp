package com.agcoding.cartrackingapp.presentation.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agcoding.cartrackingapp.domain.model.NotificationHistoryItem
import com.agcoding.cartrackingapp.domain.repository.NotificationHistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class NotificationHistoryUiState {
    object Loading : NotificationHistoryUiState()
    object Empty : NotificationHistoryUiState()
    data class Success(val notifications: List<NotificationHistoryItem>) : NotificationHistoryUiState()
    data class Error(val message: String) : NotificationHistoryUiState()
}

@HiltViewModel
class NotificationHistoryViewModel @Inject constructor(
    private val notificationHistoryRepository: NotificationHistoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<NotificationHistoryUiState>(NotificationHistoryUiState.Loading)
    val uiState: StateFlow<NotificationHistoryUiState> = _uiState.asStateFlow()

    init {
        loadHistory()
    }

    private fun loadHistory() {
        viewModelScope.launch {
            _uiState.value = NotificationHistoryUiState.Loading

            notificationHistoryRepository.getAllNotifications()
                .catch { e ->
                    _uiState.value = NotificationHistoryUiState.Error(
                        e.message ?: "Failed to load notification history"
                    )
                }
                .collect { notifications ->
                    _uiState.value = if (notifications.isEmpty()) {
                        NotificationHistoryUiState.Empty
                    } else {
                        NotificationHistoryUiState.Success(notifications)
                    }
                }
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            notificationHistoryRepository.deleteAll()
        }
    }

    fun retry() {
        loadHistory()
    }
}

