@file:OptIn(ExperimentalMaterial3Api::class)

package com.agcoding.cartrackingapp.presentation.periodcomparison

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import com.agcoding.cartrackingapp.presentation.components.DateFilterSheet
import com.agcoding.cartrackingapp.presentation.components.StyledCard
import com.agcoding.cartrackingapp.presentation.components.StyledTopAppBar
import com.agcoding.cartrackingapp.presentation.components.dateFilterLabel
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme
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
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
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
                    onEditSecondary = { editingSide = ComparisonSide.SECONDARY }
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

    Row(
        modifier = modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FilterChip(
            selected = selectedCarIds.isEmpty(),
            onClick = onAllSelected,
            label = { Text(stringResource(R.string.filter_all)) },
            leadingIcon = if (selectedCarIds.isEmpty()) {
                {
                    Icon(
                        Icons.Default.DirectionsCar,
                        contentDescription = null,
                        modifier = Modifier.size(FilterChipDefaults.IconSize)
                    )
                }
            } else null
        )
        cars.forEach { car ->
            val isSelected = car.id in selectedCarIds
            FilterChip(
                selected = isSelected,
                onClick = { onCarToggled(car.id) },
                label = { Text(car.name) },
                leadingIcon = if (isSelected) {
                    {
                        Icon(
                            Icons.Default.DirectionsCar,
                            contentDescription = null,
                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                        )
                    }
                } else null
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
 */
@Composable
private fun MonthlyBreakdownCard(
    breakdown: MonthlyBreakdown,
    primaryFilter: DateFilter,
    secondaryFilter: DateFilter,
    modifier: Modifier = Modifier
) {
    val byQuarter = primaryFilter.months.isEmpty() && secondaryFilter.months.isEmpty()
    val locale = Locale.getDefault()
    val rows = remember(breakdown, byQuarter, locale) {
        breakdownRows(breakdown, byQuarter, locale)
    }
    if (rows.isEmpty()) return

    StyledCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.period_comparison_monthly_breakdown),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = stringResource(R.string.date_filter_month),
                    modifier = Modifier.weight(1f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = dateFilterLabel(primaryFilter),
                    modifier = Modifier.weight(1.2f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.End,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = dateFilterLabel(secondaryFilter),
                    modifier = Modifier.weight(1.2f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.End,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.height(8.dp))

            rows.forEach { row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Text(
                        text = row.label,
                        modifier = Modifier.weight(1f),
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = row.primary.formatMoney(0),
                        modifier = Modifier.weight(1.2f),
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.End,
                        maxLines = 1
                    )
                    Text(
                        text = row.secondary.formatMoney(0),
                        modifier = Modifier.weight(1.2f),
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.End,
                        maxLines = 1
                    )
                }
            }
        }
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
