package com.agcoding.cartrackingapp.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppColorScheme
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppDimens
import com.agcoding.cartrackingapp.shared.ui.tokens.categoryOther
import com.agcoding.cartrackingapp.shared.ui.tokens.categoryService

/**
 * Which accent an icon tile wears. Rows are not free to pick a colour: these
 * are the same four meanings used by the charts, so a rose tile means the same
 * thing in Settings as a rose band means on a cost bar.
 */
enum class RowAccent { Primary, Service, Other, Success }

@Composable
private fun RowAccent.color(): Color {
    val scheme = LocalAppColorScheme.current
    return when (this) {
        RowAccent.Primary -> scheme.actionPrimary
        RowAccent.Service -> scheme.categoryService
        RowAccent.Other -> scheme.categoryOther
        RowAccent.Success -> scheme.statusSuccess
    }
}

/**
 * One card holding a run of rows separated by hairlines.
 *
 * This replaces the app's habit of giving every setting its own card. A
 * stack of separate cards reads as a stack of unrelated things; one card with
 * dividers reads as a list, which is what it is — and it is what the eye can
 * skim without counting shadows.
 *
 * Dividers are inserted between the rows rather than left to each caller,
 * because the failure mode of leaving it to callers is a list that is almost
 * consistent.
 */
@Composable
fun <T> GroupedList(
    items: List<T>,
    modifier: Modifier = Modifier,
    row: @Composable (T) -> Unit,
) {
    GroupedListContainer(modifier = modifier) {
        items.forEachIndexed { index, item ->
            if (index > 0) GroupedDivider()
            row(item)
        }
    }
}

/**
 * The same card for rows that are not all the same shape — a switch here, a
 * chevron there. Separate the children yourself with [GroupedDivider].
 */
@Composable
fun GroupedListContainer(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val dimens = LocalAppDimens.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(dimens.radius.groupedList))
            .background(MaterialTheme.colorScheme.surface),
        content = content,
    )
}

/**
 * The hairline between two rows.
 *
 * Inset to clear the icon tile so the line starts where the text does — a
 * divider running under the icons chops the column of tiles in half.
 */
@Composable
fun GroupedDivider(startInset: Boolean = true) {
    val dimens = LocalAppDimens.current
    HorizontalDivider(
        thickness = dimens.size.divider,
        color = LocalAppColorScheme.current.borderDefault,
        modifier = Modifier.padding(start = if (startInset) 68.dp else 0.dp),
    )
}

/**
 * A row: icon tile, title over subtitle, and something on the right.
 *
 * @param trailing replaces the chevron — a switch, a pill, a value. A row with
 *   a trailing control is usually not itself clickable, so the chevron would
 *   be promising a destination that does not exist.
 */
@Composable
fun GroupedRow(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    icon: ImageVector? = null,
    accent: RowAccent = RowAccent.Primary,
    onClick: (() -> Unit)? = null,
    showChevron: Boolean = onClick != null,
    trailing: (@Composable () -> Unit)? = null,
) {
    val scheme = LocalAppColorScheme.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        if (icon != null) {
            IconTile(icon = icon, accent = accent)
            Spacer(Modifier.width(14.dp))
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = scheme.contentPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.contentSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        // A trailing control wins over the chevron: a row that already offers
        // a switch is not also a doorway, and showing both says it is.
        if (trailing != null) {
            Spacer(Modifier.width(12.dp))
            trailing()
        } else if (showChevron) {
            Spacer(Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = scheme.contentDisabled,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

/**
 * The rounded square an icon sits in: the accent at low opacity behind the
 * accent at full strength, so the tile reads as tinted rather than as a
 * coloured block competing with the title next to it.
 */
@Composable
fun IconTile(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    accent: RowAccent = RowAccent.Primary,
) {
    val dimens = LocalAppDimens.current
    val tint = accent.color()
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(dimens.size.iconTile)
            .clip(RoundedCornerShape(dimens.radius.iconTile))
            .background(tint.copy(alpha = 0.16f)),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun GroupedListPreview() {
    CarTrackingAppTheme(darkTheme = true) {
        Column(Modifier.padding(18.dp)) {
            SectionLabel("General")
            GroupedListContainer(modifier = Modifier.padding(top = 10.dp)) {
                GroupedRow(
                    title = "Appearance",
                    subtitle = "Dark · English",
                    icon = Icons.Default.Palette,
                    onClick = {},
                )
                GroupedDivider()
                GroupedRow(
                    title = "Notifications & Help",
                    subtitle = "Service reminders on",
                    icon = Icons.Default.Notifications,
                    accent = RowAccent.Service,
                    onClick = {},
                )
                GroupedDivider()
                GroupedRow(
                    title = "Data & Storage",
                    subtitle = "12,4 MB",
                    icon = Icons.Default.Storage,
                    accent = RowAccent.Other,
                    onClick = {},
                )
            }
        }
    }
}
