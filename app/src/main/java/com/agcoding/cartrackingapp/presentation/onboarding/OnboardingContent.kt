package com.agcoding.cartrackingapp.presentation.onboarding

import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.agcoding.cartrackingapp.R

/**
 * Data class representing a single onboarding slide.
 * Keep resources as IDs so we can localize and resolve them in Composables.
 */
data class OnboardingSlide(
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int,
    val iconResId: Int? = null, // For drawable resources
    val iconVector: ImageVector? = null, // For vector icons
    val iconColor: Color = Color.Unspecified
)

/**
 * Onboarding slides content - easily extensible.
 */
object OnboardingContent {
    val slides = listOf(
        OnboardingSlide(
            titleRes = R.string.onboarding_welcome_title,
            descriptionRes = R.string.onboarding_welcome_desc,
            iconResId = null,
            iconColor = Color(0xFF4CAF50)
        ),
        OnboardingSlide(
            titleRes = R.string.onboarding_refills_title,
            descriptionRes = R.string.onboarding_refills_desc,
            iconResId = null,
            iconColor = Color(0xFF2196F3)
        ),
        OnboardingSlide(
            titleRes = R.string.onboarding_service_title,
            descriptionRes = R.string.onboarding_service_desc,
            iconResId = null,
            iconColor = Color(0xFF4CAF50)
        ),
        OnboardingSlide(
            titleRes = R.string.onboarding_expenses_title,
            descriptionRes = R.string.onboarding_expenses_desc,
            iconResId = null,
            iconColor = Color(0xFFFF9800)
        ),
        OnboardingSlide(
            titleRes = R.string.onboarding_stats_title,
            descriptionRes = R.string.onboarding_stats_desc,
            iconResId = null,
            iconColor = Color(0xFF9C27B0)
        )
    )
}
