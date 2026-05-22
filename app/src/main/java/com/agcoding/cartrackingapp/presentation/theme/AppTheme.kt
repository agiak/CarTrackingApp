package com.agcoding.cartrackingapp.presentation.theme

import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.agcoding.cartrackingapp.data.preferences.ColorPalette
import com.agcoding.cartrackingapp.shared.ui.tokens.AppColorScheme
import com.agcoding.cartrackingapp.shared.ui.tokens.AppDimens
import com.agcoding.cartrackingapp.shared.ui.tokens.AppShapes
import com.agcoding.cartrackingapp.shared.ui.tokens.AppTypography
import com.agcoding.cartrackingapp.shared.ui.tokens.buildAppColorSchemeFromPrimary
import com.agcoding.cartrackingapp.shared.ui.tokens.buildColorScheme
import com.agcoding.cartrackingapp.shared.ui.tokens.toMaterial3ColorScheme
import com.agcoding.cartrackingapp.shared.ui.tokens.brand.DefaultBrandTokens
import com.agcoding.cartrackingapp.shared.ui.tokens.brand.OceanTealBrandTokens
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppColorScheme
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppDimens

/**
 * Root theme composable. Provides [LocalAppColorScheme] and [LocalAppDimens],
 * then wraps [MaterialTheme] for backward compatibility with composables not yet
 * migrated to the token system.
 */
@Composable
fun AppTheme(
    colorPalette: ColorPalette = ColorPalette.DEFAULT_BLUE,
    isDark: Boolean,
    content: @Composable () -> Unit,
) {
    val context = LocalContext.current

    // Build AppColorScheme: BrandTokens path for default/ocean, Material3 dynamic
    // for SYSTEM, and primary-tinting for all other existing palettes.
    val appColorScheme: AppColorScheme = if (colorPalette == ColorPalette.SYSTEM) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val m3 = if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            remember(isDark) {
                AppColorScheme(
                    backgroundPrimary      = m3.background,
                    backgroundSecondary    = m3.surfaceVariant,
                    backgroundCard         = m3.surface,
                    backgroundOverlay      = Color.Black.copy(alpha = if (isDark) 0.6f else 0.4f),
                    contentPrimary         = m3.onBackground,
                    contentSecondary       = m3.onSurfaceVariant,
                    contentDisabled        = m3.onSurfaceVariant.copy(alpha = 0.5f),
                    actionPrimary          = m3.primary,
                    actionPrimaryHover     = m3.primaryContainer,
                    actionContent          = m3.onPrimary,
                    actionSecondary        = m3.primaryContainer,
                    actionSecondaryContent = m3.onPrimaryContainer,
                    borderDefault          = m3.outline,
                    borderFocused          = m3.primary,
                    borderStrong           = m3.outline,
                    statusSuccess          = if (isDark) Color(0xFF5DCAA5) else Color(0xFF1D9E75),
                    statusSuccessSubtle    = (if (isDark) Color(0xFF5DCAA5) else Color(0xFF1D9E75)).copy(alpha = 0.12f),
                    statusError            = m3.error,
                    statusErrorSubtle      = m3.error.copy(alpha = 0.12f),
                    statusWarning          = if (isDark) Color(0xFFFAC775) else Color(0xFFEF9F27),
                    statusWarningSubtle    = (if (isDark) Color(0xFFFAC775) else Color(0xFFEF9F27)).copy(alpha = 0.12f),
                )
            }
        } else {
            remember(isDark) { buildColorScheme(DefaultBrandTokens, isDark) }
        }
    } else {
        remember(colorPalette, isDark) {
            when (colorPalette) {
                ColorPalette.DEFAULT_BLUE -> buildColorScheme(DefaultBrandTokens, isDark)
                ColorPalette.OCEAN_TEAL   -> buildColorScheme(OceanTealBrandTokens, isDark)
                else -> buildAppColorSchemeFromPrimary(
                    primary = colorPalette.primaryColor(isDark),
                    isDark  = isDark,
                )
            }
        }
    }

    CompositionLocalProvider(
        LocalAppColorScheme provides appColorScheme,
        LocalAppDimens      provides AppDimens(),
    ) {
        MaterialTheme(
            colorScheme = appColorScheme.toMaterial3ColorScheme(),
            typography  = AppTypography,
            shapes      = AppShapes,
            content     = content,
        )
    }
}

// ── ColorPalette → primary color bridge ──────────────────────────────────────
// Collapses the 2500-line ColorScheme selection from the old Theme.kt into a
// single when expression keyed on the palette's primary light/dark color.

