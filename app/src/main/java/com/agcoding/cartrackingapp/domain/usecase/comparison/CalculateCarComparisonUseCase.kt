package com.agcoding.cartrackingapp.domain.usecase.comparison

import com.agcoding.cartrackingapp.domain.model.Car
import com.agcoding.cartrackingapp.domain.model.CarComparisonData
import com.agcoding.cartrackingapp.domain.model.CarComparisonResult
import com.agcoding.cartrackingapp.domain.model.ComparisonDifference
import com.agcoding.cartrackingapp.domain.model.MultiCarComparisonResult
import com.agcoding.cartrackingapp.domain.repository.CarRepository
import com.agcoding.cartrackingapp.domain.repository.ExpenseRepository
import com.agcoding.cartrackingapp.domain.repository.RefillRepository
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.math.abs

/**
 * Use case for calculating car comparison metrics from local data only
 */
class CalculateCarComparisonUseCase @Inject constructor(
    private val carRepository: CarRepository,
    private val refillRepository: RefillRepository,
    private val expenseRepository: ExpenseRepository
) {

    /**
     * Get comparison data for a single car
     */
    suspend fun getCarComparisonData(carId: Long): CarComparisonData {
        val car = carRepository.getCarById(carId).first()
            ?: throw IllegalArgumentException("Car not found: $carId")

        // Get all refills for this car
        val refills = refillRepository.getRefillsByCarId(carId).first()

        // Get all expenses for this car
        val expenses = expenseRepository.getExpensesByCarId(carId).first()

        // Calculate totals
        val totalLiters = refills.sumOf { it.litersAdded }
        val totalRefillCost = refills.sumOf { it.amountPaid }
        val totalExpenseCost = expenses.sumOf { it.amount }
        val totalExpenses = totalRefillCost + totalExpenseCost

        // Calculate total kilometers from trip distances
        val totalKilometers = refills.sumOf { it.tripDistance }

        // Calculate maintenance costs (excluding fuel-related expenses)
        val maintenanceCategories = listOf("Maintenance", "Repair", "Service", "Insurance", "Inspection")
        val totalMaintenanceCost = expenses
            .filter { expense -> maintenanceCategories.any { category -> expense.category.contains(category, ignoreCase = true) } }
            .sumOf { it.amount }

        // Calculate years active (from first to last transaction)
        val allTimestamps = (refills.map { it.timestamp } + expenses.map { it.timestamp }).sorted()
        val yearsActive = if (allTimestamps.size >= 2) {
            val firstTimestamp = allTimestamps.first()
            val lastTimestamp = allTimestamps.last()
            val daysDifference = TimeUnit.MILLISECONDS.toDays(lastTimestamp - firstTimestamp)
            maxOf(daysDifference / 365.0, 0.1) // Minimum 0.1 years to avoid extreme values
        } else {
            0.1 // Default to 0.1 years if insufficient data
        }

        // Calculate metrics (handle division by zero)
        val costPerKm = if (totalKilometers > 0) totalExpenses / totalKilometers else null
        val avgConsumption = if (totalKilometers > 0) (totalLiters / totalKilometers) * 100 else null
        val maintenancePerYear = if (yearsActive > 0) totalMaintenanceCost / yearsActive else null

        // Check if data is insufficient
        val hasInsufficientData = totalKilometers < 100 || refills.size < 3

        return CarComparisonData(
            carId = carId,
            carName = car.name,
            costPerKm = costPerKm,
            avgConsumption = avgConsumption,
            maintenancePerYear = maintenancePerYear,
            totalExpenses = totalExpenses,
            totalKilometers = totalKilometers,
            totalLiters = totalLiters,
            totalMaintenanceCost = totalMaintenanceCost,
            yearsActive = yearsActive,
            hasInsufficientData = hasInsufficientData
        )
    }

    /**
     * Compare two specific cars
     */
    suspend fun compareTwoCars(car1Id: Long, car2Id: Long): CarComparisonResult {
        val car1Data = getCarComparisonData(car1Id)
        val car2Data = getCarComparisonData(car2Id)

        // Calculate differences
        val costPerKmDiff = calculateDifference(
            car1Id = car1Id,
            car1Value = car1Data.costPerKm,
            car2Id = car2Id,
            car2Value = car2Data.costPerKm,
            lowerIsBetter = true
        )

        val consumptionDiff = calculateDifference(
            car1Id = car1Id,
            car1Value = car1Data.avgConsumption,
            car2Id = car2Id,
            car2Value = car2Data.avgConsumption,
            lowerIsBetter = true
        )

        val maintenanceDiff = calculateDifference(
            car1Id = car1Id,
            car1Value = car1Data.maintenancePerYear,
            car2Id = car2Id,
            car2Value = car2Data.maintenancePerYear,
            lowerIsBetter = true
        )

        // Determine overall winner (lower cost per km is better)
        val overallWinner = when {
            car1Data.hasInsufficientData || car2Data.hasInsufficientData -> null
            car1Data.costPerKm == null || car2Data.costPerKm == null -> null
            car1Data.costPerKm < car2Data.costPerKm -> car1Id
            car2Data.costPerKm < car1Data.costPerKm -> car2Id
            else -> null // Equal
        }

        return CarComparisonResult(
            car1 = car1Data,
            car2 = car2Data,
            costPerKmDifference = costPerKmDiff,
            consumptionDifference = consumptionDiff,
            maintenanceDifference = maintenanceDiff,
            overallWinner = overallWinner
        )
    }

    /**
     * Compare all cars and highlight best/worst
     */
    suspend fun compareAllCars(): MultiCarComparisonResult {
        val allCars = carRepository.getAllCars().first()
        val carDataList = allCars.map { getCarComparisonData(it.id) }
            .filter { !it.hasInsufficientData } // Only include cars with sufficient data

        // Find best and worst for each metric
        val bestCostPerKm = carDataList.filter { it.costPerKm != null }
            .minByOrNull { it.costPerKm!! }?.carId

        val worstCostPerKm = carDataList.filter { it.costPerKm != null }
            .maxByOrNull { it.costPerKm!! }?.carId

        val bestConsumption = carDataList.filter { it.avgConsumption != null }
            .minByOrNull { it.avgConsumption!! }?.carId

        val worstConsumption = carDataList.filter { it.avgConsumption != null }
            .maxByOrNull { it.avgConsumption!! }?.carId

        val bestMaintenance = carDataList.filter { it.maintenancePerYear != null }
            .minByOrNull { it.maintenancePerYear!! }?.carId

        val worstMaintenance = carDataList.filter { it.maintenancePerYear != null }
            .maxByOrNull { it.maintenancePerYear!! }?.carId

        // Calculate overall best/worst (based on cost per km as primary metric)
        val overallBest = bestCostPerKm
        val overallWorst = worstCostPerKm

        return MultiCarComparisonResult(
            cars = carDataList,
            bestCostPerKm = bestCostPerKm,
            worstCostPerKm = worstCostPerKm,
            bestConsumption = bestConsumption,
            worstConsumption = worstConsumption,
            bestMaintenance = bestMaintenance,
            worstMaintenance = worstMaintenance,
            overallBest = overallBest,
            overallWorst = overallWorst
        )
    }

    /**
     * Calculate percentage difference between two values
     * Returns null if either value is null or if values are equal
     */
    private fun calculateDifference(
        car1Id: Long,
        car1Value: Double?,
        car2Id: Long,
        car2Value: Double?,
        lowerIsBetter: Boolean
    ): ComparisonDifference? {
        if (car1Value == null || car2Value == null) return null
        if (car1Value == car2Value) return null

        val higher = maxOf(car1Value, car2Value)
        val lower = minOf(car1Value, car2Value)

        val percentageDifference = ((higher - lower) / lower) * 100
        val absoluteDifference = abs(higher - lower)

        val higherCarId = if (car1Value > car2Value) car1Id else car2Id
        val lowerCarId = if (car1Value < car2Value) car1Id else car2Id

        return ComparisonDifference(
            higherCarId = higherCarId,
            lowerCarId = lowerCarId,
            percentageDifference = percentageDifference,
            absoluteDifference = absoluteDifference
        )
    }
}

