@file:OptIn(ExperimentalMaterial3Api::class)

package com.agcoding.cartrackingapp.presentation.refilldetails.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Route
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.domain.model.Trip
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme

/**
 * Picks the trip a refill belongs to.
 *
 * Only the refill's own car has trips worth offering, so the list is already scoped by
 * the caller. A refill belongs to at most one trip, so picking a different one moves it
 * rather than adding a second membership — and the trip it is currently in is marked.
 *
 * Creating a trip happens in place: the form expands inside the sheet and the new trip
 * gets the refill straight away, which is the whole reason the user opened this.
 */
@Composable
fun TripPickerSheet(
    trips: List<Trip>,
    currentTripId: Long?,
    onTripSelected: (Trip) -> Unit,
    onCreateTrip: (name: String, description: String?) -> Unit,
    onRemoveFromTrip: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var creating by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = stringResource(R.string.trip_picker_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (creating) {
                NewTripForm(
                    name = name,
                    description = description,
                    onNameChange = { name = it },
                    onDescriptionChange = { description = it },
                    onCancel = { creating = false },
                    onCreate = { onCreateTrip(name, description) }
                )
            } else {
                TripRow(
                    icon = { tint ->
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = tint,
                            modifier = Modifier.size(20.dp)
                        )
                    },
                    title = stringResource(R.string.trip_picker_new),
                    subtitle = null,
                    selected = false,
                    accent = true,
                    onClick = { creating = true }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(8.dp))

            if (trips.isEmpty()) {
                Text(
                    text = stringResource(R.string.trip_picker_empty),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
            } else {
                // A plain scrolling column: a car has a handful of trips, and a lazy list
                // cannot be nested inside the sheet's own scrolling content.
                Column(
                    modifier = Modifier
                        .heightIn(max = 320.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    trips.forEach { trip ->
                        TripRow(
                            icon = { tint ->
                                Icon(
                                    imageVector = Icons.Default.Route,
                                    contentDescription = null,
                                    tint = tint,
                                    modifier = Modifier.size(20.dp)
                                )
                            },
                            title = trip.name,
                            subtitle = pluralStringResource(
                                R.plurals.refills_count,
                                trip.refills.size,
                                trip.refills.size
                            ),
                            selected = trip.id == currentTripId,
                            accent = false,
                            onClick = { onTripSelected(trip) }
                        )
                    }
                }
            }

            if (currentTripId != null) {
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = onRemoveFromTrip) {
                    Text(
                        text = stringResource(R.string.trip_picker_remove),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun NewTripForm(
    name: String,
    description: String,
    onNameChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onCancel: () -> Unit,
    onCreate: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text(stringResource(R.string.trip_name_required_label)) },
            placeholder = { Text(stringResource(R.string.trip_name_placeholder)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = description,
            onValueChange = onDescriptionChange,
            label = { Text(stringResource(R.string.trip_description_optional_label)) },
            placeholder = { Text(stringResource(R.string.trip_notes_hint)) },
            modifier = Modifier.fillMaxWidth()
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onCancel) {
                Text(stringResource(R.string.cancel))
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = onCreate,
                enabled = name.isNotBlank()
            ) {
                Text(stringResource(R.string.create_trip_title))
            }
        }
    }
}

@Composable
private fun TripRow(
    icon: @Composable (tint: Color) -> Unit,
    title: String,
    subtitle: String?,
    selected: Boolean,
    accent: Boolean,
    onClick: () -> Unit
) {
    val contentColor = when {
        accent -> MaterialTheme.colorScheme.primary
        selected -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurface
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        icon(contentColor)
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = if (selected || accent) FontWeight.SemiBold else FontWeight.Normal,
                color = contentColor
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        if (selected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// ============================================
// Preview Composables
// ============================================

@Preview(name = "New trip form", showBackground = true, widthDp = 400)
@Composable
private fun PreviewNewTripForm() {
    CarTrackingAppTheme(darkTheme = false) {
        Column(modifier = Modifier.padding(16.dp)) {
            NewTripForm(
                name = "Summer 2026",
                description = "",
                onNameChange = {},
                onDescriptionChange = {},
                onCancel = {},
                onCreate = {}
            )
        }
    }
}

@Preview(name = "Trip rows", showBackground = true, widthDp = 400)
@Composable
private fun PreviewTripRows() {
    CarTrackingAppTheme(darkTheme = false) {
        Column(modifier = Modifier.padding(16.dp)) {
            TripRow(
                icon = { tint ->
                    Icon(Icons.Default.Add, null, tint = tint, modifier = Modifier.size(20.dp))
                },
                title = "New trip",
                subtitle = null,
                selected = false,
                accent = true,
                onClick = {}
            )
            TripRow(
                icon = { tint ->
                    Icon(Icons.Default.Route, null, tint = tint, modifier = Modifier.size(20.dp))
                },
                title = "Summer Vacation 2026",
                subtitle = "6 refills",
                selected = true,
                accent = false,
                onClick = {}
            )
            TripRow(
                icon = { tint ->
                    Icon(Icons.Default.Route, null, tint = tint, modifier = Modifier.size(20.dp))
                },
                title = "Weekend in the mountains",
                subtitle = "2 refills",
                selected = false,
                accent = false,
                onClick = {}
            )
        }
    }
}
