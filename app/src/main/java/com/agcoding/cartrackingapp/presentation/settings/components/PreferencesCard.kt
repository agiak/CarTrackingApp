package com.agcoding.cartrackingapp.presentation.settings.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.presentation.components.StyledCard

@Composable
fun PreferencesCard(
    notificationsEnabled: Boolean,
    permissionPermanentlyDenied: Boolean,
    onNotificationsToggle: (Boolean) -> Unit,
    onOpenSettings: () -> Unit,
    onViewNotifications: () -> Unit,
    onViewNotificationHistory: () -> Unit = {}
) {
    StyledCard(
        modifier = Modifier.fillMaxWidth(),
        tintAlpha = 0.3f
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Notifications Row
            SettingsRow(
                icon = Icons.Default.Notifications,
                iconBackgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                iconTint = MaterialTheme.colorScheme.primary,
                title = stringResource(R.string.preferences_notifications),
                subtitle = if (notificationsEnabled) stringResource(R.string.settings_notifications_enabled) else stringResource(R.string.settings_notifications_disabled),
                trailing = {
                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = onNotificationsToggle,
                        colors = SwitchDefaults.colors(
                            checkedTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            )

            // Show helper text and button when permission is permanently denied
            if (permissionPermanentlyDenied && !notificationsEnabled) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.notification_permission_denied_helper),
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.error,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = onOpenSettings,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(stringResource(R.string.open_settings))
                    }
                }
            }

            // Divider
            androidx.compose.material3.HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            // View Reminders Row
            SettingsRow(
                icon = Icons.Default.Event,
                iconBackgroundColor = if (notificationsEnabled) {
                    MaterialTheme.colorScheme.tertiary.copy(alpha = 0.1f)
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.05f)
                },
                iconTint = if (notificationsEnabled) {
                    MaterialTheme.colorScheme.tertiary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                },
                title = stringResource(R.string.preferences_view_reminders),
                subtitle = stringResource(R.string.preferences_view_reminders_desc),
                trailing = {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = if (notificationsEnabled) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                        }
                    )
                },
                onClick = if (notificationsEnabled) onViewNotifications else null,
                enabled = notificationsEnabled
            )

            // Divider
            androidx.compose.material3.HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            // View Notification History Row
            SettingsRow(
                icon = Icons.Default.History,
                iconBackgroundColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f),
                iconTint = MaterialTheme.colorScheme.secondary,
                title = stringResource(R.string.notification_history_view),
                subtitle = stringResource(R.string.notification_history_view_desc),
                trailing = {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                onClick = onViewNotificationHistory
            )
        }
    }
}
