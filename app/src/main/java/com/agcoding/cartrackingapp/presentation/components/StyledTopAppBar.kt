package com.agcoding.cartrackingapp.presentation.components
import androidx.compose.ui.tooling.preview.Preview
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme
import androidx.compose.material3.Text

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.agcoding.cartrackingapp.util.DeviceUtils

/**
 * A styled TopAppBar that automatically adjusts its window insets based on device type and orientation.
 * - Mobile phones in landscape: Reduced top padding for better screen usage
 * - Tablets and portrait phones: Default system window insets
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StyledTopAppBar(
    title: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors(
        containerColor = MaterialTheme.colorScheme.background
    )
) {
    val isTablet = DeviceUtils.isTablet()
    val isLandscape = DeviceUtils.isLandscape()

    // Only reduce top padding for mobile phones in landscape mode
    val shouldReduceTopPadding = !isTablet && isLandscape

    TopAppBar(
        title = title,
        modifier = modifier,
        navigationIcon = navigationIcon,
        actions = actions,
        colors = colors,
        windowInsets = if (shouldReduceTopPadding) {
            WindowInsets(top = 0) // Remove top padding for mobile landscape
        } else {
            TopAppBarDefaults.windowInsets // Use default insets for tablets and portrait
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun StyledTopAppBarPreview() {
    CarTrackingAppTheme {
        StyledTopAppBar(title = { Text("Title") })
    }
}
