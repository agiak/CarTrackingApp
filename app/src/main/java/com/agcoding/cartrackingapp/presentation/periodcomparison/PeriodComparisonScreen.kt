@file:OptIn(ExperimentalMaterial3Api::class)

package com.agcoding.cartrackingapp.presentation.periodcomparison

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingFlat
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.domain.model.ComparisonDirection
import com.agcoding.cartrackingapp.domain.model.DateFilter
import com.agcoding.cartrackingapp.domain.model.PeriodComparison
import com.agcoding.cartrackingapp.domain.model.PeriodMetric
import com.agcoding.cartrackingapp.domain.model.PeriodMetricKey
import com.agcoding.cartrackingapp.domain.model.PeriodStatistics
import com.agcoding.cartrackingapp.presentation.components.DateFilterButton
import com.agcoding.cartrackingapp.presentation.components.DateFilterSheet
import com.agcoding.cartrackingapp.presentation.components.StyledCard
import com.agcoding.cartrackingapp.presentation.components.StyledTopAppBar
import com.agcoding.cartrackingapp.presentation.components.dateFilterLabel
import com.agcoding.cartrackingapp.presentation.theme.AppSuccess
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme
import com.agcoding.cartrackingapp.util.formatMoney
import com.agcoding.cartrackingapp.util.formatNumber

/** Which of the two periods a picker is editing. */
private enum class ComparisonSide { PRIMARY, SECONDARY }

/**
 * Two periods of one car's history, side by side.
 *
 * Both periods are chosen with the same year/month picker used everywhere else, so a
 * period can be one or more whole years, or specific months inside them. Each metric
 * shows its value in both periods plus the difference between them.
 */
@Composable
fun PeriodComparisonScreen(
    onNavigateBack: () -> Unit,
    viewModel: PeriodComparisonViewModel = hiltViewModel()
) {
    val comparison by viewModel.comparison.collectAsStateWithLifecycle()
    val primaryFilter by viewModel.primaryFilter.collectAsStateWithLifecycle()
    val secondaryFilter by viewModel.secondaryFilter.collectAsStateWithLifecycle()
    val availableYears by viewModel.availableYears.collectAsStateWithLifecycle()
    val car by viewModel.car.collectAsStateWithLifecycle()

    var editingSide by remember { mutableStateOf<ComparisonSide?>(null) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            StyledTopAppBar(
                title = {
                    Column {
                        Text(
                            text = stringResource(R.string.period_comparison_title),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        val name = car?.name.orEmpty()
                        if (name.isNotBlank()) {
                            Text(
                                text = name,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = viewModel::swapPeriods) {
                        Icon(
                            imageVector = Icons.Default.SwapVert,
                            contentDescription = stringResource(R.string.period_comparison_swap)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        PeriodComparisonContent(
            comparison = comparison,
            primaryFilter = primaryFilter,
            secondaryFilter = secondaryFilter,
            onEditPrimary = { editingSide = ComparisonSide.PRIMARY },
            onEditSecondary = { editingSide = ComparisonSide.SECONDARY },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        )
    }

    // One sheet, pointed at whichever period the user tapped.
    editingSide?.let { side ->
        DateFilterSheet(
            selected = if (side == ComparisonSide.PRIMARY) primaryFilter else secondaryFilter,
            availableYears = availableYears,
            onFilterChange = { filter ->
                when (side) {
                    ComparisonSide.PRIMARY -> viewModel.setPrimaryFilter(filter)
                    ComparisonSide.SECONDARY -> viewModel.setSecondaryFilter(filter)
                }
            },
            onDismiss = { editingSide = null }
        )
    }
}

@Composable
private fun PeriodComparisonContent(
    comparison: PeriodComparison,
    primaryFilter: DateFilter,
    secondaryFilter: DateFilter,
    onEditPrimary: () -> Unit,
    onEditSecondary: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
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
                NoticeCard(text = stringResource(R.string.period_comparison_no_data))
            }
            return@LazyColumn
        }

        // A one-sided comparison is still worth showing, but the deltas are just the
        // whole amount — say so instead of letting "+100%" imply a real trend.
        if (!comparison.isComparable) {
            item(key = "one_sided") {
                NoticeCard(text = stringResource(R.string.period_comparison_one_sided))
            }
        }

        item(key = "metrics_header") {
            MetricsHeaderRow(
                primaryLabel = dateFilterLabel(primaryFilter),
                secondaryLabel = dateFilterLabel(secondaryFilter)
            )
        }

        items(comparison.metrics, key = { it.key.name }) { metric ->
            ComparisonMetricRow(metric = metric)
        }
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
private fun ComparisonMetricRow(metric: PeriodMetric) {
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

@Composable
private fun NoticeCard(text: String) {
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

// ============================================
// Preview Composables
// ============================================

private val sampleComparison = PeriodComparison(
    primary = PeriodStatistics(
        filter = DateFilter.of(year = 2025),
        totalCost = 1842.5,
        fuelCost = 1412.5,
        expensesCost = 430.0,
        totalDistance = 12400.0,
        totalLiters = 848.4,
        averageConsumption = 6.84,
        averagePricePerLiter = 1.66,
        costPerKilometer = 0.149,
        refillCount = 19,
        expenseCount = 4
    ),
    secondary = PeriodStatistics(
        filter = DateFilter.of(year = 2024),
        totalCost = 1610.0,
        fuelCost = 1280.0,
        expensesCost = 330.0,
        totalDistance = 11100.0,
        totalLiters = 792.0,
        averageConsumption = 7.13,
        averagePricePerLiter = 1.62,
        costPerKilometer = 0.145,
        refillCount = 17,
        expenseCount = 3
    )
)

@Preview(name = "Period comparison", showBackground = true, widthDp = 400, heightDp = 900)
@Composable
private fun PreviewPeriodComparison() {
    CarTrackingAppTheme(darkTheme = false) {
        PeriodComparisonContent(
            comparison = sampleComparison,
            primaryFilter = DateFilter.of(year = 2025),
            secondaryFilter = DateFilter.of(year = 2024),
            onEditPrimary = {},
            onEditSecondary = {}
        )
    }
}

@Preview(name = "Period comparison - dark", showBackground = true, widthDp = 400, heightDp = 900)
@Composable
private fun PreviewPeriodComparisonDark() {
    CarTrackingAppTheme(darkTheme = true) {
        PeriodComparisonContent(
            comparison = sampleComparison,
            primaryFilter = DateFilter(years = setOf(2025), months = setOf(3, 4)),
            secondaryFilter = DateFilter(years = setOf(2024), months = setOf(3, 4)),
            onEditPrimary = {},
            onEditSecondary = {}
        )
    }
}

@Preview(name = "Period comparison - empty", showBackground = true, widthDp = 400)
@Composable
private fun PreviewPeriodComparisonEmpty() {
    CarTrackingAppTheme(darkTheme = false) {
        PeriodComparisonContent(
            comparison = PeriodComparison(),
            primaryFilter = DateFilter.of(year = 2025),
            secondaryFilter = DateFilter.of(year = 2024),
            onEditPrimary = {},
            onEditSecondary = {}
        )
    }
}
