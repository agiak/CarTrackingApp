package com.agcoding.cartrackingapp.presentation.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agcoding.cartrackingapp.domain.repository.CarRepository
import com.agcoding.cartrackingapp.domain.usecase.transaction.GetAllTransactionsUseCase
import com.agcoding.cartrackingapp.presentation.transactions.model.TransactionType
import com.agcoding.cartrackingapp.presentation.transactions.model.TransactionWithData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class TransactionFilter(
    val showRefills: Boolean = true,
    val showExpenses: Boolean = true,
    val selectedCarIds: Set<Long> = emptySet()
)

enum class SortOption {
    DATE_NEWEST,
    DATE_OLDEST,
    COST_HIGHEST,
    COST_LOWEST
}

@HiltViewModel
class TransactionsViewModel @Inject constructor(
    private val getAllTransactionsUseCase: GetAllTransactionsUseCase,
    private val carRepository: CarRepository
) : ViewModel() {

    private val _filter = MutableStateFlow(TransactionFilter())
    val filter: StateFlow<TransactionFilter> = _filter.asStateFlow()

    private val _sortOption = MutableStateFlow(SortOption.DATE_NEWEST)
    val sortOption: StateFlow<SortOption> = _sortOption.asStateFlow()

    // Get all cars for filter UI
    val cars = carRepository.getAllCars()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Get all transactions and apply filters and sorting
    val transactions: StateFlow<List<TransactionWithData>> = combine(
        getAllTransactionsUseCase(),
        _filter,
        _sortOption
    ) { allTransactions, filter, sortOption ->
        var filtered = allTransactions

        // Apply type filter
        if (!filter.showRefills || !filter.showExpenses) {
            filtered = filtered.filter { transactionData ->
                when (transactionData.transaction.type) {
                    TransactionType.REFILL -> filter.showRefills
                    TransactionType.EXPENSE -> filter.showExpenses
                }
            }
        }

        // Apply car filter
        if (filter.selectedCarIds.isNotEmpty()) {
            filtered = filtered.filter { it.transaction.carId in filter.selectedCarIds }
        }

        // Apply sorting
        when (sortOption) {
            SortOption.DATE_NEWEST -> filtered.sortedByDescending { it.transaction.timestamp }
            SortOption.DATE_OLDEST -> filtered.sortedBy { it.transaction.timestamp }
            SortOption.COST_HIGHEST -> filtered.sortedByDescending { it.transaction.amount }
            SortOption.COST_LOWEST -> filtered.sortedBy { it.transaction.amount }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun toggleRefillFilter() {
        _filter.value = _filter.value.copy(showRefills = !_filter.value.showRefills)
    }

    fun toggleExpenseFilter() {
        _filter.value = _filter.value.copy(showExpenses = !_filter.value.showExpenses)
    }

    fun toggleCarSelection(carId: Long) {
        val currentSelection = _filter.value.selectedCarIds
        _filter.value = _filter.value.copy(
            selectedCarIds = if (carId in currentSelection) {
                currentSelection - carId
            } else {
                currentSelection + carId
            }
        )
    }

    fun clearCarFilter() {
        _filter.value = _filter.value.copy(selectedCarIds = emptySet())
    }

    fun clearAllFilters() {
        _filter.value = TransactionFilter()
    }

    fun hasActiveFilters(): Boolean {
        val current = _filter.value
        return !current.showRefills || !current.showExpenses || current.selectedCarIds.isNotEmpty()
    }

    fun setSortOption(option: SortOption) {
        _sortOption.value = option
    }
}
