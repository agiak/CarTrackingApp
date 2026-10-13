package com.agcoding.cartrackingapp.presentation.cloud.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Login
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.cloud.domain.model.CloudMode
import com.agcoding.cartrackingapp.presentation.theme.AppSuccess
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme
import java.text.DateFormat
import java.util.Date
import java.util.Locale

/**
 * The top of the account screen.
 *
 * A settings list can tell you the same facts, but an account screen is one of
 * the few places where who you are matters more than what you can change here
 * — so the identity comes first and at size, and the controls follow. The
 * state of the backup is a single pill rather than a card, because a user who
 * is not worried about synchronisation should be able to glance at it and go
 * back to not worrying.
 */

// ── signed in ─────────────────────────────────────────────────────────────

@Composable
fun AccountIdentityHero(
    email: String,
    displayName: String?,
    mode: CloudMode,
    lastSyncAt: Long?,
    hasPendingChanges: Boolean,
    dataSyncAvailable: Boolean,
    modifier: Modifier = Modifier,
) {
    val accountOnly = !dataSyncAvailable && mode == CloudMode.ONLINE
    val accent = if (accountOnly) MaterialTheme.colorScheme.onSurfaceVariant else mode.accentColor()

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AccountAvatar(
            initials = initialsFor(displayName, email),
            statusColor = accent,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = displayName?.takeIf { it.isNotBlank() } ?: email.substringBefore('@'),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = email,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(14.dp))

        AccountStatusPill(mode = mode, accountOnly = accountOnly, accent = accent)

        // The pill names the state; this says what it means for your data.
        // Worth the extra line: "No internet" alone leaves the user to guess
        // whether anything was lost.
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(
                if (accountOnly) R.string.cloud_mode_account_only_desc
                else mode.descriptionRes()
            ),
            fontSize = 13.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        val detail = syncDetail(mode, lastSyncAt, hasPendingChanges)
        if (detail != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = detail,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                color = if (hasPendingChanges) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }
    }
}

/**
 * Initials on a gradient, with the backup's state as a dot.
 *
 * The dot sits on a ring of the page's own background rather than a border, so
 * it reads as floating above the avatar in both themes without having to know
 * what colour is behind it.
 */
@Composable
fun AccountAvatar(
    initials: String,
    modifier: Modifier = Modifier,
    statusColor: Color? = null,
    size: Int = 96,
) {
    Box(modifier = modifier.size(size.dp)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.tertiary,
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials,
                fontSize = (size / 2.8f).sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }

        if (statusColor != null) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size((size / 3.7f).dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size((size / 5.5f).dp)
                        .clip(CircleShape)
                        .background(statusColor)
                )
            }
        }
    }
}

@Composable
private fun AccountStatusPill(mode: CloudMode, accountOnly: Boolean, accent: Color) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(percent = 50))
            .background(accent.copy(alpha = 0.14f))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (mode == CloudMode.SYNCING) {
            CircularProgressIndicator(
                modifier = Modifier.size(14.dp),
                strokeWidth = 2.dp,
                color = accent
            )
        } else {
            Icon(
                imageVector = if (accountOnly) Icons.Default.CloudQueue else mode.icon(),
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = accent
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = stringResource(
                if (accountOnly) R.string.cloud_mode_account_only_title else mode.titleRes()
            ),
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = accent
        )
    }
}

// ── signed out ────────────────────────────────────────────────────────────

@Composable
fun AccountSignedOutHero(
    onCreateAccount: () -> Unit,
    onSignIn: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(88.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.18f),
                            MaterialTheme.colorScheme.tertiary.copy(alpha = 0.18f),
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.PhoneAndroid,
                contentDescription = null,
                modifier = Modifier.size(38.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = stringResource(R.string.cloud_pitch_title),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.cloud_pitch_body),
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Above the button, not below it. Telling someone what arrives with an
        // account only counts as telling them if they read it before deciding.
        Text(
            text = stringResource(R.string.cloud_push_note),
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 12.dp)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onCreateAccount,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.cloud_sign_up_action),
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        TextButton(onClick = onSignIn, modifier = Modifier.fillMaxWidth()) {
            Icon(
                Icons.AutoMirrored.Filled.Login,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.cloud_sign_in_action), fontSize = 15.sp)
        }
    }
}

// ── the mode, in words and colour ─────────────────────────────────────────

