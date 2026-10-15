package com.agcoding.cartrackingapp.presentation.periodcomparison

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingFlat
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.domain.model.ComparisonDirection
import com.agcoding.cartrackingapp.domain.model.DateFilter
import com.agcoding.cartrackingapp.domain.model.PeriodComparison
import com.agcoding.cartrackingapp.domain.model.PeriodMetric
import com.agcoding.cartrackingapp.domain.model.PeriodMetricKey
import com.agcoding.cartrackingapp.presentation.components.AutoSizeText
import com.agcoding.cartrackingapp.presentation.components.GroupedDivider
import com.agcoding.cartrackingapp.presentation.components.GroupedListContainer
import com.agcoding.cartrackingapp.presentation.components.StyledCard
import com.agcoding.cartrackingapp.presentation.components.dateFilterLabel
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppColorScheme
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppDimens
import com.agcoding.cartrackingapp.shared.ui.tokens.AppNumbers
import com.agcoding.cartrackingapp.shared.ui.tokens.categoryService
import com.agcoding.cartrackingapp.util.formatMoney
import com.agcoding.cartrackingapp.util.formatNumber

/**
 * The body of a period comparison: the two period pickers, then one row per metric.
 *
 * Shared by both comparison screens — one car reached from its details, and the whole
 * fleet (or a subset of it) reached from the statistics screen — so the two read
 * identically and only differ in what they scope the numbers to. Each screen supplies
 * its own LazyColumn, and can add content around these items.
 */
fun LazyListScope.periodComparisonItems(
    comparison: PeriodComparison,
    primaryFilter: DateFilter,
    secondaryFilter: DateFilter,
    onEditPrimary: () -> Unit,
    onEditSecondary: () -> Unit,
    onSwap: (() -> Unit)? = null
) {
    item(key = "period_pickers") {
        PeriodPickers(
            primaryFilter = primaryFilter,
            secondaryFilter = secondaryFilter,
            primaryCount = comparison.primary.transactionCount,
            secondaryCount = comparison.secondary.transactionCount,
            onEditPrimary = onEditPrimary,
            onEditSecondary = onEditSecondary,
            onSwap = onSwap
        )
    }

    if (!comparison.hasData) {
        item(key = "empty") {
            PeriodComparisonNotice(text = stringResource(R.string.period_comparison_no_data))
        }
        return
    }

    // A one-sided comparison is still worth showing, but the deltas are just the
    // whole amount — say so instead of letting "+100%" imply a real trend.
    if (!comparison.isComparable) {
        item(key = "one_sided") {
            PeriodComparisonNotice(text = stringResource(R.string.period_comparison_one_sided))
        }
    }

    // One table: a header naming the three columns, then one line per metric.
    // Each metric used to take two lines — its name, then its figures — which
    // meant the A column restarted its alignment every row and the thing a
    // comparison is for, reading down a column, did not work.
    item(key = "metrics") {
        Column {
            MetricsHeaderRow(
                primaryLabel = dateFilterLabel(primaryFilter),
                secondaryLabel = dateFilterLabel(secondaryFilter)
            )
            Spacer(modifier = Modifier.height(8.dp))
            GroupedListContainer {
                comparison.metrics.forEachIndexed { index, metric ->
                    if (index > 0) GroupedDivider(startInset = false)
                    PeriodMetricRow(metric = metric)
                }
            }
        }
    }
}

/**
 * The two periods, one card each, with the swap between them.
 *
 * They used to be two rows inside one card, which made them look like two
 * settings of the same thing. They are not — they are the two sides of the
 * comparison, and the layout should say so: equal cards, the same shape, one
 * above the other, and the control that exchanges them sitting on the line
 * that divides them rather than hidden as an icon in the app bar where nobody
 * looking at the periods would think to find it.
 *
 * The whole card is the tap target. The period is the only thing on it worth
 * tapping, and a chip at the end of the row is a smaller target than the row
 * it sits in for no benefit.
 */
