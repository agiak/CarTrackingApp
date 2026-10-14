package com.agcoding.cartrackingapp.presentation.notifications.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.domain.model.Expense
import com.agcoding.cartrackingapp.domain.model.NotificationHistoryItem
import com.agcoding.cartrackingapp.domain.model.NotificationKind
import com.agcoding.cartrackingapp.domain.usecase.expense.ExpenseReminder
import com.agcoding.cartrackingapp.presentation.notifications.NotificationHistoryCard
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme

/**
 * Two lists, deliberately not merged into one.
 *
 * A reminder is something that has not happened yet and that the user can
 * still act on; a received notification is a record of something that already
 * did. Sorting them together by date would interleave next month's insurance
 * renewal with yesterday's announcement, and there is no reading of that
 * order which is useful.
 */
@Composable
fun NotificationsContent(
    reminders: List<ExpenseReminder>,
    received: List<NotificationHistoryItem>,
    onToggleReminder: (Long, Boolean) -> Unit,
    onEditReminder: (Long) -> Unit,
    onDismissReminder: (Long) -> Unit,
    onViewAllReceived: () -> Unit,
    useCenteredLayout: Boolean = false,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.padding(
            horizontal = if (useCenteredLayout) 24.dp else 16.dp
        ),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
        }

        if (reminders.isNotEmpty()) {
            item {
                // The screen's name is the large title in the bar, so the
                // heading here names the section, not the screen.
                SectionHeader(
                    title = stringResource(R.string.notifications_section_upcoming),
                    caption = stringResource(
                        R.string.notifications_active_count,
                        reminders.count { it.expense.reminderEnabled }
                    )
                )
            }

            items(reminders, key = { "reminder-${it.expense.id}" }) { reminder ->
                ReminderCard(
                    reminder = reminder,
                    onToggle = { enabled -> onToggleReminder(reminder.expense.id, enabled) },
                    onEdit = { onEditReminder(reminder.expense.id) },
                    onDismiss = { onDismissReminder(reminder.expense.id) }
                )
            }
        }

        if (received.isNotEmpty()) {
            item {
                SectionHeader(
                    title = stringResource(R.string.notifications_section_received),
                    modifier = Modifier.padding(top = if (reminders.isEmpty()) 0.dp else 8.dp)
                )
            }

            items(received, key = { "received-${it.id}" }) { item ->
                NotificationHistoryCard(notification = item)
            }

            // Only the newest few are above. This is the way to the rest of
            // them — and the only place they can be cleared.
            item {
                TextButton(onClick = onViewAllReceived) {
                    Text(
                        text = stringResource(R.string.notifications_view_all),
                        fontSize = 14.sp
                    )
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        modifier = Modifier.height(18.dp)
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    caption: String? = null
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.6.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (caption != null) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = caption,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
            )
        }
    }
}

// ============================================
// Preview Composables
// ============================================

private fun previewReminder(
    id: Long,
    category: String,
    carName: String,
    enabled: Boolean = true,
    inDays: Long = 5,
    remainingKm: Int? = null
) = ExpenseReminder(
    expense = Expense(
        id = id,
        carId = 1,
        category = category,
        amount = 120.0,
        timestamp = System.currentTimeMillis(),
        notes = "Regular maintenance",
        reminderEnabled = enabled,
        reminderDate = System.currentTimeMillis() + inDays * 24 * 60 * 60 * 1000L,
        reminderMileage = null,
        preExpiryNotificationSent = false,
        reminderDismissed = false
    ),
    carName = carName,
    currentOdometer = 49700.0,
    remainingKm = remainingKm
)

private val previewReceived = listOf(
    NotificationHistoryItem(
        id = 1,
        title = "Το Caribou 1.2 βγήκε",
        description = "Τα έξοδα συγχρονίζονται πλέον ανάμεσα σε δύο κινητά.",
        timestamp = System.currentTimeMillis() - 3 * 60 * 60 * 1000L,
        kind = NotificationKind.ANNOUNCEMENT
    ),
    NotificationHistoryItem(
        id = 2,
        title = "Oil Change Reminder",
        description = "Your Toyota Corolla is due for an oil change soon.",
        timestamp = System.currentTimeMillis() - 4 * 24 * 60 * 60 * 1000L,
        kind = NotificationKind.REMINDER
    )
)

@Preview(name = "Both sections", showBackground = true, widthDp = 380, heightDp = 800)
@Composable
private fun PreviewNotificationsContentPhone() {
    CarTrackingAppTheme(darkTheme = false) {
        NotificationsContent(
            reminders = listOf(
                previewReminder(1, "Oil Change", "Toyota Corolla", remainingKm = 300),
                previewReminder(2, "Tire Change", "Toyota Corolla", inDays = 30)
            ),
            received = previewReceived,
            onToggleReminder = { _, _ -> },
            onEditReminder = {},
            onDismissReminder = {},
            onViewAllReceived = {},
            useCenteredLayout = false,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Preview(name = "Announcements only", showBackground = true, widthDp = 380, heightDp = 600)
@Composable
private fun PreviewNotificationsContentReceivedOnly() {
    CarTrackingAppTheme(darkTheme = false) {
        NotificationsContent(
            reminders = emptyList(),
            received = previewReceived,
            onToggleReminder = { _, _ -> },
            onEditReminder = {},
            onDismissReminder = {},
            onViewAllReceived = {},
            useCenteredLayout = false,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Preview(name = "Tablet", showBackground = true, widthDp = 800, heightDp = 600)
@Composable
private fun PreviewNotificationsContentTablet() {
    CarTrackingAppTheme(darkTheme = false) {
        NotificationsContent(
            reminders = listOf(
                previewReminder(1, "Service & Maintenance", "Honda Civic", inDays = 15, remainingKm = 2500)
            ),
            received = previewReceived,
            onToggleReminder = { _, _ -> },
            onEditReminder = {},
            onDismissReminder = {},
            onViewAllReceived = {},
            useCenteredLayout = true,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Preview(name = "Dark", showBackground = true, widthDp = 380, heightDp = 800)
@Composable
private fun PreviewNotificationsContentDark() {
    CarTrackingAppTheme(darkTheme = true) {
        NotificationsContent(
            reminders = listOf(
                previewReminder(1, "Insurance Renewal", "BMW 320i", inDays = 10)
            ),
            received = previewReceived,
            onToggleReminder = { _, _ -> },
            onEditReminder = {},
            onDismissReminder = {},
            onViewAllReceived = {},
            useCenteredLayout = false,
            modifier = Modifier.fillMaxSize()
        )
    }
}
