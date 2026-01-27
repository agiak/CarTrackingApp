package com.agcoding.cartrackingapp.presentation.consumptiongraph.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingDown
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.domain.model.ConsumptionTrend
import com.agcoding.cartrackingapp.presentation.components.StyledCard
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme

@Composable
fun TrendCard(
    label: String,
    trend: ConsumptionTrend,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val (icon, color, text) = when (trend) {
        ConsumptionTrend.IMPROVING -> Triple(
            Icons.AutoMirrored.Filled.TrendingDown,
            Color(0xFF34C759),
            context.getString(R.string.trend_improving)
        )
        ConsumptionTrend.WORSENING -> Triple(
            Icons.AutoMirrored.Filled.TrendingUp,
            Color(0xFFFF3B30),
            context.getString(R.string.trend_worsening)
        )
        ConsumptionTrend.STABLE -> Triple(
            Icons.Default.Remove,
            MaterialTheme.colorScheme.onSurface,
            context.getString(R.string.trend_stable)
        )
    }

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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    text = text,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = color
                )
            }
        }
    }
}

// ============================================
// Preview Composables
// ============================================

@Preview(name = "Trend Card - Improving", showBackground = true, widthDp = 150)
@Composable
private fun PreviewTrendCardImproving() {
    CarTrackingAppTheme(darkTheme = false) {
        TrendCard(
            label = "Trend",
            trend = ConsumptionTrend.IMPROVING
        )
    }
}

@Preview(name = "Trend Card - Worsening", showBackground = true, widthDp = 150)
@Composable
private fun PreviewTrendCardWorsening() {
    CarTrackingAppTheme(darkTheme = false) {
        TrendCard(
            label = "Trend",
            trend = ConsumptionTrend.WORSENING
        )
    }
}

@Preview(name = "Trend Card - Stable", showBackground = true, widthDp = 150)
@Composable
private fun PreviewTrendCardStable() {
    CarTrackingAppTheme(darkTheme = false) {
        TrendCard(
            label = "Trend",
            trend = ConsumptionTrend.STABLE
        )
    }
}

@Preview(name = "Trend Card - Dark", showBackground = true, widthDp = 150)
@Composable
private fun PreviewTrendCardDark() {
    CarTrackingAppTheme(darkTheme = true) {
        TrendCard(
            label = "Trend",
            trend = ConsumptionTrend.IMPROVING
        )
    }
}
