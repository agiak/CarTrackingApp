package com.agcoding.cartrackingapp.presentation.notifications.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme
import java.util.concurrent.TimeUnit

@Composable
internal fun getDaysUntilText(dateMillis: Long): String {
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

@Preview(showBackground = true)
@Composable
private fun GetDaysUntilTextPreview() {
    CarTrackingAppTheme {
        Text(
            text = getDaysUntilText(System.currentTimeMillis() + 3 * 24 * 60 * 60 * 1000L)
        )
    }
}
