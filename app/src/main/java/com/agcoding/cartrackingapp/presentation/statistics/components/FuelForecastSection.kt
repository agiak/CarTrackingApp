package com.agcoding.cartrackingapp.presentation.statistics.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingFlat
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.domain.model.ForecastResult
import com.agcoding.cartrackingapp.presentation.components.StyledCard
import com.agcoding.cartrackingapp.presentation.statistics.FuelForecastUiState
import java.util.Locale

/**
 * Displays fuel forecast section with predictions per car.
 *
 * Shows:
 * - Cost per km forecast
 * - Fuel efficiency forecast
 * - Trend indicators
 * - Confidence levels
 * - Low data warnings
 */
@Composable
fun FuelForecastSection(
    forecasts: List<FuelForecastUiState>,
    modifier: Modifier = Modifier
) {
    if (forecasts.isEmpty()) return

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        forecasts.forEach { forecast ->
            FuelForecastCard(forecast = forecast)
        }
    }
}

/**
 * Individual car forecast card.
 */
@Composable
private fun FuelForecastCard(
    forecast: FuelForecastUiState,
    modifier: Modifier = Modifier
) {
    StyledCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Car name
            Text(
                text = forecast.carName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Explanation text
            Text(
                text = stringResource(
                    R.string.forecast_explanation,
                    forecast.costPerKmForecast?.dataPointsUsed ?: 0
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                fontSize = 12.sp
            )

            // Low data warning
            if (forecast.showLowDataWarning) {
                LowDataWarning()
            }

            // Cost per km forecast
            forecast.costPerKmForecast?.let { costForecast ->
                ForecastMetricRow(
                    label = stringResource(R.string.forecast_cost_per_km),
                    forecast = costForecast,
                    unit = stringResource(R.string.currency_per_km),
                    trendExplanation = getTrendExplanation(costForecast.trend)
                )
            }

            // Efficiency forecast
            forecast.efficiencyForecast?.let { efficiencyForecast ->
                ForecastMetricRow(
                    label = stringResource(R.string.forecast_efficiency),
                    forecast = efficiencyForecast,
                    unit = stringResource(R.string.liters_per_100km),
                    trendExplanation = getTrendExplanation(efficiencyForecast.trend)
                )
            }

            // Car-specific improvement tips
            HowToImproveTips(forecast = forecast)
        }
    }
}

/**
 * Get trend explanation text based on trend value.
 */
@Composable
private fun getTrendExplanation(trend: Double): String {
    return when {
        trend > 0.001 -> stringResource(R.string.forecast_trend_increasing)
        trend < -0.001 -> stringResource(R.string.forecast_trend_decreasing)
        else -> stringResource(R.string.forecast_trend_stable)
    }
}

/**
 * Single forecast metric row.
 */
@Composable
private fun ForecastMetricRow(
    label: String,
    forecast: ForecastResult,
    unit: String,
    trendExplanation: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = String.format(Locale.US, "%.3f", forecast.predictedNextValue),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Text(
                        text = unit,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )

                    // Trend indicator
                    TrendIndicator(trend = forecast.trend)
                }
            }

            // Confidence badge
            ConfidenceBadge(confidence = forecast.confidence)
        }

        // Trend explanation with color coding
        val trendColor = when {
            forecast.trend > 0.001 -> Color(0xFFE57373) // Increasing (red/bad)
            forecast.trend < -0.001 -> Color(0xFF81C784) // Decreasing (green/good)
            else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f) // Stable
        }

        Text(
            text = trendExplanation,
            style = MaterialTheme.typography.bodySmall,
            color = trendColor,
            fontSize = 11.sp,
            lineHeight = 14.sp
        )
    }
}

/**
 * Trend direction indicator.
 */
@Composable
private fun TrendIndicator(
    trend: Double,
    modifier: Modifier = Modifier
) {
    val (icon, color) = when {
        trend > 0.001 -> Icons.AutoMirrored.Filled.TrendingUp to Color(0xFFE57373) // Increasing (red/bad)
        trend < -0.001 -> Icons.AutoMirrored.Filled.TrendingDown to Color(0xFF81C784) // Decreasing (green/good)
        else -> Icons.AutoMirrored.Filled.TrendingFlat to Color.Gray // Stable
    }

    Icon(
        imageVector = icon,
        contentDescription = null,
        tint = color,
        modifier = modifier.size(20.dp)
    )
}

/**
 * Confidence score badge with label.
 */
@Composable
private fun ConfidenceBadge(
    confidence: Double,
    modifier: Modifier = Modifier
) {
    val confidencePercent = (confidence * 100).toInt()
    val color = when {
        confidence >= 0.7 -> Color(0xFF81C784) // High confidence - green
        confidence >= 0.4 -> Color(0xFFFFB74D) // Medium confidence - orange
        else -> Color(0xFFE57373) // Low confidence - red
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Text(
            text = stringResource(R.string.forecast_confidence_label),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
            fontSize = 10.sp
        )
        Text(
            text = "$confidencePercent%",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

/**
 * Car-specific tips on how to improve fuel efficiency based on actual data patterns.
 */
@Composable
private fun HowToImproveTips(
    forecast: FuelForecastUiState,
    modifier: Modifier = Modifier
) {
    val insights = forecast.carInsights
    val tips = buildList {
        // Seasonal patterns
        if (insights.hasSummerConsumptionIssue) {
            add(stringResource(R.string.forecast_tip_summer_issue))
        }
        if (insights.hasWinterConsumptionIssue) {
            add(stringResource(R.string.forecast_tip_winter_issue))
        }

        // Weather stability
        if (insights.isWeatherStable) {
            add(stringResource(R.string.forecast_tip_weather_stable))
        }

        // Trend-based tips
        if (insights.hasIncreasingTrend) {
            add(stringResource(R.string.forecast_tip_increasing_trend))
        } else if (insights.hasImprovingTrend) {
            add(stringResource(R.string.forecast_tip_improving_trend))
        }

        // High variation tips
        if (insights.avgMonthlyVariation > 0.15) {
            add(stringResource(R.string.forecast_tip_high_variation))
        } else if (insights.avgMonthlyVariation < 0.05) {
            add(stringResource(R.string.forecast_tip_stable_consumption))
        }

        // General tips (always show at least these)
        if (isEmpty()) {
            add(stringResource(R.string.forecast_tip_general_1))
            add(stringResource(R.string.forecast_tip_general_2))
            add(stringResource(R.string.forecast_tip_general_3))
        }
    }

    if (tips.isEmpty()) return

    StyledCard(
        modifier = modifier.fillMaxWidth(),
        tintAlpha = 0.15f
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "💡 " + stringResource(R.string.forecast_tips_for_car, forecast.carName),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                fontSize = 12.sp
            )

            tips.forEach { tip ->
                Text(
                    text = "• $tip",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                    fontSize = 11.sp,
                    lineHeight = 15.sp
                )
            }
        }
    }
}

/**
 * Warning message for insufficient data.
 */
@Composable
private fun LowDataWarning(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        )

        Text(
            text = stringResource(R.string.forecast_low_data_warning),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
            fontSize = 12.sp
        )
    }
}

