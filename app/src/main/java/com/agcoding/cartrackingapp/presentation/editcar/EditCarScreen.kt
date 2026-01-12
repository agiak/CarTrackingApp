package com.agcoding.cartrackingapp.presentation.editcar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.agcoding.cartrackingapp.presentation.cardetails.CarDetailsUiState
import com.agcoding.cartrackingapp.presentation.cardetails.CarDetailsViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCarScreen(
    onNavigateBack: () -> Unit,
    viewModel: CarDetailsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    when (uiState) {
        is CarDetailsUiState.Loading -> {
            // Show loading state
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Edit Car") },
                        navigationIcon = {
                            IconButton(onClick = onNavigateBack) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back"
                                )
                            }
                        }
                    )
                }
            ) { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            return
        }
        is CarDetailsUiState.Error -> {
            // Navigate back on error
            onNavigateBack()
            return
        }
        is CarDetailsUiState.Success -> {
            // Continue with normal flow
        }
    }

    val car = (uiState as CarDetailsUiState.Success).statistics.car

    var name by remember { mutableStateOf(car.name) }
    var licensePlate by remember { mutableStateOf(car.licensePlate) }
    var odometer by remember { mutableStateOf(car.currentOdometer.toString()) }

    // Insurance fields
    var insuranceExpirationDate by remember { mutableStateOf(car.insuranceExpirationDate) }

    // Legal & Compliance fields
    var kteoExpirationDate by remember { mutableStateOf(car.kteoExpirationDate) }
    var emissionsCardExpirationDate by remember { mutableStateOf(car.emissionsCardExpirationDate) }
    var roadTaxAmount by remember { mutableStateOf(car.roadTaxAmount?.toString() ?: "") }
    var roadTaxDueDate by remember { mutableStateOf(car.roadTaxDueDate) }

    // Maintenance fields
    var lastServiceDate by remember { mutableStateOf(car.lastServiceDate) }
    var lastTireChangeDate by remember { mutableStateOf(car.lastTireChangeDate) }

    // Tires fields
    var tireBrand by remember { mutableStateOf(car.tireBrand ?: "") }
    var tireDimensions by remember { mutableStateOf(car.tireDimensions ?: "") }
    var tireInstallationDate by remember { mutableStateOf(car.tireInstallationDate) }

    // Date picker states
    var showInsuranceDatePicker by remember { mutableStateOf(false) }
    var showKteoDatePicker by remember { mutableStateOf(false) }
    var showEmissionsDatePicker by remember { mutableStateOf(false) }
    var showRoadTaxDueDatePicker by remember { mutableStateOf(false) }
    var showServiceDatePicker by remember { mutableStateOf(false) }
    var showTireChangeDatePicker by remember { mutableStateOf(false) }
    var showTireInstallationDatePicker by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()
    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Car") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            if (name.isNotBlank() && licensePlate.isNotBlank() && odometer.isNotBlank()) {
                                viewModel.updateCar(
                                    name = name,
                                    licensePlate = licensePlate,
                                    odometer = odometer,
                                    insuranceExpirationDate = insuranceExpirationDate,
                                    kteoExpirationDate = kteoExpirationDate,
                                    emissionsCardExpirationDate = emissionsCardExpirationDate,
                                    roadTaxAmount = roadTaxAmount.toDoubleOrNull(),
                                    roadTaxDueDate = roadTaxDueDate,
                                    lastServiceDate = lastServiceDate,
                                    lastTireChangeDate = lastTireChangeDate,
                                    tireBrand = tireBrand.takeIf { it.isNotBlank() },
                                    tireDimensions = tireDimensions.takeIf { it.isNotBlank() },
                                    tireInstallationDate = tireInstallationDate
                                )
                                onNavigateBack()
                            }
                        },
                        enabled = name.isNotBlank() && licensePlate.isNotBlank() && odometer.isNotBlank()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Save"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Basic Info Section
            Text(
                text = "Basic Information",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 16.dp)
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

            // Insurance Section
            Text(
                text = "Insurance Information",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 8.dp)
            )

            DatePickerField(
                value = insuranceExpirationDate,
                label = "Insurance Expiration Date",
                placeholder = "Select date",
                dateFormatter = dateFormatter,
                onClick = { showInsuranceDatePicker = true }
            )

            // Legal & Compliance Section
            Text(
                text = "Legal & Compliance",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 8.dp)
            )


            DatePickerField(
                value = kteoExpirationDate,
                label = "KTEO Expiration Date",
                placeholder = "Select date",
                dateFormatter = dateFormatter,
                onClick = { showKteoDatePicker = true }
            )

            DatePickerField(
                value = emissionsCardExpirationDate,
                label = "Emissions Card Expiration",
                placeholder = "Select date",
                dateFormatter = dateFormatter,
                onClick = { showEmissionsDatePicker = true }
            )

            OutlinedTextField(
                value = roadTaxAmount,
                onValueChange = { roadTaxAmount = it },
                label = { Text("Road Tax Amount (€)") },
                placeholder = { Text("e.g., 120.00") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            DatePickerField(
                value = roadTaxDueDate,
                label = "Road Tax Due Date",
                placeholder = "Select date",
                dateFormatter = dateFormatter,
                onClick = { showRoadTaxDueDatePicker = true }
            )

            // Maintenance Section
            Text(
                text = "Maintenance History",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 8.dp)
            )

            DatePickerField(
                value = lastServiceDate,
                label = "Last Service Date",
                placeholder = "Select date",
                dateFormatter = dateFormatter,
                onClick = { showServiceDatePicker = true }
            )

            DatePickerField(
                value = lastTireChangeDate,
                label = "Last Tire Change Date",
                placeholder = "Select date",
                dateFormatter = dateFormatter,
                onClick = { showTireChangeDatePicker = true }
            )

            // Tires Section
            Text(
                text = "Tires Information",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 8.dp)
            )

            OutlinedTextField(
                value = tireBrand,
                onValueChange = { tireBrand = it },
                label = { Text("Tire Brand / Model") },
                placeholder = { Text("e.g., Michelin Pilot Sport 4") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = tireDimensions,
                onValueChange = { tireDimensions = it },
                label = { Text("Tire Dimensions") },
                placeholder = { Text("e.g., 205/55 R16") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            DatePickerField(
                value = tireInstallationDate,
                label = "Tire Installation Date",
                placeholder = "Select date",
                dateFormatter = dateFormatter,
                onClick = { showTireInstallationDatePicker = true }
            )

            // Add bottom spacing
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Insurance Date Picker
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
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showInsuranceDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }


    // KTEO Date Picker
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
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showKteoDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Emissions Date Picker
    if (showEmissionsDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = emissionsCardExpirationDate ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showEmissionsDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    emissionsCardExpirationDate = datePickerState.selectedDateMillis
                    showEmissionsDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showEmissionsDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Road Tax Due Date Picker
    if (showRoadTaxDueDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = roadTaxDueDate ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showRoadTaxDueDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    roadTaxDueDate = datePickerState.selectedDateMillis
                    showRoadTaxDueDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showRoadTaxDueDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Service Date Picker
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
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showServiceDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Tire Change Date Picker
    if (showTireChangeDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = lastTireChangeDate ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showTireChangeDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    lastTireChangeDate = datePickerState.selectedDateMillis
                    showTireChangeDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showTireChangeDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Tire Installation Date Picker
    if (showTireInstallationDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = tireInstallationDate ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showTireInstallationDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    tireInstallationDate = datePickerState.selectedDateMillis
                    showTireInstallationDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showTireInstallationDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun DatePickerField(
    value: Long?,
    label: String,
    placeholder: String,
    dateFormatter: SimpleDateFormat,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        OutlinedTextField(
            value = value?.let { dateFormatter.format(Date(it)) } ?: "",
            onValueChange = {},
            label = { Text(label) },
            placeholder = { Text(placeholder) },
            readOnly = true,
            enabled = false,
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = "Select date"
                )
            },
            modifier = Modifier.fillMaxWidth(),
            colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                disabledBorderColor = MaterialTheme.colorScheme.outline,
                disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }
}

