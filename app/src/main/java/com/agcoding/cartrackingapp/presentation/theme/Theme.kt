package com.agcoding.cartrackingapp.presentation.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.agcoding.cartrackingapp.data.preferences.ColorPalette


// ============================================
// BACKGROUND TINT CONFIGURATION
// ============================================
// ⭐ ADJUST THIS ONE VALUE to control background color intensity across the entire app
// Higher = more colorful (closer to primary color)
// Lower = more neutral (closer to white/black)
// Recommended range: 0.15f to 0.35f

private val LIGHT_BACKGROUND_TINT = 0.2f  // 🎨 ADJUST THIS VALUE (currently 25% primary color)

// ⚙️ Auto-calculated values (DO NOT CHANGE - these are derived from LIGHT_BACKGROUND_TINT)
private val LIGHT_SURFACE_TINT = LIGHT_BACKGROUND_TINT * 0.4f     // Cards (40% of background tint = lighter)
private val LIGHT_SURFACE_VARIANT_TINT = LIGHT_BACKGROUND_TINT * 1.2f  // Elevated (120% of background = darker)

private val DARK_BACKGROUND_TINT = LIGHT_BACKGROUND_TINT * 1.0f   // Dark theme (same intensity)
private val DARK_SURFACE_TINT = DARK_BACKGROUND_TINT * 0.6f       // Dark cards (60% = lighter)
private val DARK_SURFACE_VARIANT_TINT = DARK_BACKGROUND_TINT * 1.3f   // Dark elevated (130% = darker)

// Helper functions to create tinted backgrounds
private fun Color.tintWithWhite(tintAmount: Float): Color {
    val white = Color.White
    return Color(
        red = this.red * tintAmount + white.red * (1f - tintAmount),
        green = this.green * tintAmount + white.green * (1f - tintAmount),
        blue = this.blue * tintAmount + white.blue * (1f - tintAmount),
        alpha = 1f
    )
}

private fun Color.tintWithBlack(tintAmount: Float): Color {
    val black = Color(0xFF0A0A0A) // Near black for dark theme
    return Color(
        red = this.red * tintAmount + black.red * (1f - tintAmount),
        green = this.green * tintAmount + black.green * (1f - tintAmount),
        blue = this.blue * tintAmount + black.blue * (1f - tintAmount),
        alpha = 1f
    )
}

// Default Blue Palette
private val DefaultBlueLightColorScheme = lightColorScheme(
    primary = DefaultBluePalette.primaryLight,
    onPrimary = Color.White,
    primaryContainer = DefaultBluePalette.primaryLight.copy(alpha = 0.12f),
    onPrimaryContainer = DefaultBluePalette.primaryLight,

    secondary = DefaultBluePalette.secondaryLight,
    onSecondary = Color.White,
    secondaryContainer = DefaultBluePalette.secondaryLight.copy(alpha = 0.12f),
    onSecondaryContainer = DefaultBluePalette.secondaryLight,

    tertiary = DefaultBluePalette.tertiaryLight,
    onTertiary = Color.White,
    tertiaryContainer = DefaultBluePalette.tertiaryLight.copy(alpha = 0.12f),
    onTertiaryContainer = DefaultBluePalette.tertiaryLight,

    background = DefaultBluePalette.primaryLight.tintWithWhite(LIGHT_BACKGROUND_TINT),
    onBackground = Color(0xFF1A1C1E),

    surface = DefaultBluePalette.primaryLight.tintWithWhite(LIGHT_SURFACE_TINT),
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = DefaultBluePalette.primaryLight.tintWithWhite(LIGHT_SURFACE_VARIANT_TINT),
    onSurfaceVariant = Color(0xFF42474E),

    outline = DefaultBluePalette.primaryLight.copy(alpha = 0.12f),
    outlineVariant = DefaultBluePalette.primaryLight.copy(alpha = 0.08f)
)

private val DefaultBlueDarkColorScheme = darkColorScheme(
    primary = DefaultBluePalette.primaryDark,
    onPrimary = Color(0xFF003258),
    primaryContainer = DefaultBluePalette.primaryDark.copy(alpha = 0.15f),
    onPrimaryContainer = DefaultBluePalette.primaryDark,

    secondary = DefaultBluePalette.secondaryDark,
    onSecondary = Color(0xFF003544),
    secondaryContainer = DefaultBluePalette.secondaryDark.copy(alpha = 0.15f),
    onSecondaryContainer = DefaultBluePalette.secondaryDark,

    tertiary = DefaultBluePalette.tertiaryDark,
    onTertiary = Color(0xFF003544),
    tertiaryContainer = DefaultBluePalette.tertiaryDark.copy(alpha = 0.15f),
    onTertiaryContainer = DefaultBluePalette.tertiaryDark,

    background = DefaultBluePalette.primaryDark.tintWithBlack(DARK_BACKGROUND_TINT),
    onBackground = Color(0xFFE2E2E6),

    surface = DefaultBluePalette.primaryDark.tintWithBlack(DARK_SURFACE_TINT),
    onSurface = Color(0xFFE2E2E6),
    surfaceVariant = DefaultBluePalette.primaryDark.tintWithBlack(DARK_SURFACE_VARIANT_TINT),
    onSurfaceVariant = Color(0xFFC2C7CE),

    outline = DefaultBluePalette.primaryDark.copy(alpha = 0.15f),
    outlineVariant = DefaultBluePalette.primaryDark.copy(alpha = 0.10f)
)

// Sunset Orange Palette
private val SunsetOrangeLightColorScheme = lightColorScheme(
    primary = SunsetOrangePalette.primaryLight,
    onPrimary = Color.White,
    primaryContainer = SunsetOrangePalette.primaryLight.copy(alpha = 0.12f),
    onPrimaryContainer = SunsetOrangePalette.primaryLight,

    secondary = SunsetOrangePalette.secondaryLight,
    onSecondary = Color.White,
    secondaryContainer = SunsetOrangePalette.secondaryLight.copy(alpha = 0.12f),
    onSecondaryContainer = SunsetOrangePalette.secondaryLight,

    tertiary = SunsetOrangePalette.tertiaryLight,
    onTertiary = Color.White,
    tertiaryContainer = SunsetOrangePalette.tertiaryLight.copy(alpha = 0.12f),
    onTertiaryContainer = SunsetOrangePalette.tertiaryLight,

    background = SunsetOrangePalette.primaryLight.tintWithWhite(LIGHT_BACKGROUND_TINT),
    onBackground = Color(0xFF1A1C1E),

    surface = SunsetOrangePalette.primaryLight.tintWithWhite(LIGHT_SURFACE_TINT),
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = SunsetOrangePalette.primaryLight.tintWithWhite(LIGHT_SURFACE_VARIANT_TINT),
    onSurfaceVariant = Color(0xFF4E4842),

    outline = SunsetOrangePalette.primaryLight.copy(alpha = 0.12f),
    outlineVariant = SunsetOrangePalette.primaryLight.copy(alpha = 0.08f)
)

private val SunsetOrangeDarkColorScheme = darkColorScheme(
    primary = SunsetOrangePalette.primaryDark,
    onPrimary = Color(0xFF4D2800),
    primaryContainer = SunsetOrangePalette.primaryDark.copy(alpha = 0.15f),
    onPrimaryContainer = SunsetOrangePalette.primaryDark,

    secondary = SunsetOrangePalette.secondaryDark,
    onSecondary = Color(0xFF5C3000),
    secondaryContainer = SunsetOrangePalette.secondaryDark.copy(alpha = 0.15f),
    onSecondaryContainer = SunsetOrangePalette.secondaryDark,

    tertiary = SunsetOrangePalette.tertiaryDark,
    onTertiary = Color(0xFF5C3000),
    tertiaryContainer = SunsetOrangePalette.tertiaryDark.copy(alpha = 0.15f),
    onTertiaryContainer = SunsetOrangePalette.tertiaryDark,

    background = SunsetOrangePalette.primaryDark.tintWithBlack(DARK_BACKGROUND_TINT),
    onBackground = Color(0xFFE6E1DD),

    surface = SunsetOrangePalette.primaryDark.tintWithBlack(DARK_SURFACE_TINT),
    onSurface = Color(0xFFE6E1DD),
    surfaceVariant = SunsetOrangePalette.primaryDark.tintWithBlack(DARK_SURFACE_VARIANT_TINT),
    onSurfaceVariant = Color(0xFFD0C4BA),

    outline = SunsetOrangePalette.primaryDark.copy(alpha = 0.15f),
    outlineVariant = SunsetOrangePalette.primaryDark.copy(alpha = 0.10f)
)

// Forest Green Palette
private val ForestGreenLightColorScheme = lightColorScheme(
    primary = ForestGreenPalette.primaryLight,
    onPrimary = Color.White,
    primaryContainer = ForestGreenPalette.primaryLight.copy(alpha = 0.12f),
    onPrimaryContainer = ForestGreenPalette.primaryLight,

    secondary = ForestGreenPalette.secondaryLight,
    onSecondary = Color.White,
    secondaryContainer = ForestGreenPalette.secondaryLight.copy(alpha = 0.12f),
    onSecondaryContainer = ForestGreenPalette.secondaryLight,

    tertiary = ForestGreenPalette.tertiaryLight,
    onTertiary = Color.White,
    tertiaryContainer = ForestGreenPalette.tertiaryLight.copy(alpha = 0.12f),
    onTertiaryContainer = ForestGreenPalette.tertiaryLight,

    background = ForestGreenPalette.primaryLight.tintWithWhite(LIGHT_BACKGROUND_TINT),
    onBackground = Color(0xFF1A1C1E),

    surface = ForestGreenPalette.primaryLight.tintWithWhite(LIGHT_SURFACE_TINT),
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = ForestGreenPalette.primaryLight.tintWithWhite(LIGHT_SURFACE_VARIANT_TINT),
    onSurfaceVariant = Color(0xFF3E4A42),

    outline = ForestGreenPalette.primaryLight.copy(alpha = 0.12f),
    outlineVariant = ForestGreenPalette.primaryLight.copy(alpha = 0.08f)
)

private val ForestGreenDarkColorScheme = darkColorScheme(
    primary = ForestGreenPalette.primaryDark,
    onPrimary = Color(0xFF003919),
    primaryContainer = ForestGreenPalette.primaryDark.copy(alpha = 0.15f),
    onPrimaryContainer = ForestGreenPalette.primaryDark,

    secondary = ForestGreenPalette.secondaryDark,
    onSecondary = Color(0xFF00441F),
    secondaryContainer = ForestGreenPalette.secondaryDark.copy(alpha = 0.15f),
    onSecondaryContainer = ForestGreenPalette.secondaryDark,

    tertiary = ForestGreenPalette.tertiaryDark,
    onTertiary = Color(0xFF00441F),
    tertiaryContainer = ForestGreenPalette.tertiaryDark.copy(alpha = 0.15f),
    onTertiaryContainer = ForestGreenPalette.tertiaryDark,

    background = ForestGreenPalette.primaryDark.tintWithBlack(DARK_BACKGROUND_TINT),
    onBackground = Color(0xFFE1E3E0),

    surface = ForestGreenPalette.primaryDark.tintWithBlack(DARK_SURFACE_TINT),
    onSurface = Color(0xFFE1E3E0),
    surfaceVariant = ForestGreenPalette.primaryDark.tintWithBlack(DARK_SURFACE_VARIANT_TINT),
    onSurfaceVariant = Color(0xFFC0C9C2),

    outline = ForestGreenPalette.primaryDark.copy(alpha = 0.15f),
    outlineVariant = ForestGreenPalette.primaryDark.copy(alpha = 0.10f)
)

// Royal Purple Palette
private val RoyalPurpleLightColorScheme = lightColorScheme(
    primary = RoyalPurplePalette.primaryLight,
    onPrimary = Color.White,
    primaryContainer = RoyalPurplePalette.primaryLight.copy(alpha = 0.12f),
    onPrimaryContainer = RoyalPurplePalette.primaryLight,

    secondary = RoyalPurplePalette.secondaryLight,
    onSecondary = Color.White,
    secondaryContainer = RoyalPurplePalette.secondaryLight.copy(alpha = 0.12f),
    onSecondaryContainer = RoyalPurplePalette.secondaryLight,

    tertiary = RoyalPurplePalette.tertiaryLight,
    onTertiary = Color.White,
    tertiaryContainer = RoyalPurplePalette.tertiaryLight.copy(alpha = 0.12f),
    onTertiaryContainer = RoyalPurplePalette.tertiaryLight,

    background = RoyalPurplePalette.primaryLight.tintWithWhite(LIGHT_BACKGROUND_TINT),
    onBackground = Color(0xFF1A1C1E),

    surface = RoyalPurplePalette.primaryLight.tintWithWhite(LIGHT_SURFACE_TINT),
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = RoyalPurplePalette.primaryLight.tintWithWhite(LIGHT_SURFACE_VARIANT_TINT),
    onSurfaceVariant = Color(0xFF4A424E),

    outline = RoyalPurplePalette.primaryLight.copy(alpha = 0.12f),
    outlineVariant = RoyalPurplePalette.primaryLight.copy(alpha = 0.08f)
)

private val RoyalPurpleDarkColorScheme = darkColorScheme(
    primary = RoyalPurplePalette.primaryDark,
    onPrimary = Color(0xFF3A0054),
    primaryContainer = RoyalPurplePalette.primaryDark.copy(alpha = 0.15f),
    onPrimaryContainer = RoyalPurplePalette.primaryDark,

    secondary = RoyalPurplePalette.secondaryDark,
    onSecondary = Color(0xFF4A0068),
    secondaryContainer = RoyalPurplePalette.secondaryDark.copy(alpha = 0.15f),
    onSecondaryContainer = RoyalPurplePalette.secondaryDark,

    tertiary = RoyalPurplePalette.tertiaryDark,
    onTertiary = Color(0xFF4A0068),
    tertiaryContainer = RoyalPurplePalette.tertiaryDark.copy(alpha = 0.15f),
    onTertiaryContainer = RoyalPurplePalette.tertiaryDark,

    background = RoyalPurplePalette.primaryDark.tintWithBlack(DARK_BACKGROUND_TINT),
    onBackground = Color(0xFFE5E1E3),

    surface = RoyalPurplePalette.primaryDark.tintWithBlack(DARK_SURFACE_TINT),
    onSurface = Color(0xFFE5E1E3),
    surfaceVariant = RoyalPurplePalette.primaryDark.tintWithBlack(DARK_SURFACE_VARIANT_TINT),
    onSurfaceVariant = Color(0xFFCEC0C9),

    outline = RoyalPurplePalette.primaryDark.copy(alpha = 0.15f),
    outlineVariant = RoyalPurplePalette.primaryDark.copy(alpha = 0.10f)
)

// Ocean Teal Palette
private val OceanTealLightColorScheme = lightColorScheme(
    primary = OceanTealPalette.primaryLight,
    onPrimary = Color.White,
    primaryContainer = OceanTealPalette.primaryLight.copy(alpha = 0.12f),
    onPrimaryContainer = OceanTealPalette.primaryLight,

    secondary = OceanTealPalette.secondaryLight,
    onSecondary = Color.White,
    secondaryContainer = OceanTealPalette.secondaryLight.copy(alpha = 0.12f),
    onSecondaryContainer = OceanTealPalette.secondaryLight,

    tertiary = OceanTealPalette.tertiaryLight,
    onTertiary = Color.White,
    tertiaryContainer = OceanTealPalette.tertiaryLight.copy(alpha = 0.12f),
    onTertiaryContainer = OceanTealPalette.tertiaryLight,

    background = OceanTealPalette.primaryLight.tintWithWhite(LIGHT_BACKGROUND_TINT),
    onBackground = Color(0xFF1A1C1E),

    surface = OceanTealPalette.primaryLight.tintWithWhite(LIGHT_SURFACE_TINT),
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = OceanTealPalette.primaryLight.tintWithWhite(LIGHT_SURFACE_VARIANT_TINT),
    onSurfaceVariant = Color(0xFF3E4A4C),

    outline = OceanTealPalette.primaryLight.copy(alpha = 0.12f),
    outlineVariant = OceanTealPalette.primaryLight.copy(alpha = 0.08f)
)

