package com.agcoding.cartrackingapp.presentation.consumptiongraph.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.util.formatNumber
import com.agcoding.cartrackingapp.domain.model.ConsumptionDataPoint
import com.agcoding.cartrackingapp.domain.model.ConsumptionTrend
import com.agcoding.cartrackingapp.domain.model.ConsumptionTrendData
import com.agcoding.cartrackingapp.domain.model.DateRange
import com.agcoding.cartrackingapp.presentation.components.ChartDataPoint
import com.agcoding.cartrackingapp.presentation.components.InteractiveLineChart
import com.agcoding.cartrackingapp.presentation.components.StyledCard
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme

@Composable
fun ConsumptionGraphContent(
    trendData: ConsumptionTrendData,
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
                    text = stringResource(R.string.consumption_graph_subtitle),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Statistics Cards - vertical layout for left panel
                StatCard(
                    label = stringResource(R.string.average_label),
                    value = "${trendData.overallAverage.formatNumber(1)} L/100km",
                    modifier = Modifier.fillMaxWidth()
                )

                TrendCard(
                    label = stringResource(R.string.trend_label),
                    trend = trendData.trend,
                    modifier = Modifier.fillMaxWidth()
                )

                StatCard(
                    label = stringResource(R.string.best_label),
                    value = "${trendData.bestConsumption.formatNumber(1)} L/100km",
                    valueColor = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.fillMaxWidth()
                )

                StatCard(
                    label = stringResource(R.string.worst_label),
                    value = "${trendData.worstConsumption.formatNumber(1)} L/100km",
                    valueColor = MaterialTheme.colorScheme.error,
                    modifier = Modifier.fillMaxWidth()
                )

                // Refills info
                Text(
                    text = stringResource(R.string.all_refills_format, trendData.totalRefills),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onBackground
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
                                text = stringResource(R.string.consumption_history),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )

                            // The actual line graph
                            InteractiveLineChart(
                                dataPoints = trendData.dataPoints.map { dataPoint ->
                                    ChartDataPoint(
                                        label = dataPoint.label,
                                        value = dataPoint.averageConsumption,
                                        formattedValue = "${dataPoint.averageConsumption.formatNumber(1)} L/100km"
                                    )
                                },
                                tooltipIcon = Icons.AutoMirrored.Filled.TrendingUp,
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
            // Header
            Text(
                text = stringResource(R.string.consumption_graph_subtitle),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Statistics Cards Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    label = stringResource(R.string.average_label),
                    value = "${trendData.overallAverage.formatNumber(1)} L/100km",
                    modifier = Modifier.weight(1f)
                )

                TrendCard(
                    label = stringResource(R.string.trend_label),
                    trend = trendData.trend,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    label = stringResource(R.string.best_label),
                    value = "${trendData.bestConsumption.formatNumber(1)} L/100km",
                    valueColor = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = stringResource(R.string.worst_label),
                    value = "${trendData.worstConsumption.formatNumber(1)} L/100km",
                    valueColor = MaterialTheme.colorScheme.error,
                    modifier = Modifier.weight(1f)
                )
            }

            // Main Graph Card
            StyledCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.consumption_history),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    // The actual line graph
                    InteractiveLineChart(
                        dataPoints = trendData.dataPoints.map { dataPoint ->
                            ChartDataPoint(
                                label = dataPoint.label,
                                value = dataPoint.averageConsumption,
                                formattedValue = "${dataPoint.averageConsumption.formatNumber(1)} L/100km"
                            )
                        },
                        tooltipIcon = Icons.AutoMirrored.Filled.TrendingUp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Refills info
            Text(
                text = stringResource(R.string.all_refills_format, trendData.totalRefills),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

// ============================================
// Preview Composables
// ============================================

@Preview(name = "Consumption Graph Content - Portrait", showBackground = true, showSystemUi = true)
@Composable
private fun PreviewConsumptionGraphContent() {
    CarTrackingAppTheme(darkTheme = false) {
        ConsumptionGraphContent(
            trendData = ConsumptionTrendData(
                dataPoints = listOf(
                    ConsumptionDataPoint(
                        timestamp = System.currentTimeMillis(),
                        averageConsumption = 6.5,
                        refillCount = 4,
                        totalDistance = 800.0,
                        label = "Jan"
                    ),
                    ConsumptionDataPoint(
                        timestamp = System.currentTimeMillis() + 2592000000L,
                        averageConsumption = 6.8,
                        refillCount = 5,
                        totalDistance = 900.0,
                        label = "Feb"
                    ),
                    ConsumptionDataPoint(
                        timestamp = System.currentTimeMillis() + 5184000000L,
                        averageConsumption = 6.2,
                        refillCount = 4,
                        totalDistance = 850.0,
                        label = "Mar"
                    ),
                    ConsumptionDataPoint(
                        timestamp = System.currentTimeMillis() + 7776000000L,
                        averageConsumption = 6.4,
                        refillCount = 5,
                        totalDistance = 880.0,
                        label = "Apr"
                    ),
                    ConsumptionDataPoint(
                        timestamp = System.currentTimeMillis() + 10368000000L,
                        averageConsumption = 6.1,
                        refillCount = 4,
                        totalDistance = 820.0,
                        label = "May"
                    ),
                    ConsumptionDataPoint(
                        timestamp = System.currentTimeMillis() + 12960000000L,
                        averageConsumption = 6.3,
                        refillCount = 6,
                        totalDistance = 950.0,
                        label = "Jun"
                    )
                ),
                overallAverage = 6.4,
                bestConsumption = 6.1,
                worstConsumption = 6.8,
                trend = ConsumptionTrend.IMPROVING,
                totalRefills = 24,
                dateRange = DateRange(
                    startMillis = System.currentTimeMillis(),
                    endMillis = System.currentTimeMillis() + 15552000000L,
                    label = "Last 6 months"
                )
            )
        )
    }
}

@Preview(
    name = "Consumption Graph Content - Landscape",
    showBackground = true,
    device = "spec:width=800dp,height=480dp,dpi=240,orientation=landscape"
)
@Composable
private fun PreviewConsumptionGraphContentLandscape() {
    CarTrackingAppTheme(darkTheme = false) {
        ConsumptionGraphContent(
            trendData = ConsumptionTrendData(
                dataPoints = listOf(
                    ConsumptionDataPoint(
                        timestamp = System.currentTimeMillis(),
                        averageConsumption = 7.2,
                        refillCount = 3,
                        totalDistance = 700.0,
                        label = "Jan"
                    ),
                    ConsumptionDataPoint(
                        timestamp = System.currentTimeMillis() + 2592000000L,
                        averageConsumption = 7.5,
                        refillCount = 3,
                        totalDistance = 720.0,
                        label = "Feb"
                    ),
                    ConsumptionDataPoint(
                        timestamp = System.currentTimeMillis() + 5184000000L,
                        averageConsumption = 7.8,
                        refillCount = 3,
                        totalDistance = 710.0,
                        label = "Mar"
                    ),
                    ConsumptionDataPoint(
                        timestamp = System.currentTimeMillis() + 7776000000L,
                        averageConsumption = 7.6,
                        refillCount = 3,
                        totalDistance = 730.0,
                        label = "Apr"
                    ),
                    ConsumptionDataPoint(
                        timestamp = System.currentTimeMillis() + 10368000000L,
                        averageConsumption = 7.9,
                        refillCount = 3,
                        totalDistance = 750.0,
                        label = "May"
                    ),
                    ConsumptionDataPoint(
                        timestamp = System.currentTimeMillis() + 12960000000L,
                        averageConsumption = 8.1,
                        refillCount = 3,
                        totalDistance = 780.0,
                        label = "Jun"
                    )
                ),
                overallAverage = 7.7,
                bestConsumption = 7.2,
                worstConsumption = 8.1,
                trend = ConsumptionTrend.WORSENING,
                totalRefills = 18,
                dateRange = DateRange(
                    startMillis = System.currentTimeMillis(),
                    endMillis = System.currentTimeMillis() + 15552000000L,
                    label = "Last 6 months"
                )
            )
        )
    }
}

@Preview(name = "Consumption Graph Content - Dark", showBackground = true, showSystemUi = true)
@Composable
private fun PreviewConsumptionGraphContentDark() {
    CarTrackingAppTheme(darkTheme = true) {
        ConsumptionGraphContent(
            trendData = ConsumptionTrendData(
                dataPoints = listOf(
                    ConsumptionDataPoint(
                        timestamp = System.currentTimeMillis(),
                        averageConsumption = 6.5,
                        refillCount = 5,
                        totalDistance = 900.0,
                        label = "Jan"
                    ),
                    ConsumptionDataPoint(
                        timestamp = System.currentTimeMillis() + 2592000000L,
                        averageConsumption = 6.5,
                        refillCount = 5,
                        totalDistance = 900.0,
                        label = "Feb"
                    ),
                    ConsumptionDataPoint(
                        timestamp = System.currentTimeMillis() + 5184000000L,
                        averageConsumption = 6.4,
                        refillCount = 5,
                        totalDistance = 890.0,
                        label = "Mar"
                    ),
                    ConsumptionDataPoint(
                        timestamp = System.currentTimeMillis() + 7776000000L,
                        averageConsumption = 6.5,
                        refillCount = 5,
                        totalDistance = 900.0,
                        label = "Apr"
                    ),
                    ConsumptionDataPoint(
                        timestamp = System.currentTimeMillis() + 10368000000L,
                        averageConsumption = 6.6,
                        refillCount = 5,
                        totalDistance = 910.0,
                        label = "May"
                    ),
                    ConsumptionDataPoint(
                        timestamp = System.currentTimeMillis() + 12960000000L,
                        averageConsumption = 6.5,
                        refillCount = 5,
                        totalDistance = 900.0,
                        label = "Jun"
                    )
                ),
                overallAverage = 6.5,
                bestConsumption = 6.4,
                worstConsumption = 6.6,
                trend = ConsumptionTrend.STABLE,
                totalRefills = 30,
                dateRange = DateRange(
                    startMillis = System.currentTimeMillis(),
                    endMillis = System.currentTimeMillis() + 15552000000L,
                    label = "Last 6 months"
                )
            )
        )
    }
}
