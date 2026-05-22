package com.agcoding.cartrackingapp.shared.ui.components.topbar

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.data.preferences.ColorPalette
import com.agcoding.cartrackingapp.presentation.theme.AppTheme
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppColorScheme
import com.agcoding.cartrackingapp.shared.ui.tokens.AppIcons
import com.agcoding.cartrackingapp.util.DeviceUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String,
    modifier: Modifier = Modifier,
    onNavigateUp: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    val colors = LocalAppColorScheme.current

    // Mobile landscape: remove top padding for better screen use.
    val windowInsets = if (!DeviceUtils.isTablet() && DeviceUtils.isLandscape()) {
        WindowInsets(top = 0)
    } else {
        TopAppBarDefaults.windowInsets
    }

    TopAppBar(
        title = {
            Text(
                text  = title,
                style = MaterialTheme.typography.titleLarge,
                color = colors.contentPrimary,
            )
        },
        navigationIcon = {
            onNavigateUp?.let {
                IconButton(onClick = it) {
                    Icon(
                        imageVector        = AppIcons.ArrowBack,
                        contentDescription = stringResource(R.string.cd_navigate_up),
                        tint               = colors.contentPrimary,
                    )
                }
            }
        },
        actions      = actions,
        colors       = TopAppBarDefaults.topAppBarColors(containerColor = colors.backgroundPrimary),
        windowInsets = windowInsets,
        modifier     = modifier,
    )
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(name = "AppTopBar – Default Light", showBackground = true)
@Composable
private fun PreviewTopBarDefaultLight() {
    AppTheme(ColorPalette.DEFAULT_BLUE, isDark = false) {
        AppTopBar(title = "Car Details", onNavigateUp = {})
    }
}

@Preview(name = "AppTopBar – Default Dark", showBackground = true)
@Composable
private fun PreviewTopBarDefaultDark() {
    AppTheme(ColorPalette.DEFAULT_BLUE, isDark = true) {
        AppTopBar(title = "Car Details", onNavigateUp = {})
    }
}

@Preview(name = "AppTopBar – Ocean Light", showBackground = true)
@Composable
private fun PreviewTopBarOceanLight() {
    AppTheme(ColorPalette.OCEAN_TEAL, isDark = false) {
        AppTopBar(title = "Statistics")
    }
}

@Preview(name = "AppTopBar – Ocean Dark", showBackground = true)
@Composable
private fun PreviewTopBarOceanDark() {
    AppTheme(ColorPalette.OCEAN_TEAL, isDark = true) {
        AppTopBar(title = "Statistics", onNavigateUp = {})
    }
}
