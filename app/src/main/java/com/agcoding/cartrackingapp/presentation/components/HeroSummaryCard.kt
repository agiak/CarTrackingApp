package com.agcoding.cartrackingapp.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppColorScheme
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppDimens
import com.agcoding.cartrackingapp.shared.ui.tokens.AppNumbers

/**
 * The one figure a sub-screen exists to show, filled with the accent.
 *
 * Every analysis screen in the app opens with a number — the month's total,
 * the selected point on a graph, next month's forecast, how many insights
 * there are. They were all set differently: some bold-24, some coloured, some
 * inside an ordinary card indistinguishable from the three cards under it. A
 * reader arriving from the Statistics screen had to work out, each time, which
 * part of the screen was the answer.
 *
 * This is that part, and only one of them may appear per screen. Everything
 * else on the page is evidence for it.
 *
 * @param trailing shown on the figure's baseline — a unit, a change, a period.
 * @param content anything that belongs inside the accent block rather than
 *   below it: a split bar, a row of sub-figures. It inherits the on-accent
 *   colour, so callers must not set their own.
 */
@Composable
fun HeroSummaryCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    caption: String? = null,
    trailing: String? = null,
    content: @Composable ColumnScope.() -> Unit = {},
) {
    val scheme = LocalAppColorScheme.current
    val dimens = LocalAppDimens.current
    val ink = scheme.actionContent

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(dimens.radius.hero))
            .background(scheme.actionPrimary)
            .padding(20.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            // Not full strength: the label names the figure, it is not the
            // figure, and at full opacity on a saturated fill the two compete.
            color = ink.copy(alpha = 0.75f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = value,
                style = AppNumbers.summary,
                color = ink,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f, fill = false),
            )
            if (trailing != null) {
                Spacer(Modifier.width(10.dp))
                Text(
                    text = trailing,
                    style = MaterialTheme.typography.labelLarge,
                    color = ink.copy(alpha = 0.75f),
                    maxLines = 1,
                    modifier = Modifier.padding(bottom = 8.dp),
                )
            }
        }
        if (caption != null) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = caption,
                style = MaterialTheme.typography.bodySmall,
                color = ink.copy(alpha = 0.75f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        content()
    }
}

@Preview(showBackground = true, widthDp = 380)
@Composable
private fun HeroSummaryCardPreview() {
    CarTrackingAppTheme(darkTheme = true) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(18.dp),
        ) {
            HeroSummaryCard(
                label = "Total spent",
                value = "988,46 €",
                caption = "Οκτώβριος 2026 · 9 εγγραφές",
            )
            HeroSummaryCard(
                label = "Next month",
                value = "412,00",
                trailing = "€",
                caption = "Based on 6 months",
            )
        }
    }
}
