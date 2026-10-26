package com.agcoding.cartrackingapp.shared.ui.brand

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.vector.PathParser

/** The design space the paths below are drawn in. */
object CaribooCar {
    const val WIDTH = 200f
    const val HEIGHT = 115f
}

private fun path(d: String): Path = PathParser().parsePathString(d).toPath()

// Convertible seen from the side, facing right, with the caribou driving it.
// Design space 200 x 115, origin y = -10 — the antlers reach above the top of
// the box, which is why [drawCaribooCar] shifts everything down by 10 before
// scaling.
private val antlers = path(
    "M58,45.5 C51,41 43,36 41,27 C39.5,20 43,14.5 50,12.5 M50,12.5 C51.5,9.5 54,8 57,7.5 " +
        "M46,15 C45,11.5 45.5,8.5 47.5,6.5 M50,12.5 C53.5,12.5 56.5,13.8 58.5,16 " +
        "M50.5,39.5 C54.5,35.5 59.5,33.5 65,33.5 M58.5,33.9 C60,31 62.5,29.2 65.5,28.5 " +
        "M41.8,30.5 C38,30 35,31 32.5,33"
)
private val ears = path(
    "M58.5,44.2 C61,41.8 64.5,40.9 68,41.4 C66.6,42.6 65,43.6 63.6,43.7 " +
        "C65.2,44.3 66.4,44 67.5,43.6 C65.6,45.6 62,46.4 58.9,45.6Z " +
        "M53.5,48.5 C50,45.6 46,44.8 42.5,45.8 C45.5,48.8 49.8,50.2 53.6,49.8Z"
)
private val head = path(
    "M40,84 C40,72 42,62 48,54 C52,48.5 57,45.5 62,45.5 C66,45.5 70.5,48.5 75,52.6 " +
        "C78,55.3 78.8,58.3 76.6,59.8 C74.4,61.3 70.6,61 67.2,60.6 C64.4,60.3 62.4,61.6 61.6,64 " +
        "C63.5,66 62.4,68 61.2,68.6 C62.8,70.6 61.8,72.8 60.4,73.4 C61.8,75.6 60.8,78 59.4,78.6 " +
        "C60.2,80.4 60.2,82.4 60,84Z"
)
private val eye = path("M64.6,51 C65.9,50.1 67.4,50.1 68.5,51 C67.4,52 65.9,52 64.6,51Z")
private val body = path(
    "M16,84 C16,76 22,72 30,71 L94,68.5 L103,68.5 L150,70 C164,71 176,76 180,84 L181,90 " +
        "C181,93 179,95 176,95 H22 C18,95 16,93 16,90Z"
)

/**
 * Draws the car so that it is [widthPx] wide with its top-left at [topLeft].
 *
 * A top-level extension rather than a member of [CaribooCar] so that it can be
 * called from any `DrawScope` with a plain import.
 *
 * @param wheelDeg wheel rotation.
 * @param tiltDeg body pitch; positive is nose down, which is what braking looks like.
 * @param swayDeg head and antler sway, pivoting at the driver's shoulders.
 */
fun DrawScope.drawCaribooCar(
    topLeft: Offset,
    widthPx: Float,
    glyph: Color,
    shade: Color,
    hub: Color,
    wheelDeg: Float = 0f,
    tiltDeg: Float = 0f,
    swayDeg: Float = 0f,
) {
    val s = widthPx / CaribooCar.WIDTH
    val fill = Brush.verticalGradient(listOf(glyph, shade), startY = -10f, endY = 105f)
    translate(topLeft.x, topLeft.y + 10f * s) {
        scale(s, s, pivot = Offset.Zero) {
            rotate(tiltDeg, pivot = Offset(100f, 95f)) {
                rotate(swayDeg, pivot = Offset(66f, 78f)) {
                    withTransform({ translate(13.3f, -10.7f); scale(1.1f, 1.1f, Offset.Zero) }) {
                        drawPath(antlers, fill, style = Stroke(3.2f, cap = StrokeCap.Round))
                        drawPath(ears, fill)
                        drawPath(head, fill)
                        drawPath(eye, hub)
                    }
                }
                drawPath(body, fill)
                drawLine(glyph, Offset(98f, 68.5f), Offset(110f, 53f), 3f, StrokeCap.Round)
                drawCircle(hub, 2.6f, Offset(176f, 80f))
            }
            // Outside the tilt: the wheels stay on the road while the body pitches.
            for (cx in floatArrayOf(48f, 150f)) drawWheel(Offset(cx, 95f), wheelDeg, glyph, hub)
        }
    }
}

private fun DrawScope.drawWheel(centre: Offset, degrees: Float, line: Color, hub: Color) {
    drawCircle(hub, 12.5f, centre)
    drawCircle(line, 12.5f, centre, style = Stroke(4.2f))
    drawCircle(line, 3.2f, centre)
    // Two spokes are enough to read as rotation and cheap enough to draw every
    // frame; more of them turn into a grey disc once the wheel is moving.
    rotate(degrees, pivot = centre) {
        drawLine(line, centre + Offset(0f, -8f), centre + Offset(0f, 8f), 1.8f, StrokeCap.Round)
        drawLine(line, centre + Offset(-8f, 0f), centre + Offset(8f, 0f), 1.8f, StrokeCap.Round)
    }
}
