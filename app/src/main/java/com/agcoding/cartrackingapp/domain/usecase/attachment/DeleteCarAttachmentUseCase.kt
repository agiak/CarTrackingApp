package com.agcoding.cartrackingapp.domain.usecase.attachment

import com.agcoding.cartrackingapp.domain.model.CarAttachment
import com.agcoding.cartrackingapp.domain.repository.CarAttachmentRepository
import javax.inject.Inject

/**
 * Use case to delete a car attachment
 */
class DeleteCarAttachmentUseCase @Inject constructor(
    private val repository: CarAttachmentRepository
) {
    suspend operator fun invoke(attachment: CarAttachment): Boolean {
        return repository.deleteAttachment(attachment)
    }
}

