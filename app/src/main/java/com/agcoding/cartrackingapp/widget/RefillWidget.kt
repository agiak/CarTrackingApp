package com.agcoding.cartrackingapp.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.Action
import androidx.glance.action.ActionParameters
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.appWidgetBackground
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.size
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import com.agcoding.cartrackingapp.R
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

/**
 * Refill, as a single home-screen button.
 *
 * The 1×1 pair are shortcuts, not dashboards: there is no room for a figure,
 * so the only thing they have to get right is being recognisable and looking
 * like part of the app. Refill is the filled one — it is the action people put
 * on their home screen — and expense is the quiet one beside it.
 */
object RefillWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Single

    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val palette = loadWidgetPalette(context)

        provideContent {
            ShortcutTile(
                icon = R.drawable.ic_refill,
                description = context.getString(R.string.widget_add_fuel),
                palette = palette,
                filled = true,
                onClick = actionRunCallback<RefillWidgetActionCallback>(),
            )
        }
    }
}

/**
 * The face both 1×1 widgets share.
 *
 * @param filled paints the tile in the accent colour with the icon knocked out
 *   of it, rather than an accent icon on the app's own surface.
 */
@Composable
internal fun ShortcutTile(
    icon: Int,
    description: String,
    palette: WidgetPalette,
    filled: Boolean,
    onClick: Action,
) {
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .appWidgetBackground()
            .background(if (filled) palette.accent else palette.background)
            .cornerRadius(22.dp)
            .clickable(onClick),
        contentAlignment = Alignment.Center
    ) {
        Image(
            provider = ImageProvider(icon),
            contentDescription = description,
            modifier = GlanceModifier.size(30.dp),
            colorFilter = ColorFilter.tint(if (filled) palette.onAccent else palette.accent)
        )
    }
}

/**
 * ActionCallback for Refill 1x1 Widget
 */
class RefillWidgetActionCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        // No car id: this widget is not configured against one, so the entry
        // screen falls back to the default car and offers the picker.
        runCatching { context.startActivity(QuickEntryActivity.createRefillIntent(context)) }
            .onFailure { Timber.e(it, "Could not open quick refill from the widget") }
    }
}

/**
 * Refill Widget Receiver
 */
@AndroidEntryPoint
class RefillWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = RefillWidget
}
