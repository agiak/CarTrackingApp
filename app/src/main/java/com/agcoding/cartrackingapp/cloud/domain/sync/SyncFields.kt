package com.agcoding.cartrackingapp.cloud.domain.sync

import java.math.BigDecimal
import java.math.RoundingMode
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Turns app values into the exact text the server will hand back.
 *
 * This is the single most load-bearing detail of the whole sync. A row's hash
 * is computed from these strings on the phone and again from what PostgREST
 * returns; if the two spellings differ by so much as a trailing zero, the row
 * looks changed on every pass and the app re-uploads it forever. So anything
 * that has more than one valid spelling goes through here, on both sides.
 */
object SyncFields {

    /** `numeric(p,2)` always comes back with two decimals — so send two. */
    fun money(value: Double): String =
        BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).toPlainString()

    /** For columns that are not money but still numeric with a fixed scale. */
    fun decimal(value: Double, scale: Int): String =
        BigDecimal.valueOf(value).setScale(scale, RoundingMode.HALF_UP).toPlainString()

    /** Epoch millis to a `date`, in the phone's zone — the day the user saw. */
    fun date(epochMillis: Long, zone: ZoneId = ZoneId.systemDefault()): String =
        DateTimeFormatter.ISO_LOCAL_DATE.format(
            Instant.ofEpochMilli(epochMillis).atZone(zone).toLocalDate()
        )

    /** Epoch millis to a `timestamptz`, always in UTC. */
    fun timestamp(epochMillis: Long): String = Instant.ofEpochMilli(epochMillis).toString()

    /**
     * A set of ids as one comparable string: blanks dropped, duplicates
     * dropped, sorted. Postgres gives no order guarantee for an array column,
     * so without sorting the same set hashes differently each way round.
     */
    fun idList(ids: Collection<String>): String =
        ids.filter { it.isNotBlank() }.distinct().sorted().joinToString(",")

    /**
     * Text cut to the column's CHECK limit. One row that violates a constraint
     * makes PostgREST reject the entire batch it travelled in, so the trimming
     * has to happen before the upload, not be left to the database.
     */
    fun text(value: String?, maxLength: Int): String? =
        value?.trim()?.takeIf { it.isNotEmpty() }?.take(maxLength)
}
