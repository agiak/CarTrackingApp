package com.agcoding.cartrackingapp.presentation.costgraph.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
                .padding(16.dp)
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
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
            label = "Average Monthly Cost",
            value = "€450.00"
        )
    }
}

@Preview(name = "Stat Card - Green Value", showBackground = true, widthDp = 150)
@Composable
private fun PreviewStatCardGreen() {
    CarTrackingAppTheme(darkTheme = false) {
        StatCard(
            label = "Lowest Month",
            value = "€320.00",
            valueColor = Color(0xFF34C759)
        )
    }
}

@Preview(name = "Stat Card - Dark", showBackground = true, widthDp = 150)
@Composable
private fun PreviewStatCardDark() {
    CarTrackingAppTheme(darkTheme = true) {
        StatCard(
            label = "Total Expenses",
            value = "24"
        )
    }
}
