@file:OptIn(ExperimentalMaterial3Api::class)

package com.agcoding.cartrackingapp.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.domain.model.Car
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppColorScheme
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppDimens

/**
 * Which cars the statistics cover.
 *
 * Three things were wrong with it, and they were the same thing three times:
 * nothing told the user where they stood. A Material checkbox at the end of a
 * bare two-line row is a small target that reads as a form control rather than
 * as "this car is included"; there was no count, so with six cars you had to
 * tally the ticks yourself; and Apply stayed live with nothing selected, which
 * applies a filter that hides everything.
 *
 * So: each car is a row of the app's own grouped list with its icon tile and
 * plate badge, the whole row toggles, selection is an accent-filled check
 * rather than a checkbox, a line above the list says "2 of 3 selected" with a
 * select-all/clear-all beside it, and Apply goes quiet with a reason under it
 * when the selection is empty.
 */
@Composable
fun CarFilterSheet(
    cars: List<Car>,
    selectedCarIds: Set<Long>,
    onCarSelectionChanged: (Long, Boolean) -> Unit,
    onDismiss: () -> Unit,
    onApply: () -> Unit,
    onSelectAll: (() -> Unit)? = null,
    onClearAll: (() -> Unit)? = null
) {
    val scheme = LocalAppColorScheme.current
    val dimens = LocalAppDimens.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val allSelected = cars.isNotEmpty() && selectedCarIds.size == cars.size
    val none = selectedCarIds.isEmpty()

    AppBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp)
        ) {
            SheetHeader(
                title = stringResource(R.string.filter_by_car),
                subtitle = stringResource(R.string.select_cars_to_filter),
                onClose = onDismiss
            )

            Spacer(Modifier.height(18.dp))

            // Where you stand, and the one gesture that changes it wholesale.
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(
                        R.string.car_filter_selected_count,
                        selectedCarIds.size,
                        cars.size
                    ),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.labelLarge,
                    color = scheme.contentSecondary
                )
                val bulk = if (allSelected) onClearAll else onSelectAll
                if (bulk != null) {
                    Text(
                        text = if (allSelected) {
                            stringResource(R.string.clear_all)
                        } else {
                            stringResource(R.string.select_all)
                        },
                        style = MaterialTheme.typography.labelLarge,
                        color = scheme.actionPrimary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(dimens.radius.button))
                            .clickable(onClick = bulk)
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            // Capped rather than free: with a dozen cars the list would push
            // Apply off the bottom of the sheet, which is the one control that
            // must always be reachable.
            Column(
                modifier = Modifier
                    .heightIn(max = 360.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                GroupedListContainer {
                    cars.forEachIndexed { index, car ->
                        if (index > 0) GroupedDivider(startInset = false)
                        CarFilterRow(
                            car = car,
                            selected = car.id in selectedCarIds,
                            onToggle = {
                                onCarSelectionChanged(car.id, car.id !in selectedCarIds)
                            }
                        )
                    }
                }
            }

            if (none) {
                Spacer(Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.ErrorOutline,
                        contentDescription = null,
                        tint = scheme.statusError,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.car_filter_need_one),
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.statusError
                    )
                }
            }

            Spacer(Modifier.height(18.dp))
            PrimaryButton(
                label = stringResource(R.string.apply),
                onClick = onApply,
                // Applying an empty filter hides every figure on the screen
                // and leaves no clue why.
                enabled = !none
            )
        }
    }
}

@Composable
private fun CarFilterRow(car: Car, selected: Boolean, onToggle: () -> Unit) {
    val scheme = LocalAppColorScheme.current
    val dimens = LocalAppDimens.current

    val box by animateColorAsState(
        targetValue = if (selected) scheme.actionPrimary else scheme.backgroundSecondary,
        animationSpec = tween(140),
        label = "carFilterBox"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
            .padding(horizontal = 16.dp, vertical = 13.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(dimens.size.iconTile)
                .clip(RoundedCornerShape(dimens.radius.iconTile))
                .background(
                    if (selected) {
                        scheme.actionPrimary.copy(alpha = 0.16f)
                    } else {
                        scheme.backgroundSecondary
                    }
                )
        ) {
            Icon(
                imageVector = Icons.Default.DirectionsCar,
                contentDescription = null,
                tint = if (selected) scheme.actionPrimary else scheme.contentSecondary,
                modifier = Modifier.size(19.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = car.name,
                style = MaterialTheme.typography.titleSmall,
                color = scheme.contentPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (car.licensePlate.isNotBlank()) {
                Spacer(Modifier.height(5.dp))
                LicensePlateBadge(plate = car.licensePlate)
            }
        }
        Spacer(Modifier.width(10.dp))
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(23.dp)
                .clip(CircleShape)
                .background(box)
                .then(
                    if (selected) {
                        Modifier
                    } else {
                        Modifier.border(
                            BorderStroke(dimens.size.divider, scheme.borderDefault),
                            CircleShape
                        )
                    }
                )
        ) {
            if (selected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = scheme.actionContent,
                    modifier = Modifier.size(15.dp)
                )
            }
        }
    }
}

// ============================================
// Preview Composables
// ============================================

private val previewCars = listOf(
    Car(1, "Toyota Corolla", "ABC-1234", 0.0, 119_625.0),
    Car(2, "Honda Civic", "XYZ-5678", 0.0, 148_194.0),
    Car(3, "Volkswagen Golf", "VWG-9012", 0.0, 121_145.0)
)

@Preview(name = "Car filter — some selected", showBackground = true, widthDp = 390, heightDp = 640)
@Composable
private fun PreviewCarFilterSheet() {
    CarTrackingAppTheme(darkTheme = true) {
        CarFilterSheet(
            cars = previewCars,
            selectedCarIds = setOf(1, 3),
            onCarSelectionChanged = { _, _ -> },
            onDismiss = {},
            onApply = {},
            onSelectAll = {},
            onClearAll = {}
        )
    }
}

@Preview(name = "Car filter — none selected", showBackground = true, widthDp = 390, heightDp = 640)
@Composable
private fun PreviewCarFilterSheetEmpty() {
    CarTrackingAppTheme(darkTheme = true) {
        CarFilterSheet(
            cars = previewCars,
            selectedCarIds = emptySet(),
            onCarSelectionChanged = { _, _ -> },
            onDismiss = {},
            onApply = {},
            onSelectAll = {},
            onClearAll = {}
        )
    }
}

@Preview(name = "Car filter — light", showBackground = true, widthDp = 390, heightDp = 640)
@Composable
private fun PreviewCarFilterSheetLight() {
    CarTrackingAppTheme(darkTheme = false) {
        CarFilterSheet(
            cars = previewCars,
            selectedCarIds = previewCars.map { it.id }.toSet(),
            onCarSelectionChanged = { _, _ -> },
            onDismiss = {},
            onApply = {},
            onSelectAll = {},
            onClearAll = {}
        )
    }
}
