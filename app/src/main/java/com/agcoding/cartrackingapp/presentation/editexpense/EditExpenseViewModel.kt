package com.agcoding.cartrackingapp.presentation.editexpense

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agcoding.cartrackingapp.domain.model.Expense
import com.agcoding.cartrackingapp.domain.repository.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class EditExpenseUiState {
    object Loading : EditExpenseUiState()
    data class Success(val category: String) : EditExpenseUiState()
    data class Error(val message: String) : EditExpenseUiState()
}

@HiltViewModel
class EditExpenseViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val expenseId: Long = savedStateHandle.get<Long>("expenseId") ?: 0L

    private var currentExpense: Expense? = null

    private val _amount = MutableStateFlow("")
    val amount: StateFlow<String> = _amount.asStateFlow()

    private val _notes = MutableStateFlow("")
    val notes: StateFlow<String> = _notes.asStateFlow()

    private val _selectedDate = MutableStateFlow(System.currentTimeMillis())
    val selectedDate: StateFlow<Long> = _selectedDate.asStateFlow()

    private val _showDatePicker = MutableStateFlow(false)
    val showDatePicker: StateFlow<Boolean> = _showDatePicker.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    val uiState: StateFlow<EditExpenseUiState> = expenseRepository.getExpenseById(expenseId)
        .map { expense ->
            if (expense == null) {
                EditExpenseUiState.Error("Expense not found")
            } else {
                currentExpense = expense
                _amount.value = expense.amount.toString()
                _notes.value = expense.notes ?: ""
                _selectedDate.value = expense.timestamp
                EditExpenseUiState.Success(category = expense.category)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = EditExpenseUiState.Loading
        )

    fun updateAmount(newAmount: String) {
        // Only allow digits and one decimal point
        if (newAmount.isEmpty() || newAmount.matches(Regex("^\\d*\\.?\\d*$"))) {
            _amount.value = newAmount
        }
    }

    fun updateNotes(newNotes: String) {
        _notes.value = newNotes
    }

    fun updateDate(newDate: Long) {
        _selectedDate.value = newDate
    }

    fun showDatePicker() {
        _showDatePicker.value = true
    }

    fun hideDatePicker() {
        _showDatePicker.value = false
    }

    fun updateExpense(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val amountValue = amount.value.toDoubleOrNull()

        if (amountValue == null || amountValue <= 0) {
            onError("Please enter a valid amount")
            return
        }

        val expense = currentExpense
        if (expense == null) {
            onError("Expense not found")
            return
        }

        viewModelScope.launch {
            try {
                _isSaving.value = true

                val updatedExpense = expense.copy(
                    amount = amountValue,
                    timestamp = selectedDate.value,
                    notes = notes.value.ifBlank { null }
                )

                expenseRepository.updateExpense(updatedExpense)
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Failed to update expense")
            } finally {
                _isSaving.value = false
            }
        }
    }
}

