package com.agcoding.cartrackingapp.presentation.settings

import android.Manifest
import android.app.Activity
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.agcoding.cartrackingapp.data.preferences.AppLanguage
import com.agcoding.cartrackingapp.data.preferences.AppTheme
import com.agcoding.cartrackingapp.data.preferences.ColorPalette
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
    val scrollState = rememberScrollState()
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
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
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
            Column(
                modifier = Modifier
                    .then(
                        if (isTablet) Modifier.fillMaxWidth(0.7f) // 70% width on tablets
                        else Modifier.fillMaxWidth()
                    )
                    .verticalScroll(scrollState)
                    .padding(horizontal = if (isTablet) 24.dp else 16.dp), // More padding on tablets
                verticalArrangement = Arrangement.spacedBy(8.dp)
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

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 12.sp,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        letterSpacing = 0.5.sp,
        modifier = Modifier.padding(start = 4.dp, top = 8.dp, bottom = 4.dp)
    )
}

@Composable
private fun AppearanceCard(
    currentTheme: AppTheme,
    onThemeChange: (AppTheme) -> Unit
) {
    val systemInDarkTheme = isSystemInDarkTheme()
    val isDarkActive = when (currentTheme) {
        AppTheme.DARK -> true
        AppTheme.LIGHT -> false
        AppTheme.SYSTEM -> systemInDarkTheme
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Icon with background
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isDarkActive) Icons.Default.DarkMode else Icons.Default.LightMode,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = stringResource(R.string.appearance_dark_mode),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = when (currentTheme) {
                            AppTheme.SYSTEM -> stringResource(R.string.settings_following_system)
                            AppTheme.LIGHT -> stringResource(R.string.appearance_light_theme_active)
                            AppTheme.DARK -> stringResource(R.string.settings_dark_theme_active)
                        },
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Switch(
                checked = isDarkActive,
                onCheckedChange = { enabled ->
                    onThemeChange(if (enabled) AppTheme.DARK else AppTheme.LIGHT)
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}

@Composable
private fun LanguageCard(
    selectedLanguage: AppLanguage,
    onLanguageSelected: (AppLanguage) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Language,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = stringResource(R.string.settings_app_language),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.language_choose),
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            AppLanguage.entries.forEach { language ->
                LanguageOption(
                    language = language,
                    isSelected = selectedLanguage == language,
                    onSelect = { onLanguageSelected(language) }
                )
            }
        }
    }
}

@Composable
private fun LanguageOption(
    language: AppLanguage,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onSelect)
            .padding(vertical = 8.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Dot indicator for selected
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(
                    if (isSelected) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.outlineVariant
                )
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = when (language) {
                AppLanguage.ENGLISH -> stringResource(R.string.language_english)
                AppLanguage.GREEK -> stringResource(R.string.language_greek)
            },
            fontSize = 15.sp,
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun PreferencesCard(
    notificationsEnabled: Boolean,
    permissionPermanentlyDenied: Boolean,
    onNotificationsToggle: (Boolean) -> Unit,
    onOpenSettings: () -> Unit,
    onViewNotifications: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Notifications Row
            SettingsRow(
                icon = Icons.Default.Notifications,
                iconBackgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                iconTint = MaterialTheme.colorScheme.primary,
                title = stringResource(R.string.preferences_notifications),
                subtitle = if (notificationsEnabled) stringResource(R.string.settings_notifications_enabled) else stringResource(R.string.settings_notifications_disabled),
                trailing = {
                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = onNotificationsToggle,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            )

            // Show helper text and button when permission is permanently denied
            if (permissionPermanentlyDenied && !notificationsEnabled) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.notification_permission_denied_helper),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.error,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = onOpenSettings,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(stringResource(R.string.open_settings))
                    }
                }
            }

            // Divider
            androidx.compose.material3.HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            // View Reminders Row
            SettingsRow(
                icon = Icons.Default.Event,
                iconBackgroundColor = if (notificationsEnabled) {
                    MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.05f)
                },
                iconTint = if (notificationsEnabled) {
                    MaterialTheme.colorScheme.tertiary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                },
                title = stringResource(R.string.preferences_view_reminders),
                subtitle = stringResource(R.string.preferences_view_reminders_desc),
                trailing = {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = if (notificationsEnabled) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        }
                    )
                },
                onClick = if (notificationsEnabled) onViewNotifications else null,
                enabled = notificationsEnabled
            )
        }
    }
}

