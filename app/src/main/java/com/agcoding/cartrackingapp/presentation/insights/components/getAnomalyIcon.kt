package com.agcoding.cartrackingapp.presentation.insights.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import com.agcoding.cartrackingapp.domain.model.AnomalyType
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme

@Composable
internal fun getAnomalyIcon(type: AnomalyType): ImageVector {
    return when (type) {
        AnomalyType.FUEL_PRICE_SPIKE -> Icons.AutoMirrored.Filled.TrendingUp
        AnomalyType.CONSUMPTION_SPIKE -> Icons.Default.Speed
        AnomalyType.MAINTENANCE_OUTLIER -> Icons.Default.Build
        AnomalyType.MONTHLY_SPENDING_INCREASE -> Icons.Default.AccountBalanceWallet
        AnomalyType.COST_PER_KM_DEVIATION -> Icons.Default.Payments
        AnomalyType.MISSING_TRIP_REFILL -> Icons.Default.Flag
    }
}

@Preview(showBackground = true)
@Composable
private fun getAnomalyIconPreview() {
    CarTrackingAppTheme {
        Icon(
            imageVector = getAnomalyIcon(AnomalyType.entries.first()),
            contentDescription = null
        )
    }
}
