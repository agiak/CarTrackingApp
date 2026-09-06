@file:OptIn(ExperimentalMaterial3Api::class)

package com.agcoding.cartrackingapp.presentation.refilldetails.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.presentation.components.SuccessAnimation
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme

/** How tall the success sheet stands — enough for the checkmark and its two lines. */
private val SheetHeight = 280.dp

/**
 * Confirms that the trip was created and that this refill is now in it.
 *
 * The app's usual success confirmation is a full-screen overlay, but the whole flow that
 * leads here happens in bottom sheets, so this keeps the same shape: the sheet plays the
 * shared [SuccessAnimation] and closes itself when the animation settles.
 */
@Composable
fun TripCreatedSheet(
    tripName: String,
    onFinished: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onFinished,
        sheetState = sheetState
    ) {
        SuccessAnimation(
            message = stringResource(R.string.trip_created),
            supportingText = stringResource(R.string.refill_added_to_trip, tripName),
            onFinished = onFinished,
            modifier = Modifier
                .fillMaxWidth()
                .height(SheetHeight)
        )
    }
}

// ============================================
// Preview Composables
// ============================================

@Preview(name = "Trip created", showBackground = true, widthDp = 400, heightDp = 320)
@Composable
private fun PreviewTripCreated() {
    CarTrackingAppTheme(darkTheme = false) {
        Box(modifier = Modifier.fillMaxWidth()) {
            SuccessAnimation(
                message = stringResource(R.string.trip_created),
                supportingText = stringResource(R.string.refill_added_to_trip, "Summer 2026"),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(SheetHeight)
            )
        }
    }
}