@Composable
private fun StorageCard(
    storageInfo: StorageInfo,
    isExporting: Boolean = false,
    isImporting: Boolean = false,
    onExport: () -> Unit = {},
    onImport: () -> Unit = {},
    onClear: () -> Unit = {}
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Storage,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = stringResource(R.string.data_storage_app_storage),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.settings_storage_used_format, storageInfo.formattedTotalSize),
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // App Data row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.data_storage_app_data),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = storageInfo.formattedDataSize,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Cache row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.data_storage_cache),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = storageInfo.formattedCacheSize,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Export/Import explanation
            Text(
                text = stringResource(R.string.settings_backup_transfer_title),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = stringResource(R.string.settings_backup_transfer_desc),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Action buttons row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StorageActionButton(
                    icon = Icons.Default.Upload,
                    text = if (isExporting) stringResource(R.string.settings_exporting) else stringResource(R.string.export),
                    onClick = onExport,
                    enabled = !isExporting && !isImporting,
                    isLoading = isExporting,
                    modifier = Modifier.weight(1f)
                )
                StorageActionButton(
                    icon = Icons.Default.Download,
                    text = if (isImporting) stringResource(R.string.settings_importing) else stringResource(R.string.import_action),
                    onClick = onImport,
                    enabled = !isExporting && !isImporting,
                    isLoading = isImporting,
                    modifier = Modifier.weight(1f)
                )
                StorageActionButton(
                    icon = Icons.Default.Delete,
                    text = stringResource(R.string.clear),
                    onClick = onClear,
                    enabled = !isExporting && !isImporting,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.settings_import_replaces_data_warning),
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
private fun StorageActionButton(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isLoading: Boolean = false
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = RoundedCornerShape(8.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.size(14.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.primary
            )
        } else {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
            )
        }
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            fontSize = 12.sp,
            color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        )
    }
}

@Composable
private fun HelpAboutCard(
    appVersion: String,
    onViewGuide: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // View App Guide
            SettingsRow(
                icon = Icons.AutoMirrored.Filled.Help,
                iconBackgroundColor = Color(0xFF4CAF50).copy(alpha = 0.1f),
                iconTint = Color(0xFF4CAF50),
                title = stringResource(R.string.settings_view_app_guide),
                subtitle = stringResource(R.string.settings_learn_how_to_use_the_app),
                onClick = onViewGuide,
                trailing = {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            )

            // Divider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            )

            // About
            SettingsRow(
                icon = Icons.Default.Info,
                iconBackgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                iconTint = MaterialTheme.colorScheme.primary,
                title = stringResource(R.string.settings_about),
                subtitle = null,
                trailing = {
                    Column(horizontalAlignment = Alignment.End) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = stringResource(R.string.version),
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = appVersion,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = stringResource(R.string.offline_first),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        }
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    iconBackgroundColor: Color,
    iconTint: Color,
    title: String,
    subtitle: String?,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    trailing: @Composable () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (onClick != null && enabled) Modifier.clickable(onClick = onClick)
                else Modifier
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(iconBackgroundColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = iconTint
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = if (enabled) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                }
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = if (enabled) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    }
                )
            }
        }
        trailing()
    }
}

@Composable
private fun DebugCard(
    isGenerating: Boolean,
    onGenerateSampleData: () -> Unit,
    onTriggerReminderCheck: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.settings_section_developer_options),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Sample Data Generation
            Text(
                text = stringResource(R.string.settings_sample_data_details),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onGenerateSampleData,
                enabled = !isGenerating,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.settings_generating))
                } else {
                    Text(stringResource(R.string.settings_generate_sample_data))
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.settings_sample_data_details),
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 14.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Trigger Reminder Check Button
            OutlinedButton(
                onClick = onTriggerReminderCheck,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Check Reminders Now",
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Manually trigger reminder check worker (for testing)",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 14.sp
            )
        }
    }
}

@Composable
private fun CustomizationCard(
    onManageExpenseCategoriesClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Expense Categories Row
            SettingsRow(
                icon = Icons.Default.Category, // Placeholder - you can change this
                iconBackgroundColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f),
                iconTint = MaterialTheme.colorScheme.primary,
                title = stringResource(R.string.customization_expense_categories),
                subtitle = stringResource(R.string.customization_expense_categories_desc),
                trailing = {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                onClick = onManageExpenseCategoriesClick
            )
        }
    }
}

