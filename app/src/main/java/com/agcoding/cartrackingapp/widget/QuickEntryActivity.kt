package com.agcoding.cartrackingapp.widget

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.domain.model.Car
import com.agcoding.cartrackingapp.domain.model.ExpenseCategories
import com.agcoding.cartrackingapp.domain.validation.RefillValidator
import com.agcoding.cartrackingapp.presentation.components.AppFilterChip
import com.agcoding.cartrackingapp.presentation.components.AppTextField
import com.agcoding.cartrackingapp.presentation.components.LicensePlateBadge
import com.agcoding.cartrackingapp.presentation.components.PrimaryButton
import com.agcoding.cartrackingapp.presentation.components.SuccessAnimation
import com.agcoding.cartrackingapp.presentation.components.ThousandsSeparatorTransformation
import com.agcoding.cartrackingapp.presentation.components.TonalButton
import com.agcoding.cartrackingapp.presentation.editrefill.components.CalculatedValuesCard
import com.agcoding.cartrackingapp.presentation.refill.VoiceRefillState
import com.agcoding.cartrackingapp.presentation.refill.components.VoiceCapturedFieldsRow
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppColorScheme
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppDimens
import com.agcoding.cartrackingapp.util.formatForDecimalInput
import com.agcoding.cartrackingapp.util.formatMoney
import com.agcoding.cartrackingapp.util.parseLocalizedDouble
import com.agcoding.cartrackingapp.util.sanitizeDecimalInput
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Quick entry: the refill or expense form, opened straight from the home
 * screen without starting the app proper.
 *
 * It is the same two forms as the in-app screens, cut down to what can be
 * typed in a hurry — so it is built from the same pieces: the app's one text
 * field, its calculated-values card, its buttons. It used to be stock Material
 * in a white `surface` card, under `CarTrackingAppTheme`'s *default* palette
 * rather than the one the user picked, which made it the one surface in the
 * app that ignored the theme (see [QuickEntryTheme]).
 */
@AndroidEntryPoint
class QuickEntryActivity : AppCompatActivity() {

    private val viewModel: QuickEntryViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val action = intent.getStringExtra(EXTRA_ACTION) ?: ACTION_ADD_REFILL

        // Optional: the 2×2 widget knows its car, the 1×1 shortcuts do not, and
        // the picker in the form covers the difference.
        val carId = intent.getLongExtra(EXTRA_CAR_ID, -1L)
        if (carId != -1L) {
            viewModel.setCarId(carId)
            viewModel.loadCarName()
        }
        viewModel.loadAllCars()

