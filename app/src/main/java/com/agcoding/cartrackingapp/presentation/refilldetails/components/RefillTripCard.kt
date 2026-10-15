package com.agcoding.cartrackingapp.presentation.refilldetails.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppColorScheme
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppDimens

/**
 * Which trip this refill belongs to, and the way to change that.
 *
 * The same row covers both states: with no trip it invites the user to pick
 * one, and with a trip it names it — the picker handles both, so there is only
 * one place to go.
 *
 * The "Change" / "Add to trip" text button at the end is gone and the whole
 * row is the target instead. The button was the only tappable thing in it,
 * which made the row look like a label with a control stuck on the side; it is
 * one control, so it is drawn as one.
 */
@Composable
fun RefillTripCard(
    tripName: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scheme = LocalAppColorScheme.current
    val dimens = LocalAppDimens.current
    val assigned = tripName != null

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(dimens.radius.tile))
            .background(scheme.backgroundCard)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 13.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(dimens.size.iconTile)
                .clip(RoundedCornerShape(dimens.radius.iconTile))
                .background(
                    if (assigned) {
                        scheme.actionPrimary.copy(alpha = 0.16f)
                    } else {
                        scheme.backgroundSecondary
                    }
                )
        ) {
            Icon(
                imageVector = Icons.Default.Map,
                contentDescription = null,
                tint = if (assigned) scheme.actionPrimary else scheme.contentSecondary,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.refill_trip_section),
                style = MaterialTheme.typography.labelMedium,
                color = scheme.contentSecondary
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = tripName ?: stringResource(R.string.refill_not_in_trip),
                style = MaterialTheme.typography.bodyLarge,
                color = if (assigned) scheme.contentPrimary else scheme.contentSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(Modifier.width(8.dp))
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = scheme.contentDisabled,
            modifier = Modifier.size(20.dp)
        )
    }
}

// ============================================
// Preview Composables
// ============================================

@Preview(name = "Refill trip — none", showBackground = true, widthDp = 390)
@Composable
private fun PreviewRefillTripCardEmpty() {
    CarTrackingAppTheme(darkTheme = true) {
        RefillTripCard(tripName = null, onClick = {}, modifier = Modifier.padding(18.dp))
    }
}

@Preview(name = "Refill trip — assigned", showBackground = true, widthDp = 390)
@Composable
private fun PreviewRefillTripCardAssigned() {
    CarTrackingAppTheme(darkTheme = true) {
        RefillTripCard(
            tripName = "Weekend in Nafplio",
            onClick = {},
            modifier = Modifier.padding(18.dp)
        )
    }
}

@Preview(name = "Refill trip — light", showBackground = true, widthDp = 390)
@Composable
private fun PreviewRefillTripCardLight() {
    CarTrackingAppTheme(darkTheme = false) {
        RefillTripCard(
            tripName = "Athens → Thessaloniki",
            onClick = {},
            modifier = Modifier.padding(18.dp)
        )
    }
}
