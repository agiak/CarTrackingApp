package com.agcoding.cartrackingapp.presentation.onboarding

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalGasStation
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

/** Step 1: the total-distance reading, which is what the app asks for up front. */
@Composable
private fun OdometerIllustration(accent: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = Icons.Default.DirectionsCar,
            contentDescription = null,
            tint = accent,
            modifier = Modifier.size(40.dp)
        )
        Spacer(modifier = Modifier.height(14.dp))
        MeterPanel(
            label = stringResource(R.string.onboarding_meter_odo),
            digits = "124580",
            unit = stringResource(R.string.onboarding_meter_unit_km),
            accent = accent,
            highlighted = true,
        )
    }
}

/**
 * Step 2: the same dashboard, but the *trip* row, with the reset shown as the
 * thing you do to it. The arrow is a text glyph rather than an icon so that
 * nothing here depends on an icon name.
 */
@Composable
private fun TripMeterIllustration(accent: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = Icons.Default.LocalGasStation,
            contentDescription = null,
            tint = accent,
            modifier = Modifier.size(40.dp)
        )
        Spacer(modifier = Modifier.height(14.dp))
        MeterPanel(
            label = stringResource(R.string.onboarding_meter_trip),
            digits = "4125",
            unit = stringResource(R.string.onboarding_meter_unit_km),
            accent = accent,
            highlighted = true,
            decimalFromEnd = 1,
        )
        Spacer(modifier = Modifier.height(10.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50))
                    .background(accent.copy(alpha = 0.14f))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "↺  " + stringResource(R.string.onboarding_meter_reset),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = accent
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = stringResource(R.string.onboarding_meter_reset_hint),
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/** Step 3: the kinds of thing that are not fuel. Chips, so no icons are needed. */
@Composable
private fun CostsIllustration(accent: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = Icons.Default.Build,
            contentDescription = null,
            tint = accent,
            modifier = Modifier.size(40.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CostChip(stringResource(R.string.onboarding_chip_service), accent)
            CostChip(stringResource(R.string.onboarding_chip_wash), accent)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CostChip(stringResource(R.string.onboarding_chip_tyres), accent)
            CostChip(stringResource(R.string.onboarding_chip_insurance), accent)
        }
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

/** Step 4: what comes out the other end. Bars, drawn to a fixed shape. */
@Composable
private fun StatsIllustration(accent: Color) {
    val heights = listOf(34.dp, 52.dp, 40.dp, 68.dp, 58.dp, 80.dp)
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            heights.forEachIndexed { index, barHeight ->
                val isLast = index == heights.lastIndex
                Box(
                    modifier = Modifier
                        .width(18.dp)
                        .height(barHeight)
                        .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                        .background(
                            if (isLast) accent else accent.copy(alpha = 0.3f)
                        )
                )
            }
        }
        Box(
            modifier = Modifier
                .padding(top = 6.dp)
                .width(158.dp)
                .height(2.dp)
                .background(MaterialTheme.colorScheme.outlineVariant)
        )
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
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 2.sp,
            color = if (highlighted) accent else cellColor.copy(alpha = 0.7f)
        )
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
