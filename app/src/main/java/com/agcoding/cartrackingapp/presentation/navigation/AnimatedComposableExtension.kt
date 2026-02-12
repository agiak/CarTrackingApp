package com.agcoding.cartrackingapp.presentation.navigation

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

/**
 * Extension function to easily apply navigation animations to composable destinations.
 *
 * Provides default professional animations while allowing per-screen customization.
 *
 * @param route The route for the destination
 * @param animationConfig The animation configuration to use (defaults to horizontal slide)
 * @param arguments Optional navigation arguments
 * @param content The composable content for this destination
 */
fun NavGraphBuilder.animatedComposable(
    route: String,
    animationConfig: NavigationAnimationConfig = NavigationAnimations.HorizontalSlide,
    arguments: List<androidx.navigation.NamedNavArgument> = emptyList(),
    enterTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition)? = null,
    exitTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition)? = null,
    popEnterTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition)? = null,
    popExitTransition: (AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition)? = null,
    content: @androidx.compose.runtime.Composable AnimatedContentScope.(NavBackStackEntry) -> Unit
) {
    composable(
        route = route,
        arguments = arguments,
        enterTransition = enterTransition ?: NavigationAnimations.defaultEnterTransition(animationConfig),
        exitTransition = exitTransition ?: NavigationAnimations.defaultExitTransition(animationConfig),
        popEnterTransition = popEnterTransition ?: NavigationAnimations.defaultPopEnterTransition(animationConfig),
        popExitTransition = popExitTransition ?: NavigationAnimations.defaultPopExitTransition(animationConfig),
        content = content
    )
}

