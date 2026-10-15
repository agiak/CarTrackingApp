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
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppColorScheme
import com.agcoding.cartrackingapp.shared.ui.tokens.categoryService

@Composable
internal fun getSeverityColor(severity: AnomalySeverity): Color {
    val scheme = LocalAppColorScheme.current
    // The same three meanings the charts use: the palette's rose for the
    // loudest, the accent for the middle, and plain text colour for the
    // quietest. A "low" insight is information, not a warning, so it gets no
    // colour of its own at all.
    return when (severity) {
        AnomalySeverity.LOW -> scheme.contentSecondary
        AnomalySeverity.MEDIUM -> scheme.actionPrimary
        AnomalySeverity.HIGH -> scheme.categoryService
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
