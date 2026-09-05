package com.agcoding.cartrackingapp.presentation.cartransactions

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agcoding.cartrackingapp.domain.model.DateFilter
import com.agcoding.cartrackingapp.domain.model.PeriodStatistics
import com.agcoding.cartrackingapp.domain.model.periodStatistics
import com.agcoding.cartrackingapp.domain.repository.CarRepository
import com.agcoding.cartrackingapp.domain.usecase.transaction.GetCarTransactionsUseCase
import com.agcoding.cartrackingapp.presentation.transactions.SortOption
import com.agcoding.cartrackingapp.presentation.transactions.TransactionListFilter
import com.agcoding.cartrackingapp.presentation.transactions.applyFilter
import com.agcoding.cartrackingapp.presentation.transactions.model.TransactionWithData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

/**
 * Full transaction history for one car — the "See all" destination from car details.
 *
 * Offers the same type/sort controls and the same shared date filter as the details
 * screen, so moving between the two never changes how filtering works.
 */
@HiltViewModel
class CarTransactionsViewModel @Inject constructor(
    getCarTransactionsUseCase: GetCarTransactionsUseCase,
    carRepository: CarRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val carId: Long = savedStateHandle.get<Long>("carId") ?: 0L

    private val _listFilter = MutableStateFlow(TransactionListFilter())
    val listFilter: StateFlow<TransactionListFilter> = _listFilter.asStateFlow()

    val carName: StateFlow<String> = carRepository.getCarById(carId)
        .map { it?.name.orEmpty() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")

    private val allTransactions: StateFlow<List<TransactionWithData>> =
        getCarTransactionsUseCase(carId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val availableYears: StateFlow<List<Int>> = allTransactions
        .map { transactions -> DateFilter.availableYears(transactions.map { it.transaction.timestamp }) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val transactions: StateFlow<List<TransactionWithData>> =
        combine(allTransactions, _listFilter) { transactions, filter ->
            transactions.applyFilter(filter)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Totals for the chosen period, independent of the type filter — see car details. */
    val periodStatistics: StateFlow<PeriodStatistics> =
        combine(allTransactions, _listFilter) { transactions, filter ->
            transactions.periodStatistics(filter.dateFilter)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PeriodStatistics())

    fun toggleRefillFilter() {
        _listFilter.value = _listFilter.value.let { it.copy(showRefills = !it.showRefills) }
    }

    fun toggleExpenseFilter() {
        _listFilter.value = _listFilter.value.let { it.copy(showExpenses = !it.showExpenses) }
    }

    fun setSortOption(option: SortOption) {
        _listFilter.value = _listFilter.value.copy(sortOption = option)
    }

    fun setDateFilter(dateFilter: DateFilter) {
        _listFilter.value = _listFilter.value.copy(dateFilter = dateFilter.normalized)
    }

    fun clearFilters() {
        _listFilter.value = TransactionListFilter(sortOption = _listFilter.value.sortOption)
    }
}
