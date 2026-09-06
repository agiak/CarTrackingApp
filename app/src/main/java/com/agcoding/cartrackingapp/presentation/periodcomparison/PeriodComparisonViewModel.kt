package com.agcoding.cartrackingapp.presentation.periodcomparison

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agcoding.cartrackingapp.domain.model.Car
import com.agcoding.cartrackingapp.domain.model.DateFilter
import com.agcoding.cartrackingapp.domain.model.PeriodComparison
import com.agcoding.cartrackingapp.domain.model.periodComparison
import com.agcoding.cartrackingapp.domain.repository.CarRepository
import com.agcoding.cartrackingapp.domain.usecase.transaction.GetCarTransactionsUseCase
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

/**
 * Two periods of one car's history, compared.
 *
 * Both periods are picked with the shared [DateFilter] — whole years or specific months
 * inside them — and both are measured against the same transaction list, so each side
 * reports exactly what the car details screen would report for that period.
 */
@HiltViewModel
class PeriodComparisonViewModel @Inject constructor(
    private val getCarTransactionsUseCase: GetCarTransactionsUseCase,
    carRepository: CarRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val carId: Long = savedStateHandle.get<Long>("carId") ?: 0L

    private val _primaryFilter = MutableStateFlow(DateFilter.None)
    val primaryFilter: StateFlow<DateFilter> = _primaryFilter.asStateFlow()

    private val _secondaryFilter = MutableStateFlow(DateFilter.None)
    val secondaryFilter: StateFlow<DateFilter> = _secondaryFilter.asStateFlow()

    val car: StateFlow<Car?> = carRepository.getCarById(carId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    private val allTransactions: StateFlow<List<TransactionWithData>> =
        getCarTransactionsUseCase(carId)
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /** Years that actually have records, so both pickers only offer useful choices. */
    val availableYears: StateFlow<List<Int>> = allTransactions
        .map { transactions -> DateFilter.availableYears(transactions.map { it.transaction.timestamp }) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val comparison: StateFlow<PeriodComparison> =
        combine(allTransactions, _primaryFilter, _secondaryFilter) { transactions, primary, secondary ->
            transactions.periodComparison(primary, secondary)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PeriodComparison())

    init {
        seedDefaultPeriods()
    }

    /**
     * Opens on the most useful comparison we can guess: the two most recent years that
     * have records. With a single year of history the second period stays on the whole
     * history, so the screen still shows something meaningful until the user picks.
     */
    private fun seedDefaultPeriods() {
        viewModelScope.launch {
            val years = DateFilter.availableYears(
                getCarTransactionsUseCase(carId).first().map { it.transaction.timestamp }
            )
            // The user may have picked a period while the first load was in flight.
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
}
