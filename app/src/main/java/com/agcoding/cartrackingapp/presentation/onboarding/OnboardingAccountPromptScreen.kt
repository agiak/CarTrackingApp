package com.agcoding.cartrackingapp.presentation.onboarding

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.presentation.components.StyledCard
import com.agcoding.cartrackingapp.presentation.theme.AppSuccess
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme
import com.agcoding.cartrackingapp.util.DeviceUtils

/**
 * The last step of onboarding: with an account, or without one?
 *
 * Both options are spelled out rather than nudged, because "offline" is a
 * perfectly good answer here — the app is built to work that way — and a user
 * who is pushed into an account without being told what it buys them has no
 * way to tell this apart from a sign-up wall.
 *
 * It is shown once. Afterwards the same choice lives in Settings, so skipping
 * it never closes a door.
 */
@Composable
fun OnboardingAccountPromptScreen(
    onCreateAccount: () -> Unit,
    onSignIn: () -> Unit,
    onContinueOffline: () -> Unit,
    dataSyncAvailable: Boolean = true,
) {
    val isTablet = DeviceUtils.isTablet()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = if (isTablet) Alignment.TopCenter else Alignment.TopStart
    ) {
        Column(
            modifier = Modifier
                .then(if (isTablet) Modifier.fillMaxWidth(0.7f) else Modifier.fillMaxWidth())
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = stringResource(R.string.onboarding_account_title),
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(R.string.onboarding_account_subtitle),
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

            // The same rule as the status card: while the engine moves
            // nothing, an account does not yet keep anything safe, and this is
            // the screen where the user is deciding — the worst possible place
            // to promise a backup that is not happening.
            ChoiceCard(
                icon = if (dataSyncAvailable) Icons.Default.CloudQueue else Icons.Default.Info,
                accent = if (dataSyncAvailable) {
                    AppSuccess.color
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                title = stringResource(R.string.onboarding_account_online_title),
                body = stringResource(
                    if (dataSyncAvailable) R.string.onboarding_account_online_body
                    else R.string.onboarding_account_online_body_pending
                ),
            )

            ChoiceCard(
                icon = Icons.Default.PhoneAndroid,
                accent = MaterialTheme.colorScheme.onSurfaceVariant,
                title = stringResource(R.string.onboarding_account_offline_title),
                body = stringResource(R.string.onboarding_account_offline_body),
            )

            Spacer(modifier = Modifier.height(4.dp))

            Button(
                onClick = onCreateAccount,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Icon(
                    Icons.Default.PersonAdd,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.cloud_sign_up_action),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            OutlinedButton(
                onClick = onSignIn,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Login,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.cloud_sign_in_action), fontSize = 16.sp)
            }

            TextButton(
                onClick = onContinueOffline,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.onboarding_account_continue_offline), fontSize = 15.sp)
            }

            Text(
                text = stringResource(R.string.onboarding_account_later_hint),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun ChoiceCard(
    icon: ImageVector,
    accent: Color,
    title: String,
    body: String,
) {
    StyledCard(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = accent
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = body,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 900)
@Composable
private fun PreviewOnboardingAccountPrompt() {
    CarTrackingAppTheme {
        OnboardingAccountPromptScreen(
            onCreateAccount = {},
            onSignIn = {},
            onContinueOffline = {},
            dataSyncAvailable = false,
        )
    }
}
