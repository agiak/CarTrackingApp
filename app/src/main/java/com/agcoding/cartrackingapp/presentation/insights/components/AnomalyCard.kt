package com.agcoding.cartrackingapp.presentation.insights.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.domain.model.Anomaly
import com.agcoding.cartrackingapp.domain.model.AnomalySeverity
import com.agcoding.cartrackingapp.domain.model.AnomalyType
import com.agcoding.cartrackingapp.presentation.insights.AnomalyLocalizer
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Card displaying a single anomaly with icon, title, description, and severity indicator.
 *
 * @param anomaly The anomaly to display
 * @param onClick Callback when card is clicked (only triggered if anomaly has related transaction)
 * @param onAddToTrip Callback for adding refill to trip (for MISSING_TRIP_REFILL anomalies)
 * @param modifier Modifier for the card
 */
@Composable
fun AnomalyCard(
    anomaly: Anomaly,
    onClick: () -> Unit,
    onAddToTrip: ((refillId: Long, tripId: Long) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val isClickable = anomaly.relatedTransactionId != null
    val context = LocalContext.current
    val localizer = AnomalyLocalizer(context)

    // Get localized title and description
    val localizedTitle = localizer.getLocalizedTitle(anomaly)
    val localizedDescription = localizer.getLocalizedDescription(anomaly)

    Card(
        modifier = modifier.then(
            if (isClickable) {
                Modifier.clickable(onClick = onClick)
            } else {
                Modifier
            }
        ),
        colors = CardDefaults.cardColors(
            containerColor = getSeverityBackgroundColor(anomaly.severity)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Icon
            Icon(
                imageVector = getAnomalyIcon(anomaly.type),
                contentDescription = null,
                tint = getSeverityColor(anomaly.severity),
                modifier = Modifier.size(32.dp)
            )

            // Content
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Title and severity
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = localizedTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )

                    SeverityBadge(severity = anomaly.severity)
                }

                // Description
                Text(
                    text = localizedDescription,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Detection date
                Text(
                    text = stringResource(
                        R.string.insights_detected_on,
                        anomaly.detectedAt.format(
                            DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.getDefault())
                        )
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Add to Trip button for MISSING_TRIP_REFILL anomalies
                if (anomaly.type == AnomalyType.MISSING_TRIP_REFILL &&
                    anomaly.relatedTransactionId != null &&
                    anomaly.suggestedTripId != null &&
                    onAddToTrip != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            onAddToTrip(anomaly.relatedTransactionId, anomaly.suggestedTripId)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddCircle,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = stringResource(R.string.add_to_trip),
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }

            // Arrow indicator for clickable cards
            if (isClickable) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = stringResource(R.string.view_details),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .size(20.dp)
                        .align(Alignment.CenterVertically)
                )
            }
        }
    }
}

@Composable
private fun SeverityBadge(severity: AnomalySeverity) {
    Surface(
        color = getSeverityColor(severity).copy(alpha = 0.2f),
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = getSeverityLabel(severity),
            style = MaterialTheme.typography.labelSmall,
            color = getSeverityColor(severity),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun getAnomalyIcon(type: AnomalyType): ImageVector {
    return when (type) {
        AnomalyType.FUEL_PRICE_SPIKE -> Icons.AutoMirrored.Filled.TrendingUp
        AnomalyType.CONSUMPTION_SPIKE -> Icons.Default.Speed
        AnomalyType.MAINTENANCE_OUTLIER -> Icons.Default.Build
        AnomalyType.MONTHLY_SPENDING_INCREASE -> Icons.Default.AccountBalanceWallet
        AnomalyType.COST_PER_KM_DEVIATION -> Icons.Default.Payments
        AnomalyType.MISSING_TRIP_REFILL -> Icons.Default.Flag
    }
}

@Composable
private fun getSeverityColor(severity: AnomalySeverity): Color {
    return when (severity) {
        AnomalySeverity.LOW -> MaterialTheme.colorScheme.tertiary
        AnomalySeverity.MEDIUM -> MaterialTheme.colorScheme.secondary
        AnomalySeverity.HIGH -> MaterialTheme.colorScheme.error
    }
}

@Composable
private fun getSeverityBackgroundColor(severity: AnomalySeverity): Color {
    return when (severity) {
        AnomalySeverity.LOW -> MaterialTheme.colorScheme.surfaceVariant
        AnomalySeverity.MEDIUM -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
        AnomalySeverity.HIGH -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
    }
}

@Composable
private fun getSeverityLabel(severity: AnomalySeverity): String {
    return when (severity) {
        AnomalySeverity.LOW -> stringResource(R.string.severity_low)
        AnomalySeverity.MEDIUM -> stringResource(R.string.severity_medium)
        AnomalySeverity.HIGH -> stringResource(R.string.severity_high)
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewAnomalyCardHigh() {
    CarTrackingAppTheme {
        AnomalyCard(
            anomaly = Anomaly(
                id = "1",
                carId = 1L,
                type = AnomalyType.FUEL_PRICE_SPIKE,
                severity = AnomalySeverity.HIGH,
                title = "Fuel Price Spike Detected",
                description = "Fuel price increased by 35% compared to 6-month average (€1.450/L). Current: €1.958/L",
                detectedAt = LocalDate.now(),
                relatedTransactionId = 123L
            ),
            onClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewAnomalyCardMedium() {
    CarTrackingAppTheme {
        AnomalyCard(
            anomaly = Anomaly(
                id = "2",
                carId = 1L,
                type = AnomalyType.MONTHLY_SPENDING_INCREASE,
                severity = AnomalySeverity.MEDIUM,
                title = "Monthly Spending Increased",
                description = "Total spending in February 2026 (€450.00) is 28% higher than 3-month average (€351.56).",
                detectedAt = LocalDate.now(),
                relatedTransactionId = null
            ),
            onClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewAnomalyCardLow() {
    CarTrackingAppTheme {
        AnomalyCard(
            anomaly = Anomaly(
                id = "3",
                carId = 1L,
                type = AnomalyType.CONSUMPTION_SPIKE,
                severity = AnomalySeverity.LOW,
                title = "Unusual Fuel Consumption",
                description = "Fuel consumption increased by 17% compared to 6-month average (7.2 L/100km). Current: 8.4 L/100km",
                detectedAt = LocalDate.now(),
                relatedTransactionId = 456L
            ),
            onClick = {}
        )
    }
}

