package com.agcoding.cartrackingapp.presentation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
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
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme
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
    color: Color = MaterialTheme.colorScheme.primary,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
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
 * Full-surface success state: the animated checkmark above a short confirmation
 * message. Drop this in place of a form's content once a save succeeds, and use
 * [onFinished] to close the sheet or screen when the animation settles.
 */
@Composable
fun SuccessAnimation(
    message: String,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    onFinished: (() -> Unit)? = null
) {
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
            SuccessCheckmark(onAnimationEnd = onFinished)

            Text(
                text = message,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 20.dp)
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
