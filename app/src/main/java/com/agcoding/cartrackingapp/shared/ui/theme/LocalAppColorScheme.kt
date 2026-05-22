package com.agcoding.cartrackingapp.shared.ui.theme

import androidx.compose.runtime.staticCompositionLocalOf
import com.agcoding.cartrackingapp.shared.ui.tokens.AppColorScheme
import com.agcoding.cartrackingapp.shared.ui.tokens.AppDimens

val LocalAppColorScheme = staticCompositionLocalOf<AppColorScheme> {
    error("Wrap the composable tree with AppTheme")
}

val LocalAppDimens = staticCompositionLocalOf<AppDimens> {
    error("Wrap the composable tree with AppTheme")
}
