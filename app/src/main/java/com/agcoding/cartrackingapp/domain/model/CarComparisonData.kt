package com.agcoding.cartrackingapp.domain.model

/**
 * Car comparison metrics calculated from local data
 */
data class CarComparisonData(
    val carId: Long,
    val carName: String,
    val costPerKm: Double?, // Total expenses / total kilometers
    val avgConsumption: Double?, // Total liters / total kilometers * 100 (L/100km)
    val maintenancePerYear: Double?, // Total maintenance cost / years active
    val totalExpenses: Double,
    val totalKilometers: Double,
    val totalLiters: Double,
    val totalMaintenanceCost: Double,
    val yearsActive: Double,
    val hasInsufficientData: Boolean
)

/**
 * Comparison result between two cars
 */
data class CarComparisonResult(
    val car1: CarComparisonData,
    val car2: CarComparisonData,
    val costPerKmDifference: ComparisonDifference?,
    val consumptionDifference: ComparisonDifference?,
    val maintenanceDifference: ComparisonDifference?,
    val overallWinner: Long? // Car ID of the overall more economical car
)

/**
 * Difference between two values with percentage
 */
data class ComparisonDifference(
    val higherCarId: Long,
    val lowerCarId: Long,
    val percentageDifference: Double, // Positive percentage
    val absoluteDifference: Double
)

/**
 * Multi-car comparison result
 */
data class MultiCarComparisonResult(
    val cars: List<CarComparisonData>,
    val bestCostPerKm: Long?, // Car ID
    val worstCostPerKm: Long?, // Car ID
    val bestConsumption: Long?, // Car ID
    val worstConsumption: Long?, // Car ID
    val bestMaintenance: Long?, // Car ID
    val worstMaintenance: Long?, // Car ID
    val overallBest: Long?, // Car ID of most economical overall
    val overallWorst: Long? // Car ID of least economical overall
)

