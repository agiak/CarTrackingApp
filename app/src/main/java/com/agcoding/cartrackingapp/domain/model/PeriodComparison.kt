package com.agcoding.cartrackingapp.domain.model

import com.agcoding.cartrackingapp.presentation.transactions.model.TransactionWithData
import java.time.Instant
import java.time.ZoneId
import kotlin.math.abs

/** The metrics a period comparison reports, in the order they are presented. */
enum class PeriodMetricKey {
    TOTAL_COST,
    FUEL_COST,
    EXPENSES_COST,
    DISTANCE,
    LITERS,
    AVG_CONSUMPTION,
    AVG_PRICE_PER_LITER,
    COST_PER_KM,
    TRANSACTIONS
}

/** Which way a metric moved between the two periods. */
enum class ComparisonDirection { UP, DOWN, FLAT }

/**
 * One metric measured in both periods.
 *
 * [lowerIsBetter] is deliberately null for the absolute totals. Spending less across a
 * shorter or quieter period is not an improvement — it just means less driving — so only
 * the rate metrics (consumption, price per litre, cost per kilometre), which are already
 * normalised by distance or volume, carry a good/bad judgement. The totals still report
 * their delta; they are simply shown without a verdict colour.
 */
data class PeriodMetric(
    val key: PeriodMetricKey,
    val primary: Double,
    val secondary: Double,
    val lowerIsBetter: Boolean? = null
) {
    /** How much the primary period differs from the secondary one. */
    val delta: Double get() = primary - secondary

    /**
     * The change relative to the secondary period, or null when there is no baseline to
     * grow from (the secondary period has no value for this metric).
     */
    val percentChange: Double?
        get() = if (secondary == 0.0) null else (delta / abs(secondary)) * 100.0

    /**
     * Movement between the periods.
     *
     * "Flat" is deliberately relative rather than a fixed amount: the metrics here span
     * four orders of magnitude, from a few cents per kilometre to thousands of euros, so
     * an absolute threshold would call a real change in cost-per-kilometre flat while
     * flagging a rounding error in a yearly total. Anything under a tenth of a percent
     * counts as unchanged, which keeps the arrow, the delta and the percentage agreeing
     * with each other at every scale.
     */
    val direction: ComparisonDirection
        get() {
            val scale = maxOf(abs(primary), abs(secondary))
            return when {
                scale == 0.0 || abs(delta) / scale < FLAT_RATIO -> ComparisonDirection.FLAT
                delta > 0 -> ComparisonDirection.UP
                else -> ComparisonDirection.DOWN
            }
        }

    /** True when the move is in the desirable direction; null when the metric has no verdict. */
    val isImprovement: Boolean?
        get() {
            val better = lowerIsBetter ?: return null
            return when (direction) {
                ComparisonDirection.FLAT -> null
                ComparisonDirection.UP -> !better
                ComparisonDirection.DOWN -> better
            }
        }

    /** Worth showing — a row where both periods are empty says nothing. */
    val hasData: Boolean get() = primary != 0.0 || secondary != 0.0

    private companion object {
        /** Below a tenth of a percent, a metric counts as unchanged. */
        const val FLAT_RATIO = 0.001
    }
}

/**
 * Two periods of one car's history set side by side.
 *
 * Both sides are ordinary [PeriodStatistics], computed from the same transaction list with
 * a different [DateFilter], so a period here reports exactly the same figures it would on
 * the car details screen.
 */
data class PeriodComparison(
    val primary: PeriodStatistics = PeriodStatistics(),
    val secondary: PeriodStatistics = PeriodStatistics()
) {
    /** At least one side has records — otherwise there is nothing to draw. */
    val hasData: Boolean get() = primary.hasData || secondary.hasData

    /** Both sides have records, so the deltas actually mean something. */
    val isComparable: Boolean get() = primary.hasData && secondary.hasData

    /** The metrics that have a value in at least one of the two periods. */
    val metrics: List<PeriodMetric>
        get() = listOf(
            PeriodMetric(PeriodMetricKey.TOTAL_COST, primary.totalCost, secondary.totalCost),
            PeriodMetric(PeriodMetricKey.FUEL_COST, primary.fuelCost, secondary.fuelCost),
            PeriodMetric(
                PeriodMetricKey.EXPENSES_COST,
                primary.expensesCost,
                secondary.expensesCost
            ),
            PeriodMetric(
                PeriodMetricKey.DISTANCE,
                primary.totalDistance,
                secondary.totalDistance
            ),
            PeriodMetric(PeriodMetricKey.LITERS, primary.totalLiters, secondary.totalLiters),
            PeriodMetric(
                PeriodMetricKey.AVG_CONSUMPTION,
                primary.averageConsumption,
                secondary.averageConsumption,
                lowerIsBetter = true
            ),
            PeriodMetric(
                PeriodMetricKey.AVG_PRICE_PER_LITER,
                primary.averagePricePerLiter,
                secondary.averagePricePerLiter,
                lowerIsBetter = true
            ),
            PeriodMetric(
                PeriodMetricKey.COST_PER_KM,
                primary.costPerKilometer,
                secondary.costPerKilometer,
                lowerIsBetter = true
            ),
            PeriodMetric(
                PeriodMetricKey.TRANSACTIONS,
                primary.transactionCount.toDouble(),
                secondary.transactionCount.toDouble()
            )
        ).filter { it.hasData }
}

/** Statistics for [primary] and [secondary] taken from the same list of transactions. */
fun List<TransactionWithData>.periodComparison(
    primary: DateFilter,
    secondary: DateFilter
): PeriodComparison = PeriodComparison(
    primary = periodStatistics(primary),
    secondary = periodStatistics(secondary)
)

/**
 * What the entries inside [filter] cost, totalled per calendar month (1 = January).
 *
 * Months are keyed without their year on purpose: a period can span several years, and
 * the breakdown is there to show the shape of the spending across the year — January of
 * 2024 and January of 2025 belong in the same "January" of a two-year period. Months with
 * nothing in them are absent rather than zero, so callers can tell "no records" apart
 * from "records adding up to nothing".
 */
fun List<TransactionWithData>.monthlyTotals(
    filter: DateFilter,
    zoneId: ZoneId = ZoneId.systemDefault()
): Map<Int, Double> = filterByDate(filter) { it.transaction.timestamp }
    .groupingBy { entry ->
        Instant.ofEpochMilli(entry.transaction.timestamp).atZone(zoneId).toLocalDate().monthValue
    }
    .fold(0.0) { sum, entry -> sum + entry.transaction.amount }
