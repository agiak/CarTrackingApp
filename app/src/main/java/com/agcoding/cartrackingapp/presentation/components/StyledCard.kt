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
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.dp

/**
 * A styled card component with consistent design across the app.
 * Uses rounded corners and a subtly tinted surface color.
 * The tint is derived from the theme's outline variant color.
 */
@Composable
fun StyledCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    containerColor: Color? = null,
    border: BorderStroke? = null,
    tintAlpha: Float = 0.08f, // Subtle tint strength (8% by default for more visibility)
    content: @Composable ColumnScope.() -> Unit
) {
    // Calculate the background color with subtle tint
    val finalContainerColor = when {
        containerColor != null -> containerColor
        else -> {
            // Create a tinted surface by blending the outline variant color with the surface
            val surfaceColor = MaterialTheme.colorScheme.surface
            val tintColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 1f)

            // Blend: surface color with a subtle overlay of tint color
            lerp(
                surfaceColor,
                tintColor,
                tintAlpha
            )
        }
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
