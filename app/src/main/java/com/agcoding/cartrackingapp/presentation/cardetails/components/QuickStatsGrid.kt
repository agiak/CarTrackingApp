package com.agcoding.cartrackingapp.presentation.cardetails.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.domain.model.Car
import com.agcoding.cartrackingapp.domain.model.CarStatistics
import com.agcoding.cartrackingapp.presentation.components.Kpi
import com.agcoding.cartrackingapp.presentation.components.KpiGrid
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme
import com.agcoding.cartrackingapp.util.formatNumber

/**
 * The three figures that describe how this car behaves: how much it drinks,
 * how far it has gone, what each kilometre costs.
 *
 * They were three 160dp-tall cards side by side, each with an icon above a
 * number — nearly a third of a phone screen for three short figures, and the
 * icons were decoration, since the label underneath already said what each
 * one was. As a KPI grid they take a quarter of the room and line up with the
 * same grid used on Statistics.
 */
@Composable
fun QuickStatsGrid(statistics: CarStatistics) {
    val unavailable = stringResource(R.string.not_available)

    KpiGrid(
        columns = 3,
        items = listOf(
            Kpi(
                // Not avg_consumption_short: that string carries a hard \n for
                // the old two-line card, and a newline inside a single-line
                // cell renders as a stray space.
                label = stringResource(R.string.car_card_avg_consumption),
                value = if (statistics.averageConsumption > 0) {
                    statistics.averageConsumption.formatNumber(1)
                } else {
                    unavailable
                },
                unit = if (statistics.averageConsumption > 0) "L/100" else null,
            ),
            Kpi(
                label = stringResource(R.string.car_card_distance),
                value = statistics.totalDistance.formatNumber(0),
                unit = "km",
            ),
            Kpi(
                label = stringResource(R.string.car_card_cost_per_km),
                value = if (statistics.costPerKilometer > 0) {
                    statistics.costPerKilometer.formatNumber(2)
                } else {
                    unavailable
                },
                unit = if (statistics.costPerKilometer > 0) "€" else null,
            ),
        ),
    )
}

@Preview(showBackground = true, widthDp = 380)
@Composable
private fun QuickStatsGridPreview() {
    CarTrackingAppTheme(darkTheme = true) {
        Column(Modifier.padding(18.dp)) {
            QuickStatsGrid(
                CarStatistics(
                    car = Car(
                        id = 1, name = "Toyota Corolla", licensePlate = "ΙΖΧ-4821",
                        currentOdometer = 145678.0, initialOdometer = 20000.0,
                    ),
                    totalRefills = 501,
                    averageConsumption = 6.5,
                    totalCost = 15432.50,
                    totalDistance = 125678.0,
                    costPerKilometer = 0.19,
                    serviceExpensesCost = 1200.0,
                    serviceExpenseCount = 8,
                    otherExpensesCost = 300.0,
                    otherExpenseCount = 5,
                    averagePricePerLiter = 1.65,
                    totalLiters = 5200.0,
                    recentRefills = emptyList(),
                )
            )
        }
    }
}
