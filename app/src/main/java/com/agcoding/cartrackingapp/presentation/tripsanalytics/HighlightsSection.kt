package com.agcoding.cartrackingapp.presentation.tripsanalytics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Route
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.domain.model.Trip
import com.agcoding.cartrackingapp.domain.model.TripStatistics
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme
import com.agcoding.cartrackingapp.util.formatMoney
import com.agcoding.cartrackingapp.util.formatNumber

@Composable
internal fun HighlightsSection(highlights: TripHighlights) {
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

@Preview(showBackground = true)
@Composable
private fun HighlightsSectionPreview() {
    val sampleStats = TripStatistics(
        trip = Trip(
            id = 1,
            carId = 1,
            name = "Weekend Getaway",
            description = "Trip to the mountains",
            createdAt = 0L,
            updatedAt = 0L
        ),
        totalDistance = 420.0,
        totalFuelConsumed = 32.5,
        totalCost = 68.0,
        averageConsumption = 7.7,
        refillCount = 2,
        startDate = 0L,
        endDate = 0L
    )
    CarTrackingAppTheme {
        HighlightsSection(
            highlights = TripHighlights(
                mostCostly = sampleStats,
                cheapest = sampleStats,
                longestDistance = sampleStats,
                shortestDistance = sampleStats,
                mostFuelEfficient = sampleStats,
                leastFuelEfficient = sampleStats
            )
        )
    }
}
