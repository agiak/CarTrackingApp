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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.presentation.cardetails.CarDetailsUiState
import com.agcoding.cartrackingapp.presentation.cardetails.CarDetailsViewModel
import com.agcoding.cartrackingapp.presentation.components.StyledOutlinedTextField
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
                        title = { Text(stringResource(R.string.edit_car_title)) },
                        navigationIcon = {
                            IconButton(onClick = onNavigateBack) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = stringResource(R.string.back)
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
                title = { Text(stringResource(R.string.edit_car_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
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
                            contentDescription = stringResource(R.string.save_label)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
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
                text = stringResource(R.string.edit_car_section_basic_information),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 16.dp)
            )

            StyledOutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.edit_car_field_car_name)) },
                placeholder = { Text(stringResource(R.string.edit_car_placeholder_car_name)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            StyledOutlinedTextField(
                value = licensePlate,
                onValueChange = { licensePlate = it.uppercase() },
                label = { Text(stringResource(R.string.edit_car_field_license_plate)) },
                placeholder = { Text(stringResource(R.string.edit_car_placeholder_license_plate)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            StyledOutlinedTextField(
                value = odometer,
                onValueChange = { newValue ->
                    // Only allow digits (no decimal for odometer)
                    if (newValue.isEmpty() || newValue.matches(Regex("^\\d+$"))) {
                        odometer = newValue
                    }
                },
                label = { Text(stringResource(R.string.edit_car_field_current_odometer_km)) },
                placeholder = { Text(stringResource(R.string.edit_car_placeholder_odometer)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            // Insurance Section
            Text(
                text = stringResource(R.string.edit_car_section_insurance_information),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 8.dp)
            )

            DatePickerField(
                value = insuranceExpirationDate,
                labelRes = R.string.edit_car_field_insurance_expiration_date,
                placeholderRes = R.string.edit_car_placeholder_select_date,
                trailingIconContentDescriptionRes = R.string.edit_car_cd_select_date,
                dateFormatter = dateFormatter,
                onClick = { showInsuranceDatePicker = true }
            )

            // Legal & Compliance Section
            Text(
                text = stringResource(R.string.edit_car_section_legal_compliance),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 8.dp)
            )


            DatePickerField(
                value = kteoExpirationDate,
                labelRes = R.string.edit_car_field_kteo_expiration_date,
                placeholderRes = R.string.edit_car_placeholder_select_date,
                trailingIconContentDescriptionRes = R.string.edit_car_cd_select_date,
                dateFormatter = dateFormatter,
                onClick = { showKteoDatePicker = true }
            )

            DatePickerField(
                value = emissionsCardExpirationDate,
                labelRes = R.string.edit_car_field_emissions_card_expiration,
                placeholderRes = R.string.edit_car_placeholder_select_date,
                trailingIconContentDescriptionRes = R.string.edit_car_cd_select_date,
                dateFormatter = dateFormatter,
                onClick = { showEmissionsDatePicker = true }
            )

            StyledOutlinedTextField(
                value = roadTaxAmount,
                onValueChange = { roadTaxAmount = it },
                label = { Text(stringResource(R.string.edit_car_field_road_tax_amount_eur)) },
                placeholder = { Text(stringResource(R.string.edit_car_placeholder_road_tax_amount)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            DatePickerField(
                value = roadTaxDueDate,
                labelRes = R.string.edit_car_field_road_tax_due_date,
                placeholderRes = R.string.edit_car_placeholder_select_date,
                trailingIconContentDescriptionRes = R.string.edit_car_cd_select_date,
                dateFormatter = dateFormatter,
                onClick = { showRoadTaxDueDatePicker = true }
            )

            // Maintenance Section
            Text(
                text = stringResource(R.string.edit_car_section_maintenance_history),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 8.dp)
            )

            DatePickerField(
                value = lastServiceDate,
                labelRes = R.string.edit_car_field_last_service_date,
                placeholderRes = R.string.edit_car_placeholder_select_date,
                trailingIconContentDescriptionRes = R.string.edit_car_cd_select_date,
                dateFormatter = dateFormatter,
                onClick = { showServiceDatePicker = true }
            )

            DatePickerField(
                value = lastTireChangeDate,
                labelRes = R.string.edit_car_field_last_tire_change_date,
                placeholderRes = R.string.edit_car_placeholder_select_date,
                trailingIconContentDescriptionRes = R.string.edit_car_cd_select_date,
                dateFormatter = dateFormatter,
                onClick = { showTireChangeDatePicker = true }
            )

            // Tires Section
            Text(
                text = stringResource(R.string.edit_car_section_tires_information),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 8.dp)
            )

            StyledOutlinedTextField(
                value = tireBrand,
                onValueChange = { tireBrand = it },
                label = { Text(stringResource(R.string.edit_car_field_tire_brand_model)) },
                placeholder = { Text(stringResource(R.string.edit_car_placeholder_tire_brand_model)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            StyledOutlinedTextField(
                value = tireDimensions,
                onValueChange = { tireDimensions = it },
                label = { Text(stringResource(R.string.edit_car_field_tire_dimensions)) },
                placeholder = { Text(stringResource(R.string.edit_car_placeholder_tire_dimensions)) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            DatePickerField(
                value = tireInstallationDate,
                labelRes = R.string.edit_car_field_tire_installation_date,
                placeholderRes = R.string.edit_car_placeholder_select_date,
                trailingIconContentDescriptionRes = R.string.edit_car_cd_select_date,
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
                }) { Text(stringResource(R.string.ok_label)) }
            },
            dismissButton = {
                TextButton(onClick = { showInsuranceDatePicker = false }) { Text(stringResource(R.string.cancel)) }
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
                }) { Text(stringResource(R.string.ok_label)) }
            },
            dismissButton = {
                TextButton(onClick = { showKteoDatePicker = false }) { Text(stringResource(R.string.cancel)) }
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
                }) { Text(stringResource(R.string.ok_label)) }
            },
            dismissButton = {
                TextButton(onClick = { showEmissionsDatePicker = false }) { Text(stringResource(R.string.cancel)) }
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
                }) { Text(stringResource(R.string.ok_label)) }
            },
            dismissButton = {
                TextButton(onClick = { showRoadTaxDueDatePicker = false }) { Text(stringResource(R.string.cancel)) }
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
                }) { Text(stringResource(R.string.ok_label)) }
            },
            dismissButton = {
                TextButton(onClick = { showServiceDatePicker = false }) { Text(stringResource(R.string.cancel)) }
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
                }) { Text(stringResource(R.string.ok_label)) }
            },
            dismissButton = {
                TextButton(onClick = { showTireChangeDatePicker = false }) { Text(stringResource(R.string.cancel)) }
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
                }) { Text(stringResource(R.string.ok_label)) }
            },
            dismissButton = {
                TextButton(onClick = { showTireInstallationDatePicker = false }) { Text(stringResource(R.string.cancel)) }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun DatePickerField(
    value: Long?,
    labelRes: Int,
    placeholderRes: Int,
    trailingIconContentDescriptionRes: Int,
    dateFormatter: SimpleDateFormat,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        StyledOutlinedTextField(
            value = value?.let { dateFormatter.format(Date(it)) } ?: "",
            onValueChange = {},
            label = { Text(stringResource(labelRes)) },
            placeholder = { Text(stringResource(placeholderRes)) },
            readOnly = true,
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = stringResource(trailingIconContentDescriptionRes)
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
