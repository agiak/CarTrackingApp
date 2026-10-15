package com.agcoding.cartrackingapp.presentation.carcomparison

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.domain.model.CarComparisonData
import com.agcoding.cartrackingapp.domain.model.MultiCarComparisonResult
import com.agcoding.cartrackingapp.presentation.components.SectionLabel
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppColorScheme
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppDimens
import com.agcoding.cartrackingapp.shared.ui.tokens.AppNumbers
import com.agcoding.cartrackingapp.shared.ui.tokens.categoryService
import com.agcoding.cartrackingapp.util.formatMoney
import com.agcoding.cartrackingapp.util.formatMoneyPer
import com.agcoding.cartrackingapp.util.formatNumber

/**
 * Every car, ranked on the three things that decide what one costs to run.
 *
 * The verdict goes first and alone: one banner naming the cheapest car, with
 * its cost per kilometre under it. That is the answer to the question the
 * screen was opened to ask, and it should not have to be assembled out of
 * three tables.
 *
 * Under it, each metric is a list of bars rather than a column of figures. A
 * ranked list of numbers tells you the order; it does not tell you whether the
 * second car is a hair behind the first or half as good again, which is the
 * part that decides anything. The bars are drawn against the worst value in
 * the metric, so the spread fills the width even when every car is close.
 *
 * Green is the best in each metric and rose the worst, which is the same pair
 * the deltas and the insights use. Cars with no figure for a metric are left
 * out of that metric's list and named under it — an empty bar at the bottom of
 * a ranking reads as "worst", which would be a lie.
 */
@Composable
fun AllCarsComparisonContent(result: MultiCarComparisonResult) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.screenWidthDp > configuration.screenHeightDp
    val useSplitView = configuration.screenWidthDp >= 600 || isLandscape
    val dimens = LocalAppDimens.current

    if (useSplitView) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(dimens.spacing.screenHorizontal),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                VerdictBanner(result = result)
                Spacer(modifier = Modifier.height(dimens.spacing.sectionTop))
                CostPerKmRanking(result)
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                ConsumptionRanking(result)
                Spacer(modifier = Modifier.height(dimens.spacing.sectionTop))
                MaintenanceRanking(result)
            }
        }
    } else {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(
                    start = dimens.spacing.screenHorizontal,
                    end = dimens.spacing.screenHorizontal,
                    top = 4.dp,
                    bottom = 28.dp
                )
        ) {
            VerdictBanner(result = result)
            Spacer(modifier = Modifier.height(dimens.spacing.sectionTop))
            CostPerKmRanking(result)
            Spacer(modifier = Modifier.height(dimens.spacing.sectionTop))
            ConsumptionRanking(result)
            Spacer(modifier = Modifier.height(dimens.spacing.sectionTop))
            MaintenanceRanking(result)
        }
    }
}

@Composable
private fun CostPerKmRanking(result: MultiCarComparisonResult) {
    MetricRanking(
        title = stringResource(R.string.car_comparison_cost_per_km),
        cars = result.cars,
        value = { it.costPerKm },
        format = { it.formatMoneyPer("km", decimals = 3) },
        bestCarId = result.bestCostPerKm,
        worstCarId = result.worstCostPerKm
    )
}

@Composable
private fun ConsumptionRanking(result: MultiCarComparisonResult) {
    MetricRanking(
        title = stringResource(R.string.car_comparison_avg_consumption),
        cars = result.cars,
        value = { it.avgConsumption },
        format = { "${it.formatNumber(1)} L/100km" },
        bestCarId = result.bestConsumption,
        worstCarId = result.worstConsumption
    )
}

@Composable
private fun MaintenanceRanking(result: MultiCarComparisonResult) {
    MetricRanking(
        title = stringResource(R.string.car_comparison_maintenance_per_year),
        cars = result.cars,
        value = { it.maintenancePerYear },
        format = { it.formatMoney(decimals = 0) },
        bestCarId = result.bestMaintenance,
        worstCarId = result.worstMaintenance
    )
}

/**
 * The answer, in one card.
 *
 * The least economical car is a line at the bottom rather than a second half
 * of equal weight: knowing which car is cheapest is actionable, knowing which
 * is dearest is the same fact said backwards.
 */
