package com.agcoding.cartrackingapp.presentation.expense

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.data.local.database.dao.ExpenseCategoryDao
import com.agcoding.cartrackingapp.domain.model.Expense
import com.agcoding.cartrackingapp.domain.model.ExpenseCategories
import com.agcoding.cartrackingapp.domain.repository.CarRepository
import com.agcoding.cartrackingapp.domain.repository.ExpenseRepository
import com.agcoding.cartrackingapp.domain.usecase.expense.AddExpenseUseCase
import com.agcoding.cartrackingapp.widget.QuickAddWidgetReceiver
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddExpenseViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val carRepository: CarRepository,
    private val expenseCategoryDao: ExpenseCategoryDao,
    private val addExpenseUseCase: AddExpenseUseCase,
    application: Application
) : AndroidViewModel(application) {

    private val _carId = MutableStateFlow(0L)

    private val _category = MutableStateFlow("")
    val category: StateFlow<String> = _category.asStateFlow()

    private val _showCustomCategoryField = MutableStateFlow(false)
    val showCustomCategoryField: StateFlow<Boolean> = _showCustomCategoryField.asStateFlow()

    private val _customCategoryText = MutableStateFlow("")
    val customCategoryText: StateFlow<String> = _customCategoryText.asStateFlow()

    private val _categoryExpanded = MutableStateFlow(false)
    val categoryExpanded: StateFlow<Boolean> = _categoryExpanded.asStateFlow()

    // Get translated predefined categories
    private val translatedCategories = ExpenseCategories.predefinedResIds.map { resId ->
        application.getString(resId)
    }

    // Get predefined categories plus custom categories from database
    private val _availableCategories = MutableStateFlow(translatedCategories)
    val availableCategories: StateFlow<List<String>> = _availableCategories.asStateFlow()

    init {
        loadAllCategories()
    }

    private fun loadAllCategories() {
        viewModelScope.launch {
            expenseCategoryDao.getAllCategories().collect { categoryEntities ->
                // Always use translated predefined categories (from string resources)
                // Plus custom categories from database
                val customCategories = categoryEntities
                    .filter { it.isCustom }
                    .map { it.name }

                // Combine translated predefined + custom categories
                val allCategories = (translatedCategories + customCategories)
                    .distinct()
                    .sorted()

                _availableCategories.value = allCategories
            }
        }
    }

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

    private val _amountError = MutableStateFlow<String?>(null)
    val amountError: StateFlow<String?> = _amountError.asStateFlow()

    // Service reminder fields
    private val _serviceReminderEnabled = MutableStateFlow(false)
    val serviceReminderEnabled: StateFlow<Boolean> = _serviceReminderEnabled.asStateFlow()

    private val _reminderDate = MutableStateFlow<Long?>(null)
    val reminderDate: StateFlow<Long?> = _reminderDate.asStateFlow()

    private val _reminderMileage = MutableStateFlow("")
    val reminderMileage: StateFlow<String> = _reminderMileage.asStateFlow()

    private val _showReminderDatePicker = MutableStateFlow(false)
    val showReminderDatePicker: StateFlow<Boolean> = _showReminderDatePicker.asStateFlow()

    fun setCarId(carId: Long) {
        _carId.value = carId
    }

    fun updateCategory(value: String) {
        _category.value = value
    }

    fun selectCategory(value: String) {
        _category.value = value
        _showCustomCategoryField.value = false
        _customCategoryText.value = ""
    }

    fun toggleCustomCategoryField() {
        _showCustomCategoryField.value = !_showCustomCategoryField.value
        if (_showCustomCategoryField.value) {
            // Clear the selected category when showing custom field
            _category.value = ""
        } else {
            // Clear custom text when hiding field
            _customCategoryText.value = ""
        }
    }

    fun updateCustomCategoryText(value: String) {
        _customCategoryText.value = value
        // Update the category value with the custom text
        _category.value = value
    }

    fun toggleCategoryDropdown() {
        _categoryExpanded.value = !_categoryExpanded.value
    }

    fun dismissCategoryDropdown() {
        _categoryExpanded.value = false
    }

    fun updateAmount(value: String) {
        _amount.value = value

        // Validate amount in real-time
        val amountValue = value.toDoubleOrNull()
        _amountError.value = when {
            value.isBlank() -> null // Don't show error for empty field
            amountValue == null -> application.getString(R.string.error_cost_invalid)
            amountValue < 0 -> application.getString(R.string.error_amount_negative)
            amountValue == 0.0 -> application.getString(R.string.error_cost_positive)
            else -> null
        }
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

    fun toggleServiceReminder(enabled: Boolean) {
        _serviceReminderEnabled.value = enabled
        if (!enabled) {
            // Clear reminder fields when disabled
            _reminderDate.value = null
            _reminderMileage.value = ""
        }
    }

    fun updateReminderMileage(value: String) {
        // Only allow digits
        if (value.isEmpty() || value.all { it.isDigit() }) {
            _reminderMileage.value = value
        }
    }

    fun showReminderDatePicker() {
        _showReminderDatePicker.value = true
    }

    fun hideReminderDatePicker() {
        _showReminderDatePicker.value = false
    }

    fun updateReminderDate(timestamp: Long?) {
        _reminderDate.value = timestamp
        hideReminderDatePicker()
    }

    fun clearReminderDate() {
        _reminderDate.value = null
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

                // Calculate target mileage if mileage reminder is set
                val targetMileage = if (_serviceReminderEnabled.value && _reminderMileage.value.isNotBlank()) {
                    val additionalKm = _reminderMileage.value.toIntOrNull()
                    if (additionalKm != null && additionalKm > 0) {
                        // Get current car odometer
                        val car = carRepository.getCarById(_carId.value).first()
                        val currentOdometer = car?.currentOdometer?.toInt() ?: 0

                        // Calculate target: current odometer + additional km
                        currentOdometer + additionalKm
                    } else null
                } else null

                val expense = Expense(
                    carId = _carId.value,
                    category = categoryValue,
                    amount = amountValue,
                    timestamp = selectedDate.value,
                    notes = notes.value.ifBlank { null },
                    reminderDate = if (_serviceReminderEnabled.value) _reminderDate.value else null,
                    reminderMileage = targetMileage
                )

                // Use AddExpenseUseCase for validation and widget updates
                // But insert directly here because we need to handle reminders
                // which are not part of the basic UseCase
                expenseRepository.insertExpense(expense)

                // Update widgets to show latest transaction
                QuickAddWidgetReceiver.updateWidgets(getApplication())

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
        _showCustomCategoryField.value = false
        _customCategoryText.value = ""
        _amount.value = ""
        _amountError.value = null
        _notes.value = ""
        _selectedDate.value = System.currentTimeMillis()
        _categoryExpanded.value = false
        _serviceReminderEnabled.value = false
        _reminderDate.value = null
        _reminderMileage.value = ""
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

