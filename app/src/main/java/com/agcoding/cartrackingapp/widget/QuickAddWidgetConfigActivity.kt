package com.agcoding.cartrackingapp.widget

import android.appwidget.AppWidgetManager
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.domain.model.Car
import com.agcoding.cartrackingapp.domain.repository.CarRepository
import com.agcoding.cartrackingapp.presentation.components.GroupedDivider
import com.agcoding.cartrackingapp.presentation.components.GroupedListContainer
import com.agcoding.cartrackingapp.presentation.components.LicensePlateBadge
import com.agcoding.cartrackingapp.presentation.components.PrimaryButton
import com.agcoding.cartrackingapp.presentation.components.SectionLabel
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppColorScheme
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppDimens
import com.agcoding.cartrackingapp.shared.ui.tokens.AppNumbers
import com.agcoding.cartrackingapp.util.formatMoney
import com.agcoding.cartrackingapp.util.formatNumber
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

/**
 * What the widget is for, asked once when it is dropped on the home screen.
 *
 * It used to be a bare list of car names under a stock top bar — no indication
 * of what picking one would do, and no sign of what the widget would end up
 * looking like. Now the choice sits under a live preview of the widget itself,
 * so the two switches below it ("spend", "voice") show their effect as they
 * are flipped rather than describing it.
 */
@AndroidEntryPoint
class QuickAddWidgetConfigActivity : AppCompatActivity() {

    private val viewModel: WidgetConfigViewModel by viewModels()
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Cancelled unless the user actually finishes: a widget whose
        // configuration was abandoned must not be placed.
        setResult(RESULT_CANCELED)

        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        setContent {
            QuickEntryTheme {
                WidgetConfigScreen(
                    viewModel = viewModel,
                    onConfirm = { carId, showSpend, showVoice ->
                        save(carId, showSpend, showVoice)
                        finishConfiguration()
                    },
                    onCancel = { finish() }
                )
            }
        }

        viewModel.loadCars()
    }

    private fun save(carId: Long, showSpend: Boolean, showVoice: Boolean) {
        // commit() rather than apply(): the widget is redrawn as soon as this
        // activity returns, and it reads these very keys.
        applicationContext
            .getSharedPreferences(WIDGET_PREFS_NAME, Context.MODE_PRIVATE)
            .edit(commit = true) {
                putLong(carIdKey(appWidgetId), carId)
                putBoolean(showSpendKey(appWidgetId), showSpend)
                putBoolean(showVoiceKey(appWidgetId), showVoice)
            }
        WidgetDataProvider.saveLastSelectedCarId(applicationContext, carId)
    }

    private fun finishConfiguration() {
        QuickAddWidgetReceiver.updateWidgets(applicationContext)

        setResult(
            RESULT_OK,
            Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        )
        finish()
    }

    companion object {
        const val WIDGET_PREFS_NAME = "quick_add_widget_prefs"

        private fun carIdKey(widgetId: Int) = "widget_${widgetId}_car_id"
        private fun showSpendKey(widgetId: Int) = "widget_${widgetId}_show_spend"
        private fun showVoiceKey(widgetId: Int) = "widget_${widgetId}_show_voice"

        private fun prefs(context: Context) = context.applicationContext
            .getSharedPreferences(WIDGET_PREFS_NAME, Context.MODE_PRIVATE)

        fun getWidgetCarId(context: Context, widgetId: Int): Long =
            prefs(context).getLong(carIdKey(widgetId), -1L)

        /**
         * Both default to on: a widget placed before these switches existed,
         * or restored from a backup, should look like the one in the preview
         * rather than like the stripped-down version.
         */
        fun showsSpend(context: Context, widgetId: Int): Boolean =
            prefs(context).getBoolean(showSpendKey(widgetId), true)

        fun showsVoice(context: Context, widgetId: Int): Boolean =
            prefs(context).getBoolean(showVoiceKey(widgetId), true)

        fun removeWidgetConfig(context: Context, widgetId: Int) {
            prefs(context).edit {
                remove(carIdKey(widgetId))
                remove(showSpendKey(widgetId))
                remove(showVoiceKey(widgetId))
            }
        }
    }
}

