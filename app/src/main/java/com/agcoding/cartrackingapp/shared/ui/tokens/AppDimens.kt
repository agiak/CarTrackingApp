package com.agcoding.cartrackingapp.shared.ui.tokens

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class AppDimens(
    val spacing: Spacing = Spacing(),
    val elevation: Elevation = Elevation(),
    val radius: Radius = Radius(),
    val size: Size = Size(),
) {
    data class Spacing(
        val xs: Dp  = 4.dp,
        val sm: Dp  = 8.dp,
        val md: Dp  = 16.dp,
        val lg: Dp  = 24.dp,
        val xl: Dp  = 32.dp,
        val xxl: Dp = 48.dp,

        /** Left and right margin of every screen in the redesign. */
        val screenHorizontal: Dp = 18.dp,

        /** Between two stacked cards. */
        val cardGap: Dp = 12.dp,

        /** Inside a card. */
        val cardPadding: Dp = 18.dp,

        /** Above a section header, measured from the card before it. */
        val sectionTop: Dp = 26.dp,

        /**
         * Bottom padding of a scrolling list that sits under an extended FAB,
         * so the last card is never trapped behind it.
         */
        val fabSafeBottom: Dp = 180.dp,
    )

    data class Elevation(
        val card: Dp   = 2.dp,
        val dialog: Dp = 8.dp,
        val sheet: Dp  = 16.dp,
    )

    /**
     * Corner radii. The redesign uses radius rather than elevation to separate
     * surfaces — cards are flat tonal shapes, so the only thing distinguishing
     * a hero from a tile from a chip is how round it is.
     */
    data class Radius(
        val chip: Dp        = 16.dp,
        val button: Dp      = 12.dp,
        val iconTile: Dp    = 12.dp,
        val tile: Dp        = 20.dp,
        val groupedList: Dp = 22.dp,
        val card: Dp        = 24.dp,
        val hero: Dp        = 28.dp,
        val sheet: Dp       = 30.dp,
        val fab: Dp         = 18.dp,
        val plate: Dp       = 5.dp,
    )

    /** Fixed sizes that recur across screens and must not drift apart. */
    data class Size(
        /** Square icon container inside a grouped-list row. */
        val iconTile: Dp = 38.dp,

        /** Circular icon button in a screen header. */
        val headerAction: Dp = 40.dp,

        val chipHeight: Dp = 32.dp,

        /** Hairline between rows of a grouped list, and between KPI cells. */
        val divider: Dp = 1.dp,

        /** The three-way fuel / service / other bar on a card. */
        val costBarHeight: Dp = 14.dp,

        /** The same bar inside the amber month summary, where it is secondary. */
        val costBarHeightCompact: Dp = 8.dp,
    )
}
