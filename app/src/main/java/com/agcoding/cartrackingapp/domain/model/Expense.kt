package com.agcoding.cartrackingapp.domain.model

import androidx.annotation.StringRes
import com.agcoding.cartrackingapp.R

/**
 * Predefined expense categories for suggestions.
 * Users can also create custom categories.
 */
object ExpenseCategories {
    @StringRes
    val predefinedResIds = listOf(
        R.string.expense_category_tire_change,
        R.string.expense_category_oil_change,
        R.string.expense_category_small_service,
        R.string.expense_category_big_service,
        R.string.expense_category_repairs,
        R.string.expense_category_accessories,
        R.string.expense_category_insurance,
        R.string.expense_category_registration,
        R.string.expense_category_parking,
        R.string.expense_category_toll,
        R.string.expense_category_car_wash,
        R.string.expense_category_other
    )

    // Keep for backward compatibility with existing code
    val predefined = listOf(
        "Tire change",
        "Oil change",
        "Small service",
        "Big service",
        "Repairs",
        "Accessories",
        "Insurance",
        "Registration",
        "Parking",
        "Toll",
        "Car wash",
        "Other"
    )

    /**
     * Whether an expense category counts as servicing in the statistics
     * breakdown (fuel / service / other).
     *
     * Categories are stored as the display string that was on screen when the
     * expense was saved, so the stored value depends on the app's language at
     * that moment — "Small service", "Μικρό σέρβις", or a user-typed variant.
     * Comparing against a single hard-coded literal therefore missed almost
     * everything, and real service expenses were counted as "other".
     *
     * Matching is on a normalised form (lower-cased, accents stripped, final
     * sigma unified) and looks for the service keyword anywhere in the name, so
     * "Big service", "Μεγάλο σέρβις" and a custom "Μικρό service" all count.
     */
    fun isServiceCategory(category: String): Boolean {
        val normalized = normalizeCategory(category)
        return SERVICE_KEYWORDS.any { normalized.contains(it) }
    }

    /** Service keywords in every language the app ships, already normalised. */
    private val SERVICE_KEYWORDS = listOf(
        "service",   // English, and the Greek-Latin spelling people often type
        "servis",
        "σερβισ",    // Greek "σέρβις" after normalisation
        "συντηρη"    // Greek "συντήρηση" — used by older entries
    )

    private fun normalizeCategory(value: String): String =
        java.text.Normalizer
            .normalize(value.trim().lowercase(), java.text.Normalizer.Form.NFD)
            .replace(Regex("\\p{M}+"), "")   // drop combining accents
            .replace('ς', 'σ')               // final sigma -> sigma
}

data class Expense(
    val id: Long = 0,
    val carId: Long,
    val category: String, // Flexible user-defined category
    val amount: Double,
    val timestamp: Long,
    val notes: String? = null,
    // Service reminder fields (optional)
    val reminderDate: Long? = null, // Future date for service reminder
    val reminderMileage: Int? = null, // Mileage value for service reminder
    val reminderEnabled: Boolean = true, // Whether notifications are enabled for this reminder
    val preExpiryNotificationSent: Boolean = false, // Whether pre-expiry notification was already sent
    val reminderDismissed: Boolean = false, // Whether user dismissed the reminder alert
    val deletedAt: Long? = null
)

