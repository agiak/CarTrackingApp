package com.agcoding.cartrackingapp.data.mapper

import com.agcoding.cartrackingapp.data.local.database.entity.CarAttachmentEntity
import com.agcoding.cartrackingapp.domain.model.AttachmentType
import com.agcoding.cartrackingapp.domain.model.CarAttachment

/**
 * Mapper for CarAttachment
 */
object CarAttachmentMapper {

    fun toDomain(entity: CarAttachmentEntity): CarAttachment {
        return CarAttachment(
            id = entity.id,
            carId = entity.carId,
            fileName = entity.fileName,
            fileType = AttachmentType.valueOf(entity.fileType),
            fileSizeBytes = entity.fileSizeBytes,
            dateAdded = entity.dateAdded,
            internalPath = entity.internalPath
        )
    }

    fun toEntity(domain: CarAttachment): CarAttachmentEntity {
        return CarAttachmentEntity(
            id = domain.id,
            carId = domain.carId,
            fileName = domain.fileName,
            fileType = domain.fileType.name,
            fileSizeBytes = domain.fileSizeBytes,
            dateAdded = domain.dateAdded,
            internalPath = domain.internalPath
        )
    }
}

