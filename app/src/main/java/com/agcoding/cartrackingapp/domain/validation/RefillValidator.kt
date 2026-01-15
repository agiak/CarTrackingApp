package com.agcoding.cartrackingapp.domain.validation

/**
 * Validator for Fuel Refill input data
 */
object RefillValidator {

    private const val MIN_LITERS = 0.1
    private const val MAX_LITERS = 200.0 // Reasonable max for cars
    private const val MIN_COST = 0.01
    private const val MAX_COST = 1000.0 // Reasonable max
    private const val MAX_DISTANCE = 2000.0 // Warn for very long distances
    private const val MIN_CONSUMPTION = 2.0 // L/100km
    private const val MAX_CONSUMPTION = 50.0 // L/100km
    private const val MAX_ODOMETER_VALUE = 9999999.0

    fun validateRefill(
        liters: String,
        cost: String,
        odometer: String,
        previousOdometer: Double
    ): ValidationResult {
        val errors = mutableMapOf<String, String>()

        // Liters validation
        val litersValue = liters.toDoubleOrNull()
        when {
            litersValue == null -> errors["liters"] = "Liters must be a valid number"
            litersValue <= 0 -> errors["liters"] = "Liters must be greater than 0"
            litersValue < MIN_LITERS -> errors["liters"] = "Liters seems too small. Minimum is $MIN_LITERS L"
            litersValue > MAX_LITERS -> errors["liters"] = "Liters seems too high (max $MAX_LITERS L). Please verify."
        }

        // Cost validation
        val costValue = cost.toDoubleOrNull()
        when {
            costValue == null -> errors["cost"] = "Cost must be a valid number"
            costValue <= 0 -> errors["cost"] = "Cost must be greater than 0"
            costValue < MIN_COST -> errors["cost"] = "Cost seems too small"
            costValue > MAX_COST -> errors["cost"] = "Cost seems very high. Please verify."
        }

        // Odometer validation
        val odometerValue = odometer.toDoubleOrNull()
        when {
            odometerValue == null -> errors["odometer"] = "Odometer must be a valid number"
            odometerValue < 0 -> errors["odometer"] = "Odometer cannot be negative"
            odometerValue > MAX_ODOMETER_VALUE -> errors["odometer"] = "Odometer value seems unrealistic"
            odometerValue <= previousOdometer -> {
                val prevOdoInt = previousOdometer.toInt()
                val currentOdoInt = odometerValue.toInt()
                errors["odometer"] = "Odometer cannot go backwards (previous: ${prevOdoInt}km, current: ${currentOdoInt}km)"
            }
        }

        // Distance validation (derived)
        if (odometerValue != null && odometerValue > previousOdometer) {
            val distance = odometerValue - previousOdometer
            if (distance > MAX_DISTANCE) {
                errors["odometer"] = "Distance of ${distance.toInt()}km seems very high. Please verify odometer reading."
            }

            // Consumption validation
            if (litersValue != null && litersValue > 0 && distance > 0) {
                val consumption = (litersValue / distance) * 100
                if (consumption < MIN_CONSUMPTION) {
                    errors["liters"] = "Consumption of %.1f L/100km seems unusually low. Please verify.".format(consumption)
                } else if (consumption > MAX_CONSUMPTION) {
                    errors["liters"] = "Consumption of %.1f L/100km seems unusually high. Please verify.".format(consumption)
                }
            }
        }

        return if (errors.isEmpty()) {
            ValidationResult.success()
        } else {
            ValidationResult.failure(errors)
        }
    }

    fun validateLiters(liters: String): String? {
        val value = liters.toDoubleOrNull()
        return when {
            value == null -> "Liters must be a valid number"
            value <= 0 -> "Liters must be greater than 0"
            value < MIN_LITERS -> "Liters seems too small. Minimum is $MIN_LITERS L"
            value > MAX_LITERS -> "Liters seems too high (max $MAX_LITERS L)"
            else -> null
        }
    }

    fun validateCost(cost: String): String? {
        val value = cost.toDoubleOrNull()
        return when {
            value == null -> "Cost must be a valid number"
            value <= 0 -> "Cost must be greater than 0"
            value < MIN_COST -> "Cost seems too small"
            value > MAX_COST -> "Cost seems very high"
            else -> null
        }
    }

    fun validateOdometer(odometer: String, previousOdometer: Double): String? {
        val value = odometer.toDoubleOrNull()
        return when {
            value == null -> "Odometer must be a valid number"
            value < 0 -> "Odometer cannot be negative"
            value > MAX_ODOMETER_VALUE -> "Odometer value seems unrealistic"
            value <= previousOdometer -> {
                "Odometer cannot go backwards (previous: ${previousOdometer.toInt()}km)"
            }
            else -> null
        }
    }
}

