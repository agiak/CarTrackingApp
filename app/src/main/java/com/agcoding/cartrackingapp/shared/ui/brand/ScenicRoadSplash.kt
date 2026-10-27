package com.agcoding.cartrackingapp.shared.ui.brand

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.em
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppColorScheme
import kotlin.math.PI
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * The opening animation: the car holds the centre of the screen while the road
 * and two ranges of hills rush past it and slow to a stop, the wordmark fades
 * up, and then it drives off.
 *
 * The parallax is what carries it. The car never moves during the first two
 * beats — the world does, at three different speeds — which reads as travel in
 * a way a car crossing a static screen does not.
 *
 * Painted in the chosen palette, like everything else the app draws. The field
 * it sits on cannot be: the system splash's background is a theme attribute
 * the framework reads before any of the app's code runs. So the scene opens on
 * that fixed colour and fades into the palette's own over the first third of a
 * second — a transition rather than the jump a hard cut would make.
 */
@Composable
fun ScenicRoadSplash(
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
    loop: Boolean = false,
) {
    val finished by rememberUpdatedState(onFinished)
    val scheme = LocalAppColorScheme.current
    val density = LocalDensity.current
    val time = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        do {
            time.snapTo(0f)
            time.animateTo(END, tween(durationMillis = (END * 1000).toInt(), easing = LinearEasing))
        } while (loop)
        finished()
    }

    val palette = remember(scheme) {
        // The field, as the design kit builds it: the brand colour lifted
        // towards white at the top and dropped towards black at the bottom.
        // For Ocean Teal that lands on the #7bc0b7 / #1d7269 of the app icon,
        // and every other palette gets the same treatment.
        val fieldTop = lerp(scheme.actionPrimary, Color.White, 0.28f)
        val fieldBottom = lerp(scheme.actionPrimary, Color.Black, 0.34f)
        val fieldMid = lerp(fieldTop, fieldBottom, 0.5f)
        // Not actionContent. That is what the palette puts on a button, and it
        // is chosen by a 4.5:1 text rule — on Ocean Teal it comes out dark ink,
        // which would make the car on the splash the one place in the brand
        // where the caribou is not light. This is a single large drawing with
        // thick strokes, legible to about 2:1, which crosses over at a field
        // luminance of 0.5. The launcher icon's glyph is picked the same way.
        val ink = if (fieldMid.luminance() > 0.5f) Color(0xFF1A1C1E) else Color.White
        ScenicPalette(
            fieldTop = fieldTop,
            fieldBottom = fieldBottom,
            fieldMid = fieldMid,
            glyph = ink,
            glyphShade = lerp(ink, fieldMid, 0.18f),
        )
    }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            // The colour the system splash left behind; the scene fades up out
            // of it rather than replacing it in one frame.
            .background(scheme.backgroundPrimary)
    ) {
        val geometry = remember(constraints.maxWidth, constraints.maxHeight) {
            SceneGeometry(constraints.maxWidth.toFloat(), constraints.maxHeight.toFloat())
        }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = sceneFade(time.value) }
        ) {
            drawScenicRoad(time.value, geometry, palette)
        }

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset { IntOffset(0, (geometry.road + 108f * geometry.k).roundToInt()) }
                .graphicsLayer {
                    val shown = wordmarkFade(time.value)
                    alpha = shown * sceneFade(time.value)
                    translationY = (1f - shown) * 24f * geometry.k
                },
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.splash_wordmark),
                color = palette.glyph,
                // Sized from the screen's width rather than from the type
                // scale: a wordmark is a picture of a word, and it should hold
                // its proportion to the car above it on any display.
                fontSize = with(density) { (112f * geometry.k).toSp() },
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.03f).em,
                textAlign = TextAlign.Center,
            )
            Text(
                text = stringResource(R.string.splash_tagline),
                color = palette.glyph.copy(alpha = 0.75f),
                fontSize = with(density) { (38f * geometry.k).toSp() },
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
            )
        }
    }
}

