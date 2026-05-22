package com.agcoding.cartrackingapp.shared.ui.tokens

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.agcoding.cartrackingapp.shared.ui.tokens.brand.BrandTokens

data class AppColorScheme(
    val backgroundPrimary: Color,
    val backgroundSecondary: Color,
    val backgroundCard: Color,
    val backgroundOverlay: Color,
    val contentPrimary: Color,
    val contentSecondary: Color,
    val contentDisabled: Color,
    val actionPrimary: Color,
    val actionPrimaryHover: Color,
    val actionContent: Color,
    val actionSecondary: Color,
    val actionSecondaryContent: Color,
    val borderDefault: Color,
    val borderFocused: Color,
    val borderStrong: Color,
    val statusSuccess: Color,
    val statusSuccessSubtle: Color,
    val statusError: Color,
    val statusErrorSubtle: Color,
    val statusWarning: Color,
    val statusWarningSubtle: Color,
)

// ── BrandTokens → AppColorScheme ─────────────────────────────────────────────

fun buildColorScheme(brand: BrandTokens, isDark: Boolean): AppColorScheme =
    if (isDark) darkScheme(brand) else lightScheme(brand)

private fun lightScheme(b: BrandTokens) = AppColorScheme(
    backgroundPrimary      = b.neutral50,
    backgroundSecondary    = b.neutral100,
    backgroundCard         = Color.White,
    backgroundOverlay      = Color.Black.copy(alpha = 0.4f),
    contentPrimary         = b.neutral900,
    contentSecondary       = b.neutral600,
    contentDisabled        = b.neutral400,
    actionPrimary          = b.primary400,
    actionPrimaryHover     = b.primary600,
    actionContent          = Color.White,
    actionSecondary        = b.primary50,
    actionSecondaryContent = b.primary600,
    borderDefault          = b.neutral200,
    borderFocused          = b.primary400,
    borderStrong           = b.neutral600,
    statusSuccess          = b.success400,
    statusSuccessSubtle    = b.success400.copy(alpha = 0.12f),
    statusError            = b.error400,
    statusErrorSubtle      = b.error400.copy(alpha = 0.12f),
    statusWarning          = b.warning400,
    statusWarningSubtle    = b.warning400.copy(alpha = 0.12f),
)

private fun darkScheme(b: BrandTokens) = AppColorScheme(
    backgroundPrimary      = b.neutral900,
    backgroundSecondary    = b.neutral600.copy(alpha = 0.25f),
    backgroundCard         = b.neutral100.copy(alpha = 0.08f),
    backgroundOverlay      = Color.Black.copy(alpha = 0.6f),
    contentPrimary         = b.neutral50,
    contentSecondary       = b.neutral200,
    contentDisabled        = b.neutral400,
    actionPrimary          = b.primary400,
    actionPrimaryHover     = b.primary200,
    actionContent          = Color.White,
    actionSecondary        = b.primary800,
    actionSecondaryContent = b.primary200,
    borderDefault          = b.neutral600.copy(alpha = 0.4f),
    borderFocused          = b.primary400,
    borderStrong           = b.neutral400,
    statusSuccess          = b.success300,
    statusSuccessSubtle    = b.success300.copy(alpha = 0.15f),
    statusError            = b.error300,
    statusErrorSubtle      = b.error300.copy(alpha = 0.15f),
    statusWarning          = b.warning300,
    statusWarningSubtle    = b.warning300.copy(alpha = 0.15f),
)

// ── Primary-only → AppColorScheme (for existing 40 palettes) ─────────────────
// Preserves the "tinted background" look from the original Theme.kt.

private const val LIGHT_BG_TINT     = 0.20f
private const val LIGHT_CARD_TINT   = LIGHT_BG_TINT * 0.40f   // 0.08f
private const val LIGHT_SEC_TINT    = LIGHT_BG_TINT * 1.20f   // 0.24f

private const val DARK_BG_TINT      = 0.20f
private const val DARK_CARD_TINT    = DARK_BG_TINT * 0.60f    // 0.12f
private const val DARK_SEC_TINT     = DARK_BG_TINT * 1.30f    // 0.26f

private val LIGHT_CONTENT_PRIMARY   = Color(0xFF1A1C1E)
private val LIGHT_CONTENT_SECONDARY = Color(0xFF44474F)
private val LIGHT_CONTENT_DISABLED  = Color(0xFF888888)

private val DARK_CONTENT_PRIMARY    = Color(0xFFE2E2E9)
private val DARK_CONTENT_SECONDARY  = Color(0xFFC5C6D0)
private val DARK_CONTENT_DISABLED   = Color(0xFF666870)

private val STATUS_SUCCESS_LIGHT    = Color(0xFF1D9E75)
private val STATUS_SUCCESS_DARK     = Color(0xFF5DCAA5)
private val STATUS_ERROR_LIGHT      = Color(0xFFE24B4A)
private val STATUS_ERROR_DARK       = Color(0xFFF09595)
private val STATUS_WARNING_LIGHT    = Color(0xFFEF9F27)
private val STATUS_WARNING_DARK     = Color(0xFFFAC775)

