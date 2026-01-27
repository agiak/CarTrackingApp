package com.agcoding.cartrackingapp.presentation.editrefill.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme

@Composable
fun RefillLocationCard(
    hasLocation: Boolean,
    onRefreshLocation: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = stringResource(R.string.location),
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (hasLocation)
                        stringResource(R.string.location_captured)
                    else
                        stringResource(R.string.no_location),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            TextButton(onClick = onRefreshLocation) {
                Text(stringResource(R.string.update))
            }
        }
    }
}

// ============================================
// Preview Composables
// ============================================

@Preview(name = "Location Card - With Location", showBackground = true, widthDp = 380)
@Composable
private fun PreviewLocationCardWithLocation() {
    CarTrackingAppTheme(darkTheme = false) {
        RefillLocationCard(
            hasLocation = true,
            onRefreshLocation = {},
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}

@Preview(name = "Location Card - No Location", showBackground = true, widthDp = 380)
@Composable
private fun PreviewLocationCardNoLocation() {
    CarTrackingAppTheme(darkTheme = false) {
        RefillLocationCard(
            hasLocation = false,
            onRefreshLocation = {},
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}

@Preview(name = "Location Card - Dark", showBackground = true, widthDp = 380)
@Composable
private fun PreviewLocationCardDark() {
    CarTrackingAppTheme(darkTheme = true) {
        RefillLocationCard(
            hasLocation = true,
            onRefreshLocation = {},
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}
