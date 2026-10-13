package com.agcoding.cartrackingapp.presentation.cloud.password

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.presentation.cloud.components.PasswordField
import com.agcoding.cartrackingapp.presentation.cloud.messageRes
import com.agcoding.cartrackingapp.presentation.components.StyledCard
import com.agcoding.cartrackingapp.presentation.components.StyledTopAppBar
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme
import com.agcoding.cartrackingapp.util.DeviceUtils

/**
 * Pick a new password.
 *
 * Used for both the voluntary change from the account screen and the forced
 * one after signing in with an emailed temporary password. [forced] is the
 * only difference: it swaps the wording and takes the way out away, because a
 * temporary password is a one-time code and leaving it as the account's
 * password would be worse than having no password at all.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordScreen(
    onNavigateBack: () -> Unit,
    onChanged: () -> Unit,
    forced: Boolean = false,
    viewModel: ChangePasswordViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.done) {
        if (uiState.done) {
            viewModel.consumeDone()
            onChanged()
        }
    }

    // Swallows the system back gesture while the change is mandatory.
    BackHandler(enabled = forced) {}

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            StyledTopAppBar(
                title = {
                    Text(
                        text = stringResource(
                            if (forced) {
                                R.string.cloud_change_password_forced_title
                            } else {
                                R.string.cloud_change_password_title
                            }
                        ),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    if (!forced) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.back)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
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

                if (forced) {
                    StyledCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = stringResource(R.string.cloud_change_password_forced_desc),
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }

                PasswordField(
                    value = uiState.password,
                    onValueChange = viewModel::onPasswordChange,
                    label = stringResource(R.string.cloud_new_password),
                    visible = uiState.passwordVisible,
                    onToggleVisibility = viewModel::togglePasswordVisibility,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isBusy,
                    imeAction = ImeAction.Next,
                    supportingText = stringResource(R.string.cloud_password_min)
                )

                PasswordField(
                    value = uiState.repeated,
                    onValueChange = viewModel::onRepeatedChange,
                    label = stringResource(R.string.cloud_repeat_password),
                    visible = uiState.passwordVisible,
                    onToggleVisibility = viewModel::togglePasswordVisibility,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isBusy,
                    isError = uiState.mismatch,
                    supportingText = if (uiState.mismatch) {
                        stringResource(R.string.cloud_passwords_differ)
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

                Button(
                    onClick = viewModel::submit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    enabled = uiState.canSubmit
                ) {
                    if (uiState.isBusy) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.cloud_change_password_action),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewChangePasswordScreen() {
    CarTrackingAppTheme {
        ChangePasswordScreen(onNavigateBack = {}, onChanged = {}, forced = true)
    }
}
