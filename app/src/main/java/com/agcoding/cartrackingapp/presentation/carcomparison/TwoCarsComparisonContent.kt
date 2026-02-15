package com.agcoding.cartrackingapp.presentation.carcomparison

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.domain.model.CarComparisonData
import com.agcoding.cartrackingapp.domain.model.CarComparisonResult
import com.agcoding.cartrackingapp.domain.model.ComparisonDifference
import com.agcoding.cartrackingapp.presentation.components.StyledCard

/**
 * Two-car comparison content with tablet landscape support and car selector
 */
@Composable
fun TwoCarsComparisonContent(
    result: CarComparisonResult,
    viewModel: CarComparisonViewModel
) {
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp
    val isLandscape = configuration.screenWidthDp > configuration.screenHeightDp
    val useSplitView = screenWidthDp >= 600 || isLandscape

    val availableCars by viewModel.availableCars.collectAsState()
    val selectedCar1 by viewModel.selectedCar1.collectAsState()
    val selectedCar2 by viewModel.selectedCar2.collectAsState()

    if (useSplitView) {
        // Tablet/Landscape: Split view with metrics on left and chart on right
        Row(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Left column: Car selector + Comparison cards
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Car Selector Row
                CarSelectorRow(
                    availableCars = availableCars,
                    selectedCar1 = selectedCar1,
                    selectedCar2 = selectedCar2,
                    onCar1Selected = viewModel::selectCar1,
                    onCar2Selected = viewModel::selectCar2
                )

                SummaryCard(result = result)

                ComparisonMetricCard(
                    title = stringResource(R.string.car_comparison_cost_per_km),
                    car1Name = result.car1.carName,
                    car1Value = result.car1.costPerKm,
                    car2Name = result.car2.carName,
                    car2Value = result.car2.costPerKm,
                    difference = result.costPerKmDifference,
                    suffix = "€/km",
                    lowerIsBetter = true
                )

                ComparisonMetricCard(
                    title = stringResource(R.string.car_comparison_avg_consumption),
                    car1Name = result.car1.carName,
                    car1Value = result.car1.avgConsumption,
                    car2Name = result.car2.carName,
                    car2Value = result.car2.avgConsumption,
                    difference = result.consumptionDifference,
                    suffix = "L/100km",
                    lowerIsBetter = true
                )

                ComparisonMetricCard(
                    title = stringResource(R.string.car_comparison_maintenance_per_year),
                    car1Name = result.car1.carName,
                    car1Value = result.car1.maintenancePerYear,
                    car2Name = result.car2.carName,
                    car2Value = result.car2.maintenancePerYear,
                    difference = result.maintenanceDifference,
                    suffix = "€/year",
                    lowerIsBetter = true
                )
            }

            // Right column: Detailed stats for both cars
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DetailedStatsCard(car = result.car1)
                DetailedStatsCard(car = result.car2)
            }
        }
    } else {
        // Phone Portrait: Single column
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Car Selector Row
            CarSelectorRow(
                availableCars = availableCars,
                selectedCar1 = selectedCar1,
                selectedCar2 = selectedCar2,
                onCar1Selected = viewModel::selectCar1,
                onCar2Selected = viewModel::selectCar2
            )

            // Summary card
            SummaryCard(result = result)

            // Cost per km comparison
            ComparisonMetricCard(
                title = stringResource(R.string.car_comparison_cost_per_km),
                car1Name = result.car1.carName,
                car1Value = result.car1.costPerKm,
                car2Name = result.car2.carName,
                car2Value = result.car2.costPerKm,
                difference = result.costPerKmDifference,
                suffix = "€/km",
                lowerIsBetter = true
            )

            // Consumption comparison
            ComparisonMetricCard(
                title = stringResource(R.string.car_comparison_avg_consumption),
                car1Name = result.car1.carName,
                car1Value = result.car1.avgConsumption,
                car2Name = result.car2.carName,
                car2Value = result.car2.avgConsumption,
                difference = result.consumptionDifference,
                suffix = "L/100km",
                lowerIsBetter = true
            )

            // Maintenance comparison
            ComparisonMetricCard(
                title = stringResource(R.string.car_comparison_maintenance_per_year),
                car1Name = result.car1.carName,
                car1Value = result.car1.maintenancePerYear,
                car2Name = result.car2.carName,
                car2Value = result.car2.maintenancePerYear,
                difference = result.maintenanceDifference,
                suffix = "€/year",
                lowerIsBetter = true
            )

            // Detailed stats
            DetailedStatsCard(car = result.car1)
            DetailedStatsCard(car = result.car2)
        }
    }
}

