package com.agcoding.cartrackingapp.presentation.tripsanalytics

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.domain.model.TripStatistics
import com.agcoding.cartrackingapp.util.formatMoney
import com.agcoding.cartrackingapp.util.formatNumber
import com.agcoding.cartrackingapp.presentation.components.StyledCard
import com.agcoding.cartrackingapp.presentation.components.StyledTopAppBar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripsAnalyticsScreen(
    onNavigateBack: () -> Unit,
    onTripClick: (Long) -> Unit,
    viewModel: TripsAnalyticsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val sortOption by viewModel.sortOption.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            StyledTopAppBar(
                title = { Text(stringResource(R.string.trips_analytics_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is TripsAnalyticsUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is TripsAnalyticsUiState.Empty -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Route,
                            contentDescription = null,
                            modifier = Modifier.size(80.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                        )
                        Text(
                            text = stringResource(R.string.trips_analytics_no_trips_title),
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Text(
                            text = stringResource(R.string.trips_analytics_no_trips_desc),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }

            is TripsAnalyticsUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(state.message, color = MaterialTheme.colorScheme.error)
                }
            }

            is TripsAnalyticsUiState.Success -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // ── Section A: Trip Highlights ──────────────────────────────
                    item {
                        Text(
                            text = stringResource(R.string.trips_analytics_highlights_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    item {
                        HighlightsSection(highlights = state.highlights)
                    }

                    // ── Section B: Sorted Trips List ────────────────────────────
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(R.string.trips_analytics_all_trips_title),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${state.sortedTrips.size}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    item {
                        SortChipsRow(
                            activeSortOption = sortOption,
                            onSortChange = { viewModel.setSortOption(it) }
                        )
                    }

                    items(state.sortedTrips, key = { it.trip.id }) { tripStats ->
                        TripAnalyticsItem(
                            tripStats = tripStats,
                            onClick = { onTripClick(tripStats.trip.id) }
                        )
                    }
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Highlights Section
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun HighlightsSection(highlights: TripHighlights) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Row 1: Most Costly / Cheapest
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            HighlightCard(
                label = stringResource(R.string.trips_analytics_most_costly),
                tripStats = highlights.mostCostly,
                metricValue = highlights.mostCostly?.let {
                    it.totalCost.formatMoney()
                },
                icon = Icons.Default.AttachMoney,
                modifier = Modifier.weight(1f)
            )
            HighlightCard(
                label = stringResource(R.string.trips_analytics_cheapest),
                tripStats = highlights.cheapest,
                metricValue = highlights.cheapest?.let {
                    it.totalCost.formatMoney()
                },
                icon = Icons.Default.AttachMoney,
                modifier = Modifier.weight(1f)
            )
        }

        // Row 2: Longest / Shortest Distance
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            HighlightCard(
                label = stringResource(R.string.trips_analytics_longest),
                tripStats = highlights.longestDistance,
                metricValue = highlights.longestDistance?.let {
                    "${it.totalDistance.formatNumber(0)} km"
                },
                icon = Icons.Default.Route,
                modifier = Modifier.weight(1f)
            )
            HighlightCard(
                label = stringResource(R.string.trips_analytics_shortest),
                tripStats = highlights.shortestDistance,
                metricValue = highlights.shortestDistance?.let {
                    "${it.totalDistance.formatNumber(0)} km"
                },
                icon = Icons.Default.Route,
                modifier = Modifier.weight(1f)
            )
        }

        // Row 3: Most / Least Fuel Efficient
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            HighlightCard(
                label = stringResource(R.string.trips_analytics_most_efficient),
                tripStats = highlights.mostFuelEfficient,
                metricValue = highlights.mostFuelEfficient?.let {
                    "${it.averageConsumption.formatNumber(2)} L/100km"
                },
                icon = Icons.Default.LocalGasStation,
                modifier = Modifier.weight(1f)
            )
            HighlightCard(
                label = stringResource(R.string.trips_analytics_least_efficient),
                tripStats = highlights.leastFuelEfficient,
                metricValue = highlights.leastFuelEfficient?.let {
                    "${it.averageConsumption.formatNumber(2)} L/100km"
                },
                icon = Icons.Default.LocalGasStation,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun HighlightCard(
    label: String,
    tripStats: TripStatistics?,
    metricValue: String?,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    StyledCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = label,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            if (tripStats != null && metricValue != null) {
                Text(
                    text = metricValue,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = tripStats.trip.name,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                tripStats.startDate?.let { start ->
                    val dateStr = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
                        .format(Date(start))
                    Text(
                        text = dateStr,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                Text(
                    text = "—",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Sort Chips Row
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun SortChipsRow(
    activeSortOption: TripSortOption,
    onSortChange: (TripSortOption) -> Unit
) {
    val options = listOf(
        TripSortOption.MOST_RECENT to stringResource(R.string.trips_sort_most_recent),
        TripSortOption.MOST_COSTLY to stringResource(R.string.trips_sort_most_costly),
        TripSortOption.LEAST_COSTLY to stringResource(R.string.trips_sort_least_costly),
        TripSortOption.HIGHEST_FUEL_CONSUMPTION to stringResource(R.string.trips_sort_highest_consumption),
        TripSortOption.LOWEST_FUEL_CONSUMPTION to stringResource(R.string.trips_sort_lowest_consumption),
        TripSortOption.LONGEST_DISTANCE to stringResource(R.string.trips_sort_longest),
        TripSortOption.SHORTEST_DISTANCE to stringResource(R.string.trips_sort_shortest)
    )

    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { (option, label) ->
            FilterChip(
                selected = activeSortOption == option,
                onClick = { onSortChange(option) },
                label = {
                    Text(
                        text = label,
                        fontSize = 12.sp,
                        maxLines = 1
                    )
                },
                shape = RoundedCornerShape(20.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Trip Analytics Item
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun TripAnalyticsItem(
    tripStats: TripStatistics,
    onClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
    val startStr = tripStats.startDate?.let { dateFormat.format(Date(it)) } ?: "—"
    val endStr = tripStats.endDate?.let { dateFormat.format(Date(it)) } ?: "—"
    val dateRange = if (tripStats.startDate != null && tripStats.endDate != null &&
        tripStats.startDate != tripStats.endDate
    ) "$startStr – $endStr" else startStr

    StyledCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = tripStats.trip.name,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = dateRange,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = tripStats.totalCost.formatMoney(),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                TripMetricChip(
                    icon = Icons.Default.Route,
                    value = "${tripStats.totalDistance.formatNumber(0)} km"
                )
                TripMetricChip(
                    icon = Icons.Default.LocalGasStation,
                    value = if (tripStats.averageConsumption > 0)
                        "${tripStats.averageConsumption.formatNumber(2)} L/100km"
                    else "—"
                )
                TripMetricChip(
                    icon = Icons.Default.Star,
                    value = "${tripStats.refillCount} refills"
                )
            }
        }
    }
}

@Composable
private fun TripMetricChip(
    icon: ImageVector,
    value: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(13.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(3.dp))
        Text(
            text = value,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

