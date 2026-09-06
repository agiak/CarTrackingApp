package com.agcoding.cartrackingapp.presentation.periodcomparison

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agcoding.cartrackingapp.domain.model.Car
import com.agcoding.cartrackingapp.domain.model.DateFilter
import com.agcoding.cartrackingapp.domain.model.PeriodComparison
import com.agcoding.cartrackingapp.domain.model.monthlyTotals
import com.agcoding.cartrackingapp.domain.model.periodComparison
import com.agcoding.cartrackingapp.domain.repository.CarRepository
import com.agcoding.cartrackingapp.domain.usecase.transaction.GetAllTransactionsUseCase
import com.agcoding.cartrackingapp.presentation.transactions.model.TransactionWithData
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Total cost per calendar month for both periods, ready to be drawn as a table. */
data class MonthlyBreakdown(
    val primary: Map<Int, Double> = emptyMap(),
    val secondary: Map<Int, Double> = emptyMap()
) {
    val hasData: Boolean get() = primary.isNotEmpty() || secondary.isNotEmpty()
}

/**
 * Two periods compared across the whole fleet, or across whichever cars the user picked.
 *
 * Same comparison as the per-car screen — the identical [DateFilter] periods and the
 * identical metrics — with a car filter on top: no selection means every car, and any
 * number of cars can be selected, including just one.
 */
@HiltViewModel
class FleetComparisonViewModel @Inject constructor(
    private val getAllTransactionsUseCase: GetAllTransactionsUseCase,
    carRepository: CarRepository
) : ViewModel() {

    private val _primaryFilter = MutableStateFlow(DateFilter.None)
    val primaryFilter: StateFlow<DateFilter> = _primaryFilter.asStateFlow()

    private val _secondaryFilter = MutableStateFlow(DateFilter.None)
    val secondaryFilter: StateFlow<DateFilter> = _secondaryFilter.asStateFlow()

    /** Empty means every car — the same convention the statistics screen already uses. */
    private val _selectedCarIds = MutableStateFlow<Set<Long>>(emptySet())
    val selectedCarIds: StateFlow<Set<Long>> = _selectedCarIds.asStateFlow()

    val cars: StateFlow<List<Car>> = carRepository.getAllCars()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val allTransactions: StateFlow<List<TransactionWithData>> =
        getAllTransactionsUseCase()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Everything belonging to the selected cars, before any date filtering. */
    private val scopedTransactions: StateFlow<List<TransactionWithData>> =
        combine(allTransactions, _selectedCarIds) { transactions, carIds ->
            if (carIds.isEmpty()) transactions
            else transactions.filter { it.transaction.carId in carIds }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /**
     * Years offered by the pickers. Derived from the selected cars rather than the whole
     * fleet, so narrowing to one car never leaves a year on offer that has no records.
     */
    val availableYears: StateFlow<List<Int>> = scopedTransactions
        .map { transactions -> DateFilter.availableYears(transactions.map { it.transaction.timestamp }) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val comparison: StateFlow<PeriodComparison> =
        combine(scopedTransactions, _primaryFilter, _secondaryFilter) { transactions, primary, secondary ->
            transactions.periodComparison(primary, secondary)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PeriodComparison())

    val monthlyBreakdown: StateFlow<MonthlyBreakdown> =
        combine(scopedTransactions, _primaryFilter, _secondaryFilter) { transactions, primary, secondary ->
            MonthlyBreakdown(
                primary = transactions.monthlyTotals(primary),
                secondary = transactions.monthlyTotals(secondary)
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MonthlyBreakdown())

    init {
        seedDefaultPeriods()
    }

    /** Opens on the two most recent years that have records across the whole fleet. */
    private fun seedDefaultPeriods() {
        viewModelScope.launch {
            val years = DateFilter.availableYears(
                getAllTransactionsUseCase().first().map { it.transaction.timestamp }
            )
            if (_primaryFilter.value.isActive || _secondaryFilter.value.isActive) return@launch

            years.firstOrNull()?.let { _primaryFilter.value = DateFilter.of(it) }
            years.getOrNull(1)?.let { _secondaryFilter.value = DateFilter.of(it) }
        }
    }

    fun setPrimaryFilter(dateFilter: DateFilter) {
        _primaryFilter.value = dateFilter.normalized
    }

    fun setSecondaryFilter(dateFilter: DateFilter) {
        _secondaryFilter.value = dateFilter.normalized
    }

    /** Flips the two periods, which flips the sign of every delta. */
    fun swapPeriods() {
        val primary = _primaryFilter.value
        _primaryFilter.value = _secondaryFilter.value
        _secondaryFilter.value = primary
    }

    /**
     * Adds or removes one car from the selection. The chosen periods are kept: they stay
     * valid choices, and losing them on every tap would make comparing cars tedious.
     */
    fun toggleCar(carId: Long) {
        val current = _selectedCarIds.value
        _selectedCarIds.value = if (carId in current) current - carId else current + carId
    }

    /** Back to every car. */
    fun clearCarFilter() {
        _selectedCarIds.value = emptySet()
    }
}