@Composable
private fun WidgetConfigScreen(
    viewModel: WidgetConfigViewModel,
    onConfirm: (carId: Long, showSpend: Boolean, showVoice: Boolean) -> Unit,
    onCancel: () -> Unit
) {
    val cars by viewModel.cars.collectAsState()
    val preview by viewModel.preview.collectAsState()
    val scheme = LocalAppColorScheme.current
    val dimens = LocalAppDimens.current

    var selectedCarId by remember { mutableLongStateOf(-1L) }
    var showSpend by remember { mutableStateOf(true) }
    var showVoice by remember { mutableStateOf(true) }

    // The default car if there is one, otherwise the first: the widget always
    // opens with something chosen, so Add is never dead on arrival.
    val selected = cars.firstOrNull { it.id == selectedCarId }
        ?: cars.firstOrNull { it.isDefault }
        ?: cars.firstOrNull()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(scheme.backgroundPrimary)
            // Below the status bar, above the gesture pill. The screen is
            // launched by the launcher rather than from inside the app, so it
            // gets no insets from anywhere else — without this the close
            // button sits under the clock.
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = dimens.spacing.screenHorizontal)
            .padding(top = 12.dp, bottom = 28.dp)
    ) {
        Box(
            modifier = Modifier
                .size(dimens.size.headerAction)
                .clip(CircleShape)
                .clickable(onClick = onCancel),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = stringResource(R.string.cancel),
                tint = scheme.contentPrimary
            )
        }

        Spacer(Modifier.height(10.dp))

        Text(
            text = stringResource(R.string.widget_config_title),
            style = MaterialTheme.typography.headlineSmall,
            color = scheme.contentPrimary
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = stringResource(R.string.widget_config_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = scheme.contentSecondary
        )

        Spacer(Modifier.height(18.dp))

        if (cars.isEmpty()) {
            NoCarsNotice()
        } else {
            // The preview carries the car's real month-to-date spend, so it is
            // worth a look even once the shape of the widget is familiar.
            LaunchedEffect(selected?.id) {
                selected?.let { viewModel.loadPreview(it.id) }
            }

            WidgetPreview(data = preview, showSpend = showSpend, showVoice = showVoice)

            Spacer(Modifier.height(dimens.spacing.sectionTop))
            SectionLabel(stringResource(R.string.widget_config_car))
            Spacer(Modifier.height(10.dp))

            GroupedListContainer {
                cars.forEachIndexed { index, car ->
                    if (index > 0) GroupedDivider(startInset = false)
                    CarChoiceRow(
                        car = car,
                        selected = car.id == selected?.id,
                        onClick = { selectedCarId = car.id }
                    )
                }
            }

            Spacer(Modifier.height(dimens.spacing.sectionTop))
            SectionLabel(stringResource(R.string.widget_config_shown))
            Spacer(Modifier.height(10.dp))

            GroupedListContainer {
                SwitchRow(
                    icon = Icons.Default.LocalGasStation,
                    title = stringResource(R.string.widget_config_spend),
                    subtitle = stringResource(R.string.widget_config_spend_description),
                    checked = showSpend,
                    onCheckedChange = { showSpend = it }
                )
                GroupedDivider(startInset = false)
                SwitchRow(
                    icon = Icons.Default.Mic,
                    title = stringResource(R.string.widget_config_voice),
                    subtitle = stringResource(R.string.widget_config_voice_description),
                    checked = showVoice,
                    onCheckedChange = { showVoice = it }
                )
            }

            Spacer(Modifier.height(24.dp))

            PrimaryButton(
                label = stringResource(R.string.widget_config_add),
                onClick = { selected?.let { onConfirm(it.id, showSpend, showVoice) } },
                enabled = selected != null,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * The widget as it will look.
 *
 * Not a picture: the same values from [WidgetDataProvider] that the widget
 * itself will draw, in the same colour roles, so the two switches underneath
 * show their effect rather than describing it.
 */
@Composable
private fun WidgetPreview(data: WidgetData?, showSpend: Boolean, showVoice: Boolean) {
    val scheme = LocalAppColorScheme.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(26.dp))
            .background(scheme.backgroundSecondary)
            .padding(vertical = 22.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .width(172.dp)
                .height(172.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(scheme.backgroundCard)
                .border(1.dp, scheme.borderDefault, RoundedCornerShape(24.dp))
                .padding(14.dp)
        ) {
            Text(
                text = data?.carName.orEmpty(),
                style = MaterialTheme.typography.titleSmall,
                color = scheme.contentPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (!data?.licensePlate.isNullOrBlank()) {
                Spacer(Modifier.height(4.dp))
                LicensePlateBadge(data?.licensePlate.orEmpty())
            }

            Spacer(Modifier.weight(1f))

            if (showSpend) {
                Text(
                    text = data?.monthLabel.orEmpty(),
                    style = MaterialTheme.typography.labelMedium,
                    color = scheme.contentSecondary
                )
                Text(
                    text = (data?.monthlyTotal ?: 0.0).formatMoney(),
                    style = AppNumbers.value,
                    color = scheme.contentPrimary
                )
                Spacer(Modifier.height(10.dp))
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                PreviewButton(Icons.Default.LocalGasStation, primary = true, modifier = Modifier.weight(1f))
                PreviewButton(Icons.Default.Receipt, primary = false, modifier = Modifier.weight(1f))
                if (showVoice) {
                    PreviewButton(Icons.Default.Mic, primary = false, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun PreviewButton(icon: ImageVector, primary: Boolean, modifier: Modifier = Modifier) {
    val scheme = LocalAppColorScheme.current
    Box(
        modifier = modifier
            .height(40.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(if (primary) scheme.actionPrimary else scheme.backgroundSecondary),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (primary) scheme.actionContent else scheme.actionPrimary,
            modifier = Modifier.size(19.dp)
        )
    }
}

@Composable
private fun CarChoiceRow(car: Car, selected: Boolean, onClick: () -> Unit) {
    val scheme = LocalAppColorScheme.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        RadioDot(selected = selected)
        Spacer(Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = car.name,
                style = MaterialTheme.typography.titleSmall,
                color = scheme.contentPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (car.averageConsumption > 0) {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "${car.averageConsumption.formatNumber(1)} L/100 km",
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.contentSecondary
                )
            }
        }

        LicensePlateBadge(car.licensePlate)
    }
}

/**
 * A radio, drawn the way the rest of the app draws selection: an accent ring
 * with a filled core, rather than Material's own control, which in several of
 * the palettes comes out as a grey circle nobody reads as "chosen".
 */
@Composable
private fun RadioDot(selected: Boolean) {
    val scheme = LocalAppColorScheme.current
    Box(
        modifier = Modifier
            .size(22.dp)
            .clip(CircleShape)
            .border(
                width = 2.dp,
                color = if (selected) scheme.actionPrimary else scheme.contentDisabled,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        if (selected) {
            Box(
                modifier = Modifier
                    .size(11.dp)
                    .clip(CircleShape)
                    .background(scheme.actionPrimary)
            )
        }
    }
}

@Composable
private fun SwitchRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    val scheme = LocalAppColorScheme.current
    val dimens = LocalAppDimens.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(dimens.size.iconTile)
                .clip(RoundedCornerShape(dimens.radius.iconTile))
                .background(scheme.actionSecondary),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = scheme.actionPrimary,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = scheme.contentPrimary
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = scheme.contentSecondary
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = scheme.actionContent,
                checkedTrackColor = scheme.actionPrimary,
            )
        )
    }
}

@Composable
private fun NoCarsNotice() {
    val scheme = LocalAppColorScheme.current
    val dimens = LocalAppDimens.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(dimens.radius.card))
            .background(scheme.backgroundCard)
            .border(1.dp, scheme.borderDefault, RoundedCornerShape(dimens.radius.card))
            .padding(dimens.spacing.cardPadding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(dimens.radius.iconTile))
                .background(scheme.actionSecondary),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.DirectionsCar,
                contentDescription = null,
                tint = scheme.actionPrimary
            )
        }
        Spacer(Modifier.height(12.dp))
        Text(
            text = stringResource(R.string.widget_no_car),
            style = MaterialTheme.typography.titleMedium,
            color = scheme.contentPrimary
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = stringResource(R.string.widget_add_car_first),
            style = MaterialTheme.typography.bodyMedium,
            color = scheme.contentSecondary
        )
    }
}

