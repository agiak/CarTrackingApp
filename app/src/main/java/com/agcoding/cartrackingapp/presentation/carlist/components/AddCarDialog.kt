@file:OptIn(ExperimentalMaterial3Api::class)

package com.agcoding.cartrackingapp.presentation.carlist.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.presentation.components.LicensePlateBadge
import com.agcoding.cartrackingapp.presentation.components.PrimaryButton
import com.agcoding.cartrackingapp.presentation.components.SheetHeader
import com.agcoding.cartrackingapp.presentation.components.AppTextField
import com.agcoding.cartrackingapp.presentation.components.ThousandsSeparatorTransformation
import com.agcoding.cartrackingapp.presentation.components.TonalButton
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppColorScheme
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppDimens
import com.agcoding.cartrackingapp.shared.ui.tokens.AppNumbers
import com.agcoding.cartrackingapp.util.formatNumber
import com.agcoding.cartrackingapp.util.sanitizeIntInput
import com.agcoding.cartrackingapp.presentation.components.AppBottomSheet

/**
 * Adding a car, as a sheet rather than a dialog.
 *
 * A dialog with three text fields fights the keyboard: it is centred, so the
 * keyboard pushes it up and crops it, and on a short phone the buttons leave
 * the screen entirely. A sheet is anchored to the bottom where the keyboard
 * already is.
 *
 * Above the fields is a live preview of the card this is about to create. It
 * costs nothing — the data is already in hand — and it answers the question
 * the plate field actually raises, which is not "is this valid" but "is this
 * how it will look".
 *
 * The name is kept for the call sites; this is no longer a dialog.
 */
@Composable
fun AddCarDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, licensePlate: String, odometer: String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var name by remember { mutableStateOf("") }
    var licensePlate by remember { mutableStateOf("") }
    var odometer by remember { mutableStateOf("") }

    val complete = name.isNotBlank() && licensePlate.isNotBlank() && odometer.isNotBlank()

    AppBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                // Three fields, a preview row and two buttons do not fit a
                // sheet on a short phone, and with the keyboard up they fit on
                // none: without this the odometer field and both buttons are
                // simply below the bottom of the screen.
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            SheetHeader(
                title = stringResource(R.string.add_car_title),
                subtitle = stringResource(R.string.add_car_later_hint),
                onClose = onDismiss,
            )

            CarPreviewRow(name = name, plate = licensePlate, odometer = odometer)

            AppTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.add_car_field_name)) },
                placeholder = { Text(stringResource(R.string.add_car_placeholder_name)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            AppTextField(
                value = licensePlate,
                onValueChange = { licensePlate = it.uppercase() },
                label = { Text(stringResource(R.string.add_car_field_license_plate)) },
                placeholder = { Text(stringResource(R.string.add_car_placeholder_license_plate)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            AppTextField(
                value = odometer,
                onValueChange = { odometer = sanitizeIntInput(it) },
                label = { Text(stringResource(R.string.add_car_field_odometer)) },
                placeholder = { Text(stringResource(R.string.add_car_placeholder_odometer)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                visualTransformation = ThousandsSeparatorTransformation(),
                suffix = { Text("km") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(2.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                TonalButton(
                    label = stringResource(R.string.cancel),
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                )
                PrimaryButton(
                    label = stringResource(R.string.add_car_action),
                    onClick = { if (complete) onConfirm(name, licensePlate, odometer) },
                    enabled = complete,
                    modifier = Modifier.weight(1.4f),
                )
            }
        }
    }
}

/**
 * The card being built, as it stands.
 *
 * Placeholders rather than an empty box while the fields are blank: an empty
 * preview looks broken, and the greyed placeholder also shows what each field
 * will become.
 */
@Composable
private fun CarPreviewRow(name: String, plate: String, odometer: String) {
    val scheme = LocalAppColorScheme.current
    val dimens = LocalAppDimens.current
    val filled = name.isNotBlank()

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(dimens.radius.tile))
            .background(scheme.backgroundSecondary)
            .padding(14.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(dimens.size.iconTile)
                .clip(RoundedCornerShape(dimens.radius.iconTile))
                .background(scheme.actionSecondary),
        ) {
            Icon(
                imageVector = Icons.Default.DirectionsCar,
                contentDescription = null,
                tint = scheme.actionPrimary,
                modifier = Modifier.size(20.dp),
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = name.ifBlank { stringResource(R.string.add_car_field_name) },
                style = MaterialTheme.typography.titleSmall,
                color = if (filled) scheme.contentPrimary else scheme.contentDisabled,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (plate.isNotBlank()) {
                Spacer(Modifier.height(5.dp))
                LicensePlateBadge(plate = plate)
            }
        }
        if (odometer.isNotBlank()) {
            Spacer(Modifier.width(10.dp))
            Text(
                text = stringResource(
                    R.string.car_card_km_format,
                    // The app's own formatter, so the preview groups digits
                    // the same way the card it is previewing will.
                    odometer.toLongOrNull()?.formatNumber() ?: odometer,
                ),
                style = AppNumbers.compact,
                color = scheme.contentPrimary,
                maxLines = 1,
            )
        }
    }
}

@Preview(name = "Add car — empty", showBackground = true, widthDp = 400, heightDp = 620)
@Composable
private fun PreviewAddCarSheet() {
    CarTrackingAppTheme(darkTheme = true) {
        AddCarDialog(onDismiss = {}, onConfirm = { _, _, _ -> })
    }
}
