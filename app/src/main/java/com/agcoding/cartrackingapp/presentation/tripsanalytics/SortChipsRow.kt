package com.agcoding.cartrackingapp.presentation.tripsanalytics

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme

@Composable
internal fun SortChipsRow(
    activeSortOption: TripSortOption,
    onSortChange: (TripSortOption) -> Unit
) {
    val options = listOf(
        TripSortOption.MOST_RECENT to stringResource(R.string.trips_sort_most_recent),
        TripSortOption.MOST_COSTLY to stringResource(R.string.trips_sort_most_costly),
        TripSortOption.LEAST_COSTLY to stringResource(R.string.trips_sort_least_costly),
        TripSortOption.HIGHEST_FUEL_CONSUMPTION to stringResource(R.string.trips_sort_highest_consumption),
        TripSortOption.LOWEST_FUEL_CONSUMPTION to stringResource(R.string.trips_sort_lowest_consumption),
        TripSortOption.LONGEST_DISTANCE to stringResource(R.string.trips_sort_longest),
        TripSortOption.SHORTEST_DISTANCE to stringResource(R.string.trips_sort_shortest)
    )

    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        options.forEach { (option, label) ->
            FilterChip(
                selected = activeSortOption == option,
                onClick = { onSortChange(option) },
                label = {
                    Text(
                        text = label,
                        fontSize = 12.sp,
                        maxLines = 1
                    )
                },
                shape = RoundedCornerShape(20.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SortChipsRowPreview() {
    CarTrackingAppTheme {
        SortChipsRow(
            activeSortOption = TripSortOption.MOST_RECENT,
            onSortChange = {}
        )
    }
}