@Composable
private fun PeriodPickers(
    primaryFilter: DateFilter,
    secondaryFilter: DateFilter,
    primaryCount: Int,
    secondaryCount: Int,
    onEditPrimary: () -> Unit,
    onEditSecondary: () -> Unit,
    onSwap: (() -> Unit)?
) {
    val scheme = LocalAppColorScheme.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        PeriodPickerCard(
            label = stringResource(R.string.period_comparison_primary),
            accent = scheme.actionPrimary,
            filter = primaryFilter,
            transactionCount = primaryCount,
            onClick = onEditPrimary,
            modifier = Modifier.weight(1f)
        )
        if (onSwap != null) {
            SwapButton(onSwap = onSwap)
        } else {
            Spacer(modifier = Modifier.width(10.dp))
        }
        PeriodPickerCard(
            label = stringResource(R.string.period_comparison_secondary),
            accent = scheme.contentPrimary,
            filter = secondaryFilter,
            transactionCount = secondaryCount,
            onClick = onEditSecondary,
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * One side of the comparison.
 *
 * Side by side rather than stacked, because that is how the table below reads
 * them: A is the left column there and the left card here. Stacked, the card
 * order and the column order disagreed and the eye had to translate.
 */
@Composable
private fun PeriodPickerCard(
    label: String,
    accent: Color,
    filter: DateFilter,
    transactionCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = LocalAppColorScheme.current
    val dimens = LocalAppDimens.current

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(dimens.radius.tile))
            .background(scheme.backgroundCard)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(width = 10.dp, height = 3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(accent)
            )
            Spacer(modifier = Modifier.width(7.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = scheme.contentSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(modifier = Modifier.height(5.dp))
        Text(
            text = dateFilterLabel(filter),
            style = MaterialTheme.typography.titleSmall,
            color = scheme.contentPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = stringResource(R.string.period_comparison_record_count, transactionCount),
            style = AppNumbers.tag,
            color = scheme.contentSecondary,
            maxLines = 1
        )
    }
}

/** The exchange, between the two cards it exchanges. */
@Composable
private fun SwapButton(onSwap: () -> Unit) {
    val scheme = LocalAppColorScheme.current

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .padding(horizontal = 8.dp)
            .size(38.dp)
            .clip(CircleShape)
            .background(scheme.actionSecondary)
            .clickable(onClick = onSwap)
    ) {
        Icon(
            imageVector = Icons.Default.SwapHoriz,
            contentDescription = stringResource(R.string.period_comparison_swap),
            tint = scheme.actionPrimary,
            modifier = Modifier.size(20.dp)
        )
    }
}

/**
 * The four column widths, declared once.
 *
 * The header and every row read from the same four numbers, which is the only
 * way the columns actually line up — and lining up is the entire point of a
 * comparison table.
 */
private const val COL_METRIC = 1.25f
private const val COL_VALUE = 1f
private const val COL_DELTA = 0.85f

/** Names the four columns once, so every metric row below can stay bare numbers. */
@Composable
private fun MetricsHeaderRow(primaryLabel: String, secondaryLabel: String) {
    val scheme = LocalAppColorScheme.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, start = 16.dp, end = 16.dp)
    ) {
        Spacer(modifier = Modifier.weight(COL_METRIC))
        Text(
            text = primaryLabel,
            modifier = Modifier.weight(COL_VALUE),
            style = MaterialTheme.typography.labelMedium,
            color = scheme.actionPrimary,
            textAlign = TextAlign.End,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = secondaryLabel,
            modifier = Modifier.weight(COL_VALUE),
            style = MaterialTheme.typography.labelMedium,
            color = scheme.contentPrimary,
            textAlign = TextAlign.End,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = stringResource(R.string.period_comparison_difference),
            modifier = Modifier.weight(COL_DELTA),
            style = MaterialTheme.typography.labelMedium,
            color = scheme.contentSecondary,
            textAlign = TextAlign.End,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * One metric, on one line.
 *
 * It used to take two: the name, then the three figures under it. That made
 * the table twice as long and, worse, restarted the column alignment on every
 * row — eight metrics meant eight separate little three-column layouts rather
 * than one table you can read down.
 */
@Composable
private fun PeriodMetricRow(metric: PeriodMetric) {
    val scheme = LocalAppColorScheme.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 13.dp)
    ) {
        Text(
            text = metricLabel(metric.key),
            modifier = Modifier.weight(COL_METRIC),
            style = MaterialTheme.typography.bodyMedium,
            color = scheme.contentSecondary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        AutoSizeText(
            text = formatMetric(metric.key, metric.primary),
            style = AppNumbers.compact,
            color = scheme.actionPrimary,
            textAlign = TextAlign.End,
            modifier = Modifier
                .weight(COL_VALUE)
                .padding(start = 6.dp)
        )
        AutoSizeText(
            text = formatMetric(metric.key, metric.secondary),
            style = AppNumbers.compact,
            color = scheme.contentPrimary,
            textAlign = TextAlign.End,
            modifier = Modifier
                .weight(COL_VALUE)
                .padding(start = 6.dp)
        )
        DeltaCell(
            metric = metric,
            modifier = Modifier
                .weight(COL_DELTA)
                .padding(start = 6.dp)
        )
    }
}

/**
 * The difference between the two periods, as a percentage with an arrow.
 *
 * The absolute change went: on one line there is no room for both, and the
 * percentage is the one that answers "is this a lot" without knowing the
 * units. The absolute figures are the two columns to its left.
 *
 * Only the rate metrics are coloured — see [PeriodMetric.lowerIsBetter] for why a
 * smaller total is not automatically good news.
 */
@Composable
private fun DeltaCell(metric: PeriodMetric, modifier: Modifier = Modifier) {
    // The palette's success and rose, the same pair the insights and the
    // destructive actions use, rather than Material's fixed error red.
    val scheme = LocalAppColorScheme.current
    val tint = when (metric.isImprovement) {
        true -> scheme.statusSuccess
        false -> scheme.categoryService
        null -> scheme.contentSecondary
    }

    Row(
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Icon(
            imageVector = when (metric.direction) {
                ComparisonDirection.UP -> Icons.AutoMirrored.Filled.TrendingUp
                ComparisonDirection.DOWN -> Icons.AutoMirrored.Filled.TrendingDown
                ComparisonDirection.FLAT -> Icons.AutoMirrored.Filled.TrendingFlat
            },
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(3.dp))
        Text(
            text = metric.percentChange
                ?.let { signed(it, "${it.formatNumber(0)}%") }
                ?: "—",
            style = AppNumbers.tag,
            color = tint,
            textAlign = TextAlign.End,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/** Full-width note explaining why the comparison below is empty or one-sided. */
@Composable
fun PeriodComparisonNotice(text: String) {
    val scheme = LocalAppColorScheme.current
    StyledCard(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.contentSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Negative numbers already carry their minus sign; positive ones need a plus so the
 * direction of the change is readable without looking at the arrow.
 */
private fun signed(value: Double, formatted: String): String =
    if (value > 0) "+$formatted" else formatted

@Composable
private fun metricLabel(key: PeriodMetricKey): String = stringResource(
    when (key) {
        PeriodMetricKey.TOTAL_COST -> R.string.stat_total_spent
        PeriodMetricKey.FUEL_COST -> R.string.fuel_cost
        PeriodMetricKey.EXPENSES_COST -> R.string.expenses
        PeriodMetricKey.DISTANCE -> R.string.distance
        PeriodMetricKey.LITERS -> R.string.stat_liters
        PeriodMetricKey.AVG_CONSUMPTION -> R.string.avg_consumption
        PeriodMetricKey.AVG_PRICE_PER_LITER -> R.string.average_price_per_liter
        PeriodMetricKey.COST_PER_KM -> R.string.cost_per_km
        PeriodMetricKey.TRANSACTIONS -> R.string.period_comparison_records
    }
)

/**
 * Same units the period statistics card uses, so the two screens read alike.
 *
 * Cost per kilometre is the exception: it lives in the third decimal, and at two decimals
 * two visibly different periods would both print the same figure with a difference of
 * "0,00 €" between them.
 */
private fun formatMetric(key: PeriodMetricKey, value: Double): String = when (key) {
    PeriodMetricKey.TOTAL_COST,
    PeriodMetricKey.FUEL_COST,
    PeriodMetricKey.EXPENSES_COST,
    PeriodMetricKey.AVG_PRICE_PER_LITER -> value.formatMoney()

    PeriodMetricKey.COST_PER_KM -> value.formatMoney(decimals = 3)
    PeriodMetricKey.DISTANCE -> "${value.toInt().formatNumber()} km"
    PeriodMetricKey.LITERS -> "${value.formatNumber(1)} L"
    PeriodMetricKey.AVG_CONSUMPTION -> "${value.formatNumber(1)} L/100km"
    PeriodMetricKey.TRANSACTIONS -> value.toInt().formatNumber()
}

// ============================================
// Preview Composables
// ============================================

/** A neutral total, and the two verdicts a rate metric can carry. */
private val neutralMetric =
    PeriodMetric(PeriodMetricKey.TOTAL_COST, primary = 1842.5, secondary = 1610.0)
private val improvedMetric = PeriodMetric(
    PeriodMetricKey.AVG_CONSUMPTION,
    primary = 6.84,
    secondary = 7.13,
    lowerIsBetter = true
)
private val worsenedMetric = PeriodMetric(
    PeriodMetricKey.COST_PER_KM,
    primary = 0.149,
    secondary = 0.145,
    lowerIsBetter = true
)
private val noBaselineMetric =
    PeriodMetric(PeriodMetricKey.EXPENSES_COST, primary = 430.0, secondary = 0.0)

@Preview(name = "Period pickers", showBackground = true, widthDp = 400)
@Composable
private fun PreviewPeriodPickersCard() {
    CarTrackingAppTheme(darkTheme = true) {
        PeriodPickers(
            primaryFilter = DateFilter.of(year = 2025),
            secondaryFilter = DateFilter(years = setOf(2023, 2024), months = setOf(3, 4)),
            primaryCount = 23,
            secondaryCount = 20,
            onEditPrimary = {},
            onEditSecondary = {},
            onSwap = {}
        )
    }
}

@Preview(name = "Metric rows", showBackground = true, widthDp = 400)
@Composable
private fun PreviewPeriodMetricRows() {
    CarTrackingAppTheme(darkTheme = false) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricsHeaderRow(primaryLabel = "2025", secondaryLabel = "2024")
            PeriodMetricRow(metric = neutralMetric)
            PeriodMetricRow(metric = improvedMetric)
            PeriodMetricRow(metric = worsenedMetric)
            PeriodMetricRow(metric = noBaselineMetric)
        }
    }
}

@Preview(name = "Metric rows - dark", showBackground = true, widthDp = 400)
@Composable
private fun PreviewPeriodMetricRowsDark() {
    CarTrackingAppTheme(darkTheme = true) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricsHeaderRow(primaryLabel = "Mar, Apr · 2025", secondaryLabel = "Mar, Apr · 2024")
            PeriodMetricRow(metric = improvedMetric)
            PeriodMetricRow(metric = worsenedMetric)
        }
    }
}

@Preview(name = "Notices", showBackground = true, widthDp = 400)
@Composable
private fun PreviewPeriodComparisonNotice() {
    CarTrackingAppTheme(darkTheme = false) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            PeriodComparisonNotice(text = stringResource(R.string.period_comparison_no_data))
            PeriodComparisonNotice(text = stringResource(R.string.period_comparison_one_sided))
        }
    }
}
