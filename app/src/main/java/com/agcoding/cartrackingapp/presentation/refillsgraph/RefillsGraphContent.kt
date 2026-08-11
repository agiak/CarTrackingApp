package com.agcoding.cartrackingapp.presentation.refillsgraph

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.util.formatNumber
import com.agcoding.cartrackingapp.domain.model.RefillItem
import com.agcoding.cartrackingapp.domain.model.RefillsTrendData
import com.agcoding.cartrackingapp.presentation.components.ChartDataPoint
import com.agcoding.cartrackingapp.presentation.components.InteractiveBarChart
import com.agcoding.cartrackingapp.presentation.components.StyledCard
import com.agcoding.cartrackingapp.domain.model.DateRange
import com.agcoding.cartrackingapp.domain.model.MonthlyRefills
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme
import androidx.compose.ui.tooling.preview.Preview

@Composable
internal fun RefillsGraphContent(
    trendData: RefillsTrendData,
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
                // Header
                Text(
                    text = stringResource(R.string.refills_graph_subtitle),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Statistics Cards - vertical layout for left panel
                StatCard(
                    label = stringResource(R.string.total_refills_label),
                    value = trendData.totalRefills.toString(),
                    modifier = Modifier.fillMaxWidth()
                )

                StatCard(
                    label = stringResource(R.string.average_per_month),
                    value = trendData.averageRefillsPerMonth.formatNumber(1),
                    modifier = Modifier.fillMaxWidth()
                )

                StatCard(
                    label = stringResource(R.string.avg_liters_per_refill),
                    value = "${trendData.averageLitersPerRefill.formatNumber(1)} L",
                    modifier = Modifier.fillMaxWidth()
                )

                StatCard(
                    label = stringResource(R.string.highest_month),
                    value = trendData.highestMonthRefills.toString(),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Right side: Graph (65%)
            LazyColumn(
                modifier = Modifier
                    .weight(0.65f)
                    .fillMaxHeight(),
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Main Graph Card
                item {
                    StyledCard(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.monthly_refills_trend),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            // The actual bar graph
                            InteractiveBarChart(
                                dataPoints = trendData.monthlyRefills.map { monthData ->
                                    ChartDataPoint(
                                        label = if (monthData.month.contains(monthData.year.toString())) monthData.month else "${monthData.month} ${monthData.year}",
                                        value = monthData.refillCount.toDouble(),
                                        formattedValue = stringResource(R.string.refills_format, monthData.refillCount)
                                    )
                                },
                                tooltipIcon = Icons.Default.LocalGasStation,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }
    } else {
        // Original single-column layout for portrait phones
        Column(
            modifier = modifier
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
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
                    imageVector = Icons.Default.LocalGasStation,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Title and subtitle
        Text(
            text = stringResource(R.string.refills_graph_title),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = stringResource(R.string.refills_graph_subtitle),
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )


        Spacer(modifier = Modifier.height(8.dp))

        // Total Refills Card (Main highlight card)
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
                        imageVector = Icons.Default.LocalGasStation,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(R.string.total_refills_label),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${trendData.totalRefills}",
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
                label = stringResource(R.string.average_per_month),
                value = trendData.averageRefillsPerMonth.formatNumber(1),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                label = stringResource(R.string.highest_month),
                value = "${trendData.highestMonthRefills}",
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                label = stringResource(R.string.lowest_month),
                value = "${trendData.lowestMonthRefills}",
                valueColor = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                label = stringResource(R.string.avg_liters_per_refill),
                value = "${trendData.averageLitersPerRefill.formatNumber(1)} L",
                modifier = Modifier.weight(1f)
            )
        }

        // Monthly Refills Chart
        if (trendData.monthlyRefills.isNotEmpty()) {
            StyledCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.monthly_refills_trend),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    InteractiveBarChart(
                        dataPoints = trendData.monthlyRefills.map { monthData ->
                            ChartDataPoint(
                                label = if (monthData.month.contains(monthData.year.toString())) monthData.month else "${monthData.month} ${monthData.year}",
                                value = monthData.refillCount.toDouble(),
                                formattedValue = stringResource(R.string.refills_format, monthData.refillCount)
                            )
                        },
                        tooltipIcon = Icons.Default.LocalGasStation,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Recent Refills Section
        if (trendData.recentRefills.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.recent_refills),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                StyledCard(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    border = null
                ) {
                    Text(
                        text = stringResource(R.string.items_format, trendData.recentRefills.size),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Refill list
            trendData.recentRefills.forEach { refill ->
                RefillItemCard(refill = refill)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// ============================================
// Preview Composables
// ============================================

@Preview(name = "Refills Graph Content - Portrait", showBackground = true, showSystemUi = true)
@Composable
private fun PreviewRefillsGraphContent() {
    CarTrackingAppTheme(darkTheme = false) {
        RefillsGraphContent(
            trendData = RefillsTrendData(
                monthlyRefills = listOf(
                    MonthlyRefills("Jan", 2026, 4, 150.0, 250.0, System.currentTimeMillis()),
                    MonthlyRefills("Feb", 2026, 5, 200.0, 320.0, System.currentTimeMillis() + 2592000000L)
                ),
                totalRefills = 9,
                averageRefillsPerMonth = 4.5,
                highestMonthRefills = 5,
                lowestMonthRefills = 4,
                totalLiters = 350.0,
                averageLitersPerRefill = 38.8,
                recentRefills = listOf(
                    RefillItem(1, System.currentTimeMillis(), 40.0, 65.0, 1.625, "Toyota Corolla")
                ),
                dateRange = DateRange(System.currentTimeMillis() - 5184000000L, System.currentTimeMillis(), "Last 2 months")
            )
        )
    }
}

@Preview(
    name = "Refills Graph Content - Landscape",
    showBackground = true,
    device = "spec:width=800dp,height=480dp,dpi=240,orientation=landscape"
)
@Composable
private fun PreviewRefillsGraphContentLandscape() {
    CarTrackingAppTheme(darkTheme = false) {
        RefillsGraphContent(
            trendData = RefillsTrendData(
                monthlyRefills = listOf(
                    MonthlyRefills("2025", 2025, 48, 1800.0, 3000.0, System.currentTimeMillis() - 31536000000L),
                    MonthlyRefills("2026", 2026, 10, 400.0, 650.0, System.currentTimeMillis())
                ),
                totalRefills = 58,
                averageRefillsPerMonth = 4.1,
                highestMonthRefills = 6,
                lowestMonthRefills = 2,
                totalLiters = 2200.0,
                averageLitersPerRefill = 37.9,
                recentRefills = listOf(
                    RefillItem(1, System.currentTimeMillis(), 45.0, 75.0, 1.66, "BMW 320i")
                ),
                dateRange = DateRange(System.currentTimeMillis() - 63072000000L, System.currentTimeMillis(), "All Time")
            )
        )
    }
}