internal fun ColorPalette.primaryColor(isDark: Boolean): Color = when (this) {
    ColorPalette.SYSTEM,
    ColorPalette.DEFAULT_BLUE,
    ColorPalette.OCEAN_TEAL       -> error("BrandTokens palettes must not use primaryColor()")

    ColorPalette.SUNSET_ORANGE    -> if (isDark) SunsetOrangePalette.primaryDark    else SunsetOrangePalette.primaryLight
    ColorPalette.FOREST_GREEN     -> if (isDark) ForestGreenPalette.primaryDark     else ForestGreenPalette.primaryLight
    ColorPalette.ROYAL_PURPLE     -> if (isDark) RoyalPurplePalette.primaryDark     else RoyalPurplePalette.primaryLight
    ColorPalette.CRIMSON_RED      -> if (isDark) CrimsonRedPalette.primaryDark      else CrimsonRedPalette.primaryLight
    ColorPalette.AMBER_GOLD       -> if (isDark) AmberGoldPalette.primaryDark       else AmberGoldPalette.primaryLight
    ColorPalette.DEEP_INDIGO      -> if (isDark) DeepIndigoPalette.primaryDark      else DeepIndigoPalette.primaryLight
    ColorPalette.SLATE_GRAY       -> if (isDark) SlateGrayPalette.primaryDark       else SlateGrayPalette.primaryLight
    ColorPalette.ROSE_PINK        -> if (isDark) RosePinkPalette.primaryDark        else RosePinkPalette.primaryLight
    ColorPalette.MINT_BREEZE      -> if (isDark) MintBreezePalette.primaryDark      else MintBreezePalette.primaryLight
    ColorPalette.LAVENDER_DREAM   -> if (isDark) LavenderDreamPalette.primaryDark   else LavenderDreamPalette.primaryLight
    ColorPalette.CORAL_SUNSET     -> if (isDark) CoralSunsetPalette.primaryDark     else CoralSunsetPalette.primaryLight
    ColorPalette.EMERALD_FOREST   -> if (isDark) EmeraldForestPalette.primaryDark   else EmeraldForestPalette.primaryLight
    ColorPalette.ELECTRIC_CYAN    -> if (isDark) ElectricCyanPalette.primaryDark    else ElectricCyanPalette.primaryLight
    ColorPalette.MIDNIGHT_BLACK   -> if (isDark) MidnightBlackPalette.primaryDark   else MidnightBlackPalette.primaryLight
    ColorPalette.ICE_WHITE        -> if (isDark) IceWhitePalette.primaryDark        else IceWhitePalette.primaryLight
    ColorPalette.NEON_MAGENTA     -> if (isDark) NeonMagentaPalette.primaryDark     else NeonMagentaPalette.primaryLight
    ColorPalette.DARK_OLIVE       -> if (isDark) DarkOlivePalette.primaryDark       else DarkOlivePalette.primaryLight
    ColorPalette.VOLCANIC_ASH     -> if (isDark) VolcanicAshPalette.primaryDark     else VolcanicAshPalette.primaryLight
    ColorPalette.SUNSET_FIRE      -> if (isDark) SunsetFirePalette.primaryDark      else SunsetFirePalette.primaryLight
    ColorPalette.TROPICAL_PARADISE -> if (isDark) TropicalParadisePalette.primaryDark else TropicalParadisePalette.primaryLight
    ColorPalette.ROYAL_GOLD       -> if (isDark) RoyalGoldPalette.primaryDark       else RoyalGoldPalette.primaryLight
    ColorPalette.BERRY_BLAST      -> if (isDark) BerryBlastPalette.primaryDark      else BerryBlastPalette.primaryLight
    ColorPalette.NEON_NIGHT       -> if (isDark) NeonNightPalette.primaryDark       else NeonNightPalette.primaryLight
    ColorPalette.AUTUMN_HARVEST   -> if (isDark) AutumnHarvestPalette.primaryDark   else AutumnHarvestPalette.primaryLight
    ColorPalette.ARCTIC_FROST     -> if (isDark) ArcticFrostPalette.primaryDark     else ArcticFrostPalette.primaryLight
    ColorPalette.CHERRY_BLOSSOM   -> if (isDark) CherryBlossomPalette.primaryDark   else CherryBlossomPalette.primaryLight
    ColorPalette.EMERALD_SEA      -> if (isDark) EmeraldSeaPalette.primaryDark      else EmeraldSeaPalette.primaryLight
    ColorPalette.GOLDEN_HOUR      -> if (isDark) GoldenHourPalette.primaryDark      else GoldenHourPalette.primaryLight
    ColorPalette.NEON_LIME        -> if (isDark) NeonLimePalette.primaryDark        else NeonLimePalette.primaryLight
    ColorPalette.HOT_LAVA         -> if (isDark) HotLavaPalette.primaryDark         else HotLavaPalette.primaryLight
    ColorPalette.CYBER_PINK       -> if (isDark) CyberPinkPalette.primaryDark       else CyberPinkPalette.primaryLight
    ColorPalette.OCEAN_SUNSET     -> if (isDark) OceanSunsetPalette.primaryDark     else OceanSunsetPalette.primaryLight
    ColorPalette.FOREST_AMBER     -> if (isDark) ForestAmberPalette.primaryDark     else ForestAmberPalette.primaryLight
    ColorPalette.SAPPHIRE_ROSE    -> if (isDark) SapphireRosePalette.primaryDark    else SapphireRosePalette.primaryLight
    ColorPalette.ELECTRIC_VIOLET  -> if (isDark) ElectricVioletPalette.primaryDark  else ElectricVioletPalette.primaryLight
    ColorPalette.CANDY_CRUSH      -> if (isDark) CandyCrushPalette.primaryDark      else CandyCrushPalette.primaryLight
    ColorPalette.MIDNIGHT_SUN     -> if (isDark) MidnightSunPalette.primaryDark     else MidnightSunPalette.primaryLight
    ColorPalette.STRAWBERRY_MINT  -> if (isDark) StrawberryMintPalette.primaryDark  else StrawberryMintPalette.primaryLight
}