private val OceanTealDarkColorScheme = darkColorScheme(
    primary = OceanTealPalette.primaryDark,
    onPrimary = Color(0xFF003735),
    primaryContainer = OceanTealPalette.primaryDark.copy(alpha = 0.15f),
    onPrimaryContainer = OceanTealPalette.primaryDark,

    secondary = OceanTealPalette.secondaryDark,
    onSecondary = Color(0xFF003B3A),
    secondaryContainer = OceanTealPalette.secondaryDark.copy(alpha = 0.15f),
    onSecondaryContainer = OceanTealPalette.secondaryDark,

    tertiary = OceanTealPalette.tertiaryDark,
    onTertiary = Color(0xFF003B3A),
    tertiaryContainer = OceanTealPalette.tertiaryDark.copy(alpha = 0.15f),
    onTertiaryContainer = OceanTealPalette.tertiaryDark,

    background = OceanTealPalette.primaryDark.tintWithBlack(DARK_BACKGROUND_TINT),
    onBackground = Color(0xFFE0E3E3),

    surface = OceanTealPalette.primaryDark.tintWithBlack(DARK_SURFACE_TINT),
    onSurface = Color(0xFFE0E3E3),
    surfaceVariant = OceanTealPalette.primaryDark.tintWithBlack(DARK_SURFACE_VARIANT_TINT),
    onSurfaceVariant = Color(0xFFBFC9C9),

    outline = OceanTealPalette.primaryDark.copy(alpha = 0.15f),
    outlineVariant = OceanTealPalette.primaryDark.copy(alpha = 0.10f)
)

// Crimson Red Palette
private val CrimsonRedLightColorScheme = lightColorScheme(
    primary = CrimsonRedPalette.primaryLight,
    onPrimary = Color.White,
    primaryContainer = CrimsonRedPalette.primaryLight.copy(alpha = 0.12f),
    onPrimaryContainer = CrimsonRedPalette.primaryLight,

    secondary = CrimsonRedPalette.secondaryLight,
    onSecondary = Color.White,
    secondaryContainer = CrimsonRedPalette.secondaryLight.copy(alpha = 0.12f),
    onSecondaryContainer = CrimsonRedPalette.secondaryLight,

    tertiary = CrimsonRedPalette.tertiaryLight,
    onTertiary = Color.White,
    tertiaryContainer = CrimsonRedPalette.tertiaryLight.copy(alpha = 0.12f),
    onTertiaryContainer = CrimsonRedPalette.tertiaryLight,

    background = CrimsonRedPalette.primaryLight.tintWithWhite(LIGHT_BACKGROUND_TINT),
    onBackground = Color(0xFF1A1C1E),

    surface = CrimsonRedPalette.primaryLight.tintWithWhite(LIGHT_SURFACE_TINT),
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = CrimsonRedPalette.primaryLight.tintWithWhite(LIGHT_SURFACE_VARIANT_TINT),
    onSurfaceVariant = Color(0xFF4E4242),

    outline = CrimsonRedPalette.primaryLight.copy(alpha = 0.12f),
    outlineVariant = CrimsonRedPalette.primaryLight.copy(alpha = 0.08f)
)

private val CrimsonRedDarkColorScheme = darkColorScheme(
    primary = CrimsonRedPalette.primaryDark,
    onPrimary = Color(0xFF5C0000),
    primaryContainer = CrimsonRedPalette.primaryDark.copy(alpha = 0.15f),
    onPrimaryContainer = CrimsonRedPalette.primaryDark,

    secondary = CrimsonRedPalette.secondaryDark,
    onSecondary = Color(0xFF6B0000),
    secondaryContainer = CrimsonRedPalette.secondaryDark.copy(alpha = 0.15f),
    onSecondaryContainer = CrimsonRedPalette.secondaryDark,

    tertiary = CrimsonRedPalette.tertiaryDark,
    onTertiary = Color(0xFF6B0000),
    tertiaryContainer = CrimsonRedPalette.tertiaryDark.copy(alpha = 0.15f),
    onTertiaryContainer = CrimsonRedPalette.tertiaryDark,

    background = CrimsonRedPalette.primaryDark.tintWithBlack(DARK_BACKGROUND_TINT),
    onBackground = Color(0xFFE6DDDD),

    surface = CrimsonRedPalette.primaryDark.tintWithBlack(DARK_SURFACE_TINT),
    onSurface = Color(0xFFE6DDDD),
    surfaceVariant = CrimsonRedPalette.primaryDark.tintWithBlack(DARK_SURFACE_VARIANT_TINT),
    onSurfaceVariant = Color(0xFFD0BABA),

    outline = CrimsonRedPalette.primaryDark.copy(alpha = 0.15f),
    outlineVariant = CrimsonRedPalette.primaryDark.copy(alpha = 0.10f)
)

// Amber Gold Palette
private val AmberGoldLightColorScheme = lightColorScheme(
    primary = AmberGoldPalette.primaryLight,
    onPrimary = Color.White,
    primaryContainer = AmberGoldPalette.primaryLight.copy(alpha = 0.12f),
    onPrimaryContainer = AmberGoldPalette.primaryLight,

    secondary = AmberGoldPalette.secondaryLight,
    onSecondary = Color.White,
    secondaryContainer = AmberGoldPalette.secondaryLight.copy(alpha = 0.12f),
    onSecondaryContainer = AmberGoldPalette.secondaryLight,

    tertiary = AmberGoldPalette.tertiaryLight,
    onTertiary = Color.White,
    tertiaryContainer = AmberGoldPalette.tertiaryLight.copy(alpha = 0.12f),
    onTertiaryContainer = AmberGoldPalette.tertiaryLight,

    background = AmberGoldPalette.primaryLight.tintWithWhite(LIGHT_BACKGROUND_TINT),
    onBackground = Color(0xFF1A1C1E),

    surface = AmberGoldPalette.primaryLight.tintWithWhite(LIGHT_SURFACE_TINT),
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = AmberGoldPalette.primaryLight.tintWithWhite(LIGHT_SURFACE_VARIANT_TINT),
    onSurfaceVariant = Color(0xFF4E4A42),

    outline = AmberGoldPalette.primaryLight.copy(alpha = 0.12f),
    outlineVariant = AmberGoldPalette.primaryLight.copy(alpha = 0.08f)
)

private val AmberGoldDarkColorScheme = darkColorScheme(
    primary = AmberGoldPalette.primaryDark,
    onPrimary = Color(0xFF5C3800),
    primaryContainer = AmberGoldPalette.primaryDark.copy(alpha = 0.15f),
    onPrimaryContainer = AmberGoldPalette.primaryDark,

    secondary = AmberGoldPalette.secondaryDark,
    onSecondary = Color(0xFF6B4000),
    secondaryContainer = AmberGoldPalette.secondaryDark.copy(alpha = 0.15f),
    onSecondaryContainer = AmberGoldPalette.secondaryDark,

    tertiary = AmberGoldPalette.tertiaryDark,
    onTertiary = Color(0xFF6B4000),
    tertiaryContainer = AmberGoldPalette.tertiaryDark.copy(alpha = 0.15f),
    onTertiaryContainer = AmberGoldPalette.tertiaryDark,

    background = AmberGoldPalette.primaryDark.tintWithBlack(DARK_BACKGROUND_TINT),
    onBackground = Color(0xFFE6E1DD),

    surface = AmberGoldPalette.primaryDark.tintWithBlack(DARK_SURFACE_TINT),
    onSurface = Color(0xFFE6E1DD),
    surfaceVariant = AmberGoldPalette.primaryDark.tintWithBlack(DARK_SURFACE_VARIANT_TINT),
    onSurfaceVariant = Color(0xFFD0C8BA),

    outline = AmberGoldPalette.primaryDark.copy(alpha = 0.15f),
    outlineVariant = AmberGoldPalette.primaryDark.copy(alpha = 0.10f)
)

// Deep Indigo Palette
private val DeepIndigoLightColorScheme = lightColorScheme(
    primary = DeepIndigoPalette.primaryLight,
    onPrimary = Color.White,
    primaryContainer = DeepIndigoPalette.primaryLight.copy(alpha = 0.12f),
    onPrimaryContainer = DeepIndigoPalette.primaryLight,

    secondary = DeepIndigoPalette.secondaryLight,
    onSecondary = Color.White,
    secondaryContainer = DeepIndigoPalette.secondaryLight.copy(alpha = 0.12f),
    onSecondaryContainer = DeepIndigoPalette.secondaryLight,

    tertiary = DeepIndigoPalette.tertiaryLight,
    onTertiary = Color.White,
    tertiaryContainer = DeepIndigoPalette.tertiaryLight.copy(alpha = 0.12f),
    onTertiaryContainer = DeepIndigoPalette.tertiaryLight,

    background = DeepIndigoPalette.primaryLight.tintWithWhite(LIGHT_BACKGROUND_TINT),
    onBackground = Color(0xFF1A1C1E),

    surface = DeepIndigoPalette.primaryLight.tintWithWhite(LIGHT_SURFACE_TINT),
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = DeepIndigoPalette.primaryLight.tintWithWhite(LIGHT_SURFACE_VARIANT_TINT),
    onSurfaceVariant = Color(0xFF42444E),

    outline = DeepIndigoPalette.primaryLight.copy(alpha = 0.12f),
    outlineVariant = DeepIndigoPalette.primaryLight.copy(alpha = 0.08f)
)

private val DeepIndigoDarkColorScheme = darkColorScheme(
    primary = DeepIndigoPalette.primaryDark,
    onPrimary = Color(0xFF001E3C),
    primaryContainer = DeepIndigoPalette.primaryDark.copy(alpha = 0.15f),
    onPrimaryContainer = DeepIndigoPalette.primaryDark,

    secondary = DeepIndigoPalette.secondaryDark,
    onSecondary = Color(0xFF00244A),
    secondaryContainer = DeepIndigoPalette.secondaryDark.copy(alpha = 0.15f),
    onSecondaryContainer = DeepIndigoPalette.secondaryDark,

    tertiary = DeepIndigoPalette.tertiaryDark,
    onTertiary = Color(0xFF00244A),
    tertiaryContainer = DeepIndigoPalette.tertiaryDark.copy(alpha = 0.15f),
    onTertiaryContainer = DeepIndigoPalette.tertiaryDark,

    background = DeepIndigoPalette.primaryDark.tintWithBlack(DARK_BACKGROUND_TINT),
    onBackground = Color(0xFFE1E2E6),

    surface = DeepIndigoPalette.primaryDark.tintWithBlack(DARK_SURFACE_TINT),
    onSurface = Color(0xFFE1E2E6),
    surfaceVariant = DeepIndigoPalette.primaryDark.tintWithBlack(DARK_SURFACE_VARIANT_TINT),
    onSurfaceVariant = Color(0xFFC0C4CE),

    outline = DeepIndigoPalette.primaryDark.copy(alpha = 0.15f),
    outlineVariant = DeepIndigoPalette.primaryDark.copy(alpha = 0.10f)
)

// Slate Gray Palette
private val SlateGrayLightColorScheme = lightColorScheme(
    primary = SlateGrayPalette.primaryLight,
    onPrimary = Color.White,
    primaryContainer = SlateGrayPalette.primaryLight.copy(alpha = 0.12f),
    onPrimaryContainer = SlateGrayPalette.primaryLight,

    secondary = SlateGrayPalette.secondaryLight,
    onSecondary = Color.White,
    secondaryContainer = SlateGrayPalette.secondaryLight.copy(alpha = 0.12f),
    onSecondaryContainer = SlateGrayPalette.secondaryLight,

    tertiary = SlateGrayPalette.tertiaryLight,
    onTertiary = Color.White,
    tertiaryContainer = SlateGrayPalette.tertiaryLight.copy(alpha = 0.12f),
    onTertiaryContainer = SlateGrayPalette.tertiaryLight,

    background = SlateGrayPalette.primaryLight.tintWithWhite(LIGHT_BACKGROUND_TINT),
    onBackground = Color(0xFF1A1C1E),

    surface = SlateGrayPalette.primaryLight.tintWithWhite(LIGHT_SURFACE_TINT),
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = SlateGrayPalette.primaryLight.tintWithWhite(LIGHT_SURFACE_VARIANT_TINT),
    onSurfaceVariant = Color(0xFF42464E),

    outline = SlateGrayPalette.primaryLight.copy(alpha = 0.12f),
    outlineVariant = SlateGrayPalette.primaryLight.copy(alpha = 0.08f)
)

private val SlateGrayDarkColorScheme = darkColorScheme(
    primary = SlateGrayPalette.primaryDark,
    onPrimary = Color(0xFF1C2428),
    primaryContainer = SlateGrayPalette.primaryDark.copy(alpha = 0.15f),
    onPrimaryContainer = SlateGrayPalette.primaryDark,

    secondary = SlateGrayPalette.secondaryDark,
    onSecondary = Color(0xFF242C32),
    secondaryContainer = SlateGrayPalette.secondaryDark.copy(alpha = 0.15f),
    onSecondaryContainer = SlateGrayPalette.secondaryDark,

    tertiary = SlateGrayPalette.tertiaryDark,
    onTertiary = Color(0xFF242C32),
    tertiaryContainer = SlateGrayPalette.tertiaryDark.copy(alpha = 0.15f),
    onTertiaryContainer = SlateGrayPalette.tertiaryDark,

    background = SlateGrayPalette.primaryDark.tintWithBlack(DARK_BACKGROUND_TINT),
    onBackground = Color(0xFFE2E3E5),

    surface = SlateGrayPalette.primaryDark.tintWithBlack(DARK_SURFACE_TINT),
    onSurface = Color(0xFFE2E3E5),
    surfaceVariant = SlateGrayPalette.primaryDark.tintWithBlack(DARK_SURFACE_VARIANT_TINT),
    onSurfaceVariant = Color(0xFFC2C6CC),

    outline = SlateGrayPalette.primaryDark.copy(alpha = 0.15f),
    outlineVariant = SlateGrayPalette.primaryDark.copy(alpha = 0.10f)
)

// Rose Pink Palette
private val RosePinkLightColorScheme = lightColorScheme(
    primary = RosePinkPalette.primaryLight,
    onPrimary = Color.White,
    primaryContainer = RosePinkPalette.primaryLight.copy(alpha = 0.12f),
    onPrimaryContainer = RosePinkPalette.primaryLight,

    secondary = RosePinkPalette.secondaryLight,
    onSecondary = Color.White,
    secondaryContainer = RosePinkPalette.secondaryLight.copy(alpha = 0.12f),
    onSecondaryContainer = RosePinkPalette.secondaryLight,

    tertiary = RosePinkPalette.tertiaryLight,
    onTertiary = Color.White,
    tertiaryContainer = RosePinkPalette.tertiaryLight.copy(alpha = 0.12f),
    onTertiaryContainer = RosePinkPalette.tertiaryLight,

    background = RosePinkPalette.primaryLight.tintWithWhite(LIGHT_BACKGROUND_TINT),
    onBackground = Color(0xFF1A1C1E),

    surface = RosePinkPalette.primaryLight.tintWithWhite(LIGHT_SURFACE_TINT),
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = RosePinkPalette.primaryLight.tintWithWhite(LIGHT_SURFACE_VARIANT_TINT),
    onSurfaceVariant = Color(0xFF4E4244),

    outline = RosePinkPalette.primaryLight.copy(alpha = 0.12f),
    outlineVariant = RosePinkPalette.primaryLight.copy(alpha = 0.08f)
)

private val RosePinkDarkColorScheme = darkColorScheme(
    primary = RosePinkPalette.primaryDark,
    onPrimary = Color(0xFF4C0032),
    primaryContainer = RosePinkPalette.primaryDark.copy(alpha = 0.15f),
    onPrimaryContainer = RosePinkPalette.primaryDark,

    secondary = RosePinkPalette.secondaryDark,
    onSecondary = Color(0xFF5C003E),
    secondaryContainer = RosePinkPalette.secondaryDark.copy(alpha = 0.15f),
    onSecondaryContainer = RosePinkPalette.secondaryDark,

    tertiary = RosePinkPalette.tertiaryDark,
    onTertiary = Color(0xFF5C003E),
    tertiaryContainer = RosePinkPalette.tertiaryDark.copy(alpha = 0.15f),
    onTertiaryContainer = RosePinkPalette.tertiaryDark,

    background = RosePinkPalette.primaryDark.tintWithBlack(DARK_BACKGROUND_TINT),
    onBackground = Color(0xFFE5E1E2),

    surface = RosePinkPalette.primaryDark.tintWithBlack(DARK_SURFACE_TINT),
    onSurface = Color(0xFFE5E1E2),
    surfaceVariant = RosePinkPalette.primaryDark.tintWithBlack(DARK_SURFACE_VARIANT_TINT),
    onSurfaceVariant = Color(0xFFCEC0C4),

    outline = RosePinkPalette.primaryDark.copy(alpha = 0.15f),
    outlineVariant = RosePinkPalette.primaryDark.copy(alpha = 0.10f)
)

