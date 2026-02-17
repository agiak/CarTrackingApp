package com.agcoding.cartrackingapp.domain.usecase.insights

import com.agcoding.cartrackingapp.domain.model.Anomaly
import com.agcoding.cartrackingapp.domain.repository.ExpenseRepository
import com.agcoding.cartrackingapp.domain.repository.RefillRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Aggregates all anomaly detectors and returns complete list of detected anomalies.
 *
 * This is the main entry point for the Insights feature.
 * Combines results from all detection use cases.
 *
 * Anomalies are computed dynamically from transaction data and NOT stored.
 */
class GetAllAnomaliesUseCase @Inject constructor(
    private val refillRepository: RefillRepository,
    private val expenseRepository: ExpenseRepository,
    private val detectFuelAnomaliesUseCase: DetectFuelAnomaliesUseCase,
    private val detectConsumptionAnomaliesUseCase: DetectConsumptionAnomaliesUseCase,
    private val detectMaintenanceAnomaliesUseCase: DetectMaintenanceAnomaliesUseCase,
    private val detectMonthlySpendingAnomaliesUseCase: DetectMonthlySpendingAnomaliesUseCase,
    private val detectCostPerKmAnomaliesUseCase: DetectCostPerKmAnomaliesUseCase
) {

    /**
     * Detects all anomalies across all cars.
     *
     * @param carId Optional car ID to filter anomalies for specific car.
     *              If null, returns anomalies for all cars.
     * @return List of all detected anomalies, sorted by detection date (newest first)
     */
    suspend operator fun invoke(carId: Long? = null): List<Anomaly> {
        // Fetch all refills and expenses
        val allRefills = refillRepository.getAllRefills().first()
        val allExpenses = expenseRepository.getAllExpenses().first()

        // Filter by car if specified
        val refills = if (carId != null) {
            allRefills.filter { it.carId == carId }
        } else {
            allRefills
        }

        val expenses = if (carId != null) {
            allExpenses.filter { it.carId == carId }
        } else {
            allExpenses
        }

        // Run all detectors
        val anomalies = mutableListOf<Anomaly>()

        // 1. Detect fuel price anomalies
        anomalies.addAll(detectFuelAnomaliesUseCase(refills))

        // 2. Detect consumption anomalies
        anomalies.addAll(detectConsumptionAnomaliesUseCase(refills))

        // 3. Detect maintenance outliers
        anomalies.addAll(detectMaintenanceAnomaliesUseCase(expenses))

        // 4. Detect monthly spending increases
        anomalies.addAll(detectMonthlySpendingAnomaliesUseCase(refills, expenses))

        // 5. Detect cost per km deviations
        anomalies.addAll(detectCostPerKmAnomaliesUseCase(refills, expenses))

        // Sort by detection date (newest first) and severity
        return anomalies
            .sortedWith(
                compareByDescending<Anomaly> { it.detectedAt }
                    .thenByDescending { it.severity }
            )
    }

    /**
     * Gets anomalies detected in the current month.
     * Used for the preview card in Stats screen.
     */
    suspend fun getCurrentMonthAnomalies(carId: Long? = null): List<Anomaly> {
        val allAnomalies = invoke(carId)
        val currentMonth = java.time.YearMonth.now()

        return allAnomalies.filter { anomaly ->
            val anomalyMonth = java.time.YearMonth.from(anomaly.detectedAt)
            anomalyMonth == currentMonth
        }
    }
}

