package com.agcoding.cartrackingapp.presentation.distancegraph

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.domain.model.DistanceTrendData
import com.agcoding.cartrackingapp.domain.model.MonthlyDistance
import com.agcoding.cartrackingapp.domain.model.TripInfo
import com.agcoding.cartrackingapp.presentation.components.ChartDataPoint
import com.agcoding.cartrackingapp.presentation.components.InteractiveLineChart
import com.agcoding.cartrackingapp.presentation.components.PeriodSelectorSheet
import com.agcoding.cartrackingapp.presentation.components.StyledCard
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DistanceGraphScreen(
    onNavigateBack: () -> Unit,
    viewModel: DistanceGraphViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedPeriod by viewModel.selectedPeriod.collectAsState()
    val showPeriodSelector by viewModel.showPeriodSelector.collectAsState()
    val allCars by viewModel.allCars.collectAsState()
    val selectedCarIds by viewModel.selectedCarIds.collectAsState()
    val showCarFilter by viewModel.showCarFilter.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                actions = {
                    // Car filter button
                    if (allCars.size > 1) {
                        TextButton(
                            onClick = { viewModel.showCarFilter() },
                            modifier = Modifier.width(160.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DirectionsCar,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            val selectedCars = allCars.filter { selectedCarIds.contains(it.id) }
                            Text(
                                text = when {
                                    selectedCars.isEmpty() -> stringResource(R.string.all_cars)
                                    selectedCars.size == 1 -> selectedCars[0].name
                                    else -> selectedCars.joinToString(", ") { it.name }
                                },
                                fontSize = 14.sp
                            )
                        }
                    }

                    // Period selector button
                    TextButton(
                        onClick = { viewModel.showPeriodSelector() }
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(selectedPeriod.labelResId),
                            fontSize = 14.sp
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        when (val state = uiState) {
            is DistanceGraphUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is DistanceGraphUiState.NoData -> {
                NoDataState(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }

            is DistanceGraphUiState.Success -> {
                DistanceGraphContent(
                    trendData = state.trendData,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }

            is DistanceGraphUiState.Error -> {
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
                    title = stringResource(R.string.distance_graph_select_period_title),
                    selectedPeriod = selectedPeriod,
                    onPeriodSelected = { period ->
                        viewModel.selectPeriod(period)
                    }
                )
            }
        }

        // Car Filter Bottom Sheet
        if (showCarFilter) {
            com.agcoding.cartrackingapp.presentation.components.CarFilterSheet(
                cars = allCars,
                selectedCarIds = selectedCarIds,
                onCarSelectionChanged = { carId, selected ->
                    viewModel.toggleCarSelection(carId, selected)
                },
                onDismiss = { viewModel.hideCarFilter() },
                onApply = { viewModel.applyCarFilter() }
            )
        }
    }
}

@Composable
private fun DistanceGraphContent(
    trendData: DistanceTrendData,
    modifier: Modifier = Modifier
) {
    val numberFormat = remember { NumberFormat.getNumberInstance(Locale.getDefault()) }
    val tripDatePattern = stringResource(R.string.distance_graph_trip_date_format)
    val tripDateFormat = remember(tripDatePattern) {
        SimpleDateFormat(tripDatePattern, Locale.getDefault())
    }

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
                            imageVector = Icons.Default.Navigation,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Title and subtitle
                Text(
                    text = stringResource(R.string.distance_graph_title),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = stringResource(R.string.distance_graph_subtitle),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )


                Spacer(modifier = Modifier.height(8.dp))

                // Total Distance Card (Main highlight card)
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
                                imageVector = Icons.Default.Navigation,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = stringResource(R.string.distance_graph_total_distance_label),
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = stringResource(
                                R.string.distance_graph_km_format,
                                numberFormat.format(trendData.totalDistance.toLong())
                            ),
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                // Stats: Average, Longest, Shortest Trip
                StatCard(
                    label = stringResource(R.string.distance_graph_average_label),
                    value = stringResource(
                        R.string.distance_graph_km_format,
                        numberFormat.format(trendData.averageTripDistance.toLong())
                    ),
                    indicatorColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth()
                )
                StatCard(
                    label = stringResource(R.string.distance_graph_longest_trip_label),
                    value = stringResource(
                        R.string.distance_graph_km_format,
                        numberFormat.format(trendData.longestTrip.toLong())
                    ),
                    indicatorColor = Color(0xFF34C759),
                    modifier = Modifier.fillMaxWidth()
                )
                StatCard(
                    label = stringResource(R.string.distance_graph_shortest_trip_label),
                    value = stringResource(
                        R.string.distance_graph_km_format,
                        numberFormat.format(trendData.shortestTrip.toLong())
                    ),
                    indicatorColor = Color(0xFFFF9500),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Right side: Graph and Recent Trips (65%)
            LazyColumn(
                modifier = Modifier
                    .weight(0.65f)
                    .fillMaxHeight(),
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Monthly Distance Line Chart
                if (trendData.monthlyDistances.isNotEmpty()) {
                    item {
                        StyledCard(modifier = Modifier.fillMaxWidth()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = stringResource(R.string.distance_graph_monthly_distance_title),
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                InteractiveLineChart(
                                    dataPoints = trendData.monthlyDistances.map { monthData ->
                                        ChartDataPoint(
                                            label = "${monthData.month} ${monthData.year}",
                                            value = monthData.distance,
                                            formattedValue = "${String.format("%.0f", monthData.distance)} km"
                                        )
                                    },
                                    tooltipIcon = Icons.Default.Navigation,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }

                // Recent Trips Section
                if (trendData.recentTrips.isNotEmpty()) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.distance_graph_recent_trips_title),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            StyledCard(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                border = null
                            ) {
                                Text(
                                    text = stringResource(R.string.distance_graph_total_trips_format, trendData.totalTrips),
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    items(trendData.recentTrips.take(10)) { trip ->
                        TripItem(trip = trip, dateFormat = tripDateFormat)
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
                        imageVector = Icons.Default.Navigation,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Title and subtitle
            Text(
                text = stringResource(R.string.distance_graph_title),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = stringResource(R.string.distance_graph_subtitle),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )


            Spacer(modifier = Modifier.height(8.dp))

            // Total Distance Card (Main highlight card)
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
                            imageVector = Icons.Default.Navigation,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.distance_graph_total_distance_label),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(
                            R.string.distance_graph_km_format,
                            numberFormat.format(trendData.totalDistance.toLong())
                        ),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // Stats Grid Row 1: Average and Longest Trip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    label = stringResource(R.string.distance_graph_average_label),
                    value = stringResource(
                        R.string.distance_graph_km_format,
                        numberFormat.format(trendData.averageTripDistance.toLong())
                    ),
                    indicatorColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = stringResource(R.string.distance_graph_longest_trip_label),
                    value = stringResource(
                        R.string.distance_graph_km_format,
                        numberFormat.format(trendData.longestTrip.toLong())
                    ),
                    indicatorColor = Color(0xFF34C759),
                    modifier = Modifier.weight(1f)
                )
            }

            // Stats Row 2: Shortest Trip (full width)
            StatCard(
                label = stringResource(R.string.distance_graph_shortest_trip_label),
                value = stringResource(
                    R.string.distance_graph_km_format,
                    numberFormat.format(trendData.shortestTrip.toLong())
                ),
                indicatorColor = Color(0xFFFF9500),
                modifier = Modifier.fillMaxWidth()
            )

            // Monthly Distance Line Chart
            if (trendData.monthlyDistances.isNotEmpty()) {
                StyledCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.distance_graph_monthly_distance_title),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        InteractiveLineChart(
                            dataPoints = trendData.monthlyDistances.map { monthData ->
                                ChartDataPoint(
                                    label = "${monthData.month} ${monthData.year}",
                                    value = monthData.distance,
                                    formattedValue = "${String.format("%.0f", monthData.distance)} km"
                                )
                            },
                            tooltipIcon = Icons.Default.Navigation,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Recent Trips Section
            if (trendData.recentTrips.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.distance_graph_recent_trips_title),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    StyledCard(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        border = null
                    ) {
                        Text(
                            text = stringResource(R.string.distance_graph_total_trips_format, trendData.totalTrips),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Trip list
                trendData.recentTrips.take(10).forEach { trip ->
                    TripItem(trip = trip, dateFormat = tripDateFormat)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    indicatorColor: Color,
    modifier: Modifier = Modifier
) {
    StyledCard(
        modifier = modifier
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
                    text = label,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(indicatorColor)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}


@Composable
private fun MonthlyDistanceBarChart(
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
                            String.format("%.0fk", value / 1000)
                        } else {
                            String.format("%.0f", value)
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
                            color = Color.White,
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
                                color = Color.White,
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

@Composable
private fun TripItem(
    trip: TripInfo,
    dateFormat: SimpleDateFormat
) {
    val numberFormat = remember { NumberFormat.getNumberInstance(Locale.getDefault()) }

    StyledCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Color indicator bar
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(40.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Color(trip.carColor))
            )
            Spacer(modifier = Modifier.width(12.dp))

            // Car name and date
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = trip.carName,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = dateFormat.format(Date(trip.timestamp)),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Distance and liters
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = stringResource(
                        R.string.distance_graph_km_format,
                        numberFormat.format(trip.distance.toLong())
                    ),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = stringResource(R.string.distance_graph_liters_format, trip.liters),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}


@Composable
private fun NoDataState(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Navigation,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.distance_graph_no_trips_title),
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.distance_graph_no_trips_desc),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.error_title),
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = onRetry) {
                Text(stringResource(R.string.retry))
            }
        }
    }
}