// Mint Breeze Palette
private val MintBreezeLightColorScheme = lightColorScheme(
    primary = MintBreezePalette.primaryLight,
    onPrimary = Color.White,
    primaryContainer = MintBreezePalette.primaryLight.copy(alpha = 0.12f),
    onPrimaryContainer = MintBreezePalette.primaryLight,

    secondary = MintBreezePalette.secondaryLight,
    onSecondary = Color.White,
    secondaryContainer = MintBreezePalette.secondaryLight.copy(alpha = 0.12f),
    onSecondaryContainer = MintBreezePalette.secondaryLight,

    tertiary = MintBreezePalette.tertiaryLight,
    onTertiary = Color.White,
    tertiaryContainer = MintBreezePalette.tertiaryLight.copy(alpha = 0.12f),
    onTertiaryContainer = MintBreezePalette.tertiaryLight,

    background = MintBreezePalette.primaryLight
        .tintWithWhite(LIGHT_BACKGROUND_TINT),
    onBackground = Color(0xFF1A1C1E),

    surface = MintBreezePalette.primaryLight
        .tintWithWhite(LIGHT_SURFACE_TINT),
    onSurface = Color(0xFF1A1C1E),

    surfaceVariant = MintBreezePalette.primaryLight
        .tintWithWhite(LIGHT_SURFACE_VARIANT_TINT),
    onSurfaceVariant = Color(0xFF3E4947),

    outline = MintBreezePalette.primaryLight.copy(alpha = 0.12f),
    outlineVariant = MintBreezePalette.primaryLight.copy(alpha = 0.08f)
)

private val MintBreezeDarkColorScheme = darkColorScheme(
    primary = MintBreezePalette.primaryDark,
    onPrimary = Color(0xFF003731),
    primaryContainer = MintBreezePalette.primaryDark.copy(alpha = 0.15f),
    onPrimaryContainer = MintBreezePalette.primaryDark,

    secondary = MintBreezePalette.secondaryDark,
    onSecondary = Color(0xFF003D38),
    secondaryContainer = MintBreezePalette.secondaryDark.copy(alpha = 0.15f),
    onSecondaryContainer = MintBreezePalette.secondaryDark,

    tertiary = MintBreezePalette.tertiaryDark,
    onTertiary = Color(0xFF003D38),
    tertiaryContainer = MintBreezePalette.tertiaryDark.copy(alpha = 0.15f),
    onTertiaryContainer = MintBreezePalette.tertiaryDark,

    background = MintBreezePalette.primaryDark
        .tintWithBlack(DARK_BACKGROUND_TINT),
    onBackground = Color(0xFFE1E7E5),

    surface = MintBreezePalette.primaryDark
        .tintWithBlack(DARK_SURFACE_TINT),
    onSurface = Color(0xFFE1E7E5),

    surfaceVariant = MintBreezePalette.primaryDark
        .tintWithBlack(DARK_SURFACE_VARIANT_TINT),
    onSurfaceVariant = Color(0xFFBEC8C5),

    outline = MintBreezePalette.primaryDark.copy(alpha = 0.15f),
    outlineVariant = MintBreezePalette.primaryDark.copy(alpha = 0.10f)
)

// Lavender Dream Palette
private val LavenderDreamLightColorScheme = lightColorScheme(
    primary = LavenderDreamPalette.primaryLight,
    onPrimary = Color.White,
    primaryContainer = LavenderDreamPalette.primaryLight.copy(alpha = 0.12f),
    onPrimaryContainer = LavenderDreamPalette.primaryLight,

    secondary = LavenderDreamPalette.secondaryLight,
    onSecondary = Color.White,
    secondaryContainer = LavenderDreamPalette.secondaryLight.copy(alpha = 0.12f),
    onSecondaryContainer = LavenderDreamPalette.secondaryLight,

    tertiary = LavenderDreamPalette.tertiaryLight,
    onTertiary = Color.White,
    tertiaryContainer = LavenderDreamPalette.tertiaryLight.copy(alpha = 0.12f),
    onTertiaryContainer = LavenderDreamPalette.tertiaryLight,

    background = LavenderDreamPalette.primaryLight
        .tintWithWhite(LIGHT_BACKGROUND_TINT),
    onBackground = Color(0xFF1A1C1E),

    surface = LavenderDreamPalette.primaryLight
        .tintWithWhite(LIGHT_SURFACE_TINT),
    onSurface = Color(0xFF1A1C1E),

    surfaceVariant = LavenderDreamPalette.primaryLight
        .tintWithWhite(LIGHT_SURFACE_VARIANT_TINT),
    onSurfaceVariant = Color(0xFF4A4149),

    outline = LavenderDreamPalette.primaryLight.copy(alpha = 0.12f),
    outlineVariant = LavenderDreamPalette.primaryLight.copy(alpha = 0.08f)
)

private val LavenderDreamDarkColorScheme = darkColorScheme(
    primary = LavenderDreamPalette.primaryDark,
    onPrimary = Color(0xFF3A0049),
    primaryContainer = LavenderDreamPalette.primaryDark.copy(alpha = 0.15f),
    onPrimaryContainer = LavenderDreamPalette.primaryDark,

    secondary = LavenderDreamPalette.secondaryDark,
    onSecondary = Color(0xFF4A0056),
    secondaryContainer = LavenderDreamPalette.secondaryDark.copy(alpha = 0.15f),
    onSecondaryContainer = LavenderDreamPalette.secondaryDark,

    tertiary = LavenderDreamPalette.tertiaryDark,
    onTertiary = Color(0xFF4A0056),
    tertiaryContainer = LavenderDreamPalette.tertiaryDark.copy(alpha = 0.15f),
    onTertiaryContainer = LavenderDreamPalette.tertiaryDark,

    background = LavenderDreamPalette.primaryDark
        .tintWithBlack(DARK_BACKGROUND_TINT),
    onBackground = Color(0xFFE5E1E7),

    surface = LavenderDreamPalette.primaryDark
        .tintWithBlack(DARK_SURFACE_TINT),
    onSurface = Color(0xFFE5E1E7),

    surfaceVariant = LavenderDreamPalette.primaryDark
        .tintWithBlack(DARK_SURFACE_VARIANT_TINT),
    onSurfaceVariant = Color(0xFFC8C0CE),

    outline = LavenderDreamPalette.primaryDark.copy(alpha = 0.15f),
    outlineVariant = LavenderDreamPalette.primaryDark.copy(alpha = 0.10f)
)

// Coral Sunset Palette
private val CoralSunsetLightColorScheme = lightColorScheme(
    primary = CoralSunsetPalette.primaryLight,
    onPrimary = Color.White,
    primaryContainer = CoralSunsetPalette.primaryLight.copy(alpha = 0.12f),
    onPrimaryContainer = CoralSunsetPalette.primaryLight,

    secondary = CoralSunsetPalette.secondaryLight,
    onSecondary = Color.White,
    secondaryContainer = CoralSunsetPalette.secondaryLight.copy(alpha = 0.12f),
    onSecondaryContainer = CoralSunsetPalette.secondaryLight,

    tertiary = CoralSunsetPalette.tertiaryLight,
    onTertiary = Color.White,
    tertiaryContainer = CoralSunsetPalette.tertiaryLight.copy(alpha = 0.12f),
    onTertiaryContainer = CoralSunsetPalette.tertiaryLight,

    background = CoralSunsetPalette.primaryLight
        .tintWithWhite(LIGHT_BACKGROUND_TINT),
    onBackground = Color(0xFF1A1C1E),

    surface = CoralSunsetPalette.primaryLight
        .tintWithWhite(LIGHT_SURFACE_TINT),
    onSurface = Color(0xFF1A1C1E),

    surfaceVariant = CoralSunsetPalette.primaryLight
        .tintWithWhite(LIGHT_SURFACE_VARIANT_TINT),
    onSurfaceVariant = Color(0xFF4E4442),

    outline = CoralSunsetPalette.primaryLight.copy(alpha = 0.12f),
    outlineVariant = CoralSunsetPalette.primaryLight.copy(alpha = 0.08f)
)

private val CoralSunsetDarkColorScheme = darkColorScheme(
    primary = CoralSunsetPalette.primaryDark,
    onPrimary = Color(0xFF5C1800),
    primaryContainer = CoralSunsetPalette.primaryDark.copy(alpha = 0.15f),
    onPrimaryContainer = CoralSunsetPalette.primaryDark,

    secondary = CoralSunsetPalette.secondaryDark,
    onSecondary = Color(0xFF6C1F00),
    secondaryContainer = CoralSunsetPalette.secondaryDark.copy(alpha = 0.15f),
    onSecondaryContainer = CoralSunsetPalette.secondaryDark,

    tertiary = CoralSunsetPalette.tertiaryDark,
    onTertiary = Color(0xFF6C1F00),
    tertiaryContainer = CoralSunsetPalette.tertiaryDark.copy(alpha = 0.15f),
    onTertiaryContainer = CoralSunsetPalette.tertiaryDark,

    background = CoralSunsetPalette.primaryDark
        .tintWithBlack(DARK_BACKGROUND_TINT),
    onBackground = Color(0xFFE5E2E1),

    surface = CoralSunsetPalette.primaryDark
        .tintWithBlack(DARK_SURFACE_TINT),
    onSurface = Color(0xFFE5E2E1),

    surfaceVariant = CoralSunsetPalette.primaryDark
        .tintWithBlack(DARK_SURFACE_VARIANT_TINT),
    onSurfaceVariant = Color(0xFFCEC4C0),

    outline = CoralSunsetPalette.primaryDark.copy(alpha = 0.15f),
    outlineVariant = CoralSunsetPalette.primaryDark.copy(alpha = 0.10f)
)

// Emerald Forest Palette
private val EmeraldForestLightColorScheme = lightColorScheme(
    primary = EmeraldForestPalette.primaryLight,
    onPrimary = Color.White,
    primaryContainer = EmeraldForestPalette.primaryLight.copy(alpha = 0.12f),
    onPrimaryContainer = EmeraldForestPalette.primaryLight,

    secondary = EmeraldForestPalette.secondaryLight,
    onSecondary = Color.White,
    secondaryContainer = EmeraldForestPalette.secondaryLight.copy(alpha = 0.12f),
    onSecondaryContainer = EmeraldForestPalette.secondaryLight,

    tertiary = EmeraldForestPalette.tertiaryLight,
    onTertiary = Color.White,
    tertiaryContainer = EmeraldForestPalette.tertiaryLight.copy(alpha = 0.12f),
    onTertiaryContainer = EmeraldForestPalette.tertiaryLight,

    background = EmeraldForestPalette.primaryLight
        .tintWithWhite(LIGHT_BACKGROUND_TINT),
    onBackground = Color(0xFF1A1C1E),

    surface = EmeraldForestPalette.primaryLight
        .tintWithWhite(LIGHT_SURFACE_TINT),
    onSurface = Color(0xFF1A1C1E),

    surfaceVariant = EmeraldForestPalette.primaryLight
        .tintWithWhite(LIGHT_SURFACE_VARIANT_TINT),
    onSurfaceVariant = Color(0xFF3E4A40),

    outline = EmeraldForestPalette.primaryLight.copy(alpha = 0.12f),
    outlineVariant = EmeraldForestPalette.primaryLight.copy(alpha = 0.08f)
)

private val EmeraldForestDarkColorScheme = darkColorScheme(
    primary = EmeraldForestPalette.primaryDark,
    onPrimary = Color(0xFF00330C),
    primaryContainer = EmeraldForestPalette.primaryDark.copy(alpha = 0.15f),
    onPrimaryContainer = EmeraldForestPalette.primaryDark,

    secondary = EmeraldForestPalette.secondaryDark,
    onSecondary = Color(0xFF003912),
    secondaryContainer = EmeraldForestPalette.secondaryDark.copy(alpha = 0.15f),
    onSecondaryContainer = EmeraldForestPalette.secondaryDark,

    tertiary = EmeraldForestPalette.tertiaryDark,
    onTertiary = Color(0xFF003912),
    tertiaryContainer = EmeraldForestPalette.tertiaryDark.copy(alpha = 0.15f),
    onTertiaryContainer = EmeraldForestPalette.tertiaryDark,

    background = EmeraldForestPalette.primaryDark
        .tintWithBlack(DARK_BACKGROUND_TINT),
    onBackground = Color(0xFFE1E7E3),

    surface = EmeraldForestPalette.primaryDark
        .tintWithBlack(DARK_SURFACE_TINT),
    onSurface = Color(0xFFE1E7E3),

    surfaceVariant = EmeraldForestPalette.primaryDark
        .tintWithBlack(DARK_SURFACE_VARIANT_TINT),
    onSurfaceVariant = Color(0xFFBEC8C2),

    outline = EmeraldForestPalette.primaryDark.copy(alpha = 0.15f),
    outlineVariant = EmeraldForestPalette.primaryDark.copy(alpha = 0.10f)
)

// Electric Cyan Palette
private val ElectricCyanLightColorScheme = lightColorScheme(
    primary = ElectricCyanPalette.primaryLight,
    onPrimary = Color.White,
    primaryContainer = ElectricCyanPalette.primaryLight.copy(alpha = 0.12f),
    onPrimaryContainer = ElectricCyanPalette.primaryLight,

    secondary = ElectricCyanPalette.secondaryLight,
    onSecondary = Color.White,
    secondaryContainer = ElectricCyanPalette.secondaryLight.copy(alpha = 0.12f),
    onSecondaryContainer = ElectricCyanPalette.secondaryLight,

    tertiary = ElectricCyanPalette.tertiaryLight,
    onTertiary = Color.White,
    tertiaryContainer = ElectricCyanPalette.tertiaryLight.copy(alpha = 0.12f),
    onTertiaryContainer = ElectricCyanPalette.tertiaryLight,

    background = ElectricCyanPalette.primaryLight
        .tintWithWhite(LIGHT_BACKGROUND_TINT),
    onBackground = Color(0xFF1A1C1E),

    surface = ElectricCyanPalette.primaryLight
        .tintWithWhite(LIGHT_SURFACE_TINT),
    onSurface = Color(0xFF1A1C1E),

    surfaceVariant = ElectricCyanPalette.primaryLight
        .tintWithWhite(LIGHT_SURFACE_VARIANT_TINT),
    onSurfaceVariant = Color(0xFF3E4A4D),

    outline = ElectricCyanPalette.primaryLight.copy(alpha = 0.12f),
    outlineVariant = ElectricCyanPalette.primaryLight.copy(alpha = 0.08f)
)

private val ElectricCyanDarkColorScheme = darkColorScheme(
    primary = ElectricCyanPalette.primaryDark,
    onPrimary = Color(0xFF004450),
    primaryContainer = ElectricCyanPalette.primaryDark.copy(alpha = 0.15f),
    onPrimaryContainer = ElectricCyanPalette.primaryDark,

    secondary = ElectricCyanPalette.secondaryDark,
    onSecondary = Color(0xFF00515C),
    secondaryContainer = ElectricCyanPalette.secondaryDark.copy(alpha = 0.15f),
    onSecondaryContainer = ElectricCyanPalette.secondaryDark,

    tertiary = ElectricCyanPalette.tertiaryDark,
    onTertiary = Color(0xFF00515C),
    tertiaryContainer = ElectricCyanPalette.tertiaryDark.copy(alpha = 0.15f),
    onTertiaryContainer = ElectricCyanPalette.tertiaryDark,

    background = ElectricCyanPalette.primaryDark
        .tintWithBlack(DARK_BACKGROUND_TINT),
    onBackground = Color(0xFFE1E6E7),

    surface = ElectricCyanPalette.primaryDark
        .tintWithBlack(DARK_SURFACE_TINT),
    onSurface = Color(0xFFE1E6E7),

    surfaceVariant = ElectricCyanPalette.primaryDark
        .tintWithBlack(DARK_SURFACE_VARIANT_TINT),
    onSurfaceVariant = Color(0xFFBEC6C8),

    outline = ElectricCyanPalette.primaryDark.copy(alpha = 0.15f),
    outlineVariant = ElectricCyanPalette.primaryDark.copy(alpha = 0.10f)
)

// Midnight Black Palette
private val MidnightBlackLightColorScheme = lightColorScheme(
    primary = MidnightBlackPalette.primaryLight,
    onPrimary = Color.White,
    primaryContainer = MidnightBlackPalette.primaryLight.copy(alpha = 0.12f),
    onPrimaryContainer = MidnightBlackPalette.primaryLight,

    secondary = MidnightBlackPalette.secondaryLight,
    onSecondary = Color.White,
    secondaryContainer = MidnightBlackPalette.secondaryLight.copy(alpha = 0.12f),
    onSecondaryContainer = MidnightBlackPalette.secondaryLight,

    tertiary = MidnightBlackPalette.tertiaryLight,
    onTertiary = Color.White,
    tertiaryContainer = MidnightBlackPalette.tertiaryLight.copy(alpha = 0.12f),
    onTertiaryContainer = MidnightBlackPalette.tertiaryLight,

    background = MidnightBlackPalette.primaryLight
        .tintWithWhite(LIGHT_BACKGROUND_TINT),
    onBackground = Color(0xFF1A1C1E),

    surface = MidnightBlackPalette.primaryLight
        .tintWithWhite(LIGHT_SURFACE_TINT),
    onSurface = Color(0xFF1A1C1E),

    surfaceVariant = MidnightBlackPalette.primaryLight
        .tintWithWhite(LIGHT_SURFACE_VARIANT_TINT),
    onSurfaceVariant = Color(0xFF424242),

    outline = MidnightBlackPalette.primaryLight.copy(alpha = 0.12f),
    outlineVariant = MidnightBlackPalette.primaryLight.copy(alpha = 0.08f)
)

