package com.agcoding.cartrackingapp.presentation.editrefill.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.presentation.components.LicensePlateBadge
import com.agcoding.cartrackingapp.presentation.components.PrimaryButton
import com.agcoding.cartrackingapp.presentation.components.SectionLabel
import com.agcoding.cartrackingapp.presentation.components.TonalButton
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppColorScheme
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppDimens
import com.agcoding.cartrackingapp.shared.ui.tokens.categoryFuel
import com.agcoding.cartrackingapp.util.formatNumber

/**
 * Editing a refill.
 *
 * The form was six fields and three cards separated by sixteen points of
 * nothing, with Save at the bottom always lit. Three things changed.
 *
 * It names the car it is editing, which the screen never did — "Edit refill"
 * alone, opened from a list, does not say which one.
 *
 * The fields are grouped: the four figures that describe the tank together,
 * then when and where, then what the app works out from them. Each group gets
 * a label, so the form reads as three short questions rather than one long
 * column.
 *
 * And it ends in a footer with Discard beside Save rather than Save alone.
 * Save is quiet until something has actually changed — an edit screen whose
 * primary action is always available invites you to press it to find out
 * whether you changed anything.
 */
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
    isSaving: Boolean,
    onSaveClick: () -> Unit,
    errorMessage: String?,
    isTablet: Boolean,
    modifier: Modifier = Modifier,
    locationName: String = "",
    onLocationNameChange: (String) -> Unit = {},
    isLoadingLocationName: Boolean = false,
    carName: String = "",
    carPlate: String = "",
    previousOdometer: Double? = null,
    expectedOdometer: Double? = null,
    isDirty: Boolean = true,
    onDiscard: (() -> Unit)? = null
) {
    val dimens = LocalAppDimens.current
    val complete = amountPaid.isNotBlank() && litersAdded.isNotBlank() &&
        tripDistance.isNotBlank() && odometerReading.isNotBlank()

    Column(modifier = modifier.verticalScroll(rememberScrollState())) {
        if (carName.isNotBlank()) {
            Spacer(Modifier.height(4.dp))
            EditContextRow(carName = carName, carPlate = carPlate)
        }

        Spacer(Modifier.height(18.dp))
        SectionLabel(stringResource(R.string.refill_details_fuel_refill))
        Spacer(Modifier.height(10.dp))
        RefillFieldsSection(
            amountPaid = amountPaid,
            onAmountPaidChange = onAmountPaidChange,
            litersAdded = litersAdded,
            onLitersAddedChange = onLitersAddedChange,
            tripDistance = tripDistance,
            onTripDistanceChange = onTripDistanceChange,
            odometerReading = odometerReading,
            onOdometerReadingChange = onOdometerReadingChange,
            previousOdometer = previousOdometer,
            expectedOdometer = expectedOdometer,
            isTablet = isTablet,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(dimens.spacing.sectionTop))
        SectionLabel(stringResource(R.string.additional_information))
        Spacer(Modifier.height(10.dp))
        RefillDateField(
            selectedDateMillis = selectedDateMillis,
            onShowDatePicker = onShowDatePicker,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(14.dp))
        RefillNotesField(
            notes = notes,
            onNotesChange = onNotesChange,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(14.dp))
        RefillLocationCard(
            locationName = locationName,
            onLocationNameChange = onLocationNameChange,
            hasLocation = hasLocation,
            onRefreshLocation = onRefreshLocation,
            isLoading = isLoadingLocationName,
            modifier = Modifier.fillMaxWidth()
        )

        CalculatedValuesCard(
            amountPaid = amountPaid,
            litersAdded = litersAdded,
            tripDistance = tripDistance,
            modifier = Modifier.fillMaxWidth()
        )

        if (errorMessage != null) {
            Spacer(Modifier.height(16.dp))
            FormError(errorMessage)
        }

        Spacer(Modifier.height(dimens.spacing.sectionTop))
        Row {
            if (onDiscard != null && isDirty) {
                TonalButton(
                    label = stringResource(R.string.edit_discard),
                    onClick = onDiscard,
                    enabled = !isSaving,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(10.dp))
            }
            PrimaryButton(
                label = stringResource(R.string.edit_save_changes),
                onClick = onSaveClick,
                // Quiet until there is something to save: a lit Save on an
                // untouched form is a button that does nothing.
                enabled = complete && isDirty,
                isBusy = isSaving,
                modifier = Modifier.weight(1.4f)
            )
        }

        Spacer(Modifier.height(24.dp))
    }
}

