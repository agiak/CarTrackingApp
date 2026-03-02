package com.agcoding.cartrackingapp.presentation.statistics.components

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.domain.model.Car
import com.agcoding.cartrackingapp.domain.model.GlobalStatistics
import com.agcoding.cartrackingapp.domain.model.MonthlyTrend
import com.agcoding.cartrackingapp.presentation.components.StyledCard
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme

/**
 * StatisticsContent component that wraps the statistics display.
 * Provides consistent layout and handles empty state, split view, and single column layouts.
 */
@Composable
fun StatisticsContent(
    statistics: GlobalStatistics,
    onMonthlyTrendsClick: () -> Unit,
    onYearlyComparisonClick: () -> Unit = {},
    onCarComparisonClick: () -> Unit = {},
    onFuelForecastClick: () -> Unit = {},
    forecastingEnabled: Boolean = false,
    summarySection: @Composable () -> Unit,
    perCarBreakdownCards: @Composable () -> Unit,
    monthlyTrendCards: @Composable () -> Unit,
    insightsSection: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp
    val isLandscape = configuration.screenWidthDp > configuration.screenHeightDp
    val useSplitView = screenWidthDp >= 600 || isLandscape

    // Check if there are no cars/data at all
    if (statistics.totalCost == 0.0 &&
        statistics.totalRefills == 0 &&
        statistics.totalDistance == 0.0
    ) {
        // Empty state
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
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
    } else if (useSplitView) {
        val distinctYears = statistics.monthlyTrends.map { it.year }.distinct().size

        // Split view for tablets and landscape
        Row(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Left side: Overall Summary (35%)
            Column(
                modifier = Modifier
                    .weight(0.35f)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.overall_summary),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                summarySection()

                // Analysis shortcuts – compact grid after summary
                AnalysisButtonsGrid(
                    onYearlyComparisonClick = if (distinctYears >= 2) onYearlyComparisonClick else null,
                    onCarComparisonClick = if (statistics.perCarStatistics.size >= 2) onCarComparisonClick else null,
                    onFuelForecastClick = if (forecastingEnabled) onFuelForecastClick else null
                )
            }

            // Right side: Details, Trends, and Insights (65%)
            LazyColumn(
                modifier = Modifier
                    .weight(0.65f)
                    .fillMaxHeight(),
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Per-Car Breakdown
                if (statistics.perCarStatistics.isNotEmpty()) {
                    item {
                        Text(
                            text = stringResource(R.string.per_car_breakdown),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    item {
                        perCarBreakdownCards()
                    }
                }

                // Monthly trends
                if (statistics.monthlyTrends.isNotEmpty()) {
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
                            if (statistics.monthlyTrends.size > 5) {
                                TextButton(onClick = onMonthlyTrendsClick) {
                                    Text(
                                        text = stringResource(R.string.see_all),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }

                    item {
                        monthlyTrendCards()
                    }

                    if (statistics.monthlyTrends.size > 5) {
                        item {
                            OutlinedButton(
                                onClick = onMonthlyTrendsClick,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    contentColor = MaterialTheme.colorScheme.primary
                                ),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                )
                            ) {
                                Text(
                                    stringResource(
                                        R.string.view_all_months_format,
                                        statistics.monthlyTrends.size
                                    )
                                )
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
                    insightsSection()
                }
            }
        }
    } else {
        val distinctYears = statistics.monthlyTrends.map { it.year }.distinct().size

        // Original single-column layout for portrait phones
        LazyColumn(
            modifier = modifier.fillMaxSize(),
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
                summarySection()
            }

            // Analysis shortcuts – compact grid after summary / expenses overview
            item {
                AnalysisButtonsGrid(
                    onYearlyComparisonClick = if (distinctYears >= 2) onYearlyComparisonClick else null,
                    onCarComparisonClick = if (statistics.perCarStatistics.size >= 2) onCarComparisonClick else null,
                    onFuelForecastClick = if (forecastingEnabled) onFuelForecastClick else null
                )
            }

            // Per-Car Breakdown
            if (statistics.perCarStatistics.isNotEmpty()) {
                item {
                    Text(
                        text = stringResource(R.string.per_car_breakdown),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                item {
                    perCarBreakdownCards()
                }
            }

            // Monthly trends
            if (statistics.monthlyTrends.isNotEmpty()) {
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
                        if (statistics.monthlyTrends.size > 5) {
                            TextButton(onClick = onMonthlyTrendsClick) {
                                Text(
                                    text = stringResource(R.string.see_all),
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }

                item {
                    monthlyTrendCards()
                }

                if (statistics.monthlyTrends.size > 5) {
                    item {
                        OutlinedButton(
                            onClick = onMonthlyTrendsClick,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = MaterialTheme.colorScheme.surface,
                                contentColor = MaterialTheme.colorScheme.primary
                            ),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )
                        ) {
                            Text(
                                stringResource(
                                    R.string.view_all_months_format,
                                    statistics.monthlyTrends.size
                                )
                            )
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
                insightsSection()
            }
        }
    }
}

// ============================================
// Analysis Buttons Grid
// ============================================

/**
 * Compact grid of analysis shortcut buttons.
 * - Yearly Comparison + Car Comparison (if available) on the same row.
 * - Fuel Forecast (if enabled) on the row below, full-width.
 */
@Composable
private fun AnalysisButtonsGrid(
    onYearlyComparisonClick: (() -> Unit)?,
    onCarComparisonClick: (() -> Unit)?,
    onFuelForecastClick: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    // Nothing to show – return early
    if (onYearlyComparisonClick == null && onCarComparisonClick == null && onFuelForecastClick == null) return

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // First row: Yearly Comparison + Car Comparison (each shown when available)
        if (onYearlyComparisonClick != null || onCarComparisonClick != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Yearly Comparison
                if (onYearlyComparisonClick != null) {
                    OutlinedButton(
                        onClick = onYearlyComparisonClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
                            contentColor = MaterialTheme.colorScheme.secondary
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.CompareArrows,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.size(4.dp))
                        Text(
                            text = stringResource(R.string.yearly_comparison_button),
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Car Comparison
                if (onCarComparisonClick != null) {
                    OutlinedButton(
                        onClick = onCarComparisonClick,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f),
                            contentColor = MaterialTheme.colorScheme.tertiary
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.4f)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.CompareArrows,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.size(4.dp))
                        Text(
                            text = stringResource(R.string.car_comparison_button),
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        // Fuel Forecast – full-width row below when enabled
        if (onFuelForecastClick != null) {
            OutlinedButton(
                onClick = onFuelForecastClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 8.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f),
                    contentColor = MaterialTheme.colorScheme.secondary
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.QueryStats,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.size(4.dp))
                Text(
                    text = stringResource(R.string.view_fuel_forecast),
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

// ============================================
// Preview Composables
// ============================================

@Preview(name = "Statistics - Phone Portrait", showBackground = true, widthDp = 380, heightDp = 800)
@Composable
private fun PreviewStatisticsContentPhone() {
    CarTrackingAppTheme(darkTheme = false) {
        StatisticsContent(
            statistics = GlobalStatistics(
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
                costPerKilometer = 0.10
            ),
            onMonthlyTrendsClick = {},
            summarySection = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    StyledCard(modifier = Modifier.fillMaxWidth()) {
                        Text("Total Cost: €1,250.50", modifier = Modifier.padding(16.dp))
                    }
                    StyledCard(modifier = Modifier.fillMaxWidth()) {
                        Text("Avg Consumption: 7.6 L/100km", modifier = Modifier.padding(16.dp))
                    }
                }
            },
            perCarBreakdownCards = {},
            monthlyTrendCards = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    StyledCard(modifier = Modifier.fillMaxWidth()) {
                        Text("January 2026 - €320.50", modifier = Modifier.padding(16.dp))
                    }
                    StyledCard(modifier = Modifier.fillMaxWidth()) {
                        Text("December 2025 - €280.00", modifier = Modifier.padding(16.dp))
                    }
                }
            },
            insightsSection = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("• Most economical: Toyota Corolla")
                    Text("• Total distance: 12,500 km")
                }
            }
        )
    }
}

@Preview(name = "Statistics - Tablet Split View", showBackground = true, widthDp = 900, heightDp = 600)
@Composable
private fun PreviewStatisticsContentTablet() {
    CarTrackingAppTheme(darkTheme = false) {
        StatisticsContent(
            statistics = GlobalStatistics(
                totalCars = 3,
                totalRefills = 48,
                totalCost = 2840.75,
                totalDistance = 28000.0,
                totalLiters = 2100.0,
                averageConsumption = 7.5,
                averagePricePerLiter = 1.62,
                mostEfficientCar = Car(1, "BMW 320i", "BMW-123", 0.0, 18000.0),
                mostExpensiveCar = Car(2, "Mazda 3", "MAZ-456", 0.0, 10000.0),
                monthlyTrends = (1..8).map { month ->
                    MonthlyTrend(
                        month = month,
                        year = 2025,
                        monthName = "Month $month",
                        totalCost = 300.0 + (month * 20),
                        totalLiters = 180.0 + (month * 10),
                        totalDistance = 2500.0 + (month * 100),
                        averageConsumption = 7.0 + (month * 0.1),
                        refillCount = 5 + month,
                        expenseCount = month,
                        expenseCost = 50.0 + (month * 10)
                    )
                },
                perCarStatistics = emptyList(),
                totalServiceExpenses = 420.0,
                totalOtherExpenses = 180.0,
                costPerKilometer = 0.11
            ),
            onMonthlyTrendsClick = {},
            summarySection = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    StyledCard(modifier = Modifier.fillMaxWidth()) {
                        Text("Total Cost: €2,840.75", modifier = Modifier.padding(16.dp))
                    }
                    StyledCard(modifier = Modifier.fillMaxWidth()) {
                        Text("Total Refills: 48", modifier = Modifier.padding(16.dp))
                    }
                }
            },
            perCarBreakdownCards = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    StyledCard(modifier = Modifier.fillMaxWidth()) {
                        Text("BMW 320i - €1,840.50", modifier = Modifier.padding(16.dp))
                    }
                    StyledCard(modifier = Modifier.fillMaxWidth()) {
                        Text("Mazda 3 - €1,000.25", modifier = Modifier.padding(16.dp))
                    }
                }
            },
            monthlyTrendCards = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    (1..5).forEach { month ->
                        StyledCard(modifier = Modifier.fillMaxWidth()) {
                            Text("Month $month - €${300 + month * 20}", modifier = Modifier.padding(16.dp))
                        }
                    }
                }
            },
            insightsSection = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("• Most economical: BMW 320i")
                    Text("• 8 months of data available")
                }
            }
        )
    }
}

