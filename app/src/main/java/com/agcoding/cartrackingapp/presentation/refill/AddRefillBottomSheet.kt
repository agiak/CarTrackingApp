package com.agcoding.cartrackingapp.presentation.refill
import androidx.compose.ui.tooling.preview.Preview
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.presentation.components.StyledCard
import com.agcoding.cartrackingapp.presentation.components.StyledOutlinedTextField
import com.agcoding.cartrackingapp.presentation.components.ThousandsSeparatorTransformation
import com.agcoding.cartrackingapp.presentation.refill.components.VoiceEntrySection
import com.agcoding.cartrackingapp.util.formatNumber
import com.agcoding.cartrackingapp.util.parseLocalizedDouble
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddRefillBottomSheet(
    carId: Long,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit = {},
    viewModel: AddRefillViewModel = hiltViewModel()
) {
    // Set the carId in the ViewModel
    viewModel.setCarId(carId)

    val uiState by viewModel.uiState.collectAsState()
    val showDatePicker by viewModel.showDatePicker.collectAsState()
    val voiceState by viewModel.voiceState.collectAsState()
    val scrollState = rememberScrollState()
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    val scope = androidx.compose.runtime.rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = {
            scope.launch {
                sheetState.hide()
            }.invokeOnCompletion {
                if (!sheetState.isVisible) {
                    viewModel.resetForm()
                    onDismiss()
                }
            }
        },
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
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
                    text = stringResource(R.string.add_fuel_refill_title),
                    style = MaterialTheme.typography.headlineSmall
                )
                IconButton(onClick = {
                    scope.launch {
                        sheetState.hide()
                    }.invokeOnCompletion {
                        if (!sheetState.isVisible) {
                            viewModel.resetForm()
                            onDismiss()
                        }
                    }
                }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.close)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Voice Entry Section
            VoiceEntrySection(
                voiceState = voiceState,
                onStartVoiceEntry = viewModel::startVoiceEntry,
                onStopVoiceRecording = viewModel::stopVoiceRecording,
                onConfirmParsedData = viewModel::confirmVoiceParsedData,
                onCancelVoiceEntry = viewModel::cancelVoiceEntry,
                onRetryVoiceEntry = viewModel::startVoiceEntry,
                isVoiceAvailable = viewModel.isVoiceAvailable
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Amount paid
            StyledOutlinedTextField(
                value = uiState.amountPaid,
                onValueChange = viewModel::updateAmountPaid,
                label = { Text(stringResource(R.string.amount_paid_eur)) },
                placeholder = { Text(stringResource(R.string.amount_paid_hint)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                visualTransformation = ThousandsSeparatorTransformation(),
                singleLine = true,
                isError = uiState.fieldErrors.containsKey("cost"),
                supportingText = uiState.fieldErrors["cost"]?.let { error ->
                    { Text(error) }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Liters added
            StyledOutlinedTextField(
                value = uiState.litersAdded,
                onValueChange = viewModel::updateLitersAdded,
                label = { Text(stringResource(R.string.liters_added)) },
                placeholder = { Text(stringResource(R.string.liters_added_hint)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                visualTransformation = ThousandsSeparatorTransformation(),
                singleLine = true,
                isError = uiState.fieldErrors.containsKey("liters"),
                supportingText = uiState.fieldErrors["liters"]?.let { error ->
                    { Text(error) }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Trip distance
            StyledOutlinedTextField(
                value = uiState.tripDistance,
                onValueChange = viewModel::updateTripDistance,
                label = { Text(stringResource(R.string.trip_distance_km)) },
                placeholder = { Text(stringResource(R.string.trip_distance_hint)) },
                supportingText = uiState.fieldErrors["distance"]?.let { error ->
                    { Text(error) }
                } ?: { Text(stringResource(R.string.trip_distance_supporting)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                visualTransformation = ThousandsSeparatorTransformation(),
                singleLine = true,
                isError = uiState.fieldErrors.containsKey("distance"),
                modifier = Modifier.fillMaxWidth()
            )

            // Calculated odometer display (shown when trip distance is entered)
            val tripDistanceValue = uiState.tripDistance.parseLocalizedDouble()
            if (tripDistanceValue != null && tripDistanceValue > 0) {
                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
                ) {
                    // Calculated odometer value
                    val calculatedOdometer = uiState.previousOdometer + tripDistanceValue
                    Text(
                        text = stringResource(
                            R.string.calculated_odometer,
                            calculatedOdometer.toInt().formatNumber()
                        ),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Informational text
                    Text(
                        text = stringResource(R.string.check_actual_odometer),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Date field
            val dateFormat = SimpleDateFormat(stringResource(R.string.date_format_dd_mmm_yyyy), Locale.getDefault())
            val dateText = dateFormat.format(Date(uiState.selectedDateMillis))

            StyledOutlinedTextField(
                value = dateText,
                onValueChange = { },
                label = { Text(stringResource(R.string.date)) },
                readOnly = true,
                trailingIcon = {
                    IconButton(onClick = { viewModel.showDatePicker() }) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = stringResource(R.string.select_date)
                        )
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Notes (optional)
            StyledOutlinedTextField(
                value = uiState.notes,
                onValueChange = viewModel::updateNotes,
                label = { Text(stringResource(R.string.expense_notes_optional)) },
                placeholder = { Text(stringResource(R.string.expense_notes_hint)) },
                minLines = 2,
                maxLines = 4,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Location name — reverse-geocoded from the captured GPS position and
            // editable by the user. Shown while capturing or once a position exists.
            if (uiState.location != null || uiState.isLoadingLocation) {
                StyledOutlinedTextField(
                    value = uiState.locationName,
                    onValueChange = viewModel::updateLocationName,
                    label = { Text(stringResource(R.string.refill_location_name_label)) },
                    placeholder = { Text(stringResource(R.string.refill_location_name_hint)) },
                    singleLine = true,
                    enabled = !uiState.isLoadingLocation && !uiState.isLoadingLocationName,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = stringResource(R.string.location),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    trailingIcon = if (uiState.isLoadingLocation || uiState.isLoadingLocationName) {
                        {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        }
                    } else null,
                    supportingText = if (uiState.isLoadingLocation || uiState.isLoadingLocationName) {
                        { Text(stringResource(R.string.location_detecting)) }
                    } else null,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Calculated values preview
            val amount = uiState.amountPaid.parseLocalizedDouble()
            val liters = uiState.litersAdded.parseLocalizedDouble()
            val distance = uiState.tripDistance.parseLocalizedDouble()

            if (amount != null && liters != null && liters > 0) {
                val pricePerLiter = amount / liters
                val consumption = if (distance != null && distance > 0) {
                    (liters / distance) * 100.0
                } else null

                StyledCard(
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    border = null
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Text(
                            text = stringResource(R.string.calculated),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(
                                R.string.price_per_liter_format,
                                pricePerLiter.formatNumber(3)
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        if (consumption != null) {
                            Text(
                                text = stringResource(
                                    R.string.fuel_consumption_format,
                                    consumption.formatNumber(2)
                                ),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Error message
            if (uiState.errorMessage != null) {
                Text(
                    text = uiState.errorMessage!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Save button
            Button(
                onClick = {
                    viewModel.saveRefill(onSuccess = {
                        scope.launch {
                            sheetState.hide()
                        }.invokeOnCompletion {
                            if (!sheetState.isVisible) {
                                onDismiss()
                                onSuccess()
                            }
                        }
                    })
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isSaving &&
                        uiState.amountPaid.isNotBlank() &&
                        uiState.litersAdded.isNotBlank() &&
                        uiState.tripDistance.isNotBlank()
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(stringResource(R.string.save_refill))
            }
        }
    }

    // Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = uiState.selectedDateMillis
        )

        DatePickerDialog(
            onDismissRequest = { viewModel.hideDatePicker() },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            viewModel.updateDate(millis)
                        }
                    }
                ) {
                    Text(stringResource(R.string.ok_label))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideDatePicker() }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AddRefillBottomSheetPreview() {
    CarTrackingAppTheme {
        AddRefillBottomSheet(carId = 1L, onDismiss = {})
    }
}
