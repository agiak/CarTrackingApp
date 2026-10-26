package com.agcoding.cartrackingapp.shared.ui.brand

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppColorScheme
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * The opening animation: the car drives in, brakes, the gauge fills from E to
 * F, and it drives off.
 *
 * It runs after the system splash, not instead of it — Android 12+ always
 * draws its own first, and `core-splashscreen` gives earlier versions the same
 * one.
 *
 * The car is painted in the chosen palette. The field behind it cannot be:
 * the system splash's background is a theme attribute the framework reads
 * before any of the app's code runs, so it is a fixed near-neutral that every
 * palette's own background sits within a couple of shades of (see
 * `@color/splash_background`). Drawing this on [surface] therefore hands over
 * from the window without a visible seam, and still follows the palette for
 * everything that carries colour.
 *
 * [onFinished] fires once, when the run ends. It is kept in a
 * [rememberUpdatedState] so that a recomposition with a new lambda does not
 * restart the animation.
 */
@Composable
fun FuelGaugeSplash(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
    surface: Color = LocalAppColorScheme.current.backgroundPrimary,
    colors: BrandColors = rememberBrandColors(surface = surface),
    loop: Boolean = false,
) {
    val finished by rememberUpdatedState(onFinished)
    val time = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        do {
            time.snapTo(0f)
            time.animateTo(END, tween(durationMillis = (END * 1000).toInt(), easing = LinearEasing))
        } while (loop)
        finished()
    }

    Box(modifier.fillMaxSize().background(surface)) {
        Canvas(Modifier.fillMaxSize()) { drawGaugeScene(time.value, colors) }
    }
}

// The four beats, in seconds. Arrive 1.3, idle 1.0, depart 0.9.
private const val ARRIVE = 0f
private const val IDLE = 1.3f
private const val DEPART = 2.3f
private const val END = 3.2f

/** Interpolates [from]..[to] over the [start]..[end] slice of the timeline. */
private fun seg(t: Float, from: Float, to: Float, start: Float, end: Float, easing: Easing) =
    from + (to - from) * easing.transform(((t - start) / (end - start)).coerceIn(0f, 1f))

private val glide = CubicBezierEasing(0.33f, 1f, 0.68f, 1f)     // easeOutCubic
private val launch = CubicBezierEasing(0.32f, 0f, 0.67f, 0f)    // easeInCubic
private val settle = CubicBezierEasing(0.34f, 1.56f, 0.64f, 1f) // easeOutBack

/** Horizontal offset from centre, in pixels, at time [t] on a [w]-wide canvas. */
private fun carX(t: Float, w: Float): Float {
    val k = w / 1080f
    return when {
        t < DEPART -> seg(t, -1150f, 0f, ARRIVE, IDLE, glide)
        // A short roll back before pulling away, so the departure reads as a
        // decision rather than as the animation being cut.
        t < DEPART + .28f -> seg(t, 0f, -42f, DEPART, DEPART + .28f, settle)
        else -> seg(t, -42f, 1500f, DEPART + .28f, END, launch)
    } * k
}

private fun DrawScope.drawGaugeScene(t: Float, colors: BrandColors) {
    val w = size.width
    val k = w / 1080f
    val carW = 670f * k
    val carH = carW * CaribooCar.HEIGHT / CaribooCar.WIDTH
    val cy = size.height * 0.458f
    val road = cy + carH / 2 + 12f * k
    val g = colors.line

    drawLine(g.copy(alpha = .22f), Offset(0f, road), Offset(w, road), 4f * k, StrokeCap.Round)

    // The gauge fades in behind the car and back out as it leaves, so the last
    // frame is the empty teal the app is about to replace.
    val op = if (t < DEPART) {
        seg(t, 0f, 1f, ARRIVE, ARRIVE + .5f, glide)
    } else {
        seg(t, 1f, 0f, DEPART + .3f, END, launch)
    }
    val lvl = if (t < DEPART) {
        seg(t, 0f, 1f, IDLE - .25f, DEPART - .1f, settle)
    } else {
        seg(t, 1f, .82f, DEPART + .2f, END, glide)
    }

    val r = 470f * k
    val c = Offset(w / 2, road - 4f * k)
    val topLeft = Offset(c.x - r, c.y - r)
    val arcSize = Size(2 * r, 2 * r)
    drawArc(g.copy(alpha = .22f * op), 180f, 180f, false, topLeft, arcSize, style = Stroke(18f * k, cap = StrokeCap.Round))
    if (lvl > .002f) {
        drawArc(g.copy(alpha = .8f * op), 180f, 180f * lvl, false, topLeft, arcSize, style = Stroke(18f * k, cap = StrokeCap.Round))
    }

    for (i in 0..4) {
        val a = PI + PI / 4 * i
        val outer = Offset((cos(a) * (r - 40f * k)).toFloat(), (sin(a) * (r - 40f * k)).toFloat())
        val inner = Offset((cos(a) * (r - 64f * k)).toFloat(), (sin(a) * (r - 64f * k)).toFloat())
        drawLine(g.copy(alpha = .6f * op), c + outer, c + inner, 6f * k, StrokeCap.Round)
    }

    val needle = PI + PI * lvl
    val tip = c + Offset((cos(needle) * (r - 95f * k)).toFloat(), (sin(needle) * (r - 95f * k)).toFloat())
    drawLine(g.copy(alpha = op), c, tip, 10f * k, StrokeCap.Round)
    drawCircle(g.copy(alpha = op), 9f * k, tip)

    val x = carX(t, w)
    val tilt = when {
        t < IDLE -> seg(t, 0f, 2.4f, IDLE - .5f, IDLE, glide)
        t < DEPART -> seg(t, 2.4f, 0f, IDLE, IDLE + .45f, settle)
        t < DEPART + .28f -> seg(t, 0f, -2f, DEPART, DEPART + .28f, settle)
        else -> seg(t, -2f, -3.5f, DEPART + .28f, END, launch)
    }
    val sway = when {
        t < IDLE - .3f -> seg(t, 0f, -3f, ARRIVE, IDLE - .3f, glide)
        t < IDLE + .15f -> seg(t, -3f, 6f, IDLE - .3f, IDLE + .15f, glide)
        t < DEPART -> seg(t, 6f, 0f, IDLE + .15f, IDLE + .8f, settle)
        else -> seg(t, 0f, -7f, DEPART + .15f, END, launch)
    }
    // Wheel rotation from the distance travelled, so the wheels never look
    // like they are turning independently of the car.
    val wheel = x / (12.5f * carW / CaribooCar.WIDTH) * 180f / PI.toFloat()

    drawCaribooCar(
        topLeft = Offset((w - carW) / 2 + x, cy - carH / 2),
        widthPx = carW,
        glyph = g,
        shade = colors.lineShade,
        hub = colors.hub,
        wheelDeg = wheel,
        tiltDeg = tilt,
        swayDeg = sway,
    )
}

@Preview(showBackground = true, widthDp = 380, heightDp = 760)
@Composable
private fun FuelGaugeSplashPreview() {
    CarTrackingAppTheme(darkTheme = true) {
        FuelGaugeSplash(onFinished = {}, loop = true)
    }
}
