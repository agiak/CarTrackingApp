package com.agcoding.cartrackingapp.presentation.settings.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.presentation.components.StyledCard
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme

@Composable
fun DebugCard(
    isGenerating: Boolean,
    onGenerateSampleData: () -> Unit,
    onTriggerReminderCheck: () -> Unit,
    onSendTestNotification: () -> Unit,
    onResetNotificationFlags: () -> Unit
) {
    StyledCard(
        modifier = Modifier.fillMaxWidth(),
        tintAlpha = 0.3f
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.settings_section_developer_options),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ── Sample Data Generation ─────────────────────────────────
            Text(
                text = stringResource(R.string.settings_sample_data_details),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onGenerateSampleData,
                enabled = !isGenerating,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.settings_generating))
                } else {
                    Text(stringResource(R.string.settings_generate_sample_data))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Notification Testing Section ───────────────────────────
            Text(
                text = "Notification Testing",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Send Test Notification (instant)
            Button(
                onClick = onSendTestNotification,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.NotificationsActive,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.dev_trigger_test_notification))
            }

            Text(
                text = stringResource(R.string.dev_trigger_test_notification_desc),
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 14.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Trigger Reminder Check (via WorkManager)
            OutlinedButton(
                onClick = onTriggerReminderCheck,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.dev_trigger_reminder_check),
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Text(
                text = stringResource(R.string.dev_trigger_reminder_check_desc),
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 14.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Reset Notification Flags
            OutlinedButton(
                onClick = onResetNotificationFlags,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.dev_reset_notification_flags),
                    color = MaterialTheme.colorScheme.error
                )
            }

            Text(
                text = stringResource(R.string.dev_reset_notification_flags_desc),
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 14.sp
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewDebugCardLight() {
    CarTrackingAppTheme(darkTheme = false) {
        DebugCard(
            isGenerating = false,
            onGenerateSampleData = {},
            onTriggerReminderCheck = {},
            onSendTestNotification = {},
            onResetNotificationFlags = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewDebugCardGenerating() {
    CarTrackingAppTheme(darkTheme = false) {
        DebugCard(
            isGenerating = true,
            onGenerateSampleData = {},
            onTriggerReminderCheck = {},
            onSendTestNotification = {},
            onResetNotificationFlags = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewDebugCardDark() {
    CarTrackingAppTheme(darkTheme = true) {
        DebugCard(
            isGenerating = false,
            onGenerateSampleData = {},
            onTriggerReminderCheck = {},
            onSendTestNotification = {},
            onResetNotificationFlags = {}
        )
    }
}

