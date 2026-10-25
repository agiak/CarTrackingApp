package com.agcoding.cartrackingapp.domain.usecase.statistics

import com.agcoding.cartrackingapp.domain.model.StationNameTotals
import com.agcoding.cartrackingapp.domain.model.StationSummary
import com.agcoding.cartrackingapp.domain.model.parseStation
import com.agcoding.cartrackingapp.domain.repository.RefillRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import javax.inject.Inject

/**
 * The stations in a period, biggest spender first.
 *
 * The database has already added up each distinct location string; the work
 * left is the part SQL cannot do — folding "Shell" into "shell ", splitting the
 * address off the name, and deciding which spelling to show.
 */
class GetFuelStationStatsUseCase @Inject constructor(
    private val refillRepository: RefillRepository,
) {
    operator fun invoke(
        window: LongRange,
        carIds: Set<Long>,
    ): Flow<List<StationSummary>> = refillRepository
        .observeStationTotals(window, carIds)
        .map { rows -> merge(rows) }
        .flowOn(Dispatchers.Default)

    private fun merge(rows: List<StationNameTotals>): List<StationSummary> = rows
        .groupBy { parseStation(it.rawName)?.key ?: UNKNOWN_KEY }
        .map { (key, group) -> summarize(key, group) }
        .sortedWith(compareByDescending<StationSummary> { it.totalCost }.thenBy { it.name })

    private fun summarize(key: String, group: List<StationNameTotals>): StationSummary {
        // The spelling that paid for the most fuel wins the label: typos and
        // half-typed names exist, and the one attached to the real spending is
        // the one the user recognises.
        val leading = group.maxByOrNull { it.totalCost }
        val parsed = leading?.let { parseStation(it.rawName) }

        // The address is taken from the most recent refill that recorded one,
        // not from the leading spelling — a station keeps its name when it
        // changes hands, and the newest address is the one to drive to.
        val address = group
            .sortedByDescending { it.lastRefillAt }
            .firstNotNullOfOrNull { parseStation(it.rawName)?.address }

        return StationSummary(
            key = key,
            name = parsed?.name.orEmpty(),
            address = address,
            isUnknown = key == UNKNOWN_KEY,
            rawNames = group.map { it.rawName }.toSet(),
            totalCost = group.sumOf { it.totalCost },
            totalLiters = group.sumOf { it.totalLiters },
            refillCount = group.sumOf { it.refillCount },
            lastRefillAt = group.maxOfOrNull { it.lastRefillAt } ?: 0L,
        )
    }

    companion object {
        /**
         * The bucket for refills that recorded no place at all. Not a station
         * name, so it can never collide with one: [parseStation] returns null
         * for blank input and every name it does return is non-empty.
         */
        const val UNKNOWN_KEY = ""
    }
}
