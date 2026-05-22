package com.agcoding.cartrackingapp.shared.ui.components.textfield

import androidx.compose.ui.graphics.Color
import com.agcoding.cartrackingapp.shared.ui.tokens.AppColorScheme

data class AppTextFieldColors(
    val focusedBorderColor: Color,
    val unfocusedBorderColor: Color,
    val errorBorderColor: Color,
    val focusedLabelColor: Color,
    val unfocusedLabelColor: Color,
    val errorLabelColor: Color,
    val cursorColor: Color,
    val errorMessageColor: Color,
)

fun AppColorScheme.defaultTextFieldColors() = AppTextFieldColors(
    focusedBorderColor   = borderFocused,
    unfocusedBorderColor = borderDefault,
    errorBorderColor     = statusError,
    focusedLabelColor    = actionPrimary,
    unfocusedLabelColor  = contentSecondary,
    errorLabelColor      = statusError,
    cursorColor          = actionPrimary,
    errorMessageColor    = statusError,
)
