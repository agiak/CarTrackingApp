package com.agcoding.cartrackingapp.presentation.notifications.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme

@Composable
fun NotificationsHeader(
    activeCount: Int,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = stringResource(R.string.notifications_upcoming_reminders),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = stringResource(R.string.notifications_active_count, activeCount),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ============================================
// Preview Composables
// ============================================

@Preview(name = "Notifications Header - Few Active", showBackground = true, widthDp = 380)
@Composable
private fun PreviewNotificationsHeaderFew() {
    CarTrackingAppTheme(darkTheme = false) {
        NotificationsHeader(activeCount = 3)
    }
}

@Preview(name = "Notifications Header - Many Active", showBackground = true, widthDp = 380)
@Composable
private fun PreviewNotificationsHeaderMany() {
    CarTrackingAppTheme(darkTheme = false) {
        NotificationsHeader(activeCount = 12)
    }
}

@Preview(name = "Notifications Header - Dark", showBackground = true, widthDp = 380)
@Composable
private fun PreviewNotificationsHeaderDark() {
    CarTrackingAppTheme(darkTheme = true) {
        NotificationsHeader(activeCount = 5)
    }
}
