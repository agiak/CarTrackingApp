package com.agcoding.cartrackingapp.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppDimens

/**
 * The app's card.
 *
 * Flat, on the palette's card surface, with a generous radius. There is no
 * shadow and no border: the redesign separates surfaces by tone and by corner
 * radius rather than by elevation, so a screen of six cards reads as six
 * panels of one object instead of six sheets of paper at different heights.
 *
 * @param tintAlpha kept so the existing call sites still compile; it no longer
 *   does anything. The old card blended the outline colour into the surface to
 *   fake a tint, which at the new radius and spacing just looked grubby.
 */
@Composable
fun StyledCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    containerColor: Color? = null,
    border: BorderStroke? = null,
    @Suppress("UNUSED_PARAMETER") tintAlpha: Float = 0f,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(LocalAppDimens.current.radius.card)
    val colors = CardDefaults.cardColors(
        containerColor = containerColor ?: MaterialTheme.colorScheme.surface,
    )
    val elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)

    if (onClick != null) {
        Card(
            onClick = onClick,
            modifier = modifier,
            shape = shape,
            colors = colors,
            elevation = elevation,
            border = border,
            content = content
        )
    } else {
        Card(
            modifier = modifier,
            shape = shape,
            colors = colors,
            elevation = elevation,
            border = border,
            content = content
        )
    }
}

@Preview(name = "Styled Card - Dark", showBackground = true, widthDp = 350)
@Composable
private fun PreviewStyledCard() {
    CarTrackingAppTheme(darkTheme = true) {
        StyledCard(modifier = Modifier.padding(16.dp)) {
            Text(text = "Default StyledCard", modifier = Modifier.padding(16.dp))
        }
    }
}

@Preview(name = "Styled Card - Light", showBackground = true, widthDp = 350)
@Composable
private fun PreviewStyledCardLight() {
    CarTrackingAppTheme(darkTheme = false) {
        StyledCard(onClick = {}, modifier = Modifier.padding(16.dp)) {
            Text(text = "Clickable StyledCard", modifier = Modifier.padding(16.dp))
        }
    }
}

@Preview(name = "Styled Card - Accent", showBackground = true, widthDp = 350)
@Composable
private fun PreviewStyledCardAccent() {
    CarTrackingAppTheme(darkTheme = true) {
        StyledCard(
            containerColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                text = "Accent card",
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}
