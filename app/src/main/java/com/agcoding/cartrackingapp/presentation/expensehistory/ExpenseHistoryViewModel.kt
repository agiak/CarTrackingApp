package com.agcoding.cartrackingapp.presentation.expensehistory

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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

sealed class ExpenseHistoryUiState {
    object Loading : ExpenseHistoryUiState()
    data class Success(
        val expenses: List<Expense>,
        val availableCategories: List<String>
    ) : ExpenseHistoryUiState()
    data class Error(val message: String) : ExpenseHistoryUiState()
}

enum class ExpenseSortOption(val displayNameResId: Int) {
    MOST_RECENT(com.agcoding.cartrackingapp.R.string.expense_sort_most_recent),
    OLDEST(com.agcoding.cartrackingapp.R.string.expense_sort_oldest),
    MOST_EXPENSIVE(com.agcoding.cartrackingapp.R.string.expense_sort_most_expensive),
    CHEAPEST(com.agcoding.cartrackingapp.R.string.expense_sort_cheapest)
}

@HiltViewModel
class ExpenseHistoryViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val carId: Long = savedStateHandle.get<Long>("carId") ?: 0L

    private val _sortOption = MutableStateFlow(ExpenseSortOption.MOST_RECENT)
    val sortOption: StateFlow<ExpenseSortOption> = _sortOption.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null) // null means "All"
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    val uiState: StateFlow<ExpenseHistoryUiState> = combine(
        expenseRepository.getExpensesByCarId(carId),
        _sortOption,
        _selectedCategory
    ) { expenses, sortOption, selectedCategory ->
        // Get all unique categories from expenses
        val availableCategories = expenses.map { it.category }.distinct().sorted()

        // Filter by category if one is selected
        val filteredExpenses = if (selectedCategory != null) {
            expenses.filter { it.category == selectedCategory }
        } else {
            expenses
        }

        // Sort the filtered expenses
        val sortedExpenses = when (sortOption) {
            ExpenseSortOption.MOST_RECENT -> filteredExpenses.sortedByDescending { it.timestamp }
            ExpenseSortOption.OLDEST -> filteredExpenses.sortedBy { it.timestamp }
            ExpenseSortOption.MOST_EXPENSIVE -> filteredExpenses.sortedByDescending { it.amount }
            ExpenseSortOption.CHEAPEST -> filteredExpenses.sortedBy { it.amount }
        }
        ExpenseHistoryUiState.Success(
            expenses = sortedExpenses,
            availableCategories = availableCategories
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ExpenseHistoryUiState.Loading
    )

    fun setSortOption(option: ExpenseSortOption) {
        _sortOption.value = option
    }

    fun setSelectedCategory(category: String?) {
        _selectedCategory.value = category
    }
}

