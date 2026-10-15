package com.agcoding.cartrackingapp.presentation.transactions.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppColorScheme
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppDimens
import com.agcoding.cartrackingapp.shared.ui.tokens.AppNumbers

/**
 * What this period cost, as the loudest thing on the screen.
 *
 * It is filled with the accent rather than sitting on the card surface like
 * everything else, because it is the one number the screen exists to answer
 * and the list below it is the evidence. Only one element per screen gets to
 * do this.
 *
 * The three shares are drawn in the *foreground* colour at falling opacities
 * rather than in the category colours. On a solid accent background the
 * category hues would fight the surface they sit on — and two of them are
 * derived from that very accent, so service would be nearly invisible on it.
 * Opacity keeps the ordering readable on any palette the user picks.
 *
 * @param onPrevious null when the card is not showing a single month — a
 *   chevron that cannot say which month it would step to is a lie. The sheet
 *   is still there for everything wider than a month.
 */
@Composable
fun MonthSummaryCard(
    periodLabel: String,
    total: String,
    fuel: Double,
    service: Double,
    other: Double,
    fuelLabel: String,
    serviceLabel: String,
    otherLabel: String,
    modifier: Modifier = Modifier,
    onPrevious: (() -> Unit)? = null,
    onNext: (() -> Unit)? = null,
    nextEnabled: Boolean = true,
) {
    val scheme = LocalAppColorScheme.current
    val dimens = LocalAppDimens.current
    val ink = scheme.actionContent

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(dimens.radius.hero))
            .background(scheme.actionPrimary)
            .padding(20.dp),
    ) {
        // ── which period ──────────────────────────────────────────────────
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth(),
        ) {
            if (onPrevious != null) {
                StepArrow(Icons.Default.ChevronLeft, ink, enabled = true, onClick = onPrevious)
            }
            Text(
                text = periodLabel,
                style = MaterialTheme.typography.labelLarge,
                color = ink.copy(alpha = 0.75f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(horizontal = 10.dp),
            )
            if (onNext != null) {
                StepArrow(Icons.Default.ChevronRight, ink, enabled = nextEnabled, onClick = onNext)
            }
        }

        Spacer(Modifier.height(6.dp))

        Text(
            text = total,
            style = AppNumbers.summary,
            color = ink,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.fillMaxWidth(),
        )

        Spacer(Modifier.height(14.dp))

        SplitBar(fuel = fuel, service = service, other = other, ink = ink)

        Spacer(Modifier.height(10.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            LegendEntry(fuelLabel, ink, 1f)
            LegendEntry(serviceLabel, ink, 0.55f)
            LegendEntry(otherLabel, ink, 0.25f)
        }
    }
}

@Composable
private fun StepArrow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    ink: Color,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .clickable(enabled = enabled, onClick = onClick)
            // Dimmed rather than removed: the arrow keeps its place, so the
            // month label does not shift sideways at the end of the range.
            .alpha(if (enabled) 1f else 0.35f),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = ink,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun SplitBar(fuel: Double, service: Double, other: Double, ink: Color) {
    val dimens = LocalAppDimens.current
    val height = dimens.size.costBarHeightCompact
    val shape = RoundedCornerShape(height / 2)
    val shares = listOf(fuel to 1f, service to 0.55f, other to 0.25f)
        .filter { it.first > 0.0 }

    if (shares.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .clip(shape)
                .background(ink.copy(alpha = 0.18f)),
        )
        return
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(height),
    ) {
        shares.forEach { (value, opacity) ->
            Box(
                modifier = Modifier
                    .weight(value.toFloat())
                    .fillMaxHeight()
                    .clip(shape)
                    .background(ink.copy(alpha = opacity)),
            )
        }
    }
}

@Composable
private fun LegendEntry(label: String, ink: Color, opacity: Float) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(ink.copy(alpha = opacity)),
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = ink.copy(alpha = 0.85f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Preview(showBackground = true, widthDp = 380)
@Composable
private fun MonthSummaryCardPreview() {
    CarTrackingAppTheme(darkTheme = true) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            MonthSummaryCard(
                periodLabel = "Οκτώβριος 2026",
                total = "988,46 €",
                fuel = 463.0, service = 286.0, other = 240.0,
                fuelLabel = "Fuel 463 €",
                serviceLabel = "Service 286 €",
                otherLabel = "Other 240 €",
                onPrevious = {}, onNext = {}, nextEnabled = false,
            )
            MonthSummaryCard(
                periodLabel = "All time",
                total = "154.611,52 €",
                fuel = 0.0, service = 0.0, other = 0.0,
                fuelLabel = "Fuel", serviceLabel = "Service", otherLabel = "Other",
            )
        }
    }
}
