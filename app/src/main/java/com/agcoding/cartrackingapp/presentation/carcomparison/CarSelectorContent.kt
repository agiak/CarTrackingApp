package com.agcoding.cartrackingapp.presentation.carcomparison

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.domain.model.Car
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppColorScheme
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppDimens
import com.agcoding.cartrackingapp.util.formatNumber

/**
 * Picking the two cars, before there is anything to compare.
 *
 * The same two boxes the comparison itself uses, stacked instead of side by
 * side because here they are the whole screen rather than a header. Keeping
 * the shape identical means the screen does not visibly change when the
 * second car is chosen and the tables appear — the pickers stay where the
 * thumb left them.
 */
@Composable
fun CarSelectorContent(viewModel: CarComparisonViewModel) {
    val scheme = LocalAppColorScheme.current
    val dimens = LocalAppDimens.current

    val availableCars by viewModel.availableCars.collectAsState()
    val selectedCar1 by viewModel.selectedCar1.collectAsState()
    val selectedCar2 by viewModel.selectedCar2.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = dimens.spacing.screenHorizontal)
            .padding(top = 8.dp, bottom = 28.dp)
    ) {
        Text(
            text = stringResource(R.string.car_comparison_select_cars_title),
            style = MaterialTheme.typography.titleMedium,
            color = scheme.contentPrimary
        )
        Spacer(modifier = Modifier.height(16.dp))

        CarPicker(
            label = stringResource(R.string.car_comparison_car_1),
            cars = availableCars,
            selectedCar = selectedCar1,
            excludeCarId = selectedCar2?.id,
            dotColor = scheme.actionPrimary,
            onCarSelected = viewModel::selectCar1
        )
        Spacer(modifier = Modifier.height(12.dp))
        CarPicker(
            label = stringResource(R.string.car_comparison_car_2),
            cars = availableCars,
            selectedCar = selectedCar2,
            excludeCarId = selectedCar1?.id,
            dotColor = scheme.contentPrimary,
            onCarSelected = viewModel::selectCar2
        )

        if (selectedCar1 != null && selectedCar2 != null &&
            selectedCar1?.id == selectedCar2?.id
        ) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.car_comparison_same_car_error),
                style = MaterialTheme.typography.bodySmall,
                color = scheme.statusError
            )
        }
    }
}

/**
 * One car slot.
 *
 * The odometer under the name is there because two cars in a household are
 * often the same make and a name like "Golf" and "Golf II" is easy to mix up;
 * the reading is the thing that tells them apart at a glance.
 */
@Composable
private fun CarPicker(
    label: String,
    cars: List<Car>,
    selectedCar: Car?,
    excludeCarId: Long?,
    dotColor: Color,
    onCarSelected: (Car) -> Unit
) {
    val scheme = LocalAppColorScheme.current
    val dimens = LocalAppDimens.current
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(dimens.radius.card))
                .background(scheme.backgroundCard)
                .clickable { expanded = true }
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelMedium,
                    color = scheme.contentSecondary,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = selectedCar?.name
                        ?: stringResource(R.string.car_comparison_select_car),
                    style = MaterialTheme.typography.titleMedium,
                    color = if (selectedCar != null) {
                        scheme.contentPrimary
                    } else {
                        scheme.contentDisabled
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = null,
                tint = scheme.contentSecondary,
                modifier = Modifier.size(22.dp)
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            cars.filter { it.id != excludeCarId }.forEach { car ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(
                                text = car.name,
                                style = MaterialTheme.typography.titleSmall,
                                color = scheme.contentPrimary
                            )
                            Text(
                                text = stringResource(
                                    R.string.car_card_km_format,
                                    car.currentOdometer.toLong().formatNumber()
                                ),
                                style = MaterialTheme.typography.labelSmall,
                                color = scheme.contentSecondary
                            )
                        }
                    },
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
 * A percentage difference, as the insight sentences print it.
 *
 * Its companion `formatMetricValue` went with the cards that used it: the
 * tables format each metric in its own unit rather than appending a suffix to
 * two decimal places, which printed cost per kilometre as "0,15 €/km" and lost
 * the digit the comparison turns on.
 */
fun formatPercentage(value: Double): String {
    return "${value.formatNumber(1)}%"
}

@Preview(name = "Car pickers", showBackground = true, widthDp = 400)
@Composable
private fun PreviewCarPicker() {
    CarTrackingAppTheme(darkTheme = true) {
        val scheme = LocalAppColorScheme.current
        Column(modifier = Modifier.padding(18.dp)) {
            CarPicker(
                label = "Car 1",
                cars = emptyList(),
                selectedCar = Car(
                    id = 1,
                    name = "Corolla",
                    licensePlate = "ABC-1234",
                    currentOdometer = 120_000.0,
                    initialOdometer = 0.0
                ),
                excludeCarId = null,
                dotColor = scheme.actionPrimary,
                onCarSelected = {}
            )
            Spacer(modifier = Modifier.height(12.dp))
            CarPicker(
                label = "Car 2",
                cars = emptyList(),
                selectedCar = null,
                excludeCarId = null,
                dotColor = scheme.contentPrimary,
                onCarSelected = {}
            )
        }
    }
}