private val MidnightBlackDarkColorScheme = darkColorScheme(
    primary = MidnightBlackPalette.primaryDark,
    onPrimary = Color(0xFF000000),
    primaryContainer = MidnightBlackPalette.primaryDark.copy(alpha = 0.15f),
    onPrimaryContainer = MidnightBlackPalette.primaryDark,

    secondary = MidnightBlackPalette.secondaryDark,
    onSecondary = Color(0xFF121212),
    secondaryContainer = MidnightBlackPalette.secondaryDark.copy(alpha = 0.15f),
    onSecondaryContainer = MidnightBlackPalette.secondaryDark,

    tertiary = MidnightBlackPalette.tertiaryDark,
    onTertiary = Color(0xFF121212),
    tertiaryContainer = MidnightBlackPalette.tertiaryDark.copy(alpha = 0.15f),
    onTertiaryContainer = MidnightBlackPalette.tertiaryDark,

    background = MidnightBlackPalette.primaryDark
        .tintWithBlack(DARK_BACKGROUND_TINT),
    onBackground = Color(0xFFE5E5E5),

    surface = MidnightBlackPalette.primaryDark
        .tintWithBlack(DARK_SURFACE_TINT),
    onSurface = Color(0xFFE5E5E5),

    surfaceVariant = MidnightBlackPalette.primaryDark
        .tintWithBlack(DARK_SURFACE_VARIANT_TINT),
    onSurfaceVariant = Color(0xFFC8C8C8),

    outline = MidnightBlackPalette.primaryDark.copy(alpha = 0.15f),
    outlineVariant = MidnightBlackPalette.primaryDark.copy(alpha = 0.10f)
)

// Ice White Palette
private val IceWhiteLightColorScheme = lightColorScheme(
    primary = IceWhitePalette.primaryLight,
    onPrimary = Color.White,
    primaryContainer = IceWhitePalette.primaryLight.copy(alpha = 0.12f),
    onPrimaryContainer = IceWhitePalette.primaryLight,

    secondary = IceWhitePalette.secondaryLight,
    onSecondary = Color.White,
    secondaryContainer = IceWhitePalette.secondaryLight.copy(alpha = 0.12f),
    onSecondaryContainer = IceWhitePalette.secondaryLight,

    tertiary = IceWhitePalette.tertiaryLight,
    onTertiary = Color.White,
    tertiaryContainer = IceWhitePalette.tertiaryLight.copy(alpha = 0.12f),
    onTertiaryContainer = IceWhitePalette.tertiaryLight,

    background = IceWhitePalette.primaryLight
        .tintWithWhite(LIGHT_BACKGROUND_TINT),
    onBackground = Color(0xFF1A1C1E),

    surface = IceWhitePalette.primaryLight
        .tintWithWhite(LIGHT_SURFACE_TINT),
    onSurface = Color(0xFF1A1C1E),

    surfaceVariant = IceWhitePalette.primaryLight
        .tintWithWhite(LIGHT_SURFACE_VARIANT_TINT),
    onSurfaceVariant = Color(0xFF3E4447),

    outline = IceWhitePalette.primaryLight.copy(alpha = 0.12f),
    outlineVariant = IceWhitePalette.primaryLight.copy(alpha = 0.08f)
)

private val IceWhiteDarkColorScheme = darkColorScheme(
    primary = IceWhitePalette.primaryDark,
    onPrimary = Color(0xFF1C2428),
    primaryContainer = IceWhitePalette.primaryDark.copy(alpha = 0.15f),
    onPrimaryContainer = IceWhitePalette.primaryDark,

    secondary = IceWhitePalette.secondaryDark,
    onSecondary = Color(0xFF242C32),
    secondaryContainer = IceWhitePalette.secondaryDark.copy(alpha = 0.15f),
    onSecondaryContainer = IceWhitePalette.secondaryDark,

    tertiary = IceWhitePalette.tertiaryDark,
    onTertiary = Color(0xFF242C32),
    tertiaryContainer = IceWhitePalette.tertiaryDark.copy(alpha = 0.15f),
    onTertiaryContainer = IceWhitePalette.tertiaryDark,

    background = IceWhitePalette.primaryDark
        .tintWithBlack(DARK_BACKGROUND_TINT),
    onBackground = Color(0xFFE8EAEB),

    surface = IceWhitePalette.primaryDark
        .tintWithBlack(DARK_SURFACE_TINT),
    onSurface = Color(0xFFE8EAEB),

    surfaceVariant = IceWhitePalette.primaryDark
        .tintWithBlack(DARK_SURFACE_VARIANT_TINT),
    onSurfaceVariant = Color(0xFFCFD3D6),

    outline = IceWhitePalette.primaryDark.copy(alpha = 0.15f),
    outlineVariant = IceWhitePalette.primaryDark.copy(alpha = 0.10f)
)

// Neon Magenta Palette
private val NeonMagentaLightColorScheme = lightColorScheme(
    primary = NeonMagentaPalette.primaryLight,
    onPrimary = Color.White,
    primaryContainer = NeonMagentaPalette.primaryLight.copy(alpha = 0.12f),
    onPrimaryContainer = NeonMagentaPalette.primaryLight,

    secondary = NeonMagentaPalette.secondaryLight,
    onSecondary = Color.White,
    secondaryContainer = NeonMagentaPalette.secondaryLight.copy(alpha = 0.12f),
    onSecondaryContainer = NeonMagentaPalette.secondaryLight,

    tertiary = NeonMagentaPalette.tertiaryLight,
    onTertiary = Color.White,
    tertiaryContainer = NeonMagentaPalette.tertiaryLight.copy(alpha = 0.12f),
    onTertiaryContainer = NeonMagentaPalette.tertiaryLight,

    background = NeonMagentaPalette.primaryLight
        .tintWithWhite(LIGHT_BACKGROUND_TINT),
    onBackground = Color(0xFF1A1C1E),

    surface = NeonMagentaPalette.primaryLight
        .tintWithWhite(LIGHT_SURFACE_TINT),
    onSurface = Color(0xFF1A1C1E),

    surfaceVariant = NeonMagentaPalette.primaryLight
        .tintWithWhite(LIGHT_SURFACE_VARIANT_TINT),
    onSurfaceVariant = Color(0xFF4E4244),

    outline = NeonMagentaPalette.primaryLight.copy(alpha = 0.12f),
    outlineVariant = NeonMagentaPalette.primaryLight.copy(alpha = 0.08f)
)

private val NeonMagentaDarkColorScheme = darkColorScheme(
    primary = NeonMagentaPalette.primaryDark,
    onPrimary = Color(0xFF5C0032),
    primaryContainer = NeonMagentaPalette.primaryDark.copy(alpha = 0.15f),
    onPrimaryContainer = NeonMagentaPalette.primaryDark,

    secondary = NeonMagentaPalette.secondaryDark,
    onSecondary = Color(0xFF6C003E),
    secondaryContainer = NeonMagentaPalette.secondaryDark.copy(alpha = 0.15f),
    onSecondaryContainer = NeonMagentaPalette.secondaryDark,

    tertiary = NeonMagentaPalette.tertiaryDark,
    onTertiary = Color(0xFF6C003E),
    tertiaryContainer = NeonMagentaPalette.tertiaryDark.copy(alpha = 0.15f),
    onTertiaryContainer = NeonMagentaPalette.tertiaryDark,

    background = NeonMagentaPalette.primaryDark
        .tintWithBlack(DARK_BACKGROUND_TINT),
    onBackground = Color(0xFFE5E1E3),

    surface = NeonMagentaPalette.primaryDark
        .tintWithBlack(DARK_SURFACE_TINT),
    onSurface = Color(0xFFE5E1E3),

    surfaceVariant = NeonMagentaPalette.primaryDark
        .tintWithBlack(DARK_SURFACE_VARIANT_TINT),
    onSurfaceVariant = Color(0xFFCEC0C6),

    outline = NeonMagentaPalette.primaryDark.copy(alpha = 0.15f),
    outlineVariant = NeonMagentaPalette.primaryDark.copy(alpha = 0.10f)
)

// Dark Olive Palette
private val DarkOliveLightColorScheme = lightColorScheme(
    primary = DarkOlivePalette.primaryLight,
    onPrimary = Color.White,
    primaryContainer = DarkOlivePalette.primaryLight.copy(alpha = 0.12f),
    onPrimaryContainer = DarkOlivePalette.primaryLight,

    secondary = DarkOlivePalette.secondaryLight,
    onSecondary = Color.White,
    secondaryContainer = DarkOlivePalette.secondaryLight.copy(alpha = 0.12f),
    onSecondaryContainer = DarkOlivePalette.secondaryLight,

    tertiary = DarkOlivePalette.tertiaryLight,
    onTertiary = Color.White,
    tertiaryContainer = DarkOlivePalette.tertiaryLight.copy(alpha = 0.12f),
    onTertiaryContainer = DarkOlivePalette.tertiaryLight,

    background = DarkOlivePalette.primaryLight
        .tintWithWhite(LIGHT_BACKGROUND_TINT),
    onBackground = Color(0xFF1A1C1E),

    surface = DarkOlivePalette.primaryLight
        .tintWithWhite(LIGHT_SURFACE_TINT),
    onSurface = Color(0xFF1A1C1E),

    surfaceVariant = DarkOlivePalette.primaryLight
        .tintWithWhite(LIGHT_SURFACE_VARIANT_TINT),
    onSurfaceVariant = Color(0xFF3E4A3C),

    outline = DarkOlivePalette.primaryLight.copy(alpha = 0.12f),
    outlineVariant = DarkOlivePalette.primaryLight.copy(alpha = 0.08f)
)

private val DarkOliveDarkColorScheme = darkColorScheme(
    primary = DarkOlivePalette.primaryDark,
    onPrimary = Color(0xFF1A3300),
    primaryContainer = DarkOlivePalette.primaryDark.copy(alpha = 0.15f),
    onPrimaryContainer = DarkOlivePalette.primaryDark,

    secondary = DarkOlivePalette.secondaryDark,
    onSecondary = Color(0xFF243800),
    secondaryContainer = DarkOlivePalette.secondaryDark.copy(alpha = 0.15f),
    onSecondaryContainer = DarkOlivePalette.secondaryDark,

    tertiary = DarkOlivePalette.tertiaryDark,
    onTertiary = Color(0xFF243800),
    tertiaryContainer = DarkOlivePalette.tertiaryDark.copy(alpha = 0.15f),
    onTertiaryContainer = DarkOlivePalette.tertiaryDark,

    background = DarkOlivePalette.primaryDark
        .tintWithBlack(DARK_BACKGROUND_TINT),
    onBackground = Color(0xFFE3E7E1),

    surface = DarkOlivePalette.primaryDark
        .tintWithBlack(DARK_SURFACE_TINT),
    onSurface = Color(0xFFE3E7E1),

    surfaceVariant = DarkOlivePalette.primaryDark
        .tintWithBlack(DARK_SURFACE_VARIANT_TINT),
    onSurfaceVariant = Color(0xFFC0C8C2),

    outline = DarkOlivePalette.primaryDark.copy(alpha = 0.15f),
    outlineVariant = DarkOlivePalette.primaryDark.copy(alpha = 0.10f)
)

// Volcanic Ash Palette
private val VolcanicAshLightColorScheme = lightColorScheme(
    primary = VolcanicAshPalette.primaryLight,
    onPrimary = Color.White,
    primaryContainer = VolcanicAshPalette.primaryLight.copy(alpha = 0.12f),
    onPrimaryContainer = VolcanicAshPalette.primaryLight,

    secondary = VolcanicAshPalette.secondaryLight,
    onSecondary = Color.White,
    secondaryContainer = VolcanicAshPalette.secondaryLight.copy(alpha = 0.12f),
    onSecondaryContainer = VolcanicAshPalette.secondaryLight,

    tertiary = VolcanicAshPalette.tertiaryLight,
    onTertiary = Color.White,
    tertiaryContainer = VolcanicAshPalette.tertiaryLight.copy(alpha = 0.12f),
    onTertiaryContainer = VolcanicAshPalette.tertiaryLight,

    background = VolcanicAshPalette.primaryLight
        .tintWithWhite(LIGHT_BACKGROUND_TINT),
    onBackground = Color(0xFF1A1C1E),

    surface = VolcanicAshPalette.primaryLight
        .tintWithWhite(LIGHT_SURFACE_TINT),
    onSurface = Color(0xFF1A1C1E),

    surfaceVariant = VolcanicAshPalette.primaryLight
        .tintWithWhite(LIGHT_SURFACE_VARIANT_TINT),
    onSurfaceVariant = Color(0xFF3E4447),

    outline = VolcanicAshPalette.primaryLight.copy(alpha = 0.12f),
    outlineVariant = VolcanicAshPalette.primaryLight.copy(alpha = 0.08f)
)

private val VolcanicAshDarkColorScheme = darkColorScheme(
    primary = VolcanicAshPalette.primaryDark,
    onPrimary = Color(0xFF0C1214),
    primaryContainer = VolcanicAshPalette.primaryDark.copy(alpha = 0.15f),
    onPrimaryContainer = VolcanicAshPalette.primaryDark,

    secondary = VolcanicAshPalette.secondaryDark,
    onSecondary = Color(0xFF141C20),
    secondaryContainer = VolcanicAshPalette.secondaryDark.copy(alpha = 0.15f),
    onSecondaryContainer = VolcanicAshPalette.secondaryDark,

    tertiary = VolcanicAshPalette.tertiaryDark,
    onTertiary = Color(0xFF141C20),
    tertiaryContainer = VolcanicAshPalette.tertiaryDark.copy(alpha = 0.15f),
    onTertiaryContainer = VolcanicAshPalette.tertiaryDark,

    background = VolcanicAshPalette.primaryDark
        .tintWithBlack(DARK_BACKGROUND_TINT),
    onBackground = Color(0xFFE5E7E9),

    surface = VolcanicAshPalette.primaryDark
        .tintWithBlack(DARK_SURFACE_TINT),
    onSurface = Color(0xFFE5E7E9),

    surfaceVariant = VolcanicAshPalette.primaryDark
        .tintWithBlack(DARK_SURFACE_VARIANT_TINT),
    onSurfaceVariant = Color(0xFFC5C9CB),

    outline = VolcanicAshPalette.primaryDark.copy(alpha = 0.15f),
    outlineVariant = VolcanicAshPalette.primaryDark.copy(alpha = 0.10f)
)

// ============================================================================
// NEW HIGH CONTRAST COLOR SCHEMES
// ============================================================================

// Sunset Fire Palette
private val SunsetFireLightColorScheme = lightColorScheme(
    primary = SunsetFirePalette.primaryLight,
    onPrimary = Color.White,
    primaryContainer = SunsetFirePalette.primaryLight.copy(alpha = 0.12f),
    onPrimaryContainer = SunsetFirePalette.primaryLight,

    secondary = SunsetFirePalette.secondaryLight,
    onSecondary = Color.White,
    secondaryContainer = SunsetFirePalette.secondaryLight.copy(alpha = 0.12f),
    onSecondaryContainer = SunsetFirePalette.secondaryLight,

    tertiary = SunsetFirePalette.tertiaryLight,
    onTertiary = Color.White,
    tertiaryContainer = SunsetFirePalette.tertiaryLight.copy(alpha = 0.12f),
    onTertiaryContainer = SunsetFirePalette.tertiaryLight,

    background = SunsetFirePalette.primaryLight.tintWithWhite(LIGHT_BACKGROUND_TINT),
    onBackground = Color(0xFF1A1C1E),

    surface = SunsetFirePalette.primaryLight.tintWithWhite(LIGHT_SURFACE_TINT),
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = SunsetFirePalette.primaryLight.tintWithWhite(LIGHT_SURFACE_VARIANT_TINT),
    onSurfaceVariant = Color(0xFF4A424E),

    outline = SunsetFirePalette.primaryLight.copy(alpha = 0.12f),
    outlineVariant = SunsetFirePalette.primaryLight.copy(alpha = 0.08f)
)

