package com.agcoding.cartrackingapp.presentation.statistics.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.domain.model.Car
import com.agcoding.cartrackingapp.domain.model.CarStatistics
import com.agcoding.cartrackingapp.presentation.components.StyledCard
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppColorScheme
import com.agcoding.cartrackingapp.shared.ui.tokens.AppNumbers
import com.agcoding.cartrackingapp.util.formatNumber

/**
 * Every car, ranked by what a kilometre costs in it.
 *
 * This replaces a stack of per-car cards, one each, repeating the same six
 * figures. Six figures about one car are a summary; the same six about four
 * cars are a spreadsheet, and nobody reads a spreadsheet by scrolling. The one
 * question a multi-car owner actually has is "which of these is the expensive
 * one", and that is a ranking — so it is drawn as one.
 *
 * Cheapest first, because the list is read top-down and the useful end is the
 * one you are not currently worried about: the car at the bottom is the
 * finding.
 *
 * The bar is relative to the most expensive car rather than to an absolute
 * scale. There is no meaningful maximum for cost per kilometre, so the only
 * honest comparison is with the others in the list.
 */
@Composable
fun CarsByCostCard(
    cars: List<CarStatistics>,
    onCarClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scheme = LocalAppColorScheme.current
    val ranked = cars.filter { it.costPerKilometer > 0 }.sortedBy { it.costPerKilometer }
    if (ranked.isEmpty()) return

    val worst = ranked.last().costPerKilometer

    StyledCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(vertical = 6.dp)) {
            ranked.forEachIndexed { index, stats ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onCarClick(stats.car.id) }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                ) {
                    Text(
                        text = "%02d".format(index + 1),
                        style = AppNumbers.compact,
                        color = scheme.contentDisabled,
                    )
                    Spacer(Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stats.car.name,
                            style = MaterialTheme.typography.titleSmall,
                            color = scheme.contentPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Spacer(Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(scheme.backgroundSecondary),
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(
                                        (stats.costPerKilometer / worst)
                                            .toFloat()
                                            .coerceIn(0.06f, 1f)
                                    )
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(scheme.actionPrimary),
                            )
                        }
                    }
                    Spacer(Modifier.width(14.dp))
                    Text(
                        text = stringResource(
                            R.string.cost_per_km_format,
                            stats.costPerKilometer.formatNumber(2),
                        ),
                        style = AppNumbers.compact,
                        color = scheme.contentPrimary,
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 380)
@Composable
private fun CarsByCostCardPreview() {
    CarTrackingAppTheme(darkTheme = true) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(18.dp),
        ) {
            CarsByCostCard(
                cars = listOf(
                    previewStats("VW Golf", 0.31),
                    previewStats("Toyota Corolla", 0.19),
                    previewStats("Tesla Model 3", 0.08),
                ),
                onCarClick = {},
            )
        }
    }
}

private fun previewStats(name: String, cpk: Double) = CarStatistics(
    car = Car(id = name.hashCode().toLong(), name = name, licensePlate = "",
        currentOdometer = 1.0, initialOdometer = 0.0),
    totalRefills = 100,
    averageConsumption = 7.0,
    totalCost = 1000.0,
    totalDistance = 10000.0,
    costPerKilometer = cpk,
    serviceExpensesCost = 100.0,
    serviceExpenseCount = 2,
    otherExpensesCost = 50.0,
    otherExpenseCount = 1,
    averagePricePerLiter = 1.7,
    totalLiters = 700.0,
    recentRefills = emptyList(),
)
