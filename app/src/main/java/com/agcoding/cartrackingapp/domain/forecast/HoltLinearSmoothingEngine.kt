package com.agcoding.cartrackingapp.domain.forecast

import com.agcoding.cartrackingapp.domain.model.ForecastResult
import kotlin.math.abs

/**
 * Implements Holt's Linear Exponential Smoothing (Double Exponential Smoothing).
 *
 * This engine forecasts time series data with trend using two smoothing parameters:
 * - Alpha (α): Level smoothing factor
 * - Beta (β): Trend smoothing factor
 *
 * Equations:
 * - Lt = α * Yt + (1 - α) * (Lt-1 + Tt-1)  // Level
 * - Tt = β * (Lt - Lt-1) + (1 - β) * Tt-1  // Trend
 * - Forecast = Lt + Tt                      // Next period prediction
 *
 * Pure Kotlin implementation with no Android dependencies.
 * Thread-safe and unit-testable.
 */
class HoltLinearSmoothingEngine(
    private val alpha: Double = 0.4,  // Level smoothing (0 < α < 1)
    private val beta: Double = 0.2     // Trend smoothing (0 < β < 1)
) {
    init {
        require(alpha in 0.0..1.0) { "Alpha must be between 0 and 1" }
        require(beta in 0.0..1.0) { "Beta must be between 0 and 1" }
    }

    /**
     * Forecasts the next value based on historical time series data.
     *
     * @param values Historical values in chronological order
     * @return ForecastResult containing prediction, trend, and confidence
     */
    fun forecast(values: List<Double>): ForecastResult {
        val dataPointsUsed = values.size

        // Edge Case 1: Less than 3 months of data
        if (dataPointsUsed < 3) {
            return ForecastResult(
                predictedNextValue = values.lastOrNull() ?: 0.0,
                trend = 0.0,
                confidence = 0.2,
                dataPointsUsed = dataPointsUsed
            )
        }

        // Initialize level and trend
        val L0 = values.first()
        val T0 = if (values.size > 1) {
            (values[1] - values[0])
        } else {
            0.0
        }

        var Lt = L0
        var Tt = T0

        // Apply Holt's equations iteratively
        for (t in 1 until values.size) {
            val Yt = values[t]
            val prevLt = Lt

            // Update level
            Lt = alpha * Yt + (1 - alpha) * (Lt + Tt)

            // Update trend
            Tt = beta * (Lt - prevLt) + (1 - beta) * Tt
        }

        // Forecast next period
        val predictedNextValue = Lt + Tt

        // Calculate confidence based on data points and accuracy
        val confidence = calculateConfidence(values, dataPointsUsed, Lt, Tt)

        return ForecastResult(
            predictedNextValue = predictedNextValue,
            trend = Tt,
            confidence = confidence,
            dataPointsUsed = dataPointsUsed
        )
    }

    /**
     * Calculates confidence score based on data sufficiency and forecast accuracy.
     *
     * Edge Cases:
     * - Case 2: 3-5 months → confidence 0.4-0.6
     * - Case 3: 6-11 months → confidence 0.6-0.8
     * - Case 4: 12+ months → confidence based on MAE, clamped 0.5-0.95
     *
     * @param values Historical values
     * @param dataPointsUsed Number of data points
     * @param Lt Final level
     * @param Tt Final trend
     * @return Confidence score (0.0 to 1.0)
     */
    private fun calculateConfidence(
        values: List<Double>,
        dataPointsUsed: Int,
        Lt: Double,
        Tt: Double
    ): Double {
        return when {
            // Case 2: 3-5 months
            dataPointsUsed in 3..5 -> {
                0.4 + (dataPointsUsed - 3) * 0.1  // 0.4 to 0.6
            }

            // Case 3: 6-11 months
            dataPointsUsed in 6..11 -> {
                0.6 + (dataPointsUsed - 6) * 0.033  // 0.6 to ~0.8
            }

            // Case 4: 12+ months - use MAE
            dataPointsUsed >= 12 -> {
                val mae = calculateMAE(values, Lt, Tt)
                val averageValue = values.average()

                val confidence = if (averageValue > 0) {
                    1 - (mae / averageValue)
                } else {
                    0.5
                }

                // Clamp between 0.5 and 0.95
                confidence.coerceIn(0.5, 0.95)
            }

            else -> 0.2
        }
    }

    /**
     * Calculates Mean Absolute Error for the forecast.
     *
     * Performs one-step-ahead forecast for each historical point
     * and compares with actual value.
     *
     * @param values Historical values
     * @param finalLt Final level
     * @param finalTt Final trend
     * @return Mean Absolute Error
     */
    private fun calculateMAE(values: List<Double>, finalLt: Double, finalTt: Double): Double {
        if (values.size < 3) return 0.0

        var totalError = 0.0
        var Lt = values.first()
        var Tt = if (values.size > 1) values[1] - values[0] else 0.0

        // Calculate one-step-ahead forecast error
        for (t in 1 until values.size) {
            val forecast = Lt + Tt
            val actual = values[t]
            totalError += abs(forecast - actual)

            // Update for next iteration
            val prevLt = Lt
            Lt = alpha * actual + (1 - alpha) * (Lt + Tt)
            Tt = beta * (Lt - prevLt) + (1 - beta) * Tt
        }

        return totalError / (values.size - 1)
    }

    /**
     * Detects and filters extreme outliers (Case 5).
     *
     * Strategy: Remove values that deviate > 60% from rolling median.
     *
     * @param values Original values
     * @return Filtered values with outliers removed
     */
    fun filterOutliers(values: List<Double>): List<Double> {
        if (values.size < 5) return values  // Need sufficient data for median

        val rollingMedian = calculateRollingMedian(values, windowSize = 3)
        val threshold = 0.6  // 60% deviation threshold

        return values.filterIndexed { index, value ->
            val median = rollingMedian.getOrNull(index) ?: value
            if (median == 0.0) true  // Keep if median is zero
            else {
                val deviation = abs(value - median) / median
                deviation <= threshold
            }
        }
    }

    /**
     * Calculates rolling median for outlier detection.
     *
     * @param values Time series values
     * @param windowSize Window size for median calculation
     * @return List of rolling medians
     */
    private fun calculateRollingMedian(values: List<Double>, windowSize: Int): List<Double> {
        val result = mutableListOf<Double>()

        for (i in values.indices) {
            val start = maxOf(0, i - windowSize / 2)
            val end = minOf(values.size, i + windowSize / 2 + 1)
            val window = values.subList(start, end).sorted()
            val median = if (window.size % 2 == 0) {
                (window[window.size / 2 - 1] + window[window.size / 2]) / 2
            } else {
                window[window.size / 2]
            }
            result.add(median)
        }

        return result
    }
}

