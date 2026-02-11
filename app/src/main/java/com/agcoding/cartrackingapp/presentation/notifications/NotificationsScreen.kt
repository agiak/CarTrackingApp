package com.agcoding.cartrackingapp.presentation.notifications

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.presentation.components.StyledTopAppBar
import com.agcoding.cartrackingapp.presentation.notifications.components.EmptyNotificationsState
import com.agcoding.cartrackingapp.presentation.notifications.components.ErrorNotificationsState
import com.agcoding.cartrackingapp.presentation.notifications.components.NotificationsContent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onNavigateBack: () -> Unit,
    onEditExpense: (Long) -> Unit,
    viewModel: NotificationsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            StyledTopAppBar(
                title = { Text(stringResource(R.string.notifications_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        val isTablet = com.agcoding.cartrackingapp.util.DeviceUtils.isTablet()
        val isLandscape = com.agcoding.cartrackingapp.util.DeviceUtils.isLandscape()
        val useCenteredLayout = isTablet || isLandscape

        // Use centered content with max width on tablets and landscape
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = if (useCenteredLayout) Alignment.TopCenter else Alignment.TopStart
        ) {
            when (val state = uiState) {
                is NotificationsUiState.Loading -> {
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

                is NotificationsUiState.Empty -> {
                    EmptyNotificationsState(
                        modifier = Modifier
                            .then(
                                if (useCenteredLayout) Modifier.fillMaxWidth(0.7f)
                                else Modifier.fillMaxWidth()
                            )
                            .fillMaxSize()
                    )
                }

                is NotificationsUiState.Success -> {
                    NotificationsContent(
                        reminders = state.reminders,
                        onToggleReminder = { expenseId, enabled ->
                            viewModel.toggleReminderEnabled(expenseId, enabled)
                        },
                        onEditReminder = onEditExpense,
                        onDismissReminder = { expenseId ->
                            viewModel.dismissReminder(expenseId)
                        },
                        useCenteredLayout = useCenteredLayout,
                        modifier = Modifier
                            .then(
                                if (useCenteredLayout) Modifier.fillMaxWidth(0.7f)
                                else Modifier.fillMaxWidth()
                            )
                            .fillMaxSize()
                    )
                }

                is NotificationsUiState.Error -> {
                    ErrorNotificationsState(
                        message = state.message,
                        onRetry = { viewModel.retry() },
                        modifier = Modifier
                            .then(
                                if (useCenteredLayout) Modifier.fillMaxWidth(0.7f)
                                else Modifier.fillMaxWidth()
                            )
                            .fillMaxSize()
                    )
                }
            }
        }
    }
}

