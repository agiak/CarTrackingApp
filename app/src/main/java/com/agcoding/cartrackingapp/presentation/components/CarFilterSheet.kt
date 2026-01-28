package com.agcoding.cartrackingapp.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.domain.model.Car
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarFilterSheet(
    cars: List<Car>,
    selectedCarIds: Set<Long>,
    onCarSelectionChanged: (Long, Boolean) -> Unit,
    onDismiss: () -> Unit,
    onApply: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.filter_by_car),
                    style = MaterialTheme.typography.headlineSmall
                )
                TextButton(onClick = onApply) {
                    Text(stringResource(R.string.apply))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Description
            Text(
                text = stringResource(R.string.select_cars_to_filter),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Car list
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                cars.forEach { car ->
                    val isSelected = selectedCarIds.contains(car.id)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onCarSelectionChanged(car.id, !isSelected)
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = car.name,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = car.licensePlate,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = { checked ->
                                onCarSelectionChanged(car.id, checked)
                            }
                        )
                    }
                }
            }
        }
    }
}

// ============================================
// Preview Composables
// ============================================

@Preview(name = "Car Filter Sheet - Multiple Cars", showBackground = true, widthDp = 380)
@Composable
private fun PreviewCarFilterSheet() {
    CarTrackingAppTheme(darkTheme = false) {
        CarFilterSheet(
            cars = listOf(
                Car(1, "Toyota Corolla", "ABC-1234", 0.0, 12500.0),
                Car(2, "Honda Civic", "XYZ-5678", 0.0, 8000.0),
                Car(3, "BMW 320i", "BMW-999", 0.0, 15000.0)
            ),
            selectedCarIds = setOf(1, 3),
            onCarSelectionChanged = { _, _ -> },
            onDismiss = {},
            onApply = {}
        )
    }
}

@Preview(name = "Car Filter Sheet - All Selected", showBackground = true, widthDp = 380)
@Composable
private fun PreviewCarFilterSheetAllSelected() {
    CarTrackingAppTheme(darkTheme = false) {
        CarFilterSheet(
            cars = listOf(
                Car(1, "Toyota Corolla", "ABC-1234", 0.0, 12500.0),
                Car(2, "Honda Civic", "XYZ-5678", 0.0, 8000.0)
            ),
            selectedCarIds = setOf(1, 2),
            onCarSelectionChanged = { _, _ -> },
            onDismiss = {},
            onApply = {}
        )
    }
}

@Preview(name = "Car Filter Sheet - Dark Mode", showBackground = true, widthDp = 380)
@Composable
private fun PreviewCarFilterSheetDark() {
    CarTrackingAppTheme(darkTheme = true) {
        CarFilterSheet(
            cars = listOf(
                Car(1, "Volkswagen Golf", "VW-111", 0.0, 9500.0),
                Car(2, "Mazda 3", "MAZ-222", 0.0, 11000.0)
            ),
            selectedCarIds = setOf(2),
            onCarSelectionChanged = { _, _ -> },
            onDismiss = {},
            onApply = {}
        )
    }
}
