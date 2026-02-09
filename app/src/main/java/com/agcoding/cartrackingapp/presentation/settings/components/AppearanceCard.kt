package com.agcoding.cartrackingapp.presentation.settings.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.data.preferences.AppTheme
import com.agcoding.cartrackingapp.data.preferences.ColorPalette
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme

@Composable
fun AppearanceCard(
    currentTheme: AppTheme,
    onThemeChange: (AppTheme) -> Unit
) {
    val systemInDarkTheme = isSystemInDarkTheme()
    val isDarkActive = when (currentTheme) {
        AppTheme.DARK -> true
        AppTheme.LIGHT -> false
        AppTheme.SYSTEM -> systemInDarkTheme
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Icon with background
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isDarkActive) Icons.Default.DarkMode else Icons.Default.LightMode,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = stringResource(R.string.appearance_dark_mode),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = when (currentTheme) {
                            AppTheme.SYSTEM -> stringResource(R.string.settings_following_system)
                            AppTheme.LIGHT -> stringResource(R.string.appearance_light_theme_active)
                            AppTheme.DARK -> stringResource(R.string.settings_dark_theme_active)
                        },
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Switch(
                checked = isDarkActive,
                onCheckedChange = { enabled ->
                    onThemeChange(if (enabled) AppTheme.DARK else AppTheme.LIGHT)
                },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewAppearanceCardLight() {
    CarTrackingAppTheme(darkTheme = false, colorPalette = ColorPalette.DEFAULT_BLUE) {
        AppearanceCard(
            currentTheme = AppTheme.LIGHT,
            onThemeChange = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewAppearanceCardDark() {
    CarTrackingAppTheme(darkTheme = true, colorPalette = ColorPalette.DEFAULT_BLUE) {
        AppearanceCard(
            currentTheme = AppTheme.DARK,
            onThemeChange = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewAppearanceCardSystem() {
    CarTrackingAppTheme(darkTheme = false, colorPalette = ColorPalette.DEFAULT_BLUE) {
        AppearanceCard(
            currentTheme = AppTheme.SYSTEM,
            onThemeChange = {}
        )
    }
}
