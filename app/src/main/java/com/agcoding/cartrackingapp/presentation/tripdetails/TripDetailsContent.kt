package com.agcoding.cartrackingapp.presentation.tripdetails

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.domain.model.Trip
import com.agcoding.cartrackingapp.domain.model.TripStatistics
import com.agcoding.cartrackingapp.presentation.components.RefillItemCard
import com.agcoding.cartrackingapp.presentation.components.StyledCard
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme
import com.agcoding.cartrackingapp.util.formatMoney
import com.agcoding.cartrackingapp.util.formatNumber
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TripDetailsContent(
    tripStatistics: TripStatistics,
    onRefillClick: (Long) -> Unit,
    onAddRefills: () -> Unit = {},
    onRemoveRefill: (Long) -> Unit = {},
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Trip Header
        item {
            StyledCard(
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = tripStatistics.trip.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    if (tripStatistics.trip.description != null && tripStatistics.trip.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = tripStatistics.trip.description,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(
                            R.string.trip_created_format,
                            SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(tripStatistics.trip.createdAt))
                        ),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                }
            }
        }

        // Statistics Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = stringResource(R.string.stat_refills),
                    value = tripStatistics.refillCount.toString(),
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = stringResource(R.string.stat_total_cost),
                    value = tripStatistics.totalCost.formatMoney(),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard(
                    title = stringResource(R.string.stat_distance),
                    value = "${tripStatistics.totalDistance.formatNumber(1)} km",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = stringResource(R.string.stat_avg_consumption),
                    value = "${tripStatistics.averageConsumption.formatNumber(2)} L/100km",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            StatCard(
                title = stringResource(R.string.stat_total_fuel),
                value = "${tripStatistics.totalFuelConsumed.formatNumber(2)} L",
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Refills Section Header with + button
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.refills_section_header, tripStatistics.refillCount),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(
                    onClick = onAddRefills,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.add_refills_to_trip_cd),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        // Refills List
        items(tripStatistics.trip.refills) { refill ->
            Box(modifier = Modifier.fillMaxWidth()) {
                RefillItemCard(
                    refill = refill,
                    carName = null,
                    onClick = { onRefillClick(refill.id) }
                )

                // Remove bubble with minus at top right corner
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 8.dp, end = 8.dp)
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.errorContainer)
                        .clickable { onRemoveRefill(refill.id) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "−",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TripDetailsContentPreview() {
    CarTrackingAppTheme {
        TripDetailsContent(
            tripStatistics = TripStatistics(
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
            onRefillClick = {}
        )
    }
}
