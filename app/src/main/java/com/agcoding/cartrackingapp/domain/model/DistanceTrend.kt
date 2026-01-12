package com.agcoding.cartrackingapp.domain.model

/**
 * Represents a data point in the distance trend graph
 */
data class DistanceDataPoint(
    val timestamp: Long, // Middle of the bucket
    val totalDistance: Double, // Total distance in this bucket (km)
    val refillCount: Int, // Number of refills in this bucket
    val label: String // Human-readable label (e.g., "Week 1", "Jan 2026")
)

/**
 * Represents a single trip (refill) with distance info
 */
data class TripInfo(
    val refillId: Long,
    val carId: Long,
    val carName: String,
    val timestamp: Long,
    val distance: Double, // km
    val liters: Double,
    val carColor: Int // Color indicator for the car
)

/**
 * Contains all data needed to render the distance traveled screen
 */
data class DistanceTrendData(
    val dataPoints: List<DistanceDataPoint>,
    val totalDistance: Double, // Total km in selected period
    val averageTripDistance: Double, // Average distance per refill
    val longestTrip: Double, // Longest single trip
    val shortestTrip: Double, // Shortest single trip
    val totalTrips: Int, // Number of refills in period
    val recentTrips: List<TripInfo>, // Recent trips for the list
    val dateRange: DateRange,
    val monthlyDistances: List<MonthlyDistance> // For bar chart
)

/**
 * Monthly distance for bar chart
 */
data class MonthlyDistance(
    val month: String, // e.g., "Jan", "Feb"
    val year: Int,
    val distance: Double,
    val timestamp: Long
)

