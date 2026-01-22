package com.agcoding.cartrackingapp.presentation.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.agcoding.cartrackingapp.data.preferences.ColorPalette

// Helper function to create tinted backgrounds (light theme)
private fun Color.lightenWithAlpha(alpha: Float = 0.08f): Color {
    return this.copy(alpha = alpha)
}

// Helper function to create tinted backgrounds (dark theme)
private fun Color.darkenWithAlpha(alpha: Float = 0.12f): Color {
    return this.copy(alpha = alpha)
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

    background = Color(0xFFF8FAFB), // Very light blue-gray tint
    onBackground = Color(0xFF1A1C1E),

    surface = Color(0xFFFAFCFD), // Subtle blue tint
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = Color(0xFFE1E8ED), // Light blue-tinted surface
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

    background = Color(0xFF111416), // Dark with subtle blue tint
    onBackground = Color(0xFFE2E2E6),

    surface = Color(0xFF1A1D1F), // Slightly elevated with blue tint
    onSurface = Color(0xFFE2E2E6),
    surfaceVariant = Color(0xFF2A3135), // Blue-tinted dark surface
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

    background = Color(0xFFFFF8F5), // Very light warm tint
    onBackground = Color(0xFF1A1C1E),

    surface = Color(0xFFFFFBF8), // Subtle orange-warm tint
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = Color(0xFFFFF0E6), // Light orange-tinted surface
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

    background = Color(0xFF1A1512), // Dark with warm tint
    onBackground = Color(0xFFE6E1DD),

    surface = Color(0xFF211D18), // Slightly elevated with warm tint
    onSurface = Color(0xFFE6E1DD),
    surfaceVariant = Color(0xFF332A22), // Orange-tinted dark surface
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

    background = Color(0xFFF6FBF7), // Very light green tint
    onBackground = Color(0xFF1A1C1E),

    surface = Color(0xFFF9FCF9), // Subtle green tint
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = Color(0xFFE8F5EC), // Light green-tinted surface
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

    background = Color(0xFF121614), // Dark with subtle green tint
    onBackground = Color(0xFFE1E3E0),

    surface = Color(0xFF1A1F1C), // Slightly elevated with green tint
    onSurface = Color(0xFFE1E3E0),
    surfaceVariant = Color(0xFF2A322C), // Green-tinted dark surface
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

    background = Color(0xFFFCF7FB), // Very light purple tint
    onBackground = Color(0xFF1A1C1E),

    surface = Color(0xFFFDFAFC), // Subtle purple tint
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = Color(0xFFF3E8F1), // Light purple-tinted surface
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

    background = Color(0xFF161214), // Dark with subtle purple tint
    onBackground = Color(0xFFE5E1E3),

    surface = Color(0xFF1F1A1D), // Slightly elevated with purple tint
    onSurface = Color(0xFFE5E1E3),
    surfaceVariant = Color(0xFF332A30), // Purple-tinted dark surface
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

    background = Color(0xFFF5FAFB), // Very light teal tint
    onBackground = Color(0xFF1A1C1E),

    surface = Color(0xFFF8FCFC), // Subtle teal tint
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = Color(0xFFE1F3F5), // Light teal-tinted surface
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

    background = Color(0xFF111616), // Dark with subtle teal tint
    onBackground = Color(0xFFE0E3E3),

    surface = Color(0xFF1A1F1F), // Slightly elevated with teal tint
    onSurface = Color(0xFFE0E3E3),
    surfaceVariant = Color(0xFF293232), // Teal-tinted dark surface
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

    background = Color(0xFFFFF8F8), // Very light red tint
    onBackground = Color(0xFF1A1C1E),

    surface = Color(0xFFFFFAFA), // Subtle red tint
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = Color(0xFFFFEBEE), // Light red-tinted surface
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

    background = Color(0xFF1A1212), // Dark with red tint
    onBackground = Color(0xFFE6DDDD),

    surface = Color(0xFF211818), // Slightly elevated with red tint
    onSurface = Color(0xFFE6DDDD),
    surfaceVariant = Color(0xFF332222), // Red-tinted dark surface
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

    background = Color(0xFFFFFCF5), // Very light amber tint
    onBackground = Color(0xFF1A1C1E),

    surface = Color(0xFFFFFDF8), // Subtle amber tint
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = Color(0xFFFFF8E1), // Light amber-tinted surface
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

    background = Color(0xFF1A1712), // Dark with amber tint
    onBackground = Color(0xFFE6E1DD),

    surface = Color(0xFF211E18), // Slightly elevated with amber tint
    onSurface = Color(0xFFE6E1DD),
    surfaceVariant = Color(0xFF332D22), // Amber-tinted dark surface
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

    background = Color(0xFFF8F9FB), // Very light indigo tint
    onBackground = Color(0xFF1A1C1E),

    surface = Color(0xFFFAFBFC), // Subtle indigo tint
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = Color(0xFFE8EAF6), // Light indigo-tinted surface
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

    background = Color(0xFF121416), // Dark with indigo tint
    onBackground = Color(0xFFE1E2E6),

    surface = Color(0xFF1A1C1F), // Slightly elevated with indigo tint
    onSurface = Color(0xFFE1E2E6),
    surfaceVariant = Color(0xFF2A2D35), // Indigo-tinted dark surface
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

    background = Color(0xFFF8F9FA), // Very light gray tint
    onBackground = Color(0xFF1A1C1E),

    surface = Color(0xFFFAFBFC), // Subtle gray tint
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = Color(0xFFECEFF1), // Light gray-tinted surface
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

    background = Color(0xFF141618), // Dark with neutral gray
    onBackground = Color(0xFFE2E3E5),

    surface = Color(0xFF1C1E20), // Slightly elevated gray
    onSurface = Color(0xFFE2E3E5),
    surfaceVariant = Color(0xFF2C3034), // Gray-tinted dark surface
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

    background = Color(0xFFFFF8F9), // Very light pink tint
    onBackground = Color(0xFF1A1C1E),

    surface = Color(0xFFFFFAFB), // Subtle pink tint
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = Color(0xFFFCE4EC), // Light pink-tinted surface
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

    background = Color(0xFF1A1214), // Dark with pink tint
    onBackground = Color(0xFFE5E1E2),

    surface = Color(0xFF211A1C), // Slightly elevated with pink tint
    onSurface = Color(0xFFE5E1E2),
    surfaceVariant = Color(0xFF332A2D), // Pink-tinted dark surface
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

    background = Color(0xFFF0FAF8), // Very light mint tint
    onBackground = Color(0xFF1A1C1E),

    surface = Color(0xFFF5FCFB), // Subtle mint tint
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = Color(0xFFE0F2F1), // Light mint-tinted surface
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

    background = Color(0xFF101816), // Dark with mint tint
    onBackground = Color(0xFFE1E7E5),

    surface = Color(0xFF1A201F), // Slightly elevated with mint tint
    onSurface = Color(0xFFE1E7E5),
    surfaceVariant = Color(0xFF28332F), // Mint-tinted dark surface
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

    background = Color(0xFFFAF7FC), // Very light lavender tint
    onBackground = Color(0xFF1A1C1E),

    surface = Color(0xFFFCFAFD), // Subtle lavender tint
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = Color(0xFFF3E5F5), // Light lavender-tinted surface
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

    background = Color(0xFF171218), // Dark with lavender tint
    onBackground = Color(0xFFE5E1E7),

    surface = Color(0xFF1F1A21), // Slightly elevated with lavender tint
    onSurface = Color(0xFFE5E1E7),
    surfaceVariant = Color(0xFF312A33), // Lavender-tinted dark surface
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

    background = Color(0xFFFFF9F7), // Very light coral tint
    onBackground = Color(0xFF1A1C1E),

    surface = Color(0xFFFFFBFA), // Subtle coral tint
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = Color(0xFFFFEBE5), // Light coral-tinted surface
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

    background = Color(0xFF1A1412), // Dark with coral tint
    onBackground = Color(0xFFE5E2E1),

    surface = Color(0xFF211C1A), // Slightly elevated with coral tint
    onSurface = Color(0xFFE5E2E1),
    surfaceVariant = Color(0xFF332C28), // Coral-tinted dark surface
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

    background = Color(0xFFF2FAF4), // Very light emerald tint
    onBackground = Color(0xFF1A1C1E),

    surface = Color(0xFFF7FCF8), // Subtle emerald tint
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = Color(0xFFE8F5E9), // Light emerald-tinted surface
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

    background = Color(0xFF101813), // Dark with emerald tint
    onBackground = Color(0xFFE1E7E3),

    surface = Color(0xFF1A201C), // Slightly elevated with emerald tint
    onSurface = Color(0xFFE1E7E3),
    surfaceVariant = Color(0xFF283330), // Emerald-tinted dark surface
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

    background = Color(0xFFF0FAFC), // Very light cyan tint
    onBackground = Color(0xFF1A1C1E),

    surface = Color(0xFFF5FCFE), // Subtle cyan tint
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = Color(0xFFE0F7FA), // Light cyan-tinted surface
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

    background = Color(0xFF101618), // Dark with cyan tint
    onBackground = Color(0xFFE1E6E7),

    surface = Color(0xFF1A1F20), // Slightly elevated with cyan tint
    onSurface = Color(0xFFE1E6E7),
    surfaceVariant = Color(0xFF283133), // Cyan-tinted dark surface
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

    background = Color(0xFFFAFAFA), // Almost white
    onBackground = Color(0xFF1A1C1E),

    surface = Color(0xFFFDFDFD), // Pure white
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = Color(0xFFF5F5F5), // Light gray surface
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

    background = Color(0xFF0A0A0A), // Almost pure black
    onBackground = Color(0xFFE5E5E5),

    surface = Color(0xFF121212), // True black surface
    onSurface = Color(0xFFE5E5E5),
    surfaceVariant = Color(0xFF1F1F1F), // Dark gray surface
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

    background = Color(0xFFFBFCFD), // Ice white
    onBackground = Color(0xFF1A1C1E),

    surface = Color(0xFFFEFEFF), // Pure ice white
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = Color(0xFFECEFF1), // Light ice gray
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

    background = Color(0xFF141618), // Dark ice gray
    onBackground = Color(0xFFE8EAEB),

    surface = Color(0xFF1C1E20), // Dark ice surface
    onSurface = Color(0xFFE8EAEB),
    surfaceVariant = Color(0xFF2C3034), // Ice-tinted dark surface
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

    background = Color(0xFFFFF8FA), // Very light magenta tint
    onBackground = Color(0xFF1A1C1E),

    surface = Color(0xFFFFFBFC), // Subtle magenta tint
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = Color(0xFFFCE4EC), // Light magenta surface
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

    background = Color(0xFF1A1214), // Dark with magenta tint
    onBackground = Color(0xFFE5E1E3),

    surface = Color(0xFF211A1D), // Slightly elevated with magenta
    onSurface = Color(0xFFE5E1E3),
    surfaceVariant = Color(0xFF332A2E), // Magenta-tinted dark surface
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

    background = Color(0xFFF7F9F3), // Very light olive tint
    onBackground = Color(0xFF1A1C1E),

    surface = Color(0xFFFAFCF7), // Subtle olive tint
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = Color(0xFFE8F5E9), // Light olive surface
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

    background = Color(0xFF121813), // Dark olive
    onBackground = Color(0xFFE3E7E1),

    surface = Color(0xFF1A201C), // Dark olive surface
    onSurface = Color(0xFFE3E7E1),
    surfaceVariant = Color(0xFF283330), // Olive-tinted dark surface
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

    background = Color(0xFFF9FAFB), // Ash white
    onBackground = Color(0xFF1A1C1E),

    surface = Color(0xFFFBFCFD), // Light ash
    onSurface = Color(0xFF1A1C1E),
    surfaceVariant = Color(0xFFECEFF1), // Volcanic ash gray
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

    background = Color(0xFF0F1315), // Volcanic dark
    onBackground = Color(0xFFE5E7E9),

    surface = Color(0xFF181B1D), // Dark ash surface
    onSurface = Color(0xFFE5E7E9),
    surfaceVariant = Color(0xFF272C2E), // Ash-tinted dark surface
    onSurfaceVariant = Color(0xFFC5C9CB),

    outline = VolcanicAshPalette.primaryDark.copy(alpha = 0.15f),
    outlineVariant = VolcanicAshPalette.primaryDark.copy(alpha = 0.10f)
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
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

