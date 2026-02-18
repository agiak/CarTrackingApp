package com.agcoding.cartrackingapp.domain.model

/**
 * Domain model for car attachment
 */
data class CarAttachment(
    val id: Long = 0,
    val carId: Long,
    val fileName: String,
    val fileType: AttachmentType,
    val fileSizeBytes: Long,
    val dateAdded: Long = System.currentTimeMillis(),
    val internalPath: String
)

/**
 * Supported attachment types
 */
enum class AttachmentType {
    PDF,
    IMAGE;

    companion object {
        fun fromMimeType(mimeType: String?): AttachmentType? {
            return when {
                mimeType?.startsWith("image/") == true -> IMAGE
                mimeType == "application/pdf" -> PDF
                else -> null
            }
        }

        fun fromFileName(fileName: String): AttachmentType? {
            val extension = fileName.substringAfterLast('.', "").lowercase()
            return when (extension) {
                "pdf" -> PDF
                "jpg", "jpeg", "png", "webp" -> IMAGE
                else -> null
            }
        }
    }
}