/** Unsent edits outrank a timestamp: it is the one of the two you can act on. */
@Composable
private fun syncDetail(
    mode: CloudMode,
    lastSyncAt: Long?,
    hasPendingChanges: Boolean,
): String? = when {
    hasPendingChanges -> stringResource(R.string.cloud_pending_changes)
    mode == CloudMode.OFFLINE_MODE -> null
    lastSyncAt == null -> stringResource(R.string.cloud_last_sync_never)
    else -> stringResource(R.string.cloud_last_sync, lastSyncAt.asDateTime())
}

private fun CloudMode.icon(): ImageVector = when (this) {
    CloudMode.OFFLINE_MODE -> Icons.Default.PhoneAndroid
    CloudMode.ONLINE -> Icons.Default.CloudDone
    CloudMode.SYNCING -> Icons.Default.CloudSync
    CloudMode.NO_INTERNET -> Icons.Default.CloudOff
    CloudMode.SYNC_ERROR -> Icons.Default.ErrorOutline
}

private fun CloudMode.titleRes(): Int = when (this) {
    CloudMode.OFFLINE_MODE -> R.string.cloud_mode_offline_title
    CloudMode.ONLINE -> R.string.cloud_mode_online_title
    CloudMode.SYNCING -> R.string.cloud_mode_syncing_title
    CloudMode.NO_INTERNET -> R.string.cloud_mode_no_internet_title
    CloudMode.SYNC_ERROR -> R.string.cloud_mode_sync_error_title
}

private fun CloudMode.descriptionRes(): Int = when (this) {
    CloudMode.OFFLINE_MODE -> R.string.cloud_mode_offline_desc
    CloudMode.ONLINE -> R.string.cloud_mode_online_desc
    CloudMode.SYNCING -> R.string.cloud_mode_syncing_desc
    CloudMode.NO_INTERNET -> R.string.cloud_mode_no_internet_desc
    CloudMode.SYNC_ERROR -> R.string.cloud_mode_sync_error_desc
}

@Composable
private fun CloudMode.accentColor(): Color = when (this) {
    CloudMode.OFFLINE_MODE -> MaterialTheme.colorScheme.onSurfaceVariant
    CloudMode.ONLINE -> AppSuccess.color
    CloudMode.SYNCING -> MaterialTheme.colorScheme.primary
    CloudMode.NO_INTERNET -> MaterialTheme.colorScheme.onSurfaceVariant
    CloudMode.SYNC_ERROR -> MaterialTheme.colorScheme.error
}

private fun Long.asDateTime(): String =
    DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(this))

/**
 * Two letters for the avatar: a name's first two words, or whatever the email
 * starts with. Never empty — a blank circle looks like a failed image load.
 */
private fun initialsFor(displayName: String?, email: String): String {
    val source = displayName?.trim()?.takeIf { it.isNotEmpty() }
        ?: email.substringBefore('@')
    val words = source.split(' ', '.', '_', '-').filter { it.isNotBlank() }
    val letters = when {
        words.size >= 2 -> "${words[0].first()}${words[1].first()}"
        words.size == 1 -> words[0].take(2)
        else -> "?"
    }
    return letters.uppercase(Locale.getDefault())
}

// ── previews ──────────────────────────────────────────────────────────────

@Preview(showBackground = true, widthDp = 380)
@Composable
private fun PreviewAccountHeroOnline() {
    CarTrackingAppTheme {
        AccountIdentityHero(
            email = "anastas.giakoumatos@gmail.com",
            displayName = "Anastasis Giakoumatos",
            mode = CloudMode.ONLINE,
            lastSyncAt = 1_760_000_000_000L,
            hasPendingChanges = false,
            dataSyncAvailable = true,
            modifier = Modifier.padding(24.dp),
        )
    }
}

@Preview(showBackground = true, widthDp = 380)
@Composable
private fun PreviewAccountHeroPendingDark() {
    CarTrackingAppTheme(darkTheme = true) {
        AccountIdentityHero(
            email = "anastas@example.com",
            displayName = null,
            mode = CloudMode.NO_INTERNET,
            lastSyncAt = 1_760_000_000_000L,
            hasPendingChanges = true,
            dataSyncAvailable = true,
            modifier = Modifier.padding(24.dp),
        )
    }
}

@Preview(showBackground = true, widthDp = 380)
@Composable
private fun PreviewAccountSignedOutHero() {
    CarTrackingAppTheme {
        AccountSignedOutHero(
            onCreateAccount = {},
            onSignIn = {},
            modifier = Modifier.padding(24.dp),
        )
    }
}
