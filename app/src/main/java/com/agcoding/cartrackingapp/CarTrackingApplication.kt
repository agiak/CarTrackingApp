package com.agcoding.cartrackingapp

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.agcoding.cartrackingapp.cloud.push.PushTokenRegistrar
import com.agcoding.cartrackingapp.data.local.database.DatabaseChangeObserver
import com.agcoding.cartrackingapp.widget.WidgetAppearanceObserver
import com.agcoding.cartrackingapp.worker.ReminderCheckWorker
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class CarTrackingApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var databaseChangeObserver: DatabaseChangeObserver

    @Inject
    lateinit var pushTokenRegistrar: PushTokenRegistrar

    @Inject
    lateinit var widgetAppearanceObserver: WidgetAppearanceObserver

    override fun onCreate() {
        super.onCreate()
        Timber.plant(Timber.DebugTree())
        // Start tracking data modifications (updates the "last modified" timestamp).
        databaseChangeObserver.start()
        // Watches the session: the server can only push to a device it has
        // been told about, and it is told here.
        pushTokenRegistrar.start()
        scheduleReminderChecks()
        // Keeps the home-screen widgets on the palette the user picked; they
        // are drawn outside the app and nothing else tells them it changed.
        widgetAppearanceObserver.start()
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    private fun scheduleReminderChecks() {
        val reminderCheckRequest = PeriodicWorkRequestBuilder<ReminderCheckWorker>(
            repeatInterval = 12, // Check every 12 hours
            repeatIntervalTimeUnit = TimeUnit.HOURS
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            ReminderCheckWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            reminderCheckRequest
        )
    }
}
