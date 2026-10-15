package com.agcoding.cartrackingapp.presentation.carlist.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.domain.usecase.expense.ReminderInfo
import com.agcoding.cartrackingapp.presentation.components.StyledCard
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppColorScheme
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppDimens
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderBanner(
    reminderInfo: ReminderInfo,
    onClick: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    // Create a descriptive message based on reminder types
    val message = when {
        reminderInfo.dateBasedCount > 0 && reminderInfo.mileageBasedCount > 0 -> {
            // Both types
            stringResource(
                R.string.reminder_banner_mixed,
                reminderInfo.dateBasedCount,
                reminderInfo.mileageBasedCount
            )
        }
        reminderInfo.dateBasedCount > 0 -> {
            // Only date-based
            if (reminderInfo.dateBasedCount == 1) {
                stringResource(R.string.reminder_banner_date_single)
            } else {
                stringResource(R.string.reminder_banner_date_multiple, reminderInfo.dateBasedCount)
            }
        }
        reminderInfo.mileageBasedCount > 0 -> {
            // Only mileage-based
            if (reminderInfo.mileageBasedCount == 1) {
                stringResource(R.string.reminder_banner_mileage_single)
            } else {
                stringResource(R.string.reminder_banner_mileage_multiple, reminderInfo.mileageBasedCount)
            }
        }
        else -> ""
    }

    AnimatedVisibility(
        visible = isVisible,
        exit = fadeOut(
            animationSpec = tween(
                durationMillis = 300,
                delayMillis = 0
            )
        ) + shrinkVertically(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        )
    ) {
        val dismissState = rememberSwipeToDismissBoxState(
            confirmValueChange = { dismissValue ->
                when (dismissValue) {
                    SwipeToDismissBoxValue.StartToEnd, SwipeToDismissBoxValue.EndToStart -> {
                        isVisible = false
                        // Add slight delay for smooth animation completion
                        scope.launch {
                            kotlinx.coroutines.delay(250)
                            onDismiss()
                        }
                        true
                    }
                    else -> false
                }
            }
        )

        SwipeToDismissBox(
            state = dismissState,
            modifier = modifier.fillMaxWidth(),
            backgroundContent = {
                val direction = dismissState.dismissDirection
                val isActive = dismissState.targetValue != SwipeToDismissBoxValue.Settled
                val color = when (dismissState.targetValue) {
                    SwipeToDismissBoxValue.StartToEnd, SwipeToDismissBoxValue.EndToStart ->
                        MaterialTheme.colorScheme.errorContainer
                    else -> Color.Transparent
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color, RoundedCornerShape(12.dp))
                        .padding(horizontal = 20.dp),
                    contentAlignment = when (direction) {
                        SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                        SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                        else -> Alignment.CenterEnd
                    }
                ) {
                    // Only show icon when actively swiping
                    if (isActive) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.dismiss),
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        ) {
            val scheme = LocalAppColorScheme.current
            val dimens = LocalAppDimens.current

            StyledCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onClick),
                // The accent at low opacity, not the accent itself: this is a
                // nudge sitting above the user's cars, and a solid block of
                // the brand colour would outrank the thing it interrupts.
                containerColor = scheme.actionSecondary,
                border = null
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(dimens.radius.iconTile))
                            .background(scheme.actionPrimary)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = null,
                            tint = scheme.actionContent,
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.reminder_banner_title),
                            style = MaterialTheme.typography.titleSmall,
                            color = scheme.contentPrimary
                        )
                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodySmall,
                            color = scheme.contentSecondary
                        )
                    }

                    // No chevron beside the ×: the whole banner is the target,
                    // and two affordances a thumb-width apart, one of which
                    // makes it go away, is a bad place to be imprecise.
                    IconButton(
                        onClick = {
                            isVisible = false
                            scope.launch {
                                kotlinx.coroutines.delay(250)
                                onDismiss()
                            }
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.dismiss),
                            tint = scheme.contentSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

// ============================================
// Preview Composables
// ============================================

@Preview(name = "Reminder Banner - Date Only", showBackground = true, widthDp = 400)
@Composable
private fun PreviewReminderBannerDateOnly() {
    CarTrackingAppTheme(darkTheme = false) {
        ReminderBanner(
            reminderInfo = ReminderInfo(
                dateBasedCount = 2,
                mileageBasedCount = 0
            ),
            onClick = {},
            onDismiss = {}
        )
    }
}

@Preview(name = "Reminder Banner - Mileage Only", showBackground = true, widthDp = 400)
@Composable
private fun PreviewReminderBannerMileageOnly() {
    CarTrackingAppTheme(darkTheme = false) {
        ReminderBanner(
            reminderInfo = ReminderInfo(
                dateBasedCount = 0,
                mileageBasedCount = 3
            ),
            onClick = {},
            onDismiss = {}
        )
    }
}

@Preview(name = "Reminder Banner - Mixed", showBackground = true, widthDp = 400)
@Composable
private fun PreviewReminderBannerMixed() {
    CarTrackingAppTheme(darkTheme = false) {
        ReminderBanner(
            reminderInfo = ReminderInfo(
                dateBasedCount = 1,
                mileageBasedCount = 2
            ),
            onClick = {},
            onDismiss = {}
        )
    }
}

@Preview(name = "Reminder Banner - Dark", showBackground = true, widthDp = 400)
@Composable
private fun PreviewReminderBannerDark() {
    CarTrackingAppTheme(darkTheme = true) {
        ReminderBanner(
            reminderInfo = ReminderInfo(
                dateBasedCount = 1,
                mileageBasedCount = 1
            ),
            onClick = {},
            onDismiss = {}
        )
    }
}
