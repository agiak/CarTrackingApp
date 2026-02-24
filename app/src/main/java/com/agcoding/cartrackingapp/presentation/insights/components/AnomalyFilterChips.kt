package com.agcoding.cartrackingapp.presentation.insights.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.domain.model.AnomalyType

/**
 * Filter chips for anomaly types.
 * Allows users to filter the anomaly list by specific type.
 */
@Composable
fun AnomalyFilterChips(
    selectedFilter: AnomalyType?,
    onFilterSelected: (AnomalyType?) -> Unit,
    onClearFilter: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = stringResource(R.string.insights_filter_by_type),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // "All" chip
            FilterChip(
                selected = selectedFilter == null,
                onClick = onClearFilter,
                label = { Text(stringResource(R.string.filter_all)) }
            )

            // Anomaly type chips
            AnomalyType.entries.forEach { type ->
                FilterChip(
                    selected = selectedFilter == type,
                    onClick = { onFilterSelected(type) },
                    label = { Text(getAnomalyTypeLabel(type)) },
                    trailingIcon = if (selectedFilter == type) {
                        {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(R.string.clear_filter),
                                modifier = Modifier.size(FilterChipDefaults.IconSize)
                            )
                        }
                    } else null
                )
            }
        }
    }
}

@Composable
private fun getAnomalyTypeLabel(type: AnomalyType): String {
    return when (type) {
        AnomalyType.FUEL_PRICE_SPIKE -> stringResource(R.string.anomaly_type_fuel_price)
        AnomalyType.CONSUMPTION_SPIKE -> stringResource(R.string.anomaly_type_consumption)
        AnomalyType.MAINTENANCE_OUTLIER -> stringResource(R.string.anomaly_type_maintenance)
        AnomalyType.MONTHLY_SPENDING_INCREASE -> stringResource(R.string.anomaly_type_spending)
        AnomalyType.COST_PER_KM_DEVIATION -> stringResource(R.string.anomaly_type_cost_per_km)
        AnomalyType.MISSING_TRIP_REFILL -> stringResource(R.string.anomaly_type_missing_trip_refill)
    }
}

