package com.agcoding.cartrackingapp.shared.ui.brand

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppColorScheme
import com.agcoding.cartrackingapp.shared.ui.tokens.readableContent

/**
 * What the brand animations — the splash, the road loader, the save and error
 * confirmations — are drawn in.
 *
 * Every value comes from the palette the user picked. There are no fixed
 * brand hexes here on purpose: a design kit arrives in one colour, and if that
 * colour is baked in, the one screen the app shows before anything else is the
 * one screen that ignores the choice the user made.
 *
 * The palette picker's SYSTEM entry needs no special case. `resolveAppColorScheme`
 * already turns it into the device's own dynamic colours, so "follows the
 * palette" and "follows the system colours when no palette is set" are the
 * same code path.
 */
data class BrandColors(
    /** The drawing itself: the car, the tank, the wallet. */
    val line: Color,
    /** The far end of the car's vertical gradient. */
    val lineShade: Color,
    /**
     * Negative space — wheel hubs, the eye, the tick cut out of a badge.
     *
     * It is the surface the animation sits on rather than a colour of its own,
     * so these read as holes in the drawing instead of as pale paint on top
     * of it.
     */
    val hub: Color,
    val errorBackground: Color,
    val errorContent: Color,
)

/**
 * [BrandColors] for an animation drawn on [surface].
 *
 * [surface] has to be the colour actually behind the animation — a card for a
 * bottom sheet, the background for a full screen — because that is what the
 * negative space is painted in.
 */
@Composable
fun rememberBrandColors(
    surface: Color = LocalAppColorScheme.current.backgroundCard,
): BrandColors {
    val scheme = LocalAppColorScheme.current
    return remember(scheme, surface) {
        BrandColors(
            line = scheme.actionPrimary,
            // A shade, not a second colour: far enough to give the body some
            // roundness, near enough that no palette ends up two-tone.
            lineShade = lerp(scheme.actionPrimary, surface, 0.18f),
            hub = surface,
            errorBackground = scheme.statusError,
            errorContent = scheme.statusError.readableContent(),
        )
    }
}
