package com.agcoding.cartrackingapp.widget

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.agcoding.cartrackingapp.data.preferences.ColorPalette
import com.agcoding.cartrackingapp.data.preferences.ColorPalettePreferences
import com.agcoding.cartrackingapp.data.preferences.ThemePreferences
import com.agcoding.cartrackingapp.presentation.theme.AppTheme

/**
 * The app's theme for the two screens that are opened from the home screen
 * rather than from inside the app: quick entry and widget configuration.
 *
 * They used to call `CarTrackingAppTheme` with no arguments, whose default is
 * `ColorPalette.SYSTEM` — so a phone themed Crimson Red in Settings opened a
 * quick-refill dialog in the wallpaper's colours. The preference is read here
 * the same way `MainActivity` reads it, so the dialog belongs to the app that
 * launched it.
 */
@Composable
fun QuickEntryTheme(content: @Composable () -> Unit) {
    val context = LocalContext.current
    val app = remember(context) { context.applicationContext }

    val paletteFlow = remember(app) { ColorPalettePreferences(app).selectedPaletteFlow }
    val overrideFlow = remember(app) { ThemePreferences(app).isDarkModeOverrideFlow }

    val palette by paletteFlow.collectAsState(initial = ColorPalette.SYSTEM)
    val override by overrideFlow.collectAsState(initial = null)

    AppTheme(
        colorPalette = palette,
        isDark = override ?: isSystemInDarkTheme(),
        content = content,
    )
}
