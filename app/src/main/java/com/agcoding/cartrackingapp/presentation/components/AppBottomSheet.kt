@file:OptIn(ExperimentalMaterial3Api::class)

package com.agcoding.cartrackingapp.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppColorScheme
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppDimens

/**
 * The app's bottom sheet. Every sheet in the app is one of these.
 *
 * Material's default container is `surfaceContainerLow`, which in the dark
 * theme is very nearly black — so a sheet opened over a dark screen appeared
 * as a black slab while the add-car sheet, the one place that passed its own
 * `containerColor`, looked right. That is the bug this component exists to
 * make impossible: the container, the corner radius, the scrim and the drag
 * handle are decided once, here.
 *
 * The container is [AppColorScheme.backgroundCard] — a sheet is a card that
 * arrived from the bottom of the screen, and it should sit on the scrim the
 * way a card sits on the screen. Fields inside it then fill with
 * backgroundSecondary and stay visible, which is the other half of the same
 * fix.
 *
 * The scrim is darker than Material's default. A sheet is modal; at 32% the
 * screen behind stays legible enough to keep competing for attention.
 */
@Composable
fun AppBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    showDragHandle: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    val scheme = LocalAppColorScheme.current
    val dimens = LocalAppDimens.current

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = dimens.radius.sheet, topEnd = dimens.radius.sheet),
        containerColor = scheme.backgroundCard,
        contentColor = scheme.contentPrimary,
        scrimColor = Color.Black.copy(alpha = 0.6f),
        dragHandle = if (showDragHandle) {
            { AppSheetDragHandle() }
        } else {
            null
        },
        content = content
    )
}

/**
 * The grab bar.
 *
 * Material's own is a 4dp pill in `onSurfaceVariant` at 40% — on these
 * surfaces it is almost invisible, and the handle is the only affordance
 * telling a user the sheet can be dragged away.
 */
@Composable
fun AppSheetDragHandle() {
    val scheme = LocalAppColorScheme.current
    Box(modifier = Modifier.padding(top = 12.dp, bottom = 6.dp)) {
        Box(
            modifier = Modifier
                .size(width = 38.dp, height = 4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(scheme.borderDefault)
        )
    }
}

/**
 * The top of a sheet: what it is, optionally why, optionally a way out.
 *
 * The close button is only drawn when [onClose] is given. A sheet can always
 * be dismissed by dragging or by tapping the scrim, so an X is a convenience
 * for the long forms where the user may have scrolled the drag handle out of
 * reach — not something every sheet needs.
 */
@Composable
fun SheetHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    onClose: (() -> Unit)? = null
) {
    val scheme = LocalAppColorScheme.current

    Row(
        verticalAlignment = Alignment.Top,
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = scheme.contentPrimary
            )
            if (subtitle != null) {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.contentSecondary
                )
            }
        }
        if (onClose != null) {
            Spacer(Modifier.width(8.dp))
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(scheme.backgroundSecondary)
                    .clickable(onClick = onClose)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(R.string.close),
                    tint = scheme.contentSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
