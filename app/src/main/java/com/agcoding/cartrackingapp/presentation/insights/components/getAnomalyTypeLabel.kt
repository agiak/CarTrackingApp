package com.agcoding.cartrackingapp.presentation.insights.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.domain.model.AnomalyType
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme

@Composable
internal fun getAnomalyTypeLabel(type: AnomalyType): String {
    return when (type) {
        AnomalyType.FUEL_PRICE_SPIKE -> stringResource(R.string.anomaly_type_fuel_price)
        AnomalyType.CONSUMPTION_SPIKE -> stringResource(R.string.anomaly_type_consumption)
        AnomalyType.MAINTENANCE_OUTLIER -> stringResource(R.string.anomaly_type_maintenance)
        AnomalyType.MONTHLY_SPENDING_INCREASE -> stringResource(R.string.anomaly_type_spending)
        AnomalyType.COST_PER_KM_DEVIATION -> stringResource(R.string.anomaly_type_cost_per_km)
        AnomalyType.MISSING_TRIP_REFILL -> stringResource(R.string.anomaly_type_missing_trip_refill)
    }
}

@Preview(showBackground = true)
@Composable
private fun getAnomalyTypeLabelPreview() {
    CarTrackingAppTheme {
        Text(text = getAnomalyTypeLabel(AnomalyType.entries.first()))
    }
}
