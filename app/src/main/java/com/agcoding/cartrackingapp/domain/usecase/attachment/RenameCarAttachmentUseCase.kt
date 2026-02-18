package com.agcoding.cartrackingapp.domain.usecase.attachment

import com.agcoding.cartrackingapp.domain.model.CarAttachment
import com.agcoding.cartrackingapp.domain.repository.CarAttachmentRepository
import javax.inject.Inject

/**
 * Use case to rename an attachment
 */
class RenameCarAttachmentUseCase @Inject constructor(
    private val repository: CarAttachmentRepository
) {
    suspend operator fun invoke(attachment: CarAttachment, newFileName: String): CarAttachment? {
        return repository.renameAttachment(attachment, newFileName)
    }
}

