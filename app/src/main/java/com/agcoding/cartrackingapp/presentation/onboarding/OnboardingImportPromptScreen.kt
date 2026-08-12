@file:OptIn(ExperimentalLayoutApi::class)

package com.agcoding.cartrackingapp.presentation.onboarding

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.presentation.settings.ImportSummary
import com.agcoding.cartrackingapp.presentation.settings.SettingsViewModel
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme
import com.agcoding.cartrackingapp.util.DeviceUtils

/** The distinct visual phases of the onboarding import flow. */
private enum class ImportPhase { PROMPT, IMPORTING, SUCCESS, ERROR }

/**
 * Shown once during onboarding, right after the permissions screen. Returning
 * users who already have a Caribou backup (JSON or Excel/CSV) can import it here
 * to restore their data before entering the app.
 *
 * The screen walks through four phases:
 *  - PROMPT: ask whether the user has previous data.
 *  - IMPORTING: a full-screen loader while the file is processed.
 *  - SUCCESS: an animated confirmation with a summary of what was imported and a
 *    button to enter the app.
 *  - ERROR: a clear error message with the option to try another file or skip.
 *
 * Both the success "continue" and the "start fresh" paths end onboarding via
 * [onFinished].
 */
@Composable
fun OnboardingImportPromptScreen(
    onFinished: () -> Unit,
    settingsViewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by settingsViewModel.uiState.collectAsState()

    var phase by remember { mutableStateOf(ImportPhase.PROMPT) }
    var summary by remember { mutableStateOf<ImportSummary?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            phase = ImportPhase.IMPORTING
            // Accept any file; the ViewModel detects JSON vs Excel/CSV and routes it.
            settingsViewModel.importFromFile(uri)
        }
    }

    // Import succeeded -> capture the summary, show the success screen.
    LaunchedEffect(uiState.importSuccess, uiState.spreadsheetImportSuccess) {
        val succeeded = uiState.importSuccess != null || uiState.spreadsheetImportSuccess != null
        if (phase == ImportPhase.IMPORTING && succeeded) {
            summary = uiState.importSummary ?: ImportSummary()
            phase = ImportPhase.SUCCESS
            settingsViewModel.resetExportImportState()
        }
    }

    // Import failed -> capture the error, show the error screen.
    LaunchedEffect(uiState.importError, uiState.spreadsheetImportError) {
        val error = uiState.importError ?: uiState.spreadsheetImportError
        if (phase == ImportPhase.IMPORTING && error != null) {
            errorMessage = error
            phase = ImportPhase.ERROR
            settingsViewModel.resetExportImportState()
        }
    }

    Scaffold(containerColor = MaterialTheme.colorScheme.background) { paddingValues ->
        val isTablet = DeviceUtils.isTablet()
        val isLandscape = DeviceUtils.isLandscape()
        val maxContentWidth = if (isTablet) 560.dp else 480.dp
        val constrainWidth = isTablet || isLandscape

        // Fill + scroll + center vertically: the content is centred when it fits and
        // becomes scrollable on short viewports (e.g. landscape phones). The inner
        // column caps its width and stays centred on tablets and landscape.
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = if (constrainWidth) {
                    Modifier.widthIn(max = maxContentWidth).fillMaxWidth()
                } else {
                    Modifier.fillMaxWidth()
                },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (phase) {
                    ImportPhase.PROMPT -> PromptContent(
                        onImportClick = { filePickerLauncher.launch(arrayOf("*/*")) },
                        onSkipClick = onFinished
                    )

                    ImportPhase.IMPORTING -> ImportingContent()

                    ImportPhase.SUCCESS -> SuccessContent(
                        summary = summary ?: ImportSummary(),
                        onContinue = onFinished
                    )

                    ImportPhase.ERROR -> ErrorContent(
                        message = errorMessage,
                        onTryAgain = { filePickerLauncher.launch(arrayOf("*/*")) },
                        onSkip = onFinished
                    )
                }
            }
        }
    }
}

@Composable
private fun PromptContent(
    onImportClick: () -> Unit,
    onSkipClick: () -> Unit
) {
    HeaderIcon(icon = Icons.Default.CloudDownload)

    Spacer(modifier = Modifier.height(24.dp))

    Text(
        text = stringResource(R.string.onboarding_import_title),
        fontSize = 28.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(12.dp))

    Text(
        text = stringResource(R.string.onboarding_import_message),
        fontSize = 16.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        lineHeight = 22.sp
    )

    Spacer(modifier = Modifier.height(40.dp))

    Button(
        onClick = onImportClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Text(
            text = stringResource(R.string.onboarding_import_yes),
            textAlign = TextAlign.Center
        )
    }

    Spacer(modifier = Modifier.height(8.dp))

    TextButton(
        onClick = onSkipClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = stringResource(R.string.onboarding_import_no))
    }
}

