package com.agcoding.cartrackingapp.data.repository

import android.content.Context
import android.net.Uri
import android.util.Log
import com.agcoding.cartrackingapp.data.local.database.dao.CarAttachmentDao
import com.agcoding.cartrackingapp.data.mapper.CarAttachmentMapper
import com.agcoding.cartrackingapp.domain.model.AttachmentType
import com.agcoding.cartrackingapp.domain.model.CarAttachment
import com.agcoding.cartrackingapp.domain.repository.CarAttachmentRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.File
import java.io.FileOutputStream
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of CarAttachmentRepository
 *
 * Files are stored in app-specific internal storage organized by car ID:
 * app_files/
 *   └── attachments/
 *       └── car_{carId}/
 *           └── {uuid}_{filename}
 */
@Singleton
class CarAttachmentRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dao: CarAttachmentDao
) : CarAttachmentRepository {

    companion object {
        private const val TAG = "CarAttachmentRepo"
        private const val ATTACHMENTS_DIR = "attachments"
    }

    override fun getAttachmentsForCar(carId: Long): Flow<List<CarAttachment>> {
        return dao.getAttachmentsForCar(carId).map { entities ->
            entities.map { CarAttachmentMapper.toDomain(it) }
        }
    }

    override suspend fun addAttachment(
        carId: Long,
        uri: Uri,
        fileName: String,
        mimeType: String?
    ): CarAttachment? {
        return try {
            // Determine file type
            val fileType = AttachmentType.fromMimeType(mimeType)
                ?: AttachmentType.fromFileName(fileName)
                ?: return null

            // Get file size
            val fileSize = getFileSize(uri)
            if (fileSize <= 0) {
                Log.e(TAG, "Invalid file size: $fileSize")
                return null
            }

            // Create car-specific directory
            val carDir = getCarAttachmentsDir(carId)
            if (!carDir.exists()) {
                carDir.mkdirs()
            }

            // Generate unique filename
            val uniqueFileName = "${UUID.randomUUID()}_$fileName"
            val destinationFile = File(carDir, uniqueFileName)

            // Copy file to internal storage
            val success = copyFile(uri, destinationFile)
            if (!success) {
                Log.e(TAG, "Failed to copy file")
                return null
            }

            // Create relative path for storage
            val relativePath = "car_$carId/$uniqueFileName"

            // Create entity
            val entity = com.agcoding.cartrackingapp.data.local.database.entity.CarAttachmentEntity(
                carId = carId,
                fileName = fileName,
                fileType = fileType.name,
                fileSizeBytes = fileSize,
                dateAdded = System.currentTimeMillis(),
                internalPath = relativePath
            )

            // Save to database
            val id = dao.insert(entity)

            // Return domain model
            CarAttachment(
                id = id,
                carId = carId,
                fileName = fileName,
                fileType = fileType,
                fileSizeBytes = fileSize,
                dateAdded = entity.dateAdded,
                internalPath = relativePath
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error adding attachment", e)
            null
        }
    }

    override suspend fun deleteAttachment(attachment: CarAttachment): Boolean {
        return try {
            // Delete physical file
            val file = getAttachmentFile(attachment)
            if (file?.exists() == true) {
                file.delete()
            }

            // Delete database record
            val entity = CarAttachmentMapper.toEntity(attachment)
            dao.delete(entity)

            true
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting attachment", e)
            false
        }
    }

    override suspend fun renameAttachment(attachment: CarAttachment, newFileName: String): CarAttachment? {
        return try {
            Log.d(TAG, "Renaming attachment ${attachment.id} to: $newFileName")

            // Validate new file name
            if (newFileName.isBlank()) {
                Log.e(TAG, "New file name cannot be blank")
                return null
            }

            // Update database record with new file name
            val entity = dao.getAttachmentById(attachment.id)
            if (entity == null) {
                Log.e(TAG, "Attachment not found in database")
                return null
            }

            val updatedEntity = entity.copy(fileName = newFileName.trim())
            dao.update(updatedEntity)

            // Return updated domain model
            CarAttachmentMapper.toDomain(updatedEntity)
        } catch (e: Exception) {
            Log.e(TAG, "Error renaming attachment", e)
            null
        }
    }

    override suspend fun getAttachmentFile(attachment: CarAttachment): File? {
        return try {
            val attachmentsDir = File(context.filesDir, ATTACHMENTS_DIR)
            val file = File(attachmentsDir, attachment.internalPath)

            if (file.exists() && file.canRead()) {
                file
            } else {
                Log.e(TAG, "Attachment file not found or not readable: ${attachment.internalPath}")
                null
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting attachment file", e)
            null
        }
    }

    override suspend fun deleteAttachmentsForCar(carId: Long) {
        try {
            // Delete all database records
            dao.deleteAllForCar(carId)

            // Delete car directory
            val carDir = getCarAttachmentsDir(carId)
            if (carDir.exists()) {
                carDir.deleteRecursively()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting attachments for car", e)
        }
    }

    /**
     * Get car-specific attachments directory
     */
    private fun getCarAttachmentsDir(carId: Long): File {
        val attachmentsDir = File(context.filesDir, ATTACHMENTS_DIR)
        return File(attachmentsDir, "car_$carId")
    }

    /**
     * Get file size from URI
     */
    private fun getFileSize(uri: Uri): Long {
        return try {
            context.contentResolver.openAssetFileDescriptor(uri, "r")?.use {
                it.length
            } ?: 0L
        } catch (e: Exception) {
            Log.e(TAG, "Error getting file size", e)
            0L
        }
    }

    /**
     * Copy file from URI to destination
     */
    private fun copyFile(sourceUri: Uri, destination: File): Boolean {
        return try {
            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(destination).use { output ->
                    input.copyTo(output)
                }
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error copying file", e)
            // Clean up partial file if exists
            if (destination.exists()) {
                destination.delete()
            }
            false
        }
    }
}

