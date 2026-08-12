package com.agcoding.cartrackingapp.presentation.insights.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.agcoding.cartrackingapp.domain.model.AnomalySeverity
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme

@Composable
internal fun SeverityBadge(severity: AnomalySeverity) {
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

@Preview(showBackground = true)
@Composable
private fun SeverityBadgePreview() {
    CarTrackingAppTheme {
        SeverityBadge(severity = AnomalySeverity.entries.first())
    }
}
