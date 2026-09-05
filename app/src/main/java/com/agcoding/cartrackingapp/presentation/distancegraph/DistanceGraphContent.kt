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
import com.agcoding.cartrackingapp.presentation.components.InteractiveBarChart
import com.agcoding.cartrackingapp.presentation.components.InteractiveLineChart
import com.agcoding.cartrackingapp.presentation.components.StyledCard
import com.agcoding.cartrackingapp.domain.model.DateRange
import com.agcoding.cartrackingapp.domain.model.DistanceDataPoint
import com.agcoding.cartrackingapp.presentation.components.StyledTopAppBar
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme
import com.agcoding.cartrackingapp.util.formatNumber
import androidx.compose.ui.tooling.preview.Preview
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
internal fun DistanceGraphContent(
    trendData: DistanceTrendData,
    modifier: Modifier = Modifier
) {
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
                                trendData.totalDistance.toLong().formatNumber()
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
                        trendData.averageTripDistance.toLong().formatNumber()
                    ),
                    indicatorColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth()
                )
                StatCard(
                    label = stringResource(R.string.distance_graph_longest_trip_label),
                    value = stringResource(
                        R.string.distance_graph_km_format,
                        trendData.longestTrip.toLong().formatNumber()
                    ),
                    indicatorColor = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.fillMaxWidth()
                )
                StatCard(
                    label = stringResource(R.string.distance_graph_shortest_trip_label),
                    value = stringResource(
                        R.string.distance_graph_km_format,
                        trendData.shortestTrip.toLong().formatNumber()
                    ),
                    indicatorColor = MaterialTheme.colorScheme.secondary,
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

                                InteractiveBarChart(
                                    dataPoints = trendData.monthlyDistances.map { monthData ->
                                        ChartDataPoint(
                                            label = if (monthData.month.contains(monthData.year.toString())) monthData.month else "${monthData.month} ${monthData.year}",
                                            value = monthData.distance,
                                            formattedValue = "${monthData.distance.formatNumber(0)} km"
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
                            trendData.totalDistance.toLong().formatNumber()
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
                        trendData.averageTripDistance.toLong().formatNumber()
                    ),
                    indicatorColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = stringResource(R.string.distance_graph_longest_trip_label),
                    value = stringResource(
                        R.string.distance_graph_km_format,
                        trendData.longestTrip.toLong().formatNumber()
                    ),
                    indicatorColor = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.weight(1f)
                )
            }

            // Stats Row 2: Shortest Trip (full width)
            StatCard(
                label = stringResource(R.string.distance_graph_shortest_trip_label),
                value = stringResource(
                    R.string.distance_graph_km_format,
                    trendData.shortestTrip.toLong().formatNumber()
                ),
                indicatorColor = MaterialTheme.colorScheme.secondary,
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

                        InteractiveBarChart(
                            dataPoints = trendData.monthlyDistances.map { monthData ->
                                ChartDataPoint(
                                    label = if (monthData.month.contains(monthData.year.toString())) monthData.month else "${monthData.month} ${monthData.year}",
                                    value = monthData.distance,
                                    formattedValue = "${monthData.distance.formatNumber(0)} km"
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

// ============================================
// Preview Composables
// ============================================

@Preview(name = "Distance Graph Content - Portrait", showBackground = true, showSystemUi = true)
@Composable
private fun PreviewDistanceGraphContent() {
    CarTrackingAppTheme(darkTheme = false) {
        DistanceGraphContent(
            trendData = DistanceTrendData(
                dataPoints = listOf(
                    DistanceDataPoint(System.currentTimeMillis(), 450.0, 5, "Jan"),
                    DistanceDataPoint(System.currentTimeMillis() + 2592000000L, 520.0, 6, "Feb"),
                    DistanceDataPoint(System.currentTimeMillis() + 5184000000L, 480.0, 5, "Mar")
                ),
                totalDistance = 1450.0,
                averageTripDistance = 85.0,
                longestTrip = 450.0,
                shortestTrip = 20.0,
                totalTrips = 16,
                recentTrips = listOf(
                    TripInfo(1, 1, "Toyota Corolla", System.currentTimeMillis(), 450.0, 35.0, 0xFF4CAF50.toInt()),
                    TripInfo(2, 1, "Toyota Corolla", System.currentTimeMillis() - 86400000, 120.0, 10.0, 0xFF4CAF50.toInt())
                ),
                dateRange = DateRange(System.currentTimeMillis() - 7776000000L, System.currentTimeMillis(), "Last 3 months"),
                monthlyDistances = listOf(
                    MonthlyDistance("Jan", 2026, 450.0, System.currentTimeMillis()),
                    MonthlyDistance("Feb", 2026, 520.0, System.currentTimeMillis() + 2592000000L),
                    MonthlyDistance("Mar", 2026, 480.0, System.currentTimeMillis() + 5184000000L)
                )
            )
        )
    }
}

@Preview(
    name = "Distance Graph Content - Landscape",
    showBackground = true,
    device = "spec:width=800dp,height=480dp,dpi=240,orientation=landscape"
)
@Composable
private fun PreviewDistanceGraphContentLandscape() {
    CarTrackingAppTheme(darkTheme = false) {
        DistanceGraphContent(
            trendData = DistanceTrendData(
                dataPoints = listOf(
                    DistanceDataPoint(System.currentTimeMillis(), 800.0, 8, "Q1"),
                    DistanceDataPoint(System.currentTimeMillis() + 7776000000L, 1200.0, 12, "Q2")
                ),
                totalDistance = 2000.0,
                averageTripDistance = 100.0,
                longestTrip = 600.0,
                shortestTrip = 30.0,
                totalTrips = 20,
                recentTrips = listOf(
                    TripInfo(1, 2, "BMW 320i", System.currentTimeMillis(), 600.0, 50.0, 0xFF2196F3.toInt())
                ),
                dateRange = DateRange(System.currentTimeMillis() - 15552000000L, System.currentTimeMillis(), "Last 6 months"),
                monthlyDistances = listOf(
                    MonthlyDistance("Q1", 2026, 800.0, System.currentTimeMillis()),
                    MonthlyDistance("Q2", 2026, 1200.0, System.currentTimeMillis() + 7776000000L)
                )
            )
        )
    }
}
