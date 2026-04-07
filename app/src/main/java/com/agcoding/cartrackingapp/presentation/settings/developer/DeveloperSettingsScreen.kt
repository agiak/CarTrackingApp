package com.agcoding.cartrackingapp.presentation.settings.developer

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.presentation.components.StyledTopAppBar
import com.agcoding.cartrackingapp.presentation.settings.SettingsViewModel
import com.agcoding.cartrackingapp.presentation.settings.components.DebugCard
import com.agcoding.cartrackingapp.presentation.settings.components.LLMModelCard
import com.agcoding.cartrackingapp.presentation.settings.components.SectionHeader
import com.agcoding.cartrackingapp.presentation.settings.components.SettingsContent
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeveloperSettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // Capture string resources
    val sampleDataGenerated = stringResource(R.string.sample_data_generated)
    val errorFormatTemplate = stringResource(R.string.settings_error_format)

    // Show success snackbar when data generation completes
    LaunchedEffect(uiState.dataGenerationSuccess) {
        if (uiState.dataGenerationSuccess) {
            snackbarHostState.showSnackbar(
                message = sampleDataGenerated
            )
            viewModel.resetDataGenerationSuccess()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            StyledTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings_group_developer_title),
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

            // Voice & AI Section
            SectionHeader(title = "VOICE & AI")

            LLMModelCard(
                currentModel = uiState.appSettings.llmModel,
                onModelChange = { model ->
                    viewModel.updateLLMModel(model)
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = context.getString(
                                R.string.llm_model_applied,
                                model.displayName
                            )
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // DEBUG Section
            SectionHeader(title = stringResource(R.string.settings_section_developer_options))

            DebugCard(
                isGenerating = uiState.isGeneratingData,
                onGenerateSampleData = {
                    viewModel.generateSampleData(
                        onSuccess = { },
                        onError = { error ->
                            val errorMessage = String.format(errorFormatTemplate, error)
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = errorMessage
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
                            val errorMessage = String.format(errorFormatTemplate, error)
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    message = errorMessage
                                )
                            }
                        }
                    )
                },
                onSendTestNotification = {
                    viewModel.sendTestNotification(
                        onSuccess = {
                            scope.launch {
                                snackbarHostState.showSnackbar("Test notification sent! Check your notification shade.")
                            }
                        },
                        onError = { error ->
                            scope.launch {
                                snackbarHostState.showSnackbar(error)
                            }
                        }
                    )
                },
                onResetNotificationFlags = {
                    viewModel.resetNotificationFlags(
                        onSuccess = { count ->
                            scope.launch {
                                snackbarHostState.showSnackbar("Reset $count notification flag(s). Reminders can fire again.")
                            }
                        },
                        onError = { error ->
                            val errorMessage = String.format(errorFormatTemplate, error)
                            scope.launch {
                                snackbarHostState.showSnackbar(message = errorMessage)
                            }
                        }
                    )
                }
            )

            Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
