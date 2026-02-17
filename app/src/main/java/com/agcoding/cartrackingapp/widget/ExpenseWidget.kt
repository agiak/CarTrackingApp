package com.agcoding.cartrackingapp.widget

import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
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
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.Text
import com.agcoding.cartrackingapp.R
import dagger.hilt.android.AndroidEntryPoint

/**
 * Expense 1x1 Widget - Single icon shortcut for adding expenses
 */
object ExpenseWidget : GlanceAppWidget() {

    override val sizeMode: SizeMode = SizeMode.Single

    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    val WIDGET_ID_KEY = intPreferencesKey("widget_id")

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        Log.d("ExpenseWidget", "=== provideGlance called ===")

        try {
            provideContent {
                GlanceTheme {
                    ExpenseWidgetContent(context)
                }
            }
        } catch (e: Exception) {
            if (e.message?.contains("composition") == true ||
                e.message?.contains("coroutine scope") == true ||
                e is kotlinx.coroutines.CancellationException) {
                Log.d("ExpenseWidget", "Composition cancelled - widget will update on next refresh")
                return
            }

            Log.e("ExpenseWidget", "✗ Error providing content: ${e.message}", e)
            provideContent {
                Text("Error")
            }
        }
    }

    @Composable
    private fun ExpenseWidgetContent(context: Context) {
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .appWidgetBackground()
                .background(GlanceTheme.colors.background)
                .cornerRadius(16.dp)
                .padding(8.dp)
                .clickable(actionRunCallback<ExpenseWidgetActionCallback>()),
            contentAlignment = Alignment.Center
        ) {
            Image(
                provider = ImageProvider(R.drawable.ic_receipt_24dp),
                contentDescription = context.getString(R.string.widget_add_expense),
                modifier = GlanceModifier.size(32.dp),
                colorFilter = ColorFilter.tint(GlanceTheme.colors.primary)
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
        Log.d("ExpenseWidgetActionCallback", "=== onAction triggered ===")

        try {
            // Launch the expense activity (reusing existing navigation)
            val intent = QuickEntryActivity.createExpenseIntent(context)
            context.startActivity(intent)

            Log.d("ExpenseWidgetActionCallback", "✓ Activity launched successfully")

            // Update widget state
            updateAppWidgetState(context, glanceId) { prefs ->
                prefs.toMutablePreferences()
            }

            // Trigger widget refresh
            ExpenseWidget.update(context, glanceId)

        } catch (e: Exception) {
            Log.e("ExpenseWidgetActionCallback", "Error in action callback: ${e.message}", e)
        }
    }
}

/**
 * Expense Widget Receiver
 */
@AndroidEntryPoint
class ExpenseWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = ExpenseWidget
}

