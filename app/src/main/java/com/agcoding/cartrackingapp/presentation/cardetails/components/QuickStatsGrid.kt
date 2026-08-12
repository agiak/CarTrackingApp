package com.agcoding.cartrackingapp.presentation.cardetails.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.EuroSymbol
import androidx.compose.material.icons.filled.Route
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.domain.model.Car
import com.agcoding.cartrackingapp.domain.model.CarStatistics
import com.agcoding.cartrackingapp.presentation.components.StyledCard
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme
import com.agcoding.cartrackingapp.util.formatMoney
import com.agcoding.cartrackingapp.util.formatNumber

@Composable
fun QuickStatsGrid(statistics: CarStatistics) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QuickStatCard(
            icon = Icons.AutoMirrored.Filled.TrendingUp,
            label = stringResource(R.string.avg_consumption_short),
            value = if (statistics.averageConsumption > 0) {
                statistics.averageConsumption.formatNumber(1)
            } else "-",
            unit = "L/100km",
            modifier = Modifier.weight(1f)
        )

        QuickStatCard(
            icon = Icons.Default.Route,
            label = stringResource(R.string.distance),
            value = statistics.totalDistance.toInt().formatNumber(),
            unit = "km",
            modifier = Modifier.weight(1f)
        )

        QuickStatCard(
            icon = Icons.Default.EuroSymbol,
            label = "Cost/km",
            value = if (statistics.costPerKilometer > 0) {
                statistics.costPerKilometer.formatMoney()
            } else 0.0.formatMoney(),
            unit = "",
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun QuickStatCard(
    icon: ImageVector,
    label: String,
    value: String,
    unit: String,
    modifier: Modifier = Modifier
) {
    StyledCard(
        modifier = modifier
            .height(160.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp),
                contentAlignment = Alignment.Center
            ) {
                StyledCard(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f),
                    border = null
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier
                            .size(40.dp)
                            .padding(8.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = label,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 2,
                lineHeight = 14.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )

            if (unit.isNotEmpty()) {
                Text(
                    text = unit,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
    }
}

// ============================================
// Preview Composables
// ============================================

@Preview(name = "Quick Stats Grid - Light", showBackground = true, widthDp = 400)
@Composable
private fun PreviewQuickStatsGrid() {
    CarTrackingAppTheme(darkTheme = false) {
        QuickStatsGrid(
            statistics = CarStatistics(
                car = Car(
                    id = 1,
                    name = "Toyota Corolla",
                    licensePlate = "ABC-1234",
                    currentOdometer = 45000.0,
                    initialOdometer = 0.0
                ),
                totalRefills = 120,
                averageConsumption = 6.5,
                totalCost = 8500.0,
                totalDistance = 45000.0,
                costPerKilometer = 0.19,
                serviceExpensesCost = 1200.0,
                serviceExpenseCount = 8,
                otherExpensesCost = 300.0,
                otherExpenseCount = 5,
                averagePricePerLiter = 1.65,
                totalLiters = 5200.0,
                recentRefills = emptyList()
            )
        )
    }
}

@Preview(name = "Quick Stats Grid - Dark", showBackground = true, widthDp = 400)
@Composable
private fun PreviewQuickStatsGridDark() {
    CarTrackingAppTheme(darkTheme = true) {
        QuickStatsGrid(
            statistics = CarStatistics(
                car = Car(
                    id = 1,
                    name = "BMW 320i",
                    licensePlate = "XYZ-5678",
                    currentOdometer = 82000.0,
                    initialOdometer = 0.0
                ),
                totalRefills = 200,
                averageConsumption = 7.8,
                totalCost = 15000.0,
                totalDistance = 82000.0,
                costPerKilometer = 0.25,
                serviceExpensesCost = 2500.0,
                serviceExpenseCount = 15,
                otherExpensesCost = 800.0,
                otherExpenseCount = 10,
                averagePricePerLiter = 1.70,
                totalLiters = 8800.0,
                recentRefills = emptyList()
            )
        )
    }
}

@Preview(name = "Single Quick Stat Card", showBackground = true, widthDp = 120)
@Composable
private fun PreviewQuickStatCard() {
    CarTrackingAppTheme(darkTheme = false) {
        QuickStatCard(
            icon = Icons.AutoMirrored.Filled.TrendingUp,
            label = "Avg Consumption",
            value = "6.5",
            unit = "L/100km"
        )
    }
}
