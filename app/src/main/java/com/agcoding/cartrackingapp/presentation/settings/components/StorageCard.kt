package com.agcoding.cartrackingapp.presentation.settings.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.presentation.components.SectionLabel
import com.agcoding.cartrackingapp.presentation.components.StyledCard
import com.agcoding.cartrackingapp.presentation.settings.StorageInfo
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppColorScheme
import com.agcoding.cartrackingapp.shared.ui.theme.LocalAppDimens
import com.agcoding.cartrackingapp.shared.ui.tokens.AppNumbers

/**
 * How much room the app is taking, and the four things that can be done about
 * it.
 *
 * The size was a 13sp line under a heading; it is now the figure the card is
 * built around, with a bar showing how much of it is data worth keeping versus
 * cache that will come back by itself. The design asks for three segments, but
 * the app only measures two — data and cache — and inventing a third would be
 * drawing a number nobody computed.
 *
 * The three ways data moves in and out become a row of tiles. They were text
 * buttons in two rows, which read as a toolbar; as tiles each one has room for
 * its icon and a label that does not truncate in Greek.
 *
 * Clearing everything is kept apart from them, outlined rather than filled, in
 * the error colour. It is the one irreversible thing on the screen and it
 * should never be the easiest button to hit by accident.
 */
@Composable
fun StorageCard(
    storageInfo: StorageInfo,
    isExporting: Boolean = false,
    isExportingExcel: Boolean = false,
    isImporting: Boolean = false,
    isSpreadsheetImporting: Boolean = false,
    isSignedIn: Boolean = false,
    onExport: () -> Unit = {},
    onExportExcel: () -> Unit = {},
    onImport: () -> Unit = {},
    onClear: () -> Unit = {}
) {
    val scheme = LocalAppColorScheme.current
    val dimens = LocalAppDimens.current
    val anyBusy = isExporting || isExportingExcel || isImporting || isSpreadsheetImporting

    Column(modifier = Modifier.fillMaxWidth()) {
        StyledCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = stringResource(R.string.data_storage_app_storage),
                    style = MaterialTheme.typography.labelLarge,
                    color = scheme.contentSecondary,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = storageInfo.formattedTotalSize,
                    style = AppNumbers.donut,
                    color = scheme.contentPrimary,
                    maxLines = 1,
                )

                Spacer(Modifier.height(16.dp))

                StorageBar(
                    data = storageInfo.dataSize,
                    cache = storageInfo.cacheSize,
                    dataColor = scheme.actionPrimary,
                    cacheColor = scheme.contentDisabled,
                    track = scheme.backgroundSecondary,
                )

                Spacer(Modifier.height(12.dp))

                StorageLegend(
                    label = stringResource(R.string.data_storage_app_data),
                    value = storageInfo.formattedDataSize,
                    color = scheme.actionPrimary,
                )
                Spacer(Modifier.height(8.dp))
                StorageLegend(
                    label = stringResource(R.string.data_storage_cache),
                    value = storageInfo.formattedCacheSize,
                    color = scheme.contentDisabled,
                )
            }
        }

        Spacer(Modifier.height(dimens.spacing.sectionTop))
        SectionLabel(stringResource(R.string.settings_backup_transfer_title))
        Spacer(Modifier.height(6.dp))
        Text(
            text = stringResource(R.string.settings_backup_transfer_desc),
            style = MaterialTheme.typography.bodySmall,
            color = scheme.contentSecondary,
        )
        Spacer(Modifier.height(12.dp))

        // Three, not four. Generating a sample spreadsheet was the fourth
        // tile, and it is not a backup action — it is step one of importing
        // one, which is what the card below this one is about. Sitting here it
        // read as a fourth way to move data out of the app.
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ActionTile(
                icon = Icons.Default.Upload,
                label = stringResource(R.string.export_json),
                busyLabel = stringResource(R.string.settings_exporting),
                isBusy = isExporting,
                enabled = !anyBusy,
                onClick = onExport,
            )
            ActionTile(
                icon = Icons.Default.GridOn,
                label = stringResource(R.string.export_excel),
                busyLabel = stringResource(R.string.settings_exporting),
                isBusy = isExportingExcel,
                enabled = !anyBusy,
                onClick = onExportExcel,
            )
            ActionTile(
                icon = Icons.Default.Download,
                label = stringResource(R.string.import_action),
                busyLabel = stringResource(R.string.settings_importing),
                isBusy = isImporting || isSpreadsheetImporting,
                enabled = !anyBusy,
                onClick = onImport,
            )
        }

        Spacer(Modifier.height(10.dp))
        Text(
            text = stringResource(R.string.settings_import_replaces_data_warning),
            style = MaterialTheme.typography.labelSmall,
            color = scheme.statusError,
        )

        Spacer(Modifier.height(dimens.spacing.sectionTop))

        // Signed in, there is no such thing as clearing this phone.
        //
        // What the phone holds is a copy of the account, so a local clear is
        // either undone by the next sync or — as it used to be, before the
        // sync links were cleared with the data — pushed to the server and
        // taken out on every other device. Neither is "clear the data on this
        // phone", so the button is not offered at all: leaving the account is
        // what empties the phone, and deleting the account is what removes the
        // records.
        if (isSignedIn) {
            Row(verticalAlignment = Alignment.Top) {
                Icon(
                    imageVector = Icons.Default.CloudDone,
                    contentDescription = null,
                    tint = scheme.contentDisabled,
                    modifier = Modifier
                        .padding(top = 1.dp)
                        .size(17.dp),
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = stringResource(R.string.settings_clear_signed_in),
                    style = MaterialTheme.typography.bodySmall,
                    color = scheme.contentDisabled,
                )
            }
            return@Column
        }

        // Outlined, not filled: the only irreversible action on the screen
        // should not be the easiest thing on it to hit.
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(RoundedCornerShape(dimens.radius.button))
                .border(
                    BorderStroke(dimens.size.divider, scheme.statusError.copy(alpha = 0.6f)),
                    RoundedCornerShape(dimens.radius.button),
                )
                .clickable(enabled = !anyBusy, onClick = onClear)
                .alpha(if (anyBusy) 0.5f else 1f),
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = null,
                tint = scheme.statusError,
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.clear_all_data_title),
                style = MaterialTheme.typography.titleSmall,
                color = scheme.statusError,
                maxLines = 1,
            )
        }
    }
}

