package com.agcoding.cartrackingapp.presentation.insights.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.agcoding.cartrackingapp.domain.model.AnomalySeverity
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme

@Composable
internal fun getSeverityBackgroundColor(@Suppress("UNUSED_PARAMETER") severity: AnomalySeverity): Color {
    // Every card is the ordinary card surface now, whatever its severity.
    // Tinting the whole card turned a screenful of insights into coloured
    // stripes, which made the list harder to read while telling the user
    // something the badge on each card already says precisely.
    return MaterialTheme.colorScheme.surface
}

@Preview(showBackground = true)
@Composable
private fun getSeverityBackgroundColorPreview() {
    CarTrackingAppTheme {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(getSeverityBackgroundColor(AnomalySeverity.entries.first()))
        )
    }
}
