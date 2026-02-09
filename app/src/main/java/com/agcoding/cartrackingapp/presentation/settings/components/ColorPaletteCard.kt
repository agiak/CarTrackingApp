package com.agcoding.cartrackingapp.presentation.settings.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import com.agcoding.cartrackingapp.data.preferences.ColorPalette
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme

@Composable
fun ColorPaletteCard(
    selectedPalette: ColorPalette,
    onPaletteSelected: (ColorPalette) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Title
            Text(
                text = stringResource(R.string.color_palette_title),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Description
            Text(
                text = stringResource(R.string.color_palette_choose_description),
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            // System Colors Button
            PaletteOptionButton(
                title = stringResource(R.string.color_palette_wallpaper_colors),
                palette = ColorPalette.SYSTEM,
                isSelected = selectedPalette == ColorPalette.SYSTEM,
                onClick = { onPaletteSelected(ColorPalette.SYSTEM) },
                showSystemIcon = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Other Colors Label
            Text(
                text = stringResource(R.string.color_palette_other_colors),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Color Palette Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ColorPaletteItem(
                    palette = ColorPalette.DEFAULT_BLUE,
                    primaryColor = Color(0xFF1976D2),
                    secondaryColor = Color(0xFF0288D1),
                    isSelected = selectedPalette == ColorPalette.DEFAULT_BLUE,
                    onClick = { onPaletteSelected(ColorPalette.DEFAULT_BLUE) },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                ColorPaletteItem(
                    palette = ColorPalette.SUNSET_ORANGE,
                    primaryColor = Color(0xFFFF6F00),
                    secondaryColor = Color(0xFFFF8F00),
                    isSelected = selectedPalette == ColorPalette.SUNSET_ORANGE,
                    onClick = { onPaletteSelected(ColorPalette.SUNSET_ORANGE) },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                ColorPaletteItem(
                    palette = ColorPalette.FOREST_GREEN,
                    primaryColor = Color(0xFF2E7D32),
                    secondaryColor = Color(0xFF388E3C),
                    isSelected = selectedPalette == ColorPalette.FOREST_GREEN,
                    onClick = { onPaletteSelected(ColorPalette.FOREST_GREEN) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ColorPaletteItem(
                    palette = ColorPalette.ROYAL_PURPLE,
                    primaryColor = Color(0xFF6A1B9A),
                    secondaryColor = Color(0xFF8E24AA),
                    isSelected = selectedPalette == ColorPalette.ROYAL_PURPLE,
                    onClick = { onPaletteSelected(ColorPalette.ROYAL_PURPLE) },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                ColorPaletteItem(
                    palette = ColorPalette.OCEAN_TEAL,
                    primaryColor = Color(0xFF00796B),
                    secondaryColor = Color(0xFF00897B),
                    isSelected = selectedPalette == ColorPalette.OCEAN_TEAL,
                    onClick = { onPaletteSelected(ColorPalette.OCEAN_TEAL) },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                ColorPaletteItem(
                    palette = ColorPalette.CRIMSON_RED,
                    primaryColor = Color(0xFFC62828),
                    secondaryColor = Color(0xFFD32F2F),
                    isSelected = selectedPalette == ColorPalette.CRIMSON_RED,
                    onClick = { onPaletteSelected(ColorPalette.CRIMSON_RED) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ColorPaletteItem(
                    palette = ColorPalette.AMBER_GOLD,
                    primaryColor = Color(0xFFFF8F00),
                    secondaryColor = Color(0xFFFFA000),
                    isSelected = selectedPalette == ColorPalette.AMBER_GOLD,
                    onClick = { onPaletteSelected(ColorPalette.AMBER_GOLD) },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                ColorPaletteItem(
                    palette = ColorPalette.DEEP_INDIGO,
                    primaryColor = Color(0xFF283593),
                    secondaryColor = Color(0xFF3949AB),
                    isSelected = selectedPalette == ColorPalette.DEEP_INDIGO,
                    onClick = { onPaletteSelected(ColorPalette.DEEP_INDIGO) },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                ColorPaletteItem(
                    palette = ColorPalette.SLATE_GRAY,
                    primaryColor = Color(0xFF455A64),
                    secondaryColor = Color(0xFF546E7A),
                    isSelected = selectedPalette == ColorPalette.SLATE_GRAY,
                    onClick = { onPaletteSelected(ColorPalette.SLATE_GRAY) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ColorPaletteItem(
                    palette = ColorPalette.ROSE_PINK,
                    primaryColor = Color(0xFFAD1457),
                    secondaryColor = Color(0xFFC2185B),
                    isSelected = selectedPalette == ColorPalette.ROSE_PINK,
                    onClick = { onPaletteSelected(ColorPalette.ROSE_PINK) },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                ColorPaletteItem(
                    palette = ColorPalette.MINT_BREEZE,
                    primaryColor = Color(0xFF00897B),
                    secondaryColor = Color(0xFF26A69A),
                    isSelected = selectedPalette == ColorPalette.MINT_BREEZE,
                    onClick = { onPaletteSelected(ColorPalette.MINT_BREEZE) },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                ColorPaletteItem(
                    palette = ColorPalette.LAVENDER_DREAM,
                    primaryColor = Color(0xFF7B1FA2),
                    secondaryColor = Color(0xFF9C27B0),
                    isSelected = selectedPalette == ColorPalette.LAVENDER_DREAM,
                    onClick = { onPaletteSelected(ColorPalette.LAVENDER_DREAM) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ColorPaletteItem(
                    palette = ColorPalette.CORAL_SUNSET,
                    primaryColor = Color(0xFFE64A19),
                    secondaryColor = Color(0xFFFF5722),
                    isSelected = selectedPalette == ColorPalette.CORAL_SUNSET,
                    onClick = { onPaletteSelected(ColorPalette.CORAL_SUNSET) },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                ColorPaletteItem(
                    palette = ColorPalette.EMERALD_FOREST,
                    primaryColor = Color(0xFF1B5E20),
                    secondaryColor = Color(0xFF2E7D32),
                    isSelected = selectedPalette == ColorPalette.EMERALD_FOREST,
                    onClick = { onPaletteSelected(ColorPalette.EMERALD_FOREST) },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                ColorPaletteItem(
                    palette = ColorPalette.ELECTRIC_CYAN,
                    primaryColor = Color(0xFF0097A7),
                    secondaryColor = Color(0xFF00ACC1),
                    isSelected = selectedPalette == ColorPalette.ELECTRIC_CYAN,
                    onClick = { onPaletteSelected(ColorPalette.ELECTRIC_CYAN) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ColorPaletteItem(
                    palette = ColorPalette.MIDNIGHT_BLACK,
                    primaryColor = Color(0xFF212121),
                    secondaryColor = Color(0xFF424242),
                    isSelected = selectedPalette == ColorPalette.MIDNIGHT_BLACK,
                    onClick = { onPaletteSelected(ColorPalette.MIDNIGHT_BLACK) },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                ColorPaletteItem(
                    palette = ColorPalette.ICE_WHITE,
                    primaryColor = Color(0xFF37474F),
                    secondaryColor = Color(0xFFB0BEC5),
                    isSelected = selectedPalette == ColorPalette.ICE_WHITE,
                    onClick = { onPaletteSelected(ColorPalette.ICE_WHITE) },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                ColorPaletteItem(
                    palette = ColorPalette.NEON_MAGENTA,
                    primaryColor = Color(0xFFC2185B),
                    secondaryColor = Color(0xFFEC407A),
                    isSelected = selectedPalette == ColorPalette.NEON_MAGENTA,
                    onClick = { onPaletteSelected(ColorPalette.NEON_MAGENTA) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ColorPaletteItem(
                    palette = ColorPalette.DARK_OLIVE,
                    primaryColor = Color(0xFF33691E),
                    secondaryColor = Color(0xFF558B2F),
                    isSelected = selectedPalette == ColorPalette.DARK_OLIVE,
                    onClick = { onPaletteSelected(ColorPalette.DARK_OLIVE) },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                ColorPaletteItem(
                    palette = ColorPalette.VOLCANIC_ASH,
                    primaryColor = Color(0xFF263238),
                    secondaryColor = Color(0xFF455A64),
                    isSelected = selectedPalette == ColorPalette.VOLCANIC_ASH,
                    onClick = { onPaletteSelected(ColorPalette.VOLCANIC_ASH) },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                ColorPaletteItem(
                    palette = ColorPalette.SUNSET_FIRE,
                    primaryColor = Color(0xFFFF6D00),
                    secondaryColor = Color(0xFF4A148C),
                    isSelected = selectedPalette == ColorPalette.SUNSET_FIRE,
                    onClick = { onPaletteSelected(ColorPalette.SUNSET_FIRE) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ColorPaletteItem(
                    palette = ColorPalette.TROPICAL_PARADISE,
                    primaryColor = Color(0xFF00BFA5),
                    secondaryColor = Color(0xFFE91E63),
                    isSelected = selectedPalette == ColorPalette.TROPICAL_PARADISE,
                    onClick = { onPaletteSelected(ColorPalette.TROPICAL_PARADISE) },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                ColorPaletteItem(
                    palette = ColorPalette.ROYAL_GOLD,
                    primaryColor = Color(0xFFF57F17),
                    secondaryColor = Color(0xFF0D47A1),
                    isSelected = selectedPalette == ColorPalette.ROYAL_GOLD,
                    onClick = { onPaletteSelected(ColorPalette.ROYAL_GOLD) },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                ColorPaletteItem(
                    palette = ColorPalette.BERRY_BLAST,
                    primaryColor = Color(0xFF6A1B9A),
                    secondaryColor = Color(0xFF9E9D24),
                    isSelected = selectedPalette == ColorPalette.BERRY_BLAST,
                    onClick = { onPaletteSelected(ColorPalette.BERRY_BLAST) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ColorPaletteItem(
                    palette = ColorPalette.NEON_NIGHT,
                    primaryColor = Color(0xFF0091EA),
                    secondaryColor = Color(0xFFD500F9),
                    isSelected = selectedPalette == ColorPalette.NEON_NIGHT,
                    onClick = { onPaletteSelected(ColorPalette.NEON_NIGHT) },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                ColorPaletteItem(
                    palette = ColorPalette.AUTUMN_HARVEST,
                    primaryColor = Color(0xFFE65100),
                    secondaryColor = Color(0xFF4E342E),
                    isSelected = selectedPalette == ColorPalette.AUTUMN_HARVEST,
                    onClick = { onPaletteSelected(ColorPalette.AUTUMN_HARVEST) },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                ColorPaletteItem(
                    palette = ColorPalette.ARCTIC_FROST,
                    primaryColor = Color(0xFF006064),
                    secondaryColor = Color(0xFF0277BD),
                    isSelected = selectedPalette == ColorPalette.ARCTIC_FROST,
                    onClick = { onPaletteSelected(ColorPalette.ARCTIC_FROST) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ColorPaletteItem(
                    palette = ColorPalette.CHERRY_BLOSSOM,
                    primaryColor = Color(0xFFD32F2F),
                    secondaryColor = Color(0xFFC2185B),
                    isSelected = selectedPalette == ColorPalette.CHERRY_BLOSSOM,
                    onClick = { onPaletteSelected(ColorPalette.CHERRY_BLOSSOM) },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                ColorPaletteItem(
                    palette = ColorPalette.EMERALD_SEA,
                    primaryColor = Color(0xFF00695C),
                    secondaryColor = Color(0xFF01579B),
                    isSelected = selectedPalette == ColorPalette.EMERALD_SEA,
                    onClick = { onPaletteSelected(ColorPalette.EMERALD_SEA) },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                ColorPaletteItem(
                    palette = ColorPalette.GOLDEN_HOUR,
                    primaryColor = Color(0xFFF57C00),
                    secondaryColor = Color(0xFF6A1B9A),
                    isSelected = selectedPalette == ColorPalette.GOLDEN_HOUR,
                    onClick = { onPaletteSelected(ColorPalette.GOLDEN_HOUR) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ColorPaletteItem(
                    palette = ColorPalette.NEON_LIME,
                    primaryColor = Color(0xFFAEEA00),
                    secondaryColor = Color(0xFF4A148C),
                    isSelected = selectedPalette == ColorPalette.NEON_LIME,
                    onClick = { onPaletteSelected(ColorPalette.NEON_LIME) },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                ColorPaletteItem(
                    palette = ColorPalette.HOT_LAVA,
                    primaryColor = Color(0xFFDD2C00),
                    secondaryColor = Color(0xFF212121),
                    isSelected = selectedPalette == ColorPalette.HOT_LAVA,
                    onClick = { onPaletteSelected(ColorPalette.HOT_LAVA) },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                ColorPaletteItem(
                    palette = ColorPalette.CYBER_PINK,
                    primaryColor = Color(0xFFF50057),
                    secondaryColor = Color(0xFF00BFA5),
                    isSelected = selectedPalette == ColorPalette.CYBER_PINK,
                    onClick = { onPaletteSelected(ColorPalette.CYBER_PINK) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ColorPaletteItem(
                    palette = ColorPalette.OCEAN_SUNSET,
                    primaryColor = Color(0xFF01579B),
                    secondaryColor = Color(0xFFFF6E40),
                    isSelected = selectedPalette == ColorPalette.OCEAN_SUNSET,
                    onClick = { onPaletteSelected(ColorPalette.OCEAN_SUNSET) },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                ColorPaletteItem(
                    palette = ColorPalette.FOREST_AMBER,
                    primaryColor = Color(0xFF1B5E20),
                    secondaryColor = Color(0xFFFF6F00),
                    isSelected = selectedPalette == ColorPalette.FOREST_AMBER,
                    onClick = { onPaletteSelected(ColorPalette.FOREST_AMBER) },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                ColorPaletteItem(
                    palette = ColorPalette.SAPPHIRE_ROSE,
                    primaryColor = Color(0xFF1A237E),
                    secondaryColor = Color(0xFFE91E63),
                    isSelected = selectedPalette == ColorPalette.SAPPHIRE_ROSE,
                    onClick = { onPaletteSelected(ColorPalette.SAPPHIRE_ROSE) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ColorPaletteItem(
                    palette = ColorPalette.ELECTRIC_VIOLET,
                    primaryColor = Color(0xFF6200EA),
                    secondaryColor = Color(0xFFFFEA00),
                    isSelected = selectedPalette == ColorPalette.ELECTRIC_VIOLET,
                    onClick = { onPaletteSelected(ColorPalette.ELECTRIC_VIOLET) },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                ColorPaletteItem(
                    palette = ColorPalette.CANDY_CRUSH,
                    primaryColor = Color(0xFFE91E63),
                    secondaryColor = Color(0xFF00BCD4),
                    isSelected = selectedPalette == ColorPalette.CANDY_CRUSH,
                    onClick = { onPaletteSelected(ColorPalette.CANDY_CRUSH) },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                ColorPaletteItem(
                    palette = ColorPalette.MIDNIGHT_SUN,
                    primaryColor = Color(0xFF0D47A1),
                    secondaryColor = Color(0xFFFFD600),
                    isSelected = selectedPalette == ColorPalette.MIDNIGHT_SUN,
                    onClick = { onPaletteSelected(ColorPalette.MIDNIGHT_SUN) },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ColorPaletteItem(
                    palette = ColorPalette.STRAWBERRY_MINT,
                    primaryColor = Color(0xFFD32F2F),
                    secondaryColor = Color(0xFF00BFA5),
                    isSelected = selectedPalette == ColorPalette.STRAWBERRY_MINT,
                    onClick = { onPaletteSelected(ColorPalette.STRAWBERRY_MINT) },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(12.dp))
                // Empty space to balance the row
                Spacer(modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.width(12.dp))
                // Empty space to balance the row
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewColorPaletteCardLight() {
    CarTrackingAppTheme(darkTheme = false) {
        ColorPaletteCard(
            selectedPalette = ColorPalette.DEFAULT_BLUE,
            onPaletteSelected = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewColorPaletteCardDark() {
    CarTrackingAppTheme(darkTheme = true) {
        ColorPaletteCard(
            selectedPalette = ColorPalette.SUNSET_ORANGE,
            onPaletteSelected = {}
        )
    }
}

