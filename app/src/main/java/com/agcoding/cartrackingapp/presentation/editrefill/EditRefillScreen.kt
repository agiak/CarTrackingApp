package com.agcoding.cartrackingapp.presentation.editrefill

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.presentation.components.StyledTopAppBar
import com.agcoding.cartrackingapp.presentation.editrefill.components.EditRefillContent

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
            StyledTopAppBar(
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
        val isTablet = com.agcoding.cartrackingapp.util.DeviceUtils.isTablet()

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
            // Use centered content with max width on tablets for better form usability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = if (isTablet) Alignment.TopCenter else Alignment.TopStart
            ) {
                EditRefillContent(
                    amountPaid = uiState.amountPaid,
                    onAmountPaidChange = viewModel::updateAmountPaid,
                    litersAdded = uiState.litersAdded,
                    onLitersAddedChange = viewModel::updateLitersAdded,
                    tripDistance = uiState.tripDistance,
                    onTripDistanceChange = viewModel::updateTripDistance,
                    odometerReading = uiState.odometerReading,
                    onOdometerReadingChange = viewModel::updateOdometerReading,
                    selectedDateMillis = uiState.selectedDateMillis,
                    onShowDatePicker = { viewModel.showDatePicker() },
                    notes = uiState.notes,
                    onNotesChange = viewModel::updateNotes,
                    hasLocation = uiState.location != null,
                    onRefreshLocation = { viewModel.refreshLocation() },
                    isSaving = uiState.isSaving,
                    onSaveClick = {
                        viewModel.saveRefill(onSuccess = onNavigateBack)
                    },
                    errorMessage = uiState.errorMessage,
                    isTablet = isTablet,
                    modifier = Modifier
                        .then(
                            if (isTablet) Modifier.fillMaxWidth(0.7f) // 70% width on tablets
                            else Modifier.fillMaxWidth()
                        )
                        .padding(horizontal = if (isTablet) 24.dp else 24.dp)
                        .padding(bottom = 32.dp)
                )
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
