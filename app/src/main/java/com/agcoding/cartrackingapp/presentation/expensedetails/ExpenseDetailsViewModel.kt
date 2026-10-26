package com.agcoding.cartrackingapp.presentation.expensedetails

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agcoding.cartrackingapp.domain.model.Expense
import com.agcoding.cartrackingapp.domain.repository.CarRepository
import com.agcoding.cartrackingapp.domain.repository.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
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
import timber.log.Timber

sealed class ExpenseDetailsUiState {
    object Loading : ExpenseDetailsUiState()
    data class Success(
        val expense: Expense,
        val carName: String,
        /** Shown beside the name on the details row, so two cars of the same model are told apart. */
        val carPlate: String? = null
    ) : ExpenseDetailsUiState()
    data class Error(val message: String) : ExpenseDetailsUiState()
}

@HiltViewModel
class ExpenseDetailsViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val carRepository: CarRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val expenseId: Long = savedStateHandle.get<Long>("expenseId") ?: 0L

    private val _showDeleteDialog = MutableStateFlow(false)
    val showDeleteDialog: StateFlow<Boolean> = _showDeleteDialog.asStateFlow()

    val uiState: StateFlow<ExpenseDetailsUiState> = expenseRepository.getExpenseById(expenseId)
        .flatMapLatest { expense ->
            if (expense == null) {
                flowOf(ExpenseDetailsUiState.Error("Expense not found"))
            } else {
                carRepository.getCarById(expense.carId).map { car ->
                    if (car != null) {
                        ExpenseDetailsUiState.Success(
                            expense = expense,
                            carName = car.name,
                            carPlate = car.licensePlate.takeIf { it.isNotBlank() }
                        )
                    } else {
                        ExpenseDetailsUiState.Error("Car not found")
                    }
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ExpenseDetailsUiState.Loading
        )

    fun showDeleteDialog() {
        _showDeleteDialog.value = true
    }

    fun hideDeleteDialog() {
        _showDeleteDialog.value = false
    }

    /** Undoes the delete; the details flow repopulates by itself. */
    fun restoreExpense() {
        viewModelScope.launch {
            runCatching { expenseRepository.restoreExpense(expenseId) }
                .onFailure { Timber.e(it, "Could not restore expense %d", expenseId) }
        }
    }

    /**
     * Deletes the expense, and says so either way.
     *
     * The failure used to be swallowed by an empty catch, which left the user
     * looking at the row they had just confirmed removing with nothing to tell
     * them the write had not happened.
     */
    fun deleteExpense(onSuccess: () -> Unit, onError: () -> Unit) {
        viewModelScope.launch {
            try {
                expenseRepository.softDeleteExpense(expenseId)
                hideDeleteDialog()
                onSuccess()
            } catch (e: Exception) {
                Timber.e(e, "Could not delete expense %d", expenseId)
                hideDeleteDialog()
                onError()
            }
        }
    }
}