@Composable
private fun SpreadsheetImportCard(
    isImporting: Boolean,
    isGeneratingSample: Boolean,
    onImport: () -> Unit,
    onGenerateSample: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header with icon
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Upload,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = stringResource(R.string.spreadsheet_import_title),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.spreadsheet_file_format),
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Description
            Text(
                text = stringResource(R.string.spreadsheet_import_description),
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // How to use section
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = stringResource(R.string.spreadsheet_import_how_to_title),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = stringResource(R.string.spreadsheet_import_how_to_1),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = stringResource(R.string.spreadsheet_import_how_to_2),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = stringResource(R.string.spreadsheet_import_how_to_3),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Generate Sample Button
                OutlinedButton(
                    onClick = onGenerateSample,
                    enabled = !isGeneratingSample && !isImporting,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    if (isGeneratingSample) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isGeneratingSample) stringResource(R.string.spreadsheet_generating) else stringResource(R.string.spreadsheet_generate_sample),
                        fontSize = 12.sp,
                        maxLines = 1
                    )
                }

                // Import Button
                Button(
                    onClick = onImport,
                    enabled = !isImporting && !isGeneratingSample,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    if (isImporting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Upload,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isImporting) stringResource(R.string.spreadsheet_importing) else stringResource(R.string.spreadsheet_import_button),
                        fontSize = 12.sp,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Note
            Text(
                text = stringResource(R.string.spreadsheet_import_note),
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                lineHeight = 14.sp
            )
        }
    }
}

