package com.agcoding.cartrackingapp.presentation.settings.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.presentation.components.StyledCard
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme

/**
 * Whether the rolling Excel backup keeps being written to Downloads.
 *
 * Only a choice once there is an account. Signed out, that file is the only
 * copy of the data that exists anywhere, so the row is shown switched on and
 * locked rather than hidden: a setting that disappears when you sign out is
 * one the user has to rediscover, and the subtitle can say why it is fixed.
 *
 * Switching it off stops future writes and leaves whatever is already in
 * Downloads alone. Deleting a file out of the user's own Downloads folder
 * because they changed a preference is not this switch's business — but a file
 * that silently stops updating is a trap, so the subtitle says that too.
 */
@Composable
fun AutoBackupCard(
    enabled: Boolean,
    isSignedIn: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    StyledCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth()) {
            SettingsRow(
                icon = Icons.Default.Download,
                iconBackgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                iconTint = MaterialTheme.colorScheme.primary,
                title = stringResource(R.string.auto_backup_downloads),
                subtitle = stringResource(
                    when {
                        !isSignedIn -> R.string.auto_backup_downloads_signed_out
                        enabled -> R.string.auto_backup_downloads_on
                        else -> R.string.auto_backup_downloads_off
                    }
                ),
                enabled = isSignedIn,
                trailing = {
                    Switch(
                        // Signed out it is on whatever the stored value says,
                        // because that is what the backup itself does.
                        checked = enabled || !isSignedIn,
                        onCheckedChange = onToggle,
                        enabled = isSignedIn,
                        colors = SwitchDefaults.colors(
                            checkedTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AutoBackupCardSignedInPreview() {
    CarTrackingAppTheme(darkTheme = true) {
        AutoBackupCard(enabled = true, isSignedIn = true, onToggle = {})
    }
}

@Preview(showBackground = true)
@Composable
private fun AutoBackupCardSignedOutPreview() {
    CarTrackingAppTheme(darkTheme = true) {
        AutoBackupCard(enabled = false, isSignedIn = false, onToggle = {})
    }
}
