package com.agcoding.cartrackingapp

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.view.WindowCompat
import com.agcoding.cartrackingapp.data.preferences.SettingsPreferences
import com.agcoding.cartrackingapp.data.preferences.ThemePreferences
import com.agcoding.cartrackingapp.presentation.navigation.AppNavigation
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var themePreferences: ThemePreferences

    @Inject
    lateinit var settingsPreferences: SettingsPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        // Enable edge-to-edge display with proper system bar handling
        enableEdgeToEdge()


        setContent {
            val systemInDarkTheme = isSystemInDarkTheme()
            val themeOverride by themePreferences.isDarkModeOverrideFlow.collectAsState(initial = null)

            // Determine if dark theme should be used:
            // - If user has set a preference (themeOverride != null), use that
            // - Otherwise, follow system theme
            val useDarkTheme = themeOverride ?: systemInDarkTheme

            // Update system bars appearance when theme changes
            SideEffect {
                val window = this@MainActivity.window
                val insetsController = WindowCompat.getInsetsController(window, window.decorView)

                // Set status bar color to transparent for edge-to-edge
                window.statusBarColor = Color.Transparent.toArgb()
                window.navigationBarColor = Color.Transparent.toArgb()

                // Set status bar content color (icons & text)
                // Light theme = dark icons, Dark theme = light icons
                insetsController.isAppearanceLightStatusBars = !useDarkTheme
                insetsController.isAppearanceLightNavigationBars = !useDarkTheme
            }

            CarTrackingAppTheme(darkTheme = useDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}
