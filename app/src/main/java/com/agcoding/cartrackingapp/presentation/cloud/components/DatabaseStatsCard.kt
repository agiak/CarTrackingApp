package com.agcoding.cartrackingapp.presentation.cloud.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Route
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.domain.model.DatabaseStats
import com.agcoding.cartrackingapp.presentation.components.StyledCard
import com.agcoding.cartrackingapp.presentation.settings.components.SectionHeader
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme
import java.text.NumberFormat

/**
 * What this phone is actually holding, on the screen about backing it up.
 *
 * It belongs here rather than in Settings because it answers the question the
 * account screen raises and never otherwise answers: *how much would I lose?*
 * A number makes that concrete in a way "sync your data" does not.
 */
@Composable
fun DatabaseStatsCard(
    stats: DatabaseStats,
    modifier: Modifier = Modifier,
) {
    // Thousands separators from the device's locale, so 1.234 reads as a
    // number and not as a version string.
    val format = remember { NumberFormat.getIntegerInstance() }

    Column(modifier = modifier.fillMaxWidth()) {
        SectionHeader(title = stringResource(R.string.cloud_section_data))

        StyledCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    StatTile(
                        icon = Icons.Default.DirectionsCar,
                        value = format.format(stats.cars),
                        label = stringResource(R.string.cloud_stats_cars),
                        accent = MaterialTheme.colorScheme.primary,
                    )
                    StatTile(
                        icon = Icons.Default.LocalGasStation,
                        value = format.format(stats.refills),
                        label = stringResource(R.string.cloud_stats_refills),
                        accent = MaterialTheme.colorScheme.tertiary,
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    StatTile(
                        icon = Icons.Default.Payments,
                        value = format.format(stats.expenses),
                        label = stringResource(R.string.cloud_stats_expenses),
                        accent = MaterialTheme.colorScheme.secondary,
                    )
                    StatTile(
                        icon = Icons.Default.Route,
                        value = format.format(stats.trips),
                        label = stringResource(R.string.cloud_stats_trips),
                        accent = MaterialTheme.colorScheme.primary,
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = if (stats.isEmpty) {
                        stringResource(R.string.cloud_stats_empty)
                    } else {
                        stringResource(R.string.cloud_stats_total, format.format(stats.total))
                    },
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

/**
 * One figure. Takes an equal share of the row so the four numbers line up in
 * a grid rather than drifting with the width of their labels.
 */
@Composable
private fun RowScope.StatTile(
    icon: ImageVector,
    value: String,
    label: String,
    accent: Color,
) {
    Column(
        modifier = Modifier.weight(1f),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(accent.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = value,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(showBackground = true, widthDp = 380)
@Composable
private fun PreviewDatabaseStatsCard() {
    CarTrackingAppTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            DatabaseStatsCard(
                stats = DatabaseStats(cars = 3, refills = 80, expenses = 40, trips = 1_204)
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 380)
@Composable
private fun PreviewDatabaseStatsCardEmpty() {
    CarTrackingAppTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            DatabaseStatsCard(stats = DatabaseStats())
        }
    }
}
