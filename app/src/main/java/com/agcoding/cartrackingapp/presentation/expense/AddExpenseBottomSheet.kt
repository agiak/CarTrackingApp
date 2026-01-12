package com.agcoding.cartrackingapp.presentation.expense

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseBottomSheet(
    carId: Long,
    expenseType: String = "", // Kept for backward compatibility, ignored if empty
    onDismiss: () -> Unit,
    onSuccess: () -> Unit = {},
    viewModel: AddExpenseViewModel = hiltViewModel()
) {
    val category by viewModel.category.collectAsState()
    val categoryExpanded by viewModel.categoryExpanded.collectAsState()
    val availableCategories by viewModel.availableCategories.collectAsState()
    val amount by viewModel.amount.collectAsState()
    val notes by viewModel.notes.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val showDatePicker by viewModel.showDatePicker.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Filter categories based on user input
    val filteredCategories = remember(category, availableCategories) {
        if (category.isBlank()) {
            availableCategories
        } else {
            availableCategories.filter {
                it.contains(category, ignoreCase = true)
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.clearFields()
        if (expenseType.isNotBlank()) {
            viewModel.setCarIdAndType(carId, expenseType)
        } else {
            viewModel.setCarId(carId)
        }
    }

    ModalBottomSheet(
        onDismissRequest = {
            scope.launch {
                sheetState.hide()
            }.invokeOnCompletion {
                if (!sheetState.isVisible) {
                    viewModel.clearFields()
                    onDismiss()
                }
            }
        },
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
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
                    text = "Add Expense",
                    style = MaterialTheme.typography.headlineSmall
                )
                IconButton(onClick = {
                    scope.launch {
                        sheetState.hide()
                    }.invokeOnCompletion {
                        if (!sheetState.isVisible) {
                            viewModel.clearFields()
                            onDismiss()
                        }
                    }
                }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close"
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Category field with autocomplete dropdown
            Column {
                OutlinedTextField(
                    value = category,
                    onValueChange = {
                        viewModel.updateCategory(it)
                        if (!categoryExpanded && it.isNotBlank()) {
                            viewModel.toggleCategoryDropdown()
                        }
                    },
                    label = { Text("Category") },
                    placeholder = { Text("Select or type a category") },
                    trailingIcon = {
                        IconButton(onClick = { viewModel.toggleCategoryDropdown() }) {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Show categories"
                            )
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Dropdown with suggestions
                DropdownMenu(
                    expanded = categoryExpanded && filteredCategories.isNotEmpty(),
                    onDismissRequest = { viewModel.dismissCategoryDropdown() },
                    modifier = Modifier.fillMaxWidth(0.9f)
                ) {
                    filteredCategories.take(8).forEach { categoryOption ->
                        DropdownMenuItem(
                            text = { Text(categoryOption) },
                            onClick = { viewModel.selectCategory(categoryOption) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Amount field
            OutlinedTextField(
                value = amount,
                onValueChange = viewModel::updateAmount,
                label = { Text("Amount (€)") },
                placeholder = { Text("e.g., 120.00") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Date field
            OutlinedTextField(
                value = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(selectedDate)),
                onValueChange = {},
                label = { Text("Date") },
                trailingIcon = {
                    IconButton(onClick = { viewModel.showDatePicker() }) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = "Pick date"
                        )
                    }
                },
                readOnly = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Notes field
            OutlinedTextField(
                value = notes,
                onValueChange = viewModel::updateNotes,
                label = { Text("Notes (optional)") },
                placeholder = { Text("Add any notes...") },
                minLines = 2,
                maxLines = 4,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Save button
            Button(
                onClick = {
                    viewModel.saveExpense(
                        onSuccess = {
                            scope.launch {
                                sheetState.hide()
                            }.invokeOnCompletion {
                                if (!sheetState.isVisible) {
                                    onDismiss()
                                    onSuccess()
                                }
                            }
                        },
                        onError = { error ->
                            kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
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
                Text("Save Expense")
            }
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
                        Text("OK")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.hideDatePicker() }) {
                        Text("Cancel")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
    }
}
