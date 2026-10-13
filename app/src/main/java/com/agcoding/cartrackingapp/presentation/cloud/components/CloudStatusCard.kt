package com.agcoding.cartrackingapp.presentation.cloud.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import com.agcoding.cartrackingapp.cloud.domain.model.CloudMode
import com.agcoding.cartrackingapp.presentation.components.StyledCard
import com.agcoding.cartrackingapp.presentation.theme.AppSuccess
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme
import java.text.DateFormat
import java.util.Date

/**
 * The one line that answers "is my data safe?".
 *
 * Deliberately a single card with one state at a time: a user who is not
 * thinking about synchronisation should be able to glance at it and stop
 * thinking about it again.
 */
@Composable
fun CloudStatusCard(
    mode: CloudMode,
    lastSyncAt: Long?,
    modifier: Modifier = Modifier,
    hasPendingChanges: Boolean = false,
) {
    val accent = mode.accentColor()

    StyledCard(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                if (mode == CloudMode.SYNCING) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = accent
                    )
                } else {
                    Icon(
                        imageVector = mode.icon(),
                        contentDescription = null,
                        modifier = Modifier.size(22.dp),
                        tint = accent
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(mode.titleRes()),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = stringResource(mode.descriptionRes()),
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (mode != CloudMode.OFFLINE_MODE) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (lastSyncAt == null) {
                            stringResource(R.string.cloud_last_sync_never)
                        } else {
                            stringResource(R.string.cloud_last_sync, lastSyncAt.asDateTime())
                        },
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (hasPendingChanges) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = stringResource(R.string.cloud_pending_changes),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
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

@Preview(showBackground = true, widthDp = 380)
@Composable
private fun PreviewCloudStatusCardOffline() {
    CarTrackingAppTheme {
        CloudStatusCard(
            mode = CloudMode.OFFLINE_MODE,
            lastSyncAt = null,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}

@Preview(showBackground = true, widthDp = 380)
@Composable
private fun PreviewCloudStatusCardOnline() {
    CarTrackingAppTheme {
        CloudStatusCard(
            mode = CloudMode.ONLINE,
            lastSyncAt = 1_760_000_000_000L,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}

@Preview(showBackground = true, widthDp = 380)
@Composable
private fun PreviewCloudStatusCardPending() {
    CarTrackingAppTheme(darkTheme = true) {
        CloudStatusCard(
            mode = CloudMode.NO_INTERNET,
            lastSyncAt = 1_760_000_000_000L,
            hasPendingChanges = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}

@Preview(showBackground = true, widthDp = 380)
@Composable
private fun PreviewCloudStatusCardError() {
    CarTrackingAppTheme {
        CloudStatusCard(
            mode = CloudMode.SYNC_ERROR,
            lastSyncAt = 1_760_000_000_000L,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}