        setContent {
            QuickEntryTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(LocalAppColorScheme.current.backgroundOverlay),
                    contentAlignment = Alignment.Center
                ) {
                    // Each form plays the confirmation inside its own card and
                    // calls back once it has finished, so by the time we are
                    // here there is nothing left to show.
                    if (action == ACTION_ADD_EXPENSE) {
                        QuickExpenseDialog(
                            viewModel = viewModel,
                            onDismiss = { finish() },
                            onSaved = { amount, timestamp ->
                                complete(TYPE_EXPENSE, amount, timestamp)
                            }
                        )
                    } else {
                        QuickRefillDialog(
                            viewModel = viewModel,
                            // The widget's microphone button opens this form
                            // already listening; anything else opens it quiet.
                            autoStartVoice = action == ACTION_VOICE,
                            onDismiss = { finish() },
                            onSaved = { amount, timestamp ->
                                complete(TYPE_REFILL, amount, timestamp)
                            }
                        )
                    }
                }
            }
        }
    }

    /**
     * Hands the saved transaction back — to whoever started us for a result,
     * and to the widgets over the broadcast they listen on — then closes.
     *
     * The three call sites used to carry a copy of this each, differing only
     * in the word "Refill" or "Expense".
     */
    private fun complete(type: String, amount: Double, timestamp: Long) {
        val car = viewModel.selectedCar.value
        if (car != null) {
            val result = createResultIntent(type, amount, timestamp, car.name, car.id)
            setResult(RESULT_OK, result)
            sendBroadcast(Intent(ACTION_WIDGET_DATA_CHANGED).putExtras(result))
        }
        finish()
    }

    companion object {
        private const val EXTRA_ACTION = "action"
        private const val EXTRA_CAR_ID = "car_id"
        const val ACTION_ADD_REFILL = "add_refill"
        const val ACTION_ADD_EXPENSE = "add_expense"
        const val ACTION_VOICE = "voice_refill"
        const val ACTION_WIDGET_DATA_CHANGED =
            "com.agcoding.cartrackingapp.ACTION_WIDGET_DATA_CHANGED"

        private const val TYPE_REFILL = "Refill"
        private const val TYPE_EXPENSE = "Expense"

        // Result data keys
        const val RESULT_TRANSACTION_TYPE = "transaction_type"
        const val RESULT_TRANSACTION_AMOUNT = "transaction_amount"
        const val RESULT_TRANSACTION_TIMESTAMP = "transaction_timestamp"
        const val RESULT_TRANSACTION_CAR_NAME = "transaction_car_name"
        const val RESULT_TRANSACTION_CAR_ID = "transaction_car_id"

        fun createRefillIntent(context: Context, carId: Long = -1L): Intent =
            entryIntent(context, ACTION_ADD_REFILL, carId)

        fun createExpenseIntent(context: Context, carId: Long = -1L): Intent =
            entryIntent(context, ACTION_ADD_EXPENSE, carId)

        fun createVoiceIntent(context: Context, carId: Long = -1L): Intent =
            entryIntent(context, ACTION_VOICE, carId)

        private fun entryIntent(context: Context, action: String, carId: Long): Intent =
            Intent(context, QuickEntryActivity::class.java).apply {
                putExtra(EXTRA_ACTION, action)
                if (carId != -1L) putExtra(EXTRA_CAR_ID, carId)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
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
        ): Intent = Intent().apply {
            putExtra(RESULT_TRANSACTION_TYPE, type)
            putExtra(RESULT_TRANSACTION_AMOUNT, amount)
            putExtra(RESULT_TRANSACTION_TIMESTAMP, timestamp)
            putExtra(RESULT_TRANSACTION_CAR_NAME, carName)
            putExtra(RESULT_TRANSACTION_CAR_ID, carId)
        }
    }
}

/** Between two fields in the same form, as everywhere else in the redesign. */
private val FieldGap = 14.dp

/** How long the dialog gets to appear before voice entry starts listening. */
private const val VOICE_AUTOSTART_SETTLE_MILLIS = 450L

/** How long "filled from what you said" stays under the voice button. */
private const val VOICE_FILLED_NOTICE_MILLIS = 5_000L

/**
 * The line under the confirmation: which car, and how much.
 *
 * The card is about to close, and from a home-screen widget this is the only
 * chance the user gets to see that the amount landed on the car they meant.
 */
private fun savedDetail(car: Car?, saved: Pair<Double, Long>?): String? {
    val amount = saved?.first ?: return null
    return listOfNotNull(car?.name, amount.formatMoney()).joinToString(" · ")
}

// ─────────────────────────────────────────────────────────────────────────────
// Refill
// ─────────────────────────────────────────────────────────────────────────────

