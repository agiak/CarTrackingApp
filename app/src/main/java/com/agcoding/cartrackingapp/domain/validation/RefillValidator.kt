package com.agcoding.cartrackingapp.domain.validation

import android.content.Context
import com.agcoding.cartrackingapp.R

/**
 * Validator for Fuel Refill input data
 */
object RefillValidator {

    private const val MIN_LITERS = 0.1
    private const val MAX_LITERS = 2000.0 // Reasonable max for cars
    private const val MIN_COST = 0.01
    private const val MAX_COST = 10000.0 // Reasonable max
    private const val MAX_DISTANCE = 2000.0 // Warn for very long distances
    private const val MIN_CONSUMPTION = 0.5 // L/100km
    private const val MAX_CONSUMPTION = 50.0 // L/100km
    private const val MAX_ODOMETER_VALUE = 9999999.0

    fun validateRefill(
        context: Context,
        liters: String,
        cost: String,
        distance: String
    ): ValidationResult {
        val errors = mutableMapOf<String, String>()

        // Liters validation
        val litersValue = liters.toDoubleOrNull()
        when {
            litersValue == null -> errors["liters"] = context.getString(R.string.error_liters_invalid)
            litersValue <= 0 -> errors["liters"] = context.getString(R.string.error_liters_positive)
            litersValue < MIN_LITERS -> errors["liters"] = context.getString(R.string.error_liters_too_small, MIN_LITERS.toString())
            litersValue > MAX_LITERS -> errors["liters"] = context.getString(R.string.error_liters_too_high, MAX_LITERS.toInt().toString())
        }

        // Cost validation
        val costValue = cost.toDoubleOrNull()
        when {
            costValue == null -> errors["cost"] = context.getString(R.string.error_cost_invalid)
            costValue <= 0 -> errors["cost"] = context.getString(R.string.error_cost_positive)
            costValue < MIN_COST -> errors["cost"] = context.getString(R.string.error_cost_too_small)
            costValue > MAX_COST -> errors["cost"] = context.getString(R.string.error_cost_too_high)
        }

        // Distance validation
        val distanceValue = distance.toDoubleOrNull()
        when {
            distanceValue == null -> errors["distance"] = context.getString(R.string.error_distance_invalid)
            distanceValue <= 0 -> errors["distance"] = context.getString(R.string.error_distance_positive)
            distanceValue > MAX_DISTANCE -> errors["distance"] = context.getString(R.string.error_distance_too_high, distanceValue.toInt())
        }

        // Consumption validation - display as general error, not field error
        if (litersValue != null && litersValue > 0 && distanceValue != null && distanceValue > 0) {
            val consumption = (litersValue / distanceValue) * 100
            if (consumption < MIN_CONSUMPTION) {
                errors["consumption"] = context.getString(R.string.error_consumption_too_low, consumption)
            } else if (consumption > MAX_CONSUMPTION) {
                errors["consumption"] = context.getString(R.string.error_consumption_too_high, consumption)
            }
        }

        return if (errors.isEmpty()) {
            ValidationResult.success()
        } else {
            ValidationResult.failure(errors)
        }
    }

    fun validateLiters(context: Context, liters: String): String? {
        val value = liters.toDoubleOrNull()
        return when {
            value == null -> context.getString(R.string.error_liters_invalid)
            value <= 0 -> context.getString(R.string.error_liters_positive)
            value < MIN_LITERS -> context.getString(R.string.error_liters_too_small, MIN_LITERS.toString())
            value > MAX_LITERS -> context.getString(R.string.error_liters_too_high, MAX_LITERS.toInt().toString())
            else -> null
        }
    }

    fun validateCost(context: Context, cost: String): String? {
        val value = cost.toDoubleOrNull()
        return when {
            value == null -> context.getString(R.string.error_cost_invalid)
            value <= 0 -> context.getString(R.string.error_cost_positive)
            value < MIN_COST -> context.getString(R.string.error_cost_too_small)
            value > MAX_COST -> context.getString(R.string.error_cost_too_high)
            else -> null
        }
    }

    fun validateDistance(context: Context, distance: String): String? {
        val value = distance.toDoubleOrNull()
        return when {
            value == null -> context.getString(R.string.error_distance_invalid)
            value <= 0 -> context.getString(R.string.error_distance_positive)
            value > MAX_DISTANCE -> context.getString(R.string.error_distance_too_high, value.toInt())
            else -> null
        }
    }

    fun validateOdometer(context: Context, odometer: String, previousOdometer: Double): String? {
        val value = odometer.toDoubleOrNull()
        return when {
            value == null -> context.getString(R.string.error_odometer_invalid)
            value < 0 -> context.getString(R.string.error_odometer_negative)
            value > MAX_ODOMETER_VALUE -> context.getString(R.string.error_odometer_unrealistic)
            value <= previousOdometer -> {
                context.getString(R.string.error_odometer_backwards, previousOdometer.toInt(), value.toInt())
            }
            else -> null
        }
    }
}
