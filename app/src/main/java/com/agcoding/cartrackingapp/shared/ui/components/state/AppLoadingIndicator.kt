package com.agcoding.cartrackingapp.shared.ui.components.state

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.agcoding.cartrackingapp.data.preferences.ColorPalette
import com.agcoding.cartrackingapp.presentation.theme.AppTheme
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppColorScheme
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppDimens

@Composable
fun AppLoadingIndicator(
    modifier: Modifier = Modifier,
    fullscreen: Boolean = false,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = if (fullscreen) {
            modifier.fillMaxSize()
        } else {
            modifier
                .fillMaxWidth()
                .padding(LocalAppDimens.current.spacing.xl)
        },
    ) {
        CircularProgressIndicator(color = LocalAppColorScheme.current.actionPrimary)
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(name = "Loading – Default Light", showBackground = true)
@Composable
private fun PreviewLoadingDefaultLight() {
    AppTheme(ColorPalette.DEFAULT_BLUE, isDark = false) { AppLoadingIndicator() }
}

@Preview(name = "Loading – Default Dark", showBackground = true)
@Composable
private fun PreviewLoadingDefaultDark() {
    AppTheme(ColorPalette.DEFAULT_BLUE, isDark = true) { AppLoadingIndicator() }
}

@Preview(name = "Loading – Ocean Light", showBackground = true)
@Composable
private fun PreviewLoadingOceanLight() {
    AppTheme(ColorPalette.OCEAN_TEAL, isDark = false) { AppLoadingIndicator() }
}

@Preview(name = "Loading – Ocean Dark", showBackground = true)
@Composable
private fun PreviewLoadingOceanDark() {
    AppTheme(ColorPalette.OCEAN_TEAL, isDark = true) { AppLoadingIndicator() }
}