@Composable
private fun QuickRefillDialog(
    viewModel: QuickEntryViewModel,
    autoStartVoice: Boolean,
    onDismiss: () -> Unit,
    onSaved: (amount: Double, timestamp: Long) -> Unit
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

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var litersError by remember { mutableStateOf<String?>(null) }
    var costError by remember { mutableStateOf<String?>(null) }
    var distanceError by remember { mutableStateOf<String?>(null) }

    val voiceState by viewModel.voiceState.collectAsState()
    val isVoiceAvailable = viewModel.isVoiceAvailable

    LaunchedEffect(autoStartVoice, isVoiceAvailable) {
        if (!autoStartVoice || !isVoiceAvailable) return@LaunchedEffect

        // Not in the same frame as the first composition. The recogniser would
        // then be listening before the dialog had finished appearing — and its
        // five-second silence timeout would be running too, so a user who took
        // a breath before speaking got "no speech detected".
        delay(VOICE_AUTOSTART_SETTLE_MILLIS)

        // Read again rather than trusting the value captured before the delay:
        // the voice button is live throughout it, and a user quick enough to
        // press it would have had the recogniser started twice.
        if (viewModel.voiceState.value is VoiceRefillState.Idle) {
            viewModel.startVoiceEntry()
        }
    }

    // Set the moment the voice fills the form, cleared a few seconds later.
    // Without it the panel collapses straight back to the "say it instead"
    // button and a partial result — two figures out of three — looks exactly
    // like nothing having happened.
    var filledByVoice by remember { mutableStateOf(false) }

    // Whatever was heard lands in the fields, as raw comma-decimal text. Whole
    // numbers stay whole ("38", not "38,0") — a decimal only appears when the
    // user actually spoke one.
    LaunchedEffect(voiceState) {
        (voiceState as? VoiceRefillState.Parsed)?.let { parsed ->
            parsed.data.cost?.let { cost = it.formatForDecimalInput() }
            parsed.data.liters?.let { liters = it.formatForDecimalInput() }
            parsed.data.distance?.let { distance = it.formatForDecimalInput() }
            viewModel.confirmVoiceParsedData()
            filledByVoice = true
        }
    }

    LaunchedEffect(filledByVoice) {
        if (filledByVoice) {
            delay(VOICE_FILLED_NOTICE_MILLIS)
            filledByVoice = false
        }
    }

    val canSave by remember {
        derivedStateOf { liters.isNotBlank() && cost.isNotBlank() }
    }

    // Held rather than handed straight back: the card plays the confirmation
    // first, and only then is the saved refill reported to the widget.
    var saved by remember { mutableStateOf<Pair<Double, Long>?>(null) }

    QuickEntryCard(
        title = stringResource(R.string.quick_entry_refill_title),
        icon = Icons.Default.LocalGasStation,
        onDismiss = onDismiss,
        successMessage = if (saved != null) stringResource(R.string.refill_saved) else null,
        successDetail = savedDetail(selectedCar, saved),
        onSuccessFinished = { saved?.let { (amount, timestamp) -> onSaved(amount, timestamp) } }
    ) {
        CarPicker(
            cars = allCars,
            selected = selectedCar,
            enabled = !isLoading,
            isError = showCarError && selectedCar == null,
            onSelect = {
                viewModel.selectCar(it)
                showCarError = false
            }
        )

        if (isVoiceAvailable) {
            Spacer(Modifier.height(FieldGap))
            VoiceSection(
                state = voiceState,
                justFilled = filledByVoice,
                onStart = { viewModel.startVoiceEntry() },
                onStop = { viewModel.stopVoiceRecording() },
                onCancel = { viewModel.cancelVoiceEntry() },
                enabled = !isLoading
            )
        }

        Spacer(Modifier.height(FieldGap))

        DateField(
            timestamp = selectedDate,
            enabled = !isLoading,
            onClick = { showDatePicker = true }
        )

        Spacer(Modifier.height(FieldGap))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            AppTextField(
                value = liters,
                onValueChange = {
                    liters = sanitizeDecimalInput(it)
                    litersError = null
                    errorMessage = null
                },
                label = { Text(stringResource(R.string.liters_label)) },
                suffix = { Text("L") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                visualTransformation = ThousandsSeparatorTransformation(),
                singleLine = true,
                isError = litersError != null,
                supportingText = litersError?.let { { Text(it) } },
                enabled = !isLoading,
                modifier = Modifier.weight(1f)
            )
            AppTextField(
                value = cost,
                onValueChange = {
                    cost = sanitizeDecimalInput(it)
                    costError = null
                    errorMessage = null
                },
                label = { Text(stringResource(R.string.cost_label)) },
                suffix = { Text("€") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                visualTransformation = ThousandsSeparatorTransformation(),
                singleLine = true,
                isError = costError != null,
                supportingText = costError?.let { { Text(it) } },
                enabled = !isLoading,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(FieldGap))

        AppTextField(
            value = distance,
            onValueChange = {
                distance = sanitizeDecimalInput(it)
                distanceError = null
                errorMessage = null
            },
            label = { Text(stringResource(R.string.distance_label)) },
            suffix = { Text("km") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            visualTransformation = ThousandsSeparatorTransformation(),
            singleLine = true,
            isError = distanceError != null,
            supportingText = distanceError?.let { { Text(it) } },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        )

        // Price per litre and consumption, in the same grid the edit-refill
        // screen shows them in. Absent until there is something honest to put
        // in it.
        CalculatedValuesCard(
            amountPaid = cost,
            litersAdded = liters,
            tripDistance = distance
        )

        ErrorNotice(errorMessage)

        Spacer(Modifier.height(20.dp))

        DialogActions(
            confirmLabel = stringResource(R.string.add),
            confirmEnabled = canSave,
            isBusy = isLoading,
            onCancel = onDismiss,
            onConfirm = {
                litersError = null
                costError = null
                distanceError = null
                errorMessage = null

                if (selectedCar == null) {
                    showCarError = true
                    errorMessage = context.getString(R.string.error_car_required)
                    return@DialogActions
                }

                val validation = RefillValidator.validateRefill(
                    context = context,
                    liters = liters,
                    cost = cost,
                    distance = distance.ifBlank { "0" }
                )

                if (!validation.isValid) {
                    litersError = validation.errors["liters"]
                    costError = validation.errors["cost"]
                    distanceError = validation.errors["distance"]
                    errorMessage = validation.errors["consumption"]
                        ?: validation.errors.values.firstOrNull()
                    return@DialogActions
                }

                val litersValue = liters.parseLocalizedDouble()
                val costValue = cost.parseLocalizedDouble()
                if (litersValue != null && costValue != null) {
                    isLoading = true
                    viewModel.saveQuickRefill(
                        liters = litersValue,
                        cost = costValue,
                        distance = distance.parseLocalizedDouble() ?: 0.0,
                        timestamp = selectedDate,
                        onSuccess = { amount, timestamp -> saved = amount to timestamp },
                        onError = {
                            isLoading = false
                            errorMessage = context.getString(R.string.error_saving_refill)
                        }
                    )
                }
            }
        )
    }

    DatePickerSheet(
        visible = showDatePicker,
        timestamp = selectedDate,
        onDismiss = { showDatePicker = false },
        onPicked = { selectedDate = it }
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Expense
// ─────────────────────────────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun QuickExpenseDialog(
    viewModel: QuickEntryViewModel,
    onDismiss: () -> Unit,
    onSaved: (amount: Double, timestamp: Long) -> Unit
) {
    val context = LocalContext.current
    val dimens = LocalAppDimens.current

    var cost by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(ExpenseCategories.predefined.first()) }
    var notes by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val allCars by viewModel.allCars.collectAsState()
    val selectedCar by viewModel.selectedCar.collectAsState()
    var showCarError by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var costError by remember { mutableStateOf<String?>(null) }

    // See the refill form: confirmation first, report second.
    var saved by remember { mutableStateOf<Pair<Double, Long>?>(null) }

    QuickEntryCard(
        title = stringResource(R.string.quick_entry_expense_title),
        icon = Icons.Default.Receipt,
        onDismiss = onDismiss,
        successMessage = if (saved != null) stringResource(R.string.expense_saved) else null,
        successDetail = savedDetail(selectedCar, saved),
        onSuccessFinished = { saved?.let { (amount, timestamp) -> onSaved(amount, timestamp) } }
    ) {
        CarPicker(
            cars = allCars,
            selected = selectedCar,
            enabled = !isLoading,
            isError = showCarError && selectedCar == null,
            onSelect = {
                viewModel.selectCar(it)
                showCarError = false
            }
        )

        Spacer(Modifier.height(FieldGap))

        DateField(
            timestamp = selectedDate,
            enabled = !isLoading,
            onClick = { showDatePicker = true }
        )

        Spacer(Modifier.height(FieldGap))

        AppTextField(
            value = cost,
            onValueChange = {
                cost = sanitizeDecimalInput(it)
                costError = null
                errorMessage = null
            },
            label = { Text(stringResource(R.string.cost_label)) },
            suffix = { Text("€") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            visualTransformation = ThousandsSeparatorTransformation(),
            singleLine = true,
            isError = costError != null,
            supportingText = costError?.let { { Text(it) } },
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(FieldGap))

        Text(
            text = stringResource(R.string.category_label),
            style = MaterialTheme.typography.labelLarge,
            color = LocalAppColorScheme.current.contentSecondary
        )
        Spacer(Modifier.height(8.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            ExpenseCategories.predefined.forEach { option ->
                AppFilterChip(
                    label = option,
                    selected = category == option,
                    onClick = { category = option },
                    enabled = !isLoading
                )
            }
        }

        Spacer(Modifier.height(dimens.spacing.md))

        AppTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text(stringResource(R.string.notes_label)) },
            minLines = 2,
            maxLines = 3,
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth()
        )

        ErrorNotice(errorMessage)

        Spacer(Modifier.height(20.dp))

        DialogActions(
            confirmLabel = stringResource(R.string.add),
            confirmEnabled = cost.isNotBlank(),
            isBusy = isLoading,
            onCancel = onDismiss,
            onConfirm = {
                costError = null
                errorMessage = null

                if (selectedCar == null) {
                    showCarError = true
                    errorMessage = context.getString(R.string.error_car_required)
                    return@DialogActions
                }

                val costValue = cost.parseLocalizedDouble()
                when {
                    costValue == null ->
                        costError = context.getString(R.string.error_cost_invalid)

                    costValue <= 0 ->
                        costError = context.getString(R.string.error_cost_positive)

                    else -> {
                        isLoading = true
                        viewModel.saveQuickExpense(
                            cost = costValue,
                            category = category,
                            notes = notes.ifBlank { null },
                            timestamp = selectedDate,
                            onSuccess = { amount, timestamp -> saved = amount to timestamp },
                            onError = {
                                isLoading = false
                                errorMessage = context.getString(R.string.error_saving_expense)
                            }
                        )
                    }
                }
            }
        )
    }

    DatePickerSheet(
        visible = showDatePicker,
        timestamp = selectedDate,
        onDismiss = { showDatePicker = false },
        onPicked = { selectedDate = it }
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Shared pieces
// ─────────────────────────────────────────────────────────────────────────────

/**
 * The card both forms live in: icon tile, title, and a line saying where this
 * came from, over a scrolling body.
 *
 * It is capped at [R.dimen.quick_entry_dialog_max_width] so that on a tablet
 * the form does not stretch into a banner.
 */
@Composable
private fun QuickEntryCard(
    title: String,
    icon: ImageVector,
    onDismiss: () -> Unit,
    /**
     * Non-null once the save has gone through. The card then plays the app's
     * success confirmation in place of the form and calls
     * [onSuccessFinished] when it settles.
     */
    successMessage: String? = null,
    /** What was saved, under the message: "Toyota Corolla · 64,40 €". */
    successDetail: String? = null,
    onSuccessFinished: () -> Unit = {},
    content: @Composable ColumnScope.() -> Unit
) {
    val scheme = LocalAppColorScheme.current
    val dimens = LocalAppDimens.current
    val context = LocalContext.current
    val density = LocalDensity.current
    val maxWidth = with(density) {
        context.resources.getDimensionPixelSize(R.dimen.quick_entry_dialog_max_width).toDp()
    }
    val shape = RoundedCornerShape(dimens.radius.sheet)

    // The height the form settled at, so the confirmation can be played at
    // exactly the size of the card the user is already looking at. Without it
    // the card collapses to the height of a checkmark at the moment of saving,
    // which reads as the dialog closing and then something else opening.
    var cardHeight by remember { mutableStateOf<Dp?>(null) }

    val showingSuccess = successMessage != null

    Dialog(
        onDismissRequest = { if (!showingSuccess) onDismiss() },
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            // Nothing dismisses the card while the confirmation is playing:
            // closing it early would skip the callback that hands the saved
            // transaction back to the widget.
            dismissOnBackPress = !showingSuccess,
            dismissOnClickOutside = !showingSuccess,
        )
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = 14.dp)
                // A long form — the expense one, with every category chip —
                // would otherwise run under the status bar.
                .safeDrawingPadding()
                .widthIn(max = maxWidth)
                .fillMaxWidth()
                .wrapContentHeight()
                // Filled and outlined in place, then clipped — so the fill is
                // a plain draw rather than something inside a graphics layer,
                // and the clip is there only to bound the scrolling content.
                .background(scheme.backgroundCard, shape)
                .border(1.dp, scheme.borderDefault, shape)
                .clip(shape)
                .onSizeChanged {
                    if (!showingSuccess) {
                        cardHeight = with(density) { it.height.toDp() }
                    }
                }
        ) {
            if (successMessage != null) {
                SuccessAnimation(
                    message = successMessage,
                    supportingText = successDetail,
                    onFinished = onSuccessFinished,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(cardHeight ?: 320.dp)
                )
            } else {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 18.dp, vertical = 20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(RoundedCornerShape(dimens.radius.iconTile))
                                .background(scheme.actionPrimary),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = scheme.actionContent,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(Modifier.width(12.dp))

                        Column {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.titleLarge,
                                color = scheme.contentPrimary
                            )
                            Text(
                                text = stringResource(R.string.quick_entry_from_widget),
                                style = MaterialTheme.typography.bodySmall,
                                color = scheme.contentSecondary
                            )
                        }
                    }

                    Spacer(Modifier.height(18.dp))

                    content()
                }
            }
        }
    }
}

