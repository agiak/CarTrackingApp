package com.agcoding.cartrackingapp.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppColorScheme
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppDimens
import com.agcoding.cartrackingapp.shared.ui.tokens.categoryFuel
import com.agcoding.cartrackingapp.shared.ui.tokens.categoryOther
import com.agcoding.cartrackingapp.shared.ui.tokens.categoryService

/** One share of a cost bar. */
data class CostSegment(val value: Double, val color: Color, val label: String)

/**
 * Where the money went, as one bar: fuel, service, everything else.
 *
 * The segments are separated by a gap rather than by a border. A border has to
 * sit *somewhere* — on one segment or between two — and at this height either
 * choice makes one share look a pixel bigger than it is. A gap takes from both
 * equally and reads as a deliberate break rather than as an outline.
 *
 * Shares of zero are dropped instead of drawn at zero width, which would
 * otherwise leave the gap around them and so draw attention to a category that
 * has nothing in it.
 */
@Composable
fun StackedCostBar(
    segments: List<CostSegment>,
    modifier: Modifier = Modifier,
    height: Dp = LocalAppDimens.current.size.costBarHeight,
    gap: Dp = 3.dp,
) {
    val scheme = LocalAppColorScheme.current
    val visible = segments.filter { it.value > 0.0 }
    val shape = RoundedCornerShape(height / 2)

    if (visible.isEmpty()) {
        // Nothing spent yet. An empty track still says "this is where the
        // split will appear", which a missing bar does not.
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(height)
                .clip(shape)
                .background(scheme.backgroundSecondary),
        )
        return
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(gap),
        modifier = modifier
            .fillMaxWidth()
            .height(height),
    ) {
        visible.forEach { segment ->
            Box(
                modifier = Modifier
                    .weight(segment.value.toFloat())
                    .fillMaxHeight()
                    .clip(shape)
                    .background(segment.color),
            )
        }
    }
}

/**
 * The three standard shares, in the order they are always shown.
 *
 * Callers pass amounts, never colours — that is the whole point of having this
 * here, so no screen can quietly decide that service is blue today.
 */
@Composable
fun fuelServiceOtherSegments(
    fuel: Double,
    service: Double,
    other: Double,
    fuelLabel: String,
    serviceLabel: String,
    otherLabel: String,
): List<CostSegment> {
    val scheme = LocalAppColorScheme.current
    return listOf(
        CostSegment(fuel, scheme.categoryFuel, fuelLabel),
        CostSegment(service, scheme.categoryService, serviceLabel),
        CostSegment(other, scheme.categoryOther, otherLabel),
    )
}

/**
 * The key under a cost bar: a colour swatch, what it is, and how much.
 *
 * Laid out in a row that wraps nothing — with three entries on a phone it
 * fits, and a legend that reflows is a legend whose order changes.
 */
@Composable
fun CostBarLegend(
    segments: List<CostSegment>,
    amounts: List<String>,
    modifier: Modifier = Modifier,
) {
    val scheme = LocalAppColorScheme.current
    Row(
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth(),
    ) {
        segments.forEachIndexed { index, segment ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(segment.color),
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = segment.label,
                    style = MaterialTheme.typography.labelLarge,
                    color = scheme.contentSecondary,
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = amounts.getOrElse(index) { "" },
                    style = MaterialTheme.typography.labelLarge,
                    color = scheme.contentPrimary,
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun StackedCostBarPreview() {
    CarTrackingAppTheme(darkTheme = true) {
        val segments = fuelServiceOtherSegments(
            fuel = 463.0, service = 286.0, other = 240.0,
            fuelLabel = "Fuel", serviceLabel = "Service", otherLabel = "Other",
        )
        androidx.compose.foundation.layout.Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(18.dp),
        ) {
            StackedCostBar(segments = segments)
            CostBarLegend(segments = segments, amounts = listOf("463 €", "286 €", "240 €"))
            StackedCostBar(segments = emptyList())
        }
    }
}