@Composable
private fun VerdictBanner(result: MultiCarComparisonResult) {
    val scheme = LocalAppColorScheme.current
    val dimens = LocalAppDimens.current

    val best = result.cars.firstOrNull { it.carId == result.overallBest }
    val worst = result.cars.firstOrNull { it.carId == result.overallWorst }
    if (best == null) return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(dimens.radius.card))
            .background(scheme.actionSecondary)
            .padding(18.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(dimens.size.iconTile)
                    .clip(RoundedCornerShape(dimens.radius.iconTile))
                    .background(scheme.statusSuccess.copy(alpha = 0.18f))
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = scheme.statusSuccess,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.car_comparison_most_economical_label),
                    style = MaterialTheme.typography.labelMedium,
                    color = scheme.contentSecondary,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = best.carName,
                    style = MaterialTheme.typography.titleLarge,
                    color = scheme.contentPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            best.costPerKm?.let { cost ->
                Spacer(modifier = Modifier.width(10.dp))
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = cost.formatNumber(3),
                        style = AppNumbers.tile,
                        color = scheme.statusSuccess,
                        maxLines = 1
                    )
                    Text(
                        text = "€/km",
                        style = MaterialTheme.typography.labelSmall,
                        color = scheme.contentSecondary,
                        maxLines = 1
                    )
                }
            }
        }

        if (worst != null && worst.carId != best.carId) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(
                    R.string.car_comparison_least_economical,
                    worst.carName
                ),
                style = MaterialTheme.typography.bodySmall,
                color = scheme.contentSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun MetricRanking(
    title: String,
    cars: List<CarComparisonData>,
    value: (CarComparisonData) -> Double?,
    format: (Double) -> String,
    bestCarId: Long?,
    worstCarId: Long?
) {
    val scheme = LocalAppColorScheme.current
    val dimens = LocalAppDimens.current

    val ranked = cars.filter { value(it) != null }.sortedBy { value(it) }
    val missing = cars.filter { value(it) == null }

    Column(modifier = Modifier.fillMaxWidth()) {
        SectionLabel(title)
        Spacer(modifier = Modifier.height(10.dp))

        if (ranked.isEmpty()) {
            Text(
                text = stringResource(R.string.car_comparison_no_data_available),
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.contentSecondary
            )
        } else {
            // Against the worst value, not against zero: every car's cost per
            // kilometre is somewhere between 0,12 and 0,19, and bars drawn
            // from zero would all be the same length.
            val max = ranked.maxOf { value(it) ?: 0.0 }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(dimens.radius.card))
                    .background(scheme.backgroundCard)
                    .padding(16.dp)
            ) {
                ranked.forEachIndexed { index, car ->
                    if (index > 0) Spacer(modifier = Modifier.height(14.dp))
                    RankedBarRow(
                        rank = index + 1,
                        name = car.carName,
                        text = value(car)?.let(format).orEmpty(),
                        fraction = if (max > 0.0) {
                            ((value(car) ?: 0.0) / max).toFloat().coerceIn(0f, 1f)
                        } else {
                            0f
                        },
                        color = when (car.carId) {
                            bestCarId -> scheme.statusSuccess
                            worstCarId -> scheme.categoryService
                            else -> scheme.actionPrimary
                        }
                    )
                }
            }
        }

        if (missing.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(
                    R.string.car_comparison_not_enough_records,
                    missing.joinToString(", ") { it.carName }
                ),
                style = MaterialTheme.typography.labelSmall,
                color = scheme.contentDisabled,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun RankedBarRow(
    rank: Int,
    name: String,
    text: String,
    fraction: Float,
    color: Color
) {
    val scheme = LocalAppColorScheme.current

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = rank.toString().padStart(2, '0'),
                style = AppNumbers.tag,
                color = scheme.contentDisabled,
                maxLines = 1
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = name,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.titleSmall,
                color = scheme.contentPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = text,
                style = AppNumbers.compact,
                color = color,
                textAlign = TextAlign.End,
                maxLines = 1
            )
        }
        Spacer(modifier = Modifier.height(7.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(scheme.backgroundSecondary)
        ) {
            if (fraction > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(fraction)
                        .clip(RoundedCornerShape(4.dp))
                        .background(color)
                )
            }
        }
    }
}

// ============================================
// Preview Composables
// ============================================

@Preview(name = "All cars comparison", showBackground = true, widthDp = 400, heightDp = 900)
@Composable
private fun PreviewAllCarsComparisonContent() {
    CarTrackingAppTheme(darkTheme = false) {
        AllCarsComparisonContent(result = previewMultiCarResult)
    }
}

@Preview(name = "All cars comparison - dark", showBackground = true, widthDp = 400, heightDp = 900)
@Composable
private fun PreviewAllCarsComparisonContentDark() {
    CarTrackingAppTheme(darkTheme = true) {
        AllCarsComparisonContent(result = previewMultiCarResult)
    }
}

@Preview(name = "Verdict banner", showBackground = true, widthDp = 400)
@Composable
private fun PreviewVerdictBanner() {
    CarTrackingAppTheme(darkTheme = true) {
        Column(modifier = Modifier.padding(18.dp)) {
            VerdictBanner(result = previewMultiCarResult)
        }
    }
}

@Preview(name = "Metric ranking", showBackground = true, widthDp = 400)
@Composable
private fun PreviewMetricRanking() {
    CarTrackingAppTheme(darkTheme = true) {
        Column(modifier = Modifier.padding(18.dp)) {
            CostPerKmRanking(previewMultiCarResult)
        }
    }
}
