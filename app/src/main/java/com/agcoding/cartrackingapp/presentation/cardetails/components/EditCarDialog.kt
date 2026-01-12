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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agcoding.cartrackingapp.domain.model.Car
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
    var odometer by remember { mutableStateOf(car.currentOdometer.toString()) }
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
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Car Details") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Basic Info Section
                Text(
                    text = "Basic Information",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Car Name") },
                    placeholder = { Text("e.g., Toyota Corolla") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = licensePlate,
                    onValueChange = { licensePlate = it.uppercase() },
                    label = { Text("License Plate") },
                    placeholder = { Text("e.g., ABC-1234") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = odometer,
                    onValueChange = { odometer = it },
                    label = { Text("Current Odometer (km)") },
                    placeholder = { Text("e.g., 45000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = tyreSize,
                    onValueChange = { tyreSize = it },
                    label = { Text("Tyre Size (optional)") },
                    placeholder = { Text("e.g., 205/55 R16") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = licenseExpiration,
                    onValueChange = { licenseExpiration = it },
                    label = { Text("License Expiration (optional)") },
                    placeholder = { Text("e.g., 31/12/2026") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Extra Info Section
                Text(
                    text = "Extra Information",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth()
                )

                // KTEO Expiration Date
                OutlinedTextField(
                    value = kteoExpirationDate?.let { dateFormatter.format(Date(it)) } ?: "",
                    onValueChange = {},
                    label = { Text("KTEO Expiration Date") },
                    placeholder = { Text("Select date") },
                    readOnly = true,
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = "Select date"
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
                    label = { Text("Last Service Date") },
                    placeholder = { Text("Select date") },
                    readOnly = true,
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = "Select date"
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
                    label = { Text("Insurance Expiration Date") },
                    placeholder = { Text("Select date") },
                    readOnly = true,
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = "Select date"
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
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
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
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showKteoDatePicker = false }) {
                    Text("Cancel")
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
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showServiceDatePicker = false }) {
                    Text("Cancel")
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
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showInsuranceDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
