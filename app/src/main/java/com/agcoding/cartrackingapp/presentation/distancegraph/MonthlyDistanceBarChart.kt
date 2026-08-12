package com.agcoding.cartrackingapp.presentation.distancegraph

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agcoding.cartrackingapp.domain.model.MonthlyDistance
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme
import com.agcoding.cartrackingapp.util.formatNumber

@Composable
internal fun MonthlyDistanceBarChart(
    monthlyDistances: List<MonthlyDistance>,
    modifier: Modifier = Modifier
) {
    if (monthlyDistances.isEmpty()) return

    var animationProgress by remember { mutableStateOf(0f) }
    val animatedProgress by animateFloatAsState(
        targetValue = animationProgress,
        animationSpec = tween(durationMillis = 1000),
        label = "line_animation"
    )

    LaunchedEffect(monthlyDistances) {
        animationProgress = 0f
        animationProgress = 1f
    }

    val lineColor = MaterialTheme.colorScheme.primary
    val pointColor = MaterialTheme.colorScheme.primary
    val textColor = MaterialTheme.colorScheme.onSurfaceVariant
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val dotInnerColor = MaterialTheme.colorScheme.surface
    val maxDistance = monthlyDistances.maxOfOrNull { it.distance } ?: 1.0

    // Calculate nice Y-axis values
    val yAxisSteps = 4
    val stepValue = if (maxDistance > 0) maxDistance / yAxisSteps else 1.0
    val yAxisValues = (0..yAxisSteps).map { it * stepValue }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            // Y-axis labels
            Column(
                modifier = Modifier
                    .width(45.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                yAxisValues.reversed().forEach { value ->
                    Text(
                        text = if (value >= 1000) {
                            "${(value / 1000).formatNumber(0)}k"
                        } else {
                            value.formatNumber(0)
                        },
                        fontSize = 11.sp,
                        color = textColor,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                }
            }

            // Chart area
            Canvas(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
            ) {
                val chartWidth = size.width
                val chartHeight = size.height
                val maxBarHeight = chartHeight * 0.95f

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
                    monthlyDistances.size == 1 -> {
                        // Single data point - draw just a point in the center
                        val monthData = monthlyDistances.first()
                        val normalizedHeight = if (maxDistance > 0) {
                            (monthData.distance / maxDistance).toFloat()
                        } else 0f
                        val y = chartHeight - (normalizedHeight * maxBarHeight * animatedProgress)
                        val x = chartWidth / 2f

                        drawCircle(
                            color = pointColor,
                            radius = 6.dp.toPx(),
                            center = Offset(x, y)
                        )
                        drawCircle(
                            color = dotInnerColor,
                            radius = 3.dp.toPx(),
                            center = Offset(x, y)
                        )
                    }
                    monthlyDistances.size > 1 -> {
                        val pointCount = monthlyDistances.size
                        val xStep = chartWidth / (pointCount - 1).toFloat()

                        // Calculate points for the line
                        val points = monthlyDistances.mapIndexed { index, monthData ->
                            val x = index * xStep
                            val normalizedHeight = if (maxDistance > 0) {
                                (monthData.distance / maxDistance).toFloat()
                            } else 0f
                            val y = chartHeight - (normalizedHeight * maxBarHeight * animatedProgress)
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
                        points.forEach { point ->
                            drawCircle(
                                color = pointColor,
                                radius = 4.dp.toPx(),
                                center = point
                            )
                            // Draw white inner circle for better visibility
                            drawCircle(
                                color = dotInnerColor,
                                radius = 2.dp.toPx(),
                                center = point
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // X-axis labels
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 45.dp), // Offset for Y-axis width
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            monthlyDistances.forEach { monthData ->
                Text(
                    text = monthData.month,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 240)
@Composable
private fun MonthlyDistanceBarChartPreview() {
    CarTrackingAppTheme {
        MonthlyDistanceBarChart(
            monthlyDistances = listOf(
                MonthlyDistance("Jan", 2026, 450.0, System.currentTimeMillis()),
                MonthlyDistance("Feb", 2026, 520.0, System.currentTimeMillis() + 2592000000L),
                MonthlyDistance("Mar", 2026, 480.0, System.currentTimeMillis() + 5184000000L)
            ),
            modifier = Modifier.fillMaxSize()
        )
    }
}
