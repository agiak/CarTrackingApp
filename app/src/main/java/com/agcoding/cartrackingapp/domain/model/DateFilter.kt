package com.agcoding.cartrackingapp.domain.model

import java.time.Instant
import java.time.ZoneId

/**
 * The single date filter shape used everywhere in the app: the user picks a year,
 * and optionally a month inside that year.
 *
 * - [year] `null`               -> no date filtering, everything matches.
 * - [year] set, [month] `null`  -> the whole of that year.
 * - [year] and [month] set      -> that one month.
 *
 * [month] follows the `java.time` convention: 1 = January … 12 = December. A month
 * without a year is meaningless, so [normalized] drops it.
 */
data class DateFilter(
    val year: Int? = null,
    val month: Int? = null
) {
    /** True when this filter actually narrows anything down. */
    val isActive: Boolean get() = year != null

    /** A month can only be meaningful together with a year. */
    val normalized: DateFilter
        get() = if (year == null && month != null) DateFilter() else this

    /**
     * Whether a record at [timestamp] (epoch millis) falls inside this filter.
     * Uses the device time zone so the buckets match what the user sees on the cards.
     */
    fun matches(timestamp: Long, zoneId: ZoneId = ZoneId.systemDefault()): Boolean {
        val selectedYear = year ?: return true
        val date = Instant.ofEpochMilli(timestamp).atZone(zoneId).toLocalDate()
        if (date.year != selectedYear) return false
        val selectedMonth = month ?: return true
        return date.monthValue == selectedMonth
    }

    fun withYear(year: Int?): DateFilter =
        if (year == null) DateFilter() else copy(year = year)

    fun withMonth(month: Int?): DateFilter = copy(month = month)

    companion object {
        /** No date filtering — the default on every screen. */
        val None = DateFilter()

        /** The years present in [timestamps], newest first, for populating the picker. */
        fun availableYears(
            timestamps: List<Long>,
            zoneId: ZoneId = ZoneId.systemDefault()
        ): List<Int> = timestamps
            .map { Instant.ofEpochMilli(it).atZone(zoneId).toLocalDate().year }
            .distinct()
            .sortedDescending()
    }
}

/** Keeps only the entries whose [timestampOf] falls inside [dateFilter]. */
inline fun <T> Iterable<T>.filterByDate(
    dateFilter: DateFilter,
    timestampOf: (T) -> Long
): List<T> = if (!dateFilter.isActive) {
    toList()
} else {
    filter { dateFilter.matches(timestampOf(it)) }
}
