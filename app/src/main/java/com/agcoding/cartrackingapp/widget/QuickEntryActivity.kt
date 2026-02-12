package com.agcoding.cartrackingapp.widget

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.domain.model.ExpenseCategories
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Quick Entry Activity - Minimal overlay for fast refill/expense entry
 * Appears as a dialog overlay without launching full app
 */
@AndroidEntryPoint
class QuickEntryActivity : AppCompatActivity() {

    private val viewModel: QuickEntryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val action = intent.getStringExtra(EXTRA_ACTION) ?: ACTION_ADD_REFILL

        // Car ID is now optional - user will select it in the entry screen
        val carId = intent.getLongExtra(EXTRA_CAR_ID, -1L)

        // If car ID was provided, set it
        if (carId != -1L) {
            viewModel.setCarId(carId)
            viewModel.loadCarName()
        }

        // Load all cars for selection
        viewModel.loadAllCars()

        setContent {
            CarTrackingAppTheme {
                // Transparent background
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    when (action) {
                        ACTION_ADD_REFILL -> QuickRefillDialog(
                            viewModel = viewModel,
                            onDismiss = { finish() },
                            onSuccess = {
                                QuickAddWidgetReceiver.requestUpdate(this@QuickEntryActivity)
                                finish()
                            }
                        )
                        ACTION_ADD_EXPENSE -> QuickExpenseDialog(
                            viewModel = viewModel,
                            onDismiss = { finish() },
                            onSuccess = {
                                QuickAddWidgetReceiver.requestUpdate(this@QuickEntryActivity)
                                finish()
                            }
                        )
                    }
                }
            }
        }
    }

    companion object {
        private const val EXTRA_ACTION = "action"
        private const val EXTRA_CAR_ID = "car_id"
        const val ACTION_ADD_REFILL = "add_refill"
        const val ACTION_ADD_EXPENSE = "add_expense"

        fun createRefillIntent(context: Context, carId: Long = -1L): Intent {
            return Intent(context, QuickEntryActivity::class.java).apply {
                putExtra(EXTRA_ACTION, ACTION_ADD_REFILL)
                if (carId != -1L) {
                    putExtra(EXTRA_CAR_ID, carId)
                }
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
        }

        fun createExpenseIntent(context: Context, carId: Long = -1L): Intent {
            return Intent(context, QuickEntryActivity::class.java).apply {
                putExtra(EXTRA_ACTION, ACTION_ADD_EXPENSE)
                if (carId != -1L) {
                    putExtra(EXTRA_CAR_ID, carId)
                }
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuickRefillDialog(
    viewModel: QuickEntryViewModel,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    val context = LocalContext.current
    var liters by remember { mutableStateOf("") }
    var cost by remember { mutableStateOf("") }
    var distance by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val allCars by viewModel.allCars.collectAsState()
    val selectedCar by viewModel.selectedCar.collectAsState()
    var showCarError by remember { mutableStateOf(false) }

    // Calculated values
    val pricePerLiter by remember {
        derivedStateOf {
            val l = liters.toDoubleOrNull()
            val c = cost.toDoubleOrNull()
            if (l != null && c != null && l > 0) c / l else null
        }
    }

    val consumption by remember {
        derivedStateOf {
            val l = liters.toDoubleOrNull()
            val d = distance.toDoubleOrNull()
            if (l != null && d != null && d > 0) (l / d) * 100 else null
        }
    }

    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    // Get dimension resources for responsive layout
    val maxWidth = context.resources.getDimensionPixelSize(R.dimen.quick_entry_dialog_max_width)
    val horizontalPadding = context.resources.getDimensionPixelSize(R.dimen.quick_entry_dialog_horizontal_padding)
    val verticalPadding = context.resources.getDimensionPixelSize(R.dimen.quick_entry_dialog_vertical_padding)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .widthIn(max = with(androidx.compose.ui.platform.LocalDensity.current) { maxWidth.toDp() })
                    .fillMaxWidth()
                    .wrapContentHeight(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .padding(
                            horizontal = with(androidx.compose.ui.platform.LocalDensity.current) { horizontalPadding.toDp() },
                            vertical = with(androidx.compose.ui.platform.LocalDensity.current) { verticalPadding.toDp() }
                        )
                        .verticalScroll(rememberScrollState())
                ) {
                    // Header
                    Text(
                        text = stringResource(id = R.string.quick_add_refill_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                Spacer(modifier = Modifier.height(16.dp))

                // Car Selector
                CarSelector(
                    cars = allCars,
                    selectedCar = selectedCar,
                    onCarSelected = {
                        viewModel.selectCar(it)
                        showCarError = false
                    },
                    showError = showCarError,
                    enabled = !isLoading
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Date field
                OutlinedTextField(
                    value = dateFormatter.format(Date(selectedDate)),
                    onValueChange = { },
                    label = { Text(stringResource(id = R.string.date_label)) },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.DateRange, stringResource(id = R.string.select_date))
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Liters field
                OutlinedTextField(
                    value = liters,
                    onValueChange = { liters = it },
                    label = { Text(stringResource(id = R.string.liters_label)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Cost field
                OutlinedTextField(
                    value = cost,
                    onValueChange = { cost = it },
                    label = { Text(stringResource(id = R.string.cost_label)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Distance field
                OutlinedTextField(
                    value = distance,
                    onValueChange = { distance = it },
                    label = { Text(stringResource(id = R.string.distance_label)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                )

                // Info section with calculations
                if (pricePerLiter != null || consumption != null) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(id = R.string.calculated_info_title),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            pricePerLiter?.let { price ->
                                InfoRow(
                                    label = stringResource(id = R.string.price_per_liter_label),
                                    value = String.format(Locale.getDefault(), "€%.3f", price)
                                )
                            }

                            consumption?.let { cons ->
                                if (pricePerLiter != null) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                }
                                InfoRow(
                                    label = stringResource(id = R.string.fuel_consumption_label),
                                    value = String.format(Locale.getDefault(), "%.2f L/100km", cons)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        enabled = !isLoading
                    ) {
                        Text(stringResource(id = R.string.cancel))
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            // Validate car selection first
                            if (selectedCar == null) {
                                showCarError = true
                                return@Button
                            }

                            val litersValue = liters.toDoubleOrNull()
                            val costValue = cost.toDoubleOrNull()
                            val distanceValue = distance.toDoubleOrNull() ?: 0.0

                            if (litersValue != null && costValue != null) {
                                isLoading = true
                                viewModel.saveQuickRefill(
                                    liters = litersValue,
                                    cost = costValue,
                                    distance = distanceValue,
                                    timestamp = selectedDate,
                                    onSuccess = onSuccess,
                                    onError = { isLoading = false }
                                )
                            }
                        },
                        enabled = !isLoading && liters.isNotBlank() && cost.isNotBlank()
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(stringResource(id = R.string.add))
                        }
                    }
                }
            }
        }
        }
    }

    // Date picker dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDate)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { selectedDate = it }
                        showDatePicker = false
                    }
                ) {
                    Text(stringResource(id = R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(id = R.string.cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuickExpenseDialog(
    viewModel: QuickEntryViewModel,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    val context = LocalContext.current
    var cost by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(ExpenseCategories.predefined.first()) }
    var notes by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val allCars by viewModel.allCars.collectAsState()
    val selectedCar by viewModel.selectedCar.collectAsState()
    var showCarError by remember { mutableStateOf(false) }

    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    // Get dimension resources for responsive layout
    val maxWidth = context.resources.getDimensionPixelSize(R.dimen.quick_entry_dialog_max_width)
    val horizontalPadding = context.resources.getDimensionPixelSize(R.dimen.quick_entry_dialog_horizontal_padding)
    val verticalPadding = context.resources.getDimensionPixelSize(R.dimen.quick_entry_dialog_vertical_padding)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .widthIn(max = with(androidx.compose.ui.platform.LocalDensity.current) { maxWidth.toDp() })
                    .fillMaxWidth()
                    .wrapContentHeight(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .padding(
                            horizontal = with(androidx.compose.ui.platform.LocalDensity.current) { horizontalPadding.toDp() },
                            vertical = with(androidx.compose.ui.platform.LocalDensity.current) { verticalPadding.toDp() }
                        )
                        .verticalScroll(rememberScrollState())
                ) {
                    // Header
                    Text(
                        text = stringResource(id = R.string.quick_add_expense_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                Spacer(modifier = Modifier.height(16.dp))

                // Car Selector
                CarSelector(
                    cars = allCars,
                    selectedCar = selectedCar,
                    onCarSelected = {
                        viewModel.selectCar(it)
                        showCarError = false
                    },
                    showError = showCarError,
                    enabled = !isLoading
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Date field
                OutlinedTextField(
                    value = dateFormatter.format(Date(selectedDate)),
                    onValueChange = { },
                    label = { Text(stringResource(id = R.string.date_label)) },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.DateRange, stringResource(id = R.string.select_date))
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Cost field
                OutlinedTextField(
                    value = cost,
                    onValueChange = { cost = it },
                    label = { Text(stringResource(id = R.string.cost_label)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Category selector
                Text(
                    text = stringResource(id = R.string.category_label),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ExpenseCategories.predefined.forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat) },
                            enabled = !isLoading
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Notes field
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text(stringResource(id = R.string.notes_label)) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading,
                    minLines = 2,
                    maxLines = 3
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Action buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = onDismiss,
                        enabled = !isLoading
                    ) {
                        Text(stringResource(id = R.string.cancel))
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            // Validate car selection first
                            if (selectedCar == null) {
                                showCarError = true
                                return@Button
                            }

                            val costValue = cost.toDoubleOrNull()

                            if (costValue != null) {
                                isLoading = true
                                viewModel.saveQuickExpense(
                                    cost = costValue,
                                    category = category,
                                    notes = notes.ifBlank { null },
                                    timestamp = selectedDate,
                                    onSuccess = onSuccess,
                                    onError = { isLoading = false }
                                )
                            }
                        },
                        enabled = !isLoading && cost.isNotBlank()
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(stringResource(id = R.string.add))
                        }
                    }
                }
            }
        }
        } // Close Box
    } // Close Dialog

    // Date picker dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = selectedDate)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { selectedDate = it }
                        showDatePicker = false
                    }
                ) {
                    Text(stringResource(id = R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(id = R.string.cancel))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CarSelector(
    cars: List<com.agcoding.cartrackingapp.domain.model.Car>,
    selectedCar: com.agcoding.cartrackingapp.domain.model.Car?,
    onCarSelected: (com.agcoding.cartrackingapp.domain.model.Car) -> Unit,
    showError: Boolean,
    enabled: Boolean
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { if (enabled) expanded = !expanded }
        ) {
            OutlinedTextField(
                value = selectedCar?.name ?: "",
                onValueChange = { },
                readOnly = true,
                label = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(stringResource(id = R.string.select_vehicle))
                        Text(" *", color = MaterialTheme.colorScheme.error)
                    }
                },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                },
                colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                enabled = enabled,
                isError = showError && selectedCar == null
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                if (cars.isEmpty()) {
                    DropdownMenuItem(
                        text = { Text(stringResource(id = R.string.no_cars_available)) },
                        onClick = { },
                        enabled = false
                    )
                } else {
                    cars.forEach { car ->
                        DropdownMenuItem(
                            text = { Text(car.name) },
                            onClick = {
                                onCarSelected(car)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }

        if (showError && selectedCar == null) {
            Text(
                text = stringResource(id = R.string.error_car_required),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}