@Composable
private fun StorageBar(
    data: Long,
    cache: Long,
    dataColor: Color,
    cacheColor: Color,
    track: Color,
) {
    val height = 10.dp
    val shape = RoundedCornerShape(height / 2)
    val total = data + cache

    if (total <= 0L) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .clip(shape)
                .background(track),
        )
        return
    }

    Row(
        horizontalArrangement = Arrangement.spacedBy(3.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(height),
    ) {
        listOf(data to dataColor, cache to cacheColor)
            .filter { it.first > 0L }
            .forEach { (value, color) ->
                Box(
                    modifier = Modifier
                        .weight(value.toFloat())
                        .fillMaxHeight()
                        .clip(shape)
                        .background(color),
                )
            }
    }
}

@Composable
private fun StorageLegend(label: String, value: String, color: Color) {
    val scheme = LocalAppColorScheme.current
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(width = 18.dp, height = 4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color),
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = scheme.contentSecondary,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = value,
            style = AppNumbers.compact,
            color = scheme.contentPrimary,
            maxLines = 1,
        )
    }
}

@Composable
private fun RowScope.ActionTile(
    icon: ImageVector,
    label: String,
    busyLabel: String,
    isBusy: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val scheme = LocalAppColorScheme.current
    val dimens = LocalAppDimens.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .weight(1f)
            .height(78.dp)
            .clip(RoundedCornerShape(dimens.radius.tile))
            .border(1.dp, scheme.borderDefault, RoundedCornerShape(dimens.radius.tile))
            .background(scheme.backgroundCard)
            .clickable(enabled = enabled, onClick = onClick)
            .alpha(if (enabled || isBusy) 1f else 0.4f)
            .padding(horizontal = 6.dp),
    ) {
        if (isBusy) {
            CircularProgressIndicator(
                strokeWidth = 2.dp,
                color = scheme.actionPrimary,
                modifier = Modifier.size(19.dp),
            )
        } else {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = scheme.actionPrimary,
                modifier = Modifier.size(19.dp),
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = if (isBusy) busyLabel else label,
            style = MaterialTheme.typography.labelMedium,
            color = scheme.contentPrimary,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Preview(showBackground = true, widthDp = 380)
@Composable
private fun StorageCardPreview() {
    CarTrackingAppTheme(darkTheme = true) {
        Column(Modifier.padding(18.dp)) {
            StorageCard(
                storageInfo = StorageInfo(
                    totalSize = 13_041_664L,
                    dataSize = 11_534_336L,
                    cacheSize = 1_507_328L,
                )
            )
        }
    }
}

@Preview(name = "Storage — importing", showBackground = true, widthDp = 380)
@Composable
private fun StorageCardBusyPreview() {
    CarTrackingAppTheme(darkTheme = false) {
        Column(Modifier.padding(18.dp)) {
            StorageCard(
                storageInfo = StorageInfo(totalSize = 0L, dataSize = 0L, cacheSize = 0L),
                isImporting = true,
            )
        }
    }
}
