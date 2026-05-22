package com.agcoding.cartrackingapp.shared.ui.components.button

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.agcoding.cartrackingapp.data.preferences.ColorPalette
import com.agcoding.cartrackingapp.presentation.theme.AppTheme
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppColorScheme
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppDimens

enum class AppButtonStyle { Primary, Secondary, Text }

@Composable
fun AppButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: AppButtonStyle = AppButtonStyle.Primary,
    enabled: Boolean = true,
    isLoading: Boolean = false,
    leadingIcon: ImageVector? = null,
) {
    val colors = LocalAppColorScheme.current
    val buttonColors = when (style) {
        AppButtonStyle.Primary   -> colors.primaryButtonColors()
        AppButtonStyle.Secondary -> colors.secondaryButtonColors()
        AppButtonStyle.Text      -> colors.textButtonColors()
    }

    Button(
        onClick  = onClick,
        enabled  = enabled && !isLoading,
        modifier = modifier.defaultMinSize(minHeight = 48.dp),
        colors   = ButtonDefaults.buttonColors(
            containerColor         = buttonColors.containerColor,
            contentColor           = buttonColors.contentColor,
            disabledContainerColor = buttonColors.disabledContainerColor,
            disabledContentColor   = buttonColors.disabledContentColor,
        ),
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                modifier    = Modifier.size(18.dp),
                color       = buttonColors.contentColor,
                strokeWidth = 2.dp,
            )
        } else {
            leadingIcon?.let {
                Icon(it, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(LocalAppDimens.current.spacing.xs))
            }
            Text(text = text, style = MaterialTheme.typography.labelLarge)
        }
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

@Preview(name = "Primary – Default Light", showBackground = true)
@Composable
private fun PreviewPrimaryDefaultLight() {
    AppTheme(ColorPalette.DEFAULT_BLUE, isDark = false) { AppButton("Save", onClick = {}) }
}

@Preview(name = "Primary – Default Dark", showBackground = true)
@Composable
private fun PreviewPrimaryDefaultDark() {
    AppTheme(ColorPalette.DEFAULT_BLUE, isDark = true) { AppButton("Save", onClick = {}) }
}

@Preview(name = "Primary – Ocean Light", showBackground = true)
@Composable
private fun PreviewPrimaryOceanLight() {
    AppTheme(ColorPalette.OCEAN_TEAL, isDark = false) { AppButton("Save", onClick = {}) }
}

@Preview(name = "Primary – Ocean Dark", showBackground = true)
@Composable
private fun PreviewPrimaryOceanDark() {
    AppTheme(ColorPalette.OCEAN_TEAL, isDark = true) { AppButton("Save", onClick = {}) }
}

@Preview(name = "Secondary / Loading", showBackground = true)
@Composable
private fun PreviewSecondaryAndLoading() {
    AppTheme(ColorPalette.DEFAULT_BLUE, isDark = false) {
        AppButton("Retry", onClick = {}, style = AppButtonStyle.Secondary)
    }
}

@Preview(name = "Loading state", showBackground = true)
@Composable
private fun PreviewLoading() {
    AppTheme(ColorPalette.DEFAULT_BLUE, isDark = false) {
        AppButton("Saving…", onClick = {}, isLoading = true)
    }
}
