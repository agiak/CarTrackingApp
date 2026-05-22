package com.agcoding.cartrackingapp.widget

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
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
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.style.TextAlign
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
                            onSuccess = { amount, timestamp ->
                                // Get selected car
                                val car = viewModel.selectedCar.value
                                if (car != null) {
                                    // Create result intent with transaction data
                                    val resultIntent = createResultIntent(
                                        type = "Refill",
                                        amount = amount,
                                        timestamp = timestamp,
                                        carName = car.name,
                                        carId = car.id
                                    )
                                    setResult(RESULT_OK, resultIntent)

                                    // Send broadcast with transaction data for widget update
                                    val broadcastIntent = Intent(ACTION_WIDGET_DATA_CHANGED).apply {
                                        putExtra(RESULT_TRANSACTION_TYPE, "Refill")
                                        putExtra(RESULT_TRANSACTION_AMOUNT, amount)
                                        putExtra(RESULT_TRANSACTION_TIMESTAMP, timestamp)
                                        putExtra(RESULT_TRANSACTION_CAR_NAME, car.name)
                                        putExtra(RESULT_TRANSACTION_CAR_ID, car.id)
                                    }
                                    sendBroadcast(broadcastIntent)
                                }
                                finish()
                            }
                        )

                        ACTION_VOICE -> QuickRefillDialog(
                            viewModel = viewModel,
                            onDismiss = { finish() },
                            onSuccess = { amount, timestamp ->
                                // Get selected car
                                val car = viewModel.selectedCar.value
                                if (car != null) {
                                    // Create result intent with transaction data
                                    val resultIntent = createResultIntent(
                                        type = "Refill",
                                        amount = amount,
                                        timestamp = timestamp,
                                        carName = car.name,
                                        carId = car.id
                                    )
                                    setResult(RESULT_OK, resultIntent)

                                    // Send broadcast with transaction data for widget update
                                    val broadcastIntent = Intent(ACTION_WIDGET_DATA_CHANGED).apply {
                                        putExtra(RESULT_TRANSACTION_TYPE, "Refill")
                                        putExtra(RESULT_TRANSACTION_AMOUNT, amount)
                                        putExtra(RESULT_TRANSACTION_TIMESTAMP, timestamp)
                                        putExtra(RESULT_TRANSACTION_CAR_NAME, car.name)
                                        putExtra(RESULT_TRANSACTION_CAR_ID, car.id)
                                    }
                                    sendBroadcast(broadcastIntent)
                                }
                                finish()
                            }
                        )

                        ACTION_ADD_EXPENSE -> QuickExpenseDialog(
                            viewModel = viewModel,
                            onDismiss = { finish() },
                            onSuccess = { amount, timestamp ->
                                // Get selected car
                                val car = viewModel.selectedCar.value
                                if (car != null) {
                                    // Create result intent with transaction data
                                    val resultIntent = createResultIntent(
                                        type = "Expense",
                                        amount = amount,
                                        timestamp = timestamp,
                                        carName = car.name,
                                        carId = car.id
                                    )
                                    setResult(RESULT_OK, resultIntent)

                                    // Send broadcast with transaction data for widget update
                                    val broadcastIntent = Intent(ACTION_WIDGET_DATA_CHANGED).apply {
                                        putExtra(RESULT_TRANSACTION_TYPE, "Expense")
                                        putExtra(RESULT_TRANSACTION_AMOUNT, amount)
                                        putExtra(RESULT_TRANSACTION_TIMESTAMP, timestamp)
                                        putExtra(RESULT_TRANSACTION_CAR_NAME, car.name)
                                        putExtra(RESULT_TRANSACTION_CAR_ID, car.id)
                                    }
                                    sendBroadcast(broadcastIntent)
                                }
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
        const val ACTION_VOICE = "voice_refill"
        const val ACTION_WIDGET_DATA_CHANGED =
            "com.agcoding.cartrackingapp.ACTION_WIDGET_DATA_CHANGED"

        // Result data keys
        const val RESULT_TRANSACTION_TYPE = "transaction_type"
        const val RESULT_TRANSACTION_AMOUNT = "transaction_amount"
        const val RESULT_TRANSACTION_TIMESTAMP = "transaction_timestamp"
        const val RESULT_TRANSACTION_CAR_NAME = "transaction_car_name"
        const val RESULT_TRANSACTION_CAR_ID = "transaction_car_id"

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

        fun createVoiceIntent(context: Context, carId: Long = -1L): Intent {
            return Intent(context, QuickEntryActivity::class.java).apply {
                putExtra(EXTRA_ACTION, ACTION_VOICE)
                if (carId != -1L) {
                    putExtra(EXTRA_CAR_ID, carId)
                }
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
        }

        /**
         * Helper to create result intent with transaction data
         */
        fun createResultIntent(
            type: String,
            amount: Double,
            timestamp: Long,
            carName: String,
            carId: Long
        ): Intent {
            return Intent().apply {
                putExtra(RESULT_TRANSACTION_TYPE, type)
                putExtra(RESULT_TRANSACTION_AMOUNT, amount)
                putExtra(RESULT_TRANSACTION_TIMESTAMP, timestamp)
                putExtra(RESULT_TRANSACTION_CAR_NAME, carName)
                putExtra(RESULT_TRANSACTION_CAR_ID, carId)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarSelector(
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
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, enabled),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuickRefillDialog(
    viewModel: QuickEntryViewModel,
    onDismiss: () -> Unit,
    onSuccess: (amount: Double, timestamp: Long) -> Unit
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

    // Error states
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var litersError by remember { mutableStateOf<String?>(null) }
    var costError by remember { mutableStateOf<String?>(null) }
    var distanceError by remember { mutableStateOf<String?>(null) }

    // Voice state
    val voiceState by viewModel.voiceState.collectAsState()
    val isVoiceAvailable = viewModel.isVoiceAvailable

    // Handle voice parsed data - auto-fill form fields
    androidx.compose.runtime.LaunchedEffect(voiceState) {
        if (voiceState is com.agcoding.cartrackingapp.presentation.refill.VoiceRefillState.Parsed) {
            val parsedData = (voiceState as com.agcoding.cartrackingapp.presentation.refill.VoiceRefillState.Parsed).data

            // Pre-fill form fields with parsed data
            // Use Locale.US to ensure period (.) as decimal separator for parsing compatibility
            parsedData.cost?.let { cost = String.format(Locale.US, "%.2f", it) }
            parsedData.liters?.let { liters = String.format(Locale.US, "%.2f", it) }
            parsedData.distance?.let { distance = String.format(Locale.US, "%.0f", it) }

            // Reset voice state after applying data
            viewModel.confirmVoiceParsedData()
        }
    }

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
    val horizontalPadding =
        context.resources.getDimensionPixelSize(R.dimen.quick_entry_dialog_horizontal_padding)
    val verticalPadding =
        context.resources.getDimensionPixelSize(R.dimen.quick_entry_dialog_vertical_padding)

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

                    // Voice Entry Section
                    if (isVoiceAvailable) when (voiceState) {
                        is com.agcoding.cartrackingapp.presentation.refill.VoiceRefillState.Idle -> {
                            // Show voice button
                            OutlinedButton(
                                onClick = { viewModel.startVoiceEntry() },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !isLoading
                            ) {
                                Icon(
                                    painter = androidx.compose.ui.res.painterResource(R.drawable.ic_mic),
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(stringResource(R.string.voice_entry_button))
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        is com.agcoding.cartrackingapp.presentation.refill.VoiceRefillState.Listening -> {
                            // Listening state - show partial text and stop button
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            painter = androidx.compose.ui.res.painterResource(R.drawable.ic_mic),
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = stringResource(R.string.voice_listening),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    val partialText = (voiceState as com.agcoding.cartrackingapp.presentation.refill.VoiceRefillState.Listening).partialText
                                    if (partialText.isNotBlank()) {
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = partialText,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = { viewModel.cancelVoiceEntry() },
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(stringResource(R.string.cancel))
                                        }

                                        Button(
                                            onClick = { viewModel.stopVoiceRecording() },
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(stringResource(R.string.voice_stop))
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        is com.agcoding.cartrackingapp.presentation.refill.VoiceRefillState.Processing -> {
                            // Processing state
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = stringResource(R.string.voice_processing),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        is com.agcoding.cartrackingapp.presentation.refill.VoiceRefillState.Error -> {
                            // Error state
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer
                                )
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.voice_error),
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )

                                    val errorMessage = (voiceState as com.agcoding.cartrackingapp.presentation.refill.VoiceRefillState.Error).message
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = errorMessage,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        OutlinedButton(
                                            onClick = { viewModel.cancelVoiceEntry() },
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(stringResource(R.string.cancel))
                                        }

                                        Button(
                                            onClick = { viewModel.startVoiceEntry() },
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text(stringResource(R.string.retry))
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        is com.agcoding.cartrackingapp.presentation.refill.VoiceRefillState.Parsed -> {
                            // Success state - fields will be auto-filled
                            // Just show brief success message
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = stringResource(R.string.voice_parsed_success),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }

                    // Car Selector - Only show if there are multiple cars
                    if (allCars.size > 1) {
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
                    }

                    // Date field
                    OutlinedTextField(
                        value = dateFormatter.format(Date(selectedDate)),
                        onValueChange = { },
                        label = { Text(stringResource(id = R.string.date_label)) },
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { showDatePicker = true }) {
                                Icon(
                                    Icons.Default.DateRange,
                                    stringResource(id = R.string.select_date)
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Liters field
                    OutlinedTextField(
                        value = liters,
                        onValueChange = {
                            liters = it
                            litersError = null
                            errorMessage = null
                        },
                        label = { Text(stringResource(id = R.string.liters_label)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading,
                        isError = litersError != null,
                        supportingText = litersError?.let { { Text(it) } }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Cost field
                    OutlinedTextField(
                        value = cost,
                        onValueChange = {
                            cost = it
                            costError = null
                            errorMessage = null
                        },
                        label = { Text(stringResource(id = R.string.cost_label)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading,
                        isError = costError != null,
                        supportingText = costError?.let { { Text(it) } }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Distance field
                    OutlinedTextField(
                        value = distance,
                        onValueChange = {
                            distance = it
                            distanceError = null
                            errorMessage = null
                        },
                        label = { Text(stringResource(id = R.string.distance_label)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading,
                        isError = distanceError != null,
                        supportingText = distanceError?.let { { Text(it) } }
                    )

                    // Info section with calculations
                    if (pricePerLiter != null || consumption != null) {
                        Spacer(modifier = Modifier.height(16.dp))

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(
                                    alpha = 0.3f
                                )
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
                                        value = String.format(
                                            Locale.getDefault(),
                                            "%.2f L/100km",
                                            cons
                                        )
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Error message display
                    errorMessage?.let { error ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = error,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

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
                                // Clear previous errors
                                litersError = null
                                costError = null
                                distanceError = null
                                errorMessage = null

                                // Validate car selection first
                                if (selectedCar == null) {
                                    showCarError = true
                                    errorMessage = context.getString(R.string.error_car_required)
                                    return@Button
                                }

                                // Validate using RefillValidator
                                val validationResult = com.agcoding.cartrackingapp.domain.validation.RefillValidator.validateRefill(
                                    context = context,
                                    liters = liters,
                                    cost = cost,
                                    distance = if (distance.isBlank()) "0" else distance
                                )

                                if (!validationResult.isValid) {
                                    // Show field-specific errors
                                    validationResult.errors["liters"]?.let { litersError = it }
                                    validationResult.errors["cost"]?.let { costError = it }
                                    validationResult.errors["distance"]?.let { distanceError = it }
                                    validationResult.errors["consumption"]?.let {
                                        errorMessage = it
                                    }

                                    // If no field-specific error, show general error
                                    if (errorMessage == null && validationResult.errors.isNotEmpty()) {
                                        errorMessage = validationResult.errors.values.first()
                                    }
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
                                        onError = {
                                            isLoading = false
                                            errorMessage = context.getString(R.string.error_saving_refill)
                                        }
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
    onSuccess: (amount: Double, timestamp: Long) -> Unit
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

    // Error states
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var costError by remember { mutableStateOf<String?>(null) }

    val dateFormatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    // Get dimension resources for responsive layout
    val maxWidth = context.resources.getDimensionPixelSize(R.dimen.quick_entry_dialog_max_width)
    val horizontalPadding =
        context.resources.getDimensionPixelSize(R.dimen.quick_entry_dialog_horizontal_padding)
    val verticalPadding =
        context.resources.getDimensionPixelSize(R.dimen.quick_entry_dialog_vertical_padding)

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

                    // Car Selector - Only show if there are multiple cars
                    if (allCars.size > 1) {
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
                    }

                    // Date field
                    OutlinedTextField(
                        value = dateFormatter.format(Date(selectedDate)),
                        onValueChange = { },
                        label = { Text(stringResource(id = R.string.date_label)) },
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { showDatePicker = true }) {
                                Icon(
                                    Icons.Default.DateRange,
                                    stringResource(id = R.string.select_date)
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Cost field
                    OutlinedTextField(
                        value = cost,
                        onValueChange = {
                            cost = it
                            costError = null
                            errorMessage = null
                        },
                        label = { Text(stringResource(id = R.string.cost_label)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isLoading,
                        isError = costError != null,
                        supportingText = costError?.let { { Text(it) } }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Category selector
                    Text(
                        text = stringResource(id = R.string.category_label),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    androidx.compose.foundation.layout.FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ExpenseCategories.predefined.forEach { cat ->
                            FilterChip(
                                selected = category == cat,
                                onClick = { category = cat },
                                label = { Text(cat, style = MaterialTheme.typography.labelSmall) },
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

                    // Error message display
                    errorMessage?.let { error ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.Info,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = error,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

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
                                // Clear previous errors
                                costError = null
                                errorMessage = null

                                // Validate car selection first
                                if (selectedCar == null) {
                                    showCarError = true
                                    errorMessage = context.getString(R.string.error_car_required)
                                    return@Button
                                }

                                val costValue = cost.toDoubleOrNull()

                                // Validate cost
                                if (costValue == null) {
                                    costError = context.getString(R.string.error_cost_invalid)
                                    return@Button
                                }
                                if (costValue <= 0) {
                                    costError = context.getString(R.string.error_cost_positive)
                                    return@Button
                                }

                                isLoading = true
                                viewModel.saveQuickExpense(
                                    cost = costValue,
                                    category = category,
                                    notes = notes.ifBlank { null },
                                    timestamp = selectedDate,
                                    onSuccess = onSuccess,
                                    onError = {
                                        isLoading = false
                                        errorMessage = context.getString(R.string.error_saving_expense)
                                    }
                                )
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
}

/**
 * Voice Entry Dialog for quick widget voice input
 * Full implementation with voice recognition, parsing, and pre-fill
 */
@Composable
private fun QuickVoiceDialog(
    viewModel: QuickEntryViewModel,
    onDismiss: () -> Unit,
    onSuccess: (amount: Double, timestamp: Long) -> Unit
) {
    val context = LocalContext.current
    val voiceState by viewModel.voiceState.collectAsState()
    val allCars by viewModel.allCars.collectAsState()
    val selectedCar by viewModel.selectedCar.collectAsState()

    // Auto-start voice recognition when dialog opens
    androidx.compose.runtime.LaunchedEffect(Unit) {
        if (voiceState is com.agcoding.cartrackingapp.presentation.refill.VoiceRefillState.Idle) {
            viewModel.startVoiceEntry()
        }
    }

    Dialog(
        onDismissRequest = {
            viewModel.cancelVoiceEntry()
            onDismiss()
        },
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .wrapContentHeight()
                .padding(16.dp)
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    when (voiceState) {
                        is com.agcoding.cartrackingapp.presentation.refill.VoiceRefillState.Idle -> {
                            // Starting state
                            CircularProgressIndicator()
                            Text(
                                text = stringResource(R.string.voice_initializing),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        is com.agcoding.cartrackingapp.presentation.refill.VoiceRefillState.Listening -> {
                            // Listening state
                            Icon(
                                painter = androidx.compose.ui.res.painterResource(R.drawable.ic_mic),
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )

                            Text(
                                text = stringResource(R.string.voice_listening),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )

                            val partialText = (voiceState as com.agcoding.cartrackingapp.presentation.refill.VoiceRefillState.Listening).partialText
                            if (partialText.isNotBlank()) {
                                Text(
                                    text = partialText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        viewModel.cancelVoiceEntry()
                                        onDismiss()
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(stringResource(R.string.cancel))
                                }

                                Button(
                                    onClick = { viewModel.stopVoiceRecording() },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(stringResource(R.string.voice_stop))
                                }
                            }
                        }

                        is com.agcoding.cartrackingapp.presentation.refill.VoiceRefillState.Processing -> {
                            // Processing state
                            CircularProgressIndicator()
                            Text(
                                text = stringResource(R.string.voice_processing),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        is com.agcoding.cartrackingapp.presentation.refill.VoiceRefillState.Parsed -> {
                            // Success - show parsed data and transition to refill form
                            val parsedData = (voiceState as com.agcoding.cartrackingapp.presentation.refill.VoiceRefillState.Parsed).data

                            Text(
                                text = stringResource(R.string.voice_parsed_success),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            // Show parsed values
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                parsedData.cost?.let {
                                    Text("Cost: €%.2f".format(it))
                                }
                                parsedData.liters?.let {
                                    Text("Liters: %.2f L".format(it))
                                }
                                parsedData.distance?.let {
                                    Text("Distance: %.0f km".format(it))
                                }
                            }

                            // Apply parsed data and close
                            androidx.compose.runtime.LaunchedEffect(Unit) {
                                viewModel.confirmVoiceParsedData()
                                kotlinx.coroutines.delay(500) // Brief delay to show success
                                // Get the filled values and save
                                val amount = parsedData.cost ?: 0.0
                                if (amount > 0) {
                                    onSuccess(amount, System.currentTimeMillis())
                                } else {
                                    onDismiss()
                                }
                            }
                        }

                        is com.agcoding.cartrackingapp.presentation.refill.VoiceRefillState.Error -> {
                            // Error state
                            val error = (voiceState as com.agcoding.cartrackingapp.presentation.refill.VoiceRefillState.Error).message

                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.error
                            )

                            Text(
                                text = stringResource(R.string.voice_error),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )

                            Text(
                                text = error,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                TextButton(
                                    onClick = onDismiss,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(stringResource(R.string.cancel))
                                }

                                Button(
                                    onClick = { viewModel.startVoiceEntry() },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(stringResource(R.string.retry))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
