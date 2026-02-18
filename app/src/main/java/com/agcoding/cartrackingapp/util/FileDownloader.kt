package com.agcoding.cartrackingapp.util

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.RequiresApi
import com.agcoding.cartrackingapp.domain.model.CarAttachment
import java.io.File
import java.io.FileInputStream

/**
 * Utility for downloading/saving attachments to device Downloads folder
 */
object FileDownloader {

    private const val TAG = "FileDownloader"

    /**
     * Save attachment file to Downloads folder
     *
     * @param context Android context
     * @param sourceFile File to save
     * @param attachment Attachment metadata
     * @return Success/failure
     */
    fun saveToDownloads(context: Context, sourceFile: File, attachment: CarAttachment): Boolean {
        return try {
            Log.d(TAG, "Saving file to Downloads: ${attachment.fileName}")

            if (!sourceFile.exists() || !sourceFile.canRead()) {
                Log.e(TAG, "Source file not readable: ${sourceFile.absolutePath}")
                return false
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // Android 10+ - Use MediaStore (scoped storage)
                saveUsingMediaStore(context, sourceFile, attachment)
            } else {
                // Android 9 and below - Direct file access
                saveUsingLegacyMethod(sourceFile, attachment)
            }

            Log.d(TAG, "Successfully saved file to Downloads")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error saving file to Downloads", e)
            false
        }
    }

    /**
     * Save using MediaStore API (Android 10+)
     * No permissions needed - uses scoped storage
     */
    @RequiresApi(Build.VERSION_CODES.Q)
    private fun saveUsingMediaStore(
        context: Context,
        sourceFile: File,
        attachment: CarAttachment
    ) {
        val contentResolver = context.contentResolver

        // Prepare content values
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, attachment.fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, getMimeType(attachment))
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }

        // Insert into MediaStore
        val uri = contentResolver.insert(
            MediaStore.Downloads.EXTERNAL_CONTENT_URI,
            contentValues
        ) ?: throw Exception("Failed to create MediaStore entry")

        // Copy file content
        contentResolver.openOutputStream(uri)?.use { outputStream ->
            FileInputStream(sourceFile).use { inputStream ->
                inputStream.copyTo(outputStream)
            }
        } ?: throw Exception("Failed to open output stream")
    }

    /**
     * Save using legacy method (Android 9 and below)
     * Requires WRITE_EXTERNAL_STORAGE permission
     */
    private fun saveUsingLegacyMethod(
        sourceFile: File,
        attachment: CarAttachment
    ) {
        val downloadsDir = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_DOWNLOADS
        )

        // Create Downloads directory if it doesn't exist
        if (!downloadsDir.exists()) {
            downloadsDir.mkdirs()
        }

        // Destination file
        val destFile = File(downloadsDir, attachment.fileName)

        // Handle duplicate file names
        val finalDestFile = getUniqueFileName(destFile)

        // Copy file
        FileInputStream(sourceFile).use { inputStream ->
            finalDestFile.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
    }

    /**
     * Get unique file name if file already exists
     * Appends (1), (2), etc. to avoid overwriting
     */
    private fun getUniqueFileName(file: File): File {
        if (!file.exists()) return file

        val nameWithoutExt = file.nameWithoutExtension
        val extension = file.extension
        val parentDir = file.parentFile ?: return file

        var counter = 1
        var newFile: File

        do {
            val newName = if (extension.isNotEmpty()) {
                "$nameWithoutExt ($counter).$extension"
            } else {
                "$nameWithoutExt ($counter)"
            }
            newFile = File(parentDir, newName)
            counter++
        } while (newFile.exists())

        return newFile
    }

    /**
     * Get MIME type from attachment
     */
    private fun getMimeType(attachment: CarAttachment): String {
        return when (attachment.fileType) {
            com.agcoding.cartrackingapp.domain.model.AttachmentType.PDF -> "application/pdf"
            com.agcoding.cartrackingapp.domain.model.AttachmentType.IMAGE -> {
                // Try to get specific image MIME type from file extension
                val extension = attachment.fileName.substringAfterLast('.', "")
                when (extension.lowercase()) {
                    "jpg", "jpeg" -> "image/jpeg"
                    "png" -> "image/png"
                    "gif" -> "image/gif"
                    "webp" -> "image/webp"
                    else -> "image/*"
                }
            }
        }
    }
}

