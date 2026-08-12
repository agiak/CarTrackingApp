package com.agcoding.cartrackingapp.presentation.insights.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.domain.model.AnomalySeverity
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme

@Composable
internal fun getSeverityLabel(severity: AnomalySeverity): String {
    return when (severity) {
        AnomalySeverity.LOW -> stringResource(R.string.severity_low)
        AnomalySeverity.MEDIUM -> stringResource(R.string.severity_medium)
        AnomalySeverity.HIGH -> stringResource(R.string.severity_high)
    }
}

@Preview(showBackground = true)
@Composable
private fun getSeverityLabelPreview() {
    CarTrackingAppTheme {
        Text(text = getSeverityLabel(AnomalySeverity.entries.first()))
    }
}
