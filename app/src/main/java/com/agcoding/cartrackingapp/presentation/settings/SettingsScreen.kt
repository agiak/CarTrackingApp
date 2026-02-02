package com.agcoding.cartrackingapp.presentation.settings

import android.Manifest
import android.app.Activity
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.data.preferences.ColorPalette
import com.agcoding.cartrackingapp.presentation.settings.components.AppearanceCard
import com.agcoding.cartrackingapp.presentation.settings.components.ColorPaletteCard
import com.agcoding.cartrackingapp.presentation.settings.components.CustomizationCard
import com.agcoding.cartrackingapp.presentation.settings.components.DebugCard
import com.agcoding.cartrackingapp.presentation.settings.components.HelpAboutCard
import com.agcoding.cartrackingapp.presentation.settings.components.LanguageCard
import com.agcoding.cartrackingapp.presentation.settings.components.PreferencesCard
import com.agcoding.cartrackingapp.presentation.settings.components.SectionHeader
import com.agcoding.cartrackingapp.presentation.settings.components.SettingsContent
import com.agcoding.cartrackingapp.presentation.settings.components.SpreadsheetImportCard
import com.agcoding.cartrackingapp.presentation.settings.components.StorageCard
import com.agcoding.cartrackingapp.util.PermissionUtil
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onViewGuide: () -> Unit = {},
    onManageExpenseCategories: () -> Unit = {},
    onViewNotifications: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedPalette by viewModel.colorPalettePreferences.selectedPaletteFlow.collectAsState(initial = ColorPalette.SYSTEM)
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Refresh storage size every time the screen is opened/resumed
    LaunchedEffect(Unit) {
        viewModel.refreshStorageSize()
    }

    // Track notification permission state (for Android 13+)
    var notificationPermissionGranted by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            } else {
                true
            }
        )
    }

    // Track if permission was denied permanently (user denied twice or selected "Don't ask again")
    var permissionPermanentlyDenied by remember { mutableStateOf(false) }

    // Observe lifecycle to refresh permission state when user returns from settings
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                // Re-check notification permission when screen is resumed
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    val isGranted = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.POST_NOTIFICATIONS
                    ) == android.content.pm.PackageManager.PERMISSION_GRANTED

                    notificationPermissionGranted = isGranted

                    // If permission was granted, update the settings and clear permanently denied flag
                    if (isGranted) {
                        viewModel.updateNotificationsEnabled(true)
                        permissionPermanentlyDenied = false
                    }
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Notification permission launcher
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        notificationPermissionGranted = isGranted
        if (isGranted) {
            viewModel.updateNotificationsEnabled(true)
            permissionPermanentlyDenied = false
        } else {
            // Check if user permanently denied the permission
            val activity = context as? Activity
            if (activity != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                val shouldShow = activity.shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)
                permissionPermanentlyDenied = !shouldShow
            }
        }
        scope.launch {
            snackbarHostState.showSnackbar(
                if (isGranted) context.getString(R.string.notifications_enabled)
                else context.getString(R.string.permission_denied)
            )
        }
    }

    // File picker for import
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.importData(it) }
    }

    // File picker for spreadsheet import
    val spreadsheetPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { viewModel.importFromSpreadsheet(it) }
    }

    // Show confirmation dialog for import
    var showImportConfirmDialog by remember { mutableStateOf(false) }

    // Show confirmation dialog for clear data
    var showClearConfirmDialog by remember { mutableStateOf(false) }

    // Handle export/import success/error messages
    LaunchedEffect(uiState.exportSuccess) {
        uiState.exportSuccess?.let {
            snackbarHostState.showSnackbar(
                context.getString(R.string.data_exported_to, it)
            )
            viewModel.resetExportImportState()
        }
    }

    LaunchedEffect(uiState.exportError) {
        uiState.exportError?.let {
            snackbarHostState.showSnackbar(
                context.getString(R.string.export_failed, it)
            )
            viewModel.resetExportImportState()
        }
    }

    LaunchedEffect(uiState.importSuccess) {
        uiState.importSuccess?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.resetExportImportState()
        }
    }

    LaunchedEffect(uiState.importError) {
        uiState.importError?.let {
            snackbarHostState.showSnackbar(
                context.getString(R.string.import_failed, it)
            )
            viewModel.resetExportImportState()
        }
    }

    // Show success snackbar when data generation completes
    LaunchedEffect(uiState.dataGenerationSuccess) {
        if (uiState.dataGenerationSuccess) {
            snackbarHostState.showSnackbar(context.getString(R.string.sample_data_generated))
            viewModel.resetDataGenerationSuccess()
        }
    }

    // Handle spreadsheet import success/error messages
    LaunchedEffect(uiState.spreadsheetImportSuccess) {
        uiState.spreadsheetImportSuccess?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.resetExportImportState()
        }
    }

    LaunchedEffect(uiState.spreadsheetImportError) {
        uiState.spreadsheetImportError?.let {
            snackbarHostState.showSnackbar(
                context.getString(R.string.spreadsheet_import_failed, it)
            )
            viewModel.resetExportImportState()
        }
    }

    LaunchedEffect(uiState.sampleFileSuccess) {
        uiState.sampleFileSuccess?.let {
            snackbarHostState.showSnackbar(
                context.getString(R.string.spreadsheet_sample_generated, it)
            )
            viewModel.resetExportImportState()
        }
    }

    LaunchedEffect(uiState.sampleFileError) {
        uiState.sampleFileError?.let {
            snackbarHostState.showSnackbar(
                context.getString(R.string.spreadsheet_sample_failed, it)
            )
            viewModel.resetExportImportState()
        }
    }

    // Import confirmation dialog
    if (showImportConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showImportConfirmDialog = false },
            title = { Text(stringResource(R.string.import_data_title)) },
            text = {
                Text(stringResource(R.string.import_data_confirm))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showImportConfirmDialog = false
                        filePickerLauncher.launch(arrayOf("application/json"))
                    }
                ) {
                    Text(stringResource(R.string.import_action))
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportConfirmDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    // Clear data confirmation dialog
    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = { Text(stringResource(R.string.clear_all_data_title)) },
            text = {
                Text(stringResource(R.string.clear_all_data_confirm))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearConfirmDialog = false
                        viewModel.clearAllData()
                    }
                ) {
                    Text(stringResource(R.string.clear), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }


    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings),
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        val configuration = LocalConfiguration.current
        val screenWidthDp = configuration.screenWidthDp
        val isTablet = screenWidthDp >= 600

        // Use centered content with max width on tablets for better readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = if (isTablet) Alignment.TopCenter else Alignment.TopStart
        ) {
            SettingsContent(
                isTablet = isTablet,
                modifier = Modifier.then(
                    if (isTablet) Modifier.fillMaxWidth(0.7f) // 70% width on tablets
                    else Modifier.fillMaxWidth()
                )
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // APPEARANCE Section
                SectionHeader(title = stringResource(R.string.settings_section_appearance))

                AppearanceCard(
                    currentTheme = uiState.appSettings.theme,
                    onThemeChange = viewModel::updateTheme
                )

                Spacer(modifier = Modifier.height(12.dp))

                ColorPaletteCard(
                    selectedPalette = selectedPalette,
                    onPaletteSelected = viewModel::updateColorPalette
                )

                Spacer(modifier = Modifier.height(8.dp))

                // LANGUAGE Section
                SectionHeader(title = stringResource(R.string.settings_section_language))

                LanguageCard(
                    selectedLanguage = uiState.appSettings.language,
                    onLanguageSelected = viewModel::updateLanguage
                )

                Spacer(modifier = Modifier.height(8.dp))

                // PREFERENCES Section
                SectionHeader(title = stringResource(R.string.settings_section_preferences))

                PreferencesCard(
                    notificationsEnabled = uiState.appSettings.notificationsEnabled && notificationPermissionGranted,
                    permissionPermanentlyDenied = permissionPermanentlyDenied,
                    onNotificationsToggle = { enabled ->
                        if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU && !notificationPermissionGranted) {
                            // Request permission first
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            viewModel.updateNotificationsEnabled(enabled)
                        }
                    },
                    onOpenSettings = {
                        PermissionUtil.openAppSettings(context)
                    },
                    onViewNotifications = onViewNotifications
                )

                Spacer(modifier = Modifier.height(8.dp))

                // DATA & STORAGE Section
                SectionHeader(title = stringResource(R.string.settings_section_data_storage))

                StorageCard(
                    storageInfo = uiState.storageInfo,
                    isExporting = uiState.isExporting,
                    isImporting = uiState.isImporting,
                    onExport = { viewModel.exportData() },
                    onImport = { showImportConfirmDialog = true },
                    onClear = { showClearConfirmDialog = true }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // SPREADSHEET IMPORT Section
                SectionHeader(title = stringResource(R.string.settings_section_spreadsheet_import))

                SpreadsheetImportCard(
                    isImporting = uiState.isSpreadsheetImporting,
                    isGeneratingSample = uiState.isGeneratingSampleFile,
                    onImport = {
                        spreadsheetPickerLauncher.launch(
                            arrayOf(
                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                                "application/vnd.ms-excel",
                                "text/csv",
                                "text/comma-separated-values"
                            )
                        )
                    },
                    onGenerateSample = { viewModel.generateSampleSpreadsheet() }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // CUSTOMIZATION Section
                SectionHeader(title = stringResource(R.string.settings_section_customization))

                CustomizationCard(
                    onManageExpenseCategoriesClick = onManageExpenseCategories
                )

                Spacer(modifier = Modifier.height(8.dp))

                // HELP & ABOUT Section
                SectionHeader(title = stringResource(R.string.settings_section_help_about))

                HelpAboutCard(
                    appVersion = uiState.appVersion,
                    onViewGuide = onViewGuide
                )

                // Debug Section (only visible in debug builds)
                if (uiState.isDebugMode) {
                    Spacer(modifier = Modifier.height(8.dp))
                    SectionHeader(title = stringResource(R.string.settings_section_developer_options))

                    DebugCard(
                        isGenerating = uiState.isGeneratingData,
                        onGenerateSampleData = {
                            viewModel.generateSampleData(
                                onSuccess = { },
                                onError = { error ->
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            context.getString(R.string.settings_error_format, error)
                                        )
                                    }
                                }
                            )
                        },
                        onTriggerReminderCheck = {
                            viewModel.triggerReminderCheck(
                                onSuccess = {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Reminder check triggered! Check notifications in a few seconds.")
                                    }
                                },
                                onError = { error ->
                                    scope.launch {
                                        snackbarHostState.showSnackbar(
                                            context.getString(R.string.settings_error_format, error)
                                        )
                                    }
                                }
                            )
                        }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
