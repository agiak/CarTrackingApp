package com.agcoding.cartrackingapp.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.agcoding.cartrackingapp.R

/**
 * The two icons that sit on every tab screen's top bar.
 *
 * They live here rather than being written out in each screen because four
 * copies of an icon button is four chances for one of them to drift — a
 * different icon, a missing content description, the wrong destination. A tab
 * bar's whole value is that the same thing is in the same place on every tab.
 */

/** Top left on the tab screens: opens the account. */
@Composable
fun ProfileNavigationIcon(onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = stringResource(R.string.tab_profile_cd),
        )
    }
}

/** Top right on the tab screens that have room for it: opens the reminders. */
@Composable
fun NotificationsAction(onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        Icon(
            imageVector = Icons.Default.Notifications,
            contentDescription = stringResource(R.string.tab_notifications_cd),
        )
    }
}