/** The brand colours this scene needs, all derived from the chosen palette. */
private data class ScenicPalette(
    val fieldTop: Color,
    val fieldBottom: Color,
    /** What is behind the car, and so what its wheel hubs are cut out of. */
    val fieldMid: Color,
    val glyph: Color,
    val glyphShade: Color,
)

/**
 * Where everything sits, for a canvas of [w] by [h].
 *
 * The kit lays the scene out in a 1080-wide space, so [k] converts to pixels.
 * The vertical anchor is a fraction of the height rather than 1920 scaled the
 * same way, because a phone that is not 9:16 should still put the car on the
 * same line across the screen.
 */
private class SceneGeometry(val w: Float, val h: Float) {
    val k = w / 1080f
    val carW = 670f * k
    val carH = carW * CaribooCar.HEIGHT / CaribooCar.WIDTH
    val cy = h * 0.458f
    val road = cy + carH / 2f + 12f * k
}

// The three beats, in seconds: the world slows to a stop, it idles, it leaves.
private const val ARRIVE = 0f
private const val IDLE = 1.3f
private const val DEPART = 2.3f
private const val END = 3.2f

private val glide = CubicBezierEasing(0.33f, 1f, 0.68f, 1f)     // easeOutCubic
private val launch = CubicBezierEasing(0.32f, 0f, 0.67f, 0f)    // easeInCubic
private val settle = CubicBezierEasing(0.34f, 1.56f, 0.64f, 1f) // easeOutBack

private fun seg(t: Float, from: Float, to: Float, start: Float, end: Float, easing: Easing) =
    from + (to - from) * easing.transform(((t - start) / (end - start)).coerceIn(0f, 1f))

/** The hand-off from the system splash's fixed colour into the palette's own. */
private fun sceneFade(t: Float) = seg(t, 0f, 1f, 0f, 0.35f, glide)

/** The wordmark arrives once the world has stopped and leaves with the car. */
private fun wordmarkFade(t: Float): Float = if (t < DEPART) {
    seg(t, 0f, 1f, IDLE - .05f, IDLE + .45f, glide)
} else {
    seg(t, 1f, 0f, DEPART + .35f, END - .05f, launch)
}

/** How far the world has travelled past the car, in design units. */
private fun worldOffset(t: Float) = seg(t, 0f, 1150f, ARRIVE, IDLE, glide)

/** The car's own position, which only moves once it is leaving. */
private fun carOffset(t: Float): Float = when {
    t < DEPART -> 0f
    t < DEPART + .28f -> seg(t, 0f, -42f, DEPART, DEPART + .28f, settle)
    else -> seg(t, -42f, 1500f, DEPART + .28f, END, launch)
}

/**
 * Two ranges of hills, the far one slower and fainter than the near one.
 *
 * Each is the sum of two sines so that the ridge does not repeat on an
 * obvious period, and each path is drawn two screens wide so that scrolling it
 * by up to one screen never exposes its end.
 */
private class HillLayer(
    val k1: Float,
    val k2: Float,
    val amplitude: Float,
    val baseAbove: Float,
    val factor: Float,
    val alpha: Float,
    val phase: Float,
)

private val HILLS = listOf(
    HillLayer(k1 = 2f, k2 = 5f, amplitude = 260f, baseAbove = 60f, factor = .25f, alpha = .12f, phase = 1.3f),
    HillLayer(k1 = 3f, k2 = 7f, amplitude = 180f, baseAbove = 10f, factor = .55f, alpha = .2f, phase = .4f),
)

private fun hillPath(layer: HillLayer, g: SceneGeometry): Path {
    val base = g.road - layer.baseAbove * g.k
    val amplitude = layer.amplitude * g.k
    val step = 24f * g.k
    return Path().apply {
        moveTo(0f, g.road)
        var x = 0f
        while (x <= g.w * 2f) {
            val a = 2f * PI.toFloat() * layer.k1 * x / g.w + layer.phase
            val b = 2f * PI.toFloat() * layer.k2 * x / g.w + layer.phase * 2f
            lineTo(x, base - amplitude * (.62f * sin(a) + .38f * sin(b) + 1f) / 2f)
            x += step
        }
        lineTo(g.w * 2f, g.road)
        close()
    }
}

