package com.agcoding.cartrackingapp.presentation.statistics.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingFlat
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Icon
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
import com.agcoding.cartrackingapp.domain.model.ForecastResult
import com.agcoding.cartrackingapp.presentation.components.SectionLabel
import com.agcoding.cartrackingapp.presentation.statistics.CarSpecificInsights
import com.agcoding.cartrackingapp.presentation.statistics.FuelForecastUiState
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppColorScheme
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppDimens
import com.agcoding.cartrackingapp.shared.ui.tokens.AppNumbers
import com.agcoding.cartrackingapp.shared.ui.tokens.categoryService
import com.agcoding.cartrackingapp.util.formatNumber
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * What next month is likely to cost, for one car.
 *
 * The screen used to stack every car's forecast one under the other, each in a
 * card that opened with a hero figure, repeated the same figure in a row
 * beneath it, and then printed a paragraph of trend prose with an emoji in it.
 * Three cars meant three of those, and a prediction nobody could compare
 * against another.
 *
 * One car at a time now, chosen with a chip — the screen's own question is
 * "what about *this* car" — and the forecast is two readouts side by side with
 * a trend pill each, over one confidence bar. The trend prose moves into the
 * tips, which is where advice belongs.
 */
@Composable
fun FuelForecastSection(
    forecast: FuelForecastUiState,
    modifier: Modifier = Modifier
) {
    val scheme = LocalAppColorScheme.current
    val dimens = LocalAppDimens.current
    val months = maxOf(
        forecast.costPerKmForecast?.dataPointsUsed ?: 0,
        forecast.efficiencyForecast?.dataPointsUsed ?: 0
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(dimens.radius.card))
                .background(scheme.backgroundCard)
                .padding(18.dp)
        ) {
            Text(
                text = stringResource(R.string.forecast_next_month),
                style = MaterialTheme.typography.labelLarge,
                color = scheme.contentSecondary
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = forecast.carName,
                style = MaterialTheme.typography.titleMedium,
                color = scheme.contentPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.forecast_based_on_months, months),
                style = MaterialTheme.typography.bodySmall,
                color = scheme.contentSecondary
            )

            if (forecast.showLowDataWarning) {
                Spacer(Modifier.height(14.dp))
                LowDataWarning()
            }

            Spacer(Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ForecastReadout(
                    label = stringResource(R.string.forecast_cost_per_km),
                    forecast = forecast.costPerKmForecast,
                    decimals = 3,
                    unit = stringResource(R.string.currency_per_km)
                )
                ForecastReadout(
                    label = stringResource(R.string.forecast_efficiency),
                    forecast = forecast.efficiencyForecast,
                    decimals = 1,
                    unit = stringResource(R.string.liters_per_100km)
                )
            }

            val confidence = forecast.costPerKmForecast?.confidence
                ?: forecast.efficiencyForecast?.confidence
            if (confidence != null) {
                Spacer(Modifier.height(18.dp))
                ConfidenceBar(confidence = confidence)
            }
        }

        val tips = improvementTips(forecast.carInsights)
        if (tips.isNotEmpty()) {
            Spacer(Modifier.height(dimens.spacing.sectionTop))
            SectionLabel(stringResource(R.string.forecast_tips_for_car, forecast.carName))
            Spacer(Modifier.height(10.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(dimens.radius.card))
                    .background(scheme.backgroundCard)
                    .padding(16.dp)
            ) {
                tips.forEachIndexed { index, tip ->
                    if (index > 0) Spacer(Modifier.height(14.dp))
                    TipRow(tip)
                }
            }
        }
    }
}

/**
 * One predicted figure, its unit, and which way it is moving.
 *
 * A metric with no forecast is drawn as a dash rather than left out, so the
 * two cells stay the same width and the card does not change shape between
 * cars.
 */
