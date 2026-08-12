package com.agcoding.cartrackingapp.presentation.carlist.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EuroSymbol
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.domain.model.Car
import com.agcoding.cartrackingapp.presentation.components.StyledCard
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme
import com.agcoding.cartrackingapp.util.formatNumber

@Composable
fun CarCard(
    car: Car,
    onClick: () -> Unit,
    onAddRefillClick: () -> Unit,
    onAddServiceClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isTablet = com.agcoding.cartrackingapp.util.DeviceUtils.isTablet()
    val isLandscape = com.agcoding.cartrackingapp.util.DeviceUtils.isLandscape()
    val isCompactLayout = isTablet || isLandscape

    StyledCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(if (isCompactLayout) 12.dp else 16.dp)
        ) {
            // Header: Car name, license plate, and action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = car.name,
                            fontSize = if (isCompactLayout) 16.sp else 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        if (car.isDefault) {
                            Text(
                                text = stringResource(R.string.default_badge),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier
                                    .background(
                                        color = MaterialTheme.colorScheme.primaryContainer,
                                        shape = RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = car.licensePlate,
                        fontSize = if (isCompactLayout) 13.sp else 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Action buttons row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(if (isCompactLayout) 2.dp else 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Service icon button
                    androidx.compose.material3.IconButton(
                        onClick = onAddServiceClick
                    ) {
                        Icon(
                            imageVector = Icons.Default.Build,
                            contentDescription = stringResource(R.string.car_card_add_service_cd),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(if (isCompactLayout) 22.dp else 24.dp)
                        )
                    }

                    // Refill icon button
                    androidx.compose.material3.IconButton(
                        onClick = onAddRefillClick
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalGasStation,
                            contentDescription = stringResource(R.string.car_card_add_refill_cd),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(if (isCompactLayout) 22.dp else 24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(if (isCompactLayout) 12.dp else 16.dp))

            // 2x2 Grid of metrics inside the same card
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(if (isCompactLayout) 8.dp else 12.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    MetricItem(
                        icon = Icons.Default.Speed,
                        label = stringResource(R.string.car_card_odometer),
                        value = stringResource(
                            R.string.car_card_km_format,
                            car.currentOdometer.formatNumber(0)
                        ),
                        isCompact = isCompactLayout
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    MetricItem(
                        icon = Icons.Default.EuroSymbol,
                        label = stringResource(R.string.car_card_total_cost),
                        value = stringResource(
                            R.string.car_card_currency_eur_format,
                            car.totalCost.formatNumber(2)
                        ),
                        isCompact = isCompactLayout
                    )
                }
            }

            Spacer(modifier = Modifier.height(if (isCompactLayout) 8.dp else 12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(if (isCompactLayout) 8.dp else 12.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    MetricItem(
                        icon = Icons.Default.Route,
                        label = stringResource(R.string.car_card_distance),
                        value = stringResource(
                            R.string.car_card_km_format,
                            car.totalDistance.formatNumber(0)
                        ),
                        isCompact = isCompactLayout
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    MetricItem(
                        icon = Icons.Default.LocalGasStation,
                        label = stringResource(R.string.car_card_avg_consumption),
                        value = if (car.averageConsumption > 0) {
                            stringResource(
                                R.string.car_card_consumption_format,
                                car.averageConsumption.formatNumber(1)
                            )
                        } else {
                            stringResource(R.string.not_available)
                        },
                        isCompact = isCompactLayout
                    )
                }
            }
        }
    }
}

@Composable
fun MetricItem(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    isCompact: Boolean = false
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(if (isCompact) 14.dp else 16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(if (isCompact) 6.dp else 8.dp))
        Column {
            Text(
                text = label,
                fontSize = if (isCompact) 11.sp else 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = value,
                fontSize = if (isCompact) 13.sp else 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Preview(name = "CarCard Light", showBackground = true)
@Composable
private fun PreviewCarCard() {
    CarTrackingAppTheme {
        CarCard(
            car = Car(
                id = 1L,
                name = "Toyota Corolla",
                licensePlate = "ABC-1234",
                currentOdometer = 45678.0,
                initialOdometer = 20000.0,
                totalCost = 5432.50,
                totalDistance = 25678.0,
                averageConsumption = 6.5
            ),
            onClick = {},
            onAddRefillClick = {},
            onAddServiceClick = {}
        )
    }
}

@Preview(name = "CarCard Dark", showBackground = true, uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewCarCardDark() {
    CarTrackingAppTheme {
        CarCard(
            car = Car(
                id = 1L,
                name = "Toyota Corolla",
                licensePlate = "ABC-1234",
                currentOdometer = 45678.0,
                initialOdometer = 20000.0,
                totalCost = 5432.50,
                totalDistance = 25678.0,
                averageConsumption = 6.5
            ),
            onClick = {},
            onAddRefillClick = {},
            onAddServiceClick = {}
        )
    }
}

@Preview(name = "CarCard Long Name", showBackground = true)
@Composable
private fun PreviewCarCardLongName() {
    CarTrackingAppTheme {
        CarCard(
            car = Car(
                id = 1L,
                name = "Mercedes-Benz S-Class AMG",
                licensePlate = "XYZ-9876",
                currentOdometer = 125000.0,
                initialOdometer = 100000.0,
                totalCost = 12345.99,
                totalDistance = 25000.0,
                averageConsumption = 8.2
            ),
            onClick = {},
            onAddRefillClick = {},
            onAddServiceClick = {}
        )
    }
}

@Preview(name = "CarCard No Consumption", showBackground = true)
@Composable
private fun PreviewCarCardNoConsumption() {
    CarTrackingAppTheme {
        CarCard(
            car = Car(
                id = 1L,
                name = "Tesla Model 3",
                licensePlate = "EV-2024",
                currentOdometer = 5000.0,
                initialOdometer = 0.0,
                totalCost = 250.00,
                totalDistance = 5000.0,
                averageConsumption = 0.0
            ),
            onClick = {},
            onAddRefillClick = {},
            onAddServiceClick = {}
        )
    }
}

