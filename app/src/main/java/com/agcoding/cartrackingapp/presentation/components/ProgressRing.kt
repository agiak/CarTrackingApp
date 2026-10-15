package com.agcoding.cartrackingapp.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppColorScheme
import com.agcoding.cartrackingapp.shared.ui.tokens.AppNumbers
import kotlin.math.roundToInt

/**
 * A long job, as a ring with the percentage in the middle.
 *
 * A linear bar across a mostly empty screen says "wait" without saying how
 * long; a ring with the figure inside it is the same information in a shape
 * the eye can read at a glance from across a room, and it leaves the width of
 * the screen free for the list of what is actually happening.
 *
 * The sweep starts at twelve o'clock and has rounded ends, so a job that has
 * barely begun still shows a visible mark rather than nothing at all.
 */
@Composable
fun ProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    diameter: Dp = 190.dp,
    thickness: Dp = 12.dp,
) {
    val scheme = LocalAppColorScheme.current
    val clamped = progress.coerceIn(0f, 1f)

    Box(contentAlignment = Alignment.Center, modifier = modifier) {
        Canvas(modifier = Modifier.size(diameter)) {
            val stroke = thickness.toPx()
            val inset = stroke / 2f
            val topLeft = Offset(inset, inset)
            val arcSize = Size(size.width - stroke, size.height - stroke)

            drawArc(
                color = scheme.backgroundCard,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
            )
            if (clamped > 0f) {
                drawArc(
                    color = scheme.actionPrimary,
                    startAngle = -90f,
                    sweepAngle = 360f * clamped,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = stroke, cap = StrokeCap.Round),
                )
            }
        }
        Text(
            text = "${(clamped * 100).roundToInt()}%",
            // Larger than any other figure in the app, because for the length
            // of this screen it is the only one.
            style = AppNumbers.summary.copy(fontSize = 46.sp, lineHeight = 52.sp),
            color = scheme.contentPrimary,
        )
    }
}

@Preview(showBackground = true, widthDp = 380, heightDp = 300)
@Composable
private fun ProgressRingPreview() {
    CarTrackingAppTheme(darkTheme = true) {
        Column(Modifier.padding(18.dp)) {
            ProgressRing(progress = 0.62f)
        }
    }
}
