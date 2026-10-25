package com.agcoding.cartrackingapp.widget

import android.content.Context
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import com.agcoding.cartrackingapp.data.preferences.ColorPalettePreferences
import com.agcoding.cartrackingapp.data.preferences.ThemePreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Redraws the home-screen widgets when the app's colours change.
 *
 * [loadWidgetPalette] is read inside `provideGlance`, so a widget takes the
 * chosen palette the next time it is drawn — and nothing was asking it to
 * draw. Picking a new palette in Settings repainted the app and the launcher
 * icon while the widgets kept the old colours until something unrelated
 * refreshed them: a quick-add tap, a reboot, or the launcher deciding to
 * update on its own. From the user's side the widget simply looked broken.
 *
 * Watching the stored values rather than hooking the Settings screen is
 * deliberate: the palette and the light/dark override are written from more
 * than one place (the appearance screen, onboarding, the theme toggle), and a
 * writer added later should not have to remember the widgets exist.
 *
 * Started once from the Application; the collector lives for the process
 * lifetime.
 */
@Singleton
class WidgetAppearanceObserver @Inject constructor(
    @ApplicationContext private val context: Context,
    private val colorPalettePreferences: ColorPalettePreferences,
    private val themePreferences: ThemePreferences,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var started = false

    fun start() {
        if (started) return
        started = true

        scope.launch {
            combine(
                colorPalettePreferences.selectedPaletteFlow,
                themePreferences.isDarkModeOverrideFlow,
            ) { palette, darkOverride -> palette to darkOverride }
                .distinctUntilChanged()
                // The first value is the one the widgets were last drawn with
                // — neither can change while the process is dead — so acting
                // on it would mean redrawing every widget on every cold start
                // for nothing.
                .drop(1)
                // Guarded here as well as per widget: a throw out of the
                // collector would end it, and then no later palette change
                // would repaint anything for the rest of the process.
                .collect {
                    runCatching { redrawAllWidgets(context) }
                        .onFailure { error -> Timber.w(error, "Could not repaint the widgets") }
                }
        }
    }
}

/** Every widget this app puts on the home screen. */
private val WIDGETS: List<GlanceAppWidget> = listOf(QuickAddWidget, RefillWidget, ExpenseWidget)

/**
 * Asks each widget to draw itself again.
 *
 * Guarded per widget: a failure to reach one kind of widget should not stop
 * the others from being repainted, and none of this is worth taking the app
 * down for.
 */
private suspend fun redrawAllWidgets(context: Context) {
    val manager = GlanceAppWidgetManager(context)
    WIDGETS.forEach { widget ->
        runCatching {
            manager.getGlanceIds(widget.javaClass).forEach { id -> widget.update(context, id) }
        }.onFailure { Timber.w(it, "Could not redraw ${widget.javaClass.simpleName}") }
    }
}
