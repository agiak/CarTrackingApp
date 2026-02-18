package com.agcoding.cartrackingapp.domain.model

/**
 * Result of fuel forecasting using Holt's Linear Exponential Smoothing.
 *
 * @param predictedNextValue Predicted value for next month
 * @param trend Trend component (rate of change)
 * @param confidence Confidence score (0.0 to 1.0)
 * @param dataPointsUsed Number of historical data points used
 */
data class ForecastResult(
    val predictedNextValue: Double,
    val trend: Double,
    val confidence: Double,
    val dataPointsUsed: Int
) {
    /**
     * Indicates if forecast is reliable based on data sufficiency.
     */
    val isReliable: Boolean
        get() = dataPointsUsed >= 3 && confidence >= 0.4
}

