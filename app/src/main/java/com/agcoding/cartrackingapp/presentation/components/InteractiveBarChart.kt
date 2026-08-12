package com.agcoding.cartrackingapp.presentation.components
import androidx.compose.ui.tooling.preview.Preview
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info

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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

/**
 * Reusable interactive bar chart component
 */
@Composable
fun InteractiveBarChart(
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
        animationSpec = tween(durationMillis = 800),
        label = "bar_animation"
    )

    LaunchedEffect(dataPoints) {
        animationProgress = 0f
        animationProgress = 1f
    }

    val barColor = MaterialTheme.colorScheme.primary
    val selectedBarColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
    val textColor = MaterialTheme.colorScheme.onSurfaceVariant
    val gridColor = MaterialTheme.colorScheme.outlineVariant
    val maxValue = (dataPoints.maxOfOrNull { it.value } ?: 1.0).coerceAtLeast(1.0)

    val yAxisSteps = 4
    val stepValue = maxValue / yAxisSteps
    val yAxisValues = (0..yAxisSteps).map { it * stepValue }

    Column {
        val selectedData = selectedIndex?.takeIf { it < dataPoints.size }?.let { dataPoints[it] }

        AnimatedVisibility(
            visible = selectedData != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            if (selectedData != null) {
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

        Row(modifier = modifier.fillMaxWidth()) {
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

            Canvas(
                modifier = Modifier
                    .weight(1f)
                    .height(220.dp)
                    .pointerInput(dataPoints) {
                        detectTapGestures { offset ->
                            val chartWidth = size.width.toFloat()
                            val barWidthTotal = chartWidth / dataPoints.size
                            val clickedIndex = (offset.x / barWidthTotal).toInt().coerceIn(0, dataPoints.size - 1)
                            selectedIndex = clickedIndex
                        }
                    }
                    .pointerInput(dataPoints) {
                        detectDragGestures(
                            onDragEnd = { },
                            onDragCancel = { }
                        ) { change, _ ->
                            change.consume()
                            val chartWidth = size.width.toFloat()
                            val barWidthTotal = chartWidth / dataPoints.size
                            val draggedIndex = (change.position.x / barWidthTotal).toInt().coerceIn(0, dataPoints.size - 1)
                            selectedIndex = draggedIndex
                        }
                    }
            ) {
                val chartWidth = size.width
                val chartHeight = size.height
                val maxBarHeight = chartHeight * 0.90f
                val barWidthTotal = chartWidth / dataPoints.size
                val barWidth = barWidthTotal * 0.7f
                val barSpacing = barWidthTotal * 0.3f

                // Grid lines
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

                // Bars
                dataPoints.forEachIndexed { index, dataPoint ->
                    val isSelected = index == selectedIndex
                    val normalizedHeight = (dataPoint.value / maxValue).toFloat()
                    val height = normalizedHeight * maxBarHeight * animatedProgress
                    val x = index * barWidthTotal + barSpacing / 2
                    val y = chartHeight - height

                    drawRoundRect(
                        color = if (isSelected) selectedBarColor else barColor,
                        topLeft = Offset(x, y),
                        size = Size(barWidth, height),
                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                    )
                }
            }
        }

        // X-axis labels
        if (dataPoints.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = if (showYAxisLabels) 45.dp else 0.dp, top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val indicesToShow = if (dataPoints.size <= 6) {
                    dataPoints.indices.toList()
                } else {
                    listOf(0, dataPoints.size / 2, dataPoints.size - 1)
                }

                dataPoints.forEachIndexed { index, dataPoint ->
                    if (index in indicesToShow) {
                        Text(
                            text = dataPoint.label,
                            fontSize = 10.sp,
                            color = textColor,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun InteractiveBarChartPreview() {
    CarTrackingAppTheme {
        InteractiveBarChart(dataPoints = emptyList(), tooltipIcon = Icons.Default.Info)
    }
}
