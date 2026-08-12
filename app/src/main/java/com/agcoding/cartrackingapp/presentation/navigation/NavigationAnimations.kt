package com.agcoding.cartrackingapp.presentation.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.navigation.NavBackStackEntry

/**
 * Navigation animation types for different screen transitions
 */
enum class NavigationAnimationType {
    /** Default horizontal slide + fade for hierarchical navigation */
    HORIZONTAL_SLIDE,

    /** Vertical slide from bottom for action/modal screens */
    VERTICAL_SLIDE,

    /** Simple fade for subtle transitions */
    FADE_ONLY,

    /** No animation */
    NONE
}

/**
 * Animation configuration for screen transitions
 */
data class NavigationAnimationConfig(
    val type: NavigationAnimationType = NavigationAnimationType.HORIZONTAL_SLIDE,
    val durationMillis: Int = 300 // Smooth, snappy feel
)

/**
 * Professional navigation animations for a structured, productivity-focused app.
 *
 * Design principles:
 * - Fast and subtle (200ms)
 * - Predictable and structured
 * - Non-distracting
 * - Professional appearance
 * - Snappy, no lag
 *
 * Optimizations:
 * - Uses FastOutSlowInEasing for consistent speed (no slowdown at end)
 * - Reduced duration (200ms)
 * - Simplified offset calculations (30% of screen)
 * - Minimal fade for performance
 */
object NavigationAnimations {

    // Default animation durations (smooth, snappy)
    private const val DEFAULT_DURATION = 300
    private const val MODAL_DURATION = 220

    /**
     * Default enter transition: Horizontal slide from right + subtle fade
     * Used when navigating forward into hierarchical content
     */
    fun defaultEnterTransition(
        config: NavigationAnimationConfig = NavigationAnimationConfig()
    ): (AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition) = {
        when (config.type) {
            NavigationAnimationType.HORIZONTAL_SLIDE -> {
                slideInHorizontally(
                    animationSpec = tween(
                        durationMillis = config.durationMillis,
                        easing = FastOutSlowInEasing
                    ),
                    initialOffsetX = { fullWidth -> (fullWidth * 0.3).toInt() } // 30% offset for subtlety
                ) + fadeIn(
                    animationSpec = tween(
                        durationMillis = config.durationMillis / 2, // Faster fade
                        easing = FastOutSlowInEasing
                    ),
                    initialAlpha = 0.5f
                )
            }
            NavigationAnimationType.VERTICAL_SLIDE -> {
                slideInVertically(
                    animationSpec = tween(
                        durationMillis = config.durationMillis,
                        easing = FastOutSlowInEasing
                    ),
                    initialOffsetY = { fullHeight -> (fullHeight * 0.3).toInt() }
                ) + fadeIn(
                    animationSpec = tween(
                        durationMillis = config.durationMillis / 2,
                        easing = FastOutSlowInEasing
                    ),
                    initialAlpha = 0.5f
                )
            }
            NavigationAnimationType.FADE_ONLY -> {
                fadeIn(
                    animationSpec = tween(
                        durationMillis = config.durationMillis,
                        easing = FastOutSlowInEasing
                    )
                )
            }
            NavigationAnimationType.NONE -> {
                EnterTransition.None
            }
        }
    }

    /**
     * Default exit transition: Subtle fade only
     * Used when navigating forward (current screen exits)
     */
    fun defaultExitTransition(
        config: NavigationAnimationConfig = NavigationAnimationConfig()
    ): (AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition) = {
        when (config.type) {
            NavigationAnimationType.HORIZONTAL_SLIDE,
            NavigationAnimationType.VERTICAL_SLIDE,
            NavigationAnimationType.FADE_ONLY -> {
                fadeOut(
                    animationSpec = tween(
                        durationMillis = config.durationMillis / 2,
                        easing = FastOutSlowInEasing
                    ),
                    targetAlpha = 0.5f
                )
            }
            NavigationAnimationType.NONE -> {
                ExitTransition.None
            }
        }
    }

    /**
     * Default pop enter transition: Subtle fade
     * Used when navigating back (previous screen re-enters)
     */
    fun defaultPopEnterTransition(
        config: NavigationAnimationConfig = NavigationAnimationConfig()
    ): (AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition) = {
        when (config.type) {
            NavigationAnimationType.HORIZONTAL_SLIDE,
            NavigationAnimationType.VERTICAL_SLIDE,
            NavigationAnimationType.FADE_ONLY -> {
                fadeIn(
                    animationSpec = tween(
                        durationMillis = config.durationMillis / 2,
                        easing = FastOutSlowInEasing
                    ),
                    initialAlpha = 0.5f
                )
            }
            NavigationAnimationType.NONE -> {
                EnterTransition.None
            }
        }
    }

    /**
     * Default pop exit transition: Horizontal slide to right + slower fade
     * Used when navigating back (current screen exits)
     */
    fun defaultPopExitTransition(
        config: NavigationAnimationConfig = NavigationAnimationConfig()
    ): (AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition) = {
        when (config.type) {
            NavigationAnimationType.HORIZONTAL_SLIDE -> {
                slideOutHorizontally(
                    animationSpec = tween(
                        durationMillis = config.durationMillis,
                        easing = FastOutSlowInEasing
                    ),
                    targetOffsetX = { fullWidth -> fullWidth } // Full width slide for visibility
                ) + fadeOut(
                    animationSpec = tween(
                        durationMillis = config.durationMillis, // match the slide for a smooth exit
                        easing = FastOutSlowInEasing
                    ),
                    targetAlpha = 0f
                )
            }
            NavigationAnimationType.VERTICAL_SLIDE -> {
                slideOutVertically(
                    animationSpec = tween(
                        durationMillis = config.durationMillis,
                        easing = FastOutSlowInEasing
                    ),
                    targetOffsetY = { fullHeight -> fullHeight } // Full height slide
                ) + fadeOut(
                    animationSpec = tween(
                        durationMillis = config.durationMillis, // match the slide for a smooth exit
                        easing = FastOutSlowInEasing
                    ),
                    targetAlpha = 0f
                )
            }
            NavigationAnimationType.FADE_ONLY -> {
                fadeOut(
                    animationSpec = tween(
                        durationMillis = config.durationMillis,
                        easing = FastOutSlowInEasing
                    )
                )
            }
            NavigationAnimationType.NONE -> {
                ExitTransition.None
            }
        }
    }

    // ============================================
    // Predefined Animation Configurations
    // ============================================

    /** Default horizontal slide animation (primary navigation style) */
    val HorizontalSlide = NavigationAnimationConfig(
        type = NavigationAnimationType.HORIZONTAL_SLIDE,
        durationMillis = DEFAULT_DURATION
    )

    /** Vertical slide animation (for action/modal screens) */
    val VerticalSlide = NavigationAnimationConfig(
        type = NavigationAnimationType.VERTICAL_SLIDE,
        durationMillis = MODAL_DURATION
    )

    /** Fade-only animation (subtle, non-directional) */
    val FadeOnly = NavigationAnimationConfig(
        type = NavigationAnimationType.FADE_ONLY,
        durationMillis = DEFAULT_DURATION
    )

    /** No animation */
    val None = NavigationAnimationConfig(
        type = NavigationAnimationType.NONE,
        durationMillis = 0
    )
}

