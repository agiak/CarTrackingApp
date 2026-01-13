package com.agcoding.cartrackingapp.presentation.statistics

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Route
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.domain.model.GlobalStatistics
import com.agcoding.cartrackingapp.domain.model.MonthlyTrend

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    onNavigateBack: (() -> Unit)? = null,
    onConsumptionGraphClick: () -> Unit = {},
    onDistanceGraphClick: () -> Unit = {},
    onMonthlyTrendsClick: () -> Unit = {},
    onMonthClick: (month: Int, year: Int) -> Unit = { _, _ -> },
    viewModel: StatisticsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.statistics)) },
                navigationIcon = {
                    if (onNavigateBack != null) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back"
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
                // Check if there are no cars/data at all
                if (state.statistics.totalCost == 0.0 &&
                    state.statistics.totalRefills == 0 &&
                    state.statistics.totalDistance == 0.0) {
                    // Empty state
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            modifier = Modifier.padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            // Large icon for visual appeal
                            Icon(
                                imageVector = Icons.Default.QueryStats,
                                contentDescription = null,
                                modifier = Modifier.size(120.dp),
                                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                            )
                            Spacer(modifier = Modifier.height(24.dp))
                            Text(
                                text = stringResource(R.string.car_list_no_cars_title),
                                style = MaterialTheme.typography.titleLarge,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.statistics_no_data_desc),
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                    // Summary section
                    item {
                        Text(
                            text = stringResource(R.string.overall_summary),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    item {
                        SummarySection(
                            statistics = state.statistics,
                            onConsumptionGraphClick = onConsumptionGraphClick,
                            onDistanceGraphClick = onDistanceGraphClick
                        )
                    }

                    // Per-Car Breakdown
                    if (state.statistics.perCarStatistics.isNotEmpty()) {
                        item {
                            Text(
                                text = stringResource(R.string.per_car_breakdown),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        items(state.statistics.perCarStatistics) { carStats ->
                            PerCarBreakdownCard(carStats)
                        }
                    }

                   // Monthly trends (show only last 5 months)
                    if (state.statistics.monthlyTrends.isNotEmpty()) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = stringResource(R.string.monthly_trends),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                if (state.statistics.monthlyTrends.size > 5) {
                                    androidx.compose.material3.TextButton(
                                        onClick = onMonthlyTrendsClick
                                    ) {
                                        Text(
                                            text = stringResource(R.string.see_all),
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }

                        // Show only first 5 months
                        val displayTrends = state.statistics.monthlyTrends.take(5)
                        items(displayTrends) { trend ->
                            MonthlyTrendCard(
                                trend = trend,
                                onClick = { onMonthClick(trend.month, trend.year) }
                            )
                        }

                        // Show "See All" button if there are more than 5 months
                        if (state.statistics.monthlyTrends.size > 5) {
                            item {
                                androidx.compose.material3.OutlinedButton(
                                    onClick = onMonthlyTrendsClick,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(stringResource(R.string.view_all_months_format, state.statistics.monthlyTrends.size))
                                }
                            }
                        }
                    }

                    // Insights section
                    item {
                        Text(
                            text = stringResource(R.string.insights),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    item {
                        InsightsSection(state.statistics)
                    }
                }
                } // Close else block for empty check
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
    onDistanceGraphClick: () -> Unit
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
                modifier = Modifier.weight(1f)
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
                modifier = Modifier.weight(1f)
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

        // Cost per kilometer
        if (statistics.totalDistance > 0) {
            SummaryCard(
                icon = Icons.Default.AttachMoney,
                title = stringResource(R.string.cost_per_kilometer),
                value = "€${String.format("%.3f", statistics.costPerKilometer)}/km",
                subtitle = stringResource(R.string.total_cost_divide_distance),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun SummaryCard(
    icon: ImageVector,
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        onClick = onClick ?: {}
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = title,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun CostBreakdownCard(statistics: GlobalStatistics) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val fuelCost = statistics.totalCost - statistics.totalExpensesCost
            val fuelPercentage = if (statistics.totalCost > 0) (fuelCost / statistics.totalCost) * 100 else 0.0
            val expensePercentage = if (statistics.totalCost > 0) (statistics.totalExpensesCost / statistics.totalCost) * 100 else 0.0

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
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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
private fun MonthlyTrendCard(
    trend: MonthlyTrend,
    onClick: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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
                        text = stringResource(R.string.fuel_label_format, String.format("%.2f", trend.totalCost)),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = stringResource(R.string.expenses_label_format, String.format("%.2f", trend.expenseCost)),
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
                    text = pluralStringResource(R.plurals.refills_count, trend.refillCount, trend.refillCount),
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
                    text = pluralStringResource(R.plurals.expense_lowercase_count, trend.expenseCount, trend.expenseCount),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun InsightsSection(statistics: GlobalStatistics) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Calculate insights
            val avgCostPerRefill = if (statistics.totalRefills > 0) {
                statistics.totalCost / statistics.totalRefills
            } else 0.0

            val avgLitersPerRefill = if (statistics.totalRefills > 0) {
                statistics.totalLiters / statistics.totalRefills
            } else 0.0

            val costPerKm = if (statistics.totalDistance > 0) {
                statistics.totalCost / statistics.totalDistance
            } else 0.0

            InsightRow(
                label = stringResource(R.string.average_cost_per_refill),
                value = "€${String.format("%.2f", avgCostPerRefill)}"
            )

            Spacer(modifier = Modifier.height(12.dp))

            InsightRow(
                label = stringResource(R.string.average_liters_per_refill),
                value = "${String.format("%.1f", avgLitersPerRefill)} L"
            )

            Spacer(modifier = Modifier.height(12.dp))

            InsightRow(
                label = stringResource(R.string.cost_per_km_label),
                value = "€${String.format("%.3f", costPerKm)}"
            )
        }
    }
}

@Composable
private fun InsightRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun PerCarBreakdownCard(
    carStats: com.agcoding.cartrackingapp.domain.model.CarStatistics
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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
                    StatItem(label = stringResource(R.string.total_cost_label), value = "€${String.format("%.2f", carStats.totalCost)}")
                    Spacer(modifier = Modifier.height(8.dp))
                    StatItem(label = stringResource(R.string.refills_label), value = "${carStats.totalRefills}")
                    Spacer(modifier = Modifier.height(8.dp))
                    StatItem(label = stringResource(R.string.consumption_label), value = "${String.format("%.1f", carStats.averageConsumption)} L/100km")
                }

                // Column 2
                Column(modifier = Modifier.weight(1f)) {
                    StatItem(label = stringResource(R.string.distance_label), value = "${String.format("%.0f", carStats.totalDistance)} km")
                    Spacer(modifier = Modifier.height(8.dp))
                    StatItem(label = stringResource(R.string.service_label), value = "€${String.format("%.2f", carStats.serviceExpensesCost)}")
                    Spacer(modifier = Modifier.height(8.dp))
                    StatItem(label = stringResource(R.string.other_label), value = "€${String.format("%.2f", carStats.otherExpensesCost)}")
                }

                // Column 3
                Column(modifier = Modifier.weight(1f)) {
                    StatItem(label = stringResource(R.string.cost_per_km_short), value = "€${String.format("%.3f", carStats.costPerKilometer)}")
                    Spacer(modifier = Modifier.height(8.dp))
                    StatItem(label = stringResource(R.string.services_short), value = "${carStats.serviceExpenseCount}")
                    Spacer(modifier = Modifier.height(8.dp))
                    StatItem(label = stringResource(R.string.others_short), value = "${carStats.otherExpenseCount}")
                }
            }
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

