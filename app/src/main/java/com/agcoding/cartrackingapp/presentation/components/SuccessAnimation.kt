package com.agcoding.cartrackingapp.presentation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.agcoding.cartrackingapp.presentation.theme.AppSuccess
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme
import com.agcoding.cartrackingapp.shared.ui.brand.ExpenseSaved
import com.agcoding.cartrackingapp.shared.ui.brand.RefillSaved
import com.agcoding.cartrackingapp.shared.ui.brand.WelcomeAboard
import com.agcoding.cartrackingapp.shared.ui.brand.rememberBrandColors
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppColorScheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * An animated success checkmark, drawn entirely with Compose primitives — no
 * animation library or bundled asset required.
 *
 * The animation runs in three overlapping beats:
 *  1. the filled disc springs in,
 *  2. a ring pulses outward and fades,
 *  3. the checkmark strokes itself on.
 *
 * [onAnimationEnd] fires once the stroke completes plus a short hold, so callers
 * can dismiss the surrounding sheet or navigate away.
 */
@Composable
fun SuccessCheckmark(
    modifier: Modifier = Modifier,
    size: Dp = 96.dp,
    color: Color = AppSuccess.color,
    containerColor: Color = AppSuccess.container,
    holdMillis: Long = 550,
    onAnimationEnd: (() -> Unit)? = null
) {
    val discScale = remember { Animatable(0f) }
    val ringProgress = remember { Animatable(0f) }
    val checkProgress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        launch {
            discScale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            )
        }
        launch {
            delay(90)
            ringProgress.animateTo(1f, tween(durationMillis = 620, easing = LinearOutSlowInEasing))
        }
        delay(180)
        checkProgress.animateTo(1f, tween(durationMillis = 380, easing = FastOutSlowInEasing))
        if (onAnimationEnd != null) {
            delay(holdMillis)
            onAnimationEnd()
        }
    }

    Canvas(modifier = modifier.size(size)) {
        val dimension = this.size.minDimension
        val center = Offset(this.size.width / 2f, this.size.height / 2f)
        val baseRadius = dimension / 2f

        // 2. Pulsing ring — expands past the disc while fading out.
        val ring = ringProgress.value
        if (ring > 0f && ring < 1f) {
            drawCircle(
                color = color,
                radius = baseRadius * (0.72f + 0.5f * ring),
                center = center,
                alpha = (1f - ring) * 0.35f,
                style = Stroke(width = dimension * 0.045f)
            )
        }

        // 1. Filled disc.
        val scale = discScale.value.coerceAtLeast(0f)
        if (scale > 0f) {
            drawCircle(
                color = containerColor,
                radius = baseRadius * 0.72f * scale,
                center = center
            )
        }

        // 3. Checkmark, stroked on progressively.
        val progress = checkProgress.value
        if (progress > 0f && scale > 0f) {
            // Check geometry as fractions of the canvas box.
            val start = Offset(dimension * 0.31f, dimension * 0.52f)
            val elbow = Offset(dimension * 0.44f, dimension * 0.65f)
            val end = Offset(dimension * 0.70f, dimension * 0.37f)

            val firstLength = (elbow - start).getDistance()
            val secondLength = (end - elbow).getDistance()
            val totalLength = firstLength + secondLength
            val drawn = totalLength * progress

            val strokeWidth = dimension * 0.085f

            if (drawn <= firstLength) {
                val t = if (firstLength == 0f) 0f else drawn / firstLength
                drawLine(
                    color = color,
                    start = start,
                    end = Offset(
                        start.x + (elbow.x - start.x) * t,
                        start.y + (elbow.y - start.y) * t
                    ),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round
                )
            } else {
                drawLine(
                    color = color,
                    start = start,
                    end = elbow,
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round
                )
                val t = if (secondLength == 0f) 0f else (drawn - firstLength) / secondLength
                drawLine(
                    color = color,
                    start = elbow,
                    end = Offset(
                        elbow.x + (end.x - elbow.x) * t,
                        elbow.y + (end.y - elbow.y) * t
                    ),
                    strokeWidth = strokeWidth,
                    cap = StrokeCap.Round
                )
            }
        }
    }
}

/**
 * Which confirmation a save shows.
 *
 * [GENERIC] is the tick that every save used to get. The others are the
 * branded animations — a tank filling, a coin landing in a wallet, the caribou
 * arriving — which say what was saved as well as that it was, and are worth
 * the extra second only where the thing saved is the point of the screen.
 */
enum class SavedKind { GENERIC, REFILL, EXPENSE, ACCOUNT }

/** How long the last frame is held before the sheet closes over it. */
private const val SETTLE_MILLIS = 550L

/** Wide enough to read the tank's level and the coin's face at arm's length. */
private val ANIMATION_WIDTH = 190.dp

