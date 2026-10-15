package com.agcoding.cartrackingapp.presentation.editexpense

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agcoding.cartrackingapp.domain.model.Expense
import com.agcoding.cartrackingapp.domain.repository.CarRepository
import com.agcoding.cartrackingapp.domain.repository.ExpenseRepository
import com.agcoding.cartrackingapp.util.parseLocalizedDouble
import com.agcoding.cartrackingapp.util.sanitizeDecimalInput
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class EditExpenseUiState {
    object Loading : EditExpenseUiState()

    /**
     * The category cannot be edited here — changing it would move the expense
     * between the service and other totals, which is a different operation
     * from correcting an amount — so it is shown rather than offered, with the
     * car it belongs to beside it.
     */
    data class Success(
        val category: String,
        val carName: String = "",
        val carPlate: String = ""
    ) : EditExpenseUiState()

    data class Error(val message: String) : EditExpenseUiState()
}

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class EditExpenseViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    carRepository: CarRepository,
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

    private val _isDirty = MutableStateFlow(false)
    val isDirty: StateFlow<Boolean> = _isDirty.asStateFlow()

    /**
     * Whether anything differs from the expense as it was loaded.
     *
     * Compared against the loaded values rather than tracked with per-field
     * flags, so typing a change and then typing it back correctly leaves Save
     * quiet again.
     */
    private fun markDirty() {
        val expense = currentExpense ?: return
        _isDirty.value = _amount.value != expense.amount.toString().replace('.', ',') ||
            _notes.value != (expense.notes ?: "") ||
            _selectedDate.value != expense.timestamp
    }

    /** Puts the form back the way it was loaded. */
    fun discardChanges() {
        val expense = currentExpense ?: return
        _amount.value = expense.amount.toString().replace('.', ',')
        _notes.value = expense.notes ?: ""
        _selectedDate.value = expense.timestamp
        _isDirty.value = false
    }

    val uiState: StateFlow<EditExpenseUiState> = expenseRepository.getExpenseById(expenseId)
        .flatMapLatest { expense ->
            if (expense == null) {
                flowOf(EditExpenseUiState.Error("Expense not found"))
            } else {
                // Only seed the fields the first time; a later emission (the
                // save writing back) must not overwrite what is being typed.
                if (currentExpense?.id != expense.id) {
                    _amount.value = expense.amount.toString().replace('.', ',')
                    _notes.value = expense.notes ?: ""
                    _selectedDate.value = expense.timestamp
                    _isDirty.value = false
                }
                currentExpense = expense
                carRepository.getCarById(expense.carId).map { car ->
                    EditExpenseUiState.Success(
                        category = expense.category,
                        carName = car?.name.orEmpty(),
                        carPlate = car?.licensePlate.orEmpty()
                    )
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = EditExpenseUiState.Loading
        )

    fun updateAmount(newAmount: String) {
        // Keep only digits and a single ',' decimal separator (grouping is visual only).
        _amount.value = sanitizeDecimalInput(newAmount)
        markDirty()
    }

    fun updateNotes(newNotes: String) {
        _notes.value = newNotes
        markDirty()
    }

    fun updateDate(newDate: Long) {
        _selectedDate.value = newDate
        markDirty()
    }

    fun showDatePicker() {
        _showDatePicker.value = true
    }

    fun hideDatePicker() {
        _showDatePicker.value = false
    }

    fun updateExpense(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val amountValue = amount.value.parseLocalizedDouble()

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
                currentExpense = updatedExpense
                _isDirty.value = false
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Failed to update expense")
            } finally {
                _isSaving.value = false
            }
        }
    }
}

