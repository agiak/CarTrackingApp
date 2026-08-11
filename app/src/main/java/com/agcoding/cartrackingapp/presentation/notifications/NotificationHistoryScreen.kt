package com.agcoding.cartrackingapp.presentation.notifications

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.presentation.components.StyledTopAppBar
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationHistoryScreen(
    onNavigateBack: () -> Unit,
    viewModel: NotificationHistoryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    var showClearDialog by remember { mutableStateOf(false) }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text(stringResource(R.string.notification_history_clear_title)) },
            text = { Text(stringResource(R.string.notification_history_clear_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearHistory()
                        showClearDialog = false
                    }
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            StyledTopAppBar(
                title = { Text(stringResource(R.string.notification_history_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                actions = {
                    if (uiState is NotificationHistoryUiState.Success) {
                        IconButton(onClick = { showClearDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.DeleteSweep,
                                contentDescription = stringResource(R.string.notification_history_clear_title)
                            )
                        }
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        val isTablet = com.agcoding.cartrackingapp.util.DeviceUtils.isTablet()
        val isLandscape = com.agcoding.cartrackingapp.util.DeviceUtils.isLandscape()
        val useCenteredLayout = isTablet || isLandscape

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = if (useCenteredLayout) Alignment.TopCenter else Alignment.TopStart
        ) {
            when (val state = uiState) {
                is NotificationHistoryUiState.Loading -> {
                    Box(
                        modifier = Modifier
                            .then(
                                if (useCenteredLayout) Modifier.fillMaxWidth(0.7f)
                                else Modifier.fillMaxWidth()
                            )
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is NotificationHistoryUiState.Empty -> {
                    EmptyHistoryState(
                        modifier = Modifier
                            .then(
                                if (useCenteredLayout) Modifier.fillMaxWidth(0.7f)
                                else Modifier.fillMaxWidth()
                            )
                            .fillMaxSize()
                    )
                }

                is NotificationHistoryUiState.Success -> {
                    NotificationHistoryContent(
                        notifications = state.notifications,
                        useCenteredLayout = useCenteredLayout,
                        modifier = Modifier
                            .then(
                                if (useCenteredLayout) Modifier.fillMaxWidth(0.7f)
                                else Modifier.fillMaxWidth()
                            )
                            .fillMaxSize()
                    )
                }

                is NotificationHistoryUiState.Error -> {
                    Box(
                        modifier = Modifier
                            .then(
                                if (useCenteredLayout) Modifier.fillMaxWidth(0.7f)
                                else Modifier.fillMaxWidth()
                            )
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = state.message,
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            TextButton(onClick = { viewModel.retry() }) {
                                Text(stringResource(R.string.retry))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun NotificationHistoryScreenPreview() {
    CarTrackingAppTheme {
        NotificationHistoryScreen(
            onNavigateBack = {}
        )
    }
}
