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
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Divider
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

@Composable
fun CarHeaderCard(statistics: CarStatistics) {
    StyledCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        border = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Bigger car icon
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .padding(end = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    StyledCard(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                        border = null
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsCar,
                            contentDescription = null,
                            modifier = Modifier
                                .size(80.dp)
                                .padding(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Car info
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = statistics.car.name,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = statistics.car.licensePlate,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Separator line
            Spacer(modifier = Modifier.height(16.dp))
            Divider(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(16.dp))

            // Current odometer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Speed,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.current_odometer),
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = "${String.format("%,d", statistics.car.currentOdometer.toInt())} km",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

// ============================================
// Preview Composables
// ============================================

@Preview(name = "Car Header Card - Light", showBackground = true)
@Composable
private fun PreviewCarHeaderCard() {
    CarTrackingAppTheme(darkTheme = false) {
        CarHeaderCard(
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

@Preview(name = "Car Header Card - Dark", showBackground = true)
@Composable
private fun PreviewCarHeaderCardDark() {
    CarTrackingAppTheme(darkTheme = true) {
        CarHeaderCard(
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
