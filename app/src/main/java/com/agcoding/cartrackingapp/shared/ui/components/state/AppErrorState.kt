package com.agcoding.cartrackingapp.shared.ui.components.state

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.data.preferences.ColorPalette
import com.agcoding.cartrackingapp.presentation.theme.AppTheme
import com.agcoding.cartrackingapp.shared.ui.components.button.AppButton
import com.agcoding.cartrackingapp.shared.ui.components.button.AppButtonStyle
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppColorScheme
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppDimens
import com.agcoding.cartrackingapp.shared.ui.tokens.AppIcons
import com.agcoding.cartrackingapp.shared.ui.utils.UiText
import com.agcoding.cartrackingapp.shared.ui.utils.asString

@Composable
fun AppErrorState(
    message: UiText,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null,
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
            imageVector        = AppIcons.Error,
            contentDescription = null,
            tint               = colors.statusError,
            modifier           = Modifier.size(48.dp),
        )
        Spacer(Modifier.height(dimens.spacing.md))
        Text(
            text      = message.asString(context),
            color     = colors.contentSecondary,
            style     = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
        )
        onRetry?.let {
            Spacer(Modifier.height(dimens.spacing.lg))
            AppButton(
                text    = stringResource(R.string.action_retry),
                onClick = it,
                style   = AppButtonStyle.Secondary,
            )
        }
    }
}

// ── Previews ──────────────────────────────────────────────────────────────────

private val previewMessage = UiText.DynamicString("Failed to load data. Please try again.")

@Preview(name = "AppErrorState – Default Light", showBackground = true)
@Composable
private fun PreviewErrorDefaultLight() {
    AppTheme(ColorPalette.DEFAULT_BLUE, isDark = false) {
        AppErrorState(message = previewMessage, onRetry = {})
    }
}

@Preview(name = "AppErrorState – Default Dark", showBackground = true)
@Composable
private fun PreviewErrorDefaultDark() {
    AppTheme(ColorPalette.DEFAULT_BLUE, isDark = true) {
        AppErrorState(message = previewMessage, onRetry = {})
    }
}

@Preview(name = "AppErrorState – Ocean Light", showBackground = true)
@Composable
private fun PreviewErrorOceanLight() {
    AppTheme(ColorPalette.OCEAN_TEAL, isDark = false) {
        AppErrorState(message = previewMessage)
    }
}

@Preview(name = "AppErrorState – Ocean Dark", showBackground = true)
@Composable
private fun PreviewErrorOceanDark() {
    AppTheme(ColorPalette.OCEAN_TEAL, isDark = true) {
        AppErrorState(message = previewMessage, onRetry = {})
    }
}
