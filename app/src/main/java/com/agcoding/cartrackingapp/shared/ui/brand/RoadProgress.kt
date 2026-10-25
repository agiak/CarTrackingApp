package com.agcoding.cartrackingapp.shared.ui.brand

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppColorScheme
import kotlin.math.pow

/** Width to height. The drawing needs 56.3 of its 300 width units to clear. */
private const val ROAD_ASPECT = 5f

/**
 * A long job, as a car driving to the finish flag.
 *
 * Replaces the plain percentage ring on the two screens that wait on a file —
 * the first sync and an import. Both are a wait with a known end, and a
 * journey says that better than an arc does: the distance left to the flag is
 * the thing being measured, and it is the app's own car making it.
 *
 * Painted from the chosen palette, not from the brand teal the splash uses.
 * The splash has to match a window background fixed in the theme; this sits
 * inside the app, where everything else follows the palette picker.
 *
 * The value is tweened on the way in, so a progress source that reports in
 * steps still moves rather than jumping.
 */
@Composable
fun RoadProgress(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = LocalAppColorScheme.current.actionPrimary,
    hub: Color = LocalAppColorScheme.current.backgroundCard,
) {
    val value by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 450, easing = FastOutSlowInEasing),
        label = "road-progress",
    )

    // Sized by its width, not by a fixed height. Everything below is laid out
    // in units of w/300, so the car's height follows the width; at a fixed
    // 56 dp and a phone's full width the antlers are cut off by about 12 dp.
    // 5:1 leaves the tips a few dp of clearance at any width.
    Canvas(modifier.fillMaxWidth().aspectRatio(ROAD_ASPECT)) {
        val w = size.width
        val u = w / 300f // the drawing is laid out in a 300-wide design space
        val barY = size.height - 10f * u
        val barHeight = 8f * u

        drawRoundRect(
            color.copy(alpha = .2f),
            Offset(0f, barY),
            Size(w, barHeight),
            CornerRadius(barHeight / 2),
        )
        // At least one bar-height of fill, so a job that has barely started
        // still shows a mark rather than nothing at all.
        drawRoundRect(
            color,
            Offset(0f, barY),
            Size(maxOf(barHeight, w * value), barHeight),
            CornerRadius(barHeight / 2),
        )

        val fx = w - 6f * u
        val flag = Path().apply {
            moveTo(fx, barY)
            lineTo(fx, barY - 32f * u)
            relativeLineTo(14f * u, 0f)
            relativeLineTo(-4f * u, 6f * u)
            relativeLineTo(4f * u, 6f * u)
            relativeLineTo(-14f * u, 0f)
        }
        drawPath(flag, color, style = Stroke(2.4f * u, cap = StrokeCap.Round, join = StrokeJoin.Round))

        val carW = 84f * u
        val carH = carW * CaribooCar.HEIGHT / CaribooCar.WIDTH
        drawCaribooCar(
            topLeft = Offset(value * (w - carW - 8f * u), barY - carH + 2f * u),
            widthPx = carW,
            glyph = color,
            shade = color,
            hub = hub,
            wheelDeg = value * 1400f,
            // Leans forward while there is still road left, and straightens up
            // on arrival.
            swayDeg = if (value < 1f) -3f else 0f,
        )
    }
}

/**
 * The same thing with nothing to measure: one lap every three seconds.
 *
 * For previews, and for a wait whose length is genuinely unknown — a JSON
 * restore is one indivisible read, and a made-up percentage would be a lie.
 */
@Composable
fun RoadProgressLoop(modifier: Modifier = Modifier) {
    val t by rememberInfiniteTransition(label = "road-loop").animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(durationMillis = 3000, easing = LinearEasing)),
        label = "road-loop-fraction",
    )
    // Reaches the flag at 80% of the lap and rests there, so the restart reads
    // as a new run rather than as the car being yanked backwards.
    val p = (t / .8f).coerceAtMost(1f)
    RoadProgress(progress = 1f - (1f - p).pow(2.2f), modifier = modifier)
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun RoadProgressPreview() {
    CarTrackingAppTheme(darkTheme = true) {
        RoadProgress(progress = 0.42f)
    }
}
