package com.agcoding.cartrackingapp.presentation.cardetails.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.domain.model.DateFilter
import com.agcoding.cartrackingapp.domain.model.PeriodStatistics
import com.agcoding.cartrackingapp.presentation.components.DateFilterButton
import com.agcoding.cartrackingapp.presentation.components.Kpi
import com.agcoding.cartrackingapp.presentation.components.KpiGrid
import com.agcoding.cartrackingapp.presentation.components.StyledCard
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppColorScheme
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppDimens
import com.agcoding.cartrackingapp.shared.ui.tokens.AppNumbers
import com.agcoding.cartrackingapp.util.formatMoney
import com.agcoding.cartrackingapp.util.formatNumber

/**
 * What this car cost over a period the user chooses — all time, a year, a
 * single month.
 *
 * The date control in the header is the shared chip, so it now reads as the
 * active period rather than as a button labelled "filter", and carries an x
 * when something is selected. That matters most here, where a surprisingly
 * small total is far more likely to be a forgotten filter than a cheap month.
 */
@Composable
fun PeriodStatisticsCard(
    statistics: PeriodStatistics,
    onDateFilterClick: () -> Unit,
    modifier: Modifier = Modifier,
    onClearDateFilter: (() -> Unit)? = null,
) {
    val scheme = LocalAppColorScheme.current
    val dimens = LocalAppDimens.current

    StyledCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.period_statistics_title),
                    style = MaterialTheme.typography.titleSmall,
                    color = scheme.contentPrimary,
                )
                DateFilterButton(
                    filter = statistics.filter,
                    onClick = onDateFilterClick,
                    onClear = onClearDateFilter,
                )
            }

            Spacer(Modifier.height(14.dp))

            if (!statistics.hasData) {
                Text(
                    text = stringResource(R.string.period_no_data),
                    style = MaterialTheme.typography.bodyMedium,
                    color = scheme.contentSecondary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                return@Column
            }

            Text(
                text = stringResource(R.string.stat_total_spent),
                style = MaterialTheme.typography.labelLarge,
                color = scheme.contentSecondary,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                // Between a tile figure and a hero one: this card is a section
                // of the screen, not the screen's answer, so it must not
                // outshout the car's own total spending above it.
                text = statistics.totalCost.formatMoney(),
                style = AppNumbers.hero.copy(fontSize = 30.sp, lineHeight = 36.sp),
                color = scheme.actionPrimary,
                maxLines = 1,
            )

            Spacer(Modifier.height(14.dp))
            HorizontalDivider(thickness = dimens.size.divider, color = scheme.borderDefault)
            Spacer(Modifier.height(14.dp))

            KpiGrid(
                items = listOf(
                    Kpi(
                        // The count rides along with the label rather than as a
                        // caption under the value: "Fuel cost (12)" is one line
                        // and keeps every cell in the grid the same height.
                        label = stringResource(R.string.fuel_cost) +
                            " (${statistics.refillCount})",
                        value = statistics.fuelCost.formatMoney(),
                    ),
                    Kpi(
                        label = stringResource(R.string.expenses) +
                            " (${statistics.expenseCount})",
                        value = statistics.expensesCost.formatMoney(),
                    ),
                    Kpi(
                        label = stringResource(R.string.distance),
                        value = statistics.totalDistance.toInt().toLong().formatNumber(),
                        unit = "km",
                    ),
                    Kpi(
                        label = stringResource(R.string.stat_liters),
                        value = statistics.totalLiters.formatNumber(1),
                        unit = "L",
                    ),
                    Kpi(
                        label = stringResource(R.string.avg_consumption),
                        value = if (statistics.averageConsumption > 0) {
                            statistics.averageConsumption.formatNumber(1)
                        } else {
                            stringResource(R.string.not_available)
                        },
                        unit = if (statistics.averageConsumption > 0) "L/100" else null,
                    ),
                    Kpi(
                        label = stringResource(R.string.cost_per_km),
                        value = if (statistics.costPerKilometer > 0) {
                            statistics.costPerKilometer.formatMoney()
                        } else {
                            stringResource(R.string.not_available)
                        },
                    ),
                ),
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 380)
@Composable
private fun PeriodStatisticsCardPreview() {
    CarTrackingAppTheme(darkTheme = true) {
        Column(Modifier.padding(18.dp)) {
            PeriodStatisticsCard(
                statistics = PeriodStatistics(
                    filter = DateFilter.of(2026, 10),
                    totalCost = 988.46,
                    fuelCost = 463.0,
                    expensesCost = 525.46,
                    refillCount = 6,
                    expenseCount = 3,
                    totalDistance = 1840.0,
                    totalLiters = 136.4,
                    averageConsumption = 7.4,
                    costPerKilometer = 0.54,
                ),
                onDateFilterClick = {},
                onClearDateFilter = {},
            )
        }
    }
}
