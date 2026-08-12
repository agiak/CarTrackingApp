package com.agcoding.cartrackingapp.presentation.refillhistory.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
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
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme

@Composable
fun SummaryItem(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = label,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = value,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

// ============================================
// Preview Composables
// ============================================

@Preview(name = "Summary Item - Cost", showBackground = true, widthDp = 300)
@Composable
private fun PreviewSummaryItem() {
    CarTrackingAppTheme(darkTheme = false) {
        SummaryItem(
            icon = Icons.Default.EuroSymbol,
            label = "Total Cost",
            value = "€1,250.50"
        )
    }
}

@Preview(name = "Summary Item - Dark", showBackground = true, widthDp = 300)
@Composable
private fun PreviewSummaryItemDark() {
    CarTrackingAppTheme(darkTheme = true) {
        SummaryItem(
            icon = Icons.Default.EuroSymbol,
            label = "Total Spending",
            value = "€2,840.75"
        )
    }
}
