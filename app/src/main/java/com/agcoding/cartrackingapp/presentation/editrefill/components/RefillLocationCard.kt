package com.agcoding.cartrackingapp.presentation.editrefill.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.presentation.components.StyledCard
import com.agcoding.cartrackingapp.presentation.components.StyledOutlinedTextField
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme

/**
 * Editable location card for a refill. Shows a text field with the (reverse-geocoded)
 * location name the user can edit freely, plus an action to re-detect the GPS position
 * which refreshes the suggested name.
 */
@Composable
fun RefillLocationCard(
    locationName: String,
    onLocationNameChange: (String) -> Unit,
    hasLocation: Boolean,
    onRefreshLocation: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false
) {
    StyledCard(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = stringResource(R.string.location),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.location),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
                TextButton(onClick = onRefreshLocation, enabled = !isLoading) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(stringResource(R.string.update))
                }
            }

            Spacer(modifier = Modifier.size(8.dp))

            StyledOutlinedTextField(
                value = locationName,
                onValueChange = onLocationNameChange,
                placeholder = { Text(stringResource(R.string.refill_location_name_hint)) },
                singleLine = true,
                enabled = !isLoading,
                supportingText = when {
                    isLoading -> {
                        { Text(stringResource(R.string.location_detecting)) }
                    }
                    !hasLocation && locationName.isBlank() -> {
                        { Text(stringResource(R.string.no_location)) }
                    }
                    else -> null
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// ============================================
// Preview Composables
// ============================================

@Preview(name = "Location Card - With Name", showBackground = true, widthDp = 380)
@Composable
private fun PreviewLocationCardWithName() {
    CarTrackingAppTheme(darkTheme = false) {
        var name by remember { mutableStateOf("Shell, Kifisias Ave.") }
        RefillLocationCard(
            locationName = name,
            onLocationNameChange = { name = it },
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
            locationName = "",
            onLocationNameChange = {},
            hasLocation = false,
            onRefreshLocation = {},
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}

@Preview(name = "Location Card - Loading", showBackground = true, widthDp = 380)
@Composable
private fun PreviewLocationCardLoading() {
    CarTrackingAppTheme(darkTheme = false) {
        RefillLocationCard(
            locationName = "",
            onLocationNameChange = {},
            hasLocation = true,
            onRefreshLocation = {},
            isLoading = true,
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
        var name by remember { mutableStateOf("BP, Syntagma Sq.") }
        RefillLocationCard(
            locationName = name,
            onLocationNameChange = { name = it },
            hasLocation = true,
            onRefreshLocation = {},
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )
    }
}
