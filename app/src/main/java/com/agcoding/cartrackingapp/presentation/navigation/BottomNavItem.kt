package com.agcoding.cartrackingapp.presentation.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.QueryStats
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(
    val route: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val label: String
) {
    object Cars : BottomNavItem(
        route = "cars",
        selectedIcon = Icons.Filled.DirectionsCar,
        unselectedIcon = Icons.Outlined.DirectionsCar,
        label = "Cars"
    )

    object Statistics : BottomNavItem(
        route = "statistics_tab",
        selectedIcon = Icons.Filled.QueryStats,
        unselectedIcon = Icons.Outlined.QueryStats,
        label = "Statistics"
    )

    object Settings : BottomNavItem(
        route = "settings",
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings,
        label = "Settings"
    )
}

val bottomNavItems = listOf(
    BottomNavItem.Cars,
    BottomNavItem.Statistics,
    BottomNavItem.Settings
)

