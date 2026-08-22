package com.agcoding.cartrackingapp.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

/**
 * The app-wide "success" colour — green, always.
 *
 * Success and granted-permission states are semantic: they should stay green no
 * matter which accent palette the user picked, so they are not read from the
 * ColorScheme (whose `tertiary` is cyan, orange, purple… depending on the theme).
 *
 * The light/dark tone is chosen from the *actual* surface colour rather than
 * `isSystemInDarkTheme()`, so it follows the app's own theme setting even when
 * that disagrees with the system.
 */
object AppSuccess {

    private val isDarkSurface: Boolean
        @Composable @ReadOnlyComposable
        get() = MaterialTheme.colorScheme.surface.luminance() < 0.5f

    /** Green for icons, strokes and filled badges. */
    val color: Color
        @Composable @ReadOnlyComposable
        get() = if (isDarkSurface) SuccessGreenDark else SuccessGreenLight

    /** Muted green fill to sit behind [color]. */
    val container: Color
        @Composable @ReadOnlyComposable
        get() = if (isDarkSurface) SuccessContainerDark else SuccessContainerLight

    /** Content colour for anything drawn on top of [color]. */
    val onColor: Color
        @Composable @ReadOnlyComposable
        get() = if (isDarkSurface) OnSuccessDark else OnSuccessLight
}
