package com.agcoding.cartrackingapp.presentation.onboarding

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Data class representing a single onboarding slide
 */
data class OnboardingSlide(
    val title: String,
    val description: String,
    val iconResId: Int? = null, // For drawable resources
    val iconVector: ImageVector? = null, // For vector icons
    val iconColor: Color = Color.Unspecified
)

/**
 * Onboarding slides content - easily extensible
 */
object OnboardingContent {
    val slides = listOf(
        OnboardingSlide(
            title = "Welcome to Fuel Tracker",
            description = "Track your car expenses, fuel consumption, and maintenance all in one place. Get insights into your driving habits and save money.",
            iconResId = null,
            iconColor = Color(0xFF4CAF50) // Green
        ),
        OnboardingSlide(
            title = "Track Refills",
            description = "Log every fuel refill with details like cost, liters, and distance traveled. Calculate your real-time fuel consumption automatically.",
            iconResId = null,
            iconColor = Color(0xFF2196F3) // Blue
        ),
        OnboardingSlide(
            title = "Service & Maintenance",
            description = "Keep track of all service visits and maintenance costs. Add notes to remember what was done and when.",
            iconResId = null,
            iconColor = Color(0xFF4CAF50) // Green
        ),
        OnboardingSlide(
            title = "Other Expenses",
            description = "Record parking fees, tolls, car washes, insurance, and any other car-related expenses to see your total spending.",
            iconResId = null,
            iconColor = Color(0xFFFF9800) // Orange
        ),
        OnboardingSlide(
            title = "Detailed Statistics",
            description = "View comprehensive charts and graphs showing your consumption trends, costs over time, and distance traveled.",
            iconResId = null,
            iconColor = Color(0xFF9C27B0) // Purple
        )
    )
}

