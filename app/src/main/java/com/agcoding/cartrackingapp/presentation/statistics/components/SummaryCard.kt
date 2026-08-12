package com.agcoding.cartrackingapp.presentation.statistics.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EuroSymbol
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
fun SummaryCard(
    icon: ImageVector,
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null
) {
    StyledCard(
        modifier = modifier,
        onClick = onClick
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
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = title,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ============================================
// Preview Composables
// ============================================

@Preview(name = "Summary Card - Simple", showBackground = true, widthDp = 180)
@Composable
private fun PreviewSummaryCard() {
    CarTrackingAppTheme(darkTheme = false) {
        SummaryCard(
            icon = Icons.Default.EuroSymbol,
            title = "Total Cost",
            value = "€1,250.50",
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(name = "Summary Card - With Subtitle", showBackground = true, widthDp = 180)
@Composable
private fun PreviewSummaryCardWithSubtitle() {
    CarTrackingAppTheme(darkTheme = false) {
        SummaryCard(
            icon = Icons.Default.EuroSymbol,
            title = "Total Cost",
            value = "€2,840.75",
            subtitle = "Fuel + Expenses",
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(name = "Summary Card - Dark", showBackground = true, widthDp = 180)
@Composable
private fun PreviewSummaryCardDark() {
    CarTrackingAppTheme(darkTheme = true) {
        SummaryCard(
            icon = Icons.Default.EuroSymbol,
            title = "Avg Consumption",
            value = "7.5 L/100km",
            modifier = Modifier.padding(16.dp)
        )
    }
}
