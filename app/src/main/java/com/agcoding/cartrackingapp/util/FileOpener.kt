package com.agcoding.cartrackingapp.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.util.Log
import android.webkit.MimeTypeMap
import androidx.core.content.FileProvider
import com.agcoding.cartrackingapp.BuildConfig
import com.agcoding.cartrackingapp.domain.model.AttachmentType
import com.agcoding.cartrackingapp.domain.model.CarAttachment
import java.io.File

/**
 * Utility for opening attachment files securely using FileProvider
 */
object FileOpener {

    private const val TAG = "FileOpener"

    /**
     * Get FileProvider authority - works for both debug and release builds
     */
    private fun getAuthority(): String {
        return "${BuildConfig.APPLICATION_ID}.fileprovider"
    }

    /**
     * Open attachment file using appropriate app
     */
    fun openAttachment(context: Context, file: File, attachment: CarAttachment): Boolean {
        return try {
            Log.d(TAG, "Opening file: ${file.absolutePath}")
            Log.d(TAG, "File exists: ${file.exists()}, Can read: ${file.canRead()}, Size: ${file.length()}")

            if (!file.exists()) {
                Log.e(TAG, "File does not exist: ${file.absolutePath}")
                return false
            }

            if (!file.canRead()) {
                Log.e(TAG, "Cannot read file: ${file.absolutePath}")
                return false
            }

            val authority = getAuthority()
            Log.d(TAG, "Using FileProvider authority: $authority")

            val uri = FileProvider.getUriForFile(context, authority, file)
            Log.d(TAG, "Generated URI: $uri")

            // Get MIME type - try to get specific type first, fallback to general
            val mimeType = getSpecificMimeType(file) ?: getMimeType(attachment.fileType)
            Log.d(TAG, "MIME type: $mimeType")

            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            // Use chooser to let user select which app to open the file with
            // This ensures there's always a way to open the file
            val chooserIntent = Intent.createChooser(intent, "Open with").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(chooserIntent)
            Log.d(TAG, "Successfully opened file")
            true
        } catch (e: ActivityNotFoundException) {
            Log.e(TAG, "No app found to open file", e)
            false
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "Failed to get URI for file - FileProvider issue", e)
            false
        } catch (e: Exception) {
            Log.e(TAG, "Error opening file", e)
            e.printStackTrace()
            false
        }
    }

    /**
     * Get specific MIME type from file extension
     */
    private fun getSpecificMimeType(file: File): String? {
        val extension = file.extension.lowercase()
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
    }

    /**
     * Get MIME type for attachment type (fallback)
     */
    private fun getMimeType(type: AttachmentType): String {
        return when (type) {
            AttachmentType.PDF -> "application/pdf"
            AttachmentType.IMAGE -> "image/*"
        }
    }
}