/**
 * Which car this is going on.
 *
 * Always visible, even with one car on file — from a home-screen widget the
 * one thing you cannot check at a glance is what you are about to write to.
 * It only opens a menu when there is actually a choice to make.
 */
@Composable
private fun CarPicker(
    cars: List<Car>,
    selected: Car?,
    enabled: Boolean,
    isError: Boolean,
    onSelect: (Car) -> Unit
) {
    val scheme = LocalAppColorScheme.current
    val dimens = LocalAppDimens.current
    var expanded by remember { mutableStateOf(false) }
    val choosable = enabled && cars.size > 1
    val shape = RoundedCornerShape(dimens.radius.chip)

    Box {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(scheme.backgroundSecondary)
                .border(1.dp, if (isError) scheme.statusError else scheme.borderDefault, shape)
                .then(
                    if (choosable) Modifier.clickable { expanded = true } else Modifier
                )
                .padding(horizontal = 12.dp, vertical = 11.dp)
        ) {
            Icon(
                imageVector = Icons.Default.DirectionsCar,
                contentDescription = null,
                tint = scheme.actionPrimary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = selected?.name ?: stringResource(R.string.select_vehicle),
                style = MaterialTheme.typography.titleSmall,
                color = if (selected == null) scheme.contentSecondary else scheme.contentPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            selected?.let {
                Spacer(Modifier.width(8.dp))
                LicensePlateBadge(it.licensePlate)
            }
            if (choosable) {
                Spacer(Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = scheme.contentSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            if (cars.isEmpty()) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.no_cars_available)) },
                    onClick = { },
                    enabled = false
                )
            } else {
                cars.forEach { car ->
                    DropdownMenuItem(
                        text = { Text(car.name) },
                        trailingIcon = { LicensePlateBadge(car.licensePlate) },
                        onClick = {
                            onSelect(car)
                            expanded = false
                        }
                    )
                }
            }
        }
    }

    if (isError) {
        Spacer(Modifier.height(6.dp))
        Text(
            text = stringResource(R.string.error_car_required),
            style = MaterialTheme.typography.bodySmall,
            color = scheme.statusError
        )
    }
}

