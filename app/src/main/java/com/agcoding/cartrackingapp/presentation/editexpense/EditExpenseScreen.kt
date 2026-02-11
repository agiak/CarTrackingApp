package com.agcoding.cartrackingapp.presentation.editexpense

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.presentation.editexpense.components.EditExpenseContent
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditExpenseScreen(
    onNavigateBack: () -> Unit,
    viewModel: EditExpenseViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val amount by viewModel.amount.collectAsState()
    val notes by viewModel.notes.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val showDatePicker by viewModel.showDatePicker.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.edit_expense_title)) },
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
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        when (val state = uiState) {
            is EditExpenseUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is EditExpenseUiState.Success -> {
                val isTablet = com.agcoding.cartrackingapp.util.DeviceUtils.isTablet()

                // Use centered content with max width on tablets
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = if (isTablet) Alignment.TopCenter else Alignment.TopStart
                ) {
                    EditExpenseContent(
                        category = state.category,
                        amount = amount,
                        onAmountChange = viewModel::updateAmount,
                        selectedDate = selectedDate,
                        onShowDatePicker = { viewModel.showDatePicker() },
                        notes = notes,
                        onNotesChange = viewModel::updateNotes,
                        isSaving = isSaving,
                        onSaveClick = {
                            viewModel.updateExpense(
                                onSuccess = {
                                    onNavigateBack()
                                },
                                onError = { error ->
                                    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                                        snackbarHostState.showSnackbar(error)
                                    }
                                }
                            )
                        },
                        modifier = Modifier
                            .then(
                                if (isTablet) Modifier.fillMaxWidth(0.7f) // 70% width on tablets
                                else Modifier.fillMaxWidth()
                            )
                            .padding(horizontal = if (isTablet) 24.dp else 16.dp)
                    )
                }

                // Date picker dialog
                if (showDatePicker) {
                    val datePickerState = rememberDatePickerState(
                        initialSelectedDateMillis = selectedDate
                    )

                    DatePickerDialog(
                        onDismissRequest = { viewModel.hideDatePicker() },
                        confirmButton = {
                            TextButton(
                                onClick = {
                                    datePickerState.selectedDateMillis?.let {
                                        viewModel.updateDate(it)
                                    }
                                    viewModel.hideDatePicker()
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

            is EditExpenseUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(state.message)
                }
            }
        }
    }
}
