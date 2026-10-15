package com.agcoding.cartrackingapp.presentation.settings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.agcoding.cartrackingapp.presentation.components.GroupedDivider
import com.agcoding.cartrackingapp.presentation.components.GroupedListContainer
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppColorScheme
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppDimens

/**
 * A row inside one of the settings cards.
 *
 * Kept as its own component rather than folded into the shared GroupedRow
 * because its callers hand it a colour per row — notifications in the accent,
 * help in the tertiary — and that per-row tint is how the longer cards stay
 * scannable. What it takes from the shared row is the shape: a 38dp rounded
 * square rather than a 40dp circle, and the token type scale, so a settings
 * card and a grouped list look like the same object.
 */
@Composable
fun SettingsRow(
    icon: ImageVector,
    iconBackgroundColor: Color,
    iconTint: Color,
    title: String,
    subtitle: String?,
    onClick: (() -> Unit)? = null,
    enabled: Boolean = true,
    trailing: @Composable () -> Unit
) {
    val scheme = LocalAppColorScheme.current
    val dimens = LocalAppDimens.current
    // One factor for everything that dims together, so a disabled row cannot
    // end up with a bright icon above faded text.
    val contentAlpha = if (enabled) 1f else 0.4f

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (onClick != null && enabled) Modifier.clickable(onClick = onClick)
                else Modifier
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(dimens.size.iconTile)
                .clip(RoundedCornerShape(dimens.radius.iconTile))
                .background(iconBackgroundColor.copy(alpha = iconBackgroundColor.alpha * contentAlpha)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = iconTint.copy(alpha = iconTint.alpha * contentAlpha)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = scheme.contentPrimary.copy(alpha = contentAlpha),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (subtitle != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.contentSecondary.copy(alpha = contentAlpha),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        trailing()
    }
}

@Preview(showBackground = true, widthDp = 380)
@Composable
private fun SettingsRowPreview() {
    CarTrackingAppTheme(darkTheme = true) {
        Column(Modifier.padding(18.dp)) {
            GroupedListContainer {
                SettingsRow(
                    icon = Icons.Default.Notifications,
                    iconBackgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                    iconTint = MaterialTheme.colorScheme.primary,
                    title = "Service reminders",
                    subtitle = "On",
                    trailing = { Switch(checked = true, onCheckedChange = {}) },
                )
                GroupedDivider()
                SettingsRow(
                    icon = Icons.Default.Info,
                    iconBackgroundColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.16f),
                    iconTint = MaterialTheme.colorScheme.tertiary,
                    title = "Version",
                    subtitle = "1.2.1 (27)",
                    enabled = false,
                    trailing = {},
                )
            }
        }
    }
}