private val SunsetFireDarkColorScheme = darkColorScheme(
    primary = SunsetFirePalette.primaryDark,
    onPrimary = Color(0xFF4A2000),
    primaryContainer = SunsetFirePalette.primaryDark.copy(alpha = 0.15f),
    onPrimaryContainer = SunsetFirePalette.primaryDark,

    secondary = SunsetFirePalette.secondaryDark,
    onSecondary = Color(0xFF1C0038),
    secondaryContainer = SunsetFirePalette.secondaryDark.copy(alpha = 0.15f),
    onSecondaryContainer = SunsetFirePalette.secondaryDark,

    tertiary = SunsetFirePalette.tertiaryDark,
    onTertiary = Color(0xFF4A2800),
    tertiaryContainer = SunsetFirePalette.tertiaryDark.copy(alpha = 0.15f),
    onTertiaryContainer = SunsetFirePalette.tertiaryDark,

    background = SunsetFirePalette.primaryDark.tintWithBlack(DARK_BACKGROUND_TINT),
    onBackground = Color(0xFFE8E2E5),

    surface = SunsetFirePalette.primaryDark.tintWithBlack(DARK_SURFACE_TINT),
    onSurface = Color(0xFFE8E2E5),
    surfaceVariant = SunsetFirePalette.primaryDark.tintWithBlack(DARK_SURFACE_VARIANT_TINT),
    onSurfaceVariant = Color(0xFFD0C5C8),

    outline = SunsetFirePalette.primaryDark.copy(alpha = 0.15f),
    outlineVariant = SunsetFirePalette.primaryDark.copy(alpha = 0.10f)
)

// Tropical Paradise Palette
private val TropicalParadiseLightColorScheme = lightColorScheme(
    primary = TropicalParadisePalette.primaryLight,
    onPrimary = Color.White,
    primaryContainer = TropicalParadisePalette.primaryLight.copy(alpha = 0.12f),
    onPrimaryContainer = TropicalParadisePalette.primaryLight,

    secondary = TropicalParadisePalette.secondaryLight,
    onSecondary = Color.White,
    secondaryContainer = TropicalParadisePalette.secondaryLight.copy(alpha = 0.12f),
    onSecondaryContainer = TropicalParadisePalette.secondaryLight,

    tertiary = TropicalParadisePalette.tertiaryLight,
    onTertiary = Color.White,
    tertiaryContainer = TropicalParadisePalette.tertiaryLight.copy(alpha = 0.12f),
    onTertiaryContainer = TropicalParadisePalette.tertiaryLight,

    background = TropicalParadisePalette.primaryLight.tintWithWhite(LIGHT_BACKGROUND_TINT),
    onBackground = Color(0xFF1A1C1E),

    surface = TropicalParadisePalette.primaryLight.tintWithWhite(LIGHT_SURFACE_TINT),
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = TropicalParadisePalette.primaryLight.tintWithWhite(LIGHT_SURFACE_VARIANT_TINT),
    onSurfaceVariant = Color(0xFF3E4A48),

    outline = TropicalParadisePalette.primaryLight.copy(alpha = 0.12f),
    outlineVariant = TropicalParadisePalette.primaryLight.copy(alpha = 0.08f)
)

private val TropicalParadiseDarkColorScheme = darkColorScheme(
    primary = TropicalParadisePalette.primaryDark,
    onPrimary = Color(0xFF003830),
    primaryContainer = TropicalParadisePalette.primaryDark.copy(alpha = 0.15f),
    onPrimaryContainer = TropicalParadisePalette.primaryDark,

    secondary = TropicalParadisePalette.secondaryDark,
    onSecondary = Color(0xFF4A0020),
    secondaryContainer = TropicalParadisePalette.secondaryDark.copy(alpha = 0.15f),
    onSecondaryContainer = TropicalParadisePalette.secondaryDark,

    tertiary = TropicalParadisePalette.tertiaryDark,
    onTertiary = Color(0xFF004550),
    tertiaryContainer = TropicalParadisePalette.tertiaryDark.copy(alpha = 0.15f),
    onTertiaryContainer = TropicalParadisePalette.tertiaryDark,

    background = TropicalParadisePalette.primaryDark.tintWithBlack(DARK_BACKGROUND_TINT),
    onBackground = Color(0xFFE2E8E6),

    surface = TropicalParadisePalette.primaryDark.tintWithBlack(DARK_SURFACE_TINT),
    onSurface = Color(0xFFE2E8E6),
    surfaceVariant = TropicalParadisePalette.primaryDark.tintWithBlack(DARK_SURFACE_VARIANT_TINT),
    onSurfaceVariant = Color(0xFFC0CCC8),

    outline = TropicalParadisePalette.primaryDark.copy(alpha = 0.15f),
    outlineVariant = TropicalParadisePalette.primaryDark.copy(alpha = 0.10f)
)

// Royal Gold Palette
private val RoyalGoldLightColorScheme = lightColorScheme(
    primary = RoyalGoldPalette.primaryLight,
    onPrimary = Color.White,
    primaryContainer = RoyalGoldPalette.primaryLight.copy(alpha = 0.12f),
    onPrimaryContainer = RoyalGoldPalette.primaryLight,

    secondary = RoyalGoldPalette.secondaryLight,
    onSecondary = Color.White,
    secondaryContainer = RoyalGoldPalette.secondaryLight.copy(alpha = 0.12f),
    onSecondaryContainer = RoyalGoldPalette.secondaryLight,

    tertiary = RoyalGoldPalette.tertiaryLight,
    onTertiary = Color.White,
    tertiaryContainer = RoyalGoldPalette.tertiaryLight.copy(alpha = 0.12f),
    onTertiaryContainer = RoyalGoldPalette.tertiaryLight,

    background = RoyalGoldPalette.primaryLight.tintWithWhite(LIGHT_BACKGROUND_TINT),
    onBackground = Color(0xFF1A1C1E),

    surface = RoyalGoldPalette.primaryLight.tintWithWhite(LIGHT_SURFACE_TINT),
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = RoyalGoldPalette.primaryLight.tintWithWhite(LIGHT_SURFACE_VARIANT_TINT),
    onSurfaceVariant = Color(0xFF4A4230),

    outline = RoyalGoldPalette.primaryLight.copy(alpha = 0.12f),
    outlineVariant = RoyalGoldPalette.primaryLight.copy(alpha = 0.08f)
)

private val RoyalGoldDarkColorScheme = darkColorScheme(
    primary = RoyalGoldPalette.primaryDark,
    onPrimary = Color(0xFF3E2800),
    primaryContainer = RoyalGoldPalette.primaryDark.copy(alpha = 0.15f),
    onPrimaryContainer = RoyalGoldPalette.primaryDark,

    secondary = RoyalGoldPalette.secondaryDark,
    onSecondary = Color(0xFF001838),
    secondaryContainer = RoyalGoldPalette.secondaryDark.copy(alpha = 0.15f),
    onSecondaryContainer = RoyalGoldPalette.secondaryDark,

    tertiary = RoyalGoldPalette.tertiaryDark,
    onTertiary = Color(0xFF3E2800),
    tertiaryContainer = RoyalGoldPalette.tertiaryDark.copy(alpha = 0.15f),
    onTertiaryContainer = RoyalGoldPalette.tertiaryDark,

    background = RoyalGoldPalette.primaryDark.tintWithBlack(DARK_BACKGROUND_TINT),
    onBackground = Color(0xFFE8E6E2),

    surface = RoyalGoldPalette.primaryDark.tintWithBlack(DARK_SURFACE_TINT),
    onSurface = Color(0xFFE8E6E2),
    surfaceVariant = RoyalGoldPalette.primaryDark.tintWithBlack(DARK_SURFACE_VARIANT_TINT),
    onSurfaceVariant = Color(0xFFD0CCC0),

    outline = RoyalGoldPalette.primaryDark.copy(alpha = 0.15f),
    outlineVariant = RoyalGoldPalette.primaryDark.copy(alpha = 0.10f)
)

// Berry Blast Palette
private val BerryBlastLightColorScheme = lightColorScheme(
    primary = BerryBlastPalette.primaryLight,
    onPrimary = Color.White,
    primaryContainer = BerryBlastPalette.primaryLight.copy(alpha = 0.12f),
    onPrimaryContainer = BerryBlastPalette.primaryLight,

    secondary = BerryBlastPalette.secondaryLight,
    onSecondary = Color.White,
    secondaryContainer = BerryBlastPalette.secondaryLight.copy(alpha = 0.12f),
    onSecondaryContainer = BerryBlastPalette.secondaryLight,

    tertiary = BerryBlastPalette.tertiaryLight,
    onTertiary = Color.White,
    tertiaryContainer = BerryBlastPalette.tertiaryLight.copy(alpha = 0.12f),
    onTertiaryContainer = BerryBlastPalette.tertiaryLight,

    background = Color(0xFFFCF7FB),
    onBackground = Color(0xFF1A1C1E),

    surface = Color(0xFFFDFAFC),
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = Color(0xFFF8FBF0),
    onSurfaceVariant = Color(0xFF4A424E),

    outline = BerryBlastPalette.primaryLight.copy(alpha = 0.12f),
    outlineVariant = BerryBlastPalette.primaryLight.copy(alpha = 0.08f)
)

private val BerryBlastDarkColorScheme = darkColorScheme(
    primary = BerryBlastPalette.primaryDark,
    onPrimary = Color(0xFF3A0054),
    primaryContainer = BerryBlastPalette.primaryDark.copy(alpha = 0.15f),
    onPrimaryContainer = BerryBlastPalette.primaryDark,

    secondary = BerryBlastPalette.secondaryDark,
    onSecondary = Color(0xFF2E3800),
    secondaryContainer = BerryBlastPalette.secondaryDark.copy(alpha = 0.15f),
    onSecondaryContainer = BerryBlastPalette.secondaryDark,

    tertiary = BerryBlastPalette.tertiaryDark,
    onTertiary = Color(0xFF4A0068),
    tertiaryContainer = BerryBlastPalette.tertiaryDark.copy(alpha = 0.15f),
    onTertiaryContainer = BerryBlastPalette.tertiaryDark,

    background = BerryBlastPalette.primaryDark.tintWithBlack(DARK_BACKGROUND_TINT),
    onBackground = Color(0xFFE5E1E3),

    surface = BerryBlastPalette.primaryDark.tintWithBlack(DARK_SURFACE_TINT),
    onSurface = Color(0xFFE5E1E3),
    surfaceVariant = BerryBlastPalette.primaryDark.tintWithBlack(DARK_SURFACE_VARIANT_TINT),
    onSurfaceVariant = Color(0xFFCEC0C9),

    outline = BerryBlastPalette.primaryDark.copy(alpha = 0.15f),
    outlineVariant = BerryBlastPalette.primaryDark.copy(alpha = 0.10f)
)

// Neon Night Palette
private val NeonNightLightColorScheme = lightColorScheme(
    primary = NeonNightPalette.primaryLight,
    onPrimary = Color.White,
    primaryContainer = NeonNightPalette.primaryLight.copy(alpha = 0.12f),
    onPrimaryContainer = NeonNightPalette.primaryLight,

    secondary = NeonNightPalette.secondaryLight,
    onSecondary = Color.White,
    secondaryContainer = NeonNightPalette.secondaryLight.copy(alpha = 0.12f),
    onSecondaryContainer = NeonNightPalette.secondaryLight,

    tertiary = NeonNightPalette.tertiaryLight,
    onTertiary = Color.White,
    tertiaryContainer = NeonNightPalette.tertiaryLight.copy(alpha = 0.12f),
    onTertiaryContainer = NeonNightPalette.tertiaryLight,

    background = NeonNightPalette.primaryLight.tintWithWhite(LIGHT_BACKGROUND_TINT),
    onBackground = Color(0xFF1A1C1E),

    surface = NeonNightPalette.primaryLight.tintWithWhite(LIGHT_SURFACE_TINT),
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = NeonNightPalette.primaryLight.tintWithWhite(LIGHT_SURFACE_VARIANT_TINT),
    onSurfaceVariant = Color(0xFF3E4450),

    outline = NeonNightPalette.primaryLight.copy(alpha = 0.12f),
    outlineVariant = NeonNightPalette.primaryLight.copy(alpha = 0.08f)
)

private val NeonNightDarkColorScheme = darkColorScheme(
    primary = NeonNightPalette.primaryDark,
    onPrimary = Color(0xFF003045),
    primaryContainer = NeonNightPalette.primaryDark.copy(alpha = 0.15f),
    onPrimaryContainer = NeonNightPalette.primaryDark,

    secondary = NeonNightPalette.secondaryDark,
    onSecondary = Color(0xFF42004A),
    secondaryContainer = NeonNightPalette.secondaryDark.copy(alpha = 0.15f),
    onSecondaryContainer = NeonNightPalette.secondaryDark,

    tertiary = NeonNightPalette.tertiaryDark,
    onTertiary = Color(0xFF003842),
    tertiaryContainer = NeonNightPalette.tertiaryDark.copy(alpha = 0.15f),
    onTertiaryContainer = NeonNightPalette.tertiaryDark,

    background = NeonNightPalette.primaryDark.tintWithBlack(DARK_BACKGROUND_TINT),
    onBackground = Color(0xFFE2E6E8),

    surface = NeonNightPalette.primaryDark.tintWithBlack(DARK_SURFACE_TINT),
    onSurface = Color(0xFFE2E6E8),
    surfaceVariant = NeonNightPalette.primaryDark.tintWithBlack(DARK_SURFACE_VARIANT_TINT),
    onSurfaceVariant = Color(0xFFC0C8D0),

    outline = NeonNightPalette.primaryDark.copy(alpha = 0.15f),
    outlineVariant = NeonNightPalette.primaryDark.copy(alpha = 0.10f)
)

// Autumn Harvest Palette
private val AutumnHarvestLightColorScheme = lightColorScheme(
    primary = AutumnHarvestPalette.primaryLight,
    onPrimary = Color.White,
    primaryContainer = AutumnHarvestPalette.primaryLight.copy(alpha = 0.12f),
    onPrimaryContainer = AutumnHarvestPalette.primaryLight,

    secondary = AutumnHarvestPalette.secondaryLight,
    onSecondary = Color.White,
    secondaryContainer = AutumnHarvestPalette.secondaryLight.copy(alpha = 0.12f),
    onSecondaryContainer = AutumnHarvestPalette.secondaryLight,

    tertiary = AutumnHarvestPalette.tertiaryLight,
    onTertiary = Color.White,
    tertiaryContainer = AutumnHarvestPalette.tertiaryLight.copy(alpha = 0.12f),
    onTertiaryContainer = AutumnHarvestPalette.tertiaryLight,

    background = AutumnHarvestPalette.primaryLight.tintWithWhite(LIGHT_BACKGROUND_TINT),
    onBackground = Color(0xFF1A1C1E),

    surface = AutumnHarvestPalette.primaryLight.tintWithWhite(LIGHT_SURFACE_TINT),
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = AutumnHarvestPalette.primaryLight.tintWithWhite(LIGHT_SURFACE_VARIANT_TINT),
    onSurfaceVariant = Color(0xFF4A3E38),

    outline = AutumnHarvestPalette.primaryLight.copy(alpha = 0.12f),
    outlineVariant = AutumnHarvestPalette.primaryLight.copy(alpha = 0.08f)
)

private val AutumnHarvestDarkColorScheme = darkColorScheme(
    primary = AutumnHarvestPalette.primaryDark,
    onPrimary = Color(0xFF3E1800),
    primaryContainer = AutumnHarvestPalette.primaryDark.copy(alpha = 0.15f),
    onPrimaryContainer = AutumnHarvestPalette.primaryDark,

    secondary = AutumnHarvestPalette.secondaryDark,
    onSecondary = Color(0xFF1C1410),
    secondaryContainer = AutumnHarvestPalette.secondaryDark.copy(alpha = 0.15f),
    onSecondaryContainer = AutumnHarvestPalette.secondaryDark,

    tertiary = AutumnHarvestPalette.tertiaryDark,
    onTertiary = Color(0xFF3E2000),
    tertiaryContainer = AutumnHarvestPalette.tertiaryDark.copy(alpha = 0.15f),
    onTertiaryContainer = AutumnHarvestPalette.tertiaryDark,

    background = AutumnHarvestPalette.primaryDark.tintWithBlack(DARK_BACKGROUND_TINT),
    onBackground = Color(0xFFE8E4E2),

    surface = AutumnHarvestPalette.primaryDark.tintWithBlack(DARK_SURFACE_TINT),
    onSurface = Color(0xFFE8E4E2),
    surfaceVariant = AutumnHarvestPalette.primaryDark.tintWithBlack(DARK_SURFACE_VARIANT_TINT),
    onSurfaceVariant = Color(0xFFD0C8C0),

    outline = AutumnHarvestPalette.primaryDark.copy(alpha = 0.15f),
    outlineVariant = AutumnHarvestPalette.primaryDark.copy(alpha = 0.10f)
)

