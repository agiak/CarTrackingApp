package com.agcoding.cartrackingapp.presentation.editrefill

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.presentation.components.StyledOutlinedTextField
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditRefillScreen(
    onNavigateBack: () -> Unit,
    viewModel: EditRefillViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val showDatePicker by viewModel.showDatePicker.collectAsState()
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.edit_refill_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Amount paid
                StyledOutlinedTextField(
                    value = uiState.amountPaid,
                    onValueChange = viewModel::updateAmountPaid,
                    label = { Text(stringResource(R.string.amount_paid_eur)) },
                    placeholder = { Text(stringResource(R.string.amount_paid_hint)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
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
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Trip distance
                StyledOutlinedTextField(
                    value = uiState.tripDistance,
                    onValueChange = viewModel::updateTripDistance,
                    label = { Text(stringResource(R.string.trip_distance_km)) },
                    placeholder = { Text(stringResource(R.string.trip_distance_hint)) },
                    supportingText = { Text(stringResource(R.string.trip_distance_supporting)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Odometer reading
                StyledOutlinedTextField(
                    value = uiState.odometerReading,
                    onValueChange = viewModel::updateOdometerReading,
                    label = { Text(stringResource(R.string.odometer_reading_km)) },
                    placeholder = { Text(stringResource(R.string.odometer_reading_hint)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

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

                // Location indicator with refresh option
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = stringResource(R.string.location),
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (uiState.location != null) stringResource(R.string.location_captured) else stringResource(R.string.no_location),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                        TextButton(onClick = { viewModel.refreshLocation() }) {
                            Text(stringResource(R.string.update))
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Calculated values preview
                val amount = uiState.amountPaid.toDoubleOrNull()
                val liters = uiState.litersAdded.toDoubleOrNull()
                val distance = uiState.tripDistance.toDoubleOrNull()

                if (amount != null && liters != null && liters > 0) {
                    val pricePerLiter = amount / liters
                    val consumption = if (distance != null && distance > 0) {
                        (liters / distance) * 100.0
                    } else null

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        )
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
                                    String.format("%.3f", pricePerLiter)
                                ),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                            if (consumption != null) {
                                Text(
                                    text = stringResource(
                                        R.string.fuel_consumption_format,
                                        String.format("%.2f", consumption)
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
                        viewModel.saveRefill(onSuccess = onNavigateBack)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isSaving &&
                            uiState.amountPaid.isNotBlank() &&
                            uiState.litersAdded.isNotBlank() &&
                            uiState.tripDistance.isNotBlank() &&
                            uiState.odometerReading.isNotBlank()
                ) {
                    if (uiState.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    Text(stringResource(R.string.save_changes))
                }

                Spacer(modifier = Modifier.height(16.dp))
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
}
