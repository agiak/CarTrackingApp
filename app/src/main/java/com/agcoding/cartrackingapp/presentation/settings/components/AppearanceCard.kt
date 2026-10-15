package com.agcoding.cartrackingapp.presentation.settings.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.data.preferences.AppTheme
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppColorScheme
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppDimens

/**
 * Light, dark, or whatever the phone is doing.
 *
 * This was a switch labelled "Dark mode", which could not express the setting
 * it was editing: the app has three themes and a switch has two positions, so
 * "follow the system" was reachable only until the user touched it once, and
 * then never again from this screen. Three options, one of them selected, says
 * what is true and keeps every state reachable.
 *
 * The swatches are deliberately neutral greys rather than the user's palette.
 * They are previewing light versus dark, not the accent colour — which the
 * palette picker right below is for — and tinting them would make the two
 * controls look like they do the same thing.
 */
@Composable
fun AppearanceCard(
    currentTheme: AppTheme,
    onThemeChange: (AppTheme) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        ThemeOption(
            label = stringResource(R.string.theme_name_system),
            selected = currentTheme == AppTheme.SYSTEM,
            onClick = { onThemeChange(AppTheme.SYSTEM) },
        ) {
            // Split down the middle: the one option whose result depends on
            // something outside this screen shows both of its outcomes.
            Row(Modifier.fillMaxSize()) {
                SwatchHalf(LightPreviewBackground, LightPreviewCard)
                SwatchHalf(DarkPreviewBackground, DarkPreviewCard)
            }
        }
        ThemeOption(
            label = stringResource(R.string.theme_name_light),
            selected = currentTheme == AppTheme.LIGHT,
            onClick = { onThemeChange(AppTheme.LIGHT) },
        ) {
            Swatch(LightPreviewBackground, LightPreviewCard)
        }
        ThemeOption(
            label = stringResource(R.string.theme_name_dark),
            selected = currentTheme == AppTheme.DARK,
            onClick = { onThemeChange(AppTheme.DARK) },
        ) {
            Swatch(DarkPreviewBackground, DarkPreviewCard)
        }
    }
}

@Composable
private fun RowScope.ThemeOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    swatch: @Composable () -> Unit,
) {
    val scheme = LocalAppColorScheme.current
    val dimens = LocalAppDimens.current
    val shape = RoundedCornerShape(dimens.radius.iconTile)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(dimens.radius.tile))
            .clickable(onClick = onClick)
            .padding(4.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .clip(shape)
                .border(
                    BorderStroke(
                        width = if (selected) 2.dp else dimens.size.divider,
                        color = if (selected) scheme.actionPrimary else scheme.borderDefault,
                    ),
                    shape,
                ),
        ) {
            // Inset so the selection ring sits outside the preview rather than
            // on top of it; a 2dp border over the swatch would crop it.
            Box(modifier = Modifier.fillMaxSize().padding(3.dp).clip(RoundedCornerShape(9.dp))) {
                swatch()
            }
        }
        Spacer(Modifier.height(7.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) scheme.contentPrimary else scheme.contentSecondary,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** A miniature screen: a background with a card lying on it. */
@Composable
private fun Swatch(background: Color, card: Color) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(background),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.62f)
                .height(16.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(card),
        )
    }
}

@Composable
private fun RowScope.SwatchHalf(background: Color, card: Color) {
    Box(
        modifier = Modifier
            .weight(1f)
            .fillMaxHeight()
            .background(background),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .width(14.dp)
                .height(16.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(card),
        )
    }
}

// Fixed on purpose — see the note on AppearanceCard. These preview light and
// dark themselves, not the palette painted over them.
private val LightPreviewBackground = Color(0xFFF2F2F5)
private val LightPreviewCard = Color(0xFFFFFFFF)
private val DarkPreviewBackground = Color(0xFF17171A)
private val DarkPreviewCard = Color(0xFF2B2B30)

@Preview(showBackground = true, widthDp = 380)
@Composable
private fun PreviewAppearanceCardDark() {
    CarTrackingAppTheme(darkTheme = true) {
        Column(Modifier.padding(18.dp)) {
            AppearanceCard(currentTheme = AppTheme.DARK, onThemeChange = {})
        }
    }
}

@Preview(showBackground = true, widthDp = 380)
@Composable
private fun PreviewAppearanceCardSystem() {
    CarTrackingAppTheme(darkTheme = false) {
        Column(Modifier.padding(18.dp)) {
            AppearanceCard(currentTheme = AppTheme.SYSTEM, onThemeChange = {})
        }
    }
}
