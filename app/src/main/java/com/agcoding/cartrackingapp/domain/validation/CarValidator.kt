package com.agcoding.cartrackingapp.domain.validation

/**
 * Validator for Car input data
 */
object CarValidator {

    private const val MAX_NAME_LENGTH = 50
    private const val MAX_LICENSE_PLATE_LENGTH = 20
    private const val MAX_ODOMETER_VALUE = 9999999.0 // 10 million km

    fun validateCar(
        name: String,
        licensePlate: String,
        currentOdometer: String,
        initialOdometer: String
    ): ValidationResult {
        val errors = mutableMapOf<String, String>()

        // Name validation
        when {
            name.isBlank() -> errors["name"] = "Car name cannot be empty"
            name.trim().length < 2 -> errors["name"] = "Car name must be at least 2 characters"
            name.length > MAX_NAME_LENGTH -> errors["name"] = "Car name too long (max $MAX_NAME_LENGTH characters)"
        }

        // License plate validation
        when {
            licensePlate.isBlank() -> errors["licensePlate"] = "License plate cannot be empty"
            licensePlate.length > MAX_LICENSE_PLATE_LENGTH ->
                errors["licensePlate"] = "License plate too long (max $MAX_LICENSE_PLATE_LENGTH characters)"
        }

        // Odometer validation
        val currentOdo = currentOdometer.toDoubleOrNull()
        val initialOdo = initialOdometer.toDoubleOrNull()

        when {
            currentOdo == null -> errors["currentOdometer"] = "Current odometer must be a valid number"
            currentOdo < 0 -> errors["currentOdometer"] = "Current odometer cannot be negative"
            currentOdo > MAX_ODOMETER_VALUE -> errors["currentOdometer"] = "Current odometer value seems unrealistic"
        }

        when {
            initialOdo == null -> errors["initialOdometer"] = "Initial odometer must be a valid number"
            initialOdo < 0 -> errors["initialOdometer"] = "Initial odometer cannot be negative"
            initialOdo > MAX_ODOMETER_VALUE -> errors["initialOdometer"] = "Initial odometer value seems unrealistic"
        }

        // Cross-field validation
        if (currentOdo != null && initialOdo != null && currentOdo < initialOdo) {
            errors["currentOdometer"] = "Current odometer (${currentOdo.toInt()}km) cannot be less than initial odometer (${initialOdo.toInt()}km)"
        }

        return if (errors.isEmpty()) {
            ValidationResult.success()
        } else {
            ValidationResult.failure(errors)
        }
    }

    fun validateCarName(name: String): String? {
        return when {
            name.isBlank() -> "Car name cannot be empty"
            name.trim().length < 2 -> "Car name must be at least 2 characters"
            name.length > MAX_NAME_LENGTH -> "Car name too long (max $MAX_NAME_LENGTH characters)"
            else -> null
        }
    }

    fun validateLicensePlate(licensePlate: String): String? {
        return when {
            licensePlate.isBlank() -> "License plate cannot be empty"
            licensePlate.length > MAX_LICENSE_PLATE_LENGTH ->
                "License plate too long (max $MAX_LICENSE_PLATE_LENGTH characters)"
            else -> null
        }
    }

    fun validateOdometer(odometer: String, fieldName: String = "Odometer"): String? {
        val value = odometer.toDoubleOrNull()
        return when {
            value == null -> "$fieldName must be a valid number"
            value < 0 -> "$fieldName cannot be negative"
            value > MAX_ODOMETER_VALUE -> "$fieldName value seems unrealistic"
            else -> null
        }
    }
}

