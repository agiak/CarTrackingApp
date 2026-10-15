package com.agcoding.cartrackingapp.presentation.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppColorScheme

/**
 * Two or three mutually exclusive modes, shown together.
 *
 * Where chips say "filter this down", a segmented control says "the screen is
 * one of these" — and crucially it shows the alternative. On the sign-in
 * screen that is the whole point: somebody who has no account should be able
 * to see that creating one is possible without reading to the bottom of the
 * form to find a link.
 *
 * The selected segment is filled with the accent, not merely tinted, because
 * with only two segments a subtle difference reads as "one is disabled".
 */
@Composable
fun SegmentedControl(
    options: List<String>,
    selectedIndex: Int,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (options.isEmpty()) return
    val scheme = LocalAppColorScheme.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(scheme.backgroundCard)
            .padding(4.dp),
    ) {
        options.forEachIndexed { index, label ->
            Segment(
                label = label,
                selected = index == selectedIndex,
                onClick = { onSelect(index) },
            )
        }
    }
}

@Composable
private fun RowScope.Segment(label: String, selected: Boolean, onClick: () -> Unit) {
    val scheme = LocalAppColorScheme.current

    val container by animateColorAsState(
        targetValue = if (selected) scheme.actionPrimary else scheme.backgroundCard,
        animationSpec = tween(150),
        label = "segmentContainer",
    )
    val content by animateColorAsState(
        targetValue = if (selected) scheme.actionContent else scheme.contentSecondary,
        animationSpec = tween(150),
        label = "segmentContent",
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .weight(1f)
            .fillMaxWidth()
            .clip(RoundedCornerShape(11.dp))
            .background(container)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleSmall,
            color = content,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Preview(showBackground = true, widthDp = 380)
@Composable
private fun SegmentedControlPreview() {
    CarTrackingAppTheme(darkTheme = true) {
        var selected by remember { mutableIntStateOf(0) }
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(18.dp),
        ) {
            SegmentedControl(
                options = listOf("Sign in", "Create account"),
                selectedIndex = selected,
                onSelect = { selected = it },
            )
            SegmentedControl(
                options = listOf("Consumption", "Distance", "Cost"),
                selectedIndex = 1,
                onSelect = {},
            )
        }
    }
}
