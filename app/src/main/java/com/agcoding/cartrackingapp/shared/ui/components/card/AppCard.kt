package com.agcoding.cartrackingapp.shared.ui.components.card

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.agcoding.cartrackingapp.data.preferences.ColorPalette
import com.agcoding.cartrackingapp.presentation.theme.AppTheme
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppColorScheme
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppDimens

@Composable
fun AppCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val cardColors = LocalAppColorScheme.current.defaultCardColors()
    Surface(
        color    = cardColors.containerColor,
        border   = BorderStroke(0.5.dp, cardColors.borderColor),
        shape    = MaterialTheme.shapes.medium,
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            content  = content,
        )
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(name = "AppCard – Default Light", showBackground = true)
@Composable
private fun PreviewCardDefaultLight() {
    AppTheme(ColorPalette.DEFAULT_BLUE, isDark = false) {
        AppCard { Text("Card content", modifier = androidx.compose.ui.Modifier.fillMaxWidth()) }
    }
}

@Preview(name = "AppCard – Default Dark", showBackground = true)
@Composable
private fun PreviewCardDefaultDark() {
    AppTheme(ColorPalette.DEFAULT_BLUE, isDark = true) {
        AppCard { Text("Card content") }
    }
}

@Preview(name = "AppCard – Ocean Light", showBackground = true)
@Composable
private fun PreviewCardOceanLight() {
    AppTheme(ColorPalette.OCEAN_TEAL, isDark = false) {
        AppCard { Text("Card content") }
    }
}

@Preview(name = "AppCard – Ocean Dark", showBackground = true)
@Composable
private fun PreviewCardOceanDark() {
    AppTheme(ColorPalette.OCEAN_TEAL, isDark = true) {
        AppCard { Text("Card content") }
    }
}
