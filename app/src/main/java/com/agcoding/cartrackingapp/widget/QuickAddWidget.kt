package com.agcoding.cartrackingapp.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.action.Action
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.util.formatMoney
import timber.log.Timber

/**
 * The quick-add widget.
 *
 * It is configured against one car, and it now says so. Before the redesign it
 * drew the app logo and three unlabelled icon buttons — the same picture on
 * every home screen, whichever car it was bound to, while [WidgetData] already
 * carried the car's name and the month's spend and nothing read them.
 *
 * Three layouts, picked by the size the user has dragged it to:
 *
 *  - **2×2** — car, plate, the month's spend, three icon buttons.
 *  - **4×2** — the same with the last refill beside the spend, and buttons
 *    that carry their labels now that there is room for words.
 *  - **4×1** — a single row: car, spend, two buttons.
 *
 * All three are painted from the palette the user picked in Settings (see
 * [loadWidgetPalette]) rather than from `GlanceTheme`, which is the platform's
 * wallpaper-derived colours and has nothing to do with this app.
 */
object QuickAddWidget : GlanceAppWidget() {

    /** 2×2, and the fallback whenever nothing else fits. */
    private val SMALL = DpSize(110.dp, 110.dp)

    /** 4×1: wide enough for a row, too short for anything stacked. */
    private val WIDE = DpSize(230.dp, 70.dp)

    /** 4×2. */
    private val LARGE = DpSize(230.dp, 150.dp)

    override val sizeMode: SizeMode = SizeMode.Responsive(setOf(SMALL, WIDE, LARGE))

    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    /**
     * The car a button was tapped on, handed to the quick-entry screen.
     *
     * Without it every widget opened the dialog on whichever car happened to
     * be the default, so two widgets for two cars both logged to the same one.
     */
    val CarIdParam = ActionParameters.Key<Long>("widget_car_id")

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // The AppWidget id is what the configuration screen stored against, so
        // ask Glance for it directly rather than inferring it from a parallel
        // table of our own.
        val widgetId = runCatching {
            GlanceAppWidgetManager(context).getAppWidgetId(id)
        }.getOrElse {
            Timber.w(it, "No AppWidget id for $id")
            0
        }

        val data = WidgetDataProvider.getWidgetData(context, widgetId)
        val palette = loadWidgetPalette(context)
        val showSpend = QuickAddWidgetConfigActivity.showsSpend(context, widgetId)
        // Two conditions, both of which have to hold: the user asked for the
        // button, and the app may actually record audio.
        val showVoice = QuickAddWidgetConfigActivity.showsVoice(context, widgetId) &&
            WidgetPermissionChecker.hasMicrophonePermission(context)

