package com.agcoding.cartrackingapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.core.view.WindowCompat
import com.agcoding.cartrackingapp.data.export.AutoBackupManager
import com.agcoding.cartrackingapp.data.local.database.DatabaseHealthChecker
import com.agcoding.cartrackingapp.data.local.database.DatabaseStatus
import com.agcoding.cartrackingapp.data.preferences.ColorPalettePreferences
import com.agcoding.cartrackingapp.data.preferences.SettingsPreferences
import com.agcoding.cartrackingapp.data.preferences.ThemePreferences
import com.agcoding.cartrackingapp.presentation.navigation.AppNavigation
import com.agcoding.cartrackingapp.presentation.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var themePreferences: ThemePreferences

    @Inject
    lateinit var settingsPreferences: SettingsPreferences

    @Inject
    lateinit var colorPalettePreferences: ColorPalettePreferences

    @Inject
    lateinit var databaseHealthChecker: DatabaseHealthChecker

    @Inject
    lateinit var autoBackupManager: AutoBackupManager

    private var widgetAction: String? = null
    private var widgetCarId: Long? = null
    private var notificationExpenseId: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Handle widget and notification deep links
        handleWidgetIntent(intent)
        handleNotificationIntent(intent)

        autoBackupManager.start()

        // Enable edge-to-edge display with proper system bar handling
        enableEdgeToEdge()

        setContent {
            val systemInDarkTheme = isSystemInDarkTheme()
            val themeOverride by themePreferences.isDarkModeOverrideFlow.collectAsState(initial = null)
            val selectedPalette by colorPalettePreferences.selectedPaletteFlow.collectAsState(initial = com.agcoding.cartrackingapp.data.preferences.ColorPalette.SYSTEM)
            val dbStatus by databaseHealthChecker.status.collectAsState()

            LaunchedEffect(Unit) {
                databaseHealthChecker.check()
            }

            val useDarkTheme = themeOverride ?: systemInDarkTheme

            SideEffect {
                val window = this@MainActivity.window
                val insetsController = WindowCompat.getInsetsController(window, window.decorView)
                window.statusBarColor = Color.Transparent.toArgb()
                insetsController.isAppearanceLightStatusBars = !useDarkTheme
                insetsController.isAppearanceLightNavigationBars = !useDarkTheme
            }

            AppTheme(
                colorPalette = selectedPalette,
                isDark       = useDarkTheme,
            ) {
                val backgroundColor = MaterialTheme.colorScheme.background
                SideEffect {
                    val window = this@MainActivity.window
                    window.navigationBarColor = backgroundColor.toArgb()
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color    = MaterialTheme.colorScheme.background,
                ) {
                    if (dbStatus == DatabaseStatus.Healthy || dbStatus == DatabaseStatus.Checking) {
                        AppNavigation(
                            widgetAction           = this@MainActivity.widgetAction,
                            widgetCarId            = this@MainActivity.widgetCarId,
                            notificationExpenseId  = this@MainActivity.notificationExpenseId,
                        )
                    }

                    if (dbStatus == DatabaseStatus.MigrationFailed) {
                        AlertDialog(
                            onDismissRequest = {},
                            title = { Text(stringResource(R.string.db_error_title)) },
                            text = { Text(stringResource(R.string.db_error_message)) },
                            confirmButton = {
                                TextButton(onClick = {
                                    val intent = Intent(Intent.ACTION_VIEW).apply {
                                        data = Uri.parse("market://details?id=com.agcoding.cartrackingapp")
                                        setPackage("com.android.vending")
                                    }
                                    startActivity(intent)
                                }) {
                                    Text(stringResource(R.string.db_error_open_store))
                                }
                            },
                            dismissButton = {
                                TextButton(onClick = { finishAffinity() }) {
                                    Text(stringResource(R.string.db_error_close))
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleWidgetIntent(intent)
        handleNotificationIntent(intent)
    }

    private fun handleWidgetIntent(intent: Intent) {
        widgetAction = intent.getStringExtra("widget_action")
        widgetCarId = intent.getLongExtra("car_id", -1L).takeIf { it != -1L }
    }

    private fun handleNotificationIntent(intent: Intent) {
        notificationExpenseId = intent.getLongExtra("expense_id", -1L).takeIf { it != -1L }
    }
}