@Composable
private fun DateField(timestamp: Long, enabled: Boolean, onClick: () -> Unit) {
    val formatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    Box {
        AppTextField(
            value = formatter.format(Date(timestamp)),
            onValueChange = { },
            label = { Text(stringResource(R.string.date_label)) },
            readOnly = true,
            singleLine = true,
            trailingIcon = {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = stringResource(R.string.select_date)
                )
            },
            enabled = enabled,
            modifier = Modifier.fillMaxWidth()
        )
        // Over the whole field rather than only the icon: a read-only field
        // that opens a picker should open it wherever it is tapped.
        if (enabled) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable(onClick = onClick)
            )
        }
    }
}

/**
 * Voice entry, in four states.
 *
 * At rest it is one button. While it is listening the three pills say which of
 * cost, litres and distance have been heard — recording stops by itself once
 * all three have — and the partial transcript runs underneath.
 */
@Composable
private fun VoiceSection(
    state: VoiceRefillState,
    /** The form was filled from speech a moment ago; say so under the button. */
    justFilled: Boolean,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onCancel: () -> Unit,
    enabled: Boolean
) {
    val scheme = LocalAppColorScheme.current
    val dimens = LocalAppDimens.current

    when (state) {
        is VoiceRefillState.Idle -> Column {
            TonalButton(
                label = stringResource(R.string.quick_entry_voice_prompt),
                onClick = onStart,
                icon = Icons.Default.Mic,
                enabled = enabled,
                contentColor = scheme.actionPrimary,
                modifier = Modifier.fillMaxWidth()
            )
            if (justFilled) {
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = scheme.statusSuccess,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = stringResource(R.string.quick_entry_voice_filled),
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.statusSuccess
                    )
                }
            }
        }

        is VoiceRefillState.Listening -> VoicePanel(
            icon = Icons.Default.Mic,
            title = stringResource(R.string.voice_listening),
            tint = scheme.actionPrimary,
            container = scheme.actionSecondary
        ) {
            VoiceCapturedFieldsRow(captured = state.captured)

            if (state.partialText.isNotBlank()) {
                Spacer(Modifier.height(10.dp))
                Text(
                    text = state.partialText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = scheme.contentPrimary
                )
            }

            Spacer(Modifier.height(dimens.spacing.md))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                TonalButton(
                    label = stringResource(R.string.cancel),
                    onClick = onCancel,
                    modifier = Modifier.weight(1f)
                )
                PrimaryButton(
                    label = stringResource(R.string.voice_stop),
                    onClick = onStop,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        is VoiceRefillState.Processing -> VoicePanel(
            icon = null,
            title = stringResource(R.string.voice_processing),
            tint = scheme.actionPrimary,
            container = scheme.actionSecondary
        ) {
            Text(
                text = state.transcript,
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.contentSecondary
            )
        }

        is VoiceRefillState.Error -> VoicePanel(
            icon = Icons.Default.MicOff,
            title = stringResource(R.string.voice_error),
            tint = scheme.statusError,
            container = scheme.statusErrorSubtle
        ) {
            Text(
                text = state.message,
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.contentSecondary
            )

            Spacer(Modifier.height(dimens.spacing.md))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                TonalButton(
                    label = stringResource(R.string.cancel),
                    onClick = onCancel,
                    modifier = Modifier.weight(1f)
                )
                PrimaryButton(
                    label = stringResource(R.string.retry),
                    onClick = onStart,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Parsed is a flicker: the values are copied into the fields and the
        // state is reset in the same frame, so there is nothing to draw.
        is VoiceRefillState.Parsed -> Unit
    }
}

/**
 * @param container must not be `backgroundSecondary`. The captured-field pills
 *   inside it are drawn on Material's `surfaceVariant`, which this app maps to
 *   exactly that colour — on a panel of the same colour the pills disappear and
 *   the one thing the listening state is for, showing what has been heard,
 *   stops being visible.
 */
@Composable
private fun VoicePanel(
    icon: ImageVector?,
    title: String,
    tint: Color,
    container: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    val scheme = LocalAppColorScheme.current
    val dimens = LocalAppDimens.current
    val shape = RoundedCornerShape(dimens.radius.tile)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(container)
            .border(1.dp, scheme.borderDefault, shape)
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = tint
                )
            }
            Spacer(Modifier.width(10.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = scheme.contentPrimary
            )
        }

        Spacer(Modifier.height(10.dp))

        content()
    }
}

