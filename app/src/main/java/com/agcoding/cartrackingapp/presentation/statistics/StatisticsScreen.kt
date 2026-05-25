package com.agcoding.cartrackingapp.presentation.statistics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Route
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.domain.model.Car
import com.agcoding.cartrackingapp.domain.model.GlobalStatistics
import com.agcoding.cartrackingapp.domain.model.MonthlyTrend
import com.agcoding.cartrackingapp.presentation.components.StyledCard
import com.agcoding.cartrackingapp.presentation.components.StyledTopAppBar
import com.agcoding.cartrackingapp.presentation.statistics.components.StatisticsContent
import com.agcoding.cartrackingapp.presentation.statistics.components.SummaryCard
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    onNavigateBack: (() -> Unit)? = null,
    onConsumptionGraphClick: () -> Unit = {},
    onDistanceGraphClick: () -> Unit = {},
    onCostGraphClick: () -> Unit = {},
    onRefillsGraphClick: () -> Unit = {},
    onMonthlyTrendsClick: () -> Unit = {},
    onYearlyComparisonClick: () -> Unit = {},
    onCarComparisonClick: () -> Unit = {},
    onInsightsClick: () -> Unit = {},
    onFuelForecastClick: () -> Unit = {},
    onTripAnalyticsClick: () -> Unit = {},
    onMonthClick: (month: Int, year: Int) -> Unit = { _, _ -> },
    viewModel: StatisticsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val forecastingEnabled by viewModel.forecastingEnabled.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            StyledTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
                title = { Text(stringResource(R.string.statistics)) },
                navigationIcon = {
                    if (onNavigateBack != null) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.back)
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is StatisticsUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is StatisticsUiState.Success -> {
                StatisticsContent(
                    statistics = state.statistics,
                    onMonthlyTrendsClick = onMonthlyTrendsClick,
                    onYearlyComparisonClick = onYearlyComparisonClick,
                    onCarComparisonClick = onCarComparisonClick,
                    onFuelForecastClick = onFuelForecastClick,
                    onInsightsClick = onInsightsClick,
                    forecastingEnabled = forecastingEnabled,
                    summarySection = {
                        SummarySection(
                            statistics = state.statistics,
                            onConsumptionGraphClick = onConsumptionGraphClick,
                            onDistanceGraphClick = onDistanceGraphClick,
                            onCostGraphClick = onCostGraphClick,
                            onRefillsGraphClick = onRefillsGraphClick,
                            onTripAnalyticsClick = onTripAnalyticsClick
                        )
                    },
                    perCarBreakdownCards = {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            state.statistics.perCarStatistics.forEach { carStats ->
                                PerCarBreakdownCard(carStats)
                            }
                        }
                    },
                    monthlyTrendCards = {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            val displayTrends = state.statistics.monthlyTrends.take(5)
                            displayTrends.forEach { trend ->
                                MonthlyTrendCard(
                                    trend = trend,
                                    onClick = { onMonthClick(trend.month, trend.year) }
                                )
                            }
                        }
                    },
                    modifier = Modifier.padding(paddingValues)
                )
            }

            is StatisticsUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(state.message)
                }
            }
        }
    }
}

