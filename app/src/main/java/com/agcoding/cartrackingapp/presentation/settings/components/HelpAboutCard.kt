package com.agcoding.cartrackingapp.presentation.settings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Help
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.presentation.components.StyledCard
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme

@Composable
fun HelpAboutCard(
    appVersion: String,
    onViewGuide: () -> Unit
) {
    StyledCard(
        modifier = Modifier.fillMaxWidth(),
        tintAlpha = 0.3f
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // View App Guide
            SettingsRow(
                icon = Icons.AutoMirrored.Filled.Help,
                iconBackgroundColor = Color(0xFF4CAF50).copy(alpha = 0.1f),
                iconTint = Color(0xFF4CAF50),
                title = stringResource(R.string.settings_view_app_guide),
                subtitle = stringResource(R.string.settings_learn_how_to_use_the_app),
                onClick = onViewGuide,
                trailing = {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            )

            // Divider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(1.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            )

            // About
            SettingsRow(
                icon = Icons.Default.Info,
                iconBackgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                iconTint = MaterialTheme.colorScheme.primary,
                title = stringResource(R.string.settings_about),
                subtitle = null,
                trailing = {
                    Column(horizontalAlignment = Alignment.End) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = stringResource(R.string.version),
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = appVersion,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Text(
                            text = stringResource(R.string.offline_first),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewHelpAboutCardLight() {
    CarTrackingAppTheme(darkTheme = false) {
        HelpAboutCard(
            appVersion = "1.0.0",
            onViewGuide = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewHelpAboutCardDark() {
    CarTrackingAppTheme(darkTheme = true) {
        HelpAboutCard(
            appVersion = "1.0.0",
            onViewGuide = {}
        )
    }
}

