package com.agcoding.cartrackingapp.domain.usecase.attachment

import android.net.Uri
import com.agcoding.cartrackingapp.domain.model.AttachmentType
import com.agcoding.cartrackingapp.domain.model.CarAttachment
import com.agcoding.cartrackingapp.domain.repository.CarAttachmentRepository
import javax.inject.Inject

/**
 * Use case to add a new attachment to a car
 *
 * Validates file type and size before adding
 */
class AddCarAttachmentUseCase @Inject constructor(
    private val repository: CarAttachmentRepository
) {
    companion object {
        private const val MAX_FILE_SIZE_BYTES = 50L * 1024 * 1024 // 50 MB
    }

    suspend operator fun invoke(
        carId: Long,
        uri: Uri,
        fileName: String,
        mimeType: String?,
        fileSizeBytes: Long
    ): Result {
        // Validate file type
        val fileType = AttachmentType.fromMimeType(mimeType)
            ?: AttachmentType.fromFileName(fileName)

        if (fileType == null) {
            return Result.UnsupportedFileType
        }

        // Validate file size
        if (fileSizeBytes > MAX_FILE_SIZE_BYTES) {
            return Result.FileTooLarge(MAX_FILE_SIZE_BYTES)
        }

        if (fileSizeBytes <= 0) {
            return Result.InvalidFile
        }

        // Add attachment
        val attachment = repository.addAttachment(
            carId = carId,
            uri = uri,
            fileName = fileName,
            mimeType = mimeType
        )

        return if (attachment != null) {
            Result.Success(attachment)
        } else {
            Result.Failed
        }
    }

    sealed class Result {
        data class Success(val attachment: CarAttachment) : Result()
        object UnsupportedFileType : Result()
        data class FileTooLarge(val maxSizeBytes: Long) : Result()
        object InvalidFile : Result()
        object Failed : Result()
    }
}

