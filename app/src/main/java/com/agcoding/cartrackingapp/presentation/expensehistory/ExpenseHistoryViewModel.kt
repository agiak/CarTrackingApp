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
    object EmptyFilter : ExpenseHistoryUiState()
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

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    private val _startDate = MutableStateFlow<Long?>(null)
    val startDate: StateFlow<Long?> = _startDate.asStateFlow()

    private val _endDate = MutableStateFlow<Long?>(null)
    val endDate: StateFlow<Long?> = _endDate.asStateFlow()

    val uiState: StateFlow<ExpenseHistoryUiState> = combine(
        expenseRepository.getExpensesByCarId(carId),
        _sortOption,
        _selectedCategory,
        _startDate,
        _endDate
    ) { expenses, sortOption, selectedCategory, startDate, endDate ->
        val availableCategories = expenses.map { it.category }.distinct().sorted()
        val hasDateFilter = startDate != null || endDate != null

        var filtered = expenses
        if (selectedCategory != null) filtered = filtered.filter { it.category == selectedCategory }
        if (startDate != null) filtered = filtered.filter { it.timestamp >= startDate }
        if (endDate != null) {
            filtered = filtered.filter { it.timestamp <= endDate + 86_399_999L }
        }

        if (filtered.isEmpty() && hasDateFilter && expenses.isNotEmpty()) {
            return@combine ExpenseHistoryUiState.EmptyFilter
        }

        val sorted = when (sortOption) {
            ExpenseSortOption.MOST_RECENT -> filtered.sortedByDescending { it.timestamp }
            ExpenseSortOption.OLDEST -> filtered.sortedBy { it.timestamp }
            ExpenseSortOption.MOST_EXPENSIVE -> filtered.sortedByDescending { it.amount }
            ExpenseSortOption.CHEAPEST -> filtered.sortedBy { it.amount }
        }
        ExpenseHistoryUiState.Success(expenses = sorted, availableCategories = availableCategories)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ExpenseHistoryUiState.Loading
    )

    fun setSortOption(option: ExpenseSortOption) { _sortOption.value = option }
    fun setSelectedCategory(category: String?) { _selectedCategory.value = category }
    fun setStartDate(date: Long?) { _startDate.value = date }
    fun setEndDate(date: Long?) { _endDate.value = date }
    fun clearDateFilter() { _startDate.value = null; _endDate.value = null }
}

