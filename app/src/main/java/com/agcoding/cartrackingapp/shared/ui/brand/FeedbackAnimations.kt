package com.agcoding.cartrackingapp.shared.ui.brand

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sin

/*
 * The save and sign-up confirmations, and what they look like when they fail.
 *
 * Each one plays once and then calls `onFinished`; pass a [replayKey] that
 * changes — a save counter, the error message — to play it again. All of them
 * are drawn in [BrandColors], so they follow the chosen palette rather than
 * the colour the design kit happened to arrive in.
 */

private const val PIf = PI.toFloat()

private fun cl(x: Float) = x.coerceIn(0f, 1f)

/** Where [s] sits in the [a]..[b] slice of the timeline, as 0..1. */
private fun sg(s: Float, a: Float, b: Float) = cl((s - a) / (b - a))

private fun eo(x: Float) = 1f - (1f - x).pow(3)      // easeOutCubic
private fun ei(x: Float) = x * x                      // easeInQuad

/** easeOutBack: overshoots and comes back, which is what a badge landing looks like. */
private fun ob(x: Float): Float {
    val c = 1.7f
    val y = x - 1f
    return 1f + (c + 1f) * y * y * y + c * y * y
}

/** A decaying shake over the [a]..[b] slice, for the error badges. */
private fun shk(s: Float, a: Float, b: Float, amp: Float): Float {
    val p = sg(s, a, b)
    return if (p > 0f && p < 1f) amp * sin(p * PIf * 6f) * (1f - p) else 0f
}

/** Deterministic scatter: the confetti has to look random, not be random. */
private fun rnd(i: Int, k: Int): Float {
    val x = sin(i * 127.1 + k * 311.7) * 43758.5453
    return (x - floor(x)).toFloat()
}

private fun svg(d: String): Path = PathParser().parsePathString(d).toPath()

private fun poly(vararg xy: Float) = Path().apply {
    moveTo(xy[0], xy[1])
    var i = 2
    while (i < xy.size) {
        lineTo(xy[i], xy[i + 1]); i += 2
    }
}

/**
 * Drives one run of an animation.
 *
 * [onFinished] is read through [rememberUpdatedState] so that a recomposition
 * with a new lambda does not restart the run, and the timeline is keyed on
 * [replayKey] so that changing it does.
 */
@Composable
private fun rememberTimeline(seconds: Float, replayKey: Any?, onFinished: () -> Unit): State<Float> {
    val finished by rememberUpdatedState(onFinished)
    val t = remember(replayKey) { Animatable(0f) }
    LaunchedEffect(replayKey) {
        t.snapTo(0f)
        t.animateTo(seconds, tween((seconds * 1000).toInt(), easing = LinearEasing))
        finished()
    }
    return t.asState()
}

/** A canvas in design units, scaled to whatever width the caller gives it. */
@Composable
private fun FeedbackCanvas(modifier: Modifier, vbW: Float, vbH: Float, draw: DrawScope.() -> Unit) {
    Canvas(modifier.aspectRatio(vbW / vbH)) {
        val k = size.width / vbW
        scale(k, k, pivot = Offset.Zero) { draw() }
    }
}

private val DROP = svg("M0,-16 C7,-6 11,0 11,6 A11,11 0 0 1 -11,6 C-11,0 -7,-6 0,-16Z")
private val EURO = svg("M8,-8.5 A11.7,11.7 0 1 0 8,8.5 M-12,-3 H2 M-12,3 H2")
private val ANTLERS = svg(
    "M58,45.5 C51,41 43,36 41,27 C39.5,20 43,14.5 50,12.5 M50,12.5 C51.5,9.5 54,8 57,7.5 " +
        "M46,15 C45,11.5 45.5,8.5 47.5,6.5 M50,12.5 C53.5,12.5 56.5,13.8 58.5,16 " +
        "M50.5,39.5 C54.5,35.5 59.5,33.5 65,33.5 M58.5,33.9 C60,31 62.5,29.2 65.5,28.5 " +
        "M41.8,30.5 C38,30 35,31 32.5,33"
)
private val HEAD = svg(
    "M58.5,44.2 C61,41.8 64.5,40.9 68,41.4 C66.6,42.6 65,43.6 63.6,43.7 C65.2,44.3 66.4,44 67.5,43.6 " +
        "C65.6,45.6 62,46.4 58.9,45.6Z M53.5,48.5 C50,45.6 46,44.8 42.5,45.8 C45.5,48.8 49.8,50.2 53.6,49.8Z " +
        "M40,84 C40,72 42,62 48,54 C52,48.5 57,45.5 62,45.5 C66,45.5 70.5,48.5 75,52.6 C78,55.3 78.8,58.3 76.6,59.8 " +
        "C74.4,61.3 70.6,61 67.2,60.6 C64.4,60.3 62.4,61.6 61.6,64 C63.5,66 62.4,68 61.2,68.6 C62.8,70.6 61.8,72.8 60.4,73.4 " +
        "C61.8,75.6 60.8,78 59.4,78.6 C60.2,80.4 60.2,82.4 60,84Z"
)
private val EYE = svg("M64.6,51 C65.9,50.1 67.4,50.1 68.5,51 C67.4,52 65.9,52 64.6,51Z")

