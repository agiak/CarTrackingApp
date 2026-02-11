package com.agcoding.cartrackingapp.presentation.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme

/**
 * A data class representing an active filter chip
 */
data class ActiveFilter(
    val id: String,
    val label: String,
    val onRemove: () -> Unit
)

/**
 * A horizontal scrollable row of filter chips showing currently active filters.
 * Each chip can be individually removed, providing immediate visual feedback
 * about what filters are applied to the current view.
 *
 * @param activeFilters List of currently active filters
 * @param onClearAll Callback to clear all filters at once (optional)
 * @param modifier Modifier for the component
 */
@Composable
fun ActiveFiltersRow(
    activeFilters: List<ActiveFilter>,
    onClearAll: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    if (activeFilters.isEmpty()) return

    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Individual filter chips
        activeFilters.forEach { filter ->
            FilterChip(
                selected = true,
                onClick = { /* No-op, removal is handled by trailing icon */ },
                label = {
                    Text(
                        text = filter.label,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                },
                trailingIcon = {
                    IconButton(
                        onClick = filter.onRemove,
                        modifier = Modifier.size(18.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.remove_filter),
                            modifier = Modifier.size(14.dp)
                        )
                    }
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }

        // Clear all button (if provided and more than one filter is active)
        if (onClearAll != null && activeFilters.size > 1) {
            FilterChip(
                selected = false,
                onClick = onClearAll,
                label = {
                    Text(
                        text = stringResource(R.string.clear_all),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

// ============================================
// Preview Composables
// ============================================

@Preview(name = "Active Filters - Multiple", showBackground = true)
@Composable
private fun PreviewActiveFiltersMultiple() {
    CarTrackingAppTheme(darkTheme = false) {
        ActiveFiltersRow(
            activeFilters = listOf(
                ActiveFilter("refills", "Refills") { },
                ActiveFilter("car_honda", "Honda Civic") { },
                ActiveFilter("car_toyota", "Toyota Corolla") { }
            ),
            onClearAll = { }
        )
    }
}

@Preview(name = "Active Filters - Single", showBackground = true)
@Composable
private fun PreviewActiveFiltersSingle() {
    CarTrackingAppTheme(darkTheme = false) {
        ActiveFiltersRow(
            activeFilters = listOf(
                ActiveFilter("expenses", "Expenses") { }
            )
        )
    }
}

@Preview(name = "Active Filters - Empty", showBackground = true)
@Composable
private fun PreviewActiveFiltersEmpty() {
    CarTrackingAppTheme(darkTheme = false) {
        ActiveFiltersRow(
            activeFilters = emptyList()
        )
    }
}

@Preview(name = "Active Filters - Dark Theme", showBackground = true)
@Composable
private fun PreviewActiveFiltersDark() {
    CarTrackingAppTheme(darkTheme = true) {
        ActiveFiltersRow(
            activeFilters = listOf(
                ActiveFilter("refills", "Refills") { },
                ActiveFilter("service", "Service") { },
                ActiveFilter("car_bmw", "BMW X5") { }
            ),
            onClearAll = { }
        )
    }
}
