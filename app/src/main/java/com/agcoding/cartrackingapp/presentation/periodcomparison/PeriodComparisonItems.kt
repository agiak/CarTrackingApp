package com.agcoding.cartrackingapp.presentation.periodcomparison

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
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingFlat
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.domain.model.ComparisonDirection
import com.agcoding.cartrackingapp.domain.model.DateFilter
import com.agcoding.cartrackingapp.domain.model.PeriodComparison
import com.agcoding.cartrackingapp.domain.model.PeriodMetric
import com.agcoding.cartrackingapp.domain.model.PeriodMetricKey
import com.agcoding.cartrackingapp.presentation.components.DateFilterButton
import com.agcoding.cartrackingapp.presentation.components.StyledCard
import com.agcoding.cartrackingapp.presentation.components.dateFilterLabel
import com.agcoding.cartrackingapp.presentation.theme.AppSuccess
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
    onEditSecondary: () -> Unit
) {
    item(key = "period_pickers") {
        PeriodPickersCard(
            primaryFilter = primaryFilter,
            secondaryFilter = secondaryFilter,
            primaryCount = comparison.primary.transactionCount,
            secondaryCount = comparison.secondary.transactionCount,
            onEditPrimary = onEditPrimary,
            onEditSecondary = onEditSecondary
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

    items(comparison.metrics, key = { it.key.name }) { metric ->
        PeriodMetricRow(metric = metric)
    }
}

@Composable
private fun PeriodPickersCard(
    primaryFilter: DateFilter,
    secondaryFilter: DateFilter,
    primaryCount: Int,
    secondaryCount: Int,
    onEditPrimary: () -> Unit,
    onEditSecondary: () -> Unit
) {
    StyledCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            PeriodPickerRow(
                label = stringResource(R.string.period_comparison_primary),
                accent = MaterialTheme.colorScheme.primary,
                filter = primaryFilter,
                transactionCount = primaryCount,
                onClick = onEditPrimary
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            PeriodPickerRow(
                label = stringResource(R.string.period_comparison_secondary),
                accent = MaterialTheme.colorScheme.onSurface,
                filter = secondaryFilter,
                transactionCount = secondaryCount,
                onClick = onEditSecondary
            )
        }
    }
}

@Composable
private fun PeriodPickerRow(
    label: String,
    accent: Color,
    filter: DateFilter,
    transactionCount: Int,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = accent
            )
            Text(
                text = stringResource(R.string.transactions_section_header, transactionCount),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        DateFilterButton(filter = filter, onClick = onClick)
    }
}

/** Names the three columns once, so every metric row below can stay bare numbers. */
@Composable
private fun MetricsHeaderRow(primaryLabel: String, secondaryLabel: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, start = 4.dp, end = 4.dp)
    ) {
        Text(
            text = primaryLabel,
            modifier = Modifier.weight(1f),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
            maxLines = 1
        )
        Text(
            text = secondaryLabel,
            modifier = Modifier.weight(1f),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1
        )
        Text(
            text = stringResource(R.string.period_comparison_difference),
            modifier = Modifier.weight(1.2f),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.End,
            maxLines = 1
        )
    }
}

@Composable
private fun PeriodMetricRow(metric: PeriodMetric) {
    StyledCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Text(
                text = metricLabel(metric.key),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = formatMetric(metric.key, metric.primary),
                    modifier = Modifier.weight(1f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = formatMetric(metric.key, metric.secondary),
                    modifier = Modifier.weight(1f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
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
    val tint = when (metric.isImprovement) {
        true -> AppSuccess.color
        false -> MaterialTheme.colorScheme.error
        null -> MaterialTheme.colorScheme.onSurfaceVariant
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
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = tint,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Text(
            text = metric.percentChange
                ?.let { signed(it, "${it.formatNumber(1)}%") }
                ?: stringResource(R.string.period_comparison_no_baseline),
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.End,
            maxLines = 1
        )
    }
}

/** Full-width note explaining why the comparison below is empty or one-sided. */
@Composable
fun PeriodComparisonNotice(text: String) {
    StyledCard(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