/** Draws the first [p] of [path], so a stroke can write itself on. */
private fun DrawScope.strokePart(path: Path, p: Float, c: Color, w: Float) {
    if (p <= 0f) return
    val measure = PathMeasure().apply { setPath(path, false) }
    val out = Path()
    measure.getSegment(0f, measure.length * cl(p), out, true)
    drawPath(out, c, style = Stroke(w, cap = StrokeCap.Round, join = StrokeJoin.Round))
}

/** The spokes that fly outward when something lands. */
private fun DrawScope.burst(s: Float, st: Float, c: Offset, r0: Float, r1: Float, n: Int, color: Color) {
    val p = sg(s, st, st + .5f)
    if (p <= 0f || p >= 1f) return
    repeat(n) { i ->
        val a = i.toFloat() / n * 2f * PIf - PIf / 2f + .2f
        val ri = r0 + (r1 - r0) * eo(p)
        val ro = ri + 12f * (1f - p)
        drawLine(
            color.copy(alpha = color.alpha * (1f - p)),
            c + Offset(ri * cos(a), ri * sin(a)),
            c + Offset(ro * cos(a), ro * sin(a)),
            4f,
            StrokeCap.Round,
        )
    }
}

private fun DrawScope.badge(
    c: Offset,
    sc: Float,
    r: Float,
    fill: Color,
    ring: Color,
    mark: DrawScope.() -> Unit,
) {
    if (sc <= 0f) return
    withTransform({ translate(c.x, c.y); scale(sc, sc, Offset.Zero) }) {
        drawCircle(fill, r, Offset.Zero)
        drawCircle(ring, r, Offset.Zero, style = Stroke(4f))
        mark()
    }
}

private fun DrawScope.exclamation(s: Float, at: Float, h: Float, w: Float, c: Color) {
    strokePart(poly(0f, -h, 0f, h * .3f), eo(sg(s, at, at + .18f)), c, w)
    val a = sg(s, at + .14f, at + .22f)
    if (a > 0f) drawCircle(c.copy(alpha = a), w * .62f, Offset(0f, h * .9f))
}

private fun DrawScope.cross(s: Float, at: Float, r: Float, w: Float, c: Color) {
    strokePart(poly(-r, -r, r, r), eo(sg(s, at, at + .18f)), c, w)
    strokePart(poly(r, -r, -r, r), eo(sg(s, at + .12f, at + .3f)), c, w)
}

/** Round tank with a wavy fuel level [level] 0..1, and the drop falling into it. */
private fun DrawScope.tank(s: Float, level: Float, amp: Float, col: BrandColors) {
    val cx = 120f
    val cy = 100f
    val y = 150f - 108f * level
    drawCircle(col.line.copy(alpha = .35f), 52f, Offset(cx, cy), style = Stroke(5f))
    clipPath(Path().apply { addOval(Rect(Offset(cx, cy), 47f)) }) {
        val liquid = Path().apply {
            moveTo(68f, y)
            var x = 68f
            while (x <= 172f) {
                lineTo(x, y + amp * sin(x / 12f + s * 9f)); x += 8f
            }
            lineTo(172f, 160f); lineTo(68f, 160f); close()
        }
        drawPath(liquid, col.line)
        // The ripple the drop leaves on the surface.
        val rp = sg(s, .45f, .95f)
        if (rp > 0f && rp < 1f) {
            val rr = 6f + 38f * eo(rp)
            drawOval(
                col.hub.copy(alpha = (1f - rp) * .8f),
                Offset(cx - rr, y - rr * .25f),
                Size(rr * 2f, rr * .5f),
                style = Stroke(3f),
            )
        }
    }
    if (s < .45f) {
        val f = sg(s, 0f, .45f)
        withTransform({
            translate(cx, -40f + 180f * ei(f)); scale(1f - .15f * f, 1f + .3f * f, Offset.Zero)
        }) { drawPath(DROP, col.line) }
    }
}

