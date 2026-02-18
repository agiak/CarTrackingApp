package com.agcoding.cartrackingapp.domain.model

import java.time.YearMonth

/**
 * Represents aggregated fuel metrics for a single month.
 * Used as input for forecasting algorithms.
 *
 * @param month The year-month period
 * @param costPerKm Total fuel cost divided by total kilometers traveled
 * @param efficiency Fuel efficiency in L/100km
 */
data class MonthlyFuelMetric(
    val month: YearMonth,
    val costPerKm: Double,
    val efficiency: Double
)