/** Which car this refill belongs to. */
@Composable
private fun EditContextRow(carName: String, carPlate: String) {
    val scheme = LocalAppColorScheme.current
    val dimens = LocalAppDimens.current

    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(dimens.size.iconTile)
                .clip(RoundedCornerShape(dimens.radius.iconTile))
                .background(scheme.categoryFuel.copy(alpha = 0.16f))
        ) {
            Icon(
                imageVector = Icons.Default.LocalGasStation,
                contentDescription = null,
                tint = scheme.categoryFuel,
                modifier = Modifier.size(19.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Text(
            text = carName,
            style = MaterialTheme.typography.titleSmall,
            color = scheme.contentPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (carPlate.isNotBlank()) {
            Spacer(Modifier.width(10.dp))
            LicensePlateBadge(plate = carPlate)
        }
    }
}

/** Whatever went wrong with saving, said where the eye already is. */
@Composable
internal fun FormError(message: String) {
    val scheme = LocalAppColorScheme.current
    val dimens = LocalAppDimens.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(dimens.radius.tile))
            .background(scheme.statusError.copy(alpha = 0.12f))
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Icon(
            imageVector = Icons.Default.ErrorOutline,
            contentDescription = null,
            tint = scheme.statusError,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = scheme.statusError
        )
    }
}

/** Formats an odometer figure the way the fields and hints both print it. */
internal fun odometerText(value: Double): String = value.toLong().formatNumber()

// ============================================
// Preview Composables
// ============================================

@Preview(name = "Edit refill — untouched", showBackground = true, widthDp = 390, heightDp = 1100)
@Composable
private fun PreviewEditRefillUntouched() {
    CarTrackingAppTheme(darkTheme = true) {
        PreviewForm(dirty = false, error = null)
    }
}

@Preview(name = "Edit refill — changed", showBackground = true, widthDp = 390, heightDp = 1100)
@Composable
private fun PreviewEditRefillChanged() {
    CarTrackingAppTheme(darkTheme = true) {
        PreviewForm(dirty = true, error = null)
    }
}

@Preview(name = "Edit refill — error", showBackground = true, widthDp = 390, heightDp = 1100)
@Composable
private fun PreviewEditRefillError() {
    CarTrackingAppTheme(darkTheme = false) {
        PreviewForm(dirty = true, error = "Please enter a valid amount")
    }
}

@Composable
private fun PreviewForm(dirty: Boolean, error: String?) {
    var amount by remember { mutableStateOf("70,43") }
    var liters by remember { mutableStateOf("39,3") }
    var distance by remember { mutableStateOf("641") }
    var odometer by remember { mutableStateOf("119625") }
    var notes by remember { mutableStateOf("Full tank before the weekend trip.") }

    EditRefillContent(
        amountPaid = amount,
        onAmountPaidChange = { amount = it },
        litersAdded = liters,
        onLitersAddedChange = { liters = it },
        tripDistance = distance,
        onTripDistanceChange = { distance = it },
        odometerReading = odometer,
        onOdometerReadingChange = { odometer = it },
        selectedDateMillis = 1_760_000_000_000L,
        onShowDatePicker = {},
        notes = notes,
        onNotesChange = { notes = it },
        hasLocation = true,
        onRefreshLocation = {},
        locationName = "Shell · Kifisias Ave 212, Marousi",
        onLocationNameChange = {},
        isSaving = false,
        onSaveClick = {},
        errorMessage = error,
        isTablet = false,
        carName = "Toyota Corolla",
        carPlate = "ABC-1234",
        previousOdometer = 118_984.0,
        expectedOdometer = 119_625.0,
        isDirty = dirty,
        onDiscard = {},
        modifier = Modifier.padding(horizontal = 18.dp)
    )
}