// Arctic Frost Palette
private val ArcticFrostLightColorScheme = lightColorScheme(
    primary = ArcticFrostPalette.primaryLight,
    onPrimary = Color.White,
    primaryContainer = ArcticFrostPalette.primaryLight.copy(alpha = 0.12f),
    onPrimaryContainer = ArcticFrostPalette.primaryLight,

    secondary = ArcticFrostPalette.secondaryLight,
    onSecondary = Color.White,
    secondaryContainer = ArcticFrostPalette.secondaryLight.copy(alpha = 0.12f),
    onSecondaryContainer = ArcticFrostPalette.secondaryLight,

    tertiary = ArcticFrostPalette.tertiaryLight,
    onTertiary = Color.White,
    tertiaryContainer = ArcticFrostPalette.tertiaryLight.copy(alpha = 0.12f),
    onTertiaryContainer = ArcticFrostPalette.tertiaryLight,

    background = ArcticFrostPalette.primaryLight.tintWithWhite(LIGHT_BACKGROUND_TINT),
    onBackground = Color(0xFF1A1C1E),

    surface = ArcticFrostPalette.primaryLight.tintWithWhite(LIGHT_SURFACE_TINT),
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = ArcticFrostPalette.primaryLight.tintWithWhite(LIGHT_SURFACE_VARIANT_TINT),
    onSurfaceVariant = Color(0xFF3E4648),

    outline = ArcticFrostPalette.primaryLight.copy(alpha = 0.12f),
    outlineVariant = ArcticFrostPalette.primaryLight.copy(alpha = 0.08f)
)

private val ArcticFrostDarkColorScheme = darkColorScheme(
    primary = ArcticFrostPalette.primaryDark,
    onPrimary = Color(0xFF002025),
    primaryContainer = ArcticFrostPalette.primaryDark.copy(alpha = 0.15f),
    onPrimaryContainer = ArcticFrostPalette.primaryDark,

    secondary = ArcticFrostPalette.secondaryDark,
    onSecondary = Color(0xFF002838),
    secondaryContainer = ArcticFrostPalette.secondaryDark.copy(alpha = 0.15f),
    onSecondaryContainer = ArcticFrostPalette.secondaryDark,

    tertiary = ArcticFrostPalette.tertiaryDark,
    onTertiary = Color(0xFF002830),
    tertiaryContainer = ArcticFrostPalette.tertiaryDark.copy(alpha = 0.15f),
    onTertiaryContainer = ArcticFrostPalette.tertiaryDark,

    background = ArcticFrostPalette.primaryDark.tintWithBlack(DARK_BACKGROUND_TINT),
    onBackground = Color(0xFFE2E7E8),

    surface = ArcticFrostPalette.primaryDark.tintWithBlack(DARK_SURFACE_TINT),
    onSurface = Color(0xFFE2E7E8),
    surfaceVariant = ArcticFrostPalette.primaryDark.tintWithBlack(DARK_SURFACE_VARIANT_TINT),
    onSurfaceVariant = Color(0xFFC0CCD0),

    outline = ArcticFrostPalette.primaryDark.copy(alpha = 0.15f),
    outlineVariant = ArcticFrostPalette.primaryDark.copy(alpha = 0.10f)
)

// Cherry Blossom Palette
private val CherryBlossomLightColorScheme = lightColorScheme(
    primary = CherryBlossomPalette.primaryLight,
    onPrimary = Color.White,
    primaryContainer = CherryBlossomPalette.primaryLight.copy(alpha = 0.12f),
    onPrimaryContainer = CherryBlossomPalette.primaryLight,

    secondary = CherryBlossomPalette.secondaryLight,
    onSecondary = Color.White,
    secondaryContainer = CherryBlossomPalette.secondaryLight.copy(alpha = 0.12f),
    onSecondaryContainer = CherryBlossomPalette.secondaryLight,

    tertiary = CherryBlossomPalette.tertiaryLight,
    onTertiary = Color.White,
    tertiaryContainer = CherryBlossomPalette.tertiaryLight.copy(alpha = 0.12f),
    onTertiaryContainer = CherryBlossomPalette.tertiaryLight,

    background = CherryBlossomPalette.primaryLight.tintWithWhite(LIGHT_BACKGROUND_TINT),
    onBackground = Color(0xFF1A1C1E),

    surface = CherryBlossomPalette.primaryLight.tintWithWhite(LIGHT_SURFACE_TINT),
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = CherryBlossomPalette.primaryLight.tintWithWhite(LIGHT_SURFACE_VARIANT_TINT),
    onSurfaceVariant = Color(0xFF4A3E40),

    outline = CherryBlossomPalette.primaryLight.copy(alpha = 0.12f),
    outlineVariant = CherryBlossomPalette.primaryLight.copy(alpha = 0.08f)
)

private val CherryBlossomDarkColorScheme = darkColorScheme(
    primary = CherryBlossomPalette.primaryDark,
    onPrimary = Color(0xFF3E0010),
    primaryContainer = CherryBlossomPalette.primaryDark.copy(alpha = 0.15f),
    onPrimaryContainer = CherryBlossomPalette.primaryDark,

    secondary = CherryBlossomPalette.secondaryDark,
    onSecondary = Color(0xFF42001C),
    secondaryContainer = CherryBlossomPalette.secondaryDark.copy(alpha = 0.15f),
    onSecondaryContainer = CherryBlossomPalette.secondaryDark,

    tertiary = CherryBlossomPalette.tertiaryDark,
    onTertiary = Color(0xFF3E0010),
    tertiaryContainer = CherryBlossomPalette.tertiaryDark.copy(alpha = 0.15f),
    onTertiaryContainer = CherryBlossomPalette.tertiaryDark,

    background = CherryBlossomPalette.primaryDark.tintWithBlack(DARK_BACKGROUND_TINT),
    onBackground = Color(0xFFE8E2E4),

    surface = CherryBlossomPalette.primaryDark.tintWithBlack(DARK_SURFACE_TINT),
    onSurface = Color(0xFFE8E2E4),
    surfaceVariant = CherryBlossomPalette.primaryDark.tintWithBlack(DARK_SURFACE_VARIANT_TINT),
    onSurfaceVariant = Color(0xFFD0C0C4),

    outline = CherryBlossomPalette.primaryDark.copy(alpha = 0.15f),
    outlineVariant = CherryBlossomPalette.primaryDark.copy(alpha = 0.10f)
)

// Emerald Sea Palette
private val EmeraldSeaLightColorScheme = lightColorScheme(
    primary = EmeraldSeaPalette.primaryLight,
    onPrimary = Color.White,
    primaryContainer = EmeraldSeaPalette.primaryLight.copy(alpha = 0.12f),
    onPrimaryContainer = EmeraldSeaPalette.primaryLight,

    secondary = EmeraldSeaPalette.secondaryLight,
    onSecondary = Color.White,
    secondaryContainer = EmeraldSeaPalette.secondaryLight.copy(alpha = 0.12f),
    onSecondaryContainer = EmeraldSeaPalette.secondaryLight,

    tertiary = EmeraldSeaPalette.tertiaryLight,
    onTertiary = Color.White,
    tertiaryContainer = EmeraldSeaPalette.tertiaryLight.copy(alpha = 0.12f),
    onTertiaryContainer = EmeraldSeaPalette.tertiaryLight,

    background = EmeraldSeaPalette.primaryLight.tintWithWhite(LIGHT_BACKGROUND_TINT),
    onBackground = Color(0xFF1A1C1E),

    surface = EmeraldSeaPalette.primaryLight.tintWithWhite(LIGHT_SURFACE_TINT),
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = EmeraldSeaPalette.primaryLight.tintWithWhite(LIGHT_SURFACE_VARIANT_TINT),
    onSurfaceVariant = Color(0xFF3E4846),

    outline = EmeraldSeaPalette.primaryLight.copy(alpha = 0.12f),
    outlineVariant = EmeraldSeaPalette.primaryLight.copy(alpha = 0.08f)
)

private val EmeraldSeaDarkColorScheme = darkColorScheme(
    primary = EmeraldSeaPalette.primaryDark,
    onPrimary = Color(0xFF002220),
    primaryContainer = EmeraldSeaPalette.primaryDark.copy(alpha = 0.15f),
    onPrimaryContainer = EmeraldSeaPalette.primaryDark,

    secondary = EmeraldSeaPalette.secondaryDark,
    onSecondary = Color(0xFF002030),
    secondaryContainer = EmeraldSeaPalette.secondaryDark.copy(alpha = 0.15f),
    onSecondaryContainer = EmeraldSeaPalette.secondaryDark,

    tertiary = EmeraldSeaPalette.tertiaryDark,
    onTertiary = Color(0xFF002528),
    tertiaryContainer = EmeraldSeaPalette.tertiaryDark.copy(alpha = 0.15f),
    onTertiaryContainer = EmeraldSeaPalette.tertiaryDark,

    background = EmeraldSeaPalette.primaryDark.tintWithBlack(LIGHT_BACKGROUND_TINT),
    onBackground = Color(0xFFE2E8E6),

    surface = EmeraldSeaPalette.primaryDark.tintWithBlack(LIGHT_SURFACE_TINT),
    onSurface = Color(0xFFE2E8E6),
    surfaceVariant = EmeraldSeaPalette.primaryDark.tintWithBlack(LIGHT_SURFACE_VARIANT_TINT),
    onSurfaceVariant = Color(0xFFC0CCC8),

    outline = EmeraldSeaPalette.primaryDark.copy(alpha = 0.15f),
    outlineVariant = EmeraldSeaPalette.primaryDark.copy(alpha = 0.10f)
)

// Golden Hour Palette
private val GoldenHourLightColorScheme = lightColorScheme(
    primary = GoldenHourPalette.primaryLight,
    onPrimary = Color.White,
    primaryContainer = GoldenHourPalette.primaryLight.copy(alpha = 0.12f),
    onPrimaryContainer = GoldenHourPalette.primaryLight,

    secondary = GoldenHourPalette.secondaryLight,
    onSecondary = Color.White,
    secondaryContainer = GoldenHourPalette.secondaryLight.copy(alpha = 0.12f),
    onSecondaryContainer = GoldenHourPalette.secondaryLight,

    tertiary = GoldenHourPalette.tertiaryLight,
    onTertiary = Color.White,
    tertiaryContainer = GoldenHourPalette.tertiaryLight.copy(alpha = 0.12f),
    onTertiaryContainer = GoldenHourPalette.tertiaryLight,

    background = GoldenHourPalette.primaryLight.tintWithWhite(LIGHT_BACKGROUND_TINT),
    onBackground = Color(0xFF1A1C1E),

    surface = GoldenHourPalette.primaryLight.tintWithWhite(LIGHT_SURFACE_TINT),
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = GoldenHourPalette.primaryLight.tintWithWhite(LIGHT_SURFACE_VARIANT_TINT),
    onSurfaceVariant = Color(0xFF4A4238),

    outline = GoldenHourPalette.primaryLight.copy(alpha = 0.12f),
    outlineVariant = GoldenHourPalette.primaryLight.copy(alpha = 0.08f)
)

private val GoldenHourDarkColorScheme = darkColorScheme(
    primary = GoldenHourPalette.primaryDark,
    onPrimary = Color(0xFF3E2400),
    primaryContainer = GoldenHourPalette.primaryDark.copy(alpha = 0.15f),
    onPrimaryContainer = GoldenHourPalette.primaryDark,

    secondary = GoldenHourPalette.secondaryDark,
    onSecondary = Color(0xFF3A0054),
    secondaryContainer = GoldenHourPalette.secondaryDark.copy(alpha = 0.15f),
    onSecondaryContainer = GoldenHourPalette.secondaryDark,

    tertiary = GoldenHourPalette.tertiaryDark,
    onTertiary = Color(0xFF3E2800),
    tertiaryContainer = GoldenHourPalette.tertiaryDark.copy(alpha = 0.15f),
    onTertiaryContainer = GoldenHourPalette.tertiaryDark,

    background = GoldenHourPalette.primaryDark.tintWithBlack(LIGHT_BACKGROUND_TINT),
    onBackground = Color(0xFFE8E6E2),

    surface = GoldenHourPalette.primaryDark.tintWithBlack(LIGHT_SURFACE_TINT),
    onSurface = Color(0xFFE8E6E2),
    surfaceVariant = GoldenHourPalette.primaryDark.tintWithBlack(LIGHT_SURFACE_VARIANT_TINT),
    onSurfaceVariant = Color(0xFFD0C9C0),

    outline = GoldenHourPalette.primaryDark.copy(alpha = 0.15f),
    outlineVariant = GoldenHourPalette.primaryDark.copy(alpha = 0.10f)
)

// ============================================================================
// SECOND WAVE HIGH CONTRAST COLOR SCHEMES
// ============================================================================

// Neon Lime Palette
private val NeonLimeLightColorScheme = lightColorScheme(
    primary = NeonLimePalette.primaryLight,
    onPrimary = Color(0xFF1A1C1E),
    primaryContainer = NeonLimePalette.primaryLight.copy(alpha = 0.12f),
    onPrimaryContainer = NeonLimePalette.primaryLight,

    secondary = NeonLimePalette.secondaryLight,
    onSecondary = Color.White,
    secondaryContainer = NeonLimePalette.secondaryLight.copy(alpha = 0.12f),
    onSecondaryContainer = NeonLimePalette.secondaryLight,

    tertiary = NeonLimePalette.tertiaryLight,
    onTertiary = Color(0xFF1A1C1E),
    tertiaryContainer = NeonLimePalette.tertiaryLight.copy(alpha = 0.12f),
    onTertiaryContainer = NeonLimePalette.tertiaryLight,

    background = NeonLimePalette.primaryLight.tintWithWhite(LIGHT_BACKGROUND_TINT),
    onBackground = Color(0xFF1A1C1E),

    surface = NeonLimePalette.primaryLight.tintWithWhite(LIGHT_SURFACE_TINT),
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = NeonLimePalette.primaryLight.tintWithWhite(LIGHT_SURFACE_VARIANT_TINT),
    onSurfaceVariant = Color(0xFF3E4238),

    outline = NeonLimePalette.primaryLight.copy(alpha = 0.12f),
    outlineVariant = NeonLimePalette.primaryLight.copy(alpha = 0.08f)
)

private val NeonLimeDarkColorScheme = darkColorScheme(
    primary = NeonLimePalette.primaryDark,
    onPrimary = Color(0xFF2E3800),
    primaryContainer = NeonLimePalette.primaryDark.copy(alpha = 0.15f),
    onPrimaryContainer = NeonLimePalette.primaryDark,

    secondary = NeonLimePalette.secondaryDark,
    onSecondary = Color(0xFF1C0038),
    secondaryContainer = NeonLimePalette.secondaryDark.copy(alpha = 0.15f),
    onSecondaryContainer = NeonLimePalette.secondaryDark,

    tertiary = NeonLimePalette.tertiaryDark,
    onTertiary = Color(0xFF3E4800),
    tertiaryContainer = NeonLimePalette.tertiaryDark.copy(alpha = 0.15f),
    onTertiaryContainer = NeonLimePalette.tertiaryDark,

    background = NeonLimePalette.primaryDark.tintWithBlack(LIGHT_BACKGROUND_TINT),
    onBackground = Color(0xFFE8E8E2),

    surface = NeonLimePalette.primaryDark.tintWithBlack(LIGHT_SURFACE_TINT),
    onSurface = Color(0xFFE8E8E2),
    surfaceVariant = NeonLimePalette.primaryDark.tintWithBlack(LIGHT_SURFACE_VARIANT_TINT),
    onSurfaceVariant = Color(0xFFD0CCC0),

    outline = NeonLimePalette.primaryDark.copy(alpha = 0.15f),
    outlineVariant = NeonLimePalette.primaryDark.copy(alpha = 0.10f)
)

// Hot Lava Palette
private val HotLavaLightColorScheme = lightColorScheme(
    primary = HotLavaPalette.primaryLight,
    onPrimary = Color.White,
    primaryContainer = HotLavaPalette.primaryLight.copy(alpha = 0.12f),
    onPrimaryContainer = HotLavaPalette.primaryLight,

    secondary = HotLavaPalette.secondaryLight,
    onSecondary = Color.White,
    secondaryContainer = HotLavaPalette.secondaryLight.copy(alpha = 0.12f),
    onSecondaryContainer = HotLavaPalette.secondaryLight,

    tertiary = HotLavaPalette.tertiaryLight,
    onTertiary = Color.White,
    tertiaryContainer = HotLavaPalette.tertiaryLight.copy(alpha = 0.12f),
    onTertiaryContainer = HotLavaPalette.tertiaryLight,

    background = HotLavaPalette.primaryLight.tintWithWhite(LIGHT_BACKGROUND_TINT),
    onBackground = Color(0xFF1A1C1E),

    surface = HotLavaPalette.primaryLight.tintWithWhite(LIGHT_SURFACE_TINT),
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = HotLavaPalette.primaryLight.tintWithWhite(LIGHT_SURFACE_VARIANT_TINT),
    onSurfaceVariant = Color(0xFF4A3E38),

    outline = HotLavaPalette.primaryLight.copy(alpha = 0.12f),
    outlineVariant = HotLavaPalette.primaryLight.copy(alpha = 0.08f)
)

