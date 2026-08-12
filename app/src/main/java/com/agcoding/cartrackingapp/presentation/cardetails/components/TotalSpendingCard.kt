package com.agcoding.cartrackingapp.presentation.cardetails.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EuroSymbol
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.domain.model.Car
import com.agcoding.cartrackingapp.domain.model.CarStatistics
import com.agcoding.cartrackingapp.presentation.components.StyledCard
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme
import com.agcoding.cartrackingapp.util.formatMoney

@Composable
fun TotalSpendingCard(statistics: CarStatistics) {
    val totalSpending = statistics.totalCost + statistics.serviceExpensesCost + statistics.otherExpensesCost

    StyledCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.EuroSymbol,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.total_spending),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = totalSpending.formatMoney(),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SpendingBreakdown(
                    label = stringResource(R.string.fuel),
                    amount = statistics.totalCost
                )
                SpendingBreakdown(
                    label = stringResource(R.string.service),
                    amount = statistics.serviceExpensesCost
                )
                SpendingBreakdown(
                    label = stringResource(R.string.other),
                    amount = statistics.otherExpensesCost
                )
            }
        }
    }
}

@Composable
fun SpendingBreakdown(label: String, amount: Double) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = amount.formatMoney(),
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

// ============================================
// Preview Composables
// ============================================

@Preview(name = "Total Spending Card - Light", showBackground = true, widthDp = 400)
@Composable
private fun PreviewTotalSpendingCard() {
    CarTrackingAppTheme(darkTheme = false) {
        TotalSpendingCard(
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

@Preview(name = "Total Spending Card - Dark", showBackground = true, widthDp = 400)
@Composable
private fun PreviewTotalSpendingCardDark() {
    CarTrackingAppTheme(darkTheme = true) {
        TotalSpendingCard(
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

@Preview(name = "Spending Breakdown Item", showBackground = true)
@Composable
private fun PreviewSpendingBreakdown() {
    CarTrackingAppTheme(darkTheme = false) {
        SpendingBreakdown(
            label = "Fuel",
            amount = 8500.0
        )
    }
}
