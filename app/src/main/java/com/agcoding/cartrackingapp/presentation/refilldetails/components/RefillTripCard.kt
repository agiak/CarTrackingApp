package com.agcoding.cartrackingapp.presentation.refilldetails.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.presentation.components.StyledCard
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme

/**
 * Which trip this refill belongs to, and the way to change that.
 *
 * The same card covers both states: with no trip it invites the user to pick one, and
 * with a trip it names it and offers to change it — the picker handles both, so there is
 * only one place to go.
 */
@Composable
fun RefillTripCard(
    tripName: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    StyledCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 8.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = Icons.Default.Flag,
                contentDescription = null,
                tint = if (tripName != null) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.refill_trip_section),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = tripName ?: stringResource(R.string.refill_not_in_trip),
                    fontSize = 15.sp,
                    fontWeight = if (tripName != null) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (tripName != null) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            TextButton(onClick = onClick) {
                Text(
                    text = if (tripName != null) {
                        stringResource(R.string.refill_change_trip)
                    } else {
                        stringResource(R.string.refill_add_to_trip)
                    },
                    maxLines = 1
                )
            }
        }
    }
}

// ============================================
// Preview Composables
// ============================================

@Preview(name = "Refill trip - none", showBackground = true, widthDp = 400)
@Composable
private fun PreviewRefillTripCardEmpty() {
    CarTrackingAppTheme(darkTheme = false) {
        RefillTripCard(tripName = null, onClick = {})
    }
}

@Preview(name = "Refill trip - assigned", showBackground = true, widthDp = 400)
@Composable
private fun PreviewRefillTripCardAssigned() {
    CarTrackingAppTheme(darkTheme = false) {
        RefillTripCard(tripName = "Summer Vacation 2026", onClick = {})
    }
}

@Preview(name = "Refill trip - dark", showBackground = true, widthDp = 400)
@Composable
private fun PreviewRefillTripCardDark() {
    CarTrackingAppTheme(darkTheme = true) {
        RefillTripCard(tripName = "Weekend in the mountains", onClick = {})
    }
}
