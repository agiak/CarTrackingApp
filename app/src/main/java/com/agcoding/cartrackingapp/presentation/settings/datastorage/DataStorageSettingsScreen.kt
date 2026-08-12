package com.agcoding.cartrackingapp.presentation.settings.datastorage
import androidx.compose.ui.tooling.preview.Preview
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.presentation.components.StyledCard
import com.agcoding.cartrackingapp.presentation.components.StyledTopAppBar
import com.agcoding.cartrackingapp.presentation.settings.SettingsViewModel
import com.agcoding.cartrackingapp.presentation.settings.components.ForecastCard
import com.agcoding.cartrackingapp.presentation.settings.components.SectionHeader
import com.agcoding.cartrackingapp.presentation.settings.components.SettingsContent
import com.agcoding.cartrackingapp.presentation.settings.components.SettingsRow

import com.agcoding.cartrackingapp.presentation.settings.components.StorageCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataStorageSettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToTrash: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // Capture string resources that will be used in LaunchedEffects
    val exportFailedTemplate = stringResource(R.string.export_failed)
    val exportExcelFailedTemplate = stringResource(R.string.export_excel_failed)
    val importFailedTemplate = stringResource(R.string.import_failed)
    val spreadsheetImportFailedTemplate = stringResource(R.string.spreadsheet_import_failed)
    val spreadsheetSampleFailedTemplate = stringResource(R.string.spreadsheet_sample_failed)
    val dataExportedToTemplate = stringResource(R.string.data_exported_to)
    val spreadsheetSampleGeneratedTemplate = stringResource(R.string.spreadsheet_sample_generated)

    // Refresh storage size when screen opens
    LaunchedEffect(Unit) {
        viewModel.refreshStorageSize()
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
        uiState.exportSuccess?.let { path ->
            snackbarHostState.showSnackbar(
                message = String.format(dataExportedToTemplate, path)
            )
            viewModel.resetExportImportState()
        }
    }

    LaunchedEffect(uiState.exportError) {
        uiState.exportError?.let { error ->
            snackbarHostState.showSnackbar(
                message = String.format(exportFailedTemplate, error)
            )
            viewModel.resetExportImportState()
        }
    }

    LaunchedEffect(uiState.exportExcelSuccess) {
        uiState.exportExcelSuccess?.let { path ->
            snackbarHostState.showSnackbar(
                message = String.format(dataExportedToTemplate, path)
            )
            viewModel.resetExportImportState()
        }
    }

    LaunchedEffect(uiState.exportExcelError) {
        uiState.exportExcelError?.let { error ->
            snackbarHostState.showSnackbar(
                message = String.format(exportExcelFailedTemplate, error)
            )
            viewModel.resetExportImportState()
        }
    }

    LaunchedEffect(uiState.importSuccess) {
        uiState.importSuccess?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.resetExportImportState()
        }
    }

    LaunchedEffect(uiState.importError) {
        uiState.importError?.let { error ->
            snackbarHostState.showSnackbar(
                message = String.format(importFailedTemplate, error)
            )
            viewModel.resetExportImportState()
        }
    }

    // Handle spreadsheet import success/error messages
    LaunchedEffect(uiState.spreadsheetImportSuccess) {
        uiState.spreadsheetImportSuccess?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.resetExportImportState()
        }
    }

    LaunchedEffect(uiState.spreadsheetImportError) {
        uiState.spreadsheetImportError?.let { error ->
            snackbarHostState.showSnackbar(
                message = String.format(spreadsheetImportFailedTemplate, error)
            )
            viewModel.resetExportImportState()
        }
    }

    LaunchedEffect(uiState.sampleFileSuccess) {
        uiState.sampleFileSuccess?.let { path ->
            snackbarHostState.showSnackbar(
                message = String.format(spreadsheetSampleGeneratedTemplate, path)
            )
            viewModel.resetExportImportState()
        }
    }

    LaunchedEffect(uiState.sampleFileError) {
        uiState.sampleFileError?.let { error ->
            snackbarHostState.showSnackbar(
                message = String.format(spreadsheetSampleFailedTemplate, error)
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
            StyledTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings_group_data_storage_title),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        val isTablet = com.agcoding.cartrackingapp.util.DeviceUtils.isTablet()

        // Use centered content with max width on tablets for better readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = if (isTablet) Alignment.TopCenter else Alignment.TopStart
        ) {
            SettingsContent(
                modifier = Modifier.then(
                    if (isTablet) Modifier.fillMaxWidth(0.7f) // 70% width on tablets
                    else Modifier.fillMaxWidth()
                )
            ) {
            Spacer(modifier = Modifier.height(8.dp))

            // DATA & STORAGE Section
            SectionHeader(title = stringResource(R.string.settings_section_data_storage))

            StorageCard(
                storageInfo = uiState.storageInfo,
                isExporting = uiState.isExporting,
                isExportingExcel = uiState.isExportingExcel,
                isImporting = uiState.isImporting,
                isSpreadsheetImporting = uiState.isSpreadsheetImporting,
                isGeneratingSample = uiState.isGeneratingSampleFile,
                onExport = { viewModel.exportData() },
                onExportExcel = { viewModel.exportToExcel() },
                onImportJson = { showImportConfirmDialog = true },
                onImportExcel = {
                    spreadsheetPickerLauncher.launch(
                        arrayOf(
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                            "application/vnd.ms-excel",
                            "text/csv",
                            "text/comma-separated-values"
                        )
                    )
                },
                onGenerateSample = { viewModel.generateSampleSpreadsheet() },
                onClear = { showClearConfirmDialog = true }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // FORECAST & PREDICTIONS Section
            SectionHeader(title = stringResource(R.string.forecast_predictions))

            ForecastCard(
                forecastingEnabled = uiState.appSettings.forecastingEnabled,
                onForecastingToggle = { viewModel.updateForecastingEnabled(it) }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // RECENTLY DELETED Section
            SectionHeader(title = stringResource(R.string.recently_deleted))

            StyledCard(modifier = Modifier.fillMaxWidth()) {
                SettingsRow(
                    icon = Icons.Default.Delete,
                    iconBackgroundColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                    iconTint = MaterialTheme.colorScheme.error,
                    title = stringResource(R.string.recently_deleted),
                    subtitle = stringResource(R.string.recently_deleted_desc),
                    onClick = onNavigateToTrash,
                    trailing = {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DataStorageSettingsScreenPreview() {
    CarTrackingAppTheme {
        DataStorageSettingsScreen(onNavigateBack = {})
    }
}
