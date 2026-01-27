package com.agcoding.cartrackingapp.presentation.notifications.components

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
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.domain.model.Expense
import com.agcoding.cartrackingapp.domain.usecase.expense.ExpenseReminder
import com.agcoding.cartrackingapp.presentation.components.StyledCard
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@Composable
fun ReminderCard(
    reminder: ExpenseReminder,
    onToggle: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    val isCloseToReached = isReminderCloseToReached(reminder)

    val containerColor = MaterialTheme.colorScheme.surface
    val borderColor = if (isCloseToReached) {
        MaterialTheme.colorScheme.tertiary
    } else {
        MaterialTheme.colorScheme.outlineVariant
    }

    StyledCard(
        modifier = modifier,
        containerColor = containerColor,
        border = androidx.compose.foundation.BorderStroke(
            width = if (isCloseToReached) 2.dp else 1.dp,
            color = borderColor
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header row with icon, service type, and toggle
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            if (isCloseToReached) {
                                MaterialTheme.colorScheme.tertiaryContainer
                            } else {
                                MaterialTheme.colorScheme.primaryContainer
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Default.Build,
                        contentDescription = null,
                        tint = if (isCloseToReached) {
                            MaterialTheme.colorScheme.tertiary
                        } else {
                            MaterialTheme.colorScheme.primary
                        },
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = reminder.expense.category,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = reminder.carName,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Switch(
                    checked = reminder.expense.reminderEnabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Reminder details
            reminder.expense.reminderDate?.let { dateMillis ->
                ReminderDetailRow(
                    icon = Icons.Default.CalendarToday,
                    label = stringResource(R.string.notifications_reminder_date),
                    value = dateFormat.format(Date(dateMillis)),
                    additionalInfo = getDaysUntilText(dateMillis),
                    isUrgent = isCloseToReached
                )
            }

            if (reminder.expense.reminderDate != null && reminder.expense.reminderMileage != null) {
                Spacer(modifier = Modifier.height(8.dp))
            }

            reminder.expense.reminderMileage?.let { targetMileage ->
                ReminderDetailRow(
                    icon = Icons.Default.Speed,
                    label = stringResource(R.string.notifications_reminder_mileage),
                    value = stringResource(R.string.notifications_mileage_value, targetMileage),
                    additionalInfo = reminder.remainingKm?.let {
                        if (it > 0) {
                            stringResource(R.string.notifications_remaining_km, it)
                        } else {
                            stringResource(R.string.notifications_target_reached)
                        }
                    },
                    isUrgent = isCloseToReached
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action buttons row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.End
            ) {
                if (reminder.expense.preExpiryNotificationSent && !reminder.expense.reminderDismissed) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.dismiss))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }

                TextButton(onClick = onEdit) {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = stringResource(R.string.edit),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(R.string.edit))
                }
            }
        }
    }
}

@Composable
private fun getDaysUntilText(dateMillis: Long): String {
    val now = System.currentTimeMillis()
    val diffMillis = dateMillis - now
    val days = TimeUnit.MILLISECONDS.toDays(diffMillis)

    return when {
        days < 0 -> stringResource(R.string.notifications_overdue)
        days == 0L -> stringResource(R.string.notifications_today)
        days == 1L -> stringResource(R.string.notifications_tomorrow)
        days < 7 -> stringResource(R.string.notifications_in_days, days)
        days < 30 -> {
            val weeks = days / 7
            stringResource(R.string.notifications_in_weeks, weeks)
        }
        else -> {
            val months = days / 30
            stringResource(R.string.notifications_in_months, months)
        }
    }
}

private fun isReminderCloseToReached(reminder: ExpenseReminder): Boolean {
    val now = System.currentTimeMillis()

    reminder.expense.reminderDate?.let { dateMillis ->
        val diffMillis = dateMillis - now
        val days = TimeUnit.MILLISECONDS.toDays(diffMillis)
        if (days <= 7) {
            return true
        }
    }

    reminder.remainingKm?.let { remaining ->
        if (remaining <= 500) {
            return true
        }
    }

    return false
}

// ============================================
// Preview Composables
// ============================================

@Preview(name = "Reminder Card - Normal", showBackground = true, widthDp = 380)
@Composable
private fun PreviewReminderCardNormal() {
    CarTrackingAppTheme(darkTheme = false) {
        var enabled by remember { mutableStateOf(true) }

        ReminderCard(
            reminder = ExpenseReminder(
                expense = Expense(
                    id = 1,
                    carId = 1,
                    category = "Oil Change",
                    amount = 120.0,
                    timestamp = System.currentTimeMillis(),
                    notes = "Regular maintenance",
                    reminderEnabled = enabled,
                    reminderDate = System.currentTimeMillis() + 30 * 24 * 60 * 60 * 1000L,
                    reminderMileage = 50000,
                    preExpiryNotificationSent = false,
                    reminderDismissed = false
                ),
                carName = "Toyota Corolla",
                currentOdometer = 47500.0,
                remainingKm = 2500
            ),
            onToggle = { enabled = it },
            onEdit = {},
            onDismiss = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(name = "Reminder Card - Urgent", showBackground = true, widthDp = 380)
@Composable
private fun PreviewReminderCardUrgent() {
    CarTrackingAppTheme(darkTheme = false) {
        var enabled by remember { mutableStateOf(true) }

        ReminderCard(
            reminder = ExpenseReminder(
                expense = Expense(
                    id = 1,
                    carId = 1,
                    category = "Tire Change",
                    amount = 450.0,
                    timestamp = System.currentTimeMillis(),
                    notes = "Replace all tires",
                    reminderEnabled = enabled,
                    reminderDate = System.currentTimeMillis() + 3 * 24 * 60 * 60 * 1000L,
                    reminderMileage = 48500,
                    preExpiryNotificationSent = true,
                    reminderDismissed = false
                ),
                carName = "Honda Civic",
                currentOdometer = 48300.0,
                remainingKm = 200
            ),
            onToggle = { enabled = it },
            onEdit = {},
            onDismiss = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(name = "Reminder Card - Dark", showBackground = true, widthDp = 380)
@Composable
private fun PreviewReminderCardDark() {
    CarTrackingAppTheme(darkTheme = true) {
        var enabled by remember { mutableStateOf(true) }

        ReminderCard(
            reminder = ExpenseReminder(
                expense = Expense(
                    id = 1,
                    carId = 1,
                    category = "Service & Maintenance",
                    amount = 250.0,
                    timestamp = System.currentTimeMillis(),
                    notes = "Annual service",
                    reminderEnabled = enabled,
                    reminderDate = System.currentTimeMillis() + 15 * 24 * 60 * 60 * 1000L,
                    reminderMileage = null,
                    preExpiryNotificationSent = false,
                    reminderDismissed = false
                ),
                carName = "BMW 320i",
                currentOdometer = 62000.0,
                remainingKm = null
            ),
            onToggle = { enabled = it },
            onEdit = {},
            onDismiss = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}
