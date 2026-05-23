package com.agcoding.cartrackingapp.presentation.carcomparison

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.domain.model.CarComparisonData
import com.agcoding.cartrackingapp.domain.model.MultiCarComparisonResult

/**
 * All-cars comparison content with tablet landscape support
 */
@Composable
fun AllCarsComparisonContent(result: MultiCarComparisonResult) {
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp
    val isLandscape = configuration.screenWidthDp > configuration.screenHeightDp
    val useSplitView = screenWidthDp >= 600 || isLandscape

    if (useSplitView) {
        // Tablet/Landscape: Two columns side by side
        Row(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Left column: Summary + Rankings
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OverallSummaryCard(result = result)

                MetricRankingCard(
                    title = stringResource(R.string.car_comparison_cost_per_km),
                    cars = result.cars,
                    getValue = { it.costPerKm },
                    suffix = "€/km",
                    bestCarId = result.bestCostPerKm,
                    worstCarId = result.worstCostPerKm,
                    lowerIsBetter = true
                )
            }

            // Right column: More rankings
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                MetricRankingCard(
                    title = stringResource(R.string.car_comparison_avg_consumption),
                    cars = result.cars,
                    getValue = { it.avgConsumption },
                    suffix = "L/100km",
                    bestCarId = result.bestConsumption,
                    worstCarId = result.worstConsumption,
                    lowerIsBetter = true
                )

                MetricRankingCard(
                    title = stringResource(R.string.car_comparison_maintenance_per_year),
                    cars = result.cars,
                    getValue = { it.maintenancePerYear },
                    suffix = "€/year",
                    bestCarId = result.bestMaintenance,
                    worstCarId = result.worstMaintenance,
                    lowerIsBetter = true
                )
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
            // Overall winner/loser card
            OverallSummaryCard(result = result)

            // Cost per km ranking
            MetricRankingCard(
                title = stringResource(R.string.car_comparison_cost_per_km),
                cars = result.cars,
                getValue = { it.costPerKm },
                suffix = "€/km",
                bestCarId = result.bestCostPerKm,
                worstCarId = result.worstCostPerKm,
                lowerIsBetter = true
            )

            // Consumption ranking
            MetricRankingCard(
                title = stringResource(R.string.car_comparison_avg_consumption),
                cars = result.cars,
                getValue = { it.avgConsumption },
                suffix = "L/100km",
                bestCarId = result.bestConsumption,
                worstCarId = result.worstConsumption,
                lowerIsBetter = true
            )

            // Maintenance ranking
            MetricRankingCard(
                title = stringResource(R.string.car_comparison_maintenance_per_year),
                cars = result.cars,
                getValue = { it.maintenancePerYear },
                suffix = "€/year",
                bestCarId = result.bestMaintenance,
                worstCarId = result.worstMaintenance,
                lowerIsBetter = true
            )
        }
    }
}

@Composable
private fun OverallSummaryCard(result: MultiCarComparisonResult) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = stringResource(R.string.car_comparison_overall_ranking),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            // Best car overall
            result.overallBest?.let { bestCarId ->
                val bestCar = result.cars.find { it.carId == bestCarId }
                bestCar?.let { car ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrendingDown,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = stringResource(R.string.car_comparison_most_economical, car.carName),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Text(
                        text = buildString {
                            append(stringResource(R.string.car_comparison_best_performer))
                            car.costPerKm?.let { cost ->
                                append(" ")
                                append(stringResource(
                                    R.string.car_comparison_cost_detail,
                                    cost // Pass Double directly, not String
                                ))
                            }
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // Worst car overall
            result.overallWorst?.let { worstCarId ->
                val worstCar = result.cars.find { it.carId == worstCarId }
                worstCar?.let { car ->
                    HorizontalDivider()

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = stringResource(R.string.car_comparison_least_economical, car.carName),
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    Text(
                        text = buildString {
                            append(stringResource(R.string.car_comparison_needs_attention))
                            car.costPerKm?.let { cost ->
                                append(" ")
                                append(stringResource(
                                    R.string.car_comparison_cost_detail,
                                    cost // Pass Double directly, not String
                                ))
                            }
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}

@Composable
private fun MetricRankingCard(
    title: String,
    cars: List<CarComparisonData>,
    getValue: (CarComparisonData) -> Double?,
    suffix: String,
    bestCarId: Long?,
    worstCarId: Long?,
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

            // Sort cars by the metric value
            val sortedCars = cars.filter { getValue(it) != null }
                .sortedBy { getValue(it) }

            if (sortedCars.isEmpty()) {
                Text(
                    text = stringResource(R.string.car_comparison_no_data_available),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                sortedCars.forEachIndexed { index, car ->
                    val value = getValue(car)
                    val isBest = car.carId == bestCarId
                    val isWorst = car.carId == worstCarId
                    val rank = index + 1

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            // Rank badge
                            Surface(
                                shape = MaterialTheme.shapes.small,
                                color = when {
                                    isBest -> MaterialTheme.colorScheme.primaryContainer
                                    isWorst -> MaterialTheme.colorScheme.errorContainer
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Text(
                                        text = rank.toString(),
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = when {
                                            isBest -> MaterialTheme.colorScheme.onPrimaryContainer
                                            isWorst -> MaterialTheme.colorScheme.onErrorContainer
                                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                                        }
                                    )
                                }
                            }

                            // Car name with badge
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = car.carName,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = if (isBest || isWorst) FontWeight.Bold else FontWeight.Medium
                                )

                                when {
                                    isBest -> Icon(
                                        imageVector = Icons.Default.TrendingDown,
                                        contentDescription = "Best",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    isWorst -> Icon(
                                        imageVector = Icons.Default.TrendingUp,
                                        contentDescription = "Worst",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        // Value
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

                    if (index < sortedCars.size - 1) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                    }
                }
            }
        }
    }
}

