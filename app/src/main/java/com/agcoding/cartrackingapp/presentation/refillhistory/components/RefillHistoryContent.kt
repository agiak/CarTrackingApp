package com.agcoding.cartrackingapp.presentation.refillhistory.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.agcoding.cartrackingapp.domain.model.FuelRefill
import com.agcoding.cartrackingapp.domain.model.Location
import com.agcoding.cartrackingapp.presentation.components.RefillItemCard
import com.agcoding.cartrackingapp.presentation.components.StyledCard
import com.agcoding.cartrackingapp.presentation.refillhistory.RefillSortOption
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme

@Composable
fun RefillHistoryContent(
    carName: String,
    refills: List<FuelRefill>,
    selectedSort: RefillSortOption,
    onRefillClick: (Long) -> Unit,
    onRefillLongClick: (Long) -> Unit = {},
    isSelectionMode: Boolean = false,
    selectedRefillIds: Set<Long> = emptySet(),
    refillTripNames: Map<Long, String> = emptyMap(),
    modifier: Modifier = Modifier
) {
    val isTablet = com.agcoding.cartrackingapp.util.DeviceUtils.isTablet()
    val isLandscape = com.agcoding.cartrackingapp.util.DeviceUtils.isLandscape()
    val useSplitView = isTablet || isLandscape

    if (useSplitView) {
        // Split view for tablets and landscape
        Row(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Left side: Summary stats (35%)
            Column(
                modifier = Modifier
                    .weight(0.35f)
                    .fillMaxHeight()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val totalCost = refills.sumOf { it.amountPaid }
                val totalLiters = refills.sumOf { it.litersAdded }
                val totalDistance = refills.sumOf { it.tripDistance }
                val avgConsumption = if (totalDistance > 0) (totalLiters / totalDistance) * 100 else 0.0

                RefillHistorySummaryCard(
                    carName = carName,
                    refillCount = refills.size,
                    totalCost = totalCost,
                    totalLiters = totalLiters,
                    totalDistance = totalDistance,
                    avgConsumption = avgConsumption
                )

                // Sort info
                StyledCard {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.sort),
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = stringResource(selectedSort.labelRes),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Right side: Refills list (65%)
            LazyColumn(
                modifier = Modifier
                    .weight(0.65f)
                    .fillMaxHeight(),
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(refills) { refill ->
                    RefillItemCard(
                        refill = refill,
                        carName = null,
                        onClick = { onRefillClick(refill.id) },
                        onLongClick = { onRefillLongClick(refill.id) },
                        isSelectionMode = isSelectionMode,
                        isSelected = refill.id in selectedRefillIds,
                        tripName = refillTripNames[refill.id]
                    )
                }
            }
        }
    } else {
        // Original single column layout for portrait phones
        LazyColumn(
            modifier = modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            item {
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = carName,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = stringResource(
                            R.string.refills_sorted_by_format,
                            refills.size,
                            stringResource(selectedSort.labelRes)
                        ),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Refills list
            items(refills) { refill ->
                RefillItemCard(
                    refill = refill,
                    carName = null,
                    onClick = { onRefillClick(refill.id) },
                    onLongClick = { onRefillLongClick(refill.id) },
                    isSelectionMode = isSelectionMode,
                    isSelected = refill.id in selectedRefillIds,
                    tripName = refillTripNames[refill.id]
                )
            }
        }
    }
}

// ============================================
// Preview Composables
// ============================================

@Preview(name = "Refill History - Phone", showBackground = true, widthDp = 380, heightDp = 800)
@Composable
private fun PreviewRefillHistoryContentPhone() {
    CarTrackingAppTheme(darkTheme = false) {
        RefillHistoryContent(
            carName = "Toyota Corolla",
            refills = listOf(
                FuelRefill(
                    id = 1,
                    carId = 1,
                    timestamp = System.currentTimeMillis(),
                    amountPaid = 65.50,
                    litersAdded = 42.5,
                    pricePerLiter = 1.54,
                    tripDistance = 580.0,
                    odometerReading = 12580.0,
                    fuelConsumption = 7.33,
                    location = Location(37.9838, 23.7275),
                    notes = "Regular refill"
                ),
                FuelRefill(
                    id = 2,
                    carId = 1,
                    timestamp = System.currentTimeMillis() - 7 * 24 * 60 * 60 * 1000L,
                    amountPaid = 72.30,
                    litersAdded = 45.0,
                    pricePerLiter = 1.61,
                    tripDistance = 620.0,
                    odometerReading = 12000.0,
                    fuelConsumption = 7.26,
                    location = null,
                    notes = null
                )
            ),
            selectedSort = RefillSortOption.MOST_RECENT,
            onRefillClick = {}
        )
    }
}

@Preview(name = "Refill History - Tablet Split View", showBackground = true, widthDp = 900, heightDp = 600)
@Composable
private fun PreviewRefillHistoryContentTablet() {
    CarTrackingAppTheme(darkTheme = false) {
        RefillHistoryContent(
            carName = "Honda Civic",
            refills = listOf(
                FuelRefill(
                    id = 1,
                    carId = 1,
                    timestamp = System.currentTimeMillis(),
                    amountPaid = 68.20,
                    litersAdded = 40.0,
                    pricePerLiter = 1.71,
                    tripDistance = 550.0,
                    odometerReading = 15550.0,
                    fuelConsumption = 7.27,
                    location = null,
                    notes = "Highway station"
                ),
                FuelRefill(
                    id = 2,
                    carId = 1,
                    timestamp = System.currentTimeMillis() - 5 * 24 * 60 * 60 * 1000L,
                    amountPaid = 55.00,
                    litersAdded = 35.0,
                    pricePerLiter = 1.57,
                    tripDistance = 480.0,
                    odometerReading = 15000.0,
                    fuelConsumption = 7.29,
                    location = null,
                    notes = null
                ),
                FuelRefill(
                    id = 3,
                    carId = 1,
                    timestamp = System.currentTimeMillis() - 12 * 24 * 60 * 60 * 1000L,
                    amountPaid = 62.50,
                    litersAdded = 38.5,
                    pricePerLiter = 1.62,
                    tripDistance = 520.0,
                    odometerReading = 14480.0,
                    fuelConsumption = 7.40,
                    location = null,
                    notes = null
                )
            ),
            selectedSort = RefillSortOption.MOST_RECENT,
            onRefillClick = {}
        )
    }
}

@Preview(name = "Refill History - Dark", showBackground = true, widthDp = 380, heightDp = 800)
@Composable
private fun PreviewRefillHistoryContentDark() {
    CarTrackingAppTheme(darkTheme = true) {
        RefillHistoryContent(
            carName = "BMW 320i",
            refills = listOf(
                FuelRefill(
                    id = 1,
                    carId = 1,
                    timestamp = System.currentTimeMillis(),
                    amountPaid = 75.80,
                    litersAdded = 48.0,
                    pricePerLiter = 1.58,
                    tripDistance = 600.0,
                    odometerReading = 18600.0,
                    fuelConsumption = 8.00,
                    location = Location(40.6401, 22.9444),
                    notes = "Premium fuel"
                )
            ),
            selectedSort = RefillSortOption.MOST_EXPENSIVE,
            onRefillClick = {}
        )
    }
}
