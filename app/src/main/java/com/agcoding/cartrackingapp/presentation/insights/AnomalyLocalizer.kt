package com.agcoding.cartrackingapp.presentation.insights

import android.content.Context
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.domain.model.Anomaly
import com.agcoding.cartrackingapp.domain.model.AnomalyType
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Provides localized titles and descriptions for anomalies.
 * This is in the presentation layer to access Android string resources.
 */
@Singleton
class AnomalyLocalizer @Inject constructor(
    @ApplicationContext private val context: Context
) {

    /**
     * Get localized title for an anomaly.
     */
    fun getLocalizedTitle(anomaly: Anomaly): String {
        return when (anomaly.type) {
            AnomalyType.FUEL_PRICE_SPIKE -> context.getString(R.string.anomaly_title_fuel_price_spike)
            AnomalyType.CONSUMPTION_SPIKE -> context.getString(R.string.anomaly_title_consumption_spike)
            AnomalyType.MAINTENANCE_OUTLIER -> context.getString(R.string.anomaly_title_maintenance_outlier)
            AnomalyType.MONTHLY_SPENDING_INCREASE -> context.getString(R.string.anomaly_title_monthly_spending)
            AnomalyType.COST_PER_KM_DEVIATION -> context.getString(R.string.anomaly_title_cost_per_km)
        }
    }

    /**
     * Translate expense category name to Greek.
     */
    private fun translateCategory(category: String): String {
        return when (category.trim()) {
            "Service", "Συντήρηση" -> context.getString(R.string.category_service)
            "Tire Change", "Αλλαγή Ελαστικών" -> context.getString(R.string.category_tire_change)
            "Oil Change", "Αλλαγή Λαδιών" -> context.getString(R.string.category_oil_change)
            "Small Service", "Μικρό Service" -> context.getString(R.string.category_small_service)
            "Big Service", "Μεγάλο Service" -> context.getString(R.string.category_big_service)
            "Repairs", "Επισκευές" -> context.getString(R.string.category_repairs)
            "Insurance", "Ασφάλεια" -> context.getString(R.string.category_insurance)
            "Tax", "Τέλη Κυκλοφορίας" -> context.getString(R.string.category_tax)
            "Parking", "Πάρκινγκ" -> context.getString(R.string.category_parking)
            "Toll", "Διόδια" -> context.getString(R.string.category_toll)
            "Car Wash", "Πλύσιμο" -> context.getString(R.string.category_car_wash)
            "Other", "Άλλο" -> context.getString(R.string.category_other)
            else -> category // Return as-is if not found
        }
    }

    /**
     * Translate month name from English to Greek.
     */
    private fun translateMonth(monthName: String): String {
        return when (monthName.uppercase()) {
            "JANUARY" -> "Ιανουάριο"
            "FEBRUARY" -> "Φεβρουάριο"
            "MARCH" -> "Μάρτιο"
            "APRIL" -> "Απρίλιο"
            "MAY" -> "Μάιο"
            "JUNE" -> "Ιούνιο"
            "JULY" -> "Ιούλιο"
            "AUGUST" -> "Αύγουστο"
            "SEPTEMBER" -> "Σεπτέμβριο"
            "OCTOBER" -> "Οκτώβριο"
            "NOVEMBER" -> "Νοέμβριο"
            "DECEMBER" -> "Δεκέμβριο"
            else -> monthName
        }
    }

    /**
     * Get localized description for an anomaly.
     * Extracts values from the original English description and formats them.
     */
    fun getLocalizedDescription(anomaly: Anomaly): String {
        return when (anomaly.type) {
            AnomalyType.FUEL_PRICE_SPIKE -> {
                // Extract percentage and values from original description
                // Handle both . and , as decimal separators
                val percentageMatch = Regex("increased by ([\\d.,]+)%").find(anomaly.description)
                val avgMatch = Regex("€([\\d.,]+)/L\\)").find(anomaly.description)
                val currentMatch = Regex("Current: €([\\d.,]+)/L").find(anomaly.description)

                val percentage = percentageMatch?.groupValues?.get(1)?.replace(",", ".") ?: "?"
                val avg = avgMatch?.groupValues?.get(1)?.replace(",", ".") ?: "?"
                val current = currentMatch?.groupValues?.get(1)?.replace(",", ".") ?: "?"

                context.getString(
                    R.string.anomaly_desc_fuel_price_spike,
                    percentage,
                    avg,
                    current
                )
            }

            AnomalyType.CONSUMPTION_SPIKE -> {
                val percentageMatch = Regex("(increased|decreased) by ([\\d.,]+)%").find(anomaly.description)
                val avgMatch = Regex("\\(([\\d.,]+) L/100km\\)").find(anomaly.description)
                val currentMatch = Regex("Current: ([\\d.,]+) L/100km").find(anomaly.description)

                val direction = percentageMatch?.groupValues?.get(1) ?: "increased"
                val percentage = percentageMatch?.groupValues?.get(2)?.replace(",", ".") ?: "?"
                val avg = avgMatch?.groupValues?.get(1)?.replace(",", ".") ?: "?"
                val current = currentMatch?.groupValues?.get(1)?.replace(",", ".") ?: "?"

                val directionStr = if (direction == "increased") {
                    context.getString(R.string.increased)
                } else {
                    context.getString(R.string.decreased)
                }

                context.getString(
                    R.string.anomaly_desc_consumption_spike,
                    directionStr,
                    percentage,
                    avg,
                    current
                )
            }

            AnomalyType.MAINTENANCE_OUTLIER -> {
                val categoryMatch = Regex("^([^(]+)\\s+expense").find(anomaly.description)
                val costMatch = Regex("€([\\d.,]+)\\)").find(anomaly.description)
                val percentageMatch = Regex("is ([\\d.,]+)%").find(anomaly.description)
                val avgMatch = Regex("average \\(€([\\d.,]+)\\)").find(anomaly.description)

                val categoryRaw = categoryMatch?.groupValues?.get(1)?.trim() ?: "Maintenance"
                val category = translateCategory(categoryRaw)
                val cost = costMatch?.groupValues?.get(1)?.replace(",", ".") ?: "?"
                val percentage = percentageMatch?.groupValues?.get(1)?.replace(",", ".") ?: "?"
                val avg = avgMatch?.groupValues?.get(1)?.replace(",", ".") ?: "?"

                context.getString(
                    R.string.anomaly_desc_maintenance_outlier,
                    category,
                    cost,
                    percentage,
                    avg
                )
            }

            AnomalyType.MONTHLY_SPENDING_INCREASE -> {
                val monthMatch = Regex("in ([A-Za-z]+) (\\d+)").find(anomaly.description)
                val costMatch = Regex("€([\\d.,]+)\\)").find(anomaly.description)
                val percentageMatch = Regex("is ([\\d.,]+)%").find(anomaly.description)
                val avgMatch = Regex("average \\(€([\\d.,]+)\\)").find(anomaly.description)

                val monthRaw = monthMatch?.groupValues?.get(1) ?: "?"
                val month = translateMonth(monthRaw)
                val year = monthMatch?.groupValues?.get(2) ?: "?"
                val cost = costMatch?.groupValues?.get(1)?.replace(",", ".") ?: "?"
                val percentage = percentageMatch?.groupValues?.get(1)?.replace(",", ".") ?: "?"
                val avg = avgMatch?.groupValues?.get(1)?.replace(",", ".") ?: "?"

                context.getString(
                    R.string.anomaly_desc_monthly_spending,
                    month,
                    year,
                    cost,
                    percentage,
                    avg
                )
            }

            AnomalyType.COST_PER_KM_DEVIATION -> {
                val directionMatch = Regex("(increased|decreased) by").find(anomaly.description)
                val percentageMatch = Regex("by ([\\d.,]+)%").find(anomaly.description)
                val currentMatch = Regex("Current: €([\\d.,]+)/km").find(anomaly.description)
                val avgMatch = Regex("avg: €([\\d.,]+)/km").find(anomaly.description)
                val distanceMatch = Regex("\\(([\\d.,]+) km").find(anomaly.description)

                val direction = directionMatch?.groupValues?.get(1) ?: "increased"
                val percentage = percentageMatch?.groupValues?.get(1)?.replace(",", ".") ?: "?"
                val current = currentMatch?.groupValues?.get(1)?.replace(",", ".") ?: "?"
                val avg = avgMatch?.groupValues?.get(1)?.replace(",", ".") ?: "?"
                val distance = distanceMatch?.groupValues?.get(1)?.replace(",", ".") ?: "?"

                val directionStr = if (direction == "increased") {
                    context.getString(R.string.increased)
                } else {
                    context.getString(R.string.decreased)
                }

                // Extract month/year if present
                val monthMatch = Regex("in ([A-Za-z]+) (\\d+)").find(anomaly.description)
                val monthRaw = monthMatch?.groupValues?.get(1) ?: ""
                val month = if (monthRaw.isNotEmpty()) translateMonth(monthRaw) else ""
                val year = monthMatch?.groupValues?.get(2) ?: ""

                if (month.isNotEmpty() && year.isNotEmpty()) {
                    context.getString(
                        R.string.anomaly_desc_cost_per_km_with_month,
                        month,
                        year,
                        directionStr,
                        percentage,
                        current,
                        avg,
                        distance
                    )
                } else {
                    context.getString(
                        R.string.anomaly_desc_cost_per_km,
                        directionStr,
                        percentage,
                        current,
                        avg,
                        distance
                    )
                }
            }
        }
    }
}

