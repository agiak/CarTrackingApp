package com.agcoding.cartrackingapp.presentation.tripsanalytics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agcoding.cartrackingapp.domain.model.Trip
import com.agcoding.cartrackingapp.domain.model.TripStatistics
import com.agcoding.cartrackingapp.presentation.components.StyledCard
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme
import com.agcoding.cartrackingapp.util.formatMoney
import com.agcoding.cartrackingapp.util.formatNumber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
internal fun TripAnalyticsItem(
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

@Preview(showBackground = true)
@Composable
private fun TripAnalyticsItemPreview() {
    CarTrackingAppTheme {
        TripAnalyticsItem(
            tripStats = TripStatistics(
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
            ),
            onClick = {}
        )
    }
}
