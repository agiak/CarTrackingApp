package com.agcoding.cartrackingapp.presentation.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agcoding.cartrackingapp.domain.model.Expense
import com.agcoding.cartrackingapp.domain.model.FuelRefill
import com.agcoding.cartrackingapp.domain.repository.CarRepository
import com.agcoding.cartrackingapp.domain.repository.ExpenseRepository
import com.agcoding.cartrackingapp.domain.repository.RefillRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class MonthDetailsViewModel @Inject constructor(
    private val refillRepository: RefillRepository,
    private val expenseRepository: ExpenseRepository,
    private val carRepository: CarRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<MonthDetailsUiState>(MonthDetailsUiState.Loading)
    val uiState: StateFlow<MonthDetailsUiState> = _uiState.asStateFlow()

    fun loadMonthData(month: Int, year: Int) {
        viewModelScope.launch {
            _uiState.value = MonthDetailsUiState.Loading

            try {
                // Get all data
                val allRefills = refillRepository.getAllRefills().first()
                val allExpenses = expenseRepository.getAllExpenses().first()
                val allCars = carRepository.getAllCars().first()

                val calendar = Calendar.getInstance()

                // Filter refills for this month
                val monthRefills = allRefills.filter { refill ->
                    calendar.timeInMillis = refill.timestamp
                    calendar.get(Calendar.YEAR) == year && calendar.get(Calendar.MONTH) == month
                }.sortedByDescending { it.timestamp }

                // Filter expenses for this month
                val monthExpenses = allExpenses.filter { expense ->
                    calendar.timeInMillis = expense.timestamp
                    calendar.get(Calendar.YEAR) == year && calendar.get(Calendar.MONTH) == month
                }.sortedByDescending { it.timestamp }

                // Get unique car IDs from refills and expenses
                val carIds = (monthRefills.map { it.carId } + monthExpenses.map { it.carId }).distinct()

                // Create car name map
                val carNames = allCars
                    .filter { it.id in carIds }
                    .associate { it.id to it.name }

                // Calculate statistics
                val refillsCost = monthRefills.sumOf { it.amountPaid }
                val expensesCost = monthExpenses.sumOf { it.amount }
                val totalCost = refillsCost + expensesCost
                val totalLiters = monthRefills.sumOf { it.litersAdded }
                val totalDistance = monthRefills.sumOf { it.tripDistance }
                val averageConsumption = if (totalDistance > 0) {
                    (totalLiters / totalDistance) * 100.0
                } else 0.0

                _uiState.value = MonthDetailsUiState.Success(
                    month = month,
                    year = year,
                    refills = monthRefills,
                    expenses = monthExpenses,
                    carNames = carNames,
                    refillsCost = refillsCost,
                    expensesCost = expensesCost,
                    totalCost = totalCost,
                    totalLiters = totalLiters,
                    totalDistance = totalDistance,
                    averageConsumption = averageConsumption
                )
            } catch (e: Exception) {
                _uiState.value = MonthDetailsUiState.Error(e.message ?: "Failed to load month data")
            }
        }
    }
}

sealed class MonthDetailsUiState {
    object Loading : MonthDetailsUiState()
    data class Success(
        val month: Int,
        val year: Int,
        val refills: List<FuelRefill>,
        val expenses: List<Expense>,
        val carNames: Map<Long, String>,
        val refillsCost: Double,
        val expensesCost: Double,
        val totalCost: Double,
        val totalLiters: Double,
        val totalDistance: Double,
        val averageConsumption: Double
    ) : MonthDetailsUiState()
    data class Error(val message: String) : MonthDetailsUiState()
}

