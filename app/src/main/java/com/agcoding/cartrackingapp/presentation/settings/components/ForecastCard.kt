package com.agcoding.cartrackingapp.presentation.settings.components
import androidx.compose.ui.tooling.preview.Preview
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.presentation.components.StyledCard

/**
 * Settings card for Fuel Forecasting feature.
 *
 * Allows users to:
 * - Enable/disable fuel forecasting
 * - View description of the feature
 */
@Composable
fun ForecastCard(
    forecastingEnabled: Boolean,
    onForecastingToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    StyledCard(
        modifier = modifier.fillMaxWidth(),
        tintAlpha = 0.3f
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Forecasting Toggle Row
            SettingsRow(
                icon = Icons.Default.AutoGraph,
                iconBackgroundColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                iconTint = MaterialTheme.colorScheme.secondary,
                title = stringResource(R.string.forecasting_enabled),
                subtitle = stringResource(R.string.forecasting_description),
                trailing = {
                    Switch(
                        checked = forecastingEnabled,
                        onCheckedChange = onForecastingToggle,
                        colors = SwitchDefaults.colors(
                            checkedTrackColor = MaterialTheme.colorScheme.secondary
                        )
                    )
                }
            )

            // The row's own subtitle already carries forecasting_description.
            // It used to be repeated here as "helper text" whenever the switch
            // was on, so turning the feature on printed the same sentence
            // twice, one under the other.
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ForecastCardPreview() {
    CarTrackingAppTheme {
        ForecastCard(forecastingEnabled = true, onForecastingToggle = {})
    }
}
