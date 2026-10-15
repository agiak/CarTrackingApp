package com.agcoding.cartrackingapp.shared.ui.tokens

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Figures.
 *
 * The redesign treats numbers as a separate voice from text, and for a good
 * reason rather than a decorative one: almost every number in this app sits in
 * a column, a legend or a tile that updates in place. Proportional digits make
 * those jump sideways as the value changes — a total ticking from 988 to 1.012
 * shifts the decimal point and the eye has to find it again.
 *
 * The design asks for IBM Plex Mono. That would mean shipping a font file,
 * which is not something to add on the way past, so these use the platform's
 * monospace family and ask for tabular figures on top. Swapping in Plex later
 * means changing [NumberFamily] and nothing else.
 */
private val NumberFamily = FontFamily.Monospace

/** Belt and braces: harmless on a monospace face, load-bearing if one is swapped in. */
private const val TabularFigures = "tnum"

object AppNumbers {

    /** The one number a screen is about: a hero card's total. */
    val hero = TextStyle(
        fontFamily = NumberFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 38.sp,
        lineHeight = 44.sp,
        letterSpacing = (-1.33).sp,
        fontFeatureSettings = TabularFigures,
    )

    /**
     * The cents after a hero number, set smaller and quieter: "154.611" at
     * full size with ",52 €" trailing it, so the figure that matters reads
     * first.
     */
    val heroFraction = TextStyle(
        fontFamily = NumberFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp,
        letterSpacing = (-0.4).sp,
        fontFeatureSettings = TabularFigures,
    )

    /** The total on the primary-coloured month summary, the largest in the app. */
    val summary = TextStyle(
        fontFamily = NumberFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 40.sp,
        lineHeight = 46.sp,
        letterSpacing = (-1.4).sp,
        fontFeatureSettings = TabularFigures,
    )

    /** Centre of the donut. */
    val donut = TextStyle(
        fontFamily = NumberFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 30.sp,
        letterSpacing = (-0.5).sp,
        fontFeatureSettings = TabularFigures,
    )

    /** A KPI cell, and the day number on a transaction's date tile. */
    val tile = TextStyle(
        fontFamily = NumberFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 21.sp,
        lineHeight = 26.sp,
        letterSpacing = (-0.3).sp,
        fontFeatureSettings = TabularFigures,
    )

    /** A row's amount: the right-hand column of a transaction or legend. */
    val value = TextStyle(
        fontFamily = NumberFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp,
        fontFeatureSettings = TabularFigures,
    )

    /** Legend amounts and the index in a ranked list ("01", "02"). */
    val compact = TextStyle(
        fontFamily = NumberFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.sp,
        fontFeatureSettings = TabularFigures,
    )

    /** The pills under a refill: "69,9 L", "700 km", "10,0 L/100". */
    val tag = TextStyle(
        fontFamily = NumberFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        letterSpacing = 0.sp,
        fontFeatureSettings = TabularFigures,
    )

    /** A licence plate, which is lettering rather than a quantity. */
    val plate = TextStyle(
        fontFamily = NumberFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 11.5.sp,
        lineHeight = 15.sp,
        letterSpacing = 0.69.sp,
        fontFeatureSettings = TabularFigures,
    )
}