@HiltViewModel
class WidgetConfigViewModel @Inject constructor(
    private val carRepository: CarRepository,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val _cars = MutableStateFlow<List<Car>>(emptyList())
    val cars: StateFlow<List<Car>> = _cars.asStateFlow()

    /** Exactly what the widget will show for the car currently highlighted. */
    private val _preview = MutableStateFlow<WidgetData?>(null)
    val preview: StateFlow<WidgetData?> = _preview.asStateFlow()

    fun loadCars() {
        viewModelScope.launch {
            try {
                carRepository.getAllCars().collect { _cars.value = it }
            } catch (e: Exception) {
                Timber.e(e, "Error loading cars for the widget configuration")
            }
        }
    }

    fun loadPreview(carId: Long) {
        viewModelScope.launch {
            _preview.value = WidgetDataProvider.getWidgetDataForCar(context, carId)
        }
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
        currentOdometer = 120_097.0,
        initialOdometer = 90_000.0,
        averageConsumption = 7.6,
        isDefault = true,
    ),
    Car(
        id = 2,
        name = "Volkswagen Golf",
        licensePlate = "VWG-9012",
        currentOdometer = 84_000.0,
        initialOdometer = 12_000.0,
        averageConsumption = 7.1,
    ),
)

private val previewData = WidgetData(
    carId = 1,
    carName = "Toyota Corolla",
    licensePlate = "ABC-1234",
    monthlyTotal = 214.6,
    monthLabel = "October",
    lastRefill = "14 Oct · 35,9 L",
)

@Preview(name = "Widget preview — both on", showBackground = true, widthDp = 390)
@Composable
private fun PreviewWidgetPreviewBoth() {
    CarTrackingAppTheme(darkTheme = true) {
        Column(Modifier.padding(18.dp)) {
            WidgetPreview(data = previewData, showSpend = true, showVoice = true)
        }
    }
}

@Preview(name = "Widget preview — stripped", showBackground = true, widthDp = 390)
@Composable
private fun PreviewWidgetPreviewStripped() {
    CarTrackingAppTheme(darkTheme = false) {
        Column(Modifier.padding(18.dp)) {
            WidgetPreview(data = previewData, showSpend = false, showVoice = false)
        }
    }
}

@Preview(name = "Car choices", showBackground = true, widthDp = 390)
@Composable
private fun PreviewCarChoices() {
    CarTrackingAppTheme(darkTheme = true) {
        Column(Modifier.padding(18.dp)) {
            GroupedListContainer {
                previewCars.forEachIndexed { index, car ->
                    if (index > 0) GroupedDivider(startInset = false)
                    CarChoiceRow(car = car, selected = index == 0, onClick = {})
                }
            }
        }
    }
}
