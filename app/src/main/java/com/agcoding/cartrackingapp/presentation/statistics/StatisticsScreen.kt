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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.EuroSymbol
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
import androidx.compose.ui.draw.clip
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
import com.agcoding.cartrackingapp.presentation.components.Kpi
import com.agcoding.cartrackingapp.presentation.components.KpiGrid
import com.agcoding.cartrackingapp.presentation.components.StyledCard
import com.agcoding.cartrackingapp.presentation.components.NotificationsAction
import com.agcoding.cartrackingapp.presentation.components.ProfileNavigationIcon
import com.agcoding.cartrackingapp.presentation.components.StyledTopAppBar
import com.agcoding.cartrackingapp.presentation.statistics.components.CarsByCostCard
import com.agcoding.cartrackingapp.presentation.statistics.components.CostDonutCard
import com.agcoding.cartrackingapp.presentation.statistics.components.StatisticsContent
import com.agcoding.cartrackingapp.presentation.statistics.components.SummaryCard
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme
import com.agcoding.cartrackingapp.util.formatMoney
import com.agcoding.cartrackingapp.util.formatMoneyPer
import com.agcoding.cartrackingapp.util.formatNumber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    onNavigateBack: (() -> Unit)? = null,
    onConsumptionGraphClick: () -> Unit = {},
    onDistanceGraphClick: () -> Unit = {},
    onCostGraphClick: () -> Unit = {},
    onRefillsGraphClick: () -> Unit = {},
    onMonthlyTrendsClick: () -> Unit = {},
    onPeriodComparisonClick: () -> Unit = {},
    onCarComparisonClick: () -> Unit = {},
    onInsightsClick: () -> Unit = {},
    onFuelForecastClick: () -> Unit = {},
    onTripAnalyticsClick: () -> Unit = {},
    onMonthClick: (month: Int, year: Int) -> Unit = { _, _ -> },
    /** Opens the dedicated statistics screen for one car from the per-car breakdown. */
    onCarStatisticsClick: (carId: Long) -> Unit = {},
    /** Only wired when this is the tab; as a pushed screen the slot holds Back. */
    onNavigateToProfile: (() -> Unit)? = null,
    onNavigateToNotifications: (() -> Unit)? = null,
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
                    // Back wins the slot when there is somewhere to go back to:
                    // this screen is both a tab and a pushed destination, and a
                    // pushed screen without a Back is a trap.
                    if (onNavigateBack != null) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.back)
                            )
                        }
                    } else if (onNavigateToProfile != null) {
                        ProfileNavigationIcon(onClick = onNavigateToProfile)
                    }
                },
                actions = {
                    if (onNavigateToNotifications != null) {
                        NotificationsAction(onClick = onNavigateToNotifications)
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
                    onPeriodComparisonClick = onPeriodComparisonClick,
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
                        // One ranked list instead of a card per car. Six
                        // figures about one car are a summary; the same six
                        // about four cars are a spreadsheet.
                        CarsByCostCard(
                            cars = state.statistics.perCarStatistics,
                            onCarClick = onCarStatisticsClick,
                        )
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
    // The three cards that used to hold the total, the breakdown and the
    // expense counts are now one ring: they were three pieces of a single
    // answer, and keeping them apart made the reader assemble it by scrolling.
    val fuelCost = (statistics.totalCost - statistics.totalExpensesCost).coerceAtLeast(0.0)
    val unavailable = stringResource(R.string.not_available)

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        CostDonutCard(
            totalCost = statistics.totalCost,
            costPerKilometer = statistics.costPerKilometer,
            fuel = fuelCost,
            service = statistics.totalServiceExpenses,
            other = statistics.totalOtherExpenses,
            serviceCount = statistics.serviceExpenseCount,
            otherCount = statistics.otherExpenseCount,
        )

        // Every cell that has a graph behind it carries a chevron. The four
        // graph screens had no entry point of their own before this - they
        // were reached by tapping a summary card that gave no sign it was
        // tappable.
        KpiGrid(
            items = buildList {
                add(
                    Kpi(
                        label = stringResource(R.string.total_distance),
                        value = statistics.totalDistance.formatNumber(0),
                        unit = "km",
                        onClick = onDistanceGraphClick,
                    )
                )
                add(
                    Kpi(
                        label = stringResource(R.string.avg_consumption),
                        value = if (statistics.averageConsumption > 0) {
                            statistics.averageConsumption.formatNumber(1)
                        } else {
                            unavailable
                        },
                        unit = if (statistics.averageConsumption > 0) "L/100" else null,
                        onClick = onConsumptionGraphClick,
                    )
                )
                add(
                    Kpi(
                        label = stringResource(R.string.total_refills),
                        value = statistics.totalRefills.toString(),
                        onClick = onRefillsGraphClick,
                    )
                )
                add(
                    Kpi(
                        label = stringResource(R.string.cost_per_kilometer),
                        value = if (statistics.costPerKilometer > 0) {
                            statistics.costPerKilometer.formatNumber(2)
                        } else {
                            unavailable
                        },
                        unit = if (statistics.costPerKilometer > 0) "\u20ac" else null,
                        onClick = onCostGraphClick,
                    )
                )
                // Trips are a feature not every user touches; two empty cells
                // would say the app expected something of them.
                if (statistics.totalTrips > 0) {
                    add(
                        Kpi(
                            label = stringResource(R.string.total_trips_stat),
                            value = statistics.totalTrips.toString(),
                            unit = "${statistics.tripDistance.formatNumber(0)} km",
                            onClick = onTripAnalyticsClick,
                        )
                    )
                    add(
                        Kpi(
                            label = stringResource(R.string.trip_avg_consumption_stat),
                            value = if (statistics.tripAverageConsumption > 0) {
                                statistics.tripAverageConsumption.formatNumber(1)
                            } else {
                                unavailable
                            },
                            unit = if (statistics.tripAverageConsumption > 0) "L/100" else null,
                            onClick = onTripAnalyticsClick,
                        )
                    )
                }
            },
        )
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
            text = "${amount.formatMoney()} (${percentage.formatNumber(1)}%)",
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
                text = cost.formatMoney(),
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
                    text = trend.totalCombinedCost.formatMoney(),
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
                            trend.totalCost.formatNumber(2)
                        ),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = stringResource(
                            R.string.expenses_label_format,
                            trend.expenseCost.formatNumber(2)
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
                    text = "${trend.totalLiters.formatNumber(1)} L",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${trend.totalDistance.formatNumber(0)} km",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (trend.averageConsumption > 0) {
                    Text(
                        text = "${trend.averageConsumption.formatNumber(1)} L/100km",
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