        provideContent {
            WidgetBody(context, data, palette, showSpend, showVoice)
        }
    }

    @Composable
    private fun WidgetBody(
        context: Context,
        data: WidgetData,
        palette: WidgetPalette,
        showSpend: Boolean,
        showVoice: Boolean,
    ) {
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .appWidgetBackground()
                .background(palette.background)
                .cornerRadius(24.dp)
        ) {
            when {
                data.noCarsAvailable -> EmptyState(context, palette)
                LocalSize.current == WIDE -> ShortLayout(context, data, palette)
                LocalSize.current == LARGE -> WideLayout(context, data, palette, showSpend, showVoice)
                else -> CompactLayout(context, data, palette, showSpend, showVoice)
            }
        }
    }

    // ── layouts ──────────────────────────────────────────────────────────────

    /** 2×2: the default, and what most people will see. */
    @Composable
    private fun CompactLayout(
        context: Context,
        data: WidgetData,
        palette: WidgetPalette,
        showSpend: Boolean,
        showVoice: Boolean,
    ) {
        Column(modifier = GlanceModifier.fillMaxSize().padding(14.dp)) {
            CarName(data.carName, palette, fontSize = 14.sp)
            if (!data.licensePlate.isNullOrBlank()) {
                Spacer(GlanceModifier.height(3.dp))
                Plate(data.licensePlate, palette)
            }

            Spacer(GlanceModifier.defaultWeight())

            if (showSpend) {
                Label(data.monthLabel, palette)
                Spacer(GlanceModifier.height(1.dp))
                Amount(data.monthlyTotal, palette, fontSize = 20.sp)
                Spacer(GlanceModifier.height(10.dp))
            }

            ActionRow(context, data.carId, palette, showVoice, height = 44.dp)
        }
    }

    /** 4×2: the same widget with room to name things. */
    @Composable
    private fun WideLayout(
        context: Context,
        data: WidgetData,
        palette: WidgetPalette,
        showSpend: Boolean,
        showVoice: Boolean,
    ) {
        Column(modifier = GlanceModifier.fillMaxSize().padding(14.dp)) {
            Row(
                modifier = GlanceModifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CarName(
                    data.carName,
                    palette,
                    fontSize = 15.sp,
                    modifier = GlanceModifier.defaultWeight()
                )
                if (!data.licensePlate.isNullOrBlank()) {
                    Spacer(GlanceModifier.width(8.dp))
                    Plate(data.licensePlate, palette)
                }
            }

            Spacer(GlanceModifier.height(12.dp))

            Row(modifier = GlanceModifier.fillMaxWidth()) {
                if (showSpend) {
                    Column(modifier = GlanceModifier.defaultWeight()) {
                        Label(
                            context.getString(R.string.widget_spent_in, data.monthLabel),
                            palette
                        )
                        Spacer(GlanceModifier.height(1.dp))
                        Amount(data.monthlyTotal, palette, fontSize = 16.sp)
                    }
                }
                Column(modifier = GlanceModifier.defaultWeight()) {
                    Label(context.getString(R.string.widget_last_refill), palette)
                    Spacer(GlanceModifier.height(1.dp))
                    Text(
                        text = data.lastRefill ?: EM_DASH,
                        maxLines = 1,
                        style = TextStyle(
                            color = palette.content,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                        )
                    )
                }
            }

            Spacer(GlanceModifier.defaultWeight())

            LabelledActionRow(context, data.carId, palette, showVoice)
        }
    }

    /** 4×1: one row, and only the two actions that are used most. */
    @Composable
    private fun ShortLayout(context: Context, data: WidgetData, palette: WidgetPalette) {
        Row(
            modifier = GlanceModifier.fillMaxSize().padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = GlanceModifier
                    .size(40.dp)
                    .background(palette.surface)
                    .cornerRadius(14.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    provider = ImageProvider(R.drawable.ic_refill),
                    contentDescription = null,
                    modifier = GlanceModifier.size(20.dp),
                    colorFilter = ColorFilter.tint(palette.accent)
                )
            }

            Spacer(GlanceModifier.width(10.dp))

            Column(modifier = GlanceModifier.defaultWeight()) {
                CarName(data.carName, palette, fontSize = 13.sp)
                Spacer(GlanceModifier.height(1.dp))
                Text(
                    text = "${data.monthlyTotal.formatMoney()} · ${data.monthLabel}",
                    maxLines = 1,
                    style = TextStyle(color = palette.contentMuted, fontSize = 12.sp)
                )
            }

            Spacer(GlanceModifier.width(8.dp))
            IconButton(
                icon = R.drawable.ic_refill,
                description = context.getString(R.string.widget_add_fuel),
                palette = palette,
                primary = true,
                onClick = refillAction(data.carId),
                modifier = GlanceModifier.size(48.dp)
            )
            Spacer(GlanceModifier.width(6.dp))
            IconButton(
                icon = R.drawable.ic_receipt_24dp,
                description = context.getString(R.string.widget_add_expense),
                palette = palette,
                primary = false,
                onClick = expenseAction(data.carId),
                modifier = GlanceModifier.size(48.dp)
            )
        }
    }

    /**
     * No cars on file. The widget is useless until there is one, so the whole
     * face of it opens the app rather than offering buttons that cannot work.
     */
    @Composable
    private fun EmptyState(context: Context, palette: WidgetPalette) {
        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .padding(14.dp)
                .clickable(actionRunCallback<OpenAppActionCallback>()),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = GlanceModifier
                    .size(32.dp)
                    .background(palette.accent)
                    .cornerRadius(10.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    provider = ImageProvider(R.drawable.ic_refill),
                    contentDescription = null,
                    modifier = GlanceModifier.size(20.dp),
                    colorFilter = ColorFilter.tint(palette.onAccent)
                )
            }

            Spacer(GlanceModifier.height(10.dp))

            Text(
                text = context.getString(R.string.widget_no_car),
                maxLines = 1,
                style = TextStyle(
                    color = palette.content,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                )
            )
            Spacer(GlanceModifier.height(3.dp))
            Text(
                text = context.getString(R.string.widget_add_car_first),
                maxLines = 3,
                style = TextStyle(color = palette.contentMuted, fontSize = 12.sp)
            )
        }
    }

    // ── pieces ───────────────────────────────────────────────────────────────

    @Composable
    private fun CarName(
        name: String?,
        palette: WidgetPalette,
        fontSize: TextUnit,
        modifier: GlanceModifier = GlanceModifier,
    ) {
        Text(
            text = name.orEmpty(),
            modifier = modifier,
            maxLines = 1,
            style = TextStyle(
                color = palette.content,
                fontSize = fontSize,
                fontWeight = FontWeight.Bold,
            )
        )
    }

    @Composable
    private fun Plate(plate: String, palette: WidgetPalette) {
        Box(
            modifier = GlanceModifier
                .background(palette.surface)
                .cornerRadius(6.dp)
                .padding(horizontal = 5.dp, vertical = 2.dp)
        ) {
            Text(
                text = plate.uppercase(),
                maxLines = 1,
                style = TextStyle(color = palette.contentMuted, fontSize = 10.sp)
            )
        }
    }

    @Composable
    private fun Label(text: String, palette: WidgetPalette) {
        Text(
            text = text,
            maxLines = 1,
            style = TextStyle(
                color = palette.contentMuted,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
            )
        )
    }

    @Composable
    private fun Amount(value: Double, palette: WidgetPalette, fontSize: TextUnit) {
        Text(
            text = value.formatMoney(),
            maxLines = 1,
            style = TextStyle(
                color = palette.content,
                fontSize = fontSize,
                fontWeight = FontWeight.Bold,
            )
        )
    }

    /** Icon-only buttons, stretched to share the width equally. */
    @Composable
    private fun ActionRow(
        context: Context,
        carId: Long?,
        palette: WidgetPalette,
        showVoice: Boolean,
        height: Dp,
    ) {
        Row(modifier = GlanceModifier.fillMaxWidth()) {
            IconButton(
                icon = R.drawable.ic_refill,
                description = context.getString(R.string.widget_add_fuel),
                palette = palette,
                primary = true,
                onClick = refillAction(carId),
                modifier = GlanceModifier.defaultWeight().height(height)
            )
            Spacer(GlanceModifier.width(6.dp))
            IconButton(
                icon = R.drawable.ic_receipt_24dp,
                description = context.getString(R.string.widget_add_expense),
                palette = palette,
                primary = false,
                onClick = expenseAction(carId),
                modifier = GlanceModifier.defaultWeight().height(height)
            )
            if (showVoice) {
                Spacer(GlanceModifier.width(6.dp))
                IconButton(
                    icon = R.drawable.ic_mic,
                    description = context.getString(R.string.widget_voice_entry),
                    palette = palette,
                    primary = false,
                    onClick = voiceAction(carId),
                    modifier = GlanceModifier.defaultWeight().height(height)
                )
            }
        }
    }

    /** The same three actions, with their names, for the 4×2 size. */
    @Composable
    private fun LabelledActionRow(
        context: Context,
        carId: Long?,
        palette: WidgetPalette,
        showVoice: Boolean,
    ) {
        Row(modifier = GlanceModifier.fillMaxWidth()) {
            LabelledButton(
                icon = R.drawable.ic_refill,
                label = context.getString(R.string.widget_add_fuel),
                palette = palette,
                primary = true,
                onClick = refillAction(carId),
                modifier = GlanceModifier.defaultWeight()
            )
            Spacer(GlanceModifier.width(6.dp))
            LabelledButton(
                icon = R.drawable.ic_receipt_24dp,
                label = context.getString(R.string.widget_add_expense),
                palette = palette,
                primary = false,
                onClick = expenseAction(carId),
                modifier = GlanceModifier.defaultWeight()
            )
            if (showVoice) {
                Spacer(GlanceModifier.width(6.dp))
                LabelledButton(
                    icon = R.drawable.ic_mic,
                    label = context.getString(R.string.widget_voice_entry),
                    palette = palette,
                    primary = false,
                    onClick = voiceAction(carId),
                    modifier = GlanceModifier.defaultWeight()
                )
            }
        }
    }

    @Composable
    private fun IconButton(
        icon: Int,
        description: String,
        palette: WidgetPalette,
        primary: Boolean,
        onClick: Action,
        modifier: GlanceModifier = GlanceModifier,
    ) {
        Box(
            modifier = modifier
                .background(if (primary) palette.accent else palette.surface)
                .cornerRadius(14.dp)
                .clickable(onClick),
            contentAlignment = Alignment.Center
        ) {
            Image(
                provider = ImageProvider(icon),
                contentDescription = description,
                modifier = GlanceModifier.size(22.dp),
                colorFilter = ColorFilter.tint(if (primary) palette.onAccent else palette.accent)
            )
        }
    }

    @Composable
    private fun LabelledButton(
        icon: Int,
        label: String,
        palette: WidgetPalette,
        primary: Boolean,
        onClick: Action,
        modifier: GlanceModifier = GlanceModifier,
    ) {
        Row(
            modifier = modifier
                .height(44.dp)
                .background(if (primary) palette.accent else palette.surface)
                .cornerRadius(14.dp)
                .clickable(onClick),
            verticalAlignment = Alignment.CenterVertically,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                provider = ImageProvider(icon),
                contentDescription = null,
                modifier = GlanceModifier.size(20.dp),
                colorFilter = ColorFilter.tint(if (primary) palette.onAccent else palette.accent)
            )
            Spacer(GlanceModifier.width(6.dp))
            Text(
                text = label,
                maxLines = 1,
                style = TextStyle(
                    color = if (primary) palette.onAccent else palette.content,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                )
            )
        }
    }

    // ── actions ──────────────────────────────────────────────────────────────

    private fun carParams(carId: Long?) =
        actionParametersOf(CarIdParam to (carId ?: -1L))

    private fun refillAction(carId: Long?) =
        actionRunCallback<RefillActionCallback>(carParams(carId))

    private fun expenseAction(carId: Long?) =
        actionRunCallback<ExpenseActionCallback>(carParams(carId))

    private fun voiceAction(carId: Long?) =
        actionRunCallback<VoiceActionCallback>(carParams(carId))

    private const val EM_DASH = "—"
}

