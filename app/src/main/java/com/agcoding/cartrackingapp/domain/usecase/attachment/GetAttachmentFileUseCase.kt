package com.agcoding.cartrackingapp.domain.usecase.attachment

import com.agcoding.cartrackingapp.domain.model.CarAttachment
import com.agcoding.cartrackingapp.domain.repository.CarAttachmentRepository
import java.io.File
import javax.inject.Inject

/**
 * Use case to get file for opening/viewing
 */
class GetAttachmentFileUseCase @Inject constructor(
    private val repository: CarAttachmentRepository
) {
    suspend operator fun invoke(attachment: CarAttachment): File? {
        return repository.getAttachmentFile(attachment)
    }
}

