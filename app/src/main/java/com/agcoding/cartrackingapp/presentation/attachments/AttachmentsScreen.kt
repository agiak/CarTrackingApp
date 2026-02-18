package com.agcoding.cartrackingapp.presentation.attachments

import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.presentation.cardetails.CarDetailsViewModel
import com.agcoding.cartrackingapp.presentation.cardetails.components.AttachmentItem
import com.agcoding.cartrackingapp.presentation.components.StyledTopAppBar
import com.agcoding.cartrackingapp.util.FileDownloader
import com.agcoding.cartrackingapp.util.FileOpener
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttachmentsScreen(
    onNavigateBack: () -> Unit,
    viewModel: CarDetailsViewModel = hiltViewModel()
) {
    val attachments by viewModel.attachments.collectAsState()
    val attachmentToDelete by viewModel.attachmentToDelete.collectAsState()
    val attachmentToRename by viewModel.attachmentToRename.collectAsState()
    val attachmentError by viewModel.attachmentError.collectAsState()

    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // String resources for toast messages
    val noAppToOpenFileMsg = stringResource(R.string.no_app_to_open_file)
    val fileNotFoundMsg = stringResource(R.string.file_not_found)
    val downloadSuccessMsg = stringResource(R.string.download_success)
    val downloadFailedMsg = stringResource(R.string.download_failed)
    val storagePermissionRequiredMsg = stringResource(R.string.storage_permission_required)

    // State for pending download (used if permission is needed)
    var pendingDownloadAttachment by remember { mutableStateOf<com.agcoding.cartrackingapp.domain.model.CarAttachment?>(null) }

    // Storage permission launcher (only needed for Android 9 and below)
    val storagePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Permission granted, proceed with download
            pendingDownloadAttachment?.let { attachment ->
                scope.launch {
                    val file = viewModel.getAttachmentFile(attachment)
                    if (file != null && file.exists()) {
                        val success = FileDownloader.saveToDownloads(context, file, attachment)
                        if (success) {
                            Toast.makeText(context, downloadSuccessMsg, Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, downloadFailedMsg, Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(context, fileNotFoundMsg, Toast.LENGTH_SHORT).show()
                    }
                    pendingDownloadAttachment = null
                }
            }
        } else {
            // Permission denied
            Toast.makeText(context, storagePermissionRequiredMsg, Toast.LENGTH_LONG).show()
            pendingDownloadAttachment = null
        }
    }

    // File picker launcher for attachments
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            try {
                val contentResolver = context.contentResolver
                val cursor = contentResolver.query(uri, null, null, null, null)
                cursor?.use {
                    if (it.moveToFirst()) {
                        val displayNameIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                        val sizeIndex = it.getColumnIndex(android.provider.OpenableColumns.SIZE)

                        val fileName = if (displayNameIndex >= 0) it.getString(displayNameIndex) else "attachment"
                        val fileSize = if (sizeIndex >= 0) it.getLong(sizeIndex) else 0L
                        val mimeType = contentResolver.getType(uri)

                        viewModel.addAttachment(
                            uri = uri,
                            fileName = fileName,
                            mimeType = mimeType,
                            fileSizeBytes = fileSize
                        )
                    }
                }
            } catch (_: Exception) {
                Toast.makeText(context, "Failed to add attachment", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Show error toast if attachment operation fails
    LaunchedEffect(attachmentError) {
        attachmentError?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.clearAttachmentError()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            StyledTopAppBar(
                title = { Text(stringResource(R.string.attachments)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { filePickerLauncher.launch("*/*") },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.attach_file)
                )
            }
        }
    ) { paddingValues ->
        if (attachments.isEmpty()) {
            // Empty state
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AttachFile,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                    Text(
                        text = stringResource(R.string.no_attachments),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    Button(
                        onClick = { filePickerLauncher.launch("*/*") }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.attach_file))
                    }
                }
            }
        } else {
            // Attachments list
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = stringResource(R.string.attached_files_count, attachments.size),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Spacer(Modifier.height(4.dp))
                }

                items(attachments) { attachment ->
                    AttachmentItem(
                        attachment = attachment,
                        onOpen = {
                            scope.launch {
                                val file = viewModel.getAttachmentFile(attachment)
                                if (file != null && file.exists()) {
                                    val success = FileOpener.openAttachment(context, file, attachment)
                                    if (!success) {
                                        Toast.makeText(
                                            context,
                                            noAppToOpenFileMsg,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                } else {
                                    Toast.makeText(
                                        context,
                                        fileNotFoundMsg,
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        },
                        onRename = {
                            viewModel.showRenameAttachmentDialog(attachment)
                        },
                        onDownload = {
                            scope.launch {
                                val file = viewModel.getAttachmentFile(attachment)
                                if (file != null && file.exists()) {
                                    // Check if permission is needed (Android 9 and below)
                                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                                        // Need to check/request storage permission
                                        val hasPermission = context.checkSelfPermission(
                                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                                        ) == android.content.pm.PackageManager.PERMISSION_GRANTED

                                        if (hasPermission) {
                                            // Permission already granted, proceed with download
                                            val success = FileDownloader.saveToDownloads(context, file, attachment)
                                            if (success) {
                                                Toast.makeText(context, downloadSuccessMsg, Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(context, downloadFailedMsg, Toast.LENGTH_SHORT).show()
                                            }
                                        } else {
                                            // Request permission
                                            pendingDownloadAttachment = attachment
                                            storagePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                        }
                                    } else {
                                        // Android 10+, no permission needed (scoped storage)
                                        val success = FileDownloader.saveToDownloads(context, file, attachment)
                                        if (success) {
                                            Toast.makeText(context, downloadSuccessMsg, Toast.LENGTH_SHORT).show()
                                        } else {
                                            Toast.makeText(context, downloadFailedMsg, Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                } else {
                                    Toast.makeText(context, fileNotFoundMsg, Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        onDelete = {
                            viewModel.showDeleteAttachmentDialog(attachment)
                        }
                    )
                }
            }
        }
    }

    // Rename attachment dialog
    attachmentToRename?.let { attachment ->
        var newFileName by remember { mutableStateOf(attachment.fileName) }
        var showError by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = {
                viewModel.hideRenameAttachmentDialog()
                showError = false
            },
            title = { Text(stringResource(R.string.rename_attachment)) },
            text = {
                Column {
                    OutlinedTextField(
                        value = newFileName,
                        onValueChange = {
                            newFileName = it
                            showError = false
                        },
                        label = { Text(stringResource(R.string.new_file_name)) },
                        isError = showError,
                        supportingText = if (showError) {
                            { Text(stringResource(R.string.file_name_cannot_be_empty)) }
                        } else null,
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (newFileName.trim().isNotBlank()) {
                            viewModel.renameAttachment(newFileName.trim())
                            showError = false
                        } else {
                            showError = true
                        }
                    }
                ) {
                    Text(stringResource(R.string.rename))
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    viewModel.hideRenameAttachmentDialog()
                    showError = false
                }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    // Delete attachment confirmation dialog
    attachmentToDelete?.let { attachment ->
        AlertDialog(
            onDismissRequest = { viewModel.hideDeleteAttachmentDialog() },
            title = { Text(stringResource(R.string.delete_attachment)) },
            text = { Text(stringResource(R.string.delete_attachment_confirm, attachment.fileName)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteAttachment()
                    }
                ) {
                    Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideDeleteAttachmentDialog() }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

