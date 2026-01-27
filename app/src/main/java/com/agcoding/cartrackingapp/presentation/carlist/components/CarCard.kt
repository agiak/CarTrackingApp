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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
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
    val configuration = LocalConfiguration.current
    val isCompactLayout = configuration.screenWidthDp >= 600 ||
                          configuration.screenWidthDp > configuration.screenHeightDp

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
                    Text(
                        text = car.name,
                        fontSize = if (isCompactLayout) 16.sp else 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
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
                            String.format(Locale.getDefault(), "%.0f", car.currentOdometer)
                        ),
                        isCompact = isCompactLayout
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    MetricItem(
                        icon = Icons.Default.AttachMoney,
                        label = stringResource(R.string.car_card_total_cost),
                        value = stringResource(
                            R.string.car_card_currency_eur_format,
                            String.format(Locale.getDefault(), "%.2f", car.totalCost)
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
                        icon = Icons.Default.Speed,
                        label = stringResource(R.string.car_card_distance),
                        value = stringResource(
                            R.string.car_card_km_format,
                            String.format(Locale.getDefault(), "%.0f", car.totalDistance)
                        ),
                        isCompact = isCompactLayout
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
