@file:OptIn(ExperimentalMaterial3Api::class)

package com.agcoding.cartrackingapp.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme
import com.agcoding.cartrackingapp.shared.ui.brand.DeletedUndo
import com.agcoding.cartrackingapp.shared.ui.brand.UNDO_WINDOW_MILLIS
import com.agcoding.cartrackingapp.shared.ui.brand.rememberBrandColors
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppColorScheme
import kotlinx.coroutines.launch

/**
 * The app-wide "deleted" confirmation, with the offer to take it back.
 *
 * The delete has already happened by the time this is on screen. That is the
 * whole design: the row is gone from the database, the list behind the scrim
 * has already lost it, and [onUndo] puts it back. The alternative — holding
 * the delete until a timer runs out — is what this screen used to do, and a
 * pending write that lives in a composition dies with it.
 *
 * Three ways out, and they all resolve exactly once:
 *  - the ring empties, so the offer lapses and [onFinished] runs;
 *  - undo is tapped, so [onUndo] runs instead;
 *  - the sheet is swiped away, which is the user saying they are done
 *    looking at it — the same as letting it lapse.
 *
 * [onFinished] is where a details screen navigates back. A list screen has
 * nowhere to go and can leave it empty.
 */
@Composable
fun DeletedSheet(
    visible: Boolean,
    message: String,
    onUndo: () -> Unit,
    onFinished: () -> Unit,
    modifier: Modifier = Modifier,
    supportingText: String? = null,
    undoMillis: Int = UNDO_WINDOW_MILLIS,
) {
    if (!visible) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val scheme = LocalAppColorScheme.current
    val brand = rememberBrandColors(surface = scheme.backgroundCard)

    // The countdown and a tap on undo can land in the same frame. Whichever
    // arrives first wins and the other is dropped, so the caller is never
    // told both that the delete stood and that it was taken back.
    var resolved by remember { mutableStateOf(false) }
    val resolve: (() -> Unit) -> Unit = { action ->
        if (!resolved) {
            resolved = true
            scope.launch { sheetState.hide() }.invokeOnCompletion {
                if (!sheetState.isVisible) action()
            }
        }
    }

    AppBottomSheet(
        onDismissRequest = { resolve(onFinished) },
        modifier = modifier,
        sheetState = sheetState,
        // No handle: the sheet already says how long it is staying, and a
        // grab bar next to a countdown invites a drag that only cuts it short.
        showDragHandle = false,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(top = 24.dp, bottom = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            DeletedUndo(
                modifier = Modifier.size(104.dp),
                undoMillis = undoMillis,
                colors = brand,
                onTimeout = { resolve(onFinished) },
            )

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = message,
                style = MaterialTheme.typography.titleLarge,
                color = scheme.contentPrimary,
                textAlign = TextAlign.Center,
            )

            if (supportingText != null) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = supportingText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = scheme.contentSecondary,
                    textAlign = TextAlign.Center,
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // No icon. Undo is auto-mirrored in Material's set and this
            // project has never pulled that one in; a label alone is not
            // worth an import whose existence cannot be checked here.
            TonalButton(
                label = stringResource(R.string.undo),
                onClick = { resolve(onUndo) },
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 380)
@Composable
private fun DeletedSheetPreview() {
    CarTrackingAppTheme(darkTheme = true) {
        Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            DeletedUndo(modifier = Modifier.size(104.dp))
        }
    }
}
