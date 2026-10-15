package com.agcoding.cartrackingapp.presentation.onboarding

import androidx.annotation.StringRes
import com.agcoding.cartrackingapp.R

/**
 * Which theme colour a slide is drawn in.
 *
 * An enum rather than a literal colour: the slides used to be hard-coded
 * green/blue/orange/purple, which clashed with whatever palette the user had
 * chosen and looked like a different app's onboarding.
 */
enum class SlideAccent { PRIMARY, TERTIARY, SECONDARY }

/**
 * A single onboarding slide.
 *
 * [bulletRes] is where the actual instructions live. The description says what
 * the step is for; the bullets say what to do, in the order you do it. Keeping
 * them apart is what lets the description stay short enough to be read.
 */
data class OnboardingSlide(
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int,
    val bulletRes: List<Int> = emptyList(),
    val accent: SlideAccent = SlideAccent.PRIMARY,
)

/**
 * The guide: four steps, in the order the user will actually do them.
 *
 * It is deliberately not a feature tour. Two things in this app give wrong
 * answers if nobody explains them — the starting odometer reading, and
 * resetting the trip meter at every refill — and a guide that lists features
 * while leaving those two unsaid produces a user whose consumption figures are
 * quietly wrong. Those two get a step each; everything else shares the other
 * two.
 */
object OnboardingContent {
    val slides = listOf(
        OnboardingSlide(
            titleRes = R.string.onboarding_step_car_title,
            descriptionRes = R.string.onboarding_step_car_desc,
            bulletRes = listOf(
                R.string.onboarding_step_car_bullet_1,
                R.string.onboarding_step_car_bullet_2,
            ),
            accent = SlideAccent.PRIMARY,
        ),
        OnboardingSlide(
            titleRes = R.string.onboarding_step_refill_title,
            descriptionRes = R.string.onboarding_step_refill_desc,
            bulletRes = listOf(
                R.string.onboarding_step_refill_bullet_1,
                R.string.onboarding_step_refill_bullet_2,
                R.string.onboarding_step_refill_bullet_3,
            ),
            accent = SlideAccent.TERTIARY,
        ),
        OnboardingSlide(
            titleRes = R.string.onboarding_step_costs_title,
            descriptionRes = R.string.onboarding_step_costs_desc,
            bulletRes = listOf(
                R.string.onboarding_step_costs_bullet_1,
                R.string.onboarding_step_costs_bullet_2,
            ),
            accent = SlideAccent.SECONDARY,
        ),
        OnboardingSlide(
            titleRes = R.string.onboarding_step_stats_title,
            descriptionRes = R.string.onboarding_step_stats_desc,
            bulletRes = listOf(
                R.string.onboarding_step_stats_bullet_1,
                R.string.onboarding_step_stats_bullet_2,
            ),
            accent = SlideAccent.PRIMARY,
        ),
    )
}
