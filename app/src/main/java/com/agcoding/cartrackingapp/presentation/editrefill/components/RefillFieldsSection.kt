package com.agcoding.cartrackingapp.presentation.editrefill.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.presentation.components.AppTextField
import com.agcoding.cartrackingapp.presentation.components.ThousandsSeparatorTransformation
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppColorScheme
import com.agcoding.cartrackingapp.util.parseLocalizedDouble
import kotlin.math.abs

/** Between two fields in the same group. */
private val FieldGap = 14.dp

/**
 * The four figures that describe a tank of fuel.
 *
 * @param previousOdometer the reading at the refill before this one. The
 *   odometer field uses it twice: at rest it says what the last reading was,
 *   which is the number you need in order to type a sensible one; and when the
 *   typed reading disagrees with previous + trip distance it says so.
 *
 *   That check matters more than it looks. A mistyped odometer does not break
 *   this refill — it breaks the *next* one's trip distance, and every
 *   consumption figure derived from it, silently and weeks later.
 */
@Composable
fun RefillFieldsSection(
    amountPaid: String,
    onAmountPaidChange: (String) -> Unit,
    litersAdded: String,
    onLitersAddedChange: (String) -> Unit,
    tripDistance: String,
    onTripDistanceChange: (String) -> Unit,
    odometerReading: String,
    onOdometerReadingChange: (String) -> Unit,
    isTablet: Boolean,
    modifier: Modifier = Modifier,
    previousOdometer: Double? = null,
    expectedOdometer: Double? = null
) {
    val scheme = LocalAppColorScheme.current

    // A kilometre either way is rounding, not a typo.
    val typed = odometerReading.parseLocalizedDouble()
    val mismatch = expectedOdometer != null && typed != null && typed > 0 &&
        abs(typed - expectedOdometer) >= 1.0

    val odometerHint: @Composable (() -> Unit)? = when {
        mismatch -> {
            {
                Text(
                    text = stringResource(
                        R.string.edit_refill_odometer_mismatch,
                        odometerText(expectedOdometer!!)
                    ),
                    color = scheme.statusWarning
                )
            }
        }

        previousOdometer != null -> {
            {
                Text(
                    stringResource(
                        R.string.edit_refill_previous_odometer,
                        odometerText(previousOdometer)
                    )
                )
            }
        }

        else -> null
    }

    val amount: @Composable () -> Unit = {
        AppTextField(
            value = amountPaid,
            onValueChange = onAmountPaidChange,
            label = { Text(stringResource(R.string.amount_paid_eur)) },
            placeholder = { Text(stringResource(R.string.amount_paid_hint)) },
            suffix = { Text("€") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            visualTransformation = ThousandsSeparatorTransformation(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
    }
    val liters: @Composable () -> Unit = {
        AppTextField(
            value = litersAdded,
            onValueChange = onLitersAddedChange,
            label = { Text(stringResource(R.string.liters_added)) },
            placeholder = { Text(stringResource(R.string.liters_added_hint)) },
            suffix = { Text("L") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            visualTransformation = ThousandsSeparatorTransformation(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
    }
    val distance: @Composable () -> Unit = {
        AppTextField(
            value = tripDistance,
            onValueChange = onTripDistanceChange,
            label = { Text(stringResource(R.string.trip_distance_km)) },
            placeholder = { Text(stringResource(R.string.trip_distance_hint)) },
            suffix = { Text("km") },
            supportingText = { Text(stringResource(R.string.trip_distance_supporting)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            visualTransformation = ThousandsSeparatorTransformation(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
    }
    val odometer: @Composable () -> Unit = {
        AppTextField(
            value = odometerReading,
            onValueChange = onOdometerReadingChange,
            label = { Text(stringResource(R.string.odometer_reading_km)) },
            placeholder = { Text(stringResource(R.string.odometer_reading_hint)) },
            suffix = { Text("km") },
            supportingText = odometerHint,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            visualTransformation = ThousandsSeparatorTransformation(),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
    }

    if (isTablet) {
        Column(modifier = modifier) {
            Row(horizontalArrangement = Arrangement.spacedBy(FieldGap)) {
                Column(Modifier.weight(1f)) { amount() }
                Column(Modifier.weight(1f)) { liters() }
            }
            Spacer(Modifier.height(FieldGap))
            Row(horizontalArrangement = Arrangement.spacedBy(FieldGap)) {
                Column(Modifier.weight(1f)) { distance() }
                Column(Modifier.weight(1f)) { odometer() }
            }
        }
    } else {
        Column(modifier = modifier) {
            amount()
            Spacer(Modifier.height(FieldGap))
            liters()
            Spacer(Modifier.height(FieldGap))
            distance()
            Spacer(Modifier.height(FieldGap))
            odometer()
        }
    }
}

// ============================================
// Preview Composables
// ============================================

@Preview(name = "Fields — odometer agrees", showBackground = true, widthDp = 390)
@Composable
private fun PreviewRefillFields() {
    CarTrackingAppTheme(darkTheme = true) {
        var odometer by remember { mutableStateOf("119625") }
        RefillFieldsSection(
            amountPaid = "70,43",
            onAmountPaidChange = {},
            litersAdded = "39,3",
            onLitersAddedChange = {},
            tripDistance = "641",
            onTripDistanceChange = {},
            odometerReading = odometer,
            onOdometerReadingChange = { odometer = it },
            previousOdometer = 118_984.0,
            expectedOdometer = 119_625.0,
            isTablet = false,
            modifier = Modifier.padding(18.dp)
        )
    }
}

@Preview(name = "Fields — odometer disagrees", showBackground = true, widthDp = 390)
@Composable
private fun PreviewRefillFieldsMismatch() {
    CarTrackingAppTheme(darkTheme = true) {
        RefillFieldsSection(
            amountPaid = "70,43",
            onAmountPaidChange = {},
            litersAdded = "39,3",
            onLitersAddedChange = {},
            tripDistance = "641",
            onTripDistanceChange = {},
            odometerReading = "119265",
            onOdometerReadingChange = {},
            previousOdometer = 118_984.0,
            expectedOdometer = 119_625.0,
            isTablet = false,
            modifier = Modifier.padding(18.dp)
        )
    }
}
