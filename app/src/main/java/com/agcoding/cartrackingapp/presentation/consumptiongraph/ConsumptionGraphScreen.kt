package com.agcoding.cartrackingapp.presentation.consumptiongraph

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.agcoding.cartrackingapp.domain.model.ConsumptionDataPoint
import com.agcoding.cartrackingapp.domain.model.ConsumptionTrend
import com.agcoding.cartrackingapp.domain.model.TrendPeriod

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConsumptionGraphScreen(
    onNavigateBack: () -> Unit,
    viewModel: ConsumptionGraphViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedPeriod by viewModel.selectedPeriod.collectAsState()
    val showPeriodSelector by viewModel.showPeriodSelector.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Fuel Consumption") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    // Period selector button
                    TextButton(
                        onClick = { viewModel.showPeriodSelector() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(selectedPeriod.label)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        when (val state = uiState) {
            is ConsumptionGraphUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is ConsumptionGraphUiState.NoData -> {
                NoDataState(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }

            is ConsumptionGraphUiState.Success -> {
                ConsumptionGraphContent(
                    trendData = state.trendData,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }

            is ConsumptionGraphUiState.Error -> {
                ErrorState(
                    message = state.message,
                    onRetry = viewModel::retry,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }
        }

        // Period Selector Bottom Sheet
        if (showPeriodSelector) {
            ModalBottomSheet(
                onDismissRequest = { viewModel.hidePeriodSelector() }
            ) {
                PeriodSelectorSheet(
                    selectedPeriod = selectedPeriod,
                    onPeriodSelected = { period ->
                        viewModel.selectPeriod(period)
                    }
                )
            }
        }
    }
}

@Composable
private fun ConsumptionGraphContent(
    trendData: com.agcoding.cartrackingapp.domain.model.ConsumptionTrendData,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Text(
            text = "Track your consumption over time",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Statistics Cards Grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                label = "Average",
                value = "%.1f L/100km".format(trendData.overallAverage),
                modifier = Modifier.weight(1f)
            )

            TrendCard(
                label = "Trend",
                trend = trendData.trend,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                label = "Best",
                value = "%.1f L/100km".format(trendData.bestConsumption),
                valueColor = Color(0xFF34C759),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                label = "Worst",
                value = "%.1f L/100km".format(trendData.worstConsumption),
                valueColor = Color(0xFFFF3B30),
                modifier = Modifier.weight(1f)
            )
        }

        // Main Graph Card
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
                Text(
                    text = "Consumption History",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // The actual line graph
                ConsumptionLineGraph(
                    dataPoints = trendData.dataPoints,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                )
            }
        }

        // Refills info
        Text(
            text = "All Refills (${trendData.totalRefills})",
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
private fun ConsumptionLineGraph(
    dataPoints: List<ConsumptionDataPoint>,
    modifier: Modifier = Modifier
) {
    if (dataPoints.isEmpty()) return

    var animationProgress by remember { mutableStateOf(0f) }
    val animatedProgress by animateFloatAsState(
        targetValue = animationProgress,
        animationSpec = tween(durationMillis = 1000),
        label = "graph_animation"
    )

    LaunchedEffect(dataPoints) {
        animationProgress = 1f
    }

    val graphColor = MaterialTheme.colorScheme.primary
    val gridColor = MaterialTheme.colorScheme.outlineVariant

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height
        val padding = 40f

        // Calculate min/max for Y-axis with some padding
        val minConsumption = dataPoints.minOf { it.averageConsumption }
        val maxConsumption = dataPoints.maxOf { it.averageConsumption }
        val range = maxConsumption - minConsumption
        val yPadding = range * 0.2 // Add 20% padding
        val yMin = (minConsumption - yPadding).coerceAtLeast(0.0)
        val yMax = maxConsumption + yPadding

        // Draw horizontal grid lines
        val gridLines = 5
        for (i in 0..gridLines) {
            val y = padding + (height - 2 * padding) * i / gridLines
            drawLine(
                color = gridColor,
                start = Offset(padding, y),
                end = Offset(width - padding, y),
                strokeWidth = 1f,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f))
            )
        }

        // Convert data points to screen coordinates
        val points = dataPoints.mapIndexed { index, point ->
            val x = padding + (width - 2 * padding) * index / (dataPoints.size - 1).coerceAtLeast(1)
            val normalizedY = ((point.averageConsumption - yMin) / (yMax - yMin)).toFloat()
            val y = height - padding - (height - 2 * padding) * normalizedY
            Offset(x, y)
        }

        // Draw the line with animation
        if (points.size > 1) {
            val path = Path()
            path.moveTo(points[0].x, points[0].y)

            for (i in 1 until points.size) {
                val progress = (i.toFloat() / points.size) * animatedProgress
                if (progress >= 1f || i < points.size * animatedProgress) {
                    path.lineTo(points[i].x, points[i].y)
                }
            }

            drawPath(
                path = path,
                color = graphColor,
                style = Stroke(
                    width = 4f,
                    cap = StrokeCap.Round
                )
            )
        }

        // Draw data points
        points.forEachIndexed { index, point ->
            val progress = (index.toFloat() / points.size) * animatedProgress
            if (progress >= 1f || index < points.size * animatedProgress) {
                drawCircle(
                    color = graphColor,
                    radius = 6f,
                    center = point
                )
                drawCircle(
                    color = Color.White,
                    radius = 3f,
                    center = point
                )
            }
        }
    }

    // X-axis labels below the graph
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        if (dataPoints.isNotEmpty()) {
            Text(
                text = dataPoints.first().label,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (dataPoints.size > 2) {
                Text(
                    text = dataPoints[dataPoints.size / 2].label,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = dataPoints.last().label,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
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
                text = label,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = valueColor
            )
        }
    }
}

@Composable
private fun TrendCard(
    label: String,
    trend: ConsumptionTrend,
    modifier: Modifier = Modifier
) {
    val (icon, color, text) = when (trend) {
        ConsumptionTrend.IMPROVING -> Triple(
            Icons.AutoMirrored.Filled.TrendingDown,
            Color(0xFF34C759),
            "Improving"
        )
        ConsumptionTrend.WORSENING -> Triple(
            Icons.AutoMirrored.Filled.TrendingUp,
            Color(0xFFFF3B30),
            "Worsening"
        )
        ConsumptionTrend.STABLE -> Triple(
            Icons.Default.Remove,
            MaterialTheme.colorScheme.onSurface,
            "Stable"
        )
    }

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
                text = label,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = text,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = color
                )
            }
        }
    }
}

@Composable
private fun PeriodSelectorSheet(
    selectedPeriod: TrendPeriod,
    onPeriodSelected: (TrendPeriod) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp)
    ) {
        Text(
            text = "Select Time Period",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
        )

        TrendPeriod.values().filter { it != TrendPeriod.CUSTOM }.forEach { period ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onPeriodSelected(period) }
                    .padding(horizontal = 24.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = period.label,
                    fontSize = 16.sp,
                    color = if (selectedPeriod == period)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.onSurface
                )
                if (selectedPeriod == period) {
                    RadioButton(
                        selected = true,
                        onClick = null
                    )
                }
            }
        }
    }
}

@Composable
private fun NoDataState(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Not enough data yet",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = "Add at least 2 refills to see trends",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = message,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.error
            )
            TextButton(onClick = onRetry) {
                Text("Retry")
            }
        }
    }
}

