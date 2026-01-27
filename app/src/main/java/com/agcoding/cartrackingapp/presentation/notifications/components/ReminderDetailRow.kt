package com.agcoding.cartrackingapp.presentation.notifications.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme

@Composable
fun ReminderDetailRow(
    icon: ImageVector,
    label: String,
    value: String,
    additionalInfo: String? = null,
    isUrgent: Boolean = false,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.Top,
        modifier = modifier.fillMaxWidth()
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

// ============================================
// Preview Composables
// ============================================

@Preview(name = "Reminder Detail Row - Date Normal", showBackground = true, widthDp = 300)
@Composable
private fun PreviewReminderDetailRowDateNormal() {
    CarTrackingAppTheme(darkTheme = false) {
        ReminderDetailRow(
            icon = Icons.Default.CalendarToday,
            label = "Reminder Date",
            value = "15 Feb 2026",
            additionalInfo = "In 19 days",
            isUrgent = false
        )
    }
}

@Preview(name = "Reminder Detail Row - Date Urgent", showBackground = true, widthDp = 300)
@Composable
private fun PreviewReminderDetailRowDateUrgent() {
    CarTrackingAppTheme(darkTheme = false) {
        ReminderDetailRow(
            icon = Icons.Default.CalendarToday,
            label = "Reminder Date",
            value = "30 Jan 2026",
            additionalInfo = "In 3 days",
            isUrgent = true
        )
    }
}

@Preview(name = "Reminder Detail Row - Mileage", showBackground = true, widthDp = 300)
@Composable
private fun PreviewReminderDetailRowMileage() {
    CarTrackingAppTheme(darkTheme = false) {
        ReminderDetailRow(
            icon = Icons.Default.Speed,
            label = "Reminder Mileage",
            value = "50,000 km",
            additionalInfo = "1,500 km remaining",
            isUrgent = false
        )
    }
}

@Preview(name = "Reminder Detail Row - Dark", showBackground = true, widthDp = 300)
@Composable
private fun PreviewReminderDetailRowDark() {
    CarTrackingAppTheme(darkTheme = true) {
        ReminderDetailRow(
            icon = Icons.Default.Speed,
            label = "Reminder Mileage",
            value = "48,500 km",
            additionalInfo = "Target reached",
            isUrgent = true
        )
    }
}
