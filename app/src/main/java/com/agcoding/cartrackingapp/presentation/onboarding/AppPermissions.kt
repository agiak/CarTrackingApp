package com.agcoding.cartrackingapp.presentation.onboarding

import android.Manifest
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Data class representing a permission that the app needs
 * This is data-driven and extensible for future permissions
 */
data class PermissionItem(
    val permission: String,
    val title: String,
    val description: String,
    val icon: ImageVector,
    val isRequired: Boolean = false // true = app won't work properly without it
)

/**
 * List of permissions the app needs - easily extensible
 * Add new permissions here when needed
 */
object AppPermissions {
    val permissions = listOf(
        PermissionItem(
            permission = Manifest.permission.ACCESS_FINE_LOCATION,
            title = "Location",
            description = "Used to automatically save where a fuel refill happened. This helps you remember which gas stations you visited.",
            icon = Icons.Default.LocationOn,
            isRequired = false
        ),
        PermissionItem(
            permission = Manifest.permission.POST_NOTIFICATIONS,
            title = "Notifications",
            description = "Get reminders about upcoming service dates, insurance renewals, and other important car-related events.",
            icon = Icons.Default.Notifications,
            isRequired = false
        )
    )

    // Get just the permission strings for requesting
    val permissionStrings: List<String>
        get() = permissions.map { it.permission }
}

