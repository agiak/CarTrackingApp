package com.agcoding.cartrackingapp.domain.model

/**
 * A count of what this phone is holding.
 *
 * [total] deliberately leaves cars out. A car is the thing records are filed
 * under, not a record itself, and adding it to the total makes "3 cars, 123
 * records" not add up for anyone who tries.
 */
data class DatabaseStats(
    val cars: Int = 0,
    val refills: Int = 0,
    val expenses: Int = 0,
    val trips: Int = 0,
) {
    val total: Int get() = refills + expenses + trips

    val isEmpty: Boolean get() = cars == 0 && total == 0
}
