package com.agcoding.cartrackingapp.shared.ui.brand

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.pow
import kotlin.math.sin

/** How long an undo stays on offer. Below about four seconds it is a tease. */
const val UNDO_WINDOW_MILLIS = 5000

/**
 * A bin pops in and flicks its lid while a ring counts the undo window down.
 *
 * The ring is the point: it is the only thing on screen that says the offer to
 * undo is finite, and how much of it is left. [onTimeout] fires when it is
 * empty — that is the caller's cue to stop offering and move on.
 *
 * Change [key] to start again; a second delete while the first is still
 * counting restarts the window rather than inheriting what is left of it.
 */
@Composable
fun DeletedUndo(
    modifier: Modifier = Modifier.size(40.dp),
    undoMillis: Int = UNDO_WINDOW_MILLIS,
    colors: BrandColors = rememberBrandColors(),
    key: Any? = Unit,
    onTimeout: () -> Unit = {},
) {
    val timedOut by rememberUpdatedState(onTimeout)
    val total = INTRO_SECONDS + undoMillis / 1000f
    val t = remember(key) { Animatable(0f) }

    LaunchedEffect(key) {
        t.snapTo(0f)
        // Linear on purpose: the ring is a clock. Easing it would make the
        // time left look like more or less than it is.
        t.animateTo(total, tween((total * 1000).toInt(), easing = LinearEasing))
        timedOut()
    }

    Canvas(modifier.aspectRatio(1f)) {
        val k = size.minDimension / 140f // design space 140 x 140, centre 70,70
        scale(k, k, pivot = Offset.Zero) { drawUndo(t.value, total, colors.line, colors.hub) }
    }
}

/** The bin pops in, the ring draws on, the lid flicks — all inside this. */
private const val INTRO_SECONDS = .6f

private val BIN_BODY = PathParser()
    .parsePathString("M86,96 L154,96 L148,163 Q147.5,168 143,168 L97,168 Q92.5,168 92,163 Z")
    .toPath()

private fun cl(x: Float) = x.coerceIn(0f, 1f)
private fun sg(s: Float, a: Float, b: Float) = cl((s - a) / (b - a))
private fun eo(x: Float) = 1f - (1f - x).pow(3)

private fun ob(x: Float): Float {
    val c = 1.7f
    val y = x - 1f
    return 1f + (c + 1f) * y * y * y + c * y * y
}

private fun DrawScope.drawUndo(s: Float, total: Float, line: Color, hub: Color) {
    val c = Offset(70f, 70f)
    val r = 58f
    val w = 7f

    drawCircle(line.copy(alpha = .2f), r, c, style = Stroke(w))
    // Draws on over the intro, then empties clockwise over the window. The
    // two are multiplied rather than sequenced so the ring never jumps
    // between them.
    val vis = eo(sg(s, .15f, .5f)) * (1f - sg(s, INTRO_SECONDS, total))
    if (vis > 0.001f) {
        drawArc(
            line,
            -90f + 360f * (1f - vis),
            360f * vis,
            false,
            Offset(c.x - r, c.y - r),
            Size(r * 2, r * 2),
            style = Stroke(w, cap = StrokeCap.Round),
        )
    }

    val sc = .62f * ob(sg(s, 0f, .35f))
    if (sc <= 0f) return
    withTransform({ translate(c.x, c.y + 2f); scale(sc, sc, Offset.Zero); translate(-120f, -126f) }) {
        drawPath(BIN_BODY, line)
        for (x in floatArrayOf(106f, 120f, 134f)) {
            drawLine(hub.copy(alpha = .35f), Offset(x, 110f), Offset(x, 154f), 4f, StrokeCap.Round)
        }
        // One flick, out and back: a lid that stayed open would read as a bin
        // still waiting for something.
        rotate(-45f * sin(PI.toFloat() * sg(s, .3f, .7f)), pivot = Offset(80f, 89f)) {
            drawRoundRect(line, Offset(80f, 84f), Size(80f, 10f), CornerRadius(5f))
            drawRoundRect(line, Offset(108f, 76f), Size(24f, 10f), CornerRadius(4f), style = Stroke(4f))
        }
    }
}
