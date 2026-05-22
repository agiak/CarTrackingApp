package com.agcoding.cartrackingapp.shared.ui.components.card

import androidx.compose.ui.graphics.Color
import com.agcoding.cartrackingapp.shared.ui.tokens.AppColorScheme

data class AppCardColors(
    val containerColor: Color,
    val borderColor: Color,
    val contentColor: Color,
)

fun AppColorScheme.defaultCardColors() = AppCardColors(
    containerColor = backgroundCard,
    borderColor    = borderDefault,
    contentColor   = contentPrimary,
)

fun AppColorScheme.elevatedCardColors() = AppCardColors(
    containerColor = backgroundSecondary,
    borderColor    = borderDefault,
    contentColor   = contentPrimary,
)
