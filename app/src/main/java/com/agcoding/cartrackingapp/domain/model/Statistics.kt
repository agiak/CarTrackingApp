package com.agcoding.cartrackingapp.domain.model

data class CarStatistics(
    val car: Car,
    val averageConsumption: Double,
    val totalCost: Double,
    val totalDistance: Double,
    val totalRefills: Int,
    val averagePricePerLiter: Double,
    val totalLiters: Double,
    val recentRefills: List<FuelRefill>,
    val recentExpenses: List<Expense> = emptyList(),
    val totalExpensesCost: Double = 0.0,
    // Expense breakdown
    val serviceExpensesCost: Double = 0.0,
    val otherExpensesCost: Double = 0.0,
    val serviceExpenseCount: Int = 0,
    val otherExpenseCount: Int = 0,
    val costPerKilometer: Double = 0.0
)

data class GlobalStatistics(
    val totalCars: Int,
    val totalRefills: Int,
    val totalCost: Double,
    val totalDistance: Double,
    val totalLiters: Double,
    val averageConsumption: Double,
    val averagePricePerLiter: Double,
    val mostEfficientCar: Car?,
    val mostExpensiveCar: Car?,
    val monthlyTrends: List<MonthlyTrend>,
    val perCarStatistics: List<CarStatistics> = emptyList(),
    // Expense-related fields
    val totalServiceExpenses: Double = 0.0,
    val totalOtherExpenses: Double = 0.0,
    val totalExpensesCost: Double = 0.0,
    val serviceExpenseCount: Int = 0,
    val otherExpenseCount: Int = 0,
    val totalExpenseCount: Int = 0,
    val costPerKilometer: Double = 0.0,
    // Trip-related fields
    val totalTrips: Int = 0,
    val tripDistance: Double = 0.0,
    val tripAverageConsumption: Double = 0.0,
    val tripRefillCount: Int = 0
)

data class MonthlyTrend(
    val month: Int,
    val year: Int,
    val monthName: String,
    val totalCost: Double,
    val totalLiters: Double,
    val totalDistance: Double,
    val averageConsumption: Double,
    val refillCount: Int,
    // Expense data
    val expenseCount: Int = 0,
    val expenseCost: Double = 0.0,
    // Combined total (refills + expenses)
    val totalCombinedCost: Double = totalCost + expenseCost
)

