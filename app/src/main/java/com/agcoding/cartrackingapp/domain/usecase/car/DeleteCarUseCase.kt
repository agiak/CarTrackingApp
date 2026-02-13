package com.agcoding.cartrackingapp.domain.usecase.car

import android.content.Context
import com.agcoding.cartrackingapp.domain.repository.CarRepository
import com.agcoding.cartrackingapp.widget.QuickAddWidgetReceiver
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class DeleteCarUseCase @Inject constructor(
    private val carRepository: CarRepository,
    @ApplicationContext private val context: Context
) {
    suspend operator fun invoke(carId: Long): Result<Unit> {
        return try {
            carRepository.deleteCar(carId)

            // Update widgets when a car is deleted
            QuickAddWidgetReceiver.updateWidgets(context)

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

