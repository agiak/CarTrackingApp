package com.agcoding.cartrackingapp.presentation.notifications.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.agcoding.cartrackingapp.domain.model.Expense
import com.agcoding.cartrackingapp.domain.usecase.expense.ExpenseReminder
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme

@Composable
fun NotificationsContent(
    reminders: List<ExpenseReminder>,
    onToggleReminder: (Long, Boolean) -> Unit,
    onEditReminder: (Long) -> Unit,
    onDismissReminder: (Long) -> Unit,
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

        item {
            val activeCount = reminders.count { it.expense.reminderEnabled }
            NotificationsHeader(
                activeCount = activeCount
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(reminders) { reminder ->
            ReminderCard(
                reminder = reminder,
                onToggle = { enabled -> onToggleReminder(reminder.expense.id, enabled) },
                onEdit = { onEditReminder(reminder.expense.id) },
                onDismiss = { onDismissReminder(reminder.expense.id) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// ============================================
// Preview Composables
// ============================================

@Preview(name = "Notifications Content - Phone", showBackground = true, widthDp = 380, heightDp = 800)
@Composable
private fun PreviewNotificationsContentPhone() {
    CarTrackingAppTheme(darkTheme = false) {
        NotificationsContent(
            reminders = listOf(
                ExpenseReminder(
                    expense = Expense(
                        id = 1,
                        carId = 1,
                        category = "Oil Change",
                        amount = 120.0,
                        timestamp = System.currentTimeMillis(),
                        notes = "Regular maintenance",
                        reminderEnabled = true,
                        reminderDate = System.currentTimeMillis() + 5 * 24 * 60 * 60 * 1000L,
                        reminderMileage = 50000,
                        preExpiryNotificationSent = true,
                        reminderDismissed = false
                    ),
                    carName = "Toyota Corolla",
                    currentOdometer = 49700.0,
                    remainingKm = 300
                ),
                ExpenseReminder(
                    expense = Expense(
                        id = 2,
                        carId = 1,
                        category = "Tire Change",
                        amount = 450.0,
                        timestamp = System.currentTimeMillis(),
                        notes = "Replace all tires",
                        reminderEnabled = true,
                        reminderDate = System.currentTimeMillis() + 30 * 24 * 60 * 60 * 1000L,
                        reminderMileage = null,
                        preExpiryNotificationSent = false,
                        reminderDismissed = false
                    ),
                    carName = "Toyota Corolla",
                    currentOdometer = 48000.0,
                    remainingKm = null
                )
            ),
            onToggleReminder = { _, _ -> },
            onEditReminder = {},
            onDismissReminder = {},
            useCenteredLayout = false,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Preview(name = "Notifications Content - Tablet", showBackground = true, widthDp = 800, heightDp = 600)
@Composable
private fun PreviewNotificationsContentTablet() {
    CarTrackingAppTheme(darkTheme = false) {
        NotificationsContent(
            reminders = listOf(
                ExpenseReminder(
                    expense = Expense(
                        id = 1,
                        carId = 1,
                        category = "Service & Maintenance",
                        amount = 250.0,
                        timestamp = System.currentTimeMillis(),
                        notes = "Annual service",
                        reminderEnabled = true,
                        reminderDate = System.currentTimeMillis() + 15 * 24 * 60 * 60 * 1000L,
                        reminderMileage = 55000,
                        preExpiryNotificationSent = false,
                        reminderDismissed = false
                    ),
                    carName = "Honda Civic",
                    currentOdometer = 52500.0,
                    remainingKm = 2500
                ),
                ExpenseReminder(
                    expense = Expense(
                        id = 2,
                        carId = 1,
                        category = "Brake Inspection",
                        amount = 180.0,
                        timestamp = System.currentTimeMillis(),
                        notes = "Check brake pads",
                        reminderEnabled = false,
                        reminderDate = System.currentTimeMillis() + 45 * 24 * 60 * 60 * 1000L,
                        reminderMileage = null,
                        preExpiryNotificationSent = false,
                        reminderDismissed = false
                    ),
                    carName = "Honda Civic",
                    currentOdometer = 52500.0,
                    remainingKm = null
                )
            ),
            onToggleReminder = { _, _ -> },
            onEditReminder = {},
            onDismissReminder = {},
            useCenteredLayout = true,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Preview(name = "Notifications Content - Dark", showBackground = true, widthDp = 380, heightDp = 800)
@Composable
private fun PreviewNotificationsContentDark() {
    CarTrackingAppTheme(darkTheme = true) {
        NotificationsContent(
            reminders = listOf(
                ExpenseReminder(
                    expense = Expense(
                        id = 1,
                        carId = 1,
                        category = "Insurance Renewal",
                        amount = 800.0,
                        timestamp = System.currentTimeMillis(),
                        notes = "Annual insurance",
                        reminderEnabled = true,
                        reminderDate = System.currentTimeMillis() + 10 * 24 * 60 * 60 * 1000L,
                        reminderMileage = null,
                        preExpiryNotificationSent = false,
                        reminderDismissed = false
                    ),
                    carName = "BMW 320i",
                    currentOdometer = 65000.0,
                    remainingKm = null
                )
            ),
            onToggleReminder = { _, _ -> },
            onEditReminder = {},
            onDismissReminder = {},
            useCenteredLayout = false,
            modifier = Modifier.fillMaxSize()
        )
    }
}
