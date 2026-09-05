package com.agcoding.cartrackingapp.presentation.transactions

import com.agcoding.cartrackingapp.domain.model.DateFilter
import com.agcoding.cartrackingapp.domain.model.filterByDate
import com.agcoding.cartrackingapp.presentation.transactions.model.TransactionType
import com.agcoding.cartrackingapp.presentation.transactions.model.TransactionWithData

/**
 * Type, date and sort settings for a single-car transaction list.
 *
 * Shared by the car details screen and its "see all" screen so both offer exactly the
 * same controls. The all-cars [TransactionsViewModel] keeps its own filter because it
 * additionally filters by car.
 */
data class TransactionListFilter(
    val showRefills: Boolean = true,
    val showExpenses: Boolean = true,
    val dateFilter: DateFilter = DateFilter.None,
    val sortOption: SortOption = SortOption.DATE_NEWEST
) {
    /** True when the type chips exclude something (both off is treated as "show nothing"). */
    val hasTypeFilter: Boolean get() = !showRefills || !showExpenses

    /** True when anything other than the default ordering is in effect. */
    val hasActiveFilters: Boolean get() = hasTypeFilter || dateFilter.isActive
}

/** Applies [listFilter]'s type and date narrowing, then its ordering. */
fun List<TransactionWithData>.applyFilter(
    listFilter: TransactionListFilter
): List<TransactionWithData> {
    val byType = if (!listFilter.hasTypeFilter) {
        this
    } else {
        filter { entry ->
            when (entry.transaction.type) {
                TransactionType.REFILL -> listFilter.showRefills
                TransactionType.EXPENSE -> listFilter.showExpenses
            }
        }
    }

    val byDate = byType.filterByDate(listFilter.dateFilter) { it.transaction.timestamp }

    return when (listFilter.sortOption) {
        SortOption.DATE_NEWEST -> byDate.sortedByDescending { it.transaction.timestamp }
        SortOption.DATE_OLDEST -> byDate.sortedBy { it.transaction.timestamp }
        SortOption.COST_HIGHEST -> byDate.sortedByDescending { it.transaction.amount }
        SortOption.COST_LOWEST -> byDate.sortedBy { it.transaction.amount }
    }
}
