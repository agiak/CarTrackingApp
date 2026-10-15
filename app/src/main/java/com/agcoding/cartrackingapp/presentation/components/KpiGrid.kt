package com.agcoding.cartrackingapp.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppColorScheme
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppDimens
import com.agcoding.cartrackingapp.shared.ui.tokens.AppNumbers

/** One figure in the grid. */
data class Kpi(
    val label: String,
    val value: String,
    val unit: String? = null,
    val onClick: (() -> Unit)? = null,
)

/**
 * The "At a glance" block: a card of figures in two columns.
 *
 * The cells are not separated by dividers but by one-pixel *gaps*, with the
 * grid's own background showing through. The effect is the same to look at and
 * much simpler to keep right: no divider has to know whether it is the last
 * one in a row, and the outer radius clips the whole thing in one go.
 *
 * An odd number of figures leaves the last row half full rather than stretching
 * one cell across it — a double-width cell reads as more important, and it is
 * not, it is just last.
 */
@Composable
fun KpiGrid(
    items: List<Kpi>,
    modifier: Modifier = Modifier,
    columns: Int = 2,
) {
    if (items.isEmpty()) return

    val scheme = LocalAppColorScheme.current
    val dimens = LocalAppDimens.current

    Column(
        verticalArrangement = Arrangement.spacedBy(dimens.size.divider),
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(dimens.radius.groupedList))
            .background(scheme.borderDefault),
    ) {
        items.chunked(columns).forEach { rowItems ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(dimens.size.divider),
                modifier = Modifier.fillMaxWidth(),
            ) {
                rowItems.forEach { kpi -> KpiCell(kpi) }
                // Pad the final row so its cells keep the width of the ones
                // above instead of spreading to fill the gap.
                repeat(columns - rowItems.size) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(scheme.backgroundCard),
                    )
                }
            }
        }
    }
}

@Composable
private fun RowScope.KpiCell(kpi: Kpi) {
    val scheme = LocalAppColorScheme.current

    Column(
        verticalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier
            .weight(1f)
            .background(scheme.backgroundCard)
            .then(if (kpi.onClick != null) Modifier.clickable(onClick = kpi.onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = kpi.label,
                style = MaterialTheme.typography.labelLarge,
                color = scheme.contentSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false),
            )
            // The chevron is the only thing saying this figure is a doorway,
            // so it goes next to the label rather than lost beside the number.
            if (kpi.onClick != null) {
                Spacer(Modifier.width(2.dp))
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    tint = scheme.contentDisabled,
                    modifier = Modifier.size(15.dp),
                )
            }
        }
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = kpi.value,
                style = AppNumbers.tile,
                color = scheme.contentPrimary,
                maxLines = 1,
            )
            if (kpi.unit != null) {
                Spacer(Modifier.width(4.dp))
                Text(
                    text = kpi.unit,
                    style = MaterialTheme.typography.labelLarge,
                    color = scheme.contentSecondary,
                    maxLines = 1,
                    modifier = Modifier.padding(bottom = 2.dp),
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun KpiGridPreview() {
    CarTrackingAppTheme(darkTheme = true) {
        Column(Modifier.padding(18.dp)) {
            KpiGrid(
                items = listOf(
                    Kpi("Distance", "388.964", "km", onClick = {}),
                    Kpi("Consumption", "7,4", "L/100", onClick = {}),
                    Kpi("Refills", "987", onClick = {}),
                    Kpi("Cost / km", "0,40", "€"),
                    Kpi("Trips", "241"),
                ),
            )
            Spacer(Modifier.height(12.dp))
        }
    }
}