@Composable
private fun SummaryCard(result: CarComparisonResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.car_comparison_summary),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            val winnerCar = when (result.overallWinner) {
                result.car1.carId -> result.car1
                result.car2.carId -> result.car2
                else -> null
            }

            if (winnerCar != null) {
                Text(
                    text = stringResource(
                        R.string.car_comparison_overall_winner,
                        winnerCar.carName
                    ),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                // Dynamic insight text
                result.costPerKmDifference?.let { diff ->
                    val winnerName = if (diff.lowerCarId == winnerCar.carId) winnerCar.carName
                    else if (diff.lowerCarId == result.car1.carId) result.car1.carName
                    else result.car2.carName

                    val loserName = if (diff.higherCarId == result.car1.carId) result.car1.carName
                    else result.car2.carName

                    Text(
                        text = stringResource(
                            R.string.car_comparison_cost_per_km_insight,
                            loserName,
                            formatPercentage(diff.percentageDifference),
                            winnerName
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                result.consumptionDifference?.let { diff ->
                    val betterCar = if (diff.lowerCarId == result.car1.carId) result.car1.carName
                    else result.car2.carName

                    val worseCar = if (diff.higherCarId == result.car1.carId) result.car1.carName
                    else result.car2.carName

                    Text(
                        text = stringResource(
                            R.string.car_comparison_consumption_insight,
                            betterCar,
                            formatPercentage(diff.percentageDifference),
                            worseCar
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                result.maintenanceDifference?.let { diff ->
                    val cheaperCar = if (diff.lowerCarId == result.car1.carId) result.car1.carName
                    else result.car2.carName

                    val expensiveCar = if (diff.higherCarId == result.car1.carId) result.car1.carName
                    else result.car2.carName

                    Text(
                        text = stringResource(
                            R.string.car_comparison_maintenance_insight,
                            expensiveCar,
                            String.format("€%.0f", diff.absoluteDifference),
                            cheaperCar
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            } else {
                Text(
                    text = stringResource(R.string.car_comparison_equal_performance),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
private fun ComparisonMetricCard(
    title: String,
    car1Name: String,
    car1Value: Double?,
    car2Name: String,
    car2Value: Double?,
    difference: ComparisonDifference?,
    suffix: String,
    lowerIsBetter: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            // Car 1
            MetricRow(
                carName = car1Name,
                value = car1Value,
                suffix = suffix,
                isBest = difference != null && car1Value != null && car2Value != null &&
                        (if (lowerIsBetter) car1Value < car2Value else car1Value > car2Value),
                isWorst = difference != null && car1Value != null && car2Value != null &&
                        (if (lowerIsBetter) car1Value > car2Value else car1Value < car2Value)
            )

            // Car 2
            MetricRow(
                carName = car2Name,
                value = car2Value,
                suffix = suffix,
                isBest = difference != null && car1Value != null && car2Value != null &&
                        (if (lowerIsBetter) car2Value < car1Value else car2Value > car1Value),
                isWorst = difference != null && car1Value != null && car2Value != null &&
                        (if (lowerIsBetter) car2Value > car1Value else car2Value < car1Value)
            )

            // Difference
            if (difference != null) {
                Divider()
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.car_comparison_difference),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatPercentage(difference.percentageDifference),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricRow(
    carName: String,
    value: Double?,
    suffix: String,
    isBest: Boolean,
    isWorst: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = carName,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            if (isBest) {
                Icon(
                    imageVector = Icons.Default.TrendingDown,
                    contentDescription = "Best",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            } else if (isWorst) {
                Icon(
                    imageVector = Icons.Default.TrendingUp,
                    contentDescription = "Worst",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Text(
            text = formatMetricValue(value, suffix),
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            color = when {
                isBest -> MaterialTheme.colorScheme.primary
                isWorst -> MaterialTheme.colorScheme.error
                else -> MaterialTheme.colorScheme.onSurface
            }
        )
    }
}

@Composable
private fun DetailedStatsCard(car: CarComparisonData) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = car.carName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            DetailRow(
                label = stringResource(R.string.car_comparison_total_expenses),
                value = String.format("€%.2f", car.totalExpenses)
            )
            DetailRow(
                label = stringResource(R.string.car_comparison_total_kilometers),
                value = String.format("%.0f km", car.totalKilometers)
            )
            DetailRow(
                label = stringResource(R.string.car_comparison_total_liters),
                value = String.format("%.2f L", car.totalLiters)
            )
            DetailRow(
                label = stringResource(R.string.car_comparison_years_active),
                value = String.format("%.1f years", car.yearsActive)
            )
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CarSelectorRow(
    availableCars: List<com.agcoding.cartrackingapp.domain.model.Car>,
    selectedCar1: com.agcoding.cartrackingapp.domain.model.Car?,
    selectedCar2: com.agcoding.cartrackingapp.domain.model.Car?,
    onCar1Selected: (com.agcoding.cartrackingapp.domain.model.Car) -> Unit,
    onCar2Selected: (com.agcoding.cartrackingapp.domain.model.Car) -> Unit
) {
    var showCar1Selector by remember { mutableStateOf(false) }
    var showCar2Selector by remember { mutableStateOf(false) }

    StyledCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Labels Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.car_comparison_car_1),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(140.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.width(32.dp)) // Space for icon

                Text(
                    text = stringResource(R.string.car_comparison_car_2),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(140.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Selectors and Icon Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Car 1 Selector
                Box {
                    CarSelectorBox(
                        carName = selectedCar1?.name ?: "Select Car",
                        onClick = { showCar1Selector = true },
                        isPrimary = true
                    )

                    DropdownMenu(
                        expanded = showCar1Selector,
                        onDismissRequest = { showCar1Selector = false }
                    ) {
                        availableCars.filter { it.id != selectedCar2?.id }.forEach { car ->
                            DropdownMenuItem(
                                text = { Text(car.name) },
                                onClick = {
                                    onCar1Selected(car)
                                    showCar1Selector = false
                                }
                            )
                        }
                    }
                }

                // VS Icon - now aligned with boxes
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.CompareArrows,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(32.dp)
                )

                // Car 2 Selector
                Box {
                    CarSelectorBox(
                        carName = selectedCar2?.name ?: "Select Car",
                        onClick = { showCar2Selector = true },
                        isPrimary = false
                    )

                    DropdownMenu(
                        expanded = showCar2Selector,
                        onDismissRequest = { showCar2Selector = false }
                    ) {
                        availableCars.filter { it.id != selectedCar1?.id }.forEach { car ->
                            DropdownMenuItem(
                                text = { Text(car.name) },
                                onClick = {
                                    onCar2Selected(car)
                                    showCar2Selector = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CarSelectorBox(
    carName: String,
    onClick: () -> Unit,
    isPrimary: Boolean
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier
            .width(140.dp)
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isPrimary) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.secondaryContainer
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 8.dp)
    ) {
        Text(
            text = carName,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = if (isPrimary) MaterialTheme.colorScheme.onPrimaryContainer
            else MaterialTheme.colorScheme.onSecondaryContainer,
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Icon(
            imageVector = Icons.Default.ArrowDropDown,
            contentDescription = null,
            tint = if (isPrimary) MaterialTheme.colorScheme.onPrimaryContainer
            else MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.size(20.dp)
        )
    }
}

