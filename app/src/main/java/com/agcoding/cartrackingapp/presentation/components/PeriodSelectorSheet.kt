package com.agcoding.cartrackingapp.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agcoding.cartrackingapp.domain.model.TrendPeriod
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme

/**
 * Shared period selector bottom sheet with radio buttons
 * Used in both DistanceGraphScreen and ConsumptionGraphScreen for consistent UI
 */
@Composable
fun PeriodSelectorSheet(
    title: String,
    selectedPeriod: TrendPeriod,
    onPeriodSelected: (TrendPeriod) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp)
    ) {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            color = MaterialTheme.colorScheme.onSurface
        )

        TrendPeriod.entries.filter { it != TrendPeriod.CUSTOM }.forEach { period ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = period == selectedPeriod,
                    onClick = { onPeriodSelected(period) }
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = stringResource(period.labelResId),
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

// ============================================
// Preview Composables
// ============================================

@Preview(name = "Period Selector - 30 Days", showBackground = true, widthDp = 380)
@Composable
private fun PreviewPeriodSelectorSheet() {
    CarTrackingAppTheme(darkTheme = false) {
        PeriodSelectorSheet(
            title = "Select Period",
            selectedPeriod = TrendPeriod.LAST_30_DAYS,
            onPeriodSelected = {}
        )
    }
}

@Preview(name = "Period Selector - 90 Days", showBackground = true, widthDp = 380)
@Composable
private fun PreviewPeriodSelectorSheetMonth() {
    CarTrackingAppTheme(darkTheme = false) {
        PeriodSelectorSheet(
            title = "Select Period",
            selectedPeriod = TrendPeriod.LAST_90_DAYS,
            onPeriodSelected = {}
        )
    }
}

@Preview(name = "Period Selector - Dark Mode", showBackground = true, widthDp = 380)
@Composable
private fun PreviewPeriodSelectorSheetDark() {
    CarTrackingAppTheme(darkTheme = true) {
        PeriodSelectorSheet(
            title = "Select Period",
            selectedPeriod = TrendPeriod.LAST_YEAR,
            onPeriodSelected = {}
        )
    }
}

