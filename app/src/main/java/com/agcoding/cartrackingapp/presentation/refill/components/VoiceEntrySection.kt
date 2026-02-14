package com.agcoding.cartrackingapp.presentation.refill.components

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.presentation.refill.VoiceRefillState

/**
 * Voice entry FAB and state handling
 *
 * Usage in AddRefillBottomSheet:
 *
 * ```kotlin
 * val voiceState by viewModel.voiceState.collectAsState()
 *
 * VoiceEntrySection(
 *     voiceState = voiceState,
 *     onStartVoiceEntry = viewModel::startVoiceEntry,
 *     onConfirmParsedData = viewModel::confirmVoiceParsedData,
 *     onCancelVoiceEntry = viewModel::cancelVoiceEntry,
 *     onRetryVoiceEntry = viewModel::startVoiceEntry
 * )
 * ```
 */
@Composable
fun VoiceEntrySection(
    voiceState: VoiceRefillState,
    onStartVoiceEntry: () -> Unit,
    onStopVoiceRecording: () -> Unit,
    onConfirmParsedData: () -> Unit,
    onCancelVoiceEntry: () -> Unit,
    onRetryVoiceEntry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showPermissionRationale by remember { mutableStateOf(false) }

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onStartVoiceEntry()
        } else {
            showPermissionRationale = true
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // Voice entry button
        AnimatedVisibility(visible = voiceState is VoiceRefillState.Idle) {
            OutlinedButton(
                onClick = {
                    // Check permission and request if needed
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                        when {
                            context.checkSelfPermission(Manifest.permission.RECORD_AUDIO) ==
                                android.content.pm.PackageManager.PERMISSION_GRANTED -> {
                                onStartVoiceEntry()
                            }
                            else -> {
                                permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        }
                    } else {
                        onStartVoiceEntry()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.voice_entry_button))
            }
        }

        // Listening state with manual Stop control
        AnimatedVisibility(visible = voiceState is VoiceRefillState.Listening) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                VoiceListeningIndicator(
                    partialText = (voiceState as? VoiceRefillState.Listening)?.partialText ?: ""
                )

                // Action buttons: Stop (primary) and Cancel (secondary)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
                ) {
                    // Cancel button (outline style)
                    OutlinedButton(
                        onClick = onCancelVoiceEntry,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(stringResource(R.string.voice_cancel))
                    }

                    // Stop button (prominent filled style)
                    Button(
                        onClick = onStopVoiceRecording,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(stringResource(R.string.voice_stop_recording))
                    }
                }
            }
        }

        // Processing state
        AnimatedVisibility(visible = voiceState is VoiceRefillState.Processing) {
            Card(modifier = Modifier.fillMaxWidth()) {
                VoiceProcessingIndicator(
                    transcript = (voiceState as? VoiceRefillState.Processing)?.transcript ?: ""
                )
            }
        }

        // Error state
        AnimatedVisibility(visible = voiceState is VoiceRefillState.Error) {
            Card(modifier = Modifier.fillMaxWidth()) {
                val errorState = voiceState as? VoiceRefillState.Error
                VoiceErrorState(
                    message = errorState?.message ?: "",
                    transcript = errorState?.transcript ?: "",
                    onRetry = onRetryVoiceEntry,
                    onCancel = onCancelVoiceEntry
                )
            }
        }
    }

    // Parsed data confirmation dialog
    if (voiceState is VoiceRefillState.Parsed) {
        val parsedState = voiceState as? VoiceRefillState.Parsed
        if (parsedState != null) {
            VoiceConfirmationDialog(
                data = parsedState.data,
                lowConfidence = parsedState.lowConfidence,
                onConfirm = onConfirmParsedData,
                onEdit = {
                    onConfirmParsedData() // Still apply the data, user can edit in form
                },
                onCancel = onCancelVoiceEntry
            )
        }
    }

    // Permission rationale dialog
    if (showPermissionRationale) {
        AlertDialog(
            onDismissRequest = { showPermissionRationale = false },
            title = { Text(stringResource(R.string.voice_error_permission)) },
            text = {
                Text("Microphone access is needed to use voice entry. Please grant permission in settings.")
            },
            confirmButton = {
                TextButton(onClick = { showPermissionRationale = false }) {
                    Text("OK")
                }
            }
        )
    }
}

