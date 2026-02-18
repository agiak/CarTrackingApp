package com.agcoding.cartrackingapp.domain.usecase.attachment

import com.agcoding.cartrackingapp.domain.model.CarAttachment
import com.agcoding.cartrackingapp.domain.repository.CarAttachmentRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case to get all attachments for a car
 */
class GetCarAttachmentsUseCase @Inject constructor(
    private val repository: CarAttachmentRepository
) {
    operator fun invoke(carId: Long): Flow<List<CarAttachment>> {
        return repository.getAttachmentsForCar(carId)
    }
}