/** A € coin, squashed horizontally so that it reads as spinning. */
private fun DrawScope.coin(x: Float, y: Float, s: Float, col: BrandColors) {
    withTransform({ translate(x, y); scale(max(.12f, abs(cos(s * 12f))), 1f, Offset.Zero) }) {
        drawCircle(col.line, 20f, Offset.Zero)
        drawCircle(col.hub, 20f, Offset.Zero, style = Stroke(3f))
        drawPath(EURO, col.hub, style = Stroke(3.2f, cap = StrokeCap.Round))
    }
}

private fun DrawScope.walletBack(col: BrandColors, dx: Float) =
    drawRoundRect(col.line.copy(alpha = .45f), Offset(66f + dx, 66f), Size(108f, 44f), CornerRadius(10f))

private fun DrawScope.walletFront(col: BrandColors, dx: Float, sx: Float = 1f, sy: Float = 1f) {
    withTransform({ translate(dx, 0f); scale(sx, sy, Offset(120f, 166f)) }) {
        drawRoundRect(col.line, Offset(58f, 92f), Size(124f, 74f), CornerRadius(14f))
        drawRoundRect(col.hub.copy(alpha = .3f), Offset(148f, 114f), Size(34f, 26f), CornerRadius(9f))
        drawCircle(col.hub, 4.5f, Offset(162f, 127f))
    }
}

// ── saved ────────────────────────────────────────────────────────────────────

/** A drop falls in, the tank fills, then the tick. 1.9 s. */
@Composable
fun RefillSaved(
    modifier: Modifier = Modifier.width(200.dp),
    colors: BrandColors = rememberBrandColors(),
    replayKey: Any? = Unit,
    onFinished: () -> Unit = {},
) {
    val t = rememberTimeline(1.9f, replayKey, onFinished)
    FeedbackCanvas(modifier, 240f, 190f) {
        val s = t.value
        val sc = 1f + .06f * sin(PIf * sg(s, 1.2f, 1.5f))
        scale(sc, sc, pivot = Offset(120f, 100f)) {
            val l = eo(sg(s, .42f, 1.3f))
            // The wave flattens as the tank fills: a full tank is a still one.
            tank(s, l, 5f * (1f - l * .7f), colors)
            strokePart(poly(99f, 101f, 114f, 116f, 143f, 85f), eo(sg(s, 1.15f, 1.45f)), colors.hub, 9f)
            burst(s, 1.3f, Offset(120f, 100f), 62f, 86f, 8, colors.line)
        }
    }
}

/** A spinning coin drops into the wallet, which squashes, then the tick badge. 1.6 s. */
@Composable
fun ExpenseSaved(
    modifier: Modifier = Modifier.width(200.dp),
    colors: BrandColors = rememberBrandColors(),
    replayKey: Any? = Unit,
    onFinished: () -> Unit = {},
) {
    val t = rememberTimeline(1.6f, replayKey, onFinished)
    FeedbackCanvas(modifier, 240f, 190f) {
        val s = t.value
        walletBack(colors, 0f)
        if (s < .62f) coin(120f, -20f + 130f * ei(sg(s, 0f, .6f)), s, colors)
        val b = sin(PIf * sg(s, .58f, .85f))
        walletFront(colors, 0f, 1f + .04f * b, 1f - .07f * b)
        badge(Offset(178f, 90f), ob(sg(s, .8f, 1.1f)), 19f, colors.line, colors.hub) {
            strokePart(poly(-8f, 1f, -2f, 7f, 9f, -6f), eo(sg(s, .95f, 1.2f)), colors.hub, 4.5f)
        }
        burst(s, 1.05f, Offset(178f, 90f), 26f, 44f, 7, colors.line)
    }
}

private data class Confetti(
    val vx: Float,
    val vy: Float,
    val r0: Float,
    val w: Float,
    val a: Float,
    val hub: Boolean,
)

private val CONFETTI = List(18) { i ->
    Confetti(
        vx = (rnd(i, 1) - .5f) * 260f,
        vy = -90f - rnd(i, 2) * 150f,
        r0 = rnd(i, 3) * 180f,
        w = (rnd(i, 4) - .5f) * 900f,
        a = .5f + rnd(i, 5) * .5f,
        hub = i % 4 == 0,
    )
}

