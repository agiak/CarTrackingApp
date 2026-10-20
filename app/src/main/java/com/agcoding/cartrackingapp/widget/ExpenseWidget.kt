package com.agcoding.cartrackingapp.widget

import android.content.Context
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.provideContent
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import com.agcoding.cartrackingapp.R
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

/**
 * Expense, as a single home-screen button. See [ShortcutTile]; this is the
 * quiet half of the pair.
 */
object ExpenseWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Single

    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val palette = loadWidgetPalette(context)

        provideContent {
            ShortcutTile(
                icon = R.drawable.ic_receipt_24dp,
                description = context.getString(R.string.widget_add_expense),
                palette = palette,
                filled = false,
                onClick = actionRunCallback<ExpenseWidgetActionCallback>(),
            )
        }
    }
}

/**
 * ActionCallback for Expense 1x1 Widget
 */
class ExpenseWidgetActionCallback : ActionCallback {
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        runCatching { context.startActivity(QuickEntryActivity.createExpenseIntent(context)) }
            .onFailure { Timber.e(it, "Could not open quick expense from the widget") }
    }
}

/**
 * Expense Widget Receiver
 */
@AndroidEntryPoint
class ExpenseWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = ExpenseWidget
}
