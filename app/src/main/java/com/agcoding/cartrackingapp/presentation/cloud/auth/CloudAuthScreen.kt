package com.agcoding.cartrackingapp.presentation.cloud.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.presentation.cloud.components.PasswordField
import com.agcoding.cartrackingapp.presentation.cloud.messageRes
import com.agcoding.cartrackingapp.presentation.components.AppTextButton
import com.agcoding.cartrackingapp.presentation.components.PrimaryButton
import com.agcoding.cartrackingapp.presentation.components.SegmentedControl
import com.agcoding.cartrackingapp.presentation.components.AppTextField
import com.agcoding.cartrackingapp.presentation.components.StyledTopAppBar
import com.agcoding.cartrackingapp.presentation.components.SuccessOverlay
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme
import com.agcoding.cartrackingapp.util.DeviceUtils

/**
 * Sign in and create account, in one screen.
 *
 * They are the same two fields and the same button, so splitting them into two
 * screens would mean two of everything to keep in step — and the user who
 * tapped the wrong one would have to go back out and start again.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CloudAuthScreen(
    onNavigateBack: () -> Unit,
    /**
     * Told which of the two just happened: what comes next is a first sync,
     * and a sign-in pulls data down where a sign-up pushes it (or has nothing
     * to push at all).
     */
    onSignedIn: (CloudAuthMode) -> Unit,
    onMustChangePassword: () -> Unit,
    initialMode: CloudAuthMode = CloudAuthMode.SignIn,
    viewModel: CloudAuthViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Held here rather than in the ViewModel: it is the animation that is
    // playing, not the account that is in a state.
    var celebrating by remember { mutableStateOf(false) }

    LaunchedEffect(initialMode) { viewModel.setMode(initialMode) }

    LaunchedEffect(uiState.outcome) {
        when (uiState.outcome) {
            CloudAuthOutcome.SignedIn -> {
                viewModel.consumeOutcome()
                // The tick plays first; leaving is what it ends with.
                celebrating = true
            }

            CloudAuthOutcome.MustChangePassword -> {
                viewModel.consumeOutcome()
                onMustChangePassword()
            }

            null -> Unit
        }
    }

    val resetSentMessage = stringResource(R.string.cloud_reset_sent)
    LaunchedEffect(uiState.resetSent) {
        if (uiState.resetSent) {
            snackbarHostState.showSnackbar(resetSentMessage)
            viewModel.consumeResetSent()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            StyledTopAppBar(
                title = {
                    Text(
                        text = stringResource(
                            if (uiState.mode == CloudAuthMode.SignIn) {
                                R.string.cloud_sign_in_title
                            } else {
                                R.string.cloud_sign_up_title
                            }
                        ),
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
            Column(
                modifier = Modifier
                    .then(
                        if (isTablet) Modifier.fillMaxWidth(0.7f) else Modifier.fillMaxWidth()
                    )
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Spacer(modifier = Modifier.height(4.dp))

                // Someone arriving here without an account could previously
                // only discover that creating one was possible by reading to
                // the bottom of a form they had no reason to fill in.
                SegmentedControl(
                    options = listOf(
                        stringResource(R.string.cloud_sign_in_action),
                        stringResource(R.string.cloud_sign_up_action),
                    ),
                    selectedIndex = if (uiState.mode == CloudAuthMode.SignIn) 0 else 1,
                    onSelect = { index ->
                        viewModel.setMode(
                            if (index == 0) CloudAuthMode.SignIn else CloudAuthMode.SignUp
                        )
                    },
                )

                Text(
                    text = stringResource(
                        if (uiState.mode == CloudAuthMode.SignIn) {
                            R.string.cloud_sign_in_subtitle
                        } else {
                            R.string.cloud_sign_up_subtitle
                        }
                    ),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (uiState.mode == CloudAuthMode.SignUp) {
                    AppTextField(
                        value = uiState.displayName,
                        onValueChange = viewModel::onDisplayNameChange,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !uiState.isBusy,
                        label = { Text(stringResource(R.string.cloud_display_name)) },
                        supportingText = { Text(stringResource(R.string.cloud_display_name_optional)) },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Next
                        )
                    )
                }

                AppTextField(
                    value = uiState.email,
                    onValueChange = viewModel::onEmailChange,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isBusy,
                    label = { Text(stringResource(R.string.cloud_email)) },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    )
                )

                PasswordField(
                    value = uiState.password,
                    onValueChange = viewModel::onPasswordChange,
                    label = stringResource(R.string.cloud_password),
                    visible = uiState.passwordVisible,
                    onToggleVisibility = viewModel::togglePasswordVisibility,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isBusy,
                    supportingText = if (uiState.mode == CloudAuthMode.SignUp) {
                        stringResource(R.string.cloud_password_min)
                    } else {
                        null
                    }
                )

                uiState.error?.let { error ->
                    Text(
                        text = stringResource(error.messageRes()),
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                PrimaryButton(
                    label = stringResource(
                        if (uiState.mode == CloudAuthMode.SignIn) {
                            R.string.cloud_sign_in_action
                        } else {
                            R.string.cloud_sign_up_action
                        }
                    ),
                    onClick = viewModel::submit,
                    enabled = uiState.canSubmit,
                    isBusy = uiState.isBusy,
                )

                if (uiState.mode == CloudAuthMode.SignIn) {
                    AppTextButton(
                        label = stringResource(R.string.cloud_forgot_password),
                        onClick = viewModel::openResetDialog,
                        enabled = !uiState.isBusy,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                AppTextButton(
                    label = stringResource(
                        if (uiState.mode == CloudAuthMode.SignIn) {
                            R.string.cloud_switch_to_sign_up
                        } else {
                            R.string.cloud_switch_to_sign_in
                        }
                    ),
                    onClick = {
                        viewModel.setMode(
                            if (uiState.mode == CloudAuthMode.SignIn) {
                                CloudAuthMode.SignUp
                            } else {
                                CloudAuthMode.SignIn
                            }
                        )
                    },
                    enabled = !uiState.isBusy,
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    SuccessOverlay(
        visible = celebrating,
        message = stringResource(
            if (uiState.mode == CloudAuthMode.SignUp) {
                R.string.cloud_account_created
            } else {
                R.string.cloud_signed_in_message
            }
        ),
        supportingText = uiState.email.takeIf { it.isNotBlank() },
        onFinished = { onSignedIn(uiState.mode) },
    )

    if (uiState.showResetDialog) {
        ResetPasswordDialog(
            email = uiState.resetEmail,
            isSending = uiState.isSendingReset,
            errorRes = uiState.error?.messageRes(),
            onEmailChange = viewModel::onResetEmailChange,
            onConfirm = viewModel::sendReset,
            onDismiss = viewModel::closeResetDialog,
        )
    }
}

@Composable
private fun ResetPasswordDialog(
    email: String,
    isSending: Boolean,
    errorRes: Int?,
    onEmailChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = { if (!isSending) onDismiss() },
        title = { Text(stringResource(R.string.cloud_reset_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = stringResource(R.string.cloud_reset_desc),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                AppTextField(
                    value = email,
                    onValueChange = onEmailChange,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isSending,
                    label = { Text(stringResource(R.string.cloud_email)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Done
                    )
                )
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
            TextButton(onClick = onConfirm, enabled = !isSending) {
                Text(stringResource(R.string.cloud_reset_action))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isSending) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun PreviewCloudAuthScreen() {
    CarTrackingAppTheme {
        CloudAuthScreen(
            onNavigateBack = {},
            onSignedIn = {},
            onMustChangePassword = {},
        )
    }
}
