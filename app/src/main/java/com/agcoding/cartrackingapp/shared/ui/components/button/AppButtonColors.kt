package com.agcoding.cartrackingapp.shared.ui.components.button

import androidx.compose.ui.graphics.Color
import com.agcoding.cartrackingapp.shared.ui.tokens.AppColorScheme

data class AppButtonColors(
    val containerColor: Color,
    val contentColor: Color,
    val containerHoverColor: Color,
    val disabledContainerColor: Color,
    val disabledContentColor: Color,
)

fun AppColorScheme.primaryButtonColors() = AppButtonColors(
    containerColor        = actionPrimary,
    contentColor          = actionContent,
    containerHoverColor   = actionPrimaryHover,
    disabledContainerColor = actionPrimary.copy(alpha = 0.38f),
    disabledContentColor  = actionContent.copy(alpha = 0.38f),
)

fun AppColorScheme.secondaryButtonColors() = AppButtonColors(
    containerColor        = actionSecondary,
    contentColor          = actionSecondaryContent,
    containerHoverColor   = actionSecondary,
    disabledContainerColor = actionSecondary.copy(alpha = 0.38f),
    disabledContentColor  = actionSecondaryContent.copy(alpha = 0.38f),
)

fun AppColorScheme.textButtonColors() = AppButtonColors(
    containerColor        = Color.Transparent,
    contentColor          = actionPrimary,
    containerHoverColor   = actionPrimary.copy(alpha = 0.08f),
    disabledContainerColor = Color.Transparent,
    disabledContentColor  = contentDisabled,
)