private val HotLavaDarkColorScheme = darkColorScheme(
    primary = HotLavaPalette.primaryDark,
    onPrimary = Color(0xFF3E0800),
    primaryContainer = HotLavaPalette.primaryDark.copy(alpha = 0.15f),
    onPrimaryContainer = HotLavaPalette.primaryDark,

    secondary = HotLavaPalette.secondaryDark,
    onSecondary = Color(0xFF000000),
    secondaryContainer = HotLavaPalette.secondaryDark.copy(alpha = 0.15f),
    onSecondaryContainer = HotLavaPalette.secondaryDark,

    tertiary = HotLavaPalette.tertiaryDark,
    onTertiary = Color(0xFF3E1000),
    tertiaryContainer = HotLavaPalette.tertiaryDark.copy(alpha = 0.15f),
    onTertiaryContainer = HotLavaPalette.tertiaryDark,

    background = HotLavaPalette.primaryDark.tintWithBlack(LIGHT_BACKGROUND_TINT),
    onBackground = Color(0xFFE8E4E2),

    surface = HotLavaPalette.primaryDark.tintWithBlack(LIGHT_SURFACE_TINT),
    onSurface = Color(0xFFE8E4E2),
    surfaceVariant = HotLavaPalette.primaryDark.tintWithBlack(LIGHT_SURFACE_VARIANT_TINT),
    onSurfaceVariant = Color(0xFFD0C4C0),

    outline = HotLavaPalette.primaryDark.copy(alpha = 0.15f),
    outlineVariant = HotLavaPalette.primaryDark.copy(alpha = 0.10f)
)
// Cyber Pink Palette
private val CyberPinkLightColorScheme = lightColorScheme(
    primary = CyberPinkPalette.primaryLight,
    onPrimary = Color.White,
    primaryContainer = CyberPinkPalette.primaryLight.copy(alpha = 0.12f),
    onPrimaryContainer = CyberPinkPalette.primaryLight,

    secondary = CyberPinkPalette.secondaryLight,
    onSecondary = Color.White,
    secondaryContainer = CyberPinkPalette.secondaryLight.copy(alpha = 0.12f),
    onSecondaryContainer = CyberPinkPalette.secondaryLight,

    tertiary = CyberPinkPalette.tertiaryLight,
    onTertiary = Color.White,
    tertiaryContainer = CyberPinkPalette.tertiaryLight.copy(alpha = 0.12f),
    onTertiaryContainer = CyberPinkPalette.tertiaryLight,

    background = CyberPinkPalette.primaryLight.tintWithWhite(LIGHT_BACKGROUND_TINT),
    onBackground = Color(0xFF1A1C1E),

    surface = CyberPinkPalette.primaryLight.tintWithWhite(LIGHT_SURFACE_TINT),
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = CyberPinkPalette.primaryLight.tintWithWhite(LIGHT_SURFACE_VARIANT_TINT),
    onSurfaceVariant = Color(0xFF4A3E42),

    outline = CyberPinkPalette.primaryLight.copy(alpha = 0.12f),
    outlineVariant = CyberPinkPalette.primaryLight.copy(alpha = 0.08f)
)

private val CyberPinkDarkColorScheme = darkColorScheme(
    primary = CyberPinkPalette.primaryDark,
    onPrimary = Color(0xFF3E001C),
    primaryContainer = CyberPinkPalette.primaryDark.copy(alpha = 0.15f),
    onPrimaryContainer = CyberPinkPalette.primaryDark,

    secondary = CyberPinkPalette.secondaryDark,
    onSecondary = Color(0xFF003830),
    secondaryContainer = CyberPinkPalette.secondaryDark.copy(alpha = 0.15f),
    onSecondaryContainer = CyberPinkPalette.secondaryDark,

    tertiary = CyberPinkPalette.tertiaryDark,
    onTertiary = Color(0xFF3E0020),
    tertiaryContainer = CyberPinkPalette.tertiaryDark.copy(alpha = 0.15f),
    onTertiaryContainer = CyberPinkPalette.tertiaryDark,

    background = CyberPinkPalette.primaryDark.tintWithBlack(LIGHT_BACKGROUND_TINT),
    onBackground = Color(0xFFE8E4E6),

    surface = CyberPinkPalette.primaryDark.tintWithBlack(LIGHT_SURFACE_TINT),
    onSurface = Color(0xFFE8E4E6),
    surfaceVariant = CyberPinkPalette.primaryDark.tintWithBlack(LIGHT_SURFACE_VARIANT_TINT),
    onSurfaceVariant = Color(0xFFD0C4C8),

    outline = CyberPinkPalette.primaryDark.copy(alpha = 0.15f),
    outlineVariant = CyberPinkPalette.primaryDark.copy(alpha = 0.10f)
)

// Ocean Sunset Palette
private val OceanSunsetLightColorScheme = lightColorScheme(
    primary = OceanSunsetPalette.primaryLight,
    onPrimary = Color.White,
    primaryContainer = OceanSunsetPalette.primaryLight.copy(alpha = 0.12f),
    onPrimaryContainer = OceanSunsetPalette.primaryLight,

    secondary = OceanSunsetPalette.secondaryLight,
    onSecondary = Color.White,
    secondaryContainer = OceanSunsetPalette.secondaryLight.copy(alpha = 0.12f),
    onSecondaryContainer = OceanSunsetPalette.secondaryLight,

    tertiary = OceanSunsetPalette.tertiaryLight,
    onTertiary = Color.White,
    tertiaryContainer = OceanSunsetPalette.tertiaryLight.copy(alpha = 0.12f),
    onTertiaryContainer = OceanSunsetPalette.tertiaryLight,

    background = OceanSunsetPalette.primaryLight.tintWithWhite(LIGHT_BACKGROUND_TINT),
    onBackground = Color(0xFF1A1C1E),

    surface = OceanSunsetPalette.primaryLight.tintWithWhite(LIGHT_SURFACE_TINT),
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = OceanSunsetPalette.primaryLight.tintWithWhite(LIGHT_SURFACE_VARIANT_TINT),
    onSurfaceVariant = Color(0xFF3E4248),

    outline = OceanSunsetPalette.primaryLight.copy(alpha = 0.12f),
    outlineVariant = OceanSunsetPalette.primaryLight.copy(alpha = 0.08f)
)

private val OceanSunsetDarkColorScheme = darkColorScheme(
    primary = OceanSunsetPalette.primaryDark,
    onPrimary = Color(0xFF001838),
    primaryContainer = OceanSunsetPalette.primaryDark.copy(alpha = 0.15f),
    onPrimaryContainer = OceanSunsetPalette.primaryDark,

    secondary = OceanSunsetPalette.secondaryDark,
    onSecondary = Color(0xFF3E1800),
    secondaryContainer = OceanSunsetPalette.secondaryDark.copy(alpha = 0.15f),
    onSecondaryContainer = OceanSunsetPalette.secondaryDark,

    tertiary = OceanSunsetPalette.tertiaryDark,
    onTertiary = Color(0xFF002838),
    tertiaryContainer = OceanSunsetPalette.tertiaryDark.copy(alpha = 0.15f),
    onTertiaryContainer = OceanSunsetPalette.tertiaryDark,

    background = OceanSunsetPalette.primaryDark.tintWithBlack(LIGHT_BACKGROUND_TINT),
    onBackground = Color(0xFFE2E6E8),

    surface = OceanSunsetPalette.primaryDark.tintWithBlack(LIGHT_SURFACE_TINT),
    onSurface = Color(0xFFE2E6E8),
    surfaceVariant = OceanSunsetPalette.primaryDark.tintWithBlack(LIGHT_SURFACE_VARIANT_TINT),
    onSurfaceVariant = Color(0xFFC0C8D0),

    outline = OceanSunsetPalette.primaryDark.copy(alpha = 0.15f),
    outlineVariant = OceanSunsetPalette.primaryDark.copy(alpha = 0.10f)
)

// Forest Amber Palette
private val ForestAmberLightColorScheme = lightColorScheme(
    primary = ForestAmberPalette.primaryLight,
    onPrimary = Color.White,
    primaryContainer = ForestAmberPalette.primaryLight.copy(alpha = 0.12f),
    onPrimaryContainer = ForestAmberPalette.primaryLight,

    secondary = ForestAmberPalette.secondaryLight,
    onSecondary = Color.White,
    secondaryContainer = ForestAmberPalette.secondaryLight.copy(alpha = 0.12f),
    onSecondaryContainer = ForestAmberPalette.secondaryLight,

    tertiary = ForestAmberPalette.tertiaryLight,
    onTertiary = Color.White,
    tertiaryContainer = ForestAmberPalette.tertiaryLight.copy(alpha = 0.12f),
    onTertiaryContainer = ForestAmberPalette.tertiaryLight,

    background = ForestAmberPalette.primaryLight.tintWithWhite(LIGHT_BACKGROUND_TINT),
    onBackground = Color(0xFF1A1C1E),

    surface = ForestAmberPalette.primaryLight.tintWithWhite(LIGHT_SURFACE_TINT),
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = ForestAmberPalette.primaryLight.tintWithWhite(LIGHT_SURFACE_VARIANT_TINT),
    onSurfaceVariant = Color(0xFF3E4638),

    outline = ForestAmberPalette.primaryLight.copy(alpha = 0.12f),
    outlineVariant = ForestAmberPalette.primaryLight.copy(alpha = 0.08f)
)

private val ForestAmberDarkColorScheme = darkColorScheme(
    primary = ForestAmberPalette.primaryDark,
    onPrimary = Color(0xFF002008),
    primaryContainer = ForestAmberPalette.primaryDark.copy(alpha = 0.15f),
    onPrimaryContainer = ForestAmberPalette.primaryDark,

    secondary = ForestAmberPalette.secondaryDark,
    onSecondary = Color(0xFF3E2000),
    secondaryContainer = ForestAmberPalette.secondaryDark.copy(alpha = 0.15f),
    onSecondaryContainer = ForestAmberPalette.secondaryDark,

    tertiary = ForestAmberPalette.tertiaryDark,
    onTertiary = Color(0xFF002810),
    tertiaryContainer = ForestAmberPalette.tertiaryDark.copy(alpha = 0.15f),
    onTertiaryContainer = ForestAmberPalette.tertiaryDark,

    background = ForestAmberPalette.primaryDark.tintWithBlack(LIGHT_BACKGROUND_TINT),
    onBackground = Color(0xFFE2E8E4),

    surface = ForestAmberPalette.primaryDark.tintWithBlack(LIGHT_SURFACE_TINT),
    onSurface = Color(0xFFE2E8E4),
    surfaceVariant = ForestAmberPalette.primaryDark.tintWithBlack(LIGHT_SURFACE_VARIANT_TINT),
    onSurfaceVariant = Color(0xFFC0CCC4),

    outline = ForestAmberPalette.primaryDark.copy(alpha = 0.15f),
    outlineVariant = ForestAmberPalette.primaryDark.copy(alpha = 0.10f)
)

// Sapphire Rose Palette
private val SapphireRoseLightColorScheme = lightColorScheme(
    primary = SapphireRosePalette.primaryLight,
    onPrimary = Color.White,
    primaryContainer = SapphireRosePalette.primaryLight.copy(alpha = 0.12f),
    onPrimaryContainer = SapphireRosePalette.primaryLight,

    secondary = SapphireRosePalette.secondaryLight,
    onSecondary = Color.White,
    secondaryContainer = SapphireRosePalette.secondaryLight.copy(alpha = 0.12f),
    onSecondaryContainer = SapphireRosePalette.secondaryLight,

    tertiary = SapphireRosePalette.tertiaryLight,
    onTertiary = Color.White,
    tertiaryContainer = SapphireRosePalette.tertiaryLight.copy(alpha = 0.12f),
    onTertiaryContainer = SapphireRosePalette.tertiaryLight,

    background = SapphireRosePalette.primaryLight.tintWithWhite(LIGHT_BACKGROUND_TINT),
    onBackground = Color(0xFF1A1C1E),

    surface = SapphireRosePalette.primaryLight.tintWithWhite(LIGHT_SURFACE_TINT),
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = SapphireRosePalette.primaryLight.tintWithWhite(LIGHT_SURFACE_VARIANT_TINT),
    onSurfaceVariant = Color(0xFF3E3E4A),

    outline = SapphireRosePalette.primaryLight.copy(alpha = 0.12f),
    outlineVariant = SapphireRosePalette.primaryLight.copy(alpha = 0.08f)
)

private val SapphireRoseDarkColorScheme = darkColorScheme(
    primary = SapphireRosePalette.primaryDark,
    onPrimary = Color(0xFF000838),
    primaryContainer = SapphireRosePalette.primaryDark.copy(alpha = 0.15f),
    onPrimaryContainer = SapphireRosePalette.primaryDark,

    secondary = SapphireRosePalette.secondaryDark,
    onSecondary = Color(0xFF3E001C),
    secondaryContainer = SapphireRosePalette.secondaryDark.copy(alpha = 0.15f),
    onSecondaryContainer = SapphireRosePalette.secondaryDark,

    tertiary = SapphireRosePalette.tertiaryDark,
    onTertiary = Color(0xFF001038),
    tertiaryContainer = SapphireRosePalette.tertiaryDark.copy(alpha = 0.15f),
    onTertiaryContainer = SapphireRosePalette.tertiaryDark,

    background = SapphireRosePalette.primaryDark.tintWithBlack(LIGHT_BACKGROUND_TINT),
    onBackground = Color(0xFFE2E4E8),

    surface = SapphireRosePalette.primaryDark.tintWithBlack(LIGHT_SURFACE_TINT),
    onSurface = Color(0xFFE2E4E8),
    surfaceVariant = SapphireRosePalette.primaryDark.tintWithBlack(LIGHT_SURFACE_VARIANT_TINT),
    onSurfaceVariant = Color(0xFFC0C4D0),

    outline = SapphireRosePalette.primaryDark.copy(alpha = 0.15f),
    outlineVariant = SapphireRosePalette.primaryDark.copy(alpha = 0.10f)
)

// Electric Violet Palette
private val ElectricVioletLightColorScheme = lightColorScheme(
    primary = ElectricVioletPalette.primaryLight,
    onPrimary = Color.White,
    primaryContainer = ElectricVioletPalette.primaryLight.copy(alpha = 0.12f),
    onPrimaryContainer = ElectricVioletPalette.primaryLight,

    secondary = ElectricVioletPalette.secondaryLight,
    onSecondary = Color(0xFF1A1C1E),
    secondaryContainer = ElectricVioletPalette.secondaryLight.copy(alpha = 0.12f),
    onSecondaryContainer = ElectricVioletPalette.secondaryLight,

    tertiary = ElectricVioletPalette.tertiaryLight,
    onTertiary = Color.White,
    tertiaryContainer = ElectricVioletPalette.tertiaryLight.copy(alpha = 0.12f),
    onTertiaryContainer = ElectricVioletPalette.tertiaryLight,

    background = ElectricVioletPalette.primaryLight.tintWithWhite(LIGHT_BACKGROUND_TINT),
    onBackground = Color(0xFF1A1C1E),

    surface = ElectricVioletPalette.primaryLight.tintWithWhite(LIGHT_SURFACE_TINT),
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = ElectricVioletPalette.primaryLight.tintWithWhite(LIGHT_SURFACE_VARIANT_TINT),
    onSurfaceVariant = Color(0xFF4A3E50),

    outline = ElectricVioletPalette.primaryLight.copy(alpha = 0.12f),
    outlineVariant = ElectricVioletPalette.primaryLight.copy(alpha = 0.08f)
)

private val ElectricVioletDarkColorScheme = darkColorScheme(
    primary = ElectricVioletPalette.primaryDark,
    onPrimary = Color(0xFF1C0050),
    primaryContainer = ElectricVioletPalette.primaryDark.copy(alpha = 0.15f),
    onPrimaryContainer = ElectricVioletPalette.primaryDark,

    secondary = ElectricVioletPalette.secondaryDark,
    onSecondary = Color(0xFF3E3800),
    secondaryContainer = ElectricVioletPalette.secondaryDark.copy(alpha = 0.15f),
    onSecondaryContainer = ElectricVioletPalette.secondaryDark,

    tertiary = ElectricVioletPalette.tertiaryDark,
    onTertiary = Color(0xFF280050),
    tertiaryContainer = ElectricVioletPalette.tertiaryDark.copy(alpha = 0.15f),
    onTertiaryContainer = ElectricVioletPalette.tertiaryDark,

    background = ElectricVioletPalette.primaryDark.tintWithBlack(LIGHT_BACKGROUND_TINT),
    onBackground = Color(0xFFE6E2E8),

    surface = ElectricVioletPalette.primaryDark.tintWithBlack(LIGHT_SURFACE_TINT),
    onSurface = Color(0xFFE6E2E8),
    surfaceVariant = ElectricVioletPalette.primaryDark.tintWithBlack(LIGHT_SURFACE_VARIANT_TINT),
    onSurfaceVariant = Color(0xFFC8C0D0),

    outline = ElectricVioletPalette.primaryDark.copy(alpha = 0.15f),
    outlineVariant = ElectricVioletPalette.primaryDark.copy(alpha = 0.10f)
)

