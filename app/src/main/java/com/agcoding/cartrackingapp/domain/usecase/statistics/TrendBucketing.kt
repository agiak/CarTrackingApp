package com.agcoding.cartrackingapp.domain.usecase.statistics

import com.agcoding.cartrackingapp.domain.model.AggregationBucket
import com.agcoding.cartrackingapp.domain.model.DateFilter
import com.agcoding.cartrackingapp.domain.model.DateRange
import java.util.Calendar

/**
 * Bucketing helpers shared by the four trend use cases.
 *
 * The graph screens filter with the same [DateFilter] as the rest of the app, which
 * can select periods that are not contiguous — March and April of both 2024 and 2025,
 * say. Two things follow from that:
 *
 *  * the range shown has to be derived from the records that actually matched, not
 *    from a "last N days" window, and
 *  * once months are being picked individually, the chart's own buckets should be
 *    calendar months, so each selected month is one point rather than being smeared
 *    across a fixed-width bucket.
 *
 * Buckets that end up empty are skipped by the callers, so the gaps between selected
 * periods simply do not appear on the chart.
 */

/**
 * The next bucket boundary after [startMillis].
 *
 * Month, quarter and year buckets step by the calendar rather than by a fixed
 * 30/90/365 days, so a bucket labelled "March" really is March. Fixed-width buckets
 * drift by roughly five days a year, which is enough to mislabel a point once a chart
 * spans more than a couple of years.
 */
fun nextBucketStart(startMillis: Long, bucket: AggregationBucket): Long {
    val calendar = Calendar.getInstance().apply { timeInMillis = startMillis }
    when (bucket) {
        AggregationBucket.MONTHLY -> calendar.add(Calendar.MONTH, 1)
        AggregationBucket.QUARTERLY -> calendar.add(Calendar.MONTH, 3)
        AggregationBucket.YEARLY -> calendar.add(Calendar.YEAR, 1)
        else -> calendar.add(Calendar.DAY_OF_YEAR, bucket.daysPerBucket)
    }
    return calendar.timeInMillis
}

/** Midnight on the first day of the month containing [millis]. */
fun startOfMonth(millis: Long): Long = Calendar.getInstance().apply {
    timeInMillis = millis
    set(Calendar.DAY_OF_MONTH, 1)
    set(Calendar.HOUR_OF_DAY, 0)
    set(Calendar.MINUTE, 0)
    set(Calendar.SECOND, 0)
    set(Calendar.MILLISECOND, 0)
}.timeInMillis

/**
 * The range and bucket size to chart for [dateFilter] over records at [timestamps].
 *
 * With no filter the whole history is charted with the existing adaptive bucket size.
 * With a filter the range covers only the matched records; picking individual months
 * pins the buckets to calendar months so each selected month reads as its own point.
 *
 * Returns `null` when nothing matched — callers report "no data" for that.
 */
fun trendWindowFor(
    dateFilter: DateFilter,
    timestamps: List<Long>,
    label: String = ""
): TrendWindow? {
    val matched = if (dateFilter.isActive) {
        timestamps.filter { dateFilter.matches(it) }
    } else {
        timestamps
    }
    if (matched.isEmpty()) return null

    val first = matched.min()
    val last = matched.max()

    val monthly = dateFilter.months.isNotEmpty()
    val startMillis = if (monthly) startOfMonth(first) else first
    // Push the end past the last record so its bucket is closed and included.
    val endMillis = last + 1

    val totalDays = ((endMillis - startMillis) / (24 * 60 * 60 * 1000L)).toInt()
    val bucket = if (monthly) {
        AggregationBucket.MONTHLY
    } else {
        AggregationBucket.forDateRange(totalDays)
    }

    return TrendWindow(
        dateRange = DateRange(startMillis, endMillis, label),
        bucket = bucket
    )
}

/** The charting window chosen for a filter: what to show, and at what granularity. */
data class TrendWindow(
    val dateRange: DateRange,
    val bucket: AggregationBucket
)