fun buildAppColorSchemeFromPrimary(primary: Color, isDark: Boolean): AppColorScheme =
    if (isDark) {
        AppColorScheme(
            backgroundPrimary      = primary.tintWithBlack(DARK_BG_TINT),
            backgroundSecondary    = primary.tintWithBlack(DARK_SEC_TINT),
            backgroundCard         = primary.tintWithBlack(DARK_CARD_TINT),
            backgroundOverlay      = Color.Black.copy(alpha = 0.6f),
            contentPrimary         = DARK_CONTENT_PRIMARY,
            contentSecondary       = DARK_CONTENT_SECONDARY,
            contentDisabled        = DARK_CONTENT_DISABLED,
            actionPrimary          = primary,
            actionPrimaryHover     = primary.copy(alpha = 0.75f),
            actionContent          = Color.White,
            actionSecondary        = primary.copy(alpha = 0.15f),
            actionSecondaryContent = primary,
            borderDefault          = primary.copy(alpha = 0.15f),
            borderFocused          = primary,
            borderStrong           = DARK_CONTENT_SECONDARY,
            statusSuccess          = STATUS_SUCCESS_DARK,
            statusSuccessSubtle    = STATUS_SUCCESS_DARK.copy(alpha = 0.15f),
            statusError            = STATUS_ERROR_DARK,
            statusErrorSubtle      = STATUS_ERROR_DARK.copy(alpha = 0.15f),
            statusWarning          = STATUS_WARNING_DARK,
            statusWarningSubtle    = STATUS_WARNING_DARK.copy(alpha = 0.15f),
        )
    } else {
        AppColorScheme(
            backgroundPrimary      = primary.tintWithWhite(LIGHT_BG_TINT),
            backgroundSecondary    = primary.tintWithWhite(LIGHT_SEC_TINT),
            backgroundCard         = Color.White,
            backgroundOverlay      = Color.Black.copy(alpha = 0.4f),
            contentPrimary         = LIGHT_CONTENT_PRIMARY,
            contentSecondary       = LIGHT_CONTENT_SECONDARY,
            contentDisabled        = LIGHT_CONTENT_DISABLED,
            actionPrimary          = primary,
            actionPrimaryHover     = primary.copy(alpha = 0.80f),
            actionContent          = Color.White,
            actionSecondary        = primary.copy(alpha = 0.12f),
            actionSecondaryContent = primary,
            borderDefault          = primary.copy(alpha = 0.12f),
            borderFocused          = primary,
            borderStrong           = LIGHT_CONTENT_SECONDARY,
            statusSuccess          = STATUS_SUCCESS_LIGHT,
            statusSuccessSubtle    = STATUS_SUCCESS_LIGHT.copy(alpha = 0.12f),
            statusError            = STATUS_ERROR_LIGHT,
            statusErrorSubtle      = STATUS_ERROR_LIGHT.copy(alpha = 0.12f),
            statusWarning          = STATUS_WARNING_LIGHT,
            statusWarningSubtle    = STATUS_WARNING_LIGHT.copy(alpha = 0.12f),
        )
    }

// ── Material3 bridge (backward compat for composables not yet migrated) ───────

fun AppColorScheme.toMaterial3ColorScheme(): ColorScheme {
    val dark = backgroundPrimary.luminance() < 0.5f
    return if (dark) darkColorScheme(
        primary              = actionPrimary,
        onPrimary            = actionContent,
        primaryContainer     = actionSecondary,
        onPrimaryContainer   = actionSecondaryContent,
        background           = backgroundPrimary,
        onBackground         = contentPrimary,
        surface              = backgroundCard,
        onSurface            = contentPrimary,
        surfaceVariant       = backgroundSecondary,
        onSurfaceVariant     = contentSecondary,
        outline              = borderDefault,
        outlineVariant       = borderDefault.copy(alpha = 0.7f),
        error                = statusError,
        onError              = Color.White,
    ) else lightColorScheme(
        primary              = actionPrimary,
        onPrimary            = actionContent,
        primaryContainer     = actionSecondary,
        onPrimaryContainer   = actionSecondaryContent,
        background           = backgroundPrimary,
        onBackground         = contentPrimary,
        surface              = backgroundCard,
        onSurface            = contentPrimary,
        surfaceVariant       = backgroundSecondary,
        onSurfaceVariant     = contentSecondary,
        outline              = borderDefault,
        outlineVariant       = borderDefault.copy(alpha = 0.7f),
        error                = statusError,
        onError              = Color.White,
    )
}

// ── Color helpers ─────────────────────────────────────────────────────────────

private fun Color.tintWithWhite(amount: Float): Color = Color(
    red   = red   * amount + 1f * (1f - amount),
    green = green * amount + 1f * (1f - amount),
    blue  = blue  * amount + 1f * (1f - amount),
    alpha = 1f,
)

private fun Color.tintWithBlack(amount: Float): Color {
    val black = Color(0xFF0A0A0A)
    return Color(
        red   = red   * amount + black.red   * (1f - amount),
        green = green * amount + black.green * (1f - amount),
        blue  = blue  * amount + black.blue  * (1f - amount),
        alpha = 1f,
    )
}
