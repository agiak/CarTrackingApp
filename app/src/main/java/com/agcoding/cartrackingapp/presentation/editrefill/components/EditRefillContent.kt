package com.agcoding.cartrackingapp.presentation.editrefill.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme

@Composable
fun EditRefillContent(
    amountPaid: String,
    onAmountPaidChange: (String) -> Unit,
    litersAdded: String,
    onLitersAddedChange: (String) -> Unit,
    tripDistance: String,
    onTripDistanceChange: (String) -> Unit,
    odometerReading: String,
    onOdometerReadingChange: (String) -> Unit,
    selectedDateMillis: Long,
    onShowDatePicker: () -> Unit,
    notes: String,
    onNotesChange: (String) -> Unit,
    hasLocation: Boolean,
    onRefreshLocation: () -> Unit,
    locationName: String = "",
    onLocationNameChange: (String) -> Unit = {},
    isLoadingLocationName: Boolean = false,
    isSaving: Boolean,
    onSaveClick: () -> Unit,
    errorMessage: String?,
    isTablet: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        RefillFieldsSection(
            amountPaid = amountPaid,
            onAmountPaidChange = onAmountPaidChange,
            litersAdded = litersAdded,
            onLitersAddedChange = onLitersAddedChange,
            tripDistance = tripDistance,
            onTripDistanceChange = onTripDistanceChange,
            odometerReading = odometerReading,
            onOdometerReadingChange = onOdometerReadingChange,
            isTablet = isTablet,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        RefillDateField(
            selectedDateMillis = selectedDateMillis,
            onShowDatePicker = onShowDatePicker,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        RefillNotesField(
            notes = notes,
            onNotesChange = onNotesChange,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        RefillLocationCard(
            locationName = locationName,
            onLocationNameChange = onLocationNameChange,
            hasLocation = hasLocation,
            onRefreshLocation = onRefreshLocation,
            isLoading = isLoadingLocationName,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        CalculatedValuesCard(
            amountPaid = amountPaid,
            litersAdded = litersAdded,
            tripDistance = tripDistance,
            modifier = Modifier.fillMaxWidth()
        )

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        SaveRefillButton(
            isSaving = isSaving,
            isEnabled = amountPaid.isNotBlank() &&
                    litersAdded.isNotBlank() &&
                    tripDistance.isNotBlank() &&
                    odometerReading.isNotBlank(),
            onSaveClick = onSaveClick,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// ============================================
// Preview Composables
// ============================================

@Preview(name = "Edit Refill Content - Phone Empty", showBackground = true, widthDp = 380)
@Composable
private fun PreviewEditRefillContentPhoneEmpty() {
    CarTrackingAppTheme(darkTheme = false) {
        var amount by remember { mutableStateOf("") }
        var liters by remember { mutableStateOf("") }
        var distance by remember { mutableStateOf("") }
        var odometer by remember { mutableStateOf("") }
        var notes by remember { mutableStateOf("") }

        EditRefillContent(
            amountPaid = amount,
            onAmountPaidChange = { amount = it },
            litersAdded = liters,
            onLitersAddedChange = { liters = it },
            tripDistance = distance,
            onTripDistanceChange = { distance = it },
            odometerReading = odometer,
            onOdometerReadingChange = { odometer = it },
            selectedDateMillis = System.currentTimeMillis(),
            onShowDatePicker = {},
            notes = notes,
            onNotesChange = { notes = it },
            hasLocation = false,
            onRefreshLocation = {},
            isSaving = false,
            onSaveClick = {},
            errorMessage = null,
            isTablet = false,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

@Preview(name = "Edit Refill Content - Phone Filled", showBackground = true, widthDp = 380)
@Composable
private fun PreviewEditRefillContentPhoneFilled() {
    CarTrackingAppTheme(darkTheme = false) {
        var amount by remember { mutableStateOf("65.50") }
        var liters by remember { mutableStateOf("42.5") }
        var distance by remember { mutableStateOf("580") }
        var odometer by remember { mutableStateOf("45280") }
        var notes by remember { mutableStateOf("Shell station on highway") }

        EditRefillContent(
            amountPaid = amount,
            onAmountPaidChange = { amount = it },
            litersAdded = liters,
            onLitersAddedChange = { liters = it },
            tripDistance = distance,
            onTripDistanceChange = { distance = it },
            odometerReading = odometer,
            onOdometerReadingChange = { odometer = it },
            selectedDateMillis = System.currentTimeMillis() - 2 * 24 * 60 * 60 * 1000L,
            onShowDatePicker = {},
            notes = notes,
            onNotesChange = { notes = it },
            hasLocation = true,
            onRefreshLocation = {},
            isSaving = false,
            onSaveClick = {},
            errorMessage = null,
            isTablet = false,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

@Preview(name = "Edit Refill Content - Tablet", showBackground = true, widthDp = 800)
@Composable
private fun PreviewEditRefillContentTablet() {
    CarTrackingAppTheme(darkTheme = false) {
        var amount by remember { mutableStateOf("72.30") }
        var liters by remember { mutableStateOf("48.2") }
        var distance by remember { mutableStateOf("650") }
        var odometer by remember { mutableStateOf("52100") }
        var notes by remember { mutableStateOf("BP station, full tank") }

        EditRefillContent(
            amountPaid = amount,
            onAmountPaidChange = { amount = it },
            litersAdded = liters,
            onLitersAddedChange = { liters = it },
            tripDistance = distance,
            onTripDistanceChange = { distance = it },
            odometerReading = odometer,
            onOdometerReadingChange = { odometer = it },
            selectedDateMillis = System.currentTimeMillis(),
            onShowDatePicker = {},
            notes = notes,
            onNotesChange = { notes = it },
            hasLocation = true,
            onRefreshLocation = {},
            isSaving = false,
            onSaveClick = {},
            errorMessage = null,
            isTablet = true,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}

@Preview(name = "Edit Refill Content - Saving", showBackground = true, widthDp = 380)
@Composable
private fun PreviewEditRefillContentSaving() {
    CarTrackingAppTheme(darkTheme = false) {
        var amount by remember { mutableStateOf("58.90") }
        var liters by remember { mutableStateOf("38.7") }
        var distance by remember { mutableStateOf("520") }
        var odometer by remember { mutableStateOf("48900") }
        var notes by remember { mutableStateOf("") }

        EditRefillContent(
            amountPaid = amount,
            onAmountPaidChange = { amount = it },
            litersAdded = liters,
            onLitersAddedChange = { liters = it },
            tripDistance = distance,
            onTripDistanceChange = { distance = it },
            odometerReading = odometer,
            onOdometerReadingChange = { odometer = it },
            selectedDateMillis = System.currentTimeMillis(),
            onShowDatePicker = {},
            notes = notes,
            onNotesChange = { notes = it },
            hasLocation = true,
            onRefreshLocation = {},
            isSaving = true,
            onSaveClick = {},
            errorMessage = null,
            isTablet = false,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

@Preview(name = "Edit Refill Content - With Error", showBackground = true, widthDp = 380)
@Composable
private fun PreviewEditRefillContentWithError() {
    CarTrackingAppTheme(darkTheme = false) {
        var amount by remember { mutableStateOf("65.50") }
        var liters by remember { mutableStateOf("42.5") }
        var distance by remember { mutableStateOf("580") }
        var odometer by remember { mutableStateOf("45280") }
        var notes by remember { mutableStateOf("") }

        EditRefillContent(
            amountPaid = amount,
            onAmountPaidChange = { amount = it },
            litersAdded = liters,
            onLitersAddedChange = { liters = it },
            tripDistance = distance,
            onTripDistanceChange = { distance = it },
            odometerReading = odometer,
            onOdometerReadingChange = { odometer = it },
            selectedDateMillis = System.currentTimeMillis(),
            onShowDatePicker = {},
            notes = notes,
            onNotesChange = { notes = it },
            hasLocation = false,
            onRefreshLocation = {},
            isSaving = false,
            onSaveClick = {},
            errorMessage = "Failed to save refill. Please try again.",
            isTablet = false,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

@Preview(name = "Edit Refill Content - Dark", showBackground = true, widthDp = 380)
@Composable
private fun PreviewEditRefillContentDark() {
    CarTrackingAppTheme(darkTheme = true) {
        var amount by remember { mutableStateOf("68.40") }
        var liters by remember { mutableStateOf("45.6") }
        var distance by remember { mutableStateOf("610") }
        var odometer by remember { mutableStateOf("50500") }
        var notes by remember { mutableStateOf("Esso station") }

        EditRefillContent(
            amountPaid = amount,
            onAmountPaidChange = { amount = it },
            litersAdded = liters,
            onLitersAddedChange = { liters = it },
            tripDistance = distance,
            onTripDistanceChange = { distance = it },
            odometerReading = odometer,
            onOdometerReadingChange = { odometer = it },
            selectedDateMillis = System.currentTimeMillis(),
            onShowDatePicker = {},
            notes = notes,
            onNotesChange = { notes = it },
            hasLocation = true,
            onRefreshLocation = {},
            isSaving = false,
            onSaveClick = {},
            errorMessage = null,
            isTablet = false,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}
