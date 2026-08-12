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
internal fun getSeverityColor(severity: AnomalySeverity): Color {
    return when (severity) {
        AnomalySeverity.LOW -> MaterialTheme.colorScheme.tertiary
        AnomalySeverity.MEDIUM -> MaterialTheme.colorScheme.secondary
        AnomalySeverity.HIGH -> MaterialTheme.colorScheme.error
    }
}

@Preview(showBackground = true)
@Composable
private fun getSeverityColorPreview() {
    CarTrackingAppTheme {
        Box(
            modifier = Modifier
                .size(24.dp)
                .background(getSeverityColor(AnomalySeverity.entries.first()))
        )
    }
}
