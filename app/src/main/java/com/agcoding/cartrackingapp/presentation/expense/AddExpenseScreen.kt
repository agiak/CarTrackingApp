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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.presentation.components.StyledCard
import com.agcoding.cartrackingapp.presentation.components.StyledOutlinedTextField
import com.agcoding.cartrackingapp.presentation.components.StyledTopAppBar
import com.agcoding.cartrackingapp.presentation.components.SuccessOverlay
import com.agcoding.cartrackingapp.presentation.components.ThousandsSeparatorTransformation
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddExpenseScreen(
    carId: Long,
    expenseType: String = "",
    onNavigateBack: () -> Unit,
    onExpenseSaved: () -> Unit = {},
    viewModel: AddExpenseViewModel = hiltViewModel()
) {
    val category by viewModel.category.collectAsState()
    val quickPickCategories by viewModel.quickPickCategories.collectAsState()
    val otherCategories by viewModel.otherCategories.collectAsState()
    val dropdownExpanded by viewModel.dropdownExpanded.collectAsState()
    val showCustomCategoryField by viewModel.showCustomCategoryField.collectAsState()
    val customCategoryText by viewModel.customCategoryText.collectAsState()
    val amount by viewModel.amount.collectAsState()
    val amountError by viewModel.amountError.collectAsState()
    val categoryError by viewModel.categoryError.collectAsState()
    val notes by viewModel.notes.collectAsState()
    val selectedDate by viewModel.selectedDate.collectAsState()
    val showDatePicker by viewModel.showDatePicker.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()

    val serviceReminderEnabled by viewModel.serviceReminderEnabled.collectAsState()
    val reminderDate by viewModel.reminderDate.collectAsState()
    val reminderMileage by viewModel.reminderMileage.collectAsState()
    val showReminderDatePicker by viewModel.showReminderDatePicker.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Set once the expense is stored; drives the success overlay below, which
    // navigates back when its animation settles.
    var showSuccess by remember { mutableStateOf(false) }

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
            StyledTopAppBar(
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
        val configuration = LocalConfiguration.current
        val screenWidthDp = configuration.screenWidthDp
        val isLandscape = configuration.screenWidthDp > configuration.screenHeightDp
        val useSplitView = screenWidthDp >= 600 || isLandscape

        if (useSplitView) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Left side: Category selection (35%)
                Column(
                    modifier = Modifier
                        .weight(0.35f)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    CategorySelector(
                        selectedCategory = category,
                        quickPickCategories = quickPickCategories,
                        otherCategories = otherCategories,
                        dropdownExpanded = dropdownExpanded,
                        showCustomField = showCustomCategoryField,
                        customText = customCategoryText,
                        categoryError = categoryError,
                        onSelectCategory = viewModel::selectCategory,
                        onToggleDropdown = viewModel::toggleDropdown,
                        onDismissDropdown = viewModel::dismissDropdown,
                        onShowCustomField = viewModel::showCustomCategoryField,
                        onHideCustomField = viewModel::hideCustomCategoryField,
                        onCustomTextChange = viewModel::updateCustomCategoryText,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Right side: Form fields (65%)
                Column(
                    modifier = Modifier
                        .weight(0.65f)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    StyledOutlinedTextField(
                        value = amount,
                        onValueChange = viewModel::updateAmount,
                        label = { Text(stringResource(R.string.expense_amount_eur)) },
                        placeholder = { Text(stringResource(R.string.amount_placeholder)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        visualTransformation = ThousandsSeparatorTransformation(),
                        singleLine = true,
                        isError = amountError != null,
                        supportingText = amountError?.let { error ->
                            { Text(error) }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

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

                    ServiceReminderSection(
                        serviceReminderEnabled = serviceReminderEnabled,
                        reminderDate = reminderDate,
                        reminderMileage = reminderMileage,
                        viewModel = viewModel
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            viewModel.saveExpense(
                                onSuccess = { showSuccess = true },
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
            }
        } else {
            // Portrait mode
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                CategorySelector(
                    selectedCategory = category,
                    quickPickCategories = quickPickCategories,
                    otherCategories = otherCategories,
                    dropdownExpanded = dropdownExpanded,
                    showCustomField = showCustomCategoryField,
                    customText = customCategoryText,
                    categoryError = categoryError,
                    onSelectCategory = viewModel::selectCategory,
                    onToggleDropdown = viewModel::toggleDropdown,
                    onDismissDropdown = viewModel::dismissDropdown,
                    onShowCustomField = viewModel::showCustomCategoryField,
                    onHideCustomField = viewModel::hideCustomCategoryField,
                    onCustomTextChange = viewModel::updateCustomCategoryText,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                StyledOutlinedTextField(
                    value = amount,
                    onValueChange = viewModel::updateAmount,
                    label = { Text(stringResource(R.string.expense_amount_eur)) },
                    placeholder = { Text(stringResource(R.string.amount_placeholder)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    visualTransformation = ThousandsSeparatorTransformation(),
                    singleLine = true,
                    isError = amountError != null,
                    supportingText = amountError?.let { error ->
                        { Text(error) }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

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

                ServiceReminderSection(
                    serviceReminderEnabled = serviceReminderEnabled,
                    reminderDate = reminderDate,
                    reminderMileage = reminderMileage,
                    viewModel = viewModel
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        viewModel.saveExpense(
                            onSuccess = { showSuccess = true },
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

        SuccessOverlay(
            visible = showSuccess,
            message = stringResource(R.string.expense_saved),
            onFinished = {
                onExpenseSaved()
                onNavigateBack()
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ServiceReminderSection(
    serviceReminderEnabled: Boolean,
    reminderDate: Long?,
    reminderMileage: String,
    viewModel: AddExpenseViewModel
) {
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

            if (serviceReminderEnabled) {
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.reminder_optional_note),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

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

                StyledOutlinedTextField(
                    value = reminderMileage,
                    onValueChange = viewModel::updateReminderMileage,
                    label = { Text(stringResource(R.string.reminder_mileage)) },
                    placeholder = { Text(stringResource(R.string.reminder_mileage_hint)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    visualTransformation = ThousandsSeparatorTransformation(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
