package com.agcoding.cartrackingapp.presentation.editreminder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agcoding.cartrackingapp.domain.repository.CarRepository
import com.agcoding.cartrackingapp.domain.repository.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditReminderViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val carRepository: CarRepository
) : ViewModel() {

    private val _expenseId = MutableStateFlow(0L)

    private val _expenseDate = MutableStateFlow<Long?>(null)
    val expenseDate: StateFlow<Long?> = _expenseDate.asStateFlow()

    private val _reminderDate = MutableStateFlow<Long?>(null)
    val reminderDate: StateFlow<Long?> = _reminderDate.asStateFlow()

    private val _reminderMileage = MutableStateFlow("")
    val reminderMileage: StateFlow<String> = _reminderMileage.asStateFlow()

    private val _currentOdometer = MutableStateFlow(0)
    val currentOdometer: StateFlow<Int> = _currentOdometer.asStateFlow()

    private val _showDatePicker = MutableStateFlow(false)
    val showDatePicker: StateFlow<Boolean> = _showDatePicker.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadReminder(expenseId: Long) {
        _expenseId.value = expenseId
        viewModelScope.launch {
            try {
                _isLoading.value = true

                expenseRepository.getExpenseById(expenseId).first()?.let { expense ->
                    // Store expense date (when it was created)
                    _expenseDate.value = expense.timestamp

                    // Load current odometer
                    val car = carRepository.getCarById(expense.carId).first()
                    _currentOdometer.value = car?.currentOdometer?.toInt() ?: 0

                    // Set reminder date
                    _reminderDate.value = expense.reminderDate

                    // Calculate remaining km from target mileage
                    expense.reminderMileage?.let { targetMileage ->
                        val remainingKm = (targetMileage - _currentOdometer.value).coerceAtLeast(0)
                        _reminderMileage.value = remainingKm.toString()
                    } ?: run {
                        _reminderMileage.value = ""
                    }
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateReminderMileage(value: String) {
        // Only allow digits
        if (value.isEmpty() || value.all { it.isDigit() }) {
            _reminderMileage.value = value
        }
    }

    fun showDatePicker() {
        _showDatePicker.value = true
    }

    fun hideDatePicker() {
        _showDatePicker.value = false
    }

    fun updateReminderDate(timestamp: Long?) {
        _reminderDate.value = timestamp
        hideDatePicker()
    }

    fun clearReminderDate() {
        _reminderDate.value = null
    }

    fun saveReminder(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                _isSaving.value = true

                expenseRepository.getExpenseById(_expenseId.value).first()?.let { expense ->
                    // Calculate new target mileage from remaining km input
                    val newTargetMileage = if (_reminderMileage.value.isNotBlank()) {
                        val additionalKm = _reminderMileage.value.toIntOrNull()
                        if (additionalKm != null && additionalKm > 0) {
                            _currentOdometer.value + additionalKm
                        } else null
                    } else null

                    // Update only reminder fields
                    val updatedExpense = expense.copy(
                        reminderDate = _reminderDate.value,
                        reminderMileage = newTargetMileage
                    )

                    expenseRepository.updateExpense(updatedExpense)
                    onSuccess()
                } ?: run {
                    onError("Expense not found")
                }
            } catch (e: Exception) {
                onError(e.message ?: "Failed to save reminder")
            } finally {
                _isSaving.value = false
            }
        }
    }
}

