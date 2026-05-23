package com.agcoding.cartrackingapp.domain.usecase.trash

import com.agcoding.cartrackingapp.domain.repository.CarRepository
import com.agcoding.cartrackingapp.domain.repository.ExpenseRepository
import com.agcoding.cartrackingapp.domain.repository.RefillRepository
import com.agcoding.cartrackingapp.domain.repository.TripRepository
import com.agcoding.cartrackingapp.shared.domain.error.AppError
import com.agcoding.cartrackingapp.shared.domain.result.Result
import javax.inject.Inject

class RestoreTrashItemUseCase @Inject constructor(
    private val carRepository: CarRepository,
    private val refillRepository: RefillRepository,
    private val expenseRepository: ExpenseRepository,
    private val tripRepository: TripRepository,
) {
    suspend fun restoreCar(carId: Long): Result<Unit> = try {
        carRepository.restoreCar(carId)
        Result.Success(Unit)
    } catch (e: Exception) { Result.Error(AppError.DatabaseError(e)) }

    suspend fun restoreRefill(refillId: Long): Result<Unit> = try {
        refillRepository.restoreRefill(refillId)
        Result.Success(Unit)
    } catch (e: Exception) { Result.Error(AppError.DatabaseError(e)) }

    suspend fun restoreExpense(expenseId: Long): Result<Unit> = try {
        expenseRepository.restoreExpense(expenseId)
        Result.Success(Unit)
    } catch (e: Exception) { Result.Error(AppError.DatabaseError(e)) }

    suspend fun restoreTrip(tripId: Long): Result<Unit> = try {
        tripRepository.restoreTrip(tripId)
        Result.Success(Unit)
    } catch (e: Exception) { Result.Error(AppError.DatabaseError(e)) }
}
