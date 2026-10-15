package com.agcoding.cartrackingapp.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppColorScheme
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppDimens

/**
 * The top of a main screen: a large title, a line of context under it, and the
 * screen's icon actions on the right.
 *
 * It is content, not an app bar. The redesign scrolls the title away with
 * everything else rather than pinning it, which on these screens is the right
 * trade: the title says where you are, and once you are reading a list of
 * refills you already know. What it buys is the full height of a phone for the
 * data, and a title that can be 28sp without a collapsing-toolbar animation to
 * maintain.
 *
 * @param meta the quiet line under the title — "3 vehicles · 388.964 km
 *   tracked". Omitted when there is nothing true to say there; an empty line
 *   still takes its space and looks like a bug.
 */
@Composable
fun ScreenHeader(
    title: String,
    modifier: Modifier = Modifier,
    meta: String? = null,
    horizontalPadding: Dp = LocalAppDimens.current.spacing.screenHorizontal,
    actions: @Composable () -> Unit = {},
) {
    val scheme = LocalAppColorScheme.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding)
            .padding(top = 12.dp, bottom = 14.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.displaySmall,
                color = scheme.contentPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (meta != null) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = meta,
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.contentSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            actions()
        }
    }
}

/**
 * A circular icon button on the card surface.
 *
 * Bigger than Material's bare icon button and with a visible container,
 * because on a screen with no app bar these two are the only chrome: they have
 * to look like controls rather than like decorations floating next to a title.
 *
 * @param showDot draws the unread marker. It is a plain dot rather than a
 *   count — the number of pending reminders is not something to decide at a
 *   glance, only whether there are any.
 */
@Composable
fun HeaderIconButton(
    icon: ImageVector,
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    showDot: Boolean = false,
) {
    val scheme = LocalAppColorScheme.current
    val dimens = LocalAppDimens.current

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(dimens.size.headerAction)
            .clip(CircleShape)
            .background(scheme.backgroundCard)
            .clickable(onClick = onClick),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = scheme.contentPrimary,
            modifier = Modifier.size(20.dp),
        )
        if (showDot) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-9).dp, y = 9.dp)
                    .size(7.dp)
                    .clip(CircleShape)
                    .background(scheme.actionPrimary),
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 380)
@Composable
private fun ScreenHeaderPreview() {
    CarTrackingAppTheme(darkTheme = true) {
        Column {
            ScreenHeader(
                title = "My Cars",
                meta = "3 vehicles · 388.964 km tracked",
                actions = {
                    HeaderIconButton(
                        icon = Icons.Default.Notifications,
                        contentDescription = null,
                        onClick = {},
                        showDot = true,
                    )
                    HeaderIconButton(
                        icon = Icons.Default.AccountCircle,
                        contentDescription = null,
                        onClick = {},
                    )
                },
            )
            ScreenHeader(title = "Statistics")
        }
    }
}
