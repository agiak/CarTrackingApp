package com.agcoding.cartrackingapp.presentation.settings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.agcoding.cartrackingapp.data.preferences.ColorPalette
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme

@Composable
fun ColorPaletteItem(
    palette: ColorPalette,
    primaryColor: Color,
    secondaryColor: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(
                    width = if (isSelected) 3.dp else 1.dp,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.outlineVariant
                    },
                    shape = RoundedCornerShape(16.dp)
                )
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            // Split circle showing both colors
            Row(modifier = Modifier.fillMaxSize()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .background(primaryColor)
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .background(secondaryColor)
                )
            }

            // Checkmark overlay when selected
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewColorPaletteItemSelected() {
    CarTrackingAppTheme(darkTheme = false) {
        ColorPaletteItem(
            palette = ColorPalette.DEFAULT_BLUE,
            primaryColor = Color(0xFF1976D2),
            secondaryColor = Color(0xFF0288D1),
            isSelected = true,
            onClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewColorPaletteItemUnselected() {
    CarTrackingAppTheme(darkTheme = false) {
        ColorPaletteItem(
            palette = ColorPalette.SUNSET_ORANGE,
            primaryColor = Color(0xFFFF6F00),
            secondaryColor = Color(0xFFFF8F00),
            isSelected = false,
            onClick = {}
        )
    }
}

