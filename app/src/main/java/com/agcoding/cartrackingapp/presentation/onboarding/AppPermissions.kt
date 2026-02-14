package com.agcoding.cartrackingapp.presentation.onboarding

import android.Manifest
import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.ui.graphics.vector.ImageVector
import com.agcoding.cartrackingapp.R

/**
 * Data class representing a permission that the app needs.
 * Data-driven and extensible for future permissions.
 */
data class PermissionItem(
    val permission: String,
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int,
    val icon: ImageVector,
    val isRequired: Boolean = false // true = app won't work properly without it
)

/**
 * List of permissions the app needs - easily extensible.
 * Add new permissions here when needed.
 */
object AppPermissions {
    val permissions = listOf(
        PermissionItem(
            permission = Manifest.permission.ACCESS_FINE_LOCATION,
            titleRes = R.string.permission_location_title,
            descriptionRes = R.string.permission_location_desc,
            icon = Icons.Default.LocationOn,
            isRequired = false
        ),
        PermissionItem(
            permission = Manifest.permission.POST_NOTIFICATIONS,
            titleRes = R.string.permission_notifications_title,
            descriptionRes = R.string.permission_notifications_desc,
            icon = Icons.Default.Notifications,
            isRequired = false
        ),
        PermissionItem(
            permission = Manifest.permission.RECORD_AUDIO,
            titleRes = R.string.permission_microphone_title,
            descriptionRes = R.string.permission_microphone_desc,
            icon = Icons.Default.Mic,
            isRequired = false
        )
    )

    // Get just the permission strings for requesting
    val permissionStrings: List<String>
        get() = permissions.map { it.permission }
}