/**
 * Full-surface success state: the animation above a short confirmation
 * message. Drop this in place of a form's content once a save succeeds, and use
 * [onFinished] to close the sheet or screen when the animation settles.
 *
 * [surface] is the colour behind this — the sheet's card, the screen's
 * background — because the branded animations cut their ticks and wheel hubs
 * out of whatever they sit on rather than painting them a colour of their own.
 */
@Composable
fun SuccessAnimation(
    message: String,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    kind: SavedKind = SavedKind.GENERIC,
    surface: Color = LocalAppColorScheme.current.backgroundCard,
    onFinished: (() -> Unit)? = null
) {
    val scope = rememberCoroutineScope()
    // The branded animations end on their last frame and say so immediately;
    // the tick holds by itself. Giving them the same pause means every
    // confirmation in the app is on screen for the same length of time.
    val finishAfterHold: () -> Unit = {
        scope.launch {
            delay(SETTLE_MILLIS)
            onFinished?.invoke()
        }
    }
    val brand = rememberBrandColors(surface = surface)

    Surface(
        modifier = modifier,
        color = Color.Transparent
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            when (kind) {
                SavedKind.GENERIC -> SuccessCheckmark(size = 112.dp, onAnimationEnd = onFinished)
                SavedKind.REFILL -> RefillSaved(
                    modifier = Modifier.width(ANIMATION_WIDTH),
                    colors = brand,
                    onFinished = finishAfterHold,
                )
                SavedKind.EXPENSE -> ExpenseSaved(
                    modifier = Modifier.width(ANIMATION_WIDTH),
                    colors = brand,
                    onFinished = finishAfterHold,
                )
                SavedKind.ACCOUNT -> WelcomeAboard(
                    modifier = Modifier.width(ANIMATION_WIDTH),
                    colors = brand,
                    onFinished = finishAfterHold,
                )
            }

            Text(
                text = message,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 22.dp)
            )

            if (supportingText != null) {
                Text(
                    text = supportingText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }
    }
}

/**
 * The app-wide success confirmation: a bottom sheet that plays
 * [SuccessAnimation] and calls [onFinished] when it settles — typically to
 * navigate back or close a dialog.
 *
 * A sheet rather than the full-screen overlay this used to be, so that every
 * confirmation in the app arrives the same way. Saving from a sheet already
 * played its animation in that sheet; saving from a screen covered the screen
 * instead, which made the same event look like two different ones depending
 * on where it was triggered from.
 *
 * It also means the confirmation is never mistaken for the next screen: the
 * one underneath stays visible behind the scrim, so "saved" reads as something
 * that happened to the screen the user is on rather than as navigation that
 * has already taken place.
 *
 * Renders nothing while [visible] is false.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SuccessOverlay(
    visible: Boolean,
    message: String,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    kind: SavedKind = SavedKind.GENERIC,
    onFinished: () -> Unit
) {
    if (!visible) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val scheme = LocalAppColorScheme.current

    AppBottomSheet(
        // Swiping it away is the same answer as waiting for it: the work is
        // already done, and the caller is owed its onFinished either way.
        // Dropping the request instead would leave a sheet the user has
        // pulled halfway down and a screen that never moves on.
        onDismissRequest = onFinished,
        modifier = modifier,
        sheetState = sheetState,
        // No handle. The sheet leaves on its own a second and a half from now,
        // and a grab bar invites a drag that only skips the confirmation.
        showDragHandle = false,
    ) {
        SuccessAnimation(
            message = message,
            supportingText = supportingText,
            kind = kind,
            // The sheet's own container, which is what the animation's ticks
            // and wheel hubs are cut out of.
            surface = scheme.backgroundCard,
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            onFinished = {
                // Let the sheet slide out before the caller navigates, so the
                // screen change happens behind a sheet that is already gone
                // rather than under one that vanishes mid-animation.
                scope.launch { sheetState.hide() }.invokeOnCompletion {
                    if (!sheetState.isVisible) onFinished()
                }
            }
        )
    }
}

// ============================================
// Preview Composables
// ============================================

@Preview(name = "Success - Light", showBackground = true, widthDp = 380, heightDp = 320)
@Composable
private fun PreviewSuccessAnimationLight() {
    CarTrackingAppTheme(darkTheme = false) {
        Box(modifier = Modifier.fillMaxSize()) {
            SuccessAnimation(message = "Expense saved")
        }
    }
}

@Preview(name = "Success - Dark", showBackground = true, widthDp = 380, heightDp = 320)
@Composable
private fun PreviewSuccessAnimationDark() {
    CarTrackingAppTheme(darkTheme = true) {
        Box(modifier = Modifier.fillMaxSize()) {
            SuccessAnimation(message = "Expense saved", supportingText = "Fuel · 45,00 €")
        }
    }
}
