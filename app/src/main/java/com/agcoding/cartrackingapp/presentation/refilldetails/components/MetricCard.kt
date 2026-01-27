package com.agcoding.cartrackingapp.presentation.refilldetails.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agcoding.cartrackingapp.presentation.components.StyledCard
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme

@Composable
fun MetricCard(
    icon: ImageVector,
    label: String,
    value: String,
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
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = label,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

// ============================================
// Preview Composables
// ============================================

@Preview(name = "Metric Card - Fuel Volume", showBackground = true, widthDp = 180)
@Composable
private fun PreviewMetricCardFuel() {
    CarTrackingAppTheme(darkTheme = false) {
        MetricCard(
            icon = Icons.Default.LocalGasStation,
            label = "Fuel Volume",
            value = "42.5 L",
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(name = "Metric Card - Price", showBackground = true, widthDp = 180)
@Composable
private fun PreviewMetricCardPrice() {
    CarTrackingAppTheme(darkTheme = false) {
        MetricCard(
            icon = Icons.Default.LocalGasStation,
            label = "Price per Liter",
            value = "€1.54",
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(name = "Metric Card - Dark", showBackground = true, widthDp = 180)
@Composable
private fun PreviewMetricCardDark() {
    CarTrackingAppTheme(darkTheme = true) {
        MetricCard(
            icon = Icons.Default.LocalGasStation,
            label = "Trip Distance",
            value = "580 km",
            modifier = Modifier.padding(16.dp)
        )
    }
}
