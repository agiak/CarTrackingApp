package com.agcoding.cartrackingapp.domain.model

/**
 * Represents parsed voice refill data
 * Simplified to only essential fields needed for refill entry
 */
data class VoiceRefillData(
    val cost: Double? = null,
    val liters: Double? = null,
    val distance: Double? = null
) {
    /**
     * Check if the parsed data is complete enough to create a refill.
     * All three fields — cost, liters AND distance — are mandatory.
     */
    fun isComplete(): Boolean {
        return cost != null && cost > 0 &&
               liters != null && liters > 0 &&
               distance != null && distance > 0
    }

    /**
     * Check if confidence is high. All three fields must be present (isComplete),
     * and we additionally sanity-check the implied price per liter — a plausible
     * value is strong evidence the numbers were mapped to the right fields.
     */
    fun isHighConfidence(): Boolean {
        if (!isComplete()) return false
        val pricePerLiter = cost!! / liters!!
        return pricePerLiter in 0.5..4.0
    }

    /**
     * Get missing fields as user-friendly list
     */
    fun getMissingFields(): List<String> {
        val missing = mutableListOf<String>()
        if (cost == null || cost <= 0) missing.add("cost")
        if (liters == null || liters <= 0) missing.add("liters")
        if (distance == null || distance <= 0) missing.add("distance")
        return missing
    }
}

/**
 * Result of voice refill parsing
 */
sealed class VoiceParsingResult {
    data class Success(val data: VoiceRefillData) : VoiceParsingResult()
    data class Error(val message: String, val transcript: String = "") : VoiceParsingResult()
    data class LowConfidence(val data: VoiceRefillData) : VoiceParsingResult()
}

