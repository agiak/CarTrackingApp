package com.agcoding.cartrackingapp.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.dp

/**
 * A styled card component with consistent design across the app.
 * Uses rounded corners, surface color, and outline variant border.
 * Automatically applies a subtle tint to the background based on the border color.
 */
@Composable
fun StyledCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    containerColor: Color? = null,
    border: BorderStroke? = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
    tintAlpha: Float = 0.08f, // Subtle tint strength (8% by default for more visibility)
    content: @Composable ColumnScope.() -> Unit
) {
    // Calculate the background color with subtle tint from border
    val finalContainerColor = when {
        containerColor != null -> containerColor
        border != null -> {
            // Extract the base color from the border brush (without alpha)
            val borderBrush = border.brush
            val borderColor = if (borderBrush is SolidColor) {
                // Get the color without alpha component
                borderBrush.value.copy(alpha = 1f)
            } else {
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 1f)
            }

            // Create a tinted surface by blending the border color with the surface
            val surfaceColor = MaterialTheme.colorScheme.surface
            // Blend: surface color with a subtle overlay of border color
            lerp(
                surfaceColor,
                borderColor,
                tintAlpha
            )
        }
        else -> MaterialTheme.colorScheme.surface
    }

    val colors = CardDefaults.cardColors(containerColor = finalContainerColor)

    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier,
            shape = RoundedCornerShape(16.dp),
            colors = colors,
            border = border,
            content = content
        )
    } else {
        Card(
            modifier = modifier,
            shape = RoundedCornerShape(16.dp),
            colors = colors,
            border = border,
            content = content
        )
    }
}
