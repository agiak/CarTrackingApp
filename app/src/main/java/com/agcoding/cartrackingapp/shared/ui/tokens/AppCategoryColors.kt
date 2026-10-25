package com.agcoding.cartrackingapp.shared.ui.tokens

import androidx.compose.ui.graphics.Color
import kotlin.math.abs

/**
 * The three spending categories as colors: fuel, service, everything else.
 *
 * They appear on the donut, the stacked cost bars, the legends and the dot on
 * a transaction's date tile, and they have to mean the same thing in all of
 * them — a user who learns that the wide band is fuel has learned it for the
 * whole app.
 *
 * The design hands these over as three fixed hues (amber / rose / lilac), but
 * fixed hues cannot be used here: the user picks their palette in Settings,
 * and a chart painted in somebody else's amber would be the one part of the
 * screen that ignores that choice. So only the *relationship* is taken from
 * the design — the angles between the three hues — and it is applied to
 * whatever primary the chosen palette provides. Fuel is the primary itself,
 * which is why the biggest share of every chart is always the accent colour
 * the user chose.
 *
 * Saturation and value come from the primary too, so a muted palette produces
 * three muted categories and a vivid one produces three vivid ones, rather
 * than two fixed pastels next to a colour that clashes with them.
 */
val AppColorScheme.categoryFuel: Color
    get() = actionPrimary

/** Rose in the reference palette: a touch backwards around the wheel. */
val AppColorScheme.categoryService: Color
    get() = actionPrimary.rotateHue(SERVICE_HUE_SHIFT)

/** Lilac in the reference palette: roughly opposite, and clearly neither. */
val AppColorScheme.categoryOther: Color
    get() = actionPrimary.rotateHue(OTHER_HUE_SHIFT)

/**
 * Measured from the reference palette so the three keep their spacing: its
 * amber sits at 33°, its rose at 347° and its lilac at 260°.
 */
private const val SERVICE_HUE_SHIFT = -45f
private const val OTHER_HUE_SHIFT = 227f

/**
 * [count] colours for a chart whose series are not a fixed set of categories —
 * the stations bar, where the bands are whatever places the user refuelled at.
 *
 * Built the same way as the three above, by turning the chosen primary around
 * the wheel, so a stations chart belongs to the user's palette exactly as the
 * cost breakdown does. The first band is the primary itself, which keeps the
 * biggest share the accent colour here too.
 *
 * The spread stops short of a full lap so the last colour cannot come back
 * round to look like the first.
 */
fun AppColorScheme.seriesColors(count: Int): List<Color> {
    if (count <= 0) return emptyList()
    if (count == 1) return listOf(actionPrimary)
    val step = SERIES_ARC / (count - 1)
    return List(count) { index -> actionPrimary.rotateHue(index * step) }
}

/** Short of 360° on purpose: see [seriesColors]. */
private const val SERIES_ARC = 280f

/**
 * Turns a colour around the hue wheel, keeping how saturated and how bright it
 * is. A grey has no hue to turn, so it is returned untouched rather than
 * becoming an arbitrary colour.
 */
internal fun Color.rotateHue(degrees: Float): Color {
    val max = maxOf(red, green, blue)
    val min = minOf(red, green, blue)
    val delta = max - min
    if (delta == 0f) return this

    val hue = when (max) {
        red -> 60f * (((green - blue) / delta) % 6f)
        green -> 60f * (((blue - red) / delta) + 2f)
        else -> 60f * (((red - green) / delta) + 4f)
    }
    // Two modulos: the first can leave a negative angle, and a negative hue
    // would send the conversion below into the wrong sector.
    val rotated = ((hue + degrees) % 360f + 360f) % 360f
    val saturation = if (max == 0f) 0f else delta / max

    return hsvToColor(rotated, saturation, max, alpha)
}

private fun hsvToColor(hue: Float, saturation: Float, value: Float, alpha: Float): Color {
    val chroma = value * saturation
    val second = chroma * (1f - abs((hue / 60f) % 2f - 1f))
    val base = value - chroma

    val (r, g, b) = when {
        hue < 60f -> Triple(chroma, second, 0f)
        hue < 120f -> Triple(second, chroma, 0f)
        hue < 180f -> Triple(0f, chroma, second)
        hue < 240f -> Triple(0f, second, chroma)
        hue < 300f -> Triple(second, 0f, chroma)
        else -> Triple(chroma, 0f, second)
    }
    return Color(r + base, g + base, b + base, alpha)
}