@Composable
private fun RowScope.ForecastReadout(
    label: String,
    forecast: ForecastResult?,
    decimals: Int,
    unit: String
) {
    val scheme = LocalAppColorScheme.current
    val dimens = LocalAppDimens.current

    Column(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .clip(RoundedCornerShape(dimens.radius.tile))
            .background(scheme.backgroundSecondary)
            .padding(14.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = scheme.contentSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = forecast?.predictedNextValue?.formatNumber(decimals) ?: "—",
                style = AppNumbers.tile,
                color = scheme.contentPrimary,
                maxLines = 1
            )
            Spacer(Modifier.width(3.dp))
            Text(
                text = unit,
                style = MaterialTheme.typography.labelSmall,
                color = scheme.contentSecondary,
                modifier = Modifier.padding(bottom = 2.dp),
                maxLines = 1
            )
        }
        if (forecast != null) {
            Spacer(Modifier.height(8.dp))
            TrendPill(trend = forecast.trend, value = forecast.predictedNextValue)
        }
    }
}

/**
 * Which way the metric is heading, as a percentage of where it is.
 *
 * Both metrics on this screen are costs, so down is good and up is not — the
 * same green and rose the rest of the app uses for exactly that. A trend too
 * small to matter gets the flat arrow and no colour rather than a precise
 * figure implying a precision the forecast does not have.
 */
@Composable
private fun TrendPill(trend: Double, value: Double) {
    val scheme = LocalAppColorScheme.current
    val percent = if (value != 0.0) abs(trend / value * 100) else 0.0
    val flat = percent < 1.0

    val tint = when {
        flat -> scheme.contentSecondary
        trend < 0 -> scheme.statusSuccess
        else -> scheme.categoryService
    }
    val icon = when {
        flat -> Icons.AutoMirrored.Filled.TrendingFlat
        trend < 0 -> Icons.AutoMirrored.Filled.TrendingDown
        else -> Icons.AutoMirrored.Filled.TrendingUp
    }
    val sign = if (trend < 0) "−" else "+"
    val text = if (flat) "±${percent.formatNumber(1)}%" else "$sign${percent.formatNumber(1)}%"

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(LocalAppDimens.current.radius.chip))
            .background(tint.copy(alpha = 0.14f))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(13.dp)
        )
        Spacer(Modifier.width(4.dp))
        Text(text = text, style = AppNumbers.tag, color = tint, maxLines = 1)
    }
}

/**
 * How much to trust the two figures above.
 *
 * A bar rather than a percentage on its own: "78%" means nothing without
 * knowing what a good score looks like, and a track that is mostly full says
 * that at a glance. The colour is the same three-step scale the old badge
 * used, now mapped onto the palette rather than onto fixed Material reds.
 */
@Composable
private fun ConfidenceBar(confidence: Double) {
    val scheme = LocalAppColorScheme.current
    val percent = (confidence * 100).roundToInt().coerceIn(0, 100)

    val tint = when {
        confidence >= 0.7 -> scheme.statusSuccess
        confidence >= 0.4 -> scheme.statusWarning
        else -> scheme.categoryService
    }
    val label = stringResource(
        when {
            confidence >= 0.7 -> R.string.forecast_confidence_high
            confidence >= 0.4 -> R.string.forecast_confidence_medium
            else -> R.string.forecast_confidence_low
        }
    )

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.forecast_confidence),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelLarge,
                color = scheme.contentSecondary
            )
            Text(
                text = stringResource(R.string.forecast_confidence_value, percent, label),
                style = AppNumbers.compact,
                color = tint,
                maxLines = 1
            )
        }
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(scheme.backgroundSecondary)
        ) {
            if (percent > 0) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(percent / 100f)
                        .clip(RoundedCornerShape(4.dp))
                        .background(tint)
                )
            }
        }
    }
}

/** Too little history for the figures above to mean much. */
@Composable
private fun LowDataWarning() {
    val scheme = LocalAppColorScheme.current
    val dimens = LocalAppDimens.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(dimens.radius.tile))
            .background(scheme.statusWarning.copy(alpha = 0.14f))
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Icon(
            imageVector = Icons.Default.WarningAmber,
            contentDescription = null,
            tint = scheme.statusWarning,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = stringResource(R.string.forecast_low_data_warning),
            style = MaterialTheme.typography.bodySmall,
            color = scheme.statusWarning
        )
    }
}

