package com.agcoding.cartrackingapp.domain.model

/**
 * Monthly cost data for the cost trend graph
 */
data class MonthlyCost(
    val month: String,      // e.g., "Jan", "Feb"
    val year: Int,
    val totalCost: Double,
    val fuelCost: Double,
    val serviceCost: Double,
    val otherCost: Double,
    val timestamp: Long
)

/**
 * Cost category breakdown
 */
data class CostCategory(
    val name: String,          // e.g., "Fuel", "Service", "Other"
    val amount: Double,
    val percentage: Double,    // Percentage of total
    val color: Int            // Color for visualization
)

/**
 * Recent expense item
 */
data class CostItem(
    val id: Long,
    val date: Long,
    val category: String,
    val description: String,
    val amount: Double,
    val carName: String?
)

/**
 * Contains all data for the cost trend screen
 */
data class CostTrendData(
    val monthlyCosts: List<MonthlyCost>,
    val totalCost: Double,
    val averageMonthlyCost: Double,
    val highestMonthCost: Double,
    val lowestMonthCost: Double,
    val costByCategory: List<CostCategory>,
    val recentExpenses: List<CostItem>,
    val dateRange: DateRange
)

