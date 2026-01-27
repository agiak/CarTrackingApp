package com.agcoding.cartrackingapp.presentation.editrefill.components

import androidx.compose.foundation.layout.Arrangement
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
import com.agcoding.cartrackingapp.presentation.components.StyledOutlinedTextField
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme

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
    modifier: Modifier = Modifier
) {
    if (isTablet) {
        // Two-column layout for Amount and Liters on tablets
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StyledOutlinedTextField(
                value = amountPaid,
                onValueChange = onAmountPaidChange,
                label = { Text(stringResource(R.string.amount_paid_eur)) },
                placeholder = { Text(stringResource(R.string.amount_paid_hint)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.weight(1f)
            )

            StyledOutlinedTextField(
                value = litersAdded,
                onValueChange = onLitersAddedChange,
                label = { Text(stringResource(R.string.liters_added)) },
                placeholder = { Text(stringResource(R.string.liters_added_hint)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Two-column for Trip Distance and Odometer
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StyledOutlinedTextField(
                value = tripDistance,
                onValueChange = onTripDistanceChange,
                label = { Text(stringResource(R.string.trip_distance_km)) },
                placeholder = { Text(stringResource(R.string.trip_distance_hint)) },
                supportingText = { Text(stringResource(R.string.trip_distance_supporting)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.weight(1f)
            )

            StyledOutlinedTextField(
                value = odometerReading,
                onValueChange = onOdometerReadingChange,
                label = { Text(stringResource(R.string.odometer_reading_km)) },
                placeholder = { Text(stringResource(R.string.odometer_reading_hint)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
        }
    } else {
        // Single column for phones
        StyledOutlinedTextField(
            value = amountPaid,
            onValueChange = onAmountPaidChange,
            label = { Text(stringResource(R.string.amount_paid_eur)) },
            placeholder = { Text(stringResource(R.string.amount_paid_hint)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = modifier
        )

        Spacer(modifier = Modifier.height(16.dp))

        StyledOutlinedTextField(
            value = litersAdded,
            onValueChange = onLitersAddedChange,
            label = { Text(stringResource(R.string.liters_added)) },
            placeholder = { Text(stringResource(R.string.liters_added_hint)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        StyledOutlinedTextField(
            value = tripDistance,
            onValueChange = onTripDistanceChange,
            label = { Text(stringResource(R.string.trip_distance_km)) },
            placeholder = { Text(stringResource(R.string.trip_distance_hint)) },
            supportingText = { Text(stringResource(R.string.trip_distance_supporting)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        StyledOutlinedTextField(
            value = odometerReading,
            onValueChange = onOdometerReadingChange,
            label = { Text(stringResource(R.string.odometer_reading_km)) },
            placeholder = { Text(stringResource(R.string.odometer_reading_hint)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

// ============================================
// Preview Composables
// ============================================

@Preview(name = "Refill Fields - Phone Empty", showBackground = true, widthDp = 380)
@Composable
private fun PreviewRefillFieldsPhone() {
    CarTrackingAppTheme(darkTheme = false) {
        var amount by remember { mutableStateOf("") }
        var liters by remember { mutableStateOf("") }
        var distance by remember { mutableStateOf("") }
        var odometer by remember { mutableStateOf("") }

        RefillFieldsSection(
            amountPaid = amount,
            onAmountPaidChange = { amount = it },
            litersAdded = liters,
            onLitersAddedChange = { liters = it },
            tripDistance = distance,
            onTripDistanceChange = { distance = it },
            odometerReading = odometer,
            onOdometerReadingChange = { odometer = it },
            isTablet = false,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}

@Preview(name = "Refill Fields - Phone Filled", showBackground = true, widthDp = 380)
@Composable
private fun PreviewRefillFieldsPhoneFilled() {
    CarTrackingAppTheme(darkTheme = false) {
        var amount by remember { mutableStateOf("65.50") }
        var liters by remember { mutableStateOf("42.5") }
        var distance by remember { mutableStateOf("580") }
        var odometer by remember { mutableStateOf("45280") }

        RefillFieldsSection(
            amountPaid = amount,
            onAmountPaidChange = { amount = it },
            litersAdded = liters,
            onLitersAddedChange = { liters = it },
            tripDistance = distance,
            onTripDistanceChange = { distance = it },
            odometerReading = odometer,
            onOdometerReadingChange = { odometer = it },
            isTablet = false,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}

@Preview(name = "Refill Fields - Tablet", showBackground = true, widthDp = 800)
@Composable
private fun PreviewRefillFieldsTablet() {
    CarTrackingAppTheme(darkTheme = false) {
        var amount by remember { mutableStateOf("72.30") }
        var liters by remember { mutableStateOf("48.2") }
        var distance by remember { mutableStateOf("650") }
        var odometer by remember { mutableStateOf("52100") }

        RefillFieldsSection(
            amountPaid = amount,
            onAmountPaidChange = { amount = it },
            litersAdded = liters,
            onLitersAddedChange = { liters = it },
            tripDistance = distance,
            onTripDistanceChange = { distance = it },
            odometerReading = odometer,
            onOdometerReadingChange = { odometer = it },
            isTablet = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        )
    }
}

@Preview(name = "Refill Fields - Dark", showBackground = true, widthDp = 380)
@Composable
private fun PreviewRefillFieldsDark() {
    CarTrackingAppTheme(darkTheme = true) {
        var amount by remember { mutableStateOf("58.90") }
        var liters by remember { mutableStateOf("38.7") }
        var distance by remember { mutableStateOf("520") }
        var odometer by remember { mutableStateOf("48900") }

        RefillFieldsSection(
            amountPaid = amount,
            onAmountPaidChange = { amount = it },
            litersAdded = liters,
            onLitersAddedChange = { liters = it },
            tripDistance = distance,
            onTripDistanceChange = { distance = it },
            odometerReading = odometer,
            onOdometerReadingChange = { odometer = it },
            isTablet = false,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}
