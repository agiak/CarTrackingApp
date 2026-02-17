package com.agcoding.cartrackingapp.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.state.updateAppWidgetState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch

/**
 * Quick Add Widget Receiver
 * Handles widget updates and broadcasts
 */
@AndroidEntryPoint
class QuickAddWidgetReceiver : GlanceAppWidgetReceiver() {

    override val glanceAppWidget: GlanceAppWidget = QuickAddWidget

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        super.onUpdate(context, appWidgetManager, appWidgetIds)

        // Store widget IDs in Glance state for each widget instance
        MainScope().launch {
            val glanceAppWidgetManager = GlanceAppWidgetManager(context)
            val glanceIds = glanceAppWidgetManager.getGlanceIds(QuickAddWidget::class.java)
            val prefs = context.getSharedPreferences("widget_glance_mapping", Context.MODE_PRIVATE)

            // For each AppWidget ID, find or create a corresponding Glance ID mapping
            appWidgetIds.forEach { appWidgetId ->
                // Get the stored Glance ID index for this widget
                val glanceIndex = prefs.getInt("glance_index_$appWidgetId", -1)

                val mappedGlanceId = if (glanceIndex >= 0 && glanceIndex < glanceIds.size) {
                    glanceIds[glanceIndex]
                } else {
                    // No mapping found, find the first available Glance ID
                    val usedIndices = prefs.all.entries
                        .filter { it.key.startsWith("glance_index_") }
                        .map { it.value as? Int }
                        .filterNotNull()
                        .toSet()

                    val availableIndex =
                        (0 until glanceIds.size).firstOrNull { it !in usedIndices } ?: 0
                    prefs.edit().putInt("glance_index_$appWidgetId", availableIndex).apply()

                    if (availableIndex < glanceIds.size) glanceIds[availableIndex] else null
                }

                if (mappedGlanceId != null) {
                    updateAppWidgetState(context, mappedGlanceId) { prefs ->
                        prefs.toMutablePreferences().apply {
                            set(QuickAddWidget.WIDGET_ID_KEY, appWidgetId)
                        }
                    }
                    QuickAddWidget.update(context, mappedGlanceId)
                }
            }
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        // Clean up widget configuration and mapping
        val prefs = context.getSharedPreferences("widget_glance_mapping", Context.MODE_PRIVATE)
        appWidgetIds.forEach { widgetId ->
            QuickAddWidgetConfigActivity.removeWidgetConfig(context, widgetId)
            prefs.edit().remove("glance_index_$widgetId").apply()
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        when (intent.action) {
            ACTION_UPDATE_WIDGET -> {
                Log.d("QuickAddWidgetReceiver", "Received widget update request: ${intent.action}")
                // Update all widget instances
                updateWidgets(context)
            }
            QuickEntryActivity.ACTION_WIDGET_DATA_CHANGED -> {
                Log.d("QuickAddWidgetReceiver", "Received widget data changed notification")
                // Update all widgets
                updateWidgets(context)
            }
        }
    }

    companion object {
        const val ACTION_UPDATE_WIDGET = "com.agcoding.cartrackingapp.ACTION_UPDATE_WIDGET"

        /**
         * Request widget update
         */
        fun requestUpdate(context: Context) {
            val intent = Intent(context, QuickAddWidgetReceiver::class.java).apply {
                action = ACTION_UPDATE_WIDGET
            }
            context.sendBroadcast(intent)
        }

        /**
         * Update all widgets when data changes
         */
        fun updateWidgets(context: Context) {
            MainScope().launch {
                try {
                    Log.d("QuickAddWidgetReceiver", "Starting widget update...")

                    val glanceAppWidgetManager = GlanceAppWidgetManager(context)
                    val glanceIds = glanceAppWidgetManager.getGlanceIds(QuickAddWidget::class.java)

                    if (glanceIds.isEmpty()) {
                        Log.d("QuickAddWidgetReceiver", "No widgets to update")
                        return@launch
                    }

                    Log.d("QuickAddWidgetReceiver", "Updating ${glanceIds.size} widget(s)")

                    // Update all widgets
                    glanceIds.forEach { glanceId ->
                        launch {
                            try {
                                Log.d("QuickAddWidgetReceiver", "Updating widget: $glanceId")

                                // Trigger widget update
                                QuickAddWidget.update(context, glanceId)

                                Log.d("QuickAddWidgetReceiver", "✓ Widget $glanceId updated successfully")
                            } catch (e: Exception) {
                                Log.e(
                                    "QuickAddWidgetReceiver",
                                    "Error updating widget $glanceId: ${e.message}",
                                    e
                                )
                            }
                        }
                    }

                    Log.d("QuickAddWidgetReceiver", "✓ Widget update completed")
                } catch (e: Exception) {
                    Log.e("QuickAddWidgetReceiver", "Error in updateWidgets: ${e.message}", e)
                }
            }
        }
    }
}