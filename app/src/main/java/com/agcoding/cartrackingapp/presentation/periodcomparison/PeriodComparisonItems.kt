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
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.HorizontalDivider
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

    item(key = "metrics_header") {
        MetricsHeaderRow(
            primaryLabel = dateFilterLabel(primaryFilter),
            secondaryLabel = dateFilterLabel(secondaryFilter)
        )
    }

    // One card holding every metric, rather than a card each. A comparison is
    // read across and down — the eye needs the A column to line up from row to
    // row, and eight separate cards with eight sets of rounded corners between
    // them is the one layout that stops it doing that.
    item(key = "metrics") {
        GroupedListContainer {
            comparison.metrics.forEachIndexed { index, metric ->
                if (index > 0) GroupedDivider(startInset = false)
                PeriodMetricRow(metric = metric)
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

    Column(modifier = Modifier.fillMaxWidth()) {
        PeriodPickerCard(
            label = stringResource(R.string.period_comparison_primary),
            accent = scheme.actionPrimary,
            filter = primaryFilter,
            transactionCount = primaryCount,
            onClick = onEditPrimary
        )
        if (onSwap != null) {
            SwapRow(onSwap = onSwap)
        } else {
            Spacer(modifier = Modifier.height(10.dp))
        }
        PeriodPickerCard(
            label = stringResource(R.string.period_comparison_secondary),
            accent = scheme.contentPrimary,
            filter = secondaryFilter,
            transactionCount = secondaryCount,
            onClick = onEditSecondary
        )
    }
}

@Composable
private fun PeriodPickerCard(
    label: String,
    accent: Color,
    filter: DateFilter,
    transactionCount: Int,
    onClick: () -> Unit
) {
    val scheme = LocalAppColorScheme.current
    val dimens = LocalAppDimens.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(dimens.radius.card))
            .background(scheme.backgroundCard)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        // A stub of colour rather than a coloured heading: the label has to
        // stay readable, and the accent is what ties this card to its column
        // in the table below.
        Box(
            modifier = Modifier
                .size(width = 3.dp, height = 34.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(accent)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = scheme.contentSecondary,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = dateFilterLabel(filter),
                style = MaterialTheme.typography.titleMedium,
                color = scheme.contentPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = stringResource(R.string.period_comparison_record_count, transactionCount),
            style = AppNumbers.tag,
            color = scheme.contentSecondary,
            textAlign = TextAlign.End,
            maxLines = 1
        )
    }
}

/** The line between the two periods, with the exchange on it. */
@Composable
private fun SwapRow(onSwap: () -> Unit) {
    val scheme = LocalAppColorScheme.current
    val dimens = LocalAppDimens.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(46.dp)
    ) {
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            thickness = dimens.size.divider,
            color = scheme.borderDefault
        )
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .size(36.dp)
                .clip(CircleShape)
                .background(scheme.actionSecondary)
                .clickable(onClick = onSwap)
        ) {
            Icon(
                imageVector = Icons.Default.SwapVert,
                contentDescription = stringResource(R.string.period_comparison_swap),
                tint = scheme.actionPrimary,
                modifier = Modifier.size(19.dp)
            )
        }
        HorizontalDivider(
            modifier = Modifier.weight(1f),
            thickness = dimens.size.divider,
            color = scheme.borderDefault
        )
    }
}

/** Names the three columns once, so every metric row below can stay bare numbers. */
@Composable
private fun MetricsHeaderRow(primaryLabel: String, secondaryLabel: String) {
    val scheme = LocalAppColorScheme.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, start = 16.dp, end = 16.dp)
    ) {
        Text(
            text = primaryLabel,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.labelMedium,
            color = scheme.actionPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = secondaryLabel,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.labelMedium,
            color = scheme.contentPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = stringResource(R.string.period_comparison_difference),
            modifier = Modifier.weight(1.2f),
            style = MaterialTheme.typography.labelMedium,
            color = scheme.contentSecondary,
            textAlign = TextAlign.End,
            maxLines = 1
        )
    }
}

@Composable
private fun PeriodMetricRow(metric: PeriodMetric) {
    val scheme = LocalAppColorScheme.current
    Box(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text(
                text = metricLabel(metric.key),
                style = MaterialTheme.typography.bodySmall,
                color = scheme.contentSecondary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatMetric(metric.key, metric.primary),
                    modifier = Modifier.weight(1f),
                    style = AppNumbers.compact,
                    color = scheme.actionPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = formatMetric(metric.key, metric.secondary),
                    modifier = Modifier.weight(1f),
                    style = AppNumbers.compact,
                    color = scheme.contentPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                DeltaCell(metric = metric, modifier = Modifier.weight(1.2f))
            }
        }
    }
}

/**
 * The difference between the two periods: the absolute change on top, the relative
 * change below.
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

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = when (metric.direction) {
                    ComparisonDirection.UP -> Icons.AutoMirrored.Filled.TrendingUp
                    ComparisonDirection.DOWN -> Icons.AutoMirrored.Filled.TrendingDown
                    ComparisonDirection.FLAT -> Icons.AutoMirrored.Filled.TrendingFlat
                },
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = signed(metric.delta, formatMetric(metric.key, metric.delta)),
                style = AppNumbers.tag,
                color = tint,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Text(
            text = metric.percentChange
                ?.let { signed(it, "${it.formatNumber(1)}%") }
                ?: stringResource(R.string.period_comparison_no_baseline),
            style = MaterialTheme.typography.labelSmall,
            color = scheme.contentSecondary,
            textAlign = TextAlign.End,
            maxLines = 1
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
