package com.agcoding.cartrackingapp.presentation.refilldetails.components

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocationOn
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.domain.model.Car
import com.agcoding.cartrackingapp.domain.model.FuelRefill
import com.agcoding.cartrackingapp.domain.model.Location
import com.agcoding.cartrackingapp.domain.usecase.refill.RefillDetails
import com.agcoding.cartrackingapp.presentation.components.StyledCard
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RefillDetailsContent(
    details: RefillDetails,
    addressString: String?,
    context: Context,
    onLocationClick: (Double, Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val refill = details.refill
    val car = details.car

    val isTablet = com.agcoding.cartrackingapp.util.DeviceUtils.isTablet()
    val isLandscape = com.agcoding.cartrackingapp.util.DeviceUtils.isLandscape()
    val useSplitView = isTablet || isLandscape

    if (useSplitView) {
        // Split view for tablets and landscape
        Row(
            modifier = modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Left side: Header, total amount, metrics (45%)
            Column(
                modifier = Modifier
                    .weight(0.45f)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Section
                RefillHeader(
                    carName = car?.name,
                    licensePlate = car?.licensePlate
                )

                // Total Amount Card
                TotalAmountCard(
                    amountPaid = refill.amountPaid,
                    timestamp = refill.timestamp
                )

                // Metrics Grid - Row 1
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        icon = Icons.Default.LocalGasStation,
                        label = stringResource(R.string.metric_fuel_volume),
                        value = "%.1f L".format(refill.litersAdded),
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        icon = Icons.Default.AttachMoney,
                        label = stringResource(R.string.metric_price_per_liter),
                        value = "€%.2f".format(refill.pricePerLiter),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Metrics Grid - Row 2
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    MetricCard(
                        icon = Icons.Default.Route,
                        label = stringResource(R.string.metric_trip_distance),
                        value = "%.0f km".format(refill.tripDistance),
                        modifier = Modifier.weight(1f)
                    )
                    MetricCard(
                        icon = Icons.AutoMirrored.Filled.TrendingUp,
                        label = stringResource(R.string.metric_consumption),
                        value = "%.1f L/100km".format(refill.fuelConsumption),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Right side: Additional info, notes, cost analysis (55%)
            Column(
                modifier = Modifier
                    .weight(0.55f)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Additional Information Section
                StyledCard {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.additional_information),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        InfoItem(
                            icon = Icons.Default.CalendarToday,
                            label = stringResource(R.string.date_time),
                            value = formatDateTime(refill.timestamp),
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        refill.location?.let { location ->
                            InfoItem(
                                icon = Icons.Default.LocationOn,
                                label = stringResource(R.string.location_label),
                                value = addressString ?: stringResource(
                                    R.string.location_lat_lng_format,
                                    location.latitude,
                                    location.longitude
                                ),
                                modifier = Modifier.clickable {
                                    onLocationClick(location.latitude, location.longitude)
                                }
                            )
                        }
                    }
                }

                // Notes if available
                if (!refill.notes.isNullOrBlank()) {
                    StyledCard {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.notes),
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            Text(
                                text = refill.notes,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // Cost Analysis Section
                StyledCard {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.cost_analysis),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        // Calculate metrics
                        val costPerKm = if (refill.tripDistance > 0) {
                            refill.amountPaid / refill.tripDistance
                        } else 0.0

                        val fuelEfficiency = if (refill.litersAdded > 0) {
                            refill.tripDistance / refill.litersAdded
                        } else 0.0

                        // Cost per kilometer
                        AnalysisRow(
                            label = stringResource(R.string.cost_per_kilometer_label),
                            value = "€%.3f/km".format(costPerKm)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Fuel efficiency
                        AnalysisRow(
                            label = stringResource(R.string.fuel_efficiency_label),
                            value = "%.1f km/L".format(fuelEfficiency)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Total liters
                        AnalysisRow(
                            label = stringResource(R.string.total_liters_label),
                            value = "%.2f L".format(refill.litersAdded)
                        )
                    }
                }
            }
        }
    } else {
        // Original single column layout for portrait phones
        Column(
            modifier = modifier
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            // Header Section
            RefillHeader(
                carName = car?.name,
                licensePlate = car?.licensePlate,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Total Amount Card
            TotalAmountCard(
                amountPaid = refill.amountPaid,
                timestamp = refill.timestamp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            // Metrics Grid - Row 1
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    icon = Icons.Default.LocalGasStation,
                    label = stringResource(R.string.metric_fuel_volume),
                    value = "%.1f L".format(refill.litersAdded),
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    icon = Icons.Default.AttachMoney,
                    label = stringResource(R.string.metric_price_per_liter),
                    value = "€%.2f".format(refill.pricePerLiter),
                    modifier = Modifier.weight(1f)
                )
            }

            // Metrics Grid - Row 2
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    icon = Icons.Default.Route,
                    label = stringResource(R.string.metric_trip_distance),
                    value = "%.0f km".format(refill.tripDistance),
                    modifier = Modifier.weight(1f)
                )
                MetricCard(
                    icon = Icons.AutoMirrored.Filled.TrendingUp,
                    label = stringResource(R.string.metric_consumption),
                    value = "%.1f L/100km".format(refill.fuelConsumption),
                    modifier = Modifier.weight(1f)
                )
            }

            // Additional Information Section
            StyledCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.additional_information),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    InfoItem(
                        icon = Icons.Default.CalendarToday,
                        label = stringResource(R.string.date_time),
                        value = formatDateTime(refill.timestamp),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    refill.location?.let { location ->
                        InfoItem(
                            icon = Icons.Default.LocationOn,
                            label = stringResource(R.string.location_label),
                            value = addressString ?: stringResource(
                                R.string.location_lat_lng_format,
                                location.latitude,
                                location.longitude
                            ),
                            modifier = Modifier.clickable {
                                onLocationClick(location.latitude, location.longitude)
                            }
                        )
                    }
                }
            }

            // Notes if available
            if (!refill.notes.isNullOrBlank()) {
                Text(
                    text = stringResource(R.string.notes),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = refill.notes,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            // Cost Analysis Section
            StyledCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.cost_analysis),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // Calculate metrics
                    val costPerKm = if (refill.tripDistance > 0) {
                        refill.amountPaid / refill.tripDistance
                    } else 0.0

                    val fuelEfficiency = if (refill.litersAdded > 0) {
                        refill.tripDistance / refill.litersAdded
                    } else 0.0

                    // Cost per kilometer
                    AnalysisRow(
                        label = stringResource(R.string.cost_per_kilometer_label),
                        value = "€%.3f/km".format(costPerKm)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Fuel efficiency
                    AnalysisRow(
                        label = stringResource(R.string.fuel_efficiency_label),
                        value = "%.1f km/L".format(fuelEfficiency)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Total liters
                    AnalysisRow(
                        label = stringResource(R.string.total_liters_label),
                        value = "%.2f L".format(refill.litersAdded)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun InfoItem(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column {
            Text(
                text = label,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
private fun AnalysisRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Medium
        )
    }
}

private fun formatDateTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM d, yyyy, h:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

// ============================================
// Preview Composables
// ============================================

@Preview(name = "Refill Details - Phone Portrait", showBackground = true, widthDp = 380, heightDp = 800)
@Composable
private fun PreviewRefillDetailsContentPhone() {
    CarTrackingAppTheme(darkTheme = false) {
        RefillDetailsContent(
            details = RefillDetails(
                refill = FuelRefill(
                    id = 1,
                    carId = 1,
                    timestamp = System.currentTimeMillis(),
                    amountPaid = 65.50,
                    litersAdded = 42.5,
                    pricePerLiter = 1.54,
                    tripDistance = 580.0,
                    odometerReading = 12580.0,
                    fuelConsumption = 7.33,
                    location = Location(
                        latitude = 37.9838,
                        longitude = 23.7275
                    ),
                    notes = "Regular refill at local gas station"
                ),
                car = Car(
                    id = 1,
                    name = "Toyota Corolla",
                    licensePlate = "ABC-1234",
                    initialOdometer = 0.0,
                    currentOdometer = 12580.0
                )
            ),
            addressString = "Athens, Greece",
            context = androidx.compose.ui.platform.LocalContext.current,
            onLocationClick = { _, _ -> }
        )
    }
}

@Preview(name = "Refill Details - Tablet Split View", showBackground = true, widthDp = 900, heightDp = 600)
@Composable
private fun PreviewRefillDetailsContentTablet() {
    CarTrackingAppTheme(darkTheme = false) {
        RefillDetailsContent(
            details = RefillDetails(
                refill = FuelRefill(
                    id = 2,
                    carId = 1,
                    timestamp = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L,
                    amountPaid = 82.30,
                    litersAdded = 50.0,
                    pricePerLiter = 1.65,
                    tripDistance = 650.0,
                    odometerReading = 13230.0,
                    fuelConsumption = 7.69,
                    location = Location(
                        latitude = 40.6401,
                        longitude = 22.9444
                    ),
                    notes = "Highway refill during road trip"
                ),
                car = Car(
                    id = 1,
                    name = "Honda Civic",
                    licensePlate = "XYZ-5678",
                    initialOdometer = 0.0,
                    currentOdometer = 13230.0
                )
            ),
            addressString = "Thessaloniki, Greece",
            context = androidx.compose.ui.platform.LocalContext.current,
            onLocationClick = { _, _ -> }
        )
    }
}

@Preview(name = "Refill Details - No Location/Notes", showBackground = true, widthDp = 380, heightDp = 800)
@Composable
private fun PreviewRefillDetailsContentNoExtras() {
    CarTrackingAppTheme(darkTheme = false) {
        RefillDetailsContent(
            details = RefillDetails(
                refill = FuelRefill(
                    id = 3,
                    carId = 1,
                    timestamp = System.currentTimeMillis(),
                    amountPaid = 55.00,
                    litersAdded = 35.0,
                    pricePerLiter = 1.57,
                    tripDistance = 480.0,
                    odometerReading = 11800.0,
                    fuelConsumption = 7.29,
                    location = null,
                    notes = null
                ),
                car = Car(
                    id = 1,
                    name = "BMW 320i",
                    licensePlate = "DEF-9876",
                    initialOdometer = 0.0,
                    currentOdometer = 11800.0
                )
            ),
            addressString = null,
            context = androidx.compose.ui.platform.LocalContext.current,
            onLocationClick = { _, _ -> }
        )
    }
}

@Preview(name = "Refill Details - Dark Mode", showBackground = true, widthDp = 380, heightDp = 800)
@Composable
private fun PreviewRefillDetailsContentDark() {
    CarTrackingAppTheme(darkTheme = true) {
        RefillDetailsContent(
            details = RefillDetails(
                refill = FuelRefill(
                    id = 4,
                    carId = 1,
                    timestamp = System.currentTimeMillis() - 2 * 24 * 60 * 60 * 1000L,
                    amountPaid = 72.80,
                    litersAdded = 45.5,
                    pricePerLiter = 1.60,
                    tripDistance = 590.0,
                    odometerReading = 14390.0,
                    fuelConsumption = 7.71,
                    location = Location(
                        latitude = 38.2466,
                        longitude = 21.7346
                    ),
                    notes = "Evening refill on way home"
                ),
                car = Car(
                    id = 1,
                    name = "Volkswagen Golf",
                    licensePlate = "GHI-4321",
                    initialOdometer = 0.0,
                    currentOdometer = 14390.0
                )
            ),
            addressString = "Patras, Greece",
            context = androidx.compose.ui.platform.LocalContext.current,
            onLocationClick = { _, _ -> }
        )
    }
}

@Preview(name = "Refill Details - Landscape", showBackground = true, widthDp = 800, heightDp = 400)
@Composable
private fun PreviewRefillDetailsContentLandscape() {
    CarTrackingAppTheme(darkTheme = false) {
        RefillDetailsContent(
            details = RefillDetails(
                refill = FuelRefill(
                    id = 5,
                    carId = 1,
                    timestamp = System.currentTimeMillis(),
                    amountPaid = 68.25,
                    litersAdded = 40.0,
                    pricePerLiter = 1.71,
                    tripDistance = 520.0,
                    odometerReading = 15020.0,
                    fuelConsumption = 7.69,
                    location = null,
                    notes = "Quick refill during lunch break"
                ),
                car = Car(
                    id = 1,
                    name = "Mazda 3",
                    licensePlate = "JKL-1111",
                    initialOdometer = 0.0,
                    currentOdometer = 15020.0
                )
            ),
            addressString = null,
            context = androidx.compose.ui.platform.LocalContext.current,
            onLocationClick = { _, _ -> }
        )
    }
}

