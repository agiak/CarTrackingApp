package com.agcoding.cartrackingapp.domain.usecase.statistics

import com.agcoding.cartrackingapp.domain.model.DateFilter
import com.agcoding.cartrackingapp.domain.repository.ExpenseRepository
import com.agcoding.cartrackingapp.domain.repository.RefillRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * Years that actually have records, newest first — what the date filter sheet offers.
 *
 * Each graph asks about the records it charts, so the picker never offers a year that
 * would draw an empty chart: the cost graph counts expenses too, the refill-based ones
 * do not.
 */
class GetAvailableYearsUseCase @Inject constructor(
    private val refillRepository: RefillRepository,
    private val expenseRepository: ExpenseRepository
) {
    operator fun invoke(
        carId: Long? = null,
        includeExpenses: Boolean = false
    ): Flow<List<Int>> {
        val refills = if (carId != null) {
            refillRepository.getRefillsByCarId(carId)
        } else {
            refillRepository.getAllRefills()
        }

        if (!includeExpenses) {
            return refills.map { list -> DateFilter.availableYears(list.map { it.timestamp }) }
        }

        val expenses = if (carId != null) {
            expenseRepository.getExpensesByCarId(carId)
        } else {
            expenseRepository.getAllExpenses()
        }

        return combine(refills, expenses) { refillList, expenseList ->
            DateFilter.availableYears(
                refillList.map { it.timestamp } + expenseList.map { it.timestamp }
            )
        }
    }
}
