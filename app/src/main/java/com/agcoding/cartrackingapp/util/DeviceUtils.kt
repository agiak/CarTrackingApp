package com.agcoding.cartrackingapp.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration

/**
 * Utility functions for device detection and screen size calculations.
 */
object DeviceUtils {

    /**
     * Determines if the current device is a tablet.
     * This function considers both screen size to properly distinguish
     * between tablets and phones in landscape mode.
     *
     * @return true if the device is a tablet, false if it's a phone (even in landscape)
     */
    @Composable
    fun isTablet(): Boolean {
        val configuration = LocalConfiguration.current

        @Suppress("DEPRECATION")
        val screenWidthDp = configuration.screenWidthDp
        @Suppress("DEPRECATION")
        val screenHeightDp = configuration.screenHeightDp

        // Get the smallest screen dimension in dp
        val smallestWidthDp = minOf(screenWidthDp, screenHeightDp)

        // Tablets typically have a smallest width of 600dp or more
        // This ensures we don't treat phones in landscape as tablets
        return smallestWidthDp >= 600
    }

    /**
     * Determines if the device is in landscape mode.
     */
    @Composable
    fun isLandscape(): Boolean {
        val configuration = LocalConfiguration.current
        @Suppress("DEPRECATION")
        return configuration.screenWidthDp > configuration.screenHeightDp
    }
}
