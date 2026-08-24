package com.agcoding.cartrackingapp.presentation.refill.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.domain.model.VoiceRefillData
import com.agcoding.cartrackingapp.presentation.theme.AppSuccess

/**
 * Listening indicator with animated waveform.
 *
 * @param captured the fields heard so far — shown as three pills so the user can
 *   see what is still missing, and why recording ends by itself once all three
 *   are green.
 */
@Composable
fun VoiceListeningIndicator(
    partialText: String,
    modifier: Modifier = Modifier,
    captured: VoiceRefillData = VoiceRefillData()
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Animated microphone icon
        Box(
            modifier = Modifier
                .size(80.dp)
                .scale(scale)
                .background(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Mic,
                contentDescription = stringResource(R.string.voice_listening),
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.voice_listening),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        VoiceCapturedFieldsRow(captured = captured)

        Spacer(modifier = Modifier.height(8.dp))

        if (partialText.isNotBlank()) {
            Text(
                text = partialText,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (captured.isComplete()) {
                    stringResource(R.string.voice_all_fields_captured)
                } else {
                    stringResource(R.string.voice_tap_stop_when_finished)
                },
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = if (captured.isComplete()) AppSuccess.color
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        } else {
            Text(
                text = stringResource(R.string.voice_speak_refill_data),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * The three refill fields shown as pills that turn green as the recognizer picks
 * them up. Once all three are green the recording stops on its own, so this row
 * doubles as an explanation of what just happened — and, before anything is
 * spoken, as a reminder of what to say.
 */
@Composable
fun VoiceCapturedFieldsRow(
    captured: VoiceRefillData,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CapturedFieldPill(
            label = stringResource(R.string.voice_field_cost),
            captured = (captured.cost ?: 0.0) > 0.0
        )
        CapturedFieldPill(
            label = stringResource(R.string.voice_field_liters),
            captured = (captured.liters ?: 0.0) > 0.0
        )
        CapturedFieldPill(
            label = stringResource(R.string.voice_field_distance),
            captured = (captured.distance ?: 0.0) > 0.0
        )
    }
}

@Composable
private fun CapturedFieldPill(
    label: String,
    captured: Boolean
) {
    Row(
        modifier = Modifier
            .background(
                color = if (captured) AppSuccess.container
                        else MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(50)
            )
            .padding(horizontal = 10.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (captured) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = AppSuccess.color
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (captured) AppSuccess.color
                    else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Processing indicator
 */
@Composable
fun VoiceProcessingIndicator(
    transcript: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(60.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.voice_processing),
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = transcript,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

