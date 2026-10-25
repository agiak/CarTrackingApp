package com.agcoding.cartrackingapp.domain.model

import com.agcoding.cartrackingapp.util.safeDivide

/**
 * Where the fuel money goes, grouped by station.
 *
 * A refill records its place as one free-text [FuelRefill.locationName] — there
 * is no separate name and address column, and there never was. The string
 * arrives either from the reverse geocoder ("Kifisias Ave 112, Marousi") or
 * from the user typing it ("Shell · Kifisias Ave 112"), so the split into a
 * name and an address is worked out from the text itself. See [parseStation].
 */
data class StationSummary(
    /** Lower-cased, space-collapsed [name]: what rows are merged on. */
    val key: String,
    val name: String,
    val address: String?,
    /** True for the bucket of refills that recorded no place at all. */
    val isUnknown: Boolean,
    /**
     * Every stored spelling that was merged into this station.
     *
     * Carried along because the database only knows the raw strings: a query
     * for "this station's refills" has to ask for all of them, and rebuilding
     * the mapping at the call site would mean repeating the merge rules.
     */
    val rawNames: Set<String>,
    val totalCost: Double,
    val totalLiters: Double,
    val refillCount: Int,
    val lastRefillAt: Long,
) {
    /** Litres bought, not the mean of each refill's rate: a 60 L fill should weigh more than a 10 L top-up. */
    val averagePricePerLiter: Double get() = totalCost.safeDivide(totalLiters)

    val averageRefillCost: Double get() = totalCost.safeDivide(refillCount.toDouble())
}

/** One refill in the Top refills ranking, with the names its ids stand for. */
data class TopRefill(
    val refill: FuelRefill,
    val carName: String?,
    val stationName: String,
    val stationAddress: String?,
    val tripName: String?,
) {
    /**
     * Derived rather than read from [FuelRefill.pricePerLiter]: that column is
     * written as `amountPaid / litersAdded` with no guard, so a refill saved
     * with zero litres stores an infinity that would sort to the top for ever.
     */
    val pricePerLiter: Double get() = refill.amountPaid.safeDivide(refill.litersAdded)
}

/**
 * Every refill that stored one exact spelling of a location, added up.
 *
 * The step between the database and [StationSummary]: the database can group
 * identical strings but cannot fold case or split an address off, so several of
 * these may still belong to the same station.
 */
data class StationNameTotals(
    val rawName: String,
    val totalCost: Double,
    val totalLiters: Double,
    val refillCount: Int,
    val lastRefillAt: Long,
)

/** How far back the Fuel stations screen looks. */
enum class StationPeriod(val months: Int?) {
    LAST_6_MONTHS(6),
    LAST_YEAR(12),
    ALL_TIME(null),
}

/**
 * The timestamp range this period selects.
 *
 * Open-ended at the top on purpose. A refill's date is typed by the user, who
 * can and does date one a day ahead after filling up late at night; closing the
 * window at "now" would drop it out of "Last 6 months" until the clock caught
 * up, which reads as lost data.
 */
fun StationPeriod.window(now: Long = System.currentTimeMillis()): LongRange {
    val months = months ?: return 0L..Long.MAX_VALUE
    val start = java.time.Instant.ofEpochMilli(now)
        .atZone(java.time.ZoneId.systemDefault())
        .minusMonths(months.toLong())
        .toInstant()
        .toEpochMilli()
    return start..Long.MAX_VALUE
}

/** Which number the Top refills tab ranks by. */
enum class TopRefillSort {
    TOTAL_COST,
    PRICE_PER_LITER,
    LITERS,
}

/** A [FuelRefill.locationName] taken apart into the two things a row shows. */
data class ParsedStation(val name: String, val address: String?) {
    /** Lower-cased and space-collapsed, so "Shell " and "shell" are one station. */
    val key: String get() = name.lowercase().replace(WHITESPACE, " ").trim()

    private companion object {
        val WHITESPACE = Regex("\\s+")
    }
}

/**
 * Splits a refill's location into a station name and an address.
 *
 * The separators are tried in the order the app itself produces them: the
 * middot is what the refill form's own placeholder uses, so where it appears
 * the user meant exactly this split; a comma is what the geocoder emits
 * between street and city; the spaced dash is the third thing people type.
 *
 * Only the *first* separator splits, so "Shell · Kifisias Ave 112, Marousi"
 * keeps its whole address together instead of losing the city.
 *
 * A purely geocoded string has no brand in it, so its street becomes the
 * station name. That is not a mistake — grouping by street is the most the
 * recorded data supports, and the row still reads as the place it was.
 */
fun parseStation(locationName: String?): ParsedStation? {
    val raw = locationName?.trim().orEmpty()
    if (raw.isEmpty()) return null

    for (separator in SEPARATORS) {
        val at = raw.indexOf(separator)
        if (at <= 0) continue
        val name = raw.substring(0, at).trim()
        val address = raw.substring(at + separator.length).trim()
        if (name.isNotEmpty()) {
            return ParsedStation(name = name, address = address.takeIf { it.isNotEmpty() })
        }
    }
    return ParsedStation(name = raw, address = null)
}

private val SEPARATORS = listOf(" · ", "·", ",", " - ")
