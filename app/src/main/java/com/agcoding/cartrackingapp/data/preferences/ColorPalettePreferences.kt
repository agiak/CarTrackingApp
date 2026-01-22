package com.agcoding.cartrackingapp.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.colorPaletteDataStore: DataStore<Preferences> by preferencesDataStore(name = "color_palette_settings")

enum class ColorPalette {
    SYSTEM,           // Follow system colors (Dynamic colors on Android 12+)
    DEFAULT_BLUE,     // Blue palette
    SUNSET_ORANGE,    // Orange/warm palette
    FOREST_GREEN,     // Green palette
    ROYAL_PURPLE,     // Purple palette
    OCEAN_TEAL,       // Teal/cyan palette
    CRIMSON_RED,      // Red palette
    AMBER_GOLD,       // Amber/gold palette
    DEEP_INDIGO,      // Indigo/blue-purple palette
    SLATE_GRAY,       // Gray/neutral palette
    ROSE_PINK,        // Pink palette
    MINT_BREEZE,      // Mint/turquoise palette
    LAVENDER_DREAM,   // Lavender/violet palette
    CORAL_SUNSET,     // Coral/peach palette
    EMERALD_FOREST,   // Emerald green palette
    ELECTRIC_CYAN,    // Electric cyan palette
    MIDNIGHT_BLACK,   // Dark gray/black palette
    ICE_WHITE,        // Ice white/blue gray palette
    NEON_MAGENTA,     // Neon magenta/pink palette
    DARK_OLIVE,       // Dark olive/green palette
    VOLCANIC_ASH,     // Volcanic ash gray palette

    // First wave high-contrast palettes
    SUNSET_FIRE,      // Bold orange and deep purple
    TROPICAL_PARADISE, // Turquoise and hot pink
    ROYAL_GOLD,       // Rich gold and navy blue
    BERRY_BLAST,      // Berry purple and lime green
    NEON_NIGHT,       // Electric blue and neon pink
    AUTUMN_HARVEST,   // Burnt orange and deep brown
    ARCTIC_FROST,     // Ice blue and deep teal
    CHERRY_BLOSSOM,   // Cherry red and soft pink
    EMERALD_SEA,      // Deep emerald and ocean blue
    GOLDEN_HOUR,      // Warm gold and deep violet

    // Second wave high-contrast palettes
    NEON_LIME,        // Electric lime and deep purple
    HOT_LAVA,         // Red-orange and charcoal black
    CYBER_PINK,       // Hot pink and electric teal
    OCEAN_SUNSET,     // Deep blue and coral
    FOREST_AMBER,     // Deep forest green and amber
    SAPPHIRE_ROSE,    // Deep sapphire and rose pink
    ELECTRIC_VIOLET,  // Electric violet and neon yellow
    CANDY_CRUSH,      // Bubblegum pink and bright cyan
    MIDNIGHT_SUN,     // Navy blue and golden yellow
    STRAWBERRY_MINT   // Strawberry red and fresh mint
}

@Singleton
class ColorPalettePreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object PreferencesKeys {
        val SELECTED_PALETTE = stringPreferencesKey("selected_color_palette")
    }

    val selectedPaletteFlow: Flow<ColorPalette> = context.colorPaletteDataStore.data
        .map { preferences ->
            val paletteString = preferences[PreferencesKeys.SELECTED_PALETTE] ?: ColorPalette.SYSTEM.name
            try {
                ColorPalette.valueOf(paletteString)
            } catch (e: IllegalArgumentException) {
                ColorPalette.SYSTEM
            }
        }

    suspend fun setColorPalette(palette: ColorPalette) {
        context.colorPaletteDataStore.edit { preferences ->
            preferences[PreferencesKeys.SELECTED_PALETTE] = palette.name
        }
    }
}