private fun DrawScope.avatar(s: Float, col: BrandColors, headDeg: Float) {
    drawCircle(col.line.copy(alpha = .22f), 58f * ob(sg(s, 0f, .4f)), Offset(120f, 96f))
    clipPath(Path().apply { addOval(Rect(Offset(120f, 96f), 58f)) }) {
        withTransform({
            translate(48.5f, 36.8f + 95f * (1f - eo(sg(s, .25f, .85f))))
            scale(1.3f, 1.3f, Offset.Zero)
            rotate(headDeg, Offset(55f, 84f))
        }) {
            drawPath(ANTLERS, col.line, style = Stroke(3.2f, cap = StrokeCap.Round))
            drawPath(HEAD, col.line)
            drawPath(EYE, col.hub)
        }
    }
}

/** The caribou rises into the avatar ring, verified badge, confetti. 2.6 s. */
@Composable
fun WelcomeAboard(
    modifier: Modifier = Modifier.width(200.dp),
    colors: BrandColors = rememberBrandColors(),
    replayKey: Any? = Unit,
    onFinished: () -> Unit = {},
) {
    val t = rememberTimeline(2.6f, replayKey, onFinished)
    FeedbackCanvas(modifier, 240f, 200f) {
        val s = t.value
        avatar(s, colors, 0f)
        val p = s - .95f
        if (p in 0f..1.6f) {
            CONFETTI.forEach { c ->
                val base = if (c.hub) colors.hub else colors.line
                withTransform({
                    // The squared term is gravity: thrown up, falling back.
                    translate(120f + c.vx * p, 96f + c.vy * p + 170f * p * p)
                    rotate(c.r0 + c.w * p, Offset.Zero)
                }) {
                    drawRoundRect(
                        base.copy(alpha = c.a * (1f - p / 1.6f)),
                        Offset(-3f, -5f),
                        Size(6f, 10f),
                        CornerRadius(1.5f),
                    )
                }
            }
        }
        badge(Offset(162f, 138f), ob(sg(s, .85f, 1.15f)), 18f, colors.line, colors.hub) {
            strokePart(poly(-8f, 0f, -2f, 6f, 9f, -6f), eo(sg(s, 1.05f, 1.3f)), colors.hub, 4.5f)
        }
    }
}

// ── not saved ────────────────────────────────────────────────────────────────

/** The tank starts to fill, then drains through two leaks; "!" badge. 2.4 s. */
@Composable
fun RefillNotSaved(
    modifier: Modifier = Modifier.width(200.dp),
    colors: BrandColors = rememberBrandColors(),
    replayKey: Any? = Unit,
    onFinished: () -> Unit = {},
) {
    val t = rememberTimeline(2.4f, replayKey, onFinished)
    FeedbackCanvas(modifier, 240f, 200f) {
        val s = t.value
        tank(s, eo(sg(s, .42f, 1f)) * .55f * (1f - eo(sg(s, 1.05f, 1.6f))), 7f, colors)
        for (i in 0..1) {
            val p = sg(s, 1f + i * .22f, 1.45f + i * .22f)
            if (p > 0f && p < 1f) {
                withTransform({
                    translate(116f + i * 8f, 152f + 40f * ei(p)); scale(.42f, .42f, Offset.Zero)
                }) { drawPath(DROP, colors.line.copy(alpha = 1f - p)) }
            }
        }
        badge(
            Offset(120f + shk(s, 1.85f, 2.3f, 8f), 100f),
            ob(sg(s, 1.45f, 1.72f)),
            30f,
            colors.errorBackground,
            colors.hub,
        ) { exclamation(s, 1.6f, 13f, 8f, colors.errorContent) }
    }
}

/** The coin bounces off the wallet rim and flies off; X badge. 1.6 s. */
@Composable
fun ExpenseNotSaved(
    modifier: Modifier = Modifier.width(200.dp),
    colors: BrandColors = rememberBrandColors(),
    replayKey: Any? = Unit,
    onFinished: () -> Unit = {},
) {
    val t = rememberTimeline(1.6f, replayKey, onFinished)
    FeedbackCanvas(modifier, 240f, 190f) {
        val s = t.value
        val dx = shk(s, .45f, .85f, 4f)
        val q = s - .45f
        walletBack(colors, dx)
        if (q < 0f) {
            coin(120f, -20f + 92f * ei(sg(s, 0f, .45f)), s, colors)
        } else if (q < 1f) {
            coin(120f + 150f * q, 72f - 150f * q + 260f * q * q, s, colors)
        }
        walletFront(colors, dx)
        badge(Offset(178f, 90f), ob(sg(s, .95f, 1.25f)), 19f, colors.errorBackground, colors.hub) {
            cross(s, 1.1f, 6f, 4.5f, colors.errorContent)
        }
    }
}
