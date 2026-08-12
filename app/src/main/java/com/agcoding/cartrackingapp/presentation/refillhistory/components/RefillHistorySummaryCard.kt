package com.agcoding.cartrackingapp.presentation.refillhistory.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.EuroSymbol
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Route
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.presentation.components.StyledCard
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme
import com.agcoding.cartrackingapp.util.formatMoney
import com.agcoding.cartrackingapp.util.formatNumber

@Composable
fun RefillHistorySummaryCard(
    carName: String,
    refillCount: Int,
    totalCost: Double,
    totalLiters: Double,
    totalDistance: Double,
    avgConsumption: Double,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header card
        StyledCard {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = carName,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stringResource(R.string.refill_history_title),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Summary stats
        if (refillCount > 0) {
            StyledCard(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                border = null
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.total_spending),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    SummaryItem(
                        icon = Icons.Default.LocalGasStation,
                        label = stringResource(R.string.total_refills),
                        value = "$refillCount"
                    )

                    SummaryItem(
                        icon = Icons.Default.EuroSymbol,
                        label = stringResource(R.string.total_cost),
                        value = totalCost.formatMoney()
                    )

                    SummaryItem(
                        icon = Icons.Default.LocalGasStation,
                        label = stringResource(R.string.total_liters_label),
                        value = "${totalLiters.formatNumber(1)} L"
                    )

                    SummaryItem(
                        icon = Icons.Default.Route,
                        label = stringResource(R.string.total_distance),
                        value = "${totalDistance.formatNumber(0)} km"
                    )

                    SummaryItem(
                        icon = Icons.AutoMirrored.Filled.TrendingUp,
                        label = stringResource(R.string.avg_consumption),
                        value = if (avgConsumption > 0) "${avgConsumption.formatNumber(1)} L/100km" else "-"
                    )
                }
            }
        }
    }
}

// ============================================
// Preview Composables
// ============================================

@Preview(name = "Summary Card - Normal", showBackground = true, widthDp = 350)
@Composable
private fun PreviewRefillHistorySummaryCard() {
    CarTrackingAppTheme(darkTheme = false) {
        RefillHistorySummaryCard(
            carName = "Toyota Corolla",
            refillCount = 12,
            totalCost = 850.50,
            totalLiters = 520.5,
            totalDistance = 6500.0,
            avgConsumption = 8.0,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(name = "Summary Card - No Refills", showBackground = true, widthDp = 350)
@Composable
private fun PreviewRefillHistorySummaryCardEmpty() {
    CarTrackingAppTheme(darkTheme = false) {
        RefillHistorySummaryCard(
            carName = "Honda Civic",
            refillCount = 0,
            totalCost = 0.0,
            totalLiters = 0.0,
            totalDistance = 0.0,
            avgConsumption = 0.0,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(name = "Summary Card - Dark", showBackground = true, widthDp = 350)
@Composable
private fun PreviewRefillHistorySummaryCardDark() {
    CarTrackingAppTheme(darkTheme = true) {
        RefillHistorySummaryCard(
            carName = "BMW 320i",
            refillCount = 24,
            totalCost = 1580.75,
            totalLiters = 980.2,
            totalDistance = 12400.0,
            avgConsumption = 7.9,
            modifier = Modifier.padding(16.dp)
        )
    }
}
