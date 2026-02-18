package com.agcoding.cartrackingapp.domain.repository

import android.net.Uri
import com.agcoding.cartrackingapp.domain.model.CarAttachment
import kotlinx.coroutines.flow.Flow
import java.io.File

/**
 * Repository interface for car attachments
 */
interface CarAttachmentRepository {

    /**
     * Get all attachments for a specific car
     */
    fun getAttachmentsForCar(carId: Long): Flow<List<CarAttachment>>

    /**
     * Add a new attachment
     * @param carId Car ID
     * @param uri Source file URI from file picker
     * @param fileName Original file name
     * @param mimeType MIME type of the file
     * @return Created attachment or null if failed
     */
    suspend fun addAttachment(
        carId: Long,
        uri: Uri,
        fileName: String,
        mimeType: String?
    ): CarAttachment?

    /**
     * Delete an attachment
     * Removes both file and database record
     */
    suspend fun deleteAttachment(attachment: CarAttachment): Boolean

    /**
     * Rename an attachment
     * Updates the file name in the database
     * @param attachment The attachment to rename
     * @param newFileName The new file name
     * @return Updated attachment or null if failed
     */
    suspend fun renameAttachment(attachment: CarAttachment, newFileName: String): CarAttachment?

    /**
     * Get file for opening/sharing
     */
    suspend fun getAttachmentFile(attachment: CarAttachment): File?

    /**
     * Delete all attachments for a car (cascade delete)
     */
    suspend fun deleteAttachmentsForCar(carId: Long)
}

