package com.agcoding.cartrackingapp.util

/**
 * Safe mathematical operations that handle edge cases like division by zero,
 * infinity, and NaN values.
 */

/**
 * Safely divides two doubles, returning a default value if the divisor is zero or the result is invalid
 */
fun Double.safeDivide(divisor: Double, default: Double = 0.0): Double {
    return if (divisor != 0.0 && divisor.isFinite() && this.isFinite()) {
        val result = this / divisor
        if (result.isFinite()) result else default
    } else {
        default
    }
}

/**
 * Safely divides two integers, returning a default value if the divisor is zero
 */
fun Int.safeDivide(divisor: Int, default: Int = 0): Int {
    return if (divisor != 0) this / divisor else default
}

/**
 * Safely divides two longs, returning a default value if the divisor is zero
 */
fun Long.safeDivide(divisor: Long, default: Long = 0L): Long {
    return if (divisor != 0L) this / divisor else default
}

/**
 * Calculates consumption safely (liters per 100km)
 * Returns 0.0 if distance is zero or invalid
 */
fun calculateConsumption(liters: Double, distance: Double): Double {
    if (distance <= 0.0 || !distance.isFinite() || !liters.isFinite()) {
        return 0.0
    }
    val consumption = (liters / distance) * 100.0
    return if (consumption.isFinite() && consumption >= 0) consumption else 0.0
}

/**
 * Calculates cost per kilometer safely
 * Returns 0.0 if distance is zero or invalid
 */
fun calculateCostPerKm(totalCost: Double, distance: Double): Double {
    return totalCost.safeDivide(distance, 0.0)
}

/**
 * Calculates average safely from a sum and count
 * Returns 0.0 if count is zero or invalid
 */
fun calculateAverage(sum: Double, count: Int): Double {
    return if (count > 0 && sum.isFinite()) {
        val average = sum / count
        if (average.isFinite()) average else 0.0
    } else {
        0.0
    }
}

/**
 * Ensures a double value is finite and non-negative
 */
fun Double.ensureValid(default: Double = 0.0): Double {
    return if (this.isFinite() && this >= 0) this else default
}

/**
 * Formats a double safely for display, handling edge cases
 */
fun Double.formatSafe(decimals: Int = 2, default: String = "0"): String {
    return if (this.isFinite()) {
        String.format("%.${decimals}f", this)
    } else {
        default
    }
}

