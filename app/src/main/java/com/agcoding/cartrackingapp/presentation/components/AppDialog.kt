package com.agcoding.cartrackingapp.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppColorScheme
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppDimens

/**
 * The app's dialog, and in particular its confirmation dialog.
 *
 * Material's `AlertDialog` is the one surface the redesign never reached, so
 * "Import Data" and "Clear all data" still appeared as stock boxes with
 * `surfaceContainerHigh` behind them and two text buttons in the corner —
 * nothing like the sheets and cards they are launched from.
 *
 * The shape here is the same one the sheets use: the app's card colour, the
 * card radius, an icon tile, title, body, and full-width buttons stacked with
 * the confirming action on top. Stacked rather than side by side because the
 * two choices are rarely of equal weight — one of them replaces or destroys
 * everything the user has recorded — and a destructive action should not be
 * one of a matched pair of small targets.
 *
 * @param destructive paints the icon tile and the confirm button in the error
 *   colour. It is the difference between "import this file" and "delete
 *   everything", which is not a difference to leave to the wording.
 */
@Composable
fun AppConfirmDialog(
    title: String,
    message: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    dismissLabel: String? = null,
    icon: ImageVector? = null,
    destructive: Boolean = false,
    extraContent: (@Composable ColumnScope.() -> Unit)? = null
) {
    val scheme = LocalAppColorScheme.current
    val dimens = LocalAppDimens.current
    val accent = if (destructive) scheme.statusError else scheme.actionPrimary

    Dialog(onDismissRequest = onDismiss) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(dimens.radius.card))
                .background(scheme.backgroundCard)
                .padding(horizontal = 22.dp, vertical = 24.dp)
        ) {
            if (icon != null) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(dimens.radius.tile))
                        .background(accent.copy(alpha = 0.14f))
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accent,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(Modifier.height(16.dp))
            }

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = scheme.contentPrimary,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = scheme.contentSecondary,
                textAlign = TextAlign.Center
            )

            if (extraContent != null) {
                Spacer(Modifier.height(16.dp))
                extraContent()
            }

            Spacer(Modifier.height(22.dp))

            PrimaryButton(
                label = confirmLabel,
                onClick = onConfirm,
                destructive = destructive,
                modifier = Modifier.fillMaxWidth()
            )
            if (dismissLabel != null) {
                Spacer(Modifier.height(8.dp))
                TonalButton(
                    label = dismissLabel,
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Preview(name = "Confirm — import", showBackground = true, widthDp = 380)
@Composable
private fun PreviewImportDialog() {
    CarTrackingAppTheme(darkTheme = true) {
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(18.dp)
        ) {
            AppConfirmDialog(
                title = "Import Data",
                message = "Importing data will replace all existing data. " +
                    "This action cannot be undone.",
                confirmLabel = "Import",
                dismissLabel = "Cancel",
                icon = Icons.Default.Upload,
                onConfirm = {},
                onDismiss = {}
            )
        }
    }
}

@Preview(name = "Confirm — destructive", showBackground = true, widthDp = 380)
@Composable
private fun PreviewDestructiveDialog() {
    CarTrackingAppTheme(darkTheme = false) {
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(18.dp)
        ) {
            AppConfirmDialog(
                title = "Clear all data",
                message = "Every car, refill and expense will be deleted.",
                confirmLabel = "Clear",
                dismissLabel = "Cancel",
                icon = Icons.Default.DeleteForever,
                destructive = true,
                onConfirm = {},
                onDismiss = {}
            )
        }
    }
}