private fun DrawScope.drawScenicRoad(t: Float, g: SceneGeometry, palette: ScenicPalette) {
    // 160deg in the kit's CSS: mostly down the screen, leaning right.
    drawRect(
        Brush.linearGradient(
            listOf(palette.fieldTop, palette.fieldBottom),
            start = Offset(0f, 0f),
            end = Offset(g.w * 0.36f, g.h),
        )
    )
    drawRect(
        Brush.radialGradient(
            listOf(Color.White.copy(alpha = 0.22f), Color.Transparent),
            center = Offset(g.w * 0.3f, g.h * 0.12f),
            radius = maxOf(g.w, g.h) * 0.6f,
        )
    )

    val off = worldOffset(t) * g.k

    HILLS.forEach { layer ->
        val shift = -((off * layer.factor) % g.w)
        translate(left = shift, top = 0f) {
            drawPath(hillPath(layer, g), palette.glyph.copy(alpha = layer.alpha))
        }
    }

    // The centre line, scrolling with the near hills.
    val dashGap = 240f * g.k
    translate(left = -(off % dashGap), top = 0f) {
        repeat(7) { i ->
            drawRoundRect(
                palette.glyph.copy(alpha = .3f),
                Offset(i * dashGap, g.road + 40f * g.k),
                Size(110f * g.k, 10f * g.k),
                CornerRadius(5f * g.k),
            )
        }
    }

    drawLine(
        palette.glyph.copy(alpha = .22f),
        Offset(0f, g.road),
        Offset(g.w, g.road),
        4f * g.k,
        StrokeCap.Round,
    )

    // Design-x 48 is the back wheel; the car is 200 units wide.
    val rear = (g.w - g.carW) / 2f + 48f * g.carW / CaribooCar.WIDTH
    drawDust(t, IDLE - .12f, rear, g, palette)
    drawDust(t, DEPART + .26f, rear, g, palette)

    val carX = carOffset(t) * g.k
    // The wheels turn on the distance covered, whether that distance was the
    // world moving or the car: at the hand-over between the two it is the same
    // number, so they never jump.
    val wheelRadius = 12.5f * g.carW / CaribooCar.WIDTH
    val wheel = (off + carX) / wheelRadius * 180f / PI.toFloat()

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

    drawCaribooCar(
        topLeft = Offset((g.w - g.carW) / 2f + carX, g.cy - g.carH / 2f),
        widthPx = g.carW,
        glyph = palette.glyph,
        shade = palette.glyphShade,
        hub = palette.fieldMid,
        wheelDeg = wheel,
        tiltDeg = tilt,
        swayDeg = sway,
    )
}

/** Three puffs off the back wheel, when it stops and again when it pulls away. */
private fun DrawScope.drawDust(t: Float, at: Float, x0: Float, g: SceneGeometry, palette: ScenicPalette) {
    val k = (t - at) / .55f
    if (k < 0f || k > 1f) return
    val e = glide.transform(k)
    repeat(3) { i ->
        val diameter = (34f + i * 10f) * g.k * (.5f + e)
        val centre = Offset(
            x0 - (20f + i * 34f) * g.k * e,
            g.cy + g.carH * .46f - i * 8f * g.k - e * 24f * g.k,
        )
        drawCircle(palette.glyph.copy(alpha = (1f - k) * .35f), diameter / 2f, centre)
    }
}

@Preview(showBackground = true, widthDp = 380, heightDp = 760)
@Composable
private fun ScenicRoadSplashPreview() {
    CarTrackingAppTheme(darkTheme = true) {
        ScenicRoadSplash(onFinished = {}, loop = true)
    }
}
