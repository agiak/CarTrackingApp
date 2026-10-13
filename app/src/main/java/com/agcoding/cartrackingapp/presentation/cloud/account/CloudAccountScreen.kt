package com.agcoding.cartrackingapp.presentation.cloud.account

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.cloud.domain.model.CloudMode
import com.agcoding.cartrackingapp.presentation.cloud.auth.CloudAuthMode
import com.agcoding.cartrackingapp.presentation.cloud.components.CloudStatusCard
import com.agcoding.cartrackingapp.presentation.cloud.messageRes
import com.agcoding.cartrackingapp.presentation.components.StyledCard
import com.agcoding.cartrackingapp.presentation.components.StyledTopAppBar
import com.agcoding.cartrackingapp.presentation.settings.components.SectionHeader
import com.agcoding.cartrackingapp.presentation.settings.components.SettingsContent
import com.agcoding.cartrackingapp.presentation.settings.components.SettingsRow
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme
import com.agcoding.cartrackingapp.util.DeviceUtils

/**
 * The account screen: everything about the backend in one place.
 *
 * Signed out it is a pitch and two buttons; signed in it is the status, a
 * sync button and the three ways to leave. Nothing else in the app changes
 * behaviour depending on whether there is an account, which is the point —
 * the account adds a backup, it is not a login wall.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CloudAccountScreen(
    onNavigateBack: () -> Unit,
    onNavigateToAuth: (CloudAuthMode) -> Unit,
    onNavigateToChangePassword: () -> Unit,
    viewModel: CloudAccountViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    val signedOutMessage = stringResource(R.string.cloud_signed_out)
    val deletedMessage = stringResource(R.string.cloud_account_deleted)
    val syncedMessage = stringResource(R.string.cloud_synced)
    val nothingToSyncMessage = stringResource(R.string.cloud_nothing_to_sync)

    LaunchedEffect(uiState.message) {
        val text = when (uiState.message) {
            CloudAccountMessage.SignedOut -> signedOutMessage
            CloudAccountMessage.AccountDeleted -> deletedMessage
            CloudAccountMessage.Synced -> syncedMessage
            CloudAccountMessage.NothingToSync -> nothingToSyncMessage
            null -> null
        }
        if (text != null) {
            // Shown before it is consumed, not after: clearing the message
            // changes this effect's key, which cancels this very coroutine —
            // and showSnackbar suspends for the whole time the snackbar is up,
            // so consuming first makes it flash and vanish.
            snackbarHostState.showSnackbar(text)
            viewModel.consumeMessage()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            StyledTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings_group_cloud_title),
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        val isTablet = DeviceUtils.isTablet()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = if (isTablet) Alignment.TopCenter else Alignment.TopStart
        ) {
            SettingsContent(
                modifier = Modifier.then(
                    if (isTablet) Modifier.fillMaxWidth(0.7f) else Modifier.fillMaxWidth()
                )
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                CloudStatusCard(
                    mode = uiState.mode,
                    lastSyncAt = uiState.cloudState.lastSyncAt,
                    hasPendingChanges = uiState.cloudState.hasPendingChanges,
                    modifier = Modifier.fillMaxWidth()
                )

                if (uiState.cloudState.isSignedIn && !uiState.dataSyncAvailable) {
                    Spacer(modifier = Modifier.height(4.dp))
                    NoticeCard(
                        title = stringResource(R.string.cloud_sync_not_ready_title),
                        body = stringResource(R.string.cloud_sync_not_ready_desc),
                    )
                }

                uiState.error?.let { error ->
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(error.messageRes()),
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (uiState.cloudState.isSignedIn) {
                    SignedInSection(
                        email = uiState.cloudState.session?.email.orEmpty(),
                        displayName = uiState.displayName,
                        isBusy = uiState.isBusy,
                        mustChangePassword = uiState.cloudState.session?.mustChangePassword == true,
                        onSyncNow = viewModel::syncNow,
                        onChangePassword = onNavigateToChangePassword,
                        onSignOut = { viewModel.openDialog(CloudAccountDialog.SignOut) },
                        onDeleteAccount = { viewModel.openDialog(CloudAccountDialog.DeleteAccount) },
                    )
                } else {
                    SignedOutSection(
                        onCreateAccount = { onNavigateToAuth(CloudAuthMode.SignUp) },
                        onSignIn = { onNavigateToAuth(CloudAuthMode.SignIn) },
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    when (uiState.dialog) {
        CloudAccountDialog.SignOut -> ConfirmDialog(
            title = stringResource(R.string.cloud_sign_out),
            message = stringResource(R.string.cloud_sign_out_message),
            confirmLabel = stringResource(R.string.cloud_sign_out),
            destructive = false,
            isBusy = uiState.isBusy,
            errorRes = uiState.error?.messageRes(),
            wipeLocalData = uiState.wipeLocalData,
            onWipeLocalDataChange = viewModel::setWipeLocalData,
            onConfirm = viewModel::confirmSignOut,
            onDismiss = viewModel::closeDialog,
        )

        CloudAccountDialog.DeleteAccount -> ConfirmDialog(
            title = stringResource(R.string.cloud_delete_account),
            message = stringResource(R.string.cloud_delete_account_message),
            confirmLabel = stringResource(R.string.cloud_delete_account_action),
            destructive = true,
            isBusy = uiState.isBusy,
            errorRes = uiState.error?.messageRes(),
            wipeLocalData = uiState.wipeLocalData,
            onWipeLocalDataChange = viewModel::setWipeLocalData,
            onConfirm = viewModel::confirmDeleteAccount,
            onDismiss = viewModel::closeDialog,
        )

        null -> Unit
    }
}

@Composable
private fun SignedOutSection(
    onCreateAccount: () -> Unit,
    onSignIn: () -> Unit,
) {
    SectionHeader(title = stringResource(R.string.cloud_section_account))

    StyledCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(R.string.cloud_pitch_title),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(R.string.cloud_pitch_body),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Button(
                onClick = onCreateAccount,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.cloud_sign_up_action))
            }

            OutlinedButton(
                onClick = onSignIn,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Login,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.cloud_sign_in_action))
            }
        }
    }
}

@Composable
private fun SignedInSection(
    email: String,
    displayName: String?,
    isBusy: Boolean,
    mustChangePassword: Boolean,
    onSyncNow: () -> Unit,
    onChangePassword: () -> Unit,
    onSignOut: () -> Unit,
    onDeleteAccount: () -> Unit,
) {
    SectionHeader(title = stringResource(R.string.cloud_section_account))

    StyledCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = stringResource(R.string.cloud_signed_in_as),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = displayName?.takeIf { it.isNotBlank() } ?: email,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (!displayName.isNullOrBlank()) {
                Text(
                    text = email,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    if (mustChangePassword) {
        Spacer(modifier = Modifier.height(4.dp))
        NoticeCard(
            title = stringResource(R.string.cloud_change_password_forced_title),
            body = stringResource(R.string.cloud_change_password_forced_desc),
        )
    }

    Spacer(modifier = Modifier.height(8.dp))
    SectionHeader(title = stringResource(R.string.cloud_section_sync))

    StyledCard(modifier = Modifier.fillMaxWidth()) {
        SettingsRow(
            icon = Icons.Default.Sync,
            iconBackgroundColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
            iconTint = MaterialTheme.colorScheme.primary,
            title = stringResource(R.string.cloud_sync_now),
            subtitle = stringResource(R.string.cloud_sync_now_desc),
            onClick = onSyncNow,
            enabled = !isBusy,
            trailing = {
                if (isBusy) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        )
    }

    Spacer(modifier = Modifier.height(8.dp))
    SectionHeader(title = stringResource(R.string.cloud_section_security))

    StyledCard(modifier = Modifier.fillMaxWidth()) {
        SettingsRow(
            icon = Icons.Default.Lock,
            iconBackgroundColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
            iconTint = MaterialTheme.colorScheme.primary,
            title = stringResource(R.string.cloud_change_password_title),
            subtitle = stringResource(R.string.cloud_change_password_desc),
            onClick = onChangePassword,
            enabled = !isBusy,
            trailing = {
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        )
        SettingsRow(
            icon = Icons.AutoMirrored.Filled.Logout,
            iconBackgroundColor = MaterialTheme.colorScheme.surfaceVariant,
            iconTint = MaterialTheme.colorScheme.onSurfaceVariant,
            title = stringResource(R.string.cloud_sign_out),
            subtitle = stringResource(R.string.cloud_sign_out_desc),
            onClick = onSignOut,
            enabled = !isBusy,
            trailing = {}
        )
        SettingsRow(
            icon = Icons.Default.DeleteForever,
            iconBackgroundColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
            iconTint = MaterialTheme.colorScheme.error,
            title = stringResource(R.string.cloud_delete_account),
            subtitle = stringResource(R.string.cloud_delete_account_desc),
            onClick = onDeleteAccount,
            enabled = !isBusy,
            trailing = {}
        )
    }
}

@Composable
private fun NoticeCard(title: String, body: String) {
    StyledCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
    ) {
        Row(modifier = Modifier.padding(16.dp)) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = body,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ConfirmDialog(
    title: String,
    message: String,
    confirmLabel: String,
    destructive: Boolean,
    isBusy: Boolean,
    errorRes: Int?,
    wipeLocalData: Boolean,
    onWipeLocalDataChange: (Boolean) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = { if (!isBusy) onDismiss() },
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(text = message, fontSize = 14.sp)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = wipeLocalData,
                        onCheckedChange = onWipeLocalDataChange,
                        enabled = !isBusy
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Column {
                        Text(
                            text = stringResource(R.string.cloud_wipe_local),
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = stringResource(R.string.cloud_wipe_local_desc),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                errorRes?.let {
                    Text(
                        text = stringResource(it),
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = !isBusy) {
                Text(
                    text = confirmLabel,
                    color = if (destructive) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isBusy) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun PreviewCloudAccountSignedOut() {
    CarTrackingAppTheme {
        Column {
            CloudStatusCard(
                mode = CloudMode.OFFLINE_MODE,
                lastSyncAt = null,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
            SignedOutSection(onCreateAccount = {}, onSignIn = {})
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewCloudAccountSignedIn() {
    CarTrackingAppTheme {
        Column {
            SignedInSection(
                email = "anastas@example.com",
                displayName = "Anastasis",
                isBusy = false,
                mustChangePassword = false,
                onSyncNow = {},
                onChangePassword = {},
                onSignOut = {},
                onDeleteAccount = {},
            )
        }
    }
}
