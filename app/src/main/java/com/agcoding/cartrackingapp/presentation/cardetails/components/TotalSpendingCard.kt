package com.agcoding.cartrackingapp.presentation.cardetails.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import com.agcoding.cartrackingapp.presentation.components.CostSegment
import com.agcoding.cartrackingapp.presentation.components.SectionHeaderRow
import com.agcoding.cartrackingapp.presentation.components.StackedCostBar
import com.agcoding.cartrackingapp.presentation.components.StyledCard
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppColorScheme
import com.agcoding.cartrackingapp.shared.ui.tokens.AppNumbers
import com.agcoding.cartrackingapp.shared.ui.tokens.categoryFuel
import com.agcoding.cartrackingapp.shared.ui.tokens.categoryOther
import com.agcoding.cartrackingapp.shared.ui.tokens.categoryService
import com.agcoding.cartrackingapp.util.formatNumber
import kotlin.math.roundToInt

/**
 * Everything this car has cost, and the shape of it.
 *
 * Three figures side by side is a list of numbers; the same three as a bar is
 * a proportion, which is the question actually being asked — not "how much was
 * fuel" but "is fuel most of it". The percentage on each row is there because
 * a bar can be read to about ten percent by eye and no better, and the
 * difference between 38% and 44% of a five-figure total is real money.
 *
 * The heading sits outside the card with the total beside it, so the total is
 * readable while scrolling past without the card having to be tall enough to
 * hold a hero number of its own.
 */
@Composable
fun TotalSpendingCard(statistics: CarStatistics) {
    val scheme = LocalAppColorScheme.current

    val fuel = statistics.totalCost
    val service = statistics.serviceExpensesCost
    val other = statistics.otherExpensesCost
    val total = fuel + service + other

    val rows = listOf(
        CostSegment(fuel, scheme.categoryFuel, stringResource(R.string.fuel)),
        CostSegment(service, scheme.categoryService, stringResource(R.string.service)),
        CostSegment(other, scheme.categoryOther, stringResource(R.string.other)),
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            SectionHeaderRow(
                title = stringResource(R.string.total_spending),
                modifier = Modifier.weight(1f),
            )
            Text(
                text = stringResource(
                    R.string.car_card_currency_eur_format,
                    total.formatNumber(2),
                ),
                style = AppNumbers.value,
                color = scheme.actionPrimary,
                maxLines = 1,
            )
        }

        Spacer(Modifier.height(10.dp))

        StyledCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(18.dp)) {
                StackedCostBar(segments = rows)
                Spacer(Modifier.height(14.dp))
                rows.forEachIndexed { index, row ->
                    if (index > 0) Spacer(Modifier.height(10.dp))
                    SpendingRow(row = row, total = total)
                }
            }
        }
    }
}

@Composable
private fun SpendingRow(row: CostSegment, total: Double) {
    val scheme = LocalAppColorScheme.current
    // Guarded rather than letting 0/0 produce NaN, which formats as "NaN%".
    val share = if (total > 0) (row.value / total * 100).roundToInt() else 0

    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(width = 18.dp, height = 4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(row.color),
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = row.label,
            style = MaterialTheme.typography.bodyMedium,
            color = scheme.contentSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = stringResource(
                R.string.car_card_currency_eur_format,
                row.value.formatNumber(2),
            ),
            style = AppNumbers.compact,
            color = scheme.contentPrimary,
            maxLines = 1,
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = "$share%",
            style = MaterialTheme.typography.labelLarge,
            color = scheme.contentDisabled,
            maxLines = 1,
        )
    }
}

@Preview(showBackground = true, widthDp = 380)
@Composable
private fun TotalSpendingCardPreview() {
    CarTrackingAppTheme(darkTheme = true) {
        Column(Modifier.padding(18.dp)) {
            TotalSpendingCard(
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
                    serviceExpensesCost = 4200.0,
                    serviceExpenseCount = 18,
                    otherExpensesCost = 980.0,
                    otherExpenseCount = 11,
                    averagePricePerLiter = 1.65,
                    totalLiters = 5200.0,
                    recentRefills = emptyList(),
                )
            )
        }
    }
}
