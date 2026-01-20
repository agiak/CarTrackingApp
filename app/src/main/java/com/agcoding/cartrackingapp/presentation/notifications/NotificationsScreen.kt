package com.agcoding.cartrackingapp.presentation.notifications

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.domain.usecase.expense.ExpenseReminder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onNavigateBack: () -> Unit,
    onEditExpense: (Long) -> Unit,
    viewModel: NotificationsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.notifications_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        when (val state = uiState) {
            is NotificationsUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is NotificationsUiState.Empty -> {
                EmptyState(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }

            is NotificationsUiState.Success -> {
                NotificationsContent(
                    reminders = state.reminders,
                    onToggleReminder = { expenseId, enabled ->
                        viewModel.toggleReminderEnabled(expenseId, enabled)
                    },
                    onEditReminder = onEditExpense,
                    onDismissReminder = { expenseId ->
                        viewModel.dismissReminder(expenseId)
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }

            is NotificationsUiState.Error -> {
                ErrorState(
                    message = state.message,
                    onRetry = { viewModel.retry() },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                )
            }
        }
    }
}

@Composable
private fun NotificationsContent(
    reminders: List<ExpenseReminder>,
    onToggleReminder: (Long, Boolean) -> Unit,
    onEditReminder: (Long) -> Unit,
    onDismissReminder: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(4.dp))
        }

        item {
            // Header section
            Row(
                verticalAlignment = Alignment.CenterVertically
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
                    val activeCount = reminders.count { it.expense.reminderEnabled }
                    Text(
                        text = stringResource(R.string.notifications_active_count, activeCount),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
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

@Composable
private fun ReminderCard(
    reminder: ExpenseReminder,
    onToggle: (Boolean) -> Unit,
    onEdit: () -> Unit,
    onDismiss: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    val isCloseToReached = isReminderCloseToReached(reminder)
    
    // Use warning colors if close to being reached (orange/amber tones)
    val containerColor = if (isCloseToReached) {
        MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f)
    } else {
        MaterialTheme.colorScheme.surface
    }
    
    val borderColor = if (isCloseToReached) {
        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.6f)
    } else {
        MaterialTheme.colorScheme.outlineVariant
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        ),
        border = BorderStroke(
            width = if (isCloseToReached) 2.dp else 1.dp,
            color = borderColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isCloseToReached) 2.dp else 0.dp
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
                                MaterialTheme.colorScheme.secondaryContainer
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Build,
                        contentDescription = null,
                        tint = if (isCloseToReached) {
                            MaterialTheme.colorScheme.tertiary
                        } else {
                            MaterialTheme.colorScheme.secondary
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

                // Toggle switch
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

            // Action buttons row - Edit and Dismiss buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                // Dismiss button (only show if notification was sent)
                if (reminder.expense.preExpiryNotificationSent && !reminder.expense.reminderDismissed) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.dismiss))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                }

                // Edit button
                TextButton(onClick = onEdit) {
                    Icon(
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
private fun ReminderDetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    additionalInfo: String? = null,
    isUrgent: Boolean = false
) {
    Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isUrgent) {
                MaterialTheme.colorScheme.tertiary
            } else {
                MaterialTheme.colorScheme.primary
            },
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            additionalInfo?.let {
                Text(
                    text = it,
                    fontSize = 12.sp,
                    fontWeight = if (isUrgent) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isUrgent) {
                        MaterialTheme.colorScheme.tertiary
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                )
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

/**
 * Determines if a reminder is close to being reached
 * - Date-based: within 7 days or overdue
 * - Mileage-based: within 500 km or reached
 */
private fun isReminderCloseToReached(reminder: ExpenseReminder): Boolean {
    val now = System.currentTimeMillis()
    
    // Check date condition
    reminder.expense.reminderDate?.let { dateMillis ->
        val diffMillis = dateMillis - now
        val days = TimeUnit.MILLISECONDS.toDays(diffMillis)
        // Consider close if within 7 days or already passed
        if (days <= 7) {
            return true
        }
    }
    
    // Check mileage condition
    reminder.remainingKm?.let { remaining ->
        // Consider close if within 500 km or already reached
        if (remaining <= 500) {
            return true
        }
    }
    
    return false
}

@Composable
private fun EmptyState(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.notifications_empty_title),
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.notifications_empty_message),
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

@Composable
private fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            Text(
                text = stringResource(R.string.error_loading_data),
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            TextButton(onClick = onRetry) {
                Text(stringResource(R.string.retry))
            }
        }
    }
}