@Composable
private fun SummarySection(
    statistics: GlobalStatistics,
    onConsumptionGraphClick: () -> Unit,
    onDistanceGraphClick: () -> Unit,
    onCostGraphClick: () -> Unit,
    onRefillsGraphClick: () -> Unit,
    onTripAnalyticsClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // First row - Total cost and consumption
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SummaryCard(
                icon = Icons.Default.AttachMoney,
                title = stringResource(R.string.total_cost),
                value = "€${String.format("%.2f", statistics.totalCost)}",
                subtitle = stringResource(R.string.fuel_plus_expenses),
                modifier = Modifier.weight(1f),
                onClick = onCostGraphClick
            )
            SummaryCard(
                icon = Icons.AutoMirrored.Filled.TrendingUp,
                title = stringResource(R.string.avg_consumption),
                value = "${String.format("%.1f", statistics.averageConsumption)} L/100km",
                modifier = Modifier.weight(1f),
                onClick = onConsumptionGraphClick
            )
        }

        // Second row - Distance and refills
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SummaryCard(
                icon = Icons.Default.Route,
                title = stringResource(R.string.total_distance),
                value = "${String.format("%.0f", statistics.totalDistance)} km",
                modifier = Modifier.weight(1f),
                onClick = onDistanceGraphClick
            )
            SummaryCard(
                icon = Icons.Default.LocalGasStation,
                title = stringResource(R.string.total_refills),
                value = "${statistics.totalRefills}",
                modifier = Modifier.weight(1f),
                onClick = onRefillsGraphClick
            )
        }

        // Third row - Cost per Kilometer
        if (statistics.costPerKilometer > 0) {
            SummaryCard(
                icon = Icons.Default.AttachMoney,
                title = stringResource(R.string.cost_per_kilometer),
                value = "€${String.format("%.2f", statistics.costPerKilometer)}/km",
                subtitle = stringResource(R.string.average_cost_subtitle),
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Cost Breakdown Section
        if (statistics.totalExpensesCost > 0) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.cost_breakdown),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            CostBreakdownCard(statistics)
        }

        // Expense Statistics
        if (statistics.totalExpenseCount > 0) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.expenses_overview),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ExpenseCard(
                    title = stringResource(R.string.service),
                    cost = statistics.totalServiceExpenses,
                    count = statistics.serviceExpenseCount,
                    modifier = Modifier.weight(1f)
                )
                ExpenseCard(
                    title = stringResource(R.string.other),
                    cost = statistics.totalOtherExpenses,
                    count = statistics.otherExpenseCount,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Trips Statistics Section
        if (statistics.totalTrips > 0) {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.trips_overview),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                androidx.compose.material3.TextButton(onClick = onTripAnalyticsClick) {
                    Text(
                        text = stringResource(R.string.trips_overview_show_all),
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 13.sp
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TripStatsCard(
                    title = stringResource(R.string.total_trips_stat),
                    value = "${statistics.totalTrips}",
                    subtitle = pluralStringResource(R.plurals.refills_count, statistics.tripRefillCount, statistics.tripRefillCount),
                    modifier = Modifier.weight(1f)
                )
                TripStatsCard(
                    title = stringResource(R.string.trip_distance_stat),
                    value = "${String.format("%.0f", statistics.tripDistance)} km",
                    subtitle = stringResource(
                        R.string.percentage_of_total,
                        String.format("%.1f%%", if (statistics.totalDistance > 0) (statistics.tripDistance / statistics.totalDistance) * 100 else 0.0)
                    ),
                    modifier = Modifier.weight(1f)
                )
            }

            if (statistics.tripAverageConsumption > 0) {
                TripStatsCard(
                    title = stringResource(R.string.trip_avg_consumption_stat),
                    value = "${String.format("%.1f", statistics.tripAverageConsumption)} L/100km",
                    subtitle = stringResource(R.string.trip_avg_consumption_subtitle),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}


@Composable
private fun CostBreakdownCard(statistics: GlobalStatistics) {
    StyledCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val fuelCost = (statistics.totalCost - statistics.totalExpensesCost).coerceAtLeast(0.0)

            // Visual breakdown bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(24.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                if (statistics.totalCost > 0) {
                    val fuelWeight = (fuelCost / statistics.totalCost).toFloat()
                    val serviceWeight = (statistics.totalServiceExpenses / statistics.totalCost).toFloat()
                    val otherWeight = (statistics.totalOtherExpenses / statistics.totalCost).toFloat()

                    if (fuelWeight > 0) {
                        Box(modifier = Modifier.fillMaxHeight().weight(fuelWeight.coerceAtLeast(0.01f)).background(MaterialTheme.colorScheme.primary))
                    }
                    if (serviceWeight > 0) {
                        Box(modifier = Modifier.fillMaxHeight().weight(serviceWeight.coerceAtLeast(0.01f)).background(MaterialTheme.colorScheme.tertiary))
                    }
                    if (otherWeight > 0) {
                        Box(modifier = Modifier.fillMaxHeight().weight(otherWeight.coerceAtLeast(0.01f)).background(MaterialTheme.colorScheme.secondary))
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                val fuelPercentage = if (statistics.totalCost > 0) (fuelCost / statistics.totalCost) * 100 else 0.0

                CostBreakdownItem(
                    label = stringResource(R.string.fuel_cost),
                    amount = fuelCost,
                    percentage = fuelPercentage,
                    color = MaterialTheme.colorScheme.primary
                )
                CostBreakdownItem(
                    label = stringResource(R.string.service_expenses),
                    amount = statistics.totalServiceExpenses,
                    percentage = if (statistics.totalCost > 0) (statistics.totalServiceExpenses / statistics.totalCost) * 100 else 0.0,
                    color = MaterialTheme.colorScheme.tertiary
                )
                CostBreakdownItem(
                    label = stringResource(R.string.other_expenses),
                    amount = statistics.totalOtherExpenses,
                    percentage = if (statistics.totalCost > 0) (statistics.totalOtherExpenses / statistics.totalCost) * 100 else 0.0,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@Composable
private fun CostBreakdownItem(
    label: String,
    amount: Double,
    percentage: Double,
    color: androidx.compose.ui.graphics.Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(color, shape = RoundedCornerShape(2.dp))
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = "€${String.format("%.2f", amount)} (${String.format("%.1f", percentage)}%)",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun ExpenseCard(
    title: String,
    cost: Double,
    count: Int,
    modifier: Modifier = Modifier
) {
    StyledCard(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                text = title,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "€${String.format("%.2f", cost)}",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = pluralStringResource(R.plurals.expense_lowercase_count, count, count),
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TripStatsCard(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    StyledCard(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                text = title,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MonthlyTrendCard(
    trend: MonthlyTrend,
    onClick: () -> Unit = {}
) {
    StyledCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${trend.monthName} ${trend.year}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "€${String.format("%.2f", trend.totalCombinedCost)}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Show fuel vs expense breakdown if there are expenses
            if (trend.expenseCost > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(
                            R.string.fuel_label_format,
                            String.format("%.2f", trend.totalCost)
                        ),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = stringResource(
                            R.string.expenses_label_format,
                            String.format("%.2f", trend.expenseCost)
                        ),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = pluralStringResource(
                        R.plurals.refills_count,
                        trend.refillCount,
                        trend.refillCount
                    ),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${String.format("%.1f", trend.totalLiters)} L",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${String.format("%.0f", trend.totalDistance)} km",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (trend.averageConsumption > 0) {
                    Text(
                        text = "${String.format("%.1f", trend.averageConsumption)} L/100km",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (trend.expenseCount > 0) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = pluralStringResource(
                        R.plurals.expense_lowercase_count,
                        trend.expenseCount,
                        trend.expenseCount
                    ),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}


@Composable
private fun PerCarBreakdownCard(
    carStats: com.agcoding.cartrackingapp.domain.model.CarStatistics
) {
    StyledCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Car name and license plate
            Text(
                text = carStats.car.name,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = carStats.car.licensePlate,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Three-column grid for stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Column 1
                Column(modifier = Modifier.weight(1f)) {
                    StatItem(
                        label = stringResource(R.string.total_cost_label),
                        value = "€${String.format("%.2f", carStats.totalCost)}"
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    StatItem(
                        label = stringResource(R.string.refills_label),
                        value = "${carStats.totalRefills}"
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    StatItem(
                        label = stringResource(R.string.consumption_label),
                        value = "${String.format("%.1f", carStats.averageConsumption)} L/100km"
                    )
                }

                // Column 2
                Column(modifier = Modifier.weight(1f)) {
                    StatItem(
                        label = stringResource(R.string.distance_label),
                        value = "${String.format("%.0f", carStats.totalDistance)} km"
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    StatItem(
                        label = stringResource(R.string.service_label),
                        value = "€${String.format("%.2f", carStats.serviceExpensesCost)}"
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    StatItem(
                        label = stringResource(R.string.other_label),
                        value = "€${String.format("%.2f", carStats.otherExpensesCost)}"
                    )
                }

                // Column 3
                Column(modifier = Modifier.weight(1f)) {
                    StatItem(
                        label = stringResource(R.string.cost_per_km_short),
                        value = "€${String.format("%.3f", carStats.costPerKilometer)}"
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    StatItem(
                        label = stringResource(R.string.services_short),
                        value = "${carStats.serviceExpenseCount}"
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    StatItem(
                        label = stringResource(R.string.others_short),
                        value = "${carStats.otherExpenseCount}"
                    )
                }
            }
        }
    }
}

// ============================================
// Preview Composables
// ============================================

@Preview(name = "Statistics Screen - Light", showBackground = true, showSystemUi = true)
@Composable
private fun PreviewStatisticsScreen() {
    val mockStats = GlobalStatistics(
        totalCars = 2,
        totalRefills = 25,
        totalCost = 1250.50,
        totalDistance = 12500.0,
        totalLiters = 950.5,
        averageConsumption = 7.6,
        averagePricePerLiter = 1.58,
        mostEfficientCar = Car(1, "Toyota Corolla", "ABC-1234", 0.0, 12500.0),
        mostExpensiveCar = Car(2, "Honda Civic", "XYZ-5678", 0.0, 8000.0),
        monthlyTrends = listOf(
            MonthlyTrend(1, 2026, "January", 320.50, 200.0, 2500.0, 8.0, 6, 2, 80.0),
            MonthlyTrend(12, 2025, "December", 280.00, 180.0, 2200.0, 8.2, 5, 1, 50.0)
        ),
        perCarStatistics = emptyList(),
        totalServiceExpenses = 150.0,
        totalOtherExpenses = 80.0,
        costPerKilometer = 0.10,
        totalExpensesCost = 230.0,
        totalExpenseCount = 3,
        serviceExpenseCount = 1,
        otherExpenseCount = 2,
        totalTrips = 5,
        tripRefillCount = 3,
        tripDistance = 1200.0,
        tripAverageConsumption = 8.2
    )

    CarTrackingAppTheme(darkTheme = false) {
        Scaffold(
            topBar = {
                StyledTopAppBar(
                    title = { Text(stringResource(R.string.statistics)) }
                )
            }
        ) { paddingValues ->
            StatisticsContent(
                statistics = mockStats,
                onMonthlyTrendsClick = {},
                forecastingEnabled = true,
                summarySection = {
                    SummarySection(
                        statistics = mockStats,
                        onConsumptionGraphClick = {},
                        onDistanceGraphClick = {},
                        onCostGraphClick = {},
                        onRefillsGraphClick = {}
                    )
                },
                perCarBreakdownCards = {},
                monthlyTrendCards = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        mockStats.monthlyTrends.forEach { trend ->
                            MonthlyTrendCard(trend = trend)
                        }
                    }
                },
                modifier = Modifier.padding(paddingValues)
            )
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column {
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
