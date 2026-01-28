package com.agcoding.cartrackingapp.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Route
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme
import kotlin.math.roundToInt

/**
 * Data point for the interactive line chart
 */
data class ChartDataPoint(
    val label: String,           // e.g., "Jan 2026" or "Week 3"
    val value: Double,           // The numeric value to plot
    val formattedValue: String   // e.g., "450 km" or "7.5 L/100km"
)

/**
 * Reusable interactive line chart component with tap/drag functionality
 *
 * @param dataPoints List of data points to plot
 * @param tooltipIcon Icon to show in the tooltip
 * @param modifier Modifier for the chart
 * @param showYAxisLabels Whether to show Y-axis labels (default: true)
 */
@Composable
fun InteractiveLineChart(
    dataPoints: List<ChartDataPoint>,
    tooltipIcon: ImageVector,
    modifier: Modifier = Modifier,
    showYAxisLabels: Boolean = true
) {
    if (dataPoints.isEmpty()) return

    var selectedIndex by remember { mutableStateOf<Int?>(null) }
    var animationProgress by remember { mutableStateOf(0f) }
    val animatedProgress by animateFloatAsState(
        targetValue = animationProgress,
        animationSpec = tween(durationMillis = 1000),
        label = "line_animation"
    )

    LaunchedEffect(dataPoints) {
        animationProgress = 0f
        animationProgress = 1f
    }

    val lineColor = MaterialTheme.colorScheme.primary
    val pointColor = MaterialTheme.colorScheme.primary
    val textColor = MaterialTheme.colorScheme.onSurfaceVariant
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val maxValue = dataPoints.maxOfOrNull { it.value } ?: 1.0

    // Calculate nice Y-axis values
    val yAxisSteps = 4
    val stepValue = if (maxValue > 0) maxValue / yAxisSteps else 1.0
    val yAxisValues = (0..yAxisSteps).map { it * stepValue }

    Column {
        // Tooltip display - outside the chart modifier to not affect chart height
        AnimatedVisibility(
            visible = selectedIndex != null && selectedIndex!! < dataPoints.size,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            if (selectedIndex != null && selectedIndex!! < dataPoints.size) {
                val selectedData = dataPoints[selectedIndex!!]
                StyledCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    border = null
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = selectedData.label,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = selectedData.formattedValue,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Icon(
                            imageVector = tooltipIcon,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }

        // Chart with fixed height
        Row(
            modifier = modifier
                .fillMaxWidth()
        ) {
            // Y-axis labels (optional)
            if (showYAxisLabels) {
                Column(
                    modifier = Modifier
                        .width(45.dp)
                        .height(220.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    yAxisValues.reversed().forEach { value ->
                        Text(
                            text = if (value >= 1000) {
                                "${(value / 1000).roundToInt()}k"
                            } else {
                                value.roundToInt().toString()
                            },
                            fontSize = 11.sp,
                            color = textColor,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                    }
                }
            }

            // Chart area with gesture detection
            Canvas(
                modifier = Modifier
                    .weight(1f)
                    .height(220.dp)
                    .pointerInput(dataPoints) {
                        detectTapGestures { offset ->
                            val chartWidth = size.width.toFloat()
                            if (dataPoints.size > 1) {
                                val xStep = chartWidth / (dataPoints.size - 1)
                                val clickedIndex =
                                    ((offset.x / xStep).toInt()).coerceIn(0, dataPoints.size - 1)
                                selectedIndex = clickedIndex
                            } else if (dataPoints.size == 1) {
                                selectedIndex = 0
                            }
                        }
                    }
                    .pointerInput(dataPoints) {
                        detectDragGestures(
                            onDragEnd = { /* Keep last selected */ },
                            onDragCancel = { /* Keep last selected */ }
                        ) { change, _ ->
                            change.consume()
                            val chartWidth = size.width.toFloat()
                            if (dataPoints.size > 1) {
                                val xStep = chartWidth / (dataPoints.size - 1)
                                val draggedIndex =
                                    ((change.position.x / xStep).toInt()).coerceIn(
                                        0,
                                        dataPoints.size - 1
                                    )
                                selectedIndex = draggedIndex
                            } else if (dataPoints.size == 1) {
                                selectedIndex = 0
                            }
                        }
                    }
            ) {
                val chartWidth = size.width
                val chartHeight = size.height
                val maxBarHeight = chartHeight * 0.90f

                // Draw horizontal grid lines
                for (i in 0..yAxisSteps) {
                    val y = chartHeight - (i.toFloat() / yAxisSteps) * maxBarHeight
                    drawLine(
                        color = gridColor,
                        start = Offset(0f, y),
                        end = Offset(chartWidth, y),
                        strokeWidth = 1f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 5f))
                    )
                }

                // Handle different data point counts
                when {
                    dataPoints.size == 1 -> {
                        // Single data point
                        val dataPoint = dataPoints.first()
                        val normalizedHeight = if (maxValue > 0) {
                            (dataPoint.value / maxValue).toFloat()
                        } else 0f
                        val y = chartHeight - (normalizedHeight * maxBarHeight * animatedProgress)
                        val x = chartWidth / 2f

                        drawCircle(
                            color = if (selectedIndex == 0) pointColor else pointColor,
                            radius = if (selectedIndex == 0) 8.dp.toPx() else 6.dp.toPx(),
                            center = Offset(x, y)
                        )
                        drawCircle(
                            color = Color.White,
                            radius = if (selectedIndex == 0) 4.dp.toPx() else 3.dp.toPx(),
                            center = Offset(x, y)
                        )
                    }

                    dataPoints.size > 1 -> {
                        val pointCount = dataPoints.size
                        val xStep = chartWidth / (pointCount - 1).toFloat()

                        // Calculate points for the line
                        val points = dataPoints.mapIndexed { index, dataPoint ->
                            val x = index * xStep
                            val normalizedHeight = if (maxValue > 0) {
                                (dataPoint.value / maxValue).toFloat()
                            } else 0f
                            val y =
                                chartHeight - (normalizedHeight * maxBarHeight * animatedProgress)
                            Offset(x, y)
                        }

                        // Draw area under the line with gradient effect
                        if (animatedProgress > 0) {
                            val pathPoints = points.toMutableList()
                            pathPoints.add(Offset(points.last().x, chartHeight))
                            pathPoints.add(Offset(points.first().x, chartHeight))

                            val path = androidx.compose.ui.graphics.Path().apply {
                                moveTo(pathPoints[0].x, pathPoints[0].y)
                                for (i in 1 until pathPoints.size) {
                                    lineTo(pathPoints[i].x, pathPoints[i].y)
                                }
                                close()
                            }

                            drawPath(
                                path = path,
                                color = lineColor.copy(alpha = 0.1f)
                            )
                        }

                        // Draw the line connecting all points
                        for (i in 0 until points.size - 1) {
                            drawLine(
                                color = lineColor,
                                start = points[i],
                                end = points[i + 1],
                                strokeWidth = 3.dp.toPx()
                            )
                        }

                        // Draw points (circles) at each data point
                        points.forEachIndexed { index, point ->
                            val isSelected = index == selectedIndex
                            drawCircle(
                                color = pointColor,
                                radius = if (isSelected) 6.dp.toPx() else 4.dp.toPx(),
                                center = point
                            )
                            drawCircle(
                                color = Color.White,
                                radius = if (isSelected) 3.dp.toPx() else 2.dp.toPx(),
                                center = point
                            )
                        }
                    }
                }
            }
        }
    }
}

// ============================================
// Preview Composables
// ============================================

@Preview(name = "Line Chart - Multiple Points", showBackground = true, widthDp = 380)
@Composable
private fun PreviewInteractiveLineChart() {
    CarTrackingAppTheme(darkTheme = false) {
        InteractiveLineChart(
            dataPoints = listOf(
                ChartDataPoint("Jan", 450.0, "450 km"),
                ChartDataPoint("Feb", 520.0, "520 km"),
                ChartDataPoint("Mar", 480.0, "480 km"),
                ChartDataPoint("Apr", 600.0, "600 km"),
                ChartDataPoint("May", 550.0, "550 km")
            ),
            tooltipIcon = androidx.compose.material.icons.Icons.Default.Route,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(name = "Line Chart - Single Point", showBackground = true, widthDp = 380)
@Composable
private fun PreviewInteractiveLineChartSinglePoint() {
    CarTrackingAppTheme(darkTheme = false) {
        InteractiveLineChart(
            dataPoints = listOf(
                ChartDataPoint("Jan", 500.0, "500 km")
            ),
            tooltipIcon = androidx.compose.material.icons.Icons.Default.Route,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(name = "Line Chart - No Y-Axis Labels", showBackground = true, widthDp = 380)
@Composable
private fun PreviewInteractiveLineChartNoYAxis() {
    CarTrackingAppTheme(darkTheme = false) {
        InteractiveLineChart(
            dataPoints = listOf(
                ChartDataPoint("W1", 7.5, "7.5 L/100km"),
                ChartDataPoint("W2", 7.2, "7.2 L/100km"),
                ChartDataPoint("W3", 7.8, "7.8 L/100km"),
                ChartDataPoint("W4", 7.4, "7.4 L/100km")
            ),
            tooltipIcon = androidx.compose.material.icons.Icons.AutoMirrored.Filled.TrendingUp,
            showYAxisLabels = false,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(name = "Line Chart - Dark Mode", showBackground = true, widthDp = 380)
@Composable
private fun PreviewInteractiveLineChartDark() {
    CarTrackingAppTheme(darkTheme = true) {
        InteractiveLineChart(
            dataPoints = listOf(
                ChartDataPoint("Jan", 120.0, "€120"),
                ChartDataPoint("Feb", 150.0, "€150"),
                ChartDataPoint("Mar", 135.0, "€135"),
                ChartDataPoint("Apr", 180.0, "€180")
            ),
            tooltipIcon = androidx.compose.material.icons.Icons.Default.AttachMoney,
            modifier = Modifier.padding(16.dp)
        )
    }
}

