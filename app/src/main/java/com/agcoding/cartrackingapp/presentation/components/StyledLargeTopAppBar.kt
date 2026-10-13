package com.agcoding.cartrackingapp.presentation.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme
import com.agcoding.cartrackingapp.util.DeviceUtils

/**
 * A top bar whose title sits *below* the bar, in large type, and collapses
 * back into it as the content scrolls up.
 *
 * This is the Material 3 way to give a screen a big heading. Writing a `Text`
 * under the Scaffold's `topBar` would look the same standing still and be
 * worse in motion: that heading scrolls away for good, so a moment after you
 * start reading, nothing on screen says which screen you are on. Here the
 * title shrinks into the bar and stays.
 *
 * On a phone in landscape it falls back to the ordinary bar — a large one eats
 * a third of a short screen, which is why Material does not recommend it there.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StyledLargeTopAppBar(
    title: @Composable () -> Unit,
    scrollBehavior: TopAppBarScrollBehavior,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
    colors: TopAppBarColors = TopAppBarDefaults.largeTopAppBarColors(
        // Flat in both states: the rest of the app's bars sit straight on the
        // background, and a bar that tints itself only once collapsed would
        // read as a different component.
        containerColor = MaterialTheme.colorScheme.background,
        scrolledContainerColor = MaterialTheme.colorScheme.background,
    ),
) {
    val isTablet = DeviceUtils.isTablet()
    val isLandscape = DeviceUtils.isLandscape()

    if (!isTablet && isLandscape) {
        StyledTopAppBar(
            title = title,
            modifier = modifier,
            navigationIcon = navigationIcon,
            actions = actions,
        )
        return
    }

    LargeTopAppBar(
        title = title,
        modifier = modifier,
        navigationIcon = navigationIcon,
        actions = actions,
        colors = colors,
        scrollBehavior = scrollBehavior,
        windowInsets = TopAppBarDefaults.windowInsets,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
private fun StyledLargeTopAppBarPreview() {
    CarTrackingAppTheme {
        StyledLargeTopAppBar(
            title = { Text("Service Reminders") },
            scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(),
        )
    }
}