@Composable
private fun ColorPaletteCard(
    selectedPalette: ColorPalette,
    onPaletteSelected: (ColorPalette) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Title
            Text(
                text = stringResource(R.string.color_palette_title),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Description
            Text(
                text = stringResource(R.string.color_palette_choose_description),
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // System Colors Button
            PaletteOptionButton(
                title = stringResource(R.string.color_palette_wallpaper_colors),
                palette = ColorPalette.SYSTEM,
                isSelected = selectedPalette == ColorPalette.SYSTEM,
                onClick = { onPaletteSelected(ColorPalette.SYSTEM) },
                showSystemIcon = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Other Colors Label
            Text(
                text = stringResource(R.string.color_palette_other_colors),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Color Palette Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ColorPaletteItem(
                    palette = ColorPalette.DEFAULT_BLUE,
                    primaryColor = Color(0xFF1976D2),
                    secondaryColor = Color(0xFF0288D1),
                    isSelected = selectedPalette == ColorPalette.DEFAULT_BLUE,
                    onClick = { onPaletteSelected(ColorPalette.DEFAULT_BLUE) },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                ColorPaletteItem(
                    palette = ColorPalette.SUNSET_ORANGE,
                    primaryColor = Color(0xFFFF6F00),
                    secondaryColor = Color(0xFFFF8F00),
                    isSelected = selectedPalette == ColorPalette.SUNSET_ORANGE,
                    onClick = { onPaletteSelected(ColorPalette.SUNSET_ORANGE) },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                ColorPaletteItem(
                    palette = ColorPalette.FOREST_GREEN,
                    primaryColor = Color(0xFF2E7D32),
                    secondaryColor = Color(0xFF388E3C),
                    isSelected = selectedPalette == ColorPalette.FOREST_GREEN,
                    onClick = { onPaletteSelected(ColorPalette.FOREST_GREEN) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ColorPaletteItem(
                    palette = ColorPalette.ROYAL_PURPLE,
                    primaryColor = Color(0xFF6A1B9A),
                    secondaryColor = Color(0xFF8E24AA),
                    isSelected = selectedPalette == ColorPalette.ROYAL_PURPLE,
                    onClick = { onPaletteSelected(ColorPalette.ROYAL_PURPLE) },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                ColorPaletteItem(
                    palette = ColorPalette.OCEAN_TEAL,
                    primaryColor = Color(0xFF00796B),
                    secondaryColor = Color(0xFF00897B),
                    isSelected = selectedPalette == ColorPalette.OCEAN_TEAL,
                    onClick = { onPaletteSelected(ColorPalette.OCEAN_TEAL) },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                ColorPaletteItem(
                    palette = ColorPalette.CRIMSON_RED,
                    primaryColor = Color(0xFFC62828),
                    secondaryColor = Color(0xFFD32F2F),
                    isSelected = selectedPalette == ColorPalette.CRIMSON_RED,
                    onClick = { onPaletteSelected(ColorPalette.CRIMSON_RED) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ColorPaletteItem(
                    palette = ColorPalette.AMBER_GOLD,
                    primaryColor = Color(0xFFFF8F00),
                    secondaryColor = Color(0xFFFFA000),
                    isSelected = selectedPalette == ColorPalette.AMBER_GOLD,
                    onClick = { onPaletteSelected(ColorPalette.AMBER_GOLD) },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                ColorPaletteItem(
                    palette = ColorPalette.DEEP_INDIGO,
                    primaryColor = Color(0xFF283593),
                    secondaryColor = Color(0xFF3949AB),
                    isSelected = selectedPalette == ColorPalette.DEEP_INDIGO,
                    onClick = { onPaletteSelected(ColorPalette.DEEP_INDIGO) },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                ColorPaletteItem(
                    palette = ColorPalette.SLATE_GRAY,
                    primaryColor = Color(0xFF455A64),
                    secondaryColor = Color(0xFF546E7A),
                    isSelected = selectedPalette == ColorPalette.SLATE_GRAY,
                    onClick = { onPaletteSelected(ColorPalette.SLATE_GRAY) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ColorPaletteItem(
                    palette = ColorPalette.ROSE_PINK,
                    primaryColor = Color(0xFFAD1457),
                    secondaryColor = Color(0xFFC2185B),
                    isSelected = selectedPalette == ColorPalette.ROSE_PINK,
                    onClick = { onPaletteSelected(ColorPalette.ROSE_PINK) },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                ColorPaletteItem(
                    palette = ColorPalette.MINT_BREEZE,
                    primaryColor = Color(0xFF00897B),
                    secondaryColor = Color(0xFF26A69A),
                    isSelected = selectedPalette == ColorPalette.MINT_BREEZE,
                    onClick = { onPaletteSelected(ColorPalette.MINT_BREEZE) },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                ColorPaletteItem(
                    palette = ColorPalette.LAVENDER_DREAM,
                    primaryColor = Color(0xFF7B1FA2),
                    secondaryColor = Color(0xFF9C27B0),
                    isSelected = selectedPalette == ColorPalette.LAVENDER_DREAM,
                    onClick = { onPaletteSelected(ColorPalette.LAVENDER_DREAM) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ColorPaletteItem(
                    palette = ColorPalette.CORAL_SUNSET,
                    primaryColor = Color(0xFFE64A19),
                    secondaryColor = Color(0xFFFF5722),
                    isSelected = selectedPalette == ColorPalette.CORAL_SUNSET,
                    onClick = { onPaletteSelected(ColorPalette.CORAL_SUNSET) },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                ColorPaletteItem(
                    palette = ColorPalette.EMERALD_FOREST,
                    primaryColor = Color(0xFF1B5E20),
                    secondaryColor = Color(0xFF2E7D32),
                    isSelected = selectedPalette == ColorPalette.EMERALD_FOREST,
                    onClick = { onPaletteSelected(ColorPalette.EMERALD_FOREST) },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                ColorPaletteItem(
                    palette = ColorPalette.ELECTRIC_CYAN,
                    primaryColor = Color(0xFF0097A7),
                    secondaryColor = Color(0xFF00ACC1),
                    isSelected = selectedPalette == ColorPalette.ELECTRIC_CYAN,
                    onClick = { onPaletteSelected(ColorPalette.ELECTRIC_CYAN) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ColorPaletteItem(
                    palette = ColorPalette.MIDNIGHT_BLACK,
                    primaryColor = Color(0xFF212121),
                    secondaryColor = Color(0xFF424242),
                    isSelected = selectedPalette == ColorPalette.MIDNIGHT_BLACK,
                    onClick = { onPaletteSelected(ColorPalette.MIDNIGHT_BLACK) },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                ColorPaletteItem(
                    palette = ColorPalette.ICE_WHITE,
                    primaryColor = Color(0xFF37474F),
                    secondaryColor = Color(0xFFB0BEC5),
                    isSelected = selectedPalette == ColorPalette.ICE_WHITE,
                    onClick = { onPaletteSelected(ColorPalette.ICE_WHITE) },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                ColorPaletteItem(
                    palette = ColorPalette.NEON_MAGENTA,
                    primaryColor = Color(0xFFC2185B),
                    secondaryColor = Color(0xFFEC407A),
                    isSelected = selectedPalette == ColorPalette.NEON_MAGENTA,
                    onClick = { onPaletteSelected(ColorPalette.NEON_MAGENTA) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ColorPaletteItem(
                    palette = ColorPalette.DARK_OLIVE,
                    primaryColor = Color(0xFF33691E),
                    secondaryColor = Color(0xFF558B2F),
                    isSelected = selectedPalette == ColorPalette.DARK_OLIVE,
                    onClick = { onPaletteSelected(ColorPalette.DARK_OLIVE) },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                ColorPaletteItem(
                    palette = ColorPalette.VOLCANIC_ASH,
                    primaryColor = Color(0xFF263238),
                    secondaryColor = Color(0xFF455A64),
                    isSelected = selectedPalette == ColorPalette.VOLCANIC_ASH,
                    onClick = { onPaletteSelected(ColorPalette.VOLCANIC_ASH) },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                ColorPaletteItem(
                    palette = ColorPalette.SUNSET_FIRE,
                    primaryColor = Color(0xFFFF6D00),
                    secondaryColor = Color(0xFF4A148C),
                    isSelected = selectedPalette == ColorPalette.SUNSET_FIRE,
                    onClick = { onPaletteSelected(ColorPalette.SUNSET_FIRE) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ColorPaletteItem(
                    palette = ColorPalette.TROPICAL_PARADISE,
                    primaryColor = Color(0xFF00BFA5),
                    secondaryColor = Color(0xFFE91E63),
                    isSelected = selectedPalette == ColorPalette.TROPICAL_PARADISE,
                    onClick = { onPaletteSelected(ColorPalette.TROPICAL_PARADISE) },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                ColorPaletteItem(
                    palette = ColorPalette.ROYAL_GOLD,
                    primaryColor = Color(0xFFF57F17),
                    secondaryColor = Color(0xFF0D47A1),
                    isSelected = selectedPalette == ColorPalette.ROYAL_GOLD,
                    onClick = { onPaletteSelected(ColorPalette.ROYAL_GOLD) },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                ColorPaletteItem(
                    palette = ColorPalette.BERRY_BLAST,
                    primaryColor = Color(0xFF6A1B9A),
                    secondaryColor = Color(0xFF9E9D24),
                    isSelected = selectedPalette == ColorPalette.BERRY_BLAST,
                    onClick = { onPaletteSelected(ColorPalette.BERRY_BLAST) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ColorPaletteItem(
                    palette = ColorPalette.NEON_NIGHT,
                    primaryColor = Color(0xFF0091EA),
                    secondaryColor = Color(0xFFD500F9),
                    isSelected = selectedPalette == ColorPalette.NEON_NIGHT,
                    onClick = { onPaletteSelected(ColorPalette.NEON_NIGHT) },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                ColorPaletteItem(
                    palette = ColorPalette.AUTUMN_HARVEST,
                    primaryColor = Color(0xFFE65100),
                    secondaryColor = Color(0xFF4E342E),
                    isSelected = selectedPalette == ColorPalette.AUTUMN_HARVEST,
                    onClick = { onPaletteSelected(ColorPalette.AUTUMN_HARVEST) },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                ColorPaletteItem(
                    palette = ColorPalette.ARCTIC_FROST,
                    primaryColor = Color(0xFF006064),
                    secondaryColor = Color(0xFF0277BD),
                    isSelected = selectedPalette == ColorPalette.ARCTIC_FROST,
                    onClick = { onPaletteSelected(ColorPalette.ARCTIC_FROST) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ColorPaletteItem(
                    palette = ColorPalette.CHERRY_BLOSSOM,
                    primaryColor = Color(0xFFD32F2F),
                    secondaryColor = Color(0xFFC2185B),
                    isSelected = selectedPalette == ColorPalette.CHERRY_BLOSSOM,
                    onClick = { onPaletteSelected(ColorPalette.CHERRY_BLOSSOM) },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                ColorPaletteItem(
                    palette = ColorPalette.EMERALD_SEA,
                    primaryColor = Color(0xFF00695C),
                    secondaryColor = Color(0xFF01579B),
                    isSelected = selectedPalette == ColorPalette.EMERALD_SEA,
                    onClick = { onPaletteSelected(ColorPalette.EMERALD_SEA) },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                ColorPaletteItem(
                    palette = ColorPalette.GOLDEN_HOUR,
                    primaryColor = Color(0xFFF57C00),
                    secondaryColor = Color(0xFF6A1B9A),
                    isSelected = selectedPalette == ColorPalette.GOLDEN_HOUR,
                    onClick = { onPaletteSelected(ColorPalette.GOLDEN_HOUR) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ColorPaletteItem(
                    palette = ColorPalette.NEON_LIME,
                    primaryColor = Color(0xFFAEEA00),
                    secondaryColor = Color(0xFF4A148C),
                    isSelected = selectedPalette == ColorPalette.NEON_LIME,
                    onClick = { onPaletteSelected(ColorPalette.NEON_LIME) },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                ColorPaletteItem(
                    palette = ColorPalette.HOT_LAVA,
                    primaryColor = Color(0xFFDD2C00),
                    secondaryColor = Color(0xFF212121),
                    isSelected = selectedPalette == ColorPalette.HOT_LAVA,
                    onClick = { onPaletteSelected(ColorPalette.HOT_LAVA) },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                ColorPaletteItem(
                    palette = ColorPalette.CYBER_PINK,
                    primaryColor = Color(0xFFF50057),
                    secondaryColor = Color(0xFF00BFA5),
                    isSelected = selectedPalette == ColorPalette.CYBER_PINK,
                    onClick = { onPaletteSelected(ColorPalette.CYBER_PINK) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ColorPaletteItem(
                    palette = ColorPalette.OCEAN_SUNSET,
                    primaryColor = Color(0xFF01579B),
                    secondaryColor = Color(0xFFFF6E40),
                    isSelected = selectedPalette == ColorPalette.OCEAN_SUNSET,
                    onClick = { onPaletteSelected(ColorPalette.OCEAN_SUNSET) },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                ColorPaletteItem(
                    palette = ColorPalette.FOREST_AMBER,
                    primaryColor = Color(0xFF1B5E20),
                    secondaryColor = Color(0xFFFF6F00),
                    isSelected = selectedPalette == ColorPalette.FOREST_AMBER,
                    onClick = { onPaletteSelected(ColorPalette.FOREST_AMBER) },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                ColorPaletteItem(
                    palette = ColorPalette.SAPPHIRE_ROSE,
                    primaryColor = Color(0xFF1A237E),
                    secondaryColor = Color(0xFFE91E63),
                    isSelected = selectedPalette == ColorPalette.SAPPHIRE_ROSE,
                    onClick = { onPaletteSelected(ColorPalette.SAPPHIRE_ROSE) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ColorPaletteItem(
                    palette = ColorPalette.ELECTRIC_VIOLET,
                    primaryColor = Color(0xFF6200EA),
                    secondaryColor = Color(0xFFFFEA00),
                    isSelected = selectedPalette == ColorPalette.ELECTRIC_VIOLET,
                    onClick = { onPaletteSelected(ColorPalette.ELECTRIC_VIOLET) },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                ColorPaletteItem(
                    palette = ColorPalette.CANDY_CRUSH,
                    primaryColor = Color(0xFFE91E63),
                    secondaryColor = Color(0xFF00BCD4),
                    isSelected = selectedPalette == ColorPalette.CANDY_CRUSH,
                    onClick = { onPaletteSelected(ColorPalette.CANDY_CRUSH) },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                ColorPaletteItem(
                    palette = ColorPalette.MIDNIGHT_SUN,
                    primaryColor = Color(0xFF0D47A1),
                    secondaryColor = Color(0xFFFFD600),
                    isSelected = selectedPalette == ColorPalette.MIDNIGHT_SUN,
                    onClick = { onPaletteSelected(ColorPalette.MIDNIGHT_SUN) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ColorPaletteItem(
                    palette = ColorPalette.STRAWBERRY_MINT,
                    primaryColor = Color(0xFFD32F2F),
                    secondaryColor = Color(0xFF00BFA5),
                    isSelected = selectedPalette == ColorPalette.STRAWBERRY_MINT,
                    onClick = { onPaletteSelected(ColorPalette.STRAWBERRY_MINT) },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                // Empty space to balance the row
                Spacer(modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.width(12.dp))
                // Empty space to balance the row
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun PaletteOptionButton(
    title: String,
    palette: ColorPalette,
    isSelected: Boolean,
    onClick: () -> Unit,
    showSystemIcon: Boolean = false
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.outlineVariant
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (showSystemIcon) {
                Icon(
                    imageVector = Icons.Default.DarkMode,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
            }
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun ColorPaletteItem(
    palette: ColorPalette,
    primaryColor: Color,
    secondaryColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(
                    width = if (isSelected) 3.dp else 1.dp,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.outlineVariant
                    },
                    shape = RoundedCornerShape(16.dp)
                )
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            // Split circle showing both colors
            Row(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .background(primaryColor)
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .background(secondaryColor)
                )
            }

            // Checkmark overlay when selected
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
