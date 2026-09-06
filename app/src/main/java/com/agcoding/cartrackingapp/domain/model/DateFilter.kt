package com.agcoding.cartrackingapp.domain.model

import java.time.Instant
import java.time.ZoneId

/**
 * The single date filter shape used everywhere in the app: the user picks any number
 * of years, and optionally any number of months inside those years.
 *
 * The two sets combine as a cross product — months narrow *each* selected year rather
 * than naming specific year/month pairs. Picking 2024 and 2025 with March and April
 * therefore matches March and April of both years.
 *
 * - [years] empty                  -> no date filtering, everything matches.
 * - [years] set, [months] empty    -> the whole of each selected year.
 * - [years] and [months] set       -> those months within those years.
 *
 * [months] follows the `java.time` convention: 1 = January … 12 = December. Months
 * without any year are meaningless, so [normalized] drops them.
 */
data class DateFilter(
    val years: Set<Int> = emptySet(),
    val months: Set<Int> = emptySet()
) {
    /** True when this filter actually narrows anything down. */
    val isActive: Boolean get() = years.isNotEmpty()

    /** A month can only be meaningful together with at least one year. */
    val normalized: DateFilter
        get() = if (years.isEmpty() && months.isNotEmpty()) DateFilter() else this

    /**
     * Whether a record at [timestamp] (epoch millis) falls inside this filter.
     * Uses the device time zone so the buckets match what the user sees on the cards.
     */
    fun matches(timestamp: Long, zoneId: ZoneId = ZoneId.systemDefault()): Boolean {
        if (years.isEmpty()) return true
        val date = Instant.ofEpochMilli(timestamp).atZone(zoneId).toLocalDate()
        if (date.year !in years) return false
        if (months.isEmpty()) return true
        return date.monthValue in months
    }

    /**
     * Whether a bucket already reduced to a calendar [year] and [month] is inside this
     * filter. [month] is 1-based like [months] — callers holding a 0-based
     * `Calendar.MONTH` must add 1.
     */
    fun matchesYearMonth(year: Int, month: Int): Boolean {
        if (years.isEmpty()) return true
        if (year !in years) return false
        if (months.isEmpty()) return true
        return month in months
    }

    /** Adds [year] if absent, removes it if present. Clearing the last year clears months too. */
    fun toggleYear(year: Int): DateFilter {
        val next = if (year in years) years - year else years + year
        return if (next.isEmpty()) DateFilter() else copy(years = next)
    }

    /** Adds [month] if absent, removes it if present. */
    fun toggleMonth(month: Int): DateFilter =
        copy(months = if (month in months) months - month else months + month)

    /** Back to every month of the selected years. */
    fun clearMonths(): DateFilter = copy(months = emptySet())

    companion object {
        /** No date filtering — the default on every screen. */
        val None = DateFilter()

        /** Convenience for a single year, optionally a single month inside it. */
        fun of(year: Int, month: Int? = null) =
            DateFilter(years = setOf(year), months = month?.let { setOf(it) } ?: emptySet())

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
