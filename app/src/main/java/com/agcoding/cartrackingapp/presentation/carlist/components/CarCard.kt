package com.agcoding.cartrackingapp.presentation.carlist.components

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.LocalGasStation
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.domain.model.Car
import com.agcoding.cartrackingapp.presentation.components.StyledCard
import java.util.Locale

@Composable
fun CarCard(
    car: Car,
    onClick: () -> Unit,
    onAddRefillClick: () -> Unit,
    onAddServiceClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    StyledCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Car name, license plate, and action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = car.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = car.licensePlate,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Action buttons row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
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
                            modifier = Modifier.size(24.dp)
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
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2x2 Grid of metrics inside the same card
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    MetricItem(
                        icon = Icons.Default.Speed,
                        label = stringResource(R.string.car_card_odometer),
                        value = stringResource(
                            R.string.car_card_km_format,
                            String.format(Locale.getDefault(), "%.0f", car.currentOdometer)
                        )
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    MetricItem(
                        icon = Icons.Default.AttachMoney,
                        label = stringResource(R.string.car_card_total_cost),
                        value = stringResource(
                            R.string.car_card_currency_eur_format,
                            String.format(Locale.getDefault(), "%.2f", car.totalCost)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    MetricItem(
                        icon = Icons.Default.Speed,
                        label = stringResource(R.string.car_card_distance),
                        value = stringResource(
                            R.string.car_card_km_format,
                            String.format(Locale.getDefault(), "%.0f", car.totalDistance)
                        )
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    MetricItem(
                        icon = Icons.Default.Speed,
                        label = stringResource(R.string.car_card_avg_consumption),
                        value = if (car.averageConsumption > 0) {
                            stringResource(
                                R.string.car_card_consumption_format,
                                String.format(Locale.getDefault(), "%.1f", car.averageConsumption)
                            )
                        } else {
                            stringResource(R.string.not_available)
                        }
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
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = label,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
