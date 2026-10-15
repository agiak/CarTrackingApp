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
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import com.agcoding.cartrackingapp.presentation.components.AppTextField
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppColorScheme
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppDimens
import com.agcoding.cartrackingapp.shared.ui.tokens.categoryOther
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
    val scheme = LocalAppColorScheme.current
    val dimens = LocalAppDimens.current

    // The app's card surface, not secondaryContainer. On the accent tint the
    // field inside sat on a coloured block and stopped reading as a field,
    // which is the only thing in here anyone needs to touch.
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(dimens.radius.card))
            .background(scheme.backgroundCard)
            .padding(14.dp)
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
                    tint = scheme.categoryOther,
                    modifier = Modifier.size(19.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.location),
                    style = MaterialTheme.typography.titleSmall,
                    color = scheme.contentPrimary
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(dimens.radius.button))
                    .clickable(enabled = !isLoading, onClick = onRefreshLocation)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(15.dp),
                        strokeWidth = 2.dp,
                        color = scheme.actionPrimary
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = null,
                        tint = scheme.actionPrimary,
                        modifier = Modifier.size(17.dp)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.update),
                    style = MaterialTheme.typography.labelLarge,
                    color = scheme.actionPrimary
                )
            }
        }

        Spacer(modifier = Modifier.size(8.dp))

        AppTextField(
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
