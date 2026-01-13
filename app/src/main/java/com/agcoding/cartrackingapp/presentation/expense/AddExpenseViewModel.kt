package com.agcoding.cartrackingapp.presentation.expense

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.agcoding.cartrackingapp.domain.model.Expense
import com.agcoding.cartrackingapp.domain.model.ExpenseCategories
import com.agcoding.cartrackingapp.domain.repository.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddExpenseViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    application: Application
) : AndroidViewModel(application) {

    private val _carId = MutableStateFlow(0L)

    private val _category = MutableStateFlow("")
    val category: StateFlow<String> = _category.asStateFlow()

    private val _categoryExpanded = MutableStateFlow(false)
    val categoryExpanded: StateFlow<Boolean> = _categoryExpanded.asStateFlow()

    // Get translated predefined categories
    private val translatedCategories = ExpenseCategories.predefinedResIds.map { resId ->
        application.getString(resId)
    }

    // Get predefined categories plus any used custom categories
    private val _availableCategories = MutableStateFlow(translatedCategories)
    val availableCategories: StateFlow<List<String>> = _availableCategories.asStateFlow()

    private val _amount = MutableStateFlow("")
    val amount: StateFlow<String> = _amount.asStateFlow()

    private val _notes = MutableStateFlow("")
    val notes: StateFlow<String> = _notes.asStateFlow()

    private val _selectedDate = MutableStateFlow(System.currentTimeMillis())
    val selectedDate: StateFlow<Long> = _selectedDate.asStateFlow()

    private val _showDatePicker = MutableStateFlow(false)
    val showDatePicker: StateFlow<Boolean> = _showDatePicker.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    fun setCarId(carId: Long) {
        _carId.value = carId
    }

    fun updateCategory(value: String) {
        _category.value = value
    }

    fun selectCategory(value: String) {
        _category.value = value
        _categoryExpanded.value = false
    }

    fun toggleCategoryDropdown() {
        _categoryExpanded.value = !_categoryExpanded.value
    }

    fun dismissCategoryDropdown() {
        _categoryExpanded.value = false
    }

    fun updateAmount(value: String) {
        _amount.value = value
    }

    fun updateNotes(value: String) {
        _notes.value = value
    }

    fun updateDate(timestamp: Long) {
        _selectedDate.value = timestamp
    }

    fun showDatePicker() {
        _showDatePicker.value = true
    }

    fun hideDatePicker() {
        _showDatePicker.value = false
    }

    fun saveExpense(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val amountValue = amount.value.toDoubleOrNull()
        val categoryValue = category.value.trim()

        if (categoryValue.isBlank()) {
            onError("Please select or enter a category")
            return
        }

        if (amountValue == null || amountValue <= 0) {
            onError("Please enter a valid amount")
            return
        }

        viewModelScope.launch {
            try {
                _isSaving.value = true

                val expense = Expense(
                    carId = _carId.value,
                    category = categoryValue,
                    amount = amountValue,
                    timestamp = selectedDate.value,
                    notes = notes.value.ifBlank { null }
                )

                expenseRepository.insertExpense(expense)
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Failed to save expense")
            } finally {
                _isSaving.value = false
            }
        }
    }

    fun clearFields() {
        _category.value = ""
        _amount.value = ""
        _notes.value = ""
        _selectedDate.value = System.currentTimeMillis()
        _categoryExpanded.value = false
    }

    // Legacy method for backward compatibility during transition
    fun setCarIdAndType(carId: Long, expenseType: String) {
        _carId.value = carId
        // Convert old type to category for backward compatibility
        _category.value = when (expenseType) {
            "SERVICE" -> "Service"
            "OTHER" -> ""
            else -> expenseType
        }
    }
}