// Candy Crush Palette
private val CandyCrushLightColorScheme = lightColorScheme(
    primary = CandyCrushPalette.primaryLight,
    onPrimary = Color.White,
    primaryContainer = CandyCrushPalette.primaryLight.copy(alpha = 0.12f),
    onPrimaryContainer = CandyCrushPalette.primaryLight,

    secondary = CandyCrushPalette.secondaryLight,
    onSecondary = Color.White,
    secondaryContainer = CandyCrushPalette.secondaryLight.copy(alpha = 0.12f),
    onSecondaryContainer = CandyCrushPalette.secondaryLight,

    tertiary = CandyCrushPalette.tertiaryLight,
    onTertiary = Color.White,
    tertiaryContainer = CandyCrushPalette.tertiaryLight.copy(alpha = 0.12f),
    onTertiaryContainer = CandyCrushPalette.tertiaryLight,

    background = CandyCrushPalette.primaryLight.tintWithWhite(LIGHT_BACKGROUND_TINT),
    onBackground = Color(0xFF1A1C1E),

    surface = CandyCrushPalette.primaryLight.tintWithWhite(LIGHT_SURFACE_TINT),
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = CandyCrushPalette.primaryLight.tintWithWhite(LIGHT_SURFACE_VARIANT_TINT),
    onSurfaceVariant = Color(0xFF4A3E44),

    outline = CandyCrushPalette.primaryLight.copy(alpha = 0.12f),
    outlineVariant = CandyCrushPalette.primaryLight.copy(alpha = 0.08f)
)

private val CandyCrushDarkColorScheme = darkColorScheme(
    primary = CandyCrushPalette.primaryDark,
    onPrimary = Color(0xFF3E001C),
    primaryContainer = CandyCrushPalette.primaryDark.copy(alpha = 0.15f),
    onPrimaryContainer = CandyCrushPalette.primaryDark,

    secondary = CandyCrushPalette.secondaryDark,
    onSecondary = Color(0xFF003840),
    secondaryContainer = CandyCrushPalette.secondaryDark.copy(alpha = 0.15f),
    onSecondaryContainer = CandyCrushPalette.secondaryDark,

    tertiary = CandyCrushPalette.tertiaryDark,
    onTertiary = Color(0xFF3E0020),
    tertiaryContainer = CandyCrushPalette.tertiaryDark.copy(alpha = 0.15f),
    onTertiaryContainer = CandyCrushPalette.tertiaryDark,

    background = CandyCrushPalette.primaryDark.tintWithBlack(LIGHT_BACKGROUND_TINT),
    onBackground = Color(0xFFE8E4E6),

    surface = CandyCrushPalette.primaryDark.tintWithBlack(LIGHT_SURFACE_TINT),
    onSurface = Color(0xFFE8E4E6),
    surfaceVariant = CandyCrushPalette.primaryDark.tintWithBlack(LIGHT_SURFACE_VARIANT_TINT),
    onSurfaceVariant = Color(0xFFD0C4C8),

    outline = CandyCrushPalette.primaryDark.copy(alpha = 0.15f),
    outlineVariant = CandyCrushPalette.primaryDark.copy(alpha = 0.10f)
)

// Midnight Sun Palette
private val MidnightSunLightColorScheme = lightColorScheme(
    primary = MidnightSunPalette.primaryLight,
    onPrimary = Color.White,
    primaryContainer = MidnightSunPalette.primaryLight.copy(alpha = 0.12f),
    onPrimaryContainer = MidnightSunPalette.primaryLight,

    secondary = MidnightSunPalette.secondaryLight,
    onSecondary = Color(0xFF1A1C1E),
    secondaryContainer = MidnightSunPalette.secondaryLight.copy(alpha = 0.12f),
    onSecondaryContainer = MidnightSunPalette.secondaryLight,

    tertiary = MidnightSunPalette.tertiaryLight,
    onTertiary = Color.White,
    tertiaryContainer = MidnightSunPalette.tertiaryLight.copy(alpha = 0.12f),
    onTertiaryContainer = MidnightSunPalette.tertiaryLight,

    background = MidnightSunPalette.primaryLight.tintWithWhite(LIGHT_BACKGROUND_TINT),
    onBackground = Color(0xFF1A1C1E),

    surface = MidnightSunPalette.primaryLight.tintWithWhite(LIGHT_SURFACE_TINT),
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = MidnightSunPalette.primaryLight.tintWithWhite(LIGHT_SURFACE_VARIANT_TINT),
    onSurfaceVariant = Color(0xFF3E4248),

    outline = MidnightSunPalette.primaryLight.copy(alpha = 0.12f),
    outlineVariant = MidnightSunPalette.primaryLight.copy(alpha = 0.08f)
)

private val MidnightSunDarkColorScheme = darkColorScheme(
    primary = MidnightSunPalette.primaryDark,
    onPrimary = Color(0xFF001838),
    primaryContainer = MidnightSunPalette.primaryDark.copy(alpha = 0.15f),
    onPrimaryContainer = MidnightSunPalette.primaryDark,

    secondary = MidnightSunPalette.secondaryDark,
    onSecondary = Color(0xFF3E3800),
    secondaryContainer = MidnightSunPalette.secondaryDark.copy(alpha = 0.15f),
    onSecondaryContainer = MidnightSunPalette.secondaryDark,

    tertiary = MidnightSunPalette.tertiaryDark,
    onTertiary = Color(0xFF002838),
    tertiaryContainer = MidnightSunPalette.tertiaryDark.copy(alpha = 0.15f),
    onTertiaryContainer = MidnightSunPalette.tertiaryDark,

    background = MidnightSunPalette.primaryDark.tintWithBlack(LIGHT_BACKGROUND_TINT),
    onBackground = Color(0xFFE2E6E8),

    surface = MidnightSunPalette.primaryDark.tintWithBlack(LIGHT_SURFACE_TINT),
    onSurface = Color(0xFFE2E6E8),
    surfaceVariant = MidnightSunPalette.primaryDark.tintWithBlack(LIGHT_SURFACE_VARIANT_TINT),
    onSurfaceVariant = Color(0xFFC0C8D0),

    outline = MidnightSunPalette.primaryDark.copy(alpha = 0.15f),
    outlineVariant = MidnightSunPalette.primaryDark.copy(alpha = 0.10f)
)

// Strawberry Mint Palette
private val StrawberryMintLightColorScheme = lightColorScheme(
    primary = StrawberryMintPalette.primaryLight,
    onPrimary = Color.White,
    primaryContainer = StrawberryMintPalette.primaryLight.copy(alpha = 0.12f),
    onPrimaryContainer = StrawberryMintPalette.primaryLight,

    secondary = StrawberryMintPalette.secondaryLight,
    onSecondary = Color.White,
    secondaryContainer = StrawberryMintPalette.secondaryLight.copy(alpha = 0.12f),
    onSecondaryContainer = StrawberryMintPalette.secondaryLight,

    tertiary = StrawberryMintPalette.tertiaryLight,
    onTertiary = Color.White,
    tertiaryContainer = StrawberryMintPalette.tertiaryLight.copy(alpha = 0.12f),
    onTertiaryContainer = StrawberryMintPalette.tertiaryLight,

    background = StrawberryMintPalette.primaryLight.tintWithWhite(LIGHT_BACKGROUND_TINT),
    onBackground = Color(0xFF1A1C1E),

    surface = StrawberryMintPalette.primaryLight.tintWithWhite(LIGHT_SURFACE_TINT),
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = StrawberryMintPalette.primaryLight.tintWithWhite(LIGHT_SURFACE_VARIANT_TINT),
    onSurfaceVariant = Color(0xFF4A3E3E),

    outline = StrawberryMintPalette.primaryLight.copy(alpha = 0.12f),
    outlineVariant = StrawberryMintPalette.primaryLight.copy(alpha = 0.08f)
)

private val StrawberryMintDarkColorScheme = darkColorScheme(
    primary = StrawberryMintPalette.primaryDark,
    onPrimary = Color(0xFF3E0010),
    primaryContainer = StrawberryMintPalette.primaryDark.copy(alpha = 0.15f),
    onPrimaryContainer = StrawberryMintPalette.primaryDark,

    secondary = StrawberryMintPalette.secondaryDark,
    onSecondary = Color(0xFF003830),
    secondaryContainer = StrawberryMintPalette.secondaryDark.copy(alpha = 0.15f),
    onSecondaryContainer = StrawberryMintPalette.secondaryDark,

    tertiary = StrawberryMintPalette.tertiaryDark,
    onTertiary = Color(0xFF3E0018),
    tertiaryContainer = StrawberryMintPalette.tertiaryDark.copy(alpha = 0.15f),
    onTertiaryContainer = StrawberryMintPalette.tertiaryDark,

    background = StrawberryMintPalette.primaryDark.tintWithBlack(LIGHT_BACKGROUND_TINT),
    onBackground = Color(0xFFE8E4E4),

    surface = StrawberryMintPalette.primaryDark.tintWithBlack(LIGHT_SURFACE_TINT),
    onSurface = Color(0xFFE8E4E4),
    surfaceVariant = StrawberryMintPalette.primaryDark.tintWithBlack(LIGHT_SURFACE_VARIANT_TINT),
    onSurfaceVariant = Color(0xFFD0C4C4),

    outline = StrawberryMintPalette.primaryDark.copy(alpha = 0.15f),
    outlineVariant = StrawberryMintPalette.primaryDark.copy(alpha = 0.10f)
)

// Legacy color schemes (for backward compatibility)
private val DarkColorScheme = DefaultBlueDarkColorScheme
private val LightColorScheme = DefaultBlueLightColorScheme

@Composable
fun CarTrackingAppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    colorPalette: ColorPalette = ColorPalette.SYSTEM,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current

    val colorScheme = when (colorPalette) {
        ColorPalette.SYSTEM -> {
            // Use system dynamic colors on Android 12+
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            } else {
                // Fallback to default blue palette on older Android versions
                if (darkTheme) DefaultBlueDarkColorScheme else DefaultBlueLightColorScheme
            }
        }
        ColorPalette.DEFAULT_BLUE -> {
            if (darkTheme) DefaultBlueDarkColorScheme else DefaultBlueLightColorScheme
        }
        ColorPalette.SUNSET_ORANGE -> {
            if (darkTheme) SunsetOrangeDarkColorScheme else SunsetOrangeLightColorScheme
        }
        ColorPalette.FOREST_GREEN -> {
            if (darkTheme) ForestGreenDarkColorScheme else ForestGreenLightColorScheme
        }
        ColorPalette.ROYAL_PURPLE -> {
            if (darkTheme) RoyalPurpleDarkColorScheme else RoyalPurpleLightColorScheme
        }
        ColorPalette.OCEAN_TEAL -> {
            if (darkTheme) OceanTealDarkColorScheme else OceanTealLightColorScheme
        }
        ColorPalette.CRIMSON_RED -> {
            if (darkTheme) CrimsonRedDarkColorScheme else CrimsonRedLightColorScheme
        }
        ColorPalette.AMBER_GOLD -> {
            if (darkTheme) AmberGoldDarkColorScheme else AmberGoldLightColorScheme
        }
        ColorPalette.DEEP_INDIGO -> {
            if (darkTheme) DeepIndigoDarkColorScheme else DeepIndigoLightColorScheme
        }
        ColorPalette.SLATE_GRAY -> {
            if (darkTheme) SlateGrayDarkColorScheme else SlateGrayLightColorScheme
        }
        ColorPalette.ROSE_PINK -> {
            if (darkTheme) RosePinkDarkColorScheme else RosePinkLightColorScheme
        }
        ColorPalette.MINT_BREEZE -> {
            if (darkTheme) MintBreezeDarkColorScheme else MintBreezeLightColorScheme
        }
        ColorPalette.LAVENDER_DREAM -> {
            if (darkTheme) LavenderDreamDarkColorScheme else LavenderDreamLightColorScheme
        }
        ColorPalette.CORAL_SUNSET -> {
            if (darkTheme) CoralSunsetDarkColorScheme else CoralSunsetLightColorScheme
        }
        ColorPalette.EMERALD_FOREST -> {
            if (darkTheme) EmeraldForestDarkColorScheme else EmeraldForestLightColorScheme
        }
        ColorPalette.ELECTRIC_CYAN -> {
            if (darkTheme) ElectricCyanDarkColorScheme else ElectricCyanLightColorScheme
        }
        ColorPalette.MIDNIGHT_BLACK -> {
            if (darkTheme) MidnightBlackDarkColorScheme else MidnightBlackLightColorScheme
        }
        ColorPalette.ICE_WHITE -> {
            if (darkTheme) IceWhiteDarkColorScheme else IceWhiteLightColorScheme
        }
        ColorPalette.NEON_MAGENTA -> {
            if (darkTheme) NeonMagentaDarkColorScheme else NeonMagentaLightColorScheme
        }
        ColorPalette.DARK_OLIVE -> {
            if (darkTheme) DarkOliveDarkColorScheme else DarkOliveLightColorScheme
        }
        ColorPalette.VOLCANIC_ASH -> {
            if (darkTheme) VolcanicAshDarkColorScheme else VolcanicAshLightColorScheme
        }
        ColorPalette.SUNSET_FIRE -> {
            if (darkTheme) SunsetFireDarkColorScheme else SunsetFireLightColorScheme
        }
        ColorPalette.TROPICAL_PARADISE -> {
            if (darkTheme) TropicalParadiseDarkColorScheme else TropicalParadiseLightColorScheme
        }
        ColorPalette.ROYAL_GOLD -> {
            if (darkTheme) RoyalGoldDarkColorScheme else RoyalGoldLightColorScheme
        }
        ColorPalette.BERRY_BLAST -> {
            if (darkTheme) BerryBlastDarkColorScheme else BerryBlastLightColorScheme
        }
        ColorPalette.NEON_NIGHT -> {
            if (darkTheme) NeonNightDarkColorScheme else NeonNightLightColorScheme
        }
        ColorPalette.AUTUMN_HARVEST -> {
            if (darkTheme) AutumnHarvestDarkColorScheme else AutumnHarvestLightColorScheme
        }
        ColorPalette.ARCTIC_FROST -> {
            if (darkTheme) ArcticFrostDarkColorScheme else ArcticFrostLightColorScheme
        }
        ColorPalette.CHERRY_BLOSSOM -> {
            if (darkTheme) CherryBlossomDarkColorScheme else CherryBlossomLightColorScheme
        }
        ColorPalette.EMERALD_SEA -> {
            if (darkTheme) EmeraldSeaDarkColorScheme else EmeraldSeaLightColorScheme
        }
        ColorPalette.GOLDEN_HOUR -> {
            if (darkTheme) GoldenHourDarkColorScheme else GoldenHourLightColorScheme
        }
        ColorPalette.NEON_LIME -> {
            if (darkTheme) NeonLimeDarkColorScheme else NeonLimeLightColorScheme
        }
        ColorPalette.HOT_LAVA -> {
            if (darkTheme) HotLavaDarkColorScheme else HotLavaLightColorScheme
        }
        ColorPalette.CYBER_PINK -> {
            if (darkTheme) CyberPinkDarkColorScheme else CyberPinkLightColorScheme
        }
        ColorPalette.OCEAN_SUNSET -> {
            if (darkTheme) OceanSunsetDarkColorScheme else OceanSunsetLightColorScheme
        }
        ColorPalette.FOREST_AMBER -> {
            if (darkTheme) ForestAmberDarkColorScheme else ForestAmberLightColorScheme
        }
        ColorPalette.SAPPHIRE_ROSE -> {
            if (darkTheme) SapphireRoseDarkColorScheme else SapphireRoseLightColorScheme
        }
        ColorPalette.ELECTRIC_VIOLET -> {
            if (darkTheme) ElectricVioletDarkColorScheme else ElectricVioletLightColorScheme
        }
        ColorPalette.CANDY_CRUSH -> {
            if (darkTheme) CandyCrushDarkColorScheme else CandyCrushLightColorScheme
        }
        ColorPalette.MIDNIGHT_SUN -> {
            if (darkTheme) MidnightSunDarkColorScheme else MidnightSunLightColorScheme
        }
        ColorPalette.STRAWBERRY_MINT -> {
            if (darkTheme) StrawberryMintDarkColorScheme else StrawberryMintLightColorScheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography
    ) {
        // Apply tinted background globally using Surface
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = colorScheme.background
        ) {
            content()
        }
    }
}

