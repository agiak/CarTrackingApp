package com.agcoding.cartrackingapp.presentation.cardetails.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.domain.model.Car
import com.agcoding.cartrackingapp.presentation.components.ThousandsSeparatorTransformation
import com.agcoding.cartrackingapp.util.sanitizeIntInput
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCarDialog(
    car: Car,
    onDismiss: () -> Unit,
    onConfirm: (
        name: String,
        licensePlate: String,
        odometer: String,
        tyreSize: String,
        licenseExpiration: String,
        kteoExpirationDate: Long?,
        lastServiceDate: Long?,
        insuranceExpirationDate: Long?
    ) -> Unit
) {
    var name by remember { mutableStateOf(car.name) }
    var licensePlate by remember { mutableStateOf(car.licensePlate) }
    var odometer by remember { mutableStateOf(car.currentOdometer.toInt().toString()) }
    var tyreSize by remember { mutableStateOf(car.tyreSize ?: "") }
    var licenseExpiration by remember { mutableStateOf(car.licenseExpiration ?: "") }

    // Extra info fields
    var kteoExpirationDate by remember { mutableStateOf(car.kteoExpirationDate) }
    var lastServiceDate by remember { mutableStateOf(car.lastServiceDate) }
    var insuranceExpirationDate by remember { mutableStateOf(car.insuranceExpirationDate) }

    // Date picker states
    var showKteoDatePicker by remember { mutableStateOf(false) }
    var showServiceDatePicker by remember { mutableStateOf(false) }
    var showInsuranceDatePicker by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()
    val datePattern = stringResource(R.string.date_format_dd_mm_yyyy)
    val dateFormatter = remember(datePattern) { SimpleDateFormat(datePattern, Locale.getDefault()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.edit_car_details_title)) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Basic Info Section
                Text(
                    text = stringResource(R.string.basic_information),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(stringResource(R.string.edit_car_field_car_name)) },
                    placeholder = { Text(stringResource(R.string.edit_car_placeholder_car_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = licensePlate,
                    onValueChange = { licensePlate = it.uppercase() },
                    label = { Text(stringResource(R.string.edit_car_field_license_plate)) },
                    placeholder = { Text(stringResource(R.string.edit_car_placeholder_license_plate)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = odometer,
                    onValueChange = { odometer = sanitizeIntInput(it) },
                    label = { Text(stringResource(R.string.edit_car_field_current_odometer_km)) },
                    placeholder = { Text(stringResource(R.string.edit_car_placeholder_odometer)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    visualTransformation = ThousandsSeparatorTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = tyreSize,
                    onValueChange = { tyreSize = it },
                    label = { Text(stringResource(R.string.tyre_size_optional)) },
                    placeholder = { Text(stringResource(R.string.tyre_size_hint)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = licenseExpiration,
                    onValueChange = { licenseExpiration = it },
                    label = { Text(stringResource(R.string.license_expiration_optional)) },
                    placeholder = { Text(stringResource(R.string.license_expiration_hint)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Extra Info Section
                Text(
                    text = stringResource(R.string.extra_information),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth()
                )

                // KTEO Expiration Date
                OutlinedTextField(
                    value = kteoExpirationDate?.let { dateFormatter.format(Date(it)) } ?: "",
                    onValueChange = {},
                    label = { Text(stringResource(R.string.kteo_expiration_date)) },
                    placeholder = { Text(stringResource(R.string.edit_car_placeholder_select_date)) },
                    readOnly = true,
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = stringResource(R.string.edit_car_cd_select_date)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showKteoDatePicker = true }
                )

                // Last Service Date
                OutlinedTextField(
                    value = lastServiceDate?.let { dateFormatter.format(Date(it)) } ?: "",
                    onValueChange = {},
                    label = { Text(stringResource(R.string.edit_car_field_last_service_date)) },
                    placeholder = { Text(stringResource(R.string.edit_car_placeholder_select_date)) },
                    readOnly = true,
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = stringResource(R.string.edit_car_cd_select_date)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showServiceDatePicker = true }
                )

                // Insurance Expiration Date
                OutlinedTextField(
                    value = insuranceExpirationDate?.let { dateFormatter.format(Date(it)) } ?: "",
                    onValueChange = {},
                    label = { Text(stringResource(R.string.edit_car_field_insurance_expiration_date)) },
                    placeholder = { Text(stringResource(R.string.edit_car_placeholder_select_date)) },
                    readOnly = true,
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = stringResource(R.string.edit_car_cd_select_date)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showInsuranceDatePicker = true }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank() && odometer.isNotBlank()) {
                        onConfirm(
                            name,
                            licensePlate,
                            odometer,
                            tyreSize,
                            licenseExpiration,
                            kteoExpirationDate,
                            lastServiceDate,
                            insuranceExpirationDate
                        )
                    }
                },
                enabled = name.isNotBlank() && odometer.isNotBlank()
            ) {
                Text(stringResource(R.string.save_label))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )

    // KTEO Date Picker Dialog
    if (showKteoDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = kteoExpirationDate ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showKteoDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    kteoExpirationDate = datePickerState.selectedDateMillis
                    showKteoDatePicker = false
                }) {
                    Text(stringResource(R.string.ok_label))
                }
            },
            dismissButton = {
                TextButton(onClick = { showKteoDatePicker = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Service Date Picker Dialog
    if (showServiceDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = lastServiceDate ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showServiceDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    lastServiceDate = datePickerState.selectedDateMillis
                    showServiceDatePicker = false
                }) {
                    Text(stringResource(R.string.ok_label))
                }
            },
            dismissButton = {
                TextButton(onClick = { showServiceDatePicker = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Insurance Date Picker Dialog
    if (showInsuranceDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = insuranceExpirationDate ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showInsuranceDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    insuranceExpirationDate = datePickerState.selectedDateMillis
                    showInsuranceDatePicker = false
                }) {
                    Text(stringResource(R.string.ok_label))
                }
            },
            dismissButton = {
                TextButton(onClick = { showInsuranceDatePicker = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
