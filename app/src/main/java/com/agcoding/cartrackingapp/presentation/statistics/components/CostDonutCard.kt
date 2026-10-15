package com.agcoding.cartrackingapp.presentation.statistics.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.presentation.components.AutoSizeText
import com.agcoding.cartrackingapp.presentation.components.CostSegment
import com.agcoding.cartrackingapp.presentation.components.StyledCard
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppColorScheme
import com.agcoding.cartrackingapp.shared.ui.tokens.AppNumbers
import com.agcoding.cartrackingapp.shared.ui.tokens.categoryFuel
import com.agcoding.cartrackingapp.shared.ui.tokens.categoryOther
import com.agcoding.cartrackingapp.shared.ui.tokens.categoryService
import com.agcoding.cartrackingapp.util.formatMoney
import com.agcoding.cartrackingapp.util.formatNumber
import kotlin.math.roundToInt

/** The ring's outer size and band width; the hole between them holds the total. */
private val DONUT_SIZE = 200.dp
private val DONUT_STROKE = 22.dp

/**
 * Where all the money went, in one card.
 *
 * This replaces three separate cards that each held a piece of the same
 * answer — a total, a breakdown, and an expenses overview — and so made the
 * user assemble it by scrolling between them. A ring puts the total in the
 * middle of its own parts, which is the only arrangement where "is fuel most
 * of it" needs no arithmetic.
 *
 * A ring rather than a pie because the middle of a pie is its least readable
 * region and its most valuable space: three thin wedges meeting at a point,
 * exactly where the total wants to be.
 */
@Composable
fun CostDonutCard(
    totalCost: Double,
    costPerKilometer: Double,
    fuel: Double,
    service: Double,
    other: Double,
    serviceCount: Int,
    otherCount: Int,
    modifier: Modifier = Modifier,
) {
    val scheme = LocalAppColorScheme.current
    val segments = listOf(
        CostSegment(fuel, scheme.categoryFuel, stringResource(R.string.fuel)),
        CostSegment(service, scheme.categoryService, stringResource(R.string.service)),
        CostSegment(other, scheme.categoryOther, stringResource(R.string.other)),
    )

    StyledCard(modifier = modifier.fillMaxWidth()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(18.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                Donut(segments = segments, track = scheme.backgroundSecondary)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    // The hole in the ring, less a margin so a long total stops
                    // short of the stroke instead of touching it. Without a
                    // bound the total simply drew across the ring: at 24sp
                    // "154.611,52 €" is wider than the 156dp hole, and the
                    // figure sat on top of the fuel and service arcs.
                    modifier = Modifier.width(DONUT_SIZE - DONUT_STROKE * 2 - 16.dp),
                ) {
                    Text(
                        text = stringResource(R.string.total_cost),
                        style = MaterialTheme.typography.labelLarge,
                        color = scheme.contentSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(Modifier.height(2.dp))
                    AutoSizeText(
                        text = totalCost.formatMoney(),
                        style = AppNumbers.donut,
                        color = scheme.contentPrimary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    if (costPerKilometer > 0) {
                        Spacer(Modifier.height(2.dp))
                        AutoSizeText(
                            text = stringResource(
                                R.string.cost_per_km_format,
                                costPerKilometer.formatNumber(2),
                            ),
                            style = MaterialTheme.typography.labelLarge,
                            color = scheme.contentSecondary,
                            minFontSize = 9.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }

            Spacer(Modifier.height(18.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LegendCell(segments[0], totalCost, null)
                LegendCell(segments[1], totalCost, serviceCount)
                LegendCell(segments[2], totalCost, otherCount)
            }
        }
    }
}

/**
 * The ring.
 *
 * Each slice is pulled in by half a gap at both ends rather than drawn
 * butt-to-butt, so neighbouring colours never touch. Without that, fuel and
 * service — which are the same hue rotated — blur into one band at the seam.
 *
 * A share too small to survive its own gaps is drawn at a visible minimum
 * instead of vanishing: 0.4% of a year's spending is still a real category,
 * and an empty space where a colour should be reads as a bug.
 */
@Composable
private fun Donut(segments: List<CostSegment>, track: Color) {
    val total = segments.sumOf { it.value }

    Canvas(modifier = Modifier.size(DONUT_SIZE)) {
        val strokeWidth = DONUT_STROKE.toPx()
        val diameter = size.minDimension - strokeWidth
        val topLeft = Offset((size.width - diameter) / 2f, (size.height - diameter) / 2f)
        val arcSize = Size(diameter, diameter)
        val stroke = Stroke(width = strokeWidth, cap = StrokeCap.Butt)

        if (total <= 0.0) {
            drawArc(
                color = track,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = stroke,
            )
            return@Canvas
        }

        val gapDegrees = 2.2f
        var start = -90f
        segments.filter { it.value > 0.0 }.forEach { segment ->
            val sweep = (segment.value / total * 360.0).toFloat()
            val visible = (sweep - gapDegrees).coerceAtLeast(1.2f)
            drawArc(
                color = segment.color,
                startAngle = start + gapDegrees / 2f,
                sweepAngle = visible,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = stroke,
            )
            start += sweep
        }
    }
}

@Composable
private fun RowScope.LegendCell(segment: CostSegment, total: Double, count: Int?) {
    val scheme = LocalAppColorScheme.current
    val share = if (total > 0) (segment.value / total * 100).roundToInt() else 0

    Column(
        modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(14.dp))
            .background(scheme.backgroundSecondary)
            .padding(10.dp),
    ) {
        Box(
            modifier = Modifier
                .size(width = 18.dp, height = 4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(segment.color),
        )
        Spacer(Modifier.height(8.dp))
        Text(
            // The count rides with the name because "Service" alone cannot say
            // whether it is one big bill or thirty small ones.
            text = if (count != null) "${segment.label} · $count" else segment.label,
            style = MaterialTheme.typography.labelLarge,
            color = scheme.contentSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(3.dp))
        Text(
            text = segment.value.formatMoney(),
            style = AppNumbers.compact,
            color = scheme.contentPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = "$share%",
            style = MaterialTheme.typography.labelSmall,
            color = scheme.contentDisabled,
            maxLines = 1,
            textAlign = TextAlign.Start,
        )
    }
}

@Preview(showBackground = true, widthDp = 380)
@Composable
private fun CostDonutCardPreview() {
    CarTrackingAppTheme(darkTheme = true) {
        Column(Modifier.padding(18.dp)) {
            CostDonutCard(
                totalCost = 154611.52,
                costPerKilometer = 0.40,
                fuel = 96000.0,
                service = 41000.0,
                other = 17611.52,
                serviceCount = 389,
                otherCount = 141,
            )
        }
    }
}

@Preview(name = "Donut — nothing spent", showBackground = true, widthDp = 380)
@Composable
private fun CostDonutCardEmptyPreview() {
    CarTrackingAppTheme(darkTheme = false) {
        Column(Modifier.padding(18.dp)) {
            CostDonutCard(
                totalCost = 0.0,
                costPerKilometer = 0.0,
                fuel = 0.0, service = 0.0, other = 0.0,
                serviceCount = 0, otherCount = 0,
            )
        }
    }
}
