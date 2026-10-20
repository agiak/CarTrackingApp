package com.agcoding.cartrackingapp.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppColorScheme
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppDimens

/** How long a chip takes to change colour. Long enough to see, short enough not to wait for. */
private const val CHIP_TRANSITION_MILLIS = 150

/**
 * The app's one filter chip: car pickers, quick date ranges, insight types,
 * graph metrics.
 *
 * Selected is drawn as an inversion — ink fill, background-coloured text —
 * rather than as the accent colour. The accent is reserved for things that are
 * *about money* (totals, the fuel share, the FAB); if selection also claimed
 * it, a row of chips would compete with the number the screen exists to show.
 */
@Composable
fun AppFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val scheme = LocalAppColorScheme.current
    val dimens = LocalAppDimens.current

    val container by animateColorAsState(
        targetValue = if (selected) scheme.contentPrimary else scheme.backgroundCard,
        animationSpec = tween(CHIP_TRANSITION_MILLIS),
        label = "chipContainer",
    )
    val content by animateColorAsState(
        targetValue = if (selected) scheme.backgroundPrimary else scheme.contentSecondary,
        animationSpec = tween(CHIP_TRANSITION_MILLIS),
        label = "chipContent",
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .height(dimens.size.chipHeight)
            .clip(RoundedCornerShape(dimens.radius.chip))
            .background(container)
            .then(
                // The border would double the outline of a selected chip,
                // which at this size reads as a thicker, slightly wrong shape.
                if (selected) Modifier
                else Modifier.border(
                    BorderStroke(dimens.size.divider, scheme.borderDefault),
                    RoundedCornerShape(dimens.radius.chip),
                )
            )
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 14.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            // Dimmed by colour rather than by a layer over the chip: the layer
            // came after `background` in the chain, so it faded the label and
            // left the fill alone. See PrimaryButton.
            color = if (enabled) content else scheme.contentDisabled,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/**
 * The chip that says which period the screen is currently showing, and lets
 * you drop it.
 *
 * It wears the accent because it is not one choice among several — it is the
 * statement that everything below is filtered, which is the one thing a user
 * must not miss when a total looks wrong.
 */
@Composable
fun ActiveDateChip(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    onClear: (() -> Unit)? = null,
) {
    val scheme = LocalAppColorScheme.current
    val dimens = LocalAppDimens.current

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .height(dimens.size.chipHeight)
            .clip(RoundedCornerShape(dimens.radius.chip))
            .background(scheme.actionSecondary)
            .clickable(onClick = onClick)
            .padding(start = 10.dp, end = if (onClear != null) 6.dp else 12.dp),
    ) {
        Icon(
            imageVector = Icons.Default.CalendarMonth,
            contentDescription = null,
            tint = scheme.actionPrimary,
            modifier = Modifier.size(15.dp),
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = scheme.actionPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (onClear != null) {
            Spacer(Modifier.width(4.dp))
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(22.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .clickable(onClick = onClear),
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = null,
                    tint = scheme.actionPrimary,
                    modifier = Modifier.size(14.dp),
                )
            }
        }
    }
}

/**
 * A scrolling run of chips, with the screen's margin as padding so the first
 * and last chip line up with the cards above and below while the row itself
 * still scrolls edge to edge.
 */
@Composable
fun FilterChipRow(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val dimens = LocalAppDimens.current
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = dimens.spacing.screenHorizontal),
    ) {
        content()
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun AppFilterChipPreview() {
    CarTrackingAppTheme(darkTheme = true) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .padding(18.dp)
                .defaultMinSize(minHeight = 40.dp),
        ) {
            AppFilterChip(label = "All cars", selected = true, onClick = {})
            AppFilterChip(label = "Corolla", selected = false, onClick = {})
            AppFilterChip(label = "Yaris", selected = false, onClick = {}, enabled = false)
            ActiveDateChip(label = "Oct 2026", onClick = {}, onClear = {})
        }
    }
}
