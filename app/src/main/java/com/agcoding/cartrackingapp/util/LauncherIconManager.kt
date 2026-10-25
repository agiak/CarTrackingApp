package com.agcoding.cartrackingapp.util

import android.app.Activity
import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import com.agcoding.cartrackingapp.data.preferences.ColorPalette
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton
import java.util.concurrent.atomic.AtomicInteger

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
 *  - Nothing is switched while an activity of the app is on screen. The task
 *    the user is in was started from one of the aliases, and disabling that
 *    one makes the system finish the task — the app closes the moment a
 *    palette is picked, with the process still alive. So [request] only
 *    records what is wanted, and it is written once the app goes to the
 *    background.
 *  - [PackageManager.DONT_KILL_APP] on every call. Without it the system is
 *    free to kill the process mid-change, which would strand the aliases
 *    half-switched and take the user out of Settings while they were still
 *    tapping through palettes.
 */
@Singleton
class LauncherIconManager @Inject constructor(
    @ApplicationContext private val context: Context,
) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val startedActivities = AtomicInteger(0)

    @Volatile
    private var pending: ColorPalette? = null

    /**
     * Hooks the foreground/background tracking up. Called once from the
     * Application, before any activity exists.
     */
    fun attach(application: Application) {
        application.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
            override fun onActivityStarted(activity: Activity) {
                startedActivities.incrementAndGet()
            }

            override fun onActivityStopped(activity: Activity) {
                // A rotation stops and restarts the activity; that is not the
                // user leaving.
                if (activity.isChangingConfigurations) {
                    startedActivities.decrementAndGet()
                    return
                }
                if (startedActivities.decrementAndGet() == 0) flushPending()
            }

            override fun onActivityCreated(activity: Activity, savedInstanceState: android.os.Bundle?) = Unit
            override fun onActivityResumed(activity: Activity) = Unit
            override fun onActivityPaused(activity: Activity) = Unit
            override fun onActivitySaveInstanceState(activity: Activity, outState: android.os.Bundle) = Unit
            override fun onActivityDestroyed(activity: Activity) = Unit
        })
    }

    /**
     * Asks for [palette]'s icon. It takes effect when the app next goes to the
     * background — never at once, not even when no activity is up yet: at a
     * cold start the launch is already under way and the alias it came from
     * is the one that would be turned off. The last request wins, so tapping
     * through palettes costs one write, not one per tap.
     */
    fun request(palette: ColorPalette) {
        pending = palette
    }

    /** Back to the icon the app ships with, for when the user turns this off. */
    fun requestReset() = request(DEFAULT_PALETTE)

    private fun flushPending() {
        val palette = pending ?: return
        pending = null
        scope.launch { apply(palette) }
    }

    /**
     * Points the launcher at [palette]'s icon. Does nothing if it is already
     * there.
     *
     * Suspending, and off the main thread by itself rather than by agreement
     * with its callers: `setComponentEnabledSetting` is a synchronous binder
     * call that has the package manager write the new state to disk, and
     * there can be a handful of them in one switch. Run from the palette
     * picker's own coroutine — which is the main one — that is enough to
     * freeze the UI while the user is still tapping through colours.
     */
    private suspend fun apply(palette: ColorPalette) {
        val wanted = aliasFor(palette)
        val packageManager = context.packageManager

        withContext(Dispatchers.IO) {
            runCatching {
                // Read the whole set first. Asking is cheap and writing is not
                // — each write is persisted by the package manager — so this
                // both skips the 40 pointless disables of the common case and
                // notices when the state is wrong in a way a single check
                // would miss. Two aliases can end up live at once: the process
                // can die between the enable and the disables below, and
                // `adb shell pm enable` on one of them does the same thing.
                // Without a full read that leaves two icons on the home screen
                // with nothing to ever repair them.
                val enabledNow = ALL_ALIASES.filter { isEnabled(packageManager, it) }
                if (enabledNow == listOf(wanted)) return@runCatching

                // Enable before disabling: for the moment in between two
                // entries are live, which a launcher shows as a duplicate icon
                // for a frame. The other order leaves a moment with none.
                if (wanted !in enabledNow) setEnabled(packageManager, wanted, enabled = true)
                enabledNow.filterNot { it == wanted }.forEach { alias ->
                    setEnabled(packageManager, alias, enabled = false)
                }
                Timber.d("Launcher icon switched to $wanted")
            }.onFailure {
                // A launcher icon is not worth taking the app down for, and
                // some OEM builds refuse the call outright.
                Timber.w(it, "Could not switch the launcher icon to $wanted")
            }
        }
    }

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
