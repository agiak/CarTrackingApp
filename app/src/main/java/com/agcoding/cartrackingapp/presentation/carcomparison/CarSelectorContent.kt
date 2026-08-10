package com.agcoding.cartrackingapp.presentation.carcomparison

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.domain.model.Car
import com.agcoding.cartrackingapp.util.formatNumber

/**
 * Car selector for two-car comparison mode
 */
@Composable
fun CarSelectorContent(viewModel: CarComparisonViewModel) {
    val availableCars by viewModel.availableCars.collectAsState()
    val selectedCar1 by viewModel.selectedCar1.collectAsState()
    val selectedCar2 by viewModel.selectedCar2.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(R.string.car_comparison_select_cars_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        // Car 1 selector
        CarDropdown(
            label = stringResource(R.string.car_comparison_car_1),
            cars = availableCars,
            selectedCar = selectedCar1,
            excludeCarId = selectedCar2?.id,
            onCarSelected = viewModel::selectCar1
        )

        // Car 2 selector
        CarDropdown(
            label = stringResource(R.string.car_comparison_car_2),
            cars = availableCars,
            selectedCar = selectedCar2,
            excludeCarId = selectedCar1?.id,
            onCarSelected = viewModel::selectCar2
        )

        if (selectedCar1 != null && selectedCar2 != null) {
            if (selectedCar1!!.id == selectedCar2!!.id) {
                Text(
                    text = stringResource(R.string.car_comparison_same_car_error),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CarDropdown(
    label: String,
    cars: List<Car>,
    selectedCar: Car?,
    excludeCarId: Long?,
    onCarSelected: (Car) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val availableCars = cars.filter { it.id != excludeCarId }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = selectedCar?.name ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true)
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            availableCars.forEach { car ->
                DropdownMenuItem(
                    text = { Text(car.name) },
                    onClick = {
                        onCarSelected(car)
                        expanded = false
                    }
                )
            }
        }
    }
}

/**
 * Format value helper
 */
fun formatMetricValue(value: Double?, suffix: String): String {
    return if (value != null) {
        "${value.formatNumber(2)} $suffix"
    } else {
        "N/A"
    }
}

/**
 * Format percentage
 */
fun formatPercentage(value: Double): String {
    return "${value.formatNumber(1)}%"
}

