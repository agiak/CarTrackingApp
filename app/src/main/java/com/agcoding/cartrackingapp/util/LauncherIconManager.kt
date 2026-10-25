package com.agcoding.cartrackingapp.util

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import com.agcoding.cartrackingapp.data.preferences.ColorPalette
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Keeps the launcher icon on the colour the app is painted in.
 *
 * Android has no way to retint an icon at runtime, so each palette ships its
 * own [android.R.attr.icon] on its own `<activity-alias>` and this class turns
 * exactly one of them on. The aliases are declared in the manifest; the names
 * below have to match them exactly, and both sides are generated from the same
 * enum by `gen_aliases.py`.
 *
 * Two things are load-bearing:
 *
 *  - The wanted alias is enabled **before** the others are disabled. For the
 *    moment in between, two entries are live, which a launcher shows as a
 *    duplicate icon for a frame. The other order leaves a moment with none,
 *    and a launcher that refreshes just then drops the app off the home
 *    screen for good.
 *  - [PackageManager.DONT_KILL_APP] on every call. Without it the system is
 *    free to kill the process mid-change, which would strand the aliases
 *    half-switched and take the user out of Settings while they were still
 *    tapping through palettes.
 */
@Singleton
class LauncherIconManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    /**
     * Points the launcher at [palette]'s icon. Does nothing if it is already
     * there, so this is safe to call on every palette change.
     */
    fun apply(palette: ColorPalette) {
        val wanted = aliasFor(palette)
        val packageManager = context.packageManager

        runCatching {
            if (isEnabled(packageManager, wanted)) return

            setEnabled(packageManager, wanted, enabled = true)
            ALL_ALIASES.filterNot { it == wanted }.forEach { alias ->
                setEnabled(packageManager, alias, enabled = false)
            }
            Timber.d("Launcher icon switched to $wanted")
        }.onFailure {
            // A launcher icon is not worth taking the app down for, and some
            // OEM builds refuse the call outright.
            Timber.w(it, "Could not switch the launcher icon to $wanted")
        }
    }

    /** Back to the icon the app ships with, for when the user turns this off. */
    fun reset() = apply(DEFAULT_PALETTE)

    private fun isEnabled(packageManager: PackageManager, alias: String): Boolean =
        when (packageManager.getComponentEnabledSetting(component(alias))) {
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED -> true
            // DEFAULT means "whatever the manifest says", and the manifest
            // enables exactly one alias.
            PackageManager.COMPONENT_ENABLED_STATE_DEFAULT -> alias == aliasFor(DEFAULT_PALETTE)
            else -> false
        }

    private fun setEnabled(packageManager: PackageManager, alias: String, enabled: Boolean) {
        packageManager.setComponentEnabledSetting(
            component(alias),
            if (enabled) {
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED
            } else {
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED
            },
            PackageManager.DONT_KILL_APP,
        )
    }

    private fun component(alias: String) = ComponentName(context.packageName, alias)

    private companion object {
        const val ALIAS_PREFIX = "com.agcoding.cartrackingapp."

        /**
         * The icon the app ships with, and what SYSTEM falls back to: dynamic
         * colour is decided from the wallpaper at runtime and no static
         * resource can follow it.
         */
        val DEFAULT_PALETTE = ColorPalette.SYSTEM

        /** Built from the enum, so it can never fall behind [aliasFor]. */
        val ALL_ALIASES: List<String> by lazy {
            ColorPalette.entries.map { aliasFor(it) }.distinct()
        }

        /**
         * Exhaustive on purpose: a palette added to the enum without an alias
         * in the manifest should fail the build here rather than silently
         * leave the icon behind.
         */
        fun aliasFor(palette: ColorPalette): String = when (palette) {
            ColorPalette.SYSTEM -> ALIAS_PREFIX + "LauncherIconSystem"
            ColorPalette.DEFAULT_BLUE -> ALIAS_PREFIX + "LauncherIconDefaultBlue"
            ColorPalette.SUNSET_ORANGE -> ALIAS_PREFIX + "LauncherIconSunsetOrange"
            ColorPalette.FOREST_GREEN -> ALIAS_PREFIX + "LauncherIconForestGreen"
            ColorPalette.ROYAL_PURPLE -> ALIAS_PREFIX + "LauncherIconRoyalPurple"
            ColorPalette.OCEAN_TEAL -> ALIAS_PREFIX + "LauncherIconOceanTeal"
            ColorPalette.CRIMSON_RED -> ALIAS_PREFIX + "LauncherIconCrimsonRed"
            ColorPalette.AMBER_GOLD -> ALIAS_PREFIX + "LauncherIconAmberGold"
            ColorPalette.DEEP_INDIGO -> ALIAS_PREFIX + "LauncherIconDeepIndigo"
            ColorPalette.SLATE_GRAY -> ALIAS_PREFIX + "LauncherIconSlateGray"
            ColorPalette.ROSE_PINK -> ALIAS_PREFIX + "LauncherIconRosePink"
            ColorPalette.MINT_BREEZE -> ALIAS_PREFIX + "LauncherIconMintBreeze"
            ColorPalette.LAVENDER_DREAM -> ALIAS_PREFIX + "LauncherIconLavenderDream"
            ColorPalette.CORAL_SUNSET -> ALIAS_PREFIX + "LauncherIconCoralSunset"
            ColorPalette.EMERALD_FOREST -> ALIAS_PREFIX + "LauncherIconEmeraldForest"
            ColorPalette.ELECTRIC_CYAN -> ALIAS_PREFIX + "LauncherIconElectricCyan"
            ColorPalette.MIDNIGHT_BLACK -> ALIAS_PREFIX + "LauncherIconMidnightBlack"
            ColorPalette.ICE_WHITE -> ALIAS_PREFIX + "LauncherIconIceWhite"
            ColorPalette.NEON_MAGENTA -> ALIAS_PREFIX + "LauncherIconNeonMagenta"
            ColorPalette.DARK_OLIVE -> ALIAS_PREFIX + "LauncherIconDarkOlive"
            ColorPalette.VOLCANIC_ASH -> ALIAS_PREFIX + "LauncherIconVolcanicAsh"
            ColorPalette.SUNSET_FIRE -> ALIAS_PREFIX + "LauncherIconSunsetFire"
            ColorPalette.TROPICAL_PARADISE -> ALIAS_PREFIX + "LauncherIconTropicalParadise"
            ColorPalette.ROYAL_GOLD -> ALIAS_PREFIX + "LauncherIconRoyalGold"
            ColorPalette.BERRY_BLAST -> ALIAS_PREFIX + "LauncherIconBerryBlast"
            ColorPalette.NEON_NIGHT -> ALIAS_PREFIX + "LauncherIconNeonNight"
            ColorPalette.AUTUMN_HARVEST -> ALIAS_PREFIX + "LauncherIconAutumnHarvest"
            ColorPalette.ARCTIC_FROST -> ALIAS_PREFIX + "LauncherIconArcticFrost"
            ColorPalette.CHERRY_BLOSSOM -> ALIAS_PREFIX + "LauncherIconCherryBlossom"
            ColorPalette.EMERALD_SEA -> ALIAS_PREFIX + "LauncherIconEmeraldSea"
            ColorPalette.GOLDEN_HOUR -> ALIAS_PREFIX + "LauncherIconGoldenHour"
            ColorPalette.NEON_LIME -> ALIAS_PREFIX + "LauncherIconNeonLime"
            ColorPalette.HOT_LAVA -> ALIAS_PREFIX + "LauncherIconHotLava"
            ColorPalette.CYBER_PINK -> ALIAS_PREFIX + "LauncherIconCyberPink"
            ColorPalette.OCEAN_SUNSET -> ALIAS_PREFIX + "LauncherIconOceanSunset"
            ColorPalette.FOREST_AMBER -> ALIAS_PREFIX + "LauncherIconForestAmber"
            ColorPalette.SAPPHIRE_ROSE -> ALIAS_PREFIX + "LauncherIconSapphireRose"
            ColorPalette.ELECTRIC_VIOLET -> ALIAS_PREFIX + "LauncherIconElectricViolet"
            ColorPalette.CANDY_CRUSH -> ALIAS_PREFIX + "LauncherIconCandyCrush"
            ColorPalette.MIDNIGHT_SUN -> ALIAS_PREFIX + "LauncherIconMidnightSun"
            ColorPalette.STRAWBERRY_MINT -> ALIAS_PREFIX + "LauncherIconStrawberryMint"
        }
    }
}
