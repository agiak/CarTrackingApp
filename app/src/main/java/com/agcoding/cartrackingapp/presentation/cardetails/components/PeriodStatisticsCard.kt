@file:OptIn(ExperimentalLayoutApi::class)

package com.agcoding.cartrackingapp.presentation.cardetails.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.domain.model.DateFilter
import com.agcoding.cartrackingapp.domain.model.PeriodStatistics
import com.agcoding.cartrackingapp.presentation.components.DateFilterButton
import com.agcoding.cartrackingapp.presentation.components.StyledCard
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme
import com.agcoding.cartrackingapp.util.formatMoney
import com.agcoding.cartrackingapp.util.formatNumber

/**
 * Statistics for the period the user picked — all time, a year, or a single month.
 *
 * The date chip in the header opens the shared date filter sheet, the same control
 * used on every other screen that filters by date.
 */
@Composable
fun PeriodStatisticsCard(
    statistics: PeriodStatistics,
    onDateFilterClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    StyledCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.period_statistics_title),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                DateFilterButton(
                    filter = statistics.filter,
                    onClick = onDateFilterClick
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (!statistics.hasData) {
                Text(
                    text = stringResource(R.string.period_no_data),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                return@Column
            }

            // Headline figure — what the period cost in total.
            Text(
                text = stringResource(R.string.stat_total_spent),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = statistics.totalCost.formatMoney(),
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(12.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                maxItemsInEachRow = 2
            ) {
                val cellModifier = Modifier.weight(1f)

                PeriodStat(
                    label = stringResource(R.string.fuel_cost),
                    value = statistics.fuelCost.formatMoney(),
                    caption = stringResource(R.string.refills_section_header, statistics.refillCount),
                    modifier = cellModifier
                )
                PeriodStat(
                    label = stringResource(R.string.expenses),
                    value = statistics.expensesCost.formatMoney(),
                    caption = stringResource(R.string.services_section_header, statistics.expenseCount),
                    modifier = cellModifier
                )
                PeriodStat(
                    label = stringResource(R.string.distance),
                    value = "${statistics.totalDistance.toInt().formatNumber()} km",
                    modifier = cellModifier
                )
                PeriodStat(
                    label = stringResource(R.string.stat_liters),
                    value = "${statistics.totalLiters.formatNumber(1)} L",
                    modifier = cellModifier
                )
                PeriodStat(
                    label = stringResource(R.string.avg_consumption),
                    value = if (statistics.averageConsumption > 0) {
                        "${statistics.averageConsumption.formatNumber(1)} L/100km"
                    } else "-",
                    modifier = cellModifier
                )
                PeriodStat(
                    label = stringResource(R.string.cost_per_km),
                    value = if (statistics.costPerKilometer > 0) {
                        statistics.costPerKilometer.formatMoney()
                    } else "-",
                    modifier = cellModifier
                )
            }
        }
    }
}

@Composable
private fun PeriodStat(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    caption: String? = null
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1
        )
        if (caption != null) {
            Text(
                text = caption,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}

// ============================================
// Preview Composables
// ============================================

private val sampleStatistics = PeriodStatistics(
    filter = DateFilter(year = 2025, month = 3),
    totalCost = 742.5,
    fuelCost = 512.5,
    expensesCost = 230.0,
    totalDistance = 1840.0,
    totalLiters = 128.4,
    averageConsumption = 6.98,
    averagePricePerLiter = 1.72,
    costPerKilometer = 0.40,
    refillCount = 4,
    expenseCount = 2
)

@Preview(name = "Period statistics", showBackground = true, widthDp = 400)
@Composable
private fun PreviewPeriodStatisticsCard() {
    CarTrackingAppTheme(darkTheme = false) {
        PeriodStatisticsCard(statistics = sampleStatistics, onDateFilterClick = {})
    }
}

@Preview(name = "Period statistics - empty", showBackground = true, widthDp = 400)
@Composable
private fun PreviewPeriodStatisticsCardEmpty() {
    CarTrackingAppTheme(darkTheme = false) {
        PeriodStatisticsCard(
            statistics = PeriodStatistics(filter = DateFilter(year = 2024)),
            onDateFilterClick = {}
        )
    }
}

@Preview(name = "Period statistics - dark", showBackground = true, widthDp = 400)
@Composable
private fun PreviewPeriodStatisticsCardDark() {
    CarTrackingAppTheme(darkTheme = true) {
        PeriodStatisticsCard(statistics = sampleStatistics, onDateFilterClick = {})
    }
}