/** The car the tapped widget is bound to, or -1 when it has none. */
private fun ActionParameters.carId(): Long = this[QuickAddWidget.CarIdParam] ?: -1L

/**
 * Opens the quick refill entry for the widget's car.
 */
class RefillActionCallback : ActionCallback {

    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        startQuickEntry(context) { QuickEntryActivity.createRefillIntent(it, parameters.carId()) }
    }
}

class ExpenseActionCallback : ActionCallback {

    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        startQuickEntry(context) { QuickEntryActivity.createExpenseIntent(it, parameters.carId()) }
    }
}

/**
 * Voice entry. The permission is checked again here: the button is only drawn
 * when it was granted, but a widget can sit on the home screen for weeks after
 * the user has taken it away again.
 */
class VoiceActionCallback : ActionCallback {

    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        if (!WidgetPermissionChecker.hasMicrophonePermission(context)) {
            Timber.w("Voice tapped without microphone permission")
            QuickAddWidgetReceiver.updateWidgets(context)
            return
        }
        startQuickEntry(context) { QuickEntryActivity.createVoiceIntent(it, parameters.carId()) }
    }
}

/** Empty-state tap: there is nothing to add to, so open the app itself. */
class OpenAppActionCallback : ActionCallback {

    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        if (intent == null) {
            Timber.w("No launch intent for ${context.packageName}")
            return
        }
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        runCatching { context.startActivity(intent) }
            .onFailure { Timber.e(it, "Could not open the app from the widget") }
    }
}

/**
 * The widget refreshes itself when the entry screen is done, over the
 * broadcast [QuickEntryActivity] already sends, so there is nothing to do here
 * beyond starting the activity.
 */
private fun startQuickEntry(context: Context, intent: (Context) -> Intent) {
    runCatching { context.startActivity(intent(context)) }
        .onFailure { Timber.e(it, "Could not open quick entry from the widget") }
}
