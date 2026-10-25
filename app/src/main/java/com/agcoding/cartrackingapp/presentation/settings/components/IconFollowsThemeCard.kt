package com.agcoding.cartrackingapp.presentation.settings.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.presentation.components.StyledCard
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme

/**
 * Whether the launcher icon takes the colour of the chosen palette.
 *
 * Sits directly under the palette picker, because that is the control it
 * modifies — on its own elsewhere in Settings it would read as a separate
 * feature rather than as a property of the choice just made.
 *
 * The subtitle mentions re-adding the icon on purpose. Changing which alias is
 * enabled makes some launchers drop the shortcut from the home screen, and a
 * user who was not warned reads that as the app having broken.
 */
@Composable
fun IconFollowsThemeCard(
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    StyledCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth()) {
            SettingsRow(
                icon = Icons.Default.Palette,
                iconBackgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                iconTint = MaterialTheme.colorScheme.primary,
                title = stringResource(R.string.icon_follows_theme),
                subtitle = stringResource(R.string.icon_follows_theme_description),
                trailing = {
                    Switch(
                        checked = enabled,
                        onCheckedChange = onToggle,
                        colors = SwitchDefaults.colors(
                            checkedTrackColor = MaterialTheme.colorScheme.primary
                        )
                    )
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun IconFollowsThemeCardPreview() {
    CarTrackingAppTheme(darkTheme = true) {
        IconFollowsThemeCard(enabled = true, onToggle = {})
    }
}
