package com.agcoding.cartrackingapp.presentation.settings.components
import androidx.compose.ui.tooling.preview.Preview
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.GridOn
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.presentation.components.StyledCard
import com.agcoding.cartrackingapp.presentation.settings.StorageInfo

@Composable
fun StorageCard(
    storageInfo: StorageInfo,
    isExporting: Boolean = false,
    isExportingExcel: Boolean = false,
    isImporting: Boolean = false,
    isSpreadsheetImporting: Boolean = false,
    isGeneratingSample: Boolean = false,
    onExport: () -> Unit = {},
    onExportExcel: () -> Unit = {},
    onImport: () -> Unit = {},
    onGenerateSample: () -> Unit = {},
    onClear: () -> Unit = {}
) {
    StyledCard(
        modifier = Modifier.fillMaxWidth(),
        tintAlpha = 0.3f
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Storage info header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Default.Storage,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = stringResource(R.string.data_storage_app_storage),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.settings_storage_used_format, storageInfo.formattedTotalSize),
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.data_storage_app_data),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = storageInfo.formattedDataSize,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.data_storage_cache),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = storageInfo.formattedCacheSize,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(R.string.settings_backup_transfer_title),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = stringResource(R.string.settings_backup_transfer_desc),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            val anyBusy = isExporting || isExportingExcel || isImporting || isSpreadsheetImporting || isGeneratingSample

            // Export row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StorageActionButton(
                    icon = Icons.Default.Upload,
                    text = if (isExporting) stringResource(R.string.settings_exporting) else stringResource(R.string.export_json),
                    onClick = onExport,
                    enabled = !anyBusy,
                    isLoading = isExporting,
                    modifier = Modifier.weight(1f)
                )
                StorageActionButton(
                    icon = Icons.Default.GridOn,
                    text = if (isExportingExcel) stringResource(R.string.settings_exporting) else stringResource(R.string.export_excel),
                    onClick = onExportExcel,
                    enabled = !anyBusy,
                    isLoading = isExportingExcel,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Import / Sample row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StorageActionButton(
                    icon = Icons.Default.Download,
                    text = when {
                        isImporting || isSpreadsheetImporting -> stringResource(R.string.settings_importing)
                        else -> stringResource(R.string.import_action)
                    },
                    onClick = onImport,
                    enabled = !anyBusy,
                    isLoading = isImporting || isSpreadsheetImporting,
                    modifier = Modifier.weight(1f)
                )
                StorageActionButton(
                    icon = Icons.Default.GridOn,
                    text = if (isGeneratingSample) stringResource(R.string.spreadsheet_generating) else stringResource(R.string.spreadsheet_generate_sample),
                    onClick = onGenerateSample,
                    enabled = !anyBusy,
                    isLoading = isGeneratingSample,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(R.string.settings_import_replaces_data_warning),
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            StorageActionButton(
                icon = Icons.Default.Delete,
                text = stringResource(R.string.clear),
                onClick = onClear,
                enabled = !anyBusy,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun StorageCardPreview() {
    CarTrackingAppTheme {
        StorageCard(storageInfo = StorageInfo())
    }
}
