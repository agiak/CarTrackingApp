package com.agcoding.cartrackingapp.shared.ui.components.state

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.agcoding.cartrackingapp.data.preferences.ColorPalette
import com.agcoding.cartrackingapp.presentation.theme.AppTheme
import com.agcoding.cartrackingapp.shared.ui.components.button.AppButton
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppColorScheme
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppDimens
import com.agcoding.cartrackingapp.shared.ui.utils.UiText
import com.agcoding.cartrackingapp.shared.ui.utils.asString

@Composable
fun AppEmptyState(
    title: UiText,
    modifier: Modifier = Modifier,
    subtitle: UiText? = null,
    icon: ImageVector = Icons.Outlined.Inbox,
    action: Pair<UiText, () -> Unit>? = null,
) {
    val context = LocalContext.current
    val colors  = LocalAppColorScheme.current
    val dimens  = LocalAppDimens.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .fillMaxWidth()
            .padding(dimens.spacing.xl),
    ) {
        Icon(
            imageVector        = icon,
            contentDescription = null,
            tint               = colors.contentDisabled,
            modifier           = Modifier.size(56.dp),
        )
        Spacer(Modifier.height(dimens.spacing.md))
        Text(
            text      = title.asString(context),
            color     = colors.contentPrimary,
            style     = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
        )
        subtitle?.let {
            Spacer(Modifier.height(dimens.spacing.sm))
            Text(
                text      = it.asString(context),
                color     = colors.contentSecondary,
                style     = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
            )
        }
        action?.let { (label, onClick) ->
            Spacer(Modifier.height(dimens.spacing.lg))
            AppButton(text = label.asString(context), onClick = onClick)
        }
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

private val previewTitle    = UiText.DynamicString("No refills yet")
private val previewSubtitle = UiText.DynamicString("Add your first refill to start tracking")
private val previewAction   = UiText.DynamicString("Add Refill") to {}

@Preview(name = "AppEmptyState – Default Light", showBackground = true)
@Composable
private fun PreviewEmptyDefaultLight() {
    AppTheme(ColorPalette.DEFAULT_BLUE, isDark = false) {
        AppEmptyState(title = previewTitle, subtitle = previewSubtitle, action = previewAction)
    }
}

@Preview(name = "AppEmptyState – Default Dark", showBackground = true)
@Composable
private fun PreviewEmptyDefaultDark() {
    AppTheme(ColorPalette.DEFAULT_BLUE, isDark = true) {
        AppEmptyState(title = previewTitle, subtitle = previewSubtitle, action = previewAction)
    }
}

@Preview(name = "AppEmptyState – Ocean Light", showBackground = true)
@Composable
private fun PreviewEmptyOceanLight() {
    AppTheme(ColorPalette.OCEAN_TEAL, isDark = false) {
        AppEmptyState(title = previewTitle)
    }
}

@Preview(name = "AppEmptyState – Ocean Dark", showBackground = true)
@Composable
private fun PreviewEmptyOceanDark() {
    AppTheme(ColorPalette.OCEAN_TEAL, isDark = true) {
        AppEmptyState(title = previewTitle, subtitle = previewSubtitle)
    }
}
