package com.agcoding.cartrackingapp.presentation.onboarding

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.graphicsLayer
import com.agcoding.cartrackingapp.shared.ui.tokens.AppNumbers
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme

/**
 * The picture at the top of each guide step.
 *
 * These are drawn from boxes and text rather than shipped as images, for one
 * reason that matters more than file size: they are built from the theme's own
 * colours, so they are still legible after the user picks a palette, and in
 * dark mode, and at any font scale. A flat PNG of a dashboard would be none of
 * those things.
 *
 * Two of them are small reproductions of a real odometer, because the step
 * they illustrate is an instruction about a physical thing in the car. Showing
 * what to look at beats describing it.
 */
@Composable
fun SlideIllustration(
    slideIndex: Int,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        when (slideIndex) {
            0 -> OdometerIllustration(accent)
            1 -> TripMeterIllustration(accent)
            2 -> CostsIllustration(accent)
            else -> StatsIllustration(accent)
        }
    }
}

/**
 * Step 1: the two readings on the dashboard, and which of them the app wants.
 *
 * It shows both rows rather than only the right one, because the mistake this
 * step exists to prevent is not "I could not find a number" — it is "I typed
 * the wrong number". The trip row is dimmed and slightly smaller so the pair
 * reads as one choice already made, not as two things to compare.
 */
@Composable
private fun OdometerIllustration(accent: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        MeterPanel(
            label = stringResource(R.string.onboarding_meter_odo),
            digits = "124580",
            unit = stringResource(R.string.onboarding_meter_unit_km),
            accent = accent,
            highlighted = true,
            tag = stringResource(R.string.onboarding_meter_use_this),
        )
        Spacer(modifier = Modifier.height(12.dp))
        Box(
            modifier = Modifier.graphicsLayer {
                scaleX = 0.92f
                scaleY = 0.92f
                alpha = 0.4f
            }
        ) {
            MeterPanel(
                label = stringResource(R.string.onboarding_meter_trip),
                digits = "6412",
                unit = stringResource(R.string.onboarding_meter_unit_km),
                accent = accent,
                decimalFromEnd = 1,
            )
        }
    }
}

/**
 * Step 2: the same dashboard with the roles swapped, and the reset actually
 * happening.
 *
 * The trip digits drop to zero on a loop and the button pulses while they do.
 * A still picture of a reset button is a picture of a button; the only way to
 * show what "reset it" means without a sentence is to let it happen, and this
 * is the one instruction in the guide that users get wrong by not doing it at
 * all rather than by doing it badly.
 */
@Composable
private fun TripMeterIllustration(accent: Color) {
    val loop = rememberInfiniteTransition(label = "trip-reset")
    // Most of the cycle sits on the driven distance, then snaps to zero and
    // holds just long enough to be read before starting over.
    val phase by loop.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = LinearEasing),
        ),
        label = "trip-phase",
    )
    val pulse by loop.animateFloat(
        initialValue = 1f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "reset-pulse",
    )
    val digits = if (phase < 0.62f) "6412" else "0000"

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier.graphicsLayer {
                scaleX = 0.92f
                scaleY = 0.92f
                alpha = 0.4f
            }
        ) {
            MeterPanel(
                label = stringResource(R.string.onboarding_meter_odo),
                digits = "124580",
                unit = stringResource(R.string.onboarding_meter_unit_km),
                accent = accent,
            )
        }
        Spacer(modifier = Modifier.height(12.dp))
        MeterPanel(
            label = stringResource(R.string.onboarding_meter_trip),
            digits = digits,
            unit = stringResource(R.string.onboarding_meter_unit_km),
            accent = accent,
            highlighted = true,
            decimalFromEnd = 1,
        )
        Spacer(modifier = Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = pulse
                        scaleY = pulse
                    }
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.18f)),
            ) {
                Icon(
                    imageVector = Icons.Default.RestartAlt,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(19.dp),
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = stringResource(R.string.onboarding_meter_reset_hint),
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Step 3: the kinds of thing that are not fuel, and the reason to record them
 * — the reminder that comes back later.
 */
@Composable
private fun CostsIllustration(accent: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CostChip(stringResource(R.string.onboarding_chip_service), accent)
            CostChip(stringResource(R.string.onboarding_chip_wash), accent)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CostChip(stringResource(R.string.onboarding_chip_tyres), accent)
            CostChip(stringResource(R.string.onboarding_chip_insurance), accent)
        }
        Spacer(modifier = Modifier.height(16.dp))
        // Filled rather than outlined, so they read as something the app will
        // say back to you rather than as two more things to tap.
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ReminderPill(stringResource(R.string.onboarding_reminder_in_days), accent)
            ReminderPill(stringResource(R.string.onboarding_reminder_in_km), accent)
        }
    }
}

