package com.agcoding.cartrackingapp.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Worker to update widgets periodically
 */
class WidgetUpdateWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val glanceAppWidgetManager = GlanceAppWidgetManager(applicationContext)
            val glanceIds = glanceAppWidgetManager.getGlanceIds(QuickAddWidget::class.java)

            glanceIds.forEach { glanceId ->
                QuickAddWidget().update(applicationContext, glanceId)
            }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "widget_update_work"
    }
}

