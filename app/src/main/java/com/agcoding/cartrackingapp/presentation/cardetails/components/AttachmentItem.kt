package com.agcoding.cartrackingapp.presentation.cardetails.components
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.domain.model.AttachmentType
import com.agcoding.cartrackingapp.domain.model.CarAttachment
import com.agcoding.cartrackingapp.presentation.theme.CarTrackingAppTheme
import com.agcoding.cartrackingapp.util.FileSizeFormatter
import java.text.SimpleDateFormat
import java.util.*
@Composable
fun AttachmentItem(
    attachment: CarAttachment,
    onOpen: () -> Unit,
    onRename: () -> Unit,
    onDownload: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onOpen() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // File icon
            Icon(
                imageVector = when (attachment.fileType) {
                    AttachmentType.PDF -> Icons.Default.Description
                    AttachmentType.IMAGE -> Icons.Default.Image
                },
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(12.dp))
            // File info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = attachment.fileName,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = FileSizeFormatter.format(attachment.fileSizeBytes),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Text(
                        text = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                            .format(Date(attachment.dateAdded)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
            }
            // Menu
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, stringResource(R.string.more_options))
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.open)) },
                        onClick = {
                            showMenu = false
                            onOpen()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.rename)) },
                        onClick = {
                            showMenu = false
                            onRename()
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.DriveFileRenameOutline,
                                contentDescription = null
                            )
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.download)) },
                        onClick = {
                            showMenu = false
                            onDownload()
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Download,
                                contentDescription = null
                            )
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.delete)) },
                        onClick = {
                            showMenu = false
                            onDelete()
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    )
                }
            }
        }
    }
}

// ============================================
// Preview Composables
// ============================================

private fun previewAttachment(
    fileName: String,
    fileType: AttachmentType,
    fileSizeBytes: Long
) = CarAttachment(
    id = 1,
    carId = 1,
    fileName = fileName,
    fileType = fileType,
    fileSizeBytes = fileSizeBytes,
    dateAdded = 1_767_225_600_000, // 2026-01-01, so the preview never shifts with the clock
    internalPath = "/attachments/$fileName"
)

@Preview(name = "Attachment - PDF", showBackground = true, widthDp = 400)
@Composable
private fun PreviewAttachmentItemPdf() {
    CarTrackingAppTheme(darkTheme = false) {
        AttachmentItem(
            attachment = previewAttachment("insurance-2026.pdf", AttachmentType.PDF, 284_000),
            onOpen = {},
            onRename = {},
            onDownload = {},
            onDelete = {}
        )
    }
}

@Preview(name = "Attachment - image, long name", showBackground = true, widthDp = 400)
@Composable
private fun PreviewAttachmentItemImage() {
    CarTrackingAppTheme(darkTheme = false) {
        AttachmentItem(
            attachment = previewAttachment(
                fileName = "a-very-long-scanned-service-invoice-name.jpg",
                fileType = AttachmentType.IMAGE,
                fileSizeBytes = 2_400_000
            ),
            onOpen = {},
            onRename = {},
            onDownload = {},
            onDelete = {}
        )
    }
}

@Preview(name = "Attachment - dark", showBackground = true, widthDp = 400)
@Composable
private fun PreviewAttachmentItemDark() {
    CarTrackingAppTheme(darkTheme = true) {
        AttachmentItem(
            attachment = previewAttachment("kteo.pdf", AttachmentType.PDF, 96_000),
            onOpen = {},
            onRename = {},
            onDownload = {},
            onDelete = {}
        )
    }
}
