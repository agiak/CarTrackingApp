package com.agcoding.cartrackingapp.presentation.notifications

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.domain.model.NotificationHistoryItem
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme

@Composable
internal fun NotificationHistoryContent(
    notifications: List<NotificationHistoryItem>,
    useCenteredLayout: Boolean = false,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.padding(
            horizontal = if (useCenteredLayout) 24.dp else 16.dp
        ),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.notification_history_count, notifications.size),
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }

        items(notifications, key = { it.id }) { notification ->
            NotificationHistoryCard(notification = notification)
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun NotificationHistoryContentPreview() {
    CarTrackingAppTheme {
        NotificationHistoryContent(
            notifications = listOf(
                NotificationHistoryItem(
                    id = 1,
                    title = "Oil Change Reminder",
                    description = "Your Toyota Corolla is due for an oil change soon.",
                    timestamp = System.currentTimeMillis() - 2 * 60 * 60 * 1000L
                ),
                NotificationHistoryItem(
                    id = 2,
                    title = "Insurance Renewal",
                    description = "Your insurance policy expires in 10 days.",
                    timestamp = System.currentTimeMillis() - 3 * 24 * 60 * 60 * 1000L
                )
            ),
            modifier = Modifier.fillMaxSize()
        )
    }
}
