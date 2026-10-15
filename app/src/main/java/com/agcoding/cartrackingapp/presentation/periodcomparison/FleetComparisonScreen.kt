@file:OptIn(ExperimentalMaterial3Api::class)

package com.agcoding.cartrackingapp.presentation.periodcomparison

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.draw.clip
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
import com.agcoding.cartrackingapp.domain.model.Car
import com.agcoding.cartrackingapp.domain.model.DateFilter
import com.agcoding.cartrackingapp.presentation.components.AppFilterChip
import com.agcoding.cartrackingapp.presentation.components.DateFilterSheet
import com.agcoding.cartrackingapp.presentation.components.FilterChipRow
import com.agcoding.cartrackingapp.presentation.components.StyledCard
import com.agcoding.cartrackingapp.presentation.components.StyledTopAppBar
import com.agcoding.cartrackingapp.presentation.components.dateFilterLabel
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppColorScheme
import com.agcoding.cartrackingapp.shared.ui.tokens.AppNumbers
import com.agcoding.cartrackingapp.util.formatMoney
import java.time.Month
import java.time.format.TextStyle
import java.util.Locale

/**
 * Two periods compared across the fleet, reached from the statistics screen.
 *
 * The same comparison the car details screen offers, with a car filter on top: no chip
 * selected means every car, and the user can select any number of them — including a
 * single car — to compare that scope across the two periods.
 */