/** Whatever went wrong with the save, in the app's error colour. */
@Composable
private fun ErrorNotice(message: String?) {
    if (message == null) return

    val scheme = LocalAppColorScheme.current
    val dimens = LocalAppDimens.current
    val shape = RoundedCornerShape(dimens.radius.button)

    Spacer(Modifier.height(FieldGap))

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(scheme.statusErrorSubtle)
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Icon(
            imageVector = Icons.Default.ErrorOutline,
            contentDescription = null,
            tint = scheme.statusError,
            modifier = Modifier.size(18.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = scheme.contentPrimary
        )
    }
}

/**
 * Cancel and the confirming action, side by side and of equal width.
 *
 * They used to be a text button and a button huddled in the bottom-right
 * corner — two small targets on a dialog opened one-handed from the home
 * screen, which is the worst place in the app for them.
 */
@Composable
private fun DialogActions(
    confirmLabel: String,
    confirmEnabled: Boolean,
    isBusy: Boolean,
    onCancel: () -> Unit,
    onConfirm: () -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        TonalButton(
            label = stringResource(R.string.cancel),
            onClick = onCancel,
            enabled = !isBusy,
            modifier = Modifier.weight(1f)
        )
        PrimaryButton(
            label = confirmLabel,
            onClick = onConfirm,
            enabled = confirmEnabled,
            isBusy = isBusy,
            modifier = Modifier.weight(1f)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DatePickerSheet(
    visible: Boolean,
    timestamp: Long,
    onDismiss: () -> Unit,
    onPicked: (Long) -> Unit
) {
    if (!visible) return

    val state = rememberDatePickerState(initialSelectedDateMillis = timestamp)

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    state.selectedDateMillis?.let(onPicked)
                    onDismiss()
                }
            ) {
                Text(stringResource(R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    ) {
        DatePicker(state = state)
    }
}

// ============================================
// Preview Composables
// ============================================

private val previewCars = listOf(
    Car(
        id = 1,
        name = "Toyota Corolla",
        licensePlate = "ABC-1234",
        currentOdometer = 124_500.0,
        initialOdometer = 90_000.0
    ),
    Car(
        id = 2,
        name = "Volkswagen Golf",
        licensePlate = "XYZ-9876",
        currentOdometer = 84_000.0,
        initialOdometer = 12_000.0
    )
)

@Preview(name = "Car picker", showBackground = true, widthDp = 390)
@Composable
private fun PreviewCarPicker() {
    CarTrackingAppTheme(darkTheme = true) {
        Column(Modifier.padding(18.dp)) {
            CarPicker(
                cars = previewCars,
                selected = previewCars.first(),
                enabled = true,
                isError = false,
                onSelect = {}
            )
        }
    }
}

@Preview(name = "Car picker — none chosen", showBackground = true, widthDp = 390)
@Composable
private fun PreviewCarPickerError() {
    CarTrackingAppTheme(darkTheme = false) {
        Column(Modifier.padding(18.dp)) {
            CarPicker(
                cars = previewCars,
                selected = null,
                enabled = true,
                isError = true,
                onSelect = {}
            )
        }
    }
}

@Preview(name = "Voice — listening", showBackground = true, widthDp = 390)
@Composable
private fun PreviewVoiceListening() {
    CarTrackingAppTheme(darkTheme = true) {
        Column(Modifier.padding(18.dp)) {
            VoiceSection(
                state = VoiceRefillState.Listening(partialText = "thirty six litres, sixty four euros"),
                justFilled = false,
                onStart = {},
                onStop = {},
                onCancel = {},
                enabled = true
            )
        }
    }
}

@Preview(name = "Voice — error", showBackground = true, widthDp = 390)
@Composable
private fun PreviewVoiceError() {
    CarTrackingAppTheme(darkTheme = false) {
        Column(Modifier.padding(18.dp)) {
            VoiceSection(
                state = VoiceRefillState.Error("No litres or cost found."),
                justFilled = false,
                onStart = {},
                onStop = {},
                onCancel = {},
                enabled = true
            )
        }
    }
}
