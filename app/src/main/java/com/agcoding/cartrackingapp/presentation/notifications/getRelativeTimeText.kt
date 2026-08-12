package com.agcoding.cartrackingapp.presentation.notifications

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme
import java.util.concurrent.TimeUnit

@Composable
internal fun getRelativeTimeText(timestampMillis: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestampMillis
    val minutes = TimeUnit.MILLISECONDS.toMinutes(diff)
    val hours = TimeUnit.MILLISECONDS.toHours(diff)
    val days = TimeUnit.MILLISECONDS.toDays(diff)

    return when {
        minutes < 1 -> stringResource(R.string.notification_history_just_now)
        minutes < 60 -> stringResource(R.string.notification_history_minutes_ago, minutes)
        hours < 24 -> stringResource(R.string.notification_history_hours_ago, hours)
        days < 7 -> stringResource(R.string.notification_history_days_ago, days)
        days < 30 -> stringResource(R.string.notification_history_weeks_ago, days / 7)
        else -> stringResource(R.string.notification_history_months_ago, days / 30)
    }
}

@Preview(showBackground = true)
@Composable
private fun GetRelativeTimeTextPreview() {
    CarTrackingAppTheme {
        Text(
            text = getRelativeTimeText(System.currentTimeMillis() - 2 * 60 * 60 * 1000L)
        )
    }
}
