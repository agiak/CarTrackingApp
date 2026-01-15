package com.agcoding.cartrackingapp.domain.validation

/**
 * Represents the result of a validation operation
 */
data class ValidationResult(
    val isValid: Boolean,
    val errors: Map<String, String> = emptyMap()
) {
    fun getError(field: String): String? = errors[field]

    fun hasError(field: String): Boolean = errors.containsKey(field)

    companion object {
        fun success() = ValidationResult(isValid = true)

        fun failure(errors: Map<String, String>) = ValidationResult(
            isValid = false,
            errors = errors
        )

        fun failure(field: String, message: String) = ValidationResult(
            isValid = false,
            errors = mapOf(field to message)
        )
    }
}