@Composable
fun FleetComparisonScreen(
    onNavigateBack: () -> Unit,
    viewModel: FleetComparisonViewModel = hiltViewModel()
) {
    val comparison by viewModel.comparison.collectAsStateWithLifecycle()
    val primaryFilter by viewModel.primaryFilter.collectAsStateWithLifecycle()
    val secondaryFilter by viewModel.secondaryFilter.collectAsStateWithLifecycle()
    val availableYears by viewModel.availableYears.collectAsStateWithLifecycle()
    val monthlyBreakdown by viewModel.monthlyBreakdown.collectAsStateWithLifecycle()
    val cars by viewModel.cars.collectAsStateWithLifecycle()
    val selectedCarIds by viewModel.selectedCarIds.collectAsStateWithLifecycle()

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
                        Text(
                            text = carScopeLabel(cars, selectedCarIds),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            CarFilterChips(
                cars = cars,
                selectedCarIds = selectedCarIds,
                onCarToggled = viewModel::toggleCar,
                onAllSelected = viewModel::clearCarFilter,
                modifier = Modifier.padding(bottom = 10.dp)
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                periodComparisonItems(
                    comparison = comparison,
                    primaryFilter = primaryFilter,
                    secondaryFilter = secondaryFilter,
                    onEditPrimary = { editingSide = ComparisonSide.PRIMARY },
                    onEditSecondary = { editingSide = ComparisonSide.SECONDARY },
                    onSwap = viewModel::swapPeriods
                )

                if (comparison.hasData && monthlyBreakdown.hasData) {
                    item(key = "monthly_breakdown") {
                        MonthlyBreakdownCard(
                            breakdown = monthlyBreakdown,
                            primaryFilter = primaryFilter,
                            secondaryFilter = secondaryFilter,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        }
    }

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

/** "All cars", the car's own name, or how many were picked. */
@Composable
private fun carScopeLabel(cars: List<Car>, selectedCarIds: Set<Long>): String = when {
    selectedCarIds.isEmpty() -> stringResource(R.string.period_comparison_all_cars)
    selectedCarIds.size == 1 -> cars.firstOrNull { it.id == selectedCarIds.first() }?.name
        ?: stringResource(R.string.period_comparison_cars_selected, 1)

    else -> stringResource(R.string.period_comparison_cars_selected, selectedCarIds.size)
}

@Composable
private fun CarFilterChips(
    cars: List<Car>,
    selectedCarIds: Set<Long>,
    onCarToggled: (Long) -> Unit,
    onAllSelected: () -> Unit,
    modifier: Modifier = Modifier
) {
    // With a single car there is nothing to choose between.
    if (cars.size <= 1) return

    FilterChipRow(modifier = modifier) {
        AppFilterChip(
            label = stringResource(R.string.filter_all),
            selected = selectedCarIds.isEmpty(),
            onClick = onAllSelected
        )
        cars.forEach { car ->
            AppFilterChip(
                label = car.name,
                selected = car.id in selectedCarIds,
                onClick = { onCarToggled(car.id) }
            )
        }
    }
}

/**
 * How the spending of each period is spread across the year.
 *
 * Whole-year periods are grouped into quarters to keep the table short; as soon as the
 * user narrows either period to specific months, the rows switch to those months — with
 * twelve quarters' worth of empty rows there would be nothing to see otherwise.
 *
 * Three columns of money became two bars per row. The question this card answers is
 * not "what was March" — that figure is in the table above — it is "which months are
 * the expensive ones, and did that shape change". A column of right-aligned euro
 * amounts hides that; two bars drawn against the same scale show it without the eye
 * doing arithmetic. The figures stay on the ends of the bars, so nothing is lost.
 */
@Composable
private fun MonthlyBreakdownCard(
    breakdown: MonthlyBreakdown,
    primaryFilter: DateFilter,
    secondaryFilter: DateFilter,
    modifier: Modifier = Modifier
) {
    val scheme = LocalAppColorScheme.current
    val byQuarter = primaryFilter.months.isEmpty() && secondaryFilter.months.isEmpty()
    val locale = Locale.getDefault()
    val rows = remember(breakdown, byQuarter, locale) {
        breakdownRows(breakdown, byQuarter, locale)
    }
    if (rows.isEmpty()) return

    // One scale for every bar on the card. Scaling each row to its own maximum
    // would make a 40 € month and a 400 € month draw the same width.
    val max = rows.maxOf { maxOf(it.primary, it.secondary) }

    StyledCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.period_comparison_monthly_breakdown),
                style = MaterialTheme.typography.titleSmall,
                color = scheme.contentPrimary
            )
            Spacer(modifier = Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                BreakdownLegend(
                    label = dateFilterLabel(primaryFilter),
                    color = scheme.actionPrimary
                )
                Spacer(modifier = Modifier.width(16.dp))
                BreakdownLegend(
                    label = dateFilterLabel(secondaryFilter),
                    color = scheme.contentDisabled
                )
            }
            Spacer(modifier = Modifier.height(14.dp))

            rows.forEachIndexed { index, row ->
                if (index > 0) Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.Top) {
                    Text(
                        text = row.label,
                        modifier = Modifier.width(34.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = scheme.contentSecondary,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        BreakdownBar(
                            value = row.primary,
                            max = max,
                            color = scheme.actionPrimary
                        )
                        BreakdownBar(
                            value = row.secondary,
                            max = max,
                            color = scheme.contentDisabled
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BreakdownLegend(label: String, color: Color) {
    val scheme = LocalAppColorScheme.current
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(width = 14.dp, height = 4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = scheme.contentSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun BreakdownBar(value: Double, max: Double, color: Color) {
    val scheme = LocalAppColorScheme.current
    val fraction = if (max > 0.0) (value / max).toFloat().coerceIn(0f, 1f) else 0f

    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(scheme.backgroundSecondary)
        ) {
            if (fraction > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(fraction)
                        .clip(RoundedCornerShape(4.dp))
                        .background(color)
                )
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = value.formatMoney(0),
            modifier = Modifier.width(62.dp),
            style = AppNumbers.tag,
            color = if (value > 0.0) scheme.contentPrimary else scheme.contentDisabled,
            textAlign = TextAlign.End,
            maxLines = 1
        )
    }
}

private data class BreakdownRow(val label: String, val primary: Double, val secondary: Double)

private fun breakdownRows(
    breakdown: MonthlyBreakdown,
    byQuarter: Boolean,
    locale: Locale
): List<BreakdownRow> = if (byQuarter) {
    (1..4).map { quarter ->
        val months = (quarter * 3 - 2)..(quarter * 3)
        BreakdownRow(
            label = "Q$quarter",
            primary = months.sumOf { breakdown.primary[it] ?: 0.0 },
            secondary = months.sumOf { breakdown.secondary[it] ?: 0.0 }
        )
    }
} else {
    (breakdown.primary.keys + breakdown.secondary.keys).sorted().map { month ->
        BreakdownRow(
            label = Month.of(month).getDisplayName(TextStyle.SHORT_STANDALONE, locale)
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() },
            primary = breakdown.primary[month] ?: 0.0,
            secondary = breakdown.secondary[month] ?: 0.0
        )
    }
}

// ============================================
// Preview Composables
// ============================================

@Preview(name = "Monthly breakdown - quarters", showBackground = true, widthDp = 400)
@Composable
private fun PreviewMonthlyBreakdownQuarters() {
    CarTrackingAppTheme(darkTheme = false) {
        MonthlyBreakdownCard(
            breakdown = MonthlyBreakdown(
                primary = mapOf(1 to 210.0, 2 to 180.0, 5 to 260.0, 11 to 320.0),
                secondary = mapOf(1 to 190.0, 3 to 240.0, 6 to 205.0, 12 to 280.0)
            ),
            primaryFilter = DateFilter.of(year = 2025),
            secondaryFilter = DateFilter.of(year = 2024)
        )
    }
}

@Preview(name = "Monthly breakdown - months", showBackground = true, widthDp = 400)
@Composable
private fun PreviewMonthlyBreakdownMonths() {
    CarTrackingAppTheme(darkTheme = true) {
        MonthlyBreakdownCard(
            breakdown = MonthlyBreakdown(
                primary = mapOf(3 to 210.0, 4 to 180.0),
                secondary = mapOf(3 to 190.0, 4 to 240.0)
            ),
            primaryFilter = DateFilter(years = setOf(2025), months = setOf(3, 4)),
            secondaryFilter = DateFilter(years = setOf(2024), months = setOf(3, 4))
        )
    }
}

@Preview(name = "Car chips", showBackground = true, widthDp = 400)
@Composable
private fun PreviewCarFilterChips() {
    CarTrackingAppTheme(darkTheme = false) {
        CarFilterChips(
            cars = listOf(
                Car(
                    id = 1,
                    name = "Corolla",
                    licensePlate = "ABC-1234",
                    currentOdometer = 120_000.0,
                    initialOdometer = 0.0
                ),
                Car(
                    id = 2,
                    name = "Golf",
                    licensePlate = "XYZ-9876",
                    currentOdometer = 84_000.0,
                    initialOdometer = 0.0
                )
            ),
            selectedCarIds = setOf(1),
            onCarToggled = {},
            onAllSelected = {}
        )
    }
}
