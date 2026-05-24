package com.agcoding.cartrackingapp.presentation.costgraph.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.domain.model.CostCategory
import com.agcoding.cartrackingapp.domain.model.CostItem
import com.agcoding.cartrackingapp.domain.model.CostTrendData
import com.agcoding.cartrackingapp.domain.model.DateRange
import com.agcoding.cartrackingapp.domain.model.MonthlyCost
import com.agcoding.cartrackingapp.presentation.components.ChartDataPoint
import com.agcoding.cartrackingapp.presentation.components.InteractiveLineChart
import com.agcoding.cartrackingapp.presentation.components.StyledCard
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme
import java.util.Locale

@Composable
fun CostGraphContent(
    trendData: CostTrendData,
    modifier: Modifier = Modifier
) {
    val isTablet = com.agcoding.cartrackingapp.util.DeviceUtils.isTablet()
    val isLandscape = com.agcoding.cartrackingapp.util.DeviceUtils.isLandscape()
    val useSplitView = isTablet || isLandscape

    if (useSplitView) {
        // Split view for tablets and landscape
        Row(
            modifier = modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Left side: Header and Stats (35%)
            Column(
                modifier = Modifier
                    .weight(0.35f)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header with icon
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AttachMoney,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Title and subtitle
                Text(
                    text = stringResource(R.string.cost_graph_title),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = stringResource(R.string.cost_graph_subtitle),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Total Cost Card
                StyledCard(
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    border = null
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AttachMoney,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = stringResource(R.string.total_cost_label),
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "€${String.format(Locale.getDefault(), "%.2f", trendData.totalCost)}",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                // Statistics Cards
                StatCard(
                    label = stringResource(R.string.average_monthly_cost),
                    value = "€${String.format(Locale.getDefault(), "%.2f", trendData.averageMonthlyCost)}",
                    modifier = Modifier.fillMaxWidth()
                )
                StatCard(
                    label = stringResource(R.string.highest_month_cost),
                    value = "€${String.format(Locale.getDefault(), "%.2f", trendData.highestMonthCost)}",
                    modifier = Modifier.fillMaxWidth()
                )
                StatCard(
                    label = stringResource(R.string.lowest_month_cost),
                    value = "€${String.format(Locale.getDefault(), "%.2f", trendData.lowestMonthCost)}",
                    valueColor = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.fillMaxWidth()
                )
                StatCard(
                    label = stringResource(R.string.total_expenses_count),
                    value = trendData.recentExpenses.size.toString(),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Right side: Graph, Category Breakdown, and Recent Expenses (65%)
            LazyColumn(
                modifier = Modifier
                    .weight(0.65f)
                    .fillMaxHeight(),
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Monthly Cost Chart
                if (trendData.monthlyCosts.isNotEmpty()) {
                    item {
                        StyledCard(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.monthly_cost_trend),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                InteractiveLineChart(
                                    dataPoints = trendData.monthlyCosts.map { monthData ->
                                        ChartDataPoint(
                                            label = "${monthData.month} ${monthData.year}",
                                            value = monthData.totalCost,
                                            formattedValue = "€${String.format(Locale.getDefault(), "%.2f", monthData.totalCost)}"
                                        )
                                    },
                                    tooltipIcon = Icons.Default.AttachMoney,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }

                // Cost by Category
                if (trendData.costByCategory.isNotEmpty()) {
                    item {
                        StyledCard(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.cost_breakdown_label),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                trendData.costByCategory.forEach { category ->
                                    CategoryItem(category = category)
                                    Spacer(modifier = Modifier.height(6.dp))
                                }
                            }
                        }
                    }
                }

                // Recent Expenses Section
                if (trendData.recentExpenses.isNotEmpty()) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.recent_expenses),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            StyledCard(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                border = null
                            ) {
                                Text(
                                    text = stringResource(R.string.items_format, trendData.recentExpenses.size),
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    items(trendData.recentExpenses) { expense ->
                        ExpenseItem(expense = expense)
                    }
                }
            }
        }
    } else {
        // Original single-column layout for portrait phones
        Column(
            modifier = modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header with icon
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AttachMoney,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Title and subtitle
            Text(
                text = stringResource(R.string.cost_graph_title),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = stringResource(R.string.cost_graph_subtitle),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Total Cost Card (Main highlight card)
            StyledCard(
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                border = null
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AttachMoney,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.total_cost_label),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "€${String.format(Locale.getDefault(), "%.2f", trendData.totalCost)}",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // Statistics Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    label = stringResource(R.string.average_monthly_cost),
                    value = "€${String.format(Locale.getDefault(), "%.2f", trendData.averageMonthlyCost)}",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = stringResource(R.string.highest_month_cost),
                    value = "€${String.format(Locale.getDefault(), "%.2f", trendData.highestMonthCost)}",
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    label = stringResource(R.string.lowest_month_cost),
                    value = "€${String.format(Locale.getDefault(), "%.2f", trendData.lowestMonthCost)}",
                    valueColor = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = stringResource(R.string.total_expenses_count),
                    value = trendData.recentExpenses.size.toString(),
                    modifier = Modifier.weight(1f)
                )
            }

            // Monthly Cost Chart
            if (trendData.monthlyCosts.isNotEmpty()) {
                StyledCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.monthly_cost_trend),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        InteractiveLineChart(
                            dataPoints = trendData.monthlyCosts.map { monthData ->
                                ChartDataPoint(
                                    label = "${monthData.month} ${monthData.year}",
                                    value = monthData.totalCost,
                                    formattedValue = "€${String.format(Locale.getDefault(), "%.2f", monthData.totalCost)}"
                                )
                            },
                            tooltipIcon = Icons.Default.AttachMoney,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Cost by Category
            if (trendData.costByCategory.isNotEmpty()) {
                StyledCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.cost_breakdown_label),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        trendData.costByCategory.forEach { category ->
                            CategoryItem(category = category)
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }
                }
            }

            // Recent Expenses Section
            if (trendData.recentExpenses.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.recent_expenses),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    StyledCard(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        border = null
                    ) {
                        Text(
                            text = stringResource(R.string.items_format, trendData.recentExpenses.size),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }

                // Expense list
                trendData.recentExpenses.forEach { expense ->
                    ExpenseItem(expense = expense)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// ============================================
// Preview Composables
// ============================================

@Preview(name = "Cost Graph Content - Portrait", showBackground = true, showSystemUi = true)
@Composable
private fun PreviewCostGraphContent() {
    CarTrackingAppTheme(darkTheme = false) {
        CostGraphContent(
            trendData = CostTrendData(
                monthlyCosts = listOf(
                    MonthlyCost(
                        month = "Jan",
                        year = 2026,
                        totalCost = 450.0,
                        fuelCost = 300.0,
                        serviceCost = 120.0,
                        otherCost = 30.0,
                        timestamp = System.currentTimeMillis()
                    ),
                    MonthlyCost(
                        month = "Feb",
                        year = 2026,
                        totalCost = 520.0,
                        fuelCost = 350.0,
                        serviceCost = 150.0,
                        otherCost = 20.0,
                        timestamp = System.currentTimeMillis()
                    ),
                    MonthlyCost(
                        month = "Mar",
                        year = 2026,
                        totalCost = 380.0,
                        fuelCost = 280.0,
                        serviceCost = 80.0,
                        otherCost = 20.0,
                        timestamp = System.currentTimeMillis()
                    )
                ),
                totalCost = 1350.0,
                averageMonthlyCost = 450.0,
                highestMonthCost = 520.0,
                lowestMonthCost = 380.0,
                costByCategory = listOf(
                    CostCategory(
                        name = "Fuel",
                        amount = 930.0,
                        percentage = 68.9,
                        color = 0xFF4CAF50.toInt()
                    ),
                    CostCategory(
                        name = "Service",
                        amount = 350.0,
                        percentage = 25.9,
                        color = 0xFF2196F3.toInt()
                    ),
                    CostCategory(
                        name = "Other",
                        amount = 70.0,
                        percentage = 5.2,
                        color = 0xFFFF9800.toInt()
                    )
                ),
                recentExpenses = listOf(
                    CostItem(
                        id = 1,
                        date = System.currentTimeMillis(),
                        category = "Fuel",
                        description = "Gas station",
                        amount = 65.50,
                        carName = "Toyota Corolla"
                    ),
                    CostItem(
                        id = 2,
                        date = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L,
                        category = "Oil Change",
                        description = "Regular maintenance",
                        amount = 120.00,
                        carName = "Toyota Corolla"
                    )
                ),
                dateRange = DateRange(
                    startMillis = System.currentTimeMillis() - 90 * 24 * 60 * 60 * 1000L,
                    endMillis = System.currentTimeMillis(),
                    label = "Last 3 months"
                )
            )
        )
    }
}

@Preview(
    name = "Cost Graph Content - Landscape",
    showBackground = true,
    device = "spec:width=800dp,height=480dp,dpi=240,orientation=landscape"
)
@Composable
private fun PreviewCostGraphContentLandscape() {
    CarTrackingAppTheme(darkTheme = false) {
        CostGraphContent(
            trendData = CostTrendData(
                monthlyCosts = listOf(
                    MonthlyCost(
                        month = "Jan",
                        year = 2026,
                        totalCost = 650.0,
                        fuelCost = 450.0,
                        serviceCost = 180.0,
                        otherCost = 20.0,
                        timestamp = System.currentTimeMillis()
                    ),
                    MonthlyCost(
                        month = "Feb",
                        year = 2026,
                        totalCost = 720.0,
                        fuelCost = 500.0,
                        serviceCost = 200.0,
                        otherCost = 20.0,
                        timestamp = System.currentTimeMillis()
                    )
                ),
                totalCost = 1370.0,
                averageMonthlyCost = 685.0,
                highestMonthCost = 720.0,
                lowestMonthCost = 650.0,
                costByCategory = listOf(
                    CostCategory(
                        name = "Fuel",
                        amount = 950.0,
                        percentage = 69.3,
                        color = 0xFF4CAF50.toInt()
                    ),
                    CostCategory(
                        name = "Service",
                        amount = 380.0,
                        percentage = 27.7,
                        color = 0xFF2196F3.toInt()
                    ),
                    CostCategory(
                        name = "Other",
                        amount = 40.0,
                        percentage = 2.9,
                        color = 0xFFFF9800.toInt()
                    )
                ),
                recentExpenses = listOf(
                    CostItem(
                        id = 1,
                        date = System.currentTimeMillis(),
                        category = "Fuel",
                        description = "Gas station",
                        amount = 75.00,
                        carName = "BMW 320i"
                    )
                ),
                dateRange = DateRange(
                    startMillis = System.currentTimeMillis() - 60 * 24 * 60 * 60 * 1000L,
                    endMillis = System.currentTimeMillis(),
                    label = "Last 2 months"
                )
            )
        )
    }
}

@Preview(name = "Cost Graph Content - Dark", showBackground = true, showSystemUi = true)
@Composable
private fun PreviewCostGraphContentDark() {
    CarTrackingAppTheme(darkTheme = true) {
        CostGraphContent(
            trendData = CostTrendData(
                monthlyCosts = listOf(
                    MonthlyCost(
                        month = "Dec",
                        year = 2025,
                        totalCost = 400.0,
                        fuelCost = 300.0,
                        serviceCost = 80.0,
                        otherCost = 20.0,
                        timestamp = System.currentTimeMillis()
                    )
                ),
                totalCost = 400.0,
                averageMonthlyCost = 400.0,
                highestMonthCost = 400.0,
                lowestMonthCost = 400.0,
                costByCategory = listOf(
                    CostCategory(
                        name = "Fuel",
                        amount = 300.0,
                        percentage = 75.0,
                        color = 0xFF4CAF50.toInt()
                    ),
                    CostCategory(
                        name = "Service",
                        amount = 80.0,
                        percentage = 20.0,
                        color = 0xFF2196F3.toInt()
                    ),
                    CostCategory(
                        name = "Other",
                        amount = 20.0,
                        percentage = 5.0,
                        color = 0xFFFF9800.toInt()
                    )
                ),
                recentExpenses = listOf(
                    CostItem(
                        id = 1,
                        date = System.currentTimeMillis(),
                        category = "Tire Change",
                        description = "All season tires",
                        amount = 450.00,
                        carName = "Honda Civic"
                    )
                ),
                dateRange = DateRange(
                    startMillis = System.currentTimeMillis() - 30 * 24 * 60 * 60 * 1000L,
                    endMillis = System.currentTimeMillis(),
                    label = "Last month"
                )
            )
        )
    }
}
