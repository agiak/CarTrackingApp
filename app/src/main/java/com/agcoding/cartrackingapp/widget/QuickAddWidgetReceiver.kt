package com.agcoding.cartrackingapp.widget

import android.content.Context
import android.content.Intent
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Quick Add Widget Receiver
 * Handles widget updates and broadcasts
 */
@AndroidEntryPoint
class QuickAddWidgetReceiver : GlanceAppWidgetReceiver() {

    override val glanceAppWidget: GlanceAppWidget = QuickAddWidget

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        super.onDeleted(context, appWidgetIds)
        appWidgetIds.forEach { QuickAddWidgetConfigActivity.removeWidgetConfig(context, it) }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        when (intent.action) {
            ACTION_UPDATE_WIDGET,
            QuickEntryActivity.ACTION_WIDGET_DATA_CHANGED -> updateWidgets(context)
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
         * Redraw every quick-add widget on the home screen.
         *
         * There used to be a table here mapping each AppWidget id to an index
         * into the list of Glance ids, rebuilt on every update and consulted
         * to decide which widget to refresh. It was guesswork — the order of
         * `getGlanceIds` is not a promise — and it is no longer needed:
         * `provideGlance` asks Glance which AppWidget it is drawing, and every
         * widget is refreshed here regardless.
         */
        fun updateWidgets(context: Context) {
            MainScope().launch {
                try {
                    val glanceIds = GlanceAppWidgetManager(context)
                        .getGlanceIds(QuickAddWidget::class.java)

                    glanceIds.forEach { QuickAddWidget.update(context, it) }
                } catch (e: Exception) {
                    Timber.e(e, "Error updating widgets")
                }
            }
        }
    }
}