@Composable
private fun ReminderPill(label: String, accent: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(accent.copy(alpha = 0.18f))
            .padding(horizontal = 12.dp, vertical = 7.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Notifications,
            contentDescription = null,
            tint = accent,
            modifier = Modifier.size(13.dp),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = accent
        )
    }
}

@Composable
private fun CostChip(label: String, accent: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(accent.copy(alpha = 0.12f))
            .border(1.dp, accent.copy(alpha = 0.25f), RoundedCornerShape(50))
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * Step 4: what comes out the other end — the two figures the whole app is for,
 * over the shape of a year's spending.
 */
@Composable
private fun StatsIllustration(accent: Color) {
    val heights = listOf(26.dp, 40.dp, 31.dp, 52.dp, 44.dp, 36.dp, 60.dp)
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatTile(
                label = stringResource(R.string.car_card_avg_consumption),
                value = "8,7",
                unit = "L/100",
                accent = accent,
            )
            StatTile(
                label = stringResource(R.string.car_card_cost_per_km),
                value = "0,40",
                unit = "\u20ac",
                accent = accent,
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            heights.forEachIndexed { index, barHeight ->
                val isLast = index == heights.lastIndex
                Box(
                    modifier = Modifier
                        .width(16.dp)
                        .height(barHeight)
                        .clip(RoundedCornerShape(topStart = 5.dp, topEnd = 5.dp))
                        .background(if (isLast) accent else accent.copy(alpha = 0.28f))
                )
            }
        }
        Box(
            modifier = Modifier
                .padding(top = 6.dp)
                .width(160.dp)
                .height(2.dp)
                .background(MaterialTheme.colorScheme.outlineVariant)
        )
    }
}

@Composable
private fun StatTile(label: String, value: String, unit: String, accent: Color) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(accent.copy(alpha = 0.10f))
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Text(
            text = label,
            fontSize = 10.5.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
        )
        Spacer(modifier = Modifier.height(3.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = value,
                style = AppNumbers.tile,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.width(3.dp))
            Text(
                text = unit,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 3.dp),
            )
        }
    }
}

/**
 * A dashboard readout: a dark panel, a small label, and digits in separate
 * cells the way a mechanical odometer shows them.
 *
 * [decimalFromEnd] puts a decimal point that many digits from the right, which
 * is how a trip meter displays tenths — and showing tenths is what makes it
 * recognisable as the trip row rather than the total.
 */
@Composable
private fun MeterPanel(
    label: String,
    digits: String,
    unit: String,
    accent: Color,
    highlighted: Boolean = false,
    decimalFromEnd: Int = 0,
    tag: String? = null,
) {
    val panelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.88f)
    val cellColor = MaterialTheme.colorScheme.surface

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(panelColor)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                color = if (highlighted) accent else cellColor.copy(alpha = 0.7f)
            )
            if (tag != null) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(accent)
                        .padding(horizontal = 7.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = tag,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = panelColor
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            digits.forEachIndexed { index, digit ->
                val isDecimal = decimalFromEnd > 0 && index == digits.length - decimalFromEnd
                if (isDecimal) {
                    Text(
                        text = ".",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = accent,
                        modifier = Modifier.padding(horizontal = 1.dp)
                    )
                }
                Box(
                    modifier = Modifier
                        .padding(horizontal = 1.5.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(cellColor.copy(alpha = 0.16f))
                        .padding(horizontal = 5.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = digit.toString(),
                        fontSize = 20.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = cellColor
                    )
                }
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = unit,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = cellColor.copy(alpha = 0.75f),
                modifier = Modifier.padding(bottom = 3.dp)
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 380, heightDp = 240)
@Composable
private fun PreviewOdometer() {
    CarTrackingAppTheme {
        Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
            SlideIllustration(slideIndex = 0, accent = MaterialTheme.colorScheme.primary)
        }
    }
}

@Preview(showBackground = true, widthDp = 380, heightDp = 280)
@Composable
private fun PreviewTripMeter() {
    CarTrackingAppTheme {
        Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
            SlideIllustration(slideIndex = 1, accent = MaterialTheme.colorScheme.tertiary)
        }
    }
}

@Preview(showBackground = true, widthDp = 380, heightDp = 240)
@Composable
private fun PreviewCosts() {
    CarTrackingAppTheme {
        Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
            SlideIllustration(slideIndex = 2, accent = MaterialTheme.colorScheme.secondary)
        }
    }
}

@Preview(showBackground = true, widthDp = 380, heightDp = 240)
@Composable
private fun PreviewStats() {
    CarTrackingAppTheme(darkTheme = true) {
        Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
            SlideIllustration(slideIndex = 3, accent = MaterialTheme.colorScheme.primary)
        }
    }
}
