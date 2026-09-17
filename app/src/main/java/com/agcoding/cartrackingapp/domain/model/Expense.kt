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
     * Servicing covers the maintenance categories: a service proper, an oil
     * change, a tyre change and repairs. Insurance, road tax, parking, tolls,
     * car washes and accessories are not maintenance and stay in "other".
     *
     * Matching is on a normalised form (lower-cased, accents stripped, final
     * sigma unified) and looks for a keyword anywhere in the name, so "Big
     * service", "Μεγάλο σέρβις" and a custom "Μικρό service" all count.
     */
    fun isServiceCategory(category: String): Boolean {
        val normalized = normalizeCategory(category)
        return SERVICE_PATTERNS.any { it.containsMatchIn(normalized) }
    }

    /**
     * Whether a category counts towards the cost of keeping the car on the road
     * in the car comparison screen.
     *
     * Wider than [isServiceCategory]: on top of servicing it includes the
     * recurring obligations of ownership — insurance and the roadworthiness
     * test. It deliberately stays out of the fuel/service/other breakdown,
     * which is a three-way split where insurance belongs in "other".
     */
    fun isMaintenanceCategory(category: String): Boolean {
        if (isServiceCategory(category)) return true
        val normalized = normalizeCategory(category)
        return OWNERSHIP_PATTERNS.any { it.containsMatchIn(normalized) }
    }

    /**
     * Maintenance keywords in every language the app ships, matched against the
     * normalised name.
     *
     * Greek entries are stems, because the language inflects ("λάδι"/"λαδιών").
     * Short English words are anchored to word boundaries — a bare "oil"
     * substring would also match an accessory called "Spoiler".
     */
    private val SERVICE_PATTERNS = listOf(
        // Service / maintenance
        Regex("service|servis|σερβισ|συντηρη"),
        // Oil change — "Oil change" / "Αλλαγή λαδιών"
        Regex("\\boil\\b|λαδ"),
        // Tyre change — "Tire change" / "Αλλαγή ελαστικών" (and colloquial λάστιχα)
        Regex("\\bt[iy]res?\\b|ελαστικ|λαστιχ"),
        // Repairs — "Repairs" / "Επισκευές"
        Regex("\\brepair|επισκευ")
    )

    /**
     * Costs of ownership that are not servicing, used only by
     * [isMaintenanceCategory]. Kept separate so widening the comparison screen
     * can never leak into the fuel/service/other breakdown.
     */
    private val OWNERSHIP_PATTERNS = listOf(
        // Insurance — "Insurance" / "Ασφάλεια"
        Regex("insurance|ασφαλ"),
        // Roadworthiness test — "Inspection" / "ΚΤΕΟ"
        Regex("inspection|κτεο|\\bkteo\\b")
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

