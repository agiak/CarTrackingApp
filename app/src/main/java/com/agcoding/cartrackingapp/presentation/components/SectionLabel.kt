package com.agcoding.cartrackingapp.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppColorScheme

/**
 * The quiet, tracked, upper-case label that names a group of cards — "QUICK",
 * "YEAR", "GENERAL".
 *
 * It is deliberately the dimmest text on the screen. Its job is to be findable
 * when scanning for structure and ignorable when reading content, which is why
 * it is small and wide-tracked rather than large and bold.
 *
 * The capitals are applied here rather than in the text style so that the
 * string handed to accessibility services stays as written; a screen reader
 * announcing "G-E-N-E-R-A-L" helps nobody.
 */
@Composable
fun SectionLabel(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = LocalAppColorScheme.current.contentDisabled,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier,
    )
}

/**
 * A section heading with an optional link on the right — "Monthly cost" with
 * "View all", "Cars by cost / km" with "Compare".
 *
 * The link is the only way into several of the sub-screens, so it is a real
 * target rather than decoration: it carries its own padding and sits on the
 * heading's baseline where the eye already is.
 */
@Composable
fun SectionHeaderRow(
    title: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onActionClick: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (actionLabel != null && onActionClick != null) {
            Text(
                text = actionLabel,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .clickable(onClick = onActionClick)
                    .padding(horizontal = 4.dp, vertical = 2.dp),
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 360)
@Composable
private fun SectionLabelPreview() {
    CarTrackingAppTheme(darkTheme = true) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(18.dp),
        ) {
            SectionLabel("General")
            SectionHeaderRow(title = "Monthly cost", actionLabel = "View all", onActionClick = {})
            SectionHeaderRow(title = "Explore")
        }
    }
}
