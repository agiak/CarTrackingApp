package com.agcoding.cartrackingapp.presentation.expense

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.presentation.components.StyledCard
import com.agcoding.cartrackingapp.presentation.components.StyledOutlinedTextField
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    carId: Long,
    expenseType: String = "", // Kept for backward compatibility
    onNavigateBack: () -> Unit,
    viewModel: AddExpenseViewModel = hiltViewModel()
) {
    val category by viewModel.category.collectAsState()
    val categoryExpanded by viewModel.categoryExpanded.collectAsState()
    val availableCategories by viewModel.availableCategories.collectAsState()
    val amount by viewModel.amount.collectAsState()
    val amountError by viewModel.amountError.collectAsState()
    val notes by viewModel.notes.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val showDatePicker by viewModel.showDatePicker.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()

    // Service reminder states
    val serviceReminderEnabled by viewModel.serviceReminderEnabled.collectAsState()
    val reminderDate by viewModel.reminderDate.collectAsState()
    val reminderMileage by viewModel.reminderMileage.collectAsState()
    val showReminderDatePicker by viewModel.showReminderDatePicker.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.clearFields()
        if (expenseType.isNotBlank()) {
            viewModel.setCarIdAndType(carId, expenseType)
        } else {
            viewModel.setCarId(carId)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.add_expense_title)) },
                navigationIcon = {
                    IconButton(onClick = {
                        viewModel.clearFields()
                        onNavigateBack()
                    }) {
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Category field with autocomplete dropdown
            ExposedDropdownMenuBox(
                expanded = categoryExpanded,
                onExpandedChange = { }
            ) {
                StyledOutlinedTextField(
                    value = category,
                    onValueChange = {
                        viewModel.updateCategory(it)
                        if (!categoryExpanded && it.isNotEmpty()) {
                            viewModel.toggleCategoryDropdown()
                        }
                    },
                    label = { Text(stringResource(R.string.expense_category)) },
                    placeholder = { Text(stringResource(R.string.expense_category_hint)) },
                    trailingIcon = {
                        IconButton(onClick = { viewModel.toggleCategoryDropdown() }) {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded)
                        }
                    },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable, true)
                )

                ExposedDropdownMenu(
                    expanded = categoryExpanded && availableCategories.isNotEmpty(),
                    onDismissRequest = { viewModel.dismissCategoryDropdown() }
                ) {
                    val categoriesToShow = if (category.isBlank()) {
                        availableCategories
                    } else {
                        availableCategories.filter {
                            it.contains(category, ignoreCase = true)
                        }
                    }

                    categoriesToShow.forEach { categoryOption ->
                        DropdownMenuItem(
                            text = { Text(categoryOption) },
                            onClick = { viewModel.selectCategory(categoryOption) },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Amount field
            StyledOutlinedTextField(
                value = amount,
                onValueChange = viewModel::updateAmount,
                label = { Text(stringResource(R.string.expense_amount_eur)) },
                placeholder = { Text(stringResource(R.string.amount_placeholder)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                isError = amountError != null,
                supportingText = amountError?.let { error ->
                    { Text(error) }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Date field
            StyledOutlinedTextField(
                value = SimpleDateFormat(
                    stringResource(R.string.date_format_dd_mmm_yyyy),
                    Locale.getDefault()
                ).format(Date(selectedDate)),
                onValueChange = {},
                label = { Text(stringResource(R.string.date)) },
                trailingIcon = {
                    IconButton(onClick = { viewModel.showDatePicker() }) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = stringResource(R.string.pick_date)
                        )
                    }
                },
                readOnly = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Notes field
            StyledOutlinedTextField(
                value = notes,
                onValueChange = viewModel::updateNotes,
                label = { Text(stringResource(R.string.expense_notes_optional)) },
                placeholder = { Text(stringResource(R.string.expense_notes_hint)) },
                minLines = 2,
                maxLines = 4,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Service Reminder Section
            StyledCard(
                modifier = Modifier.fillMaxWidth(),
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                border = null
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Toggle row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.service_reminder),
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = stringResource(R.string.service_reminder_description),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = serviceReminderEnabled,
                            onCheckedChange = viewModel::toggleServiceReminder
                        )
                    }

                    // Show reminder fields when enabled
                    if (serviceReminderEnabled) {
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = stringResource(R.string.reminder_optional_note),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Reminder date field
                        StyledOutlinedTextField(
                            value = reminderDate?.let {
                                SimpleDateFormat(
                                    stringResource(R.string.date_format_dd_mmm_yyyy),
                                    Locale.getDefault()
                                ).format(Date(it))
                            } ?: "",
                            onValueChange = {},
                            label = { Text(stringResource(R.string.reminder_date)) },
                            placeholder = { Text(stringResource(R.string.reminder_date_hint)) },
                            trailingIcon = {
                                Row {
                                    if (reminderDate != null) {
                                        IconButton(onClick = { viewModel.clearReminderDate() }) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = stringResource(R.string.close)
                                            )
                                        }
                                    }
                                    IconButton(onClick = { viewModel.showReminderDatePicker() }) {
                                        Icon(
                                            imageVector = Icons.Default.CalendarToday,
                                            contentDescription = stringResource(R.string.pick_date)
                                        )
                                    }
                                }
                            },
                            readOnly = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Reminder mileage field
                        StyledOutlinedTextField(
                            value = reminderMileage,
                            onValueChange = viewModel::updateReminderMileage,
                            label = { Text(stringResource(R.string.reminder_mileage)) },
                            placeholder = { Text(stringResource(R.string.reminder_mileage_hint)) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Save button
            Button(
                onClick = {
                    viewModel.saveExpense(
                        onSuccess = {
                            onNavigateBack()
                        },
                        onError = { error ->
                            scope.launch {
                                snackbarHostState.showSnackbar(error)
                            }
                        }
                    )
                },
                enabled = !isSaving && category.isNotBlank() && amount.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(stringResource(R.string.save_expense))
            }
        }

        // Expense date picker dialog
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

        // Reminder date picker dialog
        if (showReminderDatePicker) {
            val reminderDatePickerState = rememberDatePickerState(
                initialSelectedDateMillis = reminderDate ?: System.currentTimeMillis()
            )

            DatePickerDialog(
                onDismissRequest = { viewModel.hideReminderDatePicker() },
                confirmButton = {
                    TextButton(
                        onClick = {
                            reminderDatePickerState.selectedDateMillis?.let {
                                viewModel.updateReminderDate(it)
                            }
                        }
                    ) {
                        Text(stringResource(R.string.ok_label))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.hideReminderDatePicker() }) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            ) {
                DatePicker(state = reminderDatePickerState)
            }
        }
    }
}
