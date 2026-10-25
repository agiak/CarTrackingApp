package com.agcoding.cartrackingapp

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.agcoding.cartrackingapp.cloud.push.PushTokenRegistrar
import com.agcoding.cartrackingapp.data.local.database.DatabaseChangeObserver
import com.agcoding.cartrackingapp.data.preferences.ColorPalettePreferences
import com.agcoding.cartrackingapp.data.preferences.SettingsPreferences
import com.agcoding.cartrackingapp.util.LauncherIconManager
import com.agcoding.cartrackingapp.widget.WidgetAppearanceObserver
import com.agcoding.cartrackingapp.worker.ReminderCheckWorker
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
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
    lateinit var launcherIconManager: LauncherIconManager

    @Inject
    lateinit var settingsPreferences: SettingsPreferences

    @Inject
    lateinit var colorPalettePreferences: ColorPalettePreferences

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
        launcherIconManager.attach(this)
        syncLauncherIcon()
        // Keeps the home-screen widgets on the palette the user picked; they
        // are drawn outside the app and nothing else tells them it changed.
        widgetAppearanceObserver.start()
    }

    /**
     * Brings the launcher icon in line with the saved palette.
     *
     * Needed because the two can be set independently of each other. Somebody
     * updating to the version that added this already has a palette chosen and
     * has never been through the Settings screen that switches the icon, so
     * without this their icon would stay on the default until they happened to
     * open the palette picker and tap something.
     *
     * Only records the wish: the switch itself waits until the app is in the
     * background, because the activity the user launched from is the alias
     * being turned off. Reading the preferences stays off the main thread.
     */
    private fun syncLauncherIcon() {
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            runCatching {
                if (settingsPreferences.settingsFlow.first().iconFollowsTheme) {
                    launcherIconManager.request(colorPalettePreferences.selectedPaletteFlow.first())
                }
            }.onFailure { Timber.w(it, "Could not sync the launcher icon at startup") }
        }
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
