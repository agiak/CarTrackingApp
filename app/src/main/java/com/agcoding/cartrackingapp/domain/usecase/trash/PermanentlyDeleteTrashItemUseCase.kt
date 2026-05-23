package com.agcoding.cartrackingapp.domain.usecase.trash

import com.agcoding.cartrackingapp.domain.repository.CarRepository
import com.agcoding.cartrackingapp.domain.repository.ExpenseRepository
import com.agcoding.cartrackingapp.domain.repository.RefillRepository
import com.agcoding.cartrackingapp.domain.repository.TripRepository
import com.agcoding.cartrackingapp.shared.domain.error.AppError
import com.agcoding.cartrackingapp.shared.domain.result.Result
import javax.inject.Inject

class PermanentlyDeleteTrashItemUseCase @Inject constructor(
    private val carRepository: CarRepository,
    private val refillRepository: RefillRepository,
    private val expenseRepository: ExpenseRepository,
    private val tripRepository: TripRepository,
) {
    suspend fun deleteCar(carId: Long): Result<Unit> = try {
        carRepository.permanentlyDeleteCar(carId)
        Result.Success(Unit)
    } catch (e: Exception) { Result.Error(AppError.DatabaseError(e)) }

    suspend fun deleteRefill(refillId: Long): Result<Unit> = try {
        refillRepository.permanentlyDeleteRefill(refillId)
        Result.Success(Unit)
    } catch (e: Exception) { Result.Error(AppError.DatabaseError(e)) }

    suspend fun deleteExpense(expenseId: Long): Result<Unit> = try {
        expenseRepository.permanentlyDeleteExpense(expenseId)
        Result.Success(Unit)
    } catch (e: Exception) { Result.Error(AppError.DatabaseError(e)) }

    suspend fun deleteTrip(tripId: Long): Result<Unit> = try {
        tripRepository.permanentlyDeleteTrip(tripId)
        Result.Success(Unit)
    } catch (e: Exception) { Result.Error(AppError.DatabaseError(e)) }
}