@Preview(name = "Statistics - Empty State", showBackground = true, widthDp = 380, heightDp = 800)
@Composable
private fun PreviewStatisticsContentEmpty() {
    CarTrackingAppTheme(darkTheme = false) {
        StatisticsContent(
            statistics = GlobalStatistics(
                totalCars = 0,
                totalRefills = 0,
                totalCost = 0.0,
                totalDistance = 0.0,
                totalLiters = 0.0,
                averageConsumption = 0.0,
                averagePricePerLiter = 0.0,
                mostEfficientCar = null,
                mostExpensiveCar = null,
                monthlyTrends = emptyList(),
                perCarStatistics = emptyList()
            ),
            onMonthlyTrendsClick = {},
            summarySection = {},
            perCarBreakdownCards = {},
            monthlyTrendCards = {},
            insightsSection = {}
        )
    }
}

@Preview(name = "Statistics - Dark Mode", showBackground = true, widthDp = 380, heightDp = 800)
@Composable
private fun PreviewStatisticsContentDark() {
    CarTrackingAppTheme(darkTheme = true) {
        StatisticsContent(
            statistics = GlobalStatistics(
                totalCars = 1,
                totalRefills = 12,
                totalCost = 580.50,
                totalDistance = 6500.0,
                totalLiters = 420.0,
                averageConsumption = 6.5,
                averagePricePerLiter = 1.38,
                mostEfficientCar = Car(1, "Volkswagen Golf", "VW-999", 0.0, 6500.0),
                mostExpensiveCar = Car(1, "Volkswagen Golf", "VW-999", 0.0, 6500.0),
                monthlyTrends = listOf(
                    MonthlyTrend(1, 2026, "January", 290.25, 210.0, 3250.0, 6.5, 6, 1, 40.0)
                ),
                perCarStatistics = emptyList(),
                totalServiceExpenses = 120.0,
                totalOtherExpenses = 60.0,
                costPerKilometer = 0.09
            ),
            onMonthlyTrendsClick = {},
            summarySection = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    StyledCard(modifier = Modifier.fillMaxWidth()) {
                        Text("Total Cost: €580.50", modifier = Modifier.padding(16.dp))
                    }
                    StyledCard(modifier = Modifier.fillMaxWidth()) {
                        Text("Avg Consumption: 6.5 L/100km", modifier = Modifier.padding(16.dp))
                    }
                }
            },
            perCarBreakdownCards = {
                StyledCard(modifier = Modifier.fillMaxWidth()) {
                    Text("Volkswagen Golf - €580.50", modifier = Modifier.padding(16.dp))
                }
            },
            monthlyTrendCards = {
                StyledCard(modifier = Modifier.fillMaxWidth()) {
                    Text("January 2026 - €290.25", modifier = Modifier.padding(16.dp))
                }
            },
            insightsSection = {
                Text("• Single car tracked")
            }
        )
    }
}
