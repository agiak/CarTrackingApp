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
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

