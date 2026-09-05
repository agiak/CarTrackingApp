package com.agcoding.cartrackingapp.domain.model

import com.agcoding.cartrackingapp.presentation.transactions.model.TransactionWithData

/**
 * Totals for whatever period a [DateFilter] selects — the whole history, one year,
 * or one month. Computed from the same unified transaction list the screen shows,
 * so the numbers and the list can never disagree.
 */
data class PeriodStatistics(
    val filter: DateFilter = DateFilter.None,
    val totalCost: Double = 0.0,
    val fuelCost: Double = 0.0,
    val expensesCost: Double = 0.0,
    val totalDistance: Double = 0.0,
    val totalLiters: Double = 0.0,
    val averageConsumption: Double = 0.0,
    val averagePricePerLiter: Double = 0.0,
    val costPerKilometer: Double = 0.0,
    val refillCount: Int = 0,
    val expenseCount: Int = 0
) {
    val transactionCount: Int get() = refillCount + expenseCount
    val hasData: Boolean get() = transactionCount > 0
}

/**
 * Totals for the entries of this list that fall inside [filter].
 *
 * The averages mirror [com.agcoding.cartrackingapp.domain.usecase.statistics.GetCarStatisticsUseCase]
 * so a period covering everything reports the same figures as the all-time stats.
 */
fun List<TransactionWithData>.periodStatistics(filter: DateFilter): PeriodStatistics {
    val inPeriod = filterByDate(filter) { it.transaction.timestamp }

    val refills = inPeriod.filterIsInstance<TransactionWithData.RefillTransaction>().map { it.refill }
    val expenses = inPeriod.filterIsInstance<TransactionWithData.ExpenseTransaction>().map { it.expense }

    val fuelCost = refills.sumOf { it.amountPaid }
    val expensesCost = expenses.sumOf { it.amount }
    val totalCost = fuelCost + expensesCost
    val totalDistance = refills.sumOf { it.tripDistance }
    val totalLiters = refills.sumOf { it.litersAdded }

    return PeriodStatistics(
        filter = filter,
        totalCost = totalCost,
        fuelCost = fuelCost,
        expensesCost = expensesCost,
        totalDistance = totalDistance,
        totalLiters = totalLiters,
        averageConsumption = if (totalDistance > 0) (totalLiters / totalDistance) * 100.0 else 0.0,
        averagePricePerLiter = if (totalLiters > 0) fuelCost / totalLiters else 0.0,
        costPerKilometer = if (totalDistance > 0) totalCost / totalDistance else 0.0,
        refillCount = refills.size,
        expenseCount = expenses.size
    )
}