@Composable
private fun TipRow(tip: String) {
    val scheme = LocalAppColorScheme.current
    Row(verticalAlignment = Alignment.Top) {
        Icon(
            imageVector = Icons.Default.Lightbulb,
            contentDescription = null,
            tint = scheme.actionPrimary,
            modifier = Modifier
                .padding(top = 1.dp)
                .size(17.dp)
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = tip,
            style = MaterialTheme.typography.bodyMedium,
            color = scheme.contentSecondary
        )
    }
}

/**
 * Advice drawn from this car's own history, falling back to the general
 * advice only when nothing specific can be said.
 */
@Composable
private fun improvementTips(insights: CarSpecificInsights): List<String> = buildList {
    if (insights.hasSummerConsumptionIssue) add(stringResource(R.string.forecast_tip_summer_issue))
    if (insights.hasWinterConsumptionIssue) add(stringResource(R.string.forecast_tip_winter_issue))
    if (insights.isWeatherStable) add(stringResource(R.string.forecast_tip_weather_stable))
    if (insights.hasIncreasingTrend) {
        add(stringResource(R.string.forecast_tip_increasing_trend))
    } else if (insights.hasImprovingTrend) {
        add(stringResource(R.string.forecast_tip_improving_trend))
    }
    if (insights.avgMonthlyVariation > 0.15) {
        add(stringResource(R.string.forecast_tip_high_variation))
    } else if (insights.avgMonthlyVariation < 0.05) {
        add(stringResource(R.string.forecast_tip_stable_consumption))
    }
    if (isEmpty()) {
        add(stringResource(R.string.forecast_tip_general_1))
        add(stringResource(R.string.forecast_tip_general_2))
        add(stringResource(R.string.forecast_tip_general_3))
    }
}

// ============================================
// Preview Composables
// ============================================

private val previewForecast = FuelForecastUiState(
    carId = 1,
    carName = "Toyota Corolla",
    costPerKmForecast = ForecastResult(
        predictedNextValue = 0.142,
        trend = 0.0044,
        confidence = 0.78,
        dataPointsUsed = 18
    ),
    efficiencyForecast = ForecastResult(
        predictedNextValue = 8.9,
        trend = -0.018,
        confidence = 0.78,
        dataPointsUsed = 18
    ),
    carInsights = CarSpecificInsights(
        hasWinterConsumptionIssue = true,
        hasIncreasingTrend = true
    )
)

@Preview(name = "Forecast — confident", showBackground = true, widthDp = 390)
@Composable
private fun PreviewFuelForecast() {
    CarTrackingAppTheme(darkTheme = true) {
        Column(Modifier.padding(18.dp)) {
            FuelForecastSection(forecast = previewForecast)
        }
    }
}

@Preview(name = "Forecast — thin data", showBackground = true, widthDp = 390)
@Composable
private fun PreviewFuelForecastThin() {
    CarTrackingAppTheme(darkTheme = true) {
        Column(Modifier.padding(18.dp)) {
            FuelForecastSection(
                forecast = previewForecast.copy(
                    carName = "Volkswagen Golf",
                    costPerKmForecast = ForecastResult(0.137, 0.0005, 0.36, 2),
                    efficiencyForecast = ForecastResult(8.8, 0.17, 0.36, 2),
                    carInsights = CarSpecificInsights(avgMonthlyVariation = 0.21)
                )
            )
        }
    }
}

@Preview(name = "Forecast — light", showBackground = true, widthDp = 390)
@Composable
private fun PreviewFuelForecastLight() {
    CarTrackingAppTheme(darkTheme = false) {
        Column(Modifier.padding(18.dp)) {
            FuelForecastSection(forecast = previewForecast)
        }
    }
}
