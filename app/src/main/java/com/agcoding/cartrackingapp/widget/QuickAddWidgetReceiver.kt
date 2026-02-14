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
                // Update all widget instances with fresh data from database
                updateWidgets(context, null)
            }
            QuickEntryActivity.ACTION_WIDGET_DATA_CHANGED -> {
                Log.d("QuickAddWidgetReceiver", "Received widget update with transaction data")

                // Extract transaction data from intent
                val type = intent.getStringExtra(QuickEntryActivity.RESULT_TRANSACTION_TYPE)
                val amount = intent.getDoubleExtra(QuickEntryActivity.RESULT_TRANSACTION_AMOUNT, 0.0)
                val timestamp = intent.getLongExtra(QuickEntryActivity.RESULT_TRANSACTION_TIMESTAMP, 0L)
                val carName = intent.getStringExtra(QuickEntryActivity.RESULT_TRANSACTION_CAR_NAME) ?: ""
                val carId = intent.getLongExtra(QuickEntryActivity.RESULT_TRANSACTION_CAR_ID, -1L)

                if (type != null && carName.isNotEmpty()) {
                    val transaction = LastTransaction(
                        type = type,
                        amount = amount,
                        timestamp = timestamp,
                        carName = carName,
                        carId = carId
                    )
                    Log.d("QuickAddWidgetReceiver", "Transaction data: $transaction")
                    // Update widgets with the provided transaction data (no database query needed!)
                    updateWidgets(context, transaction)
                } else {
                    Log.w("QuickAddWidgetReceiver", "Invalid transaction data in broadcast, fetching from database")
                    updateWidgets(context, null)
                }
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
         * @param transaction If provided, use this transaction data directly without querying database
         */
        fun updateWidgets(context: Context, transaction: LastTransaction? = null) {
            MainScope().launch {
                try {
                    Log.d(
                        "QuickAddWidgetReceiver",
                        if (transaction != null) "Starting widget update with provided transaction data..."
                        else "Starting widget update with fresh data fetch..."
                    )

                    val glanceAppWidgetManager = GlanceAppWidgetManager(context)
                    val glanceIds = glanceAppWidgetManager.getGlanceIds(QuickAddWidget::class.java)

                    if (glanceIds.isEmpty()) {
                        Log.d("QuickAddWidgetReceiver", "No widgets to update")
                        return@launch
                    }

                    Log.d(
                        "QuickAddWidgetReceiver",
                        "Updating ${glanceIds.size} widget(s) with ${if (transaction != null) "provided" else "database"} data"
                    )

                    // Force update all widgets
                    glanceIds.forEach { glanceId ->
                        launch {
                            try {
                                Log.d("QuickAddWidgetReceiver", "Updating widget: $glanceId")

                                // If we have transaction data, store it in the widget state
                                if (transaction != null) {
                                    updateAppWidgetState(context, glanceId) { prefs ->
                                        prefs.toMutablePreferences().apply {
                                            // Store transaction data temporarily for immediate display
                                            this[androidx.datastore.preferences.core.stringPreferencesKey("cached_transaction_type")] = transaction.type
                                            this[androidx.datastore.preferences.core.doublePreferencesKey("cached_transaction_amount")] = transaction.amount
                                            this[androidx.datastore.preferences.core.longPreferencesKey("cached_transaction_timestamp")] = transaction.timestamp
                                            this[androidx.datastore.preferences.core.stringPreferencesKey("cached_transaction_car_name")] = transaction.carName
                                            this[androidx.datastore.preferences.core.longPreferencesKey("cached_transaction_car_id")] = transaction.carId
                                        }
                                    }
                                }

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