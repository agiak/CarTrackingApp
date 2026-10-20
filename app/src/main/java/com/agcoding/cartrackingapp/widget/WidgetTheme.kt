package com.agcoding.cartrackingapp.widget

import android.content.Context
import androidx.glance.color.ColorProvider
import androidx.glance.unit.ColorProvider
import com.agcoding.cartrackingapp.data.preferences.ColorPalette
import com.agcoding.cartrackingapp.data.preferences.ColorPalettePreferences
import com.agcoding.cartrackingapp.data.preferences.ThemePreferences
import com.agcoding.cartrackingapp.presentation.theme.resolveAppColorScheme
import com.agcoding.cartrackingapp.shared.ui.tokens.AppColorScheme
import kotlinx.coroutines.flow.first

/**
 * The widget's colours, taken from the palette the user picked in Settings.
 *
 * The widgets used to draw themselves in `GlanceTheme.colors`, which is the
 * platform's own dynamic palette — so a phone themed Crimson Red in the app
 * grew a widget in whatever colour the wallpaper happened to suggest. The
 * widget is part of the app; it is painted with the app's own palette.
 *
 * Each role is a day/night pair, because a widget lives on the launcher and
 * follows the launcher's light or dark mode, not the app's own composition. If
 * the user has overridden the theme in Settings, the two sides of the pair are
 * the same colour and the override wins everywhere.
 */
internal data class WidgetPalette(
    /** The widget's own body: a card floating on the wallpaper. */
    val background: ColorProvider,
    /** Secondary buttons and tiles inside it. */
    val surface: ColorProvider,
    val accent: ColorProvider,
    val onAccent: ColorProvider,
    val content: ColorProvider,
    val contentMuted: ColorProvider,
)

/**
 * Reads the stored palette and theme override, then builds [WidgetPalette].
 *
 * Instantiating the two preference classes directly rather than going through
 * Hilt is deliberate: a Glance widget is handed nothing but a `Context`, and
 * both classes are thin wrappers over a process-wide DataStore — a second
 * instance reads the same file the injected one does.
 *
 * Every read is guarded. A widget that cannot reach DataStore must still
 * draw, and the app's own default palette is a better fallback than a crash
 * or a blank cell on the home screen.
 */
internal suspend fun loadWidgetPalette(context: Context): WidgetPalette {
    val app = context.applicationContext

    val palette = runCatching {
        ColorPalettePreferences(app).selectedPaletteFlow.first()
    }.getOrDefault(ColorPalette.SYSTEM)

    // Failure and "no override stored" mean the same thing here: follow the
    // launcher.
    val override = runCatching {
        ThemePreferences(app).isDarkModeOverrideFlow.first()
    }.getOrNull()

    val light = resolveAppColorScheme(app, palette, isDark = false)
    val dark = resolveAppColorScheme(app, palette, isDark = true)

    return when (override) {
        true -> widgetPalette(dark, dark)
        false -> widgetPalette(light, light)
        null -> widgetPalette(light, dark)
    }
}

private fun widgetPalette(day: AppColorScheme, night: AppColorScheme) = WidgetPalette(
    // backgroundCard, not backgroundPrimary: the widget is not a screen, it is
    // a card sitting on someone's wallpaper, and it should read as the same
    // kind of surface as the cards inside the app.
    background = ColorProvider(day = day.backgroundCard, night = night.backgroundCard),
    surface = ColorProvider(day = day.backgroundSecondary, night = night.backgroundSecondary),
    accent = ColorProvider(day = day.actionPrimary, night = night.actionPrimary),
    onAccent = ColorProvider(day = day.actionContent, night = night.actionContent),
    content = ColorProvider(day = day.contentPrimary, night = night.contentPrimary),
    contentMuted = ColorProvider(day = day.contentSecondary, night = night.contentSecondary),
)
