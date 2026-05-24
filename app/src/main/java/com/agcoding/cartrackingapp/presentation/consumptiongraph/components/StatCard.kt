package com.agcoding.cartrackingapp.presentation.consumptiongraph.components

import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agcoding.cartrackingapp.presentation.components.StyledCard
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme

@Composable
fun StatCard(
    label: String,
    value: String,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    modifier: Modifier = Modifier
) {
    StyledCard(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = valueColor
            )
        }
    }
}

// ============================================
// Preview Composables
// ============================================

@Preview(name = "Stat Card - Default", showBackground = true, widthDp = 150)
@Composable
private fun PreviewStatCard() {
    CarTrackingAppTheme(darkTheme = false) {
        StatCard(
            label = "Average",
            value = "6.5 L/100km"
        )
    }
}

@Preview(name = "Stat Card - Green Value", showBackground = true, widthDp = 150)
@Composable
private fun PreviewStatCardGreen() {
    CarTrackingAppTheme(darkTheme = false) {
        StatCard(
            label = "Best",
            value = "5.8 L/100km",
            valueColor = MaterialTheme.colorScheme.tertiary
        )
    }
}

@Preview(name = "Stat Card - Red Value", showBackground = true, widthDp = 150)
@Composable
private fun PreviewStatCardRed() {
    CarTrackingAppTheme(darkTheme = false) {
        StatCard(
            label = "Worst",
            value = "8.2 L/100km",
            valueColor = MaterialTheme.colorScheme.error
        )
    }
}

@Preview(name = "Stat Card - Dark", showBackground = true, widthDp = 150)
@Composable
private fun PreviewStatCardDark() {
    CarTrackingAppTheme(darkTheme = true) {
        StatCard(
            label = "Average",
            value = "7.1 L/100km"
        )
    }
}
