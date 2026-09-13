package com.agcoding.cartrackingapp.presentation.carcomparison

import com.agcoding.cartrackingapp.domain.model.CarComparisonData
import com.agcoding.cartrackingapp.domain.model.CarComparisonResult
import com.agcoding.cartrackingapp.domain.model.ComparisonDifference
import com.agcoding.cartrackingapp.domain.model.MultiCarComparisonResult

/**
 * Sample comparison data for the @Preview functions in this package.
 *
 * Shared rather than repeated in each file so the previews of the two-car and all-cars
 * layouts show the same cars, which makes the two easy to compare side by side while
 * working on them.
 */
internal fun previewComparisonCar(
    carId: Long,
    carName: String,
    costPerKm: Double?,
    avgConsumption: Double?,
    maintenancePerYear: Double?
) = CarComparisonData(
    carId = carId,
    carName = carName,
    costPerKm = costPerKm,
    avgConsumption = avgConsumption,
    maintenancePerYear = maintenancePerYear,
    totalExpenses = (costPerKm ?: 0.0) * 42_000,
    totalKilometers = 42_000.0,
    totalLiters = (avgConsumption ?: 0.0) * 420,
    totalMaintenanceCost = (maintenancePerYear ?: 0.0) * 3,
    yearsActive = 3.0,
    hasInsufficientData = costPerKm == null && avgConsumption == null
)

internal val previewCorolla = previewComparisonCar(
    carId = 1,
    carName = "Corolla",
    costPerKm = 0.148,
    avgConsumption = 6.4,
    maintenancePerYear = 320.0
)

internal val previewGolf = previewComparisonCar(
    carId = 2,
    carName = "Golf",
    costPerKm = 0.171,
    avgConsumption = 7.2,
    maintenancePerYear = 480.0
)

/** A third car with too few records, so the "not enough data" path is covered too. */
internal val previewPanda = previewComparisonCar(
    carId = 3,
    carName = "Panda",
    costPerKm = null,
    avgConsumption = null,
    maintenancePerYear = null
)

internal val previewTwoCarResult = CarComparisonResult(
    car1 = previewCorolla,
    car2 = previewGolf,
    costPerKmDifference = ComparisonDifference(
        higherCarId = 2,
        lowerCarId = 1,
        percentageDifference = 15.5,
        absoluteDifference = 0.023
    ),
    consumptionDifference = ComparisonDifference(
        higherCarId = 2,
        lowerCarId = 1,
        percentageDifference = 12.5,
        absoluteDifference = 0.8
    ),
    maintenanceDifference = ComparisonDifference(
        higherCarId = 2,
        lowerCarId = 1,
        percentageDifference = 50.0,
        absoluteDifference = 160.0
    ),
    overallWinner = 1
)

internal val previewMultiCarResult = MultiCarComparisonResult(
    cars = listOf(previewCorolla, previewGolf, previewPanda),
    bestCostPerKm = 1,
    worstCostPerKm = 2,
    bestConsumption = 1,
    worstConsumption = 2,
    bestMaintenance = 1,
    worstMaintenance = 2,
    overallBest = 1,
    overallWorst = 2
)