@Composable
private fun ImportingContent() {
    Spacer(modifier = Modifier.height(24.dp))

    CircularProgressIndicator(
        modifier = Modifier.size(64.dp),
        strokeWidth = 5.dp,
        color = MaterialTheme.colorScheme.primary
    )

    Spacer(modifier = Modifier.height(32.dp))

    Text(
        text = stringResource(R.string.onboarding_import_importing_title),
        fontSize = 22.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(12.dp))

    Text(
        text = stringResource(R.string.onboarding_import_importing_message),
        fontSize = 15.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        lineHeight = 21.sp
    )
}

@Composable
private fun SuccessContent(
    summary: ImportSummary,
    onContinue: () -> Unit
) {
    // Animated "pop" for the success check mark.
    val scale = remember { Animatable(0.4f) }
    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }

    Spacer(modifier = Modifier.height(16.dp))

    Box(
        modifier = Modifier
            .scale(scale.value)
            .size(96.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.tertiary),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            modifier = Modifier.size(52.dp),
            tint = Color.White
        )
    }

    Spacer(modifier = Modifier.height(24.dp))

    Text(
        text = stringResource(R.string.onboarding_import_success_title),
        fontSize = 28.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = stringResource(R.string.onboarding_import_success_message),
        fontSize = 16.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center
    )

    val stats = buildList {
        if (summary.cars > 0) add(R.string.onboarding_import_stat_cars to summary.cars)
        if (summary.refills > 0) add(R.string.onboarding_import_stat_refills to summary.refills)
        if (summary.expenses > 0) add(R.string.onboarding_import_stat_expenses to summary.expenses)
        if (summary.trips > 0) add(R.string.onboarding_import_stat_trips to summary.trips)
        if (summary.reminders > 0) add(R.string.onboarding_import_stat_reminders to summary.reminders)
    }

    if (stats.isNotEmpty()) {
        Spacer(modifier = Modifier.height(28.dp))
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            stats.forEach { (labelRes, count) ->
                StatPill(count = count, label = stringResource(labelRes))
            }
        }
    }

    Spacer(modifier = Modifier.height(40.dp))

    Button(
        onClick = onContinue,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Text(
            text = stringResource(R.string.onboarding_import_continue),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun StatPill(
    count: Int,
    label: String
) {
    Box(
        modifier = Modifier
            .padding(horizontal = 6.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = count.toString(),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ErrorContent(
    message: String?,
    onTryAgain: () -> Unit,
    onSkip: () -> Unit
) {
    Spacer(modifier = Modifier.height(16.dp))

    Box(
        modifier = Modifier
            .size(88.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.ErrorOutline,
            contentDescription = null,
            modifier = Modifier.size(48.dp),
            tint = MaterialTheme.colorScheme.error
        )
    }

    Spacer(modifier = Modifier.height(24.dp))

    Text(
        text = stringResource(R.string.onboarding_import_error_title),
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onBackground,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(12.dp))

    Text(
        text = message ?: stringResource(R.string.import_unsupported_file),
        fontSize = 15.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        lineHeight = 21.sp
    )

    Spacer(modifier = Modifier.height(40.dp))

    Button(
        onClick = onTryAgain,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Text(
            text = stringResource(R.string.onboarding_import_try_again),
            textAlign = TextAlign.Center
        )
    }

    Spacer(modifier = Modifier.height(8.dp))

    TextButton(
        onClick = onSkip,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = stringResource(R.string.onboarding_import_no))
    }
}

@Composable
private fun HeaderIcon(icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Box(
        modifier = Modifier
            .size(80.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(40.dp),
            tint = MaterialTheme.colorScheme.primary
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun OnboardingImportPromptScreenPreview() {
    CarTrackingAppTheme {
        OnboardingImportPromptScreen(onFinished = {})
    }
}

@Preview(showBackground = true, name = "Success")
@Composable
private fun SuccessContentPreview() {
    CarTrackingAppTheme {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            SuccessContent(
                summary = ImportSummary(cars = 3, refills = 45, expenses = 15, trips = 8),
                onContinue = {}
            )
        }
    }
}
