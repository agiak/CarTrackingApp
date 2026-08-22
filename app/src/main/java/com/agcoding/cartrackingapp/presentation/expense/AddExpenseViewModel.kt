package com.agcoding.cartrackingapp.presentation.expense

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.data.local.database.dao.ExpenseCategoryDao
import com.agcoding.cartrackingapp.data.local.database.entity.ExpenseCategoryEntity
import com.agcoding.cartrackingapp.domain.model.Expense
import com.agcoding.cartrackingapp.domain.model.ExpenseCategories
import com.agcoding.cartrackingapp.domain.repository.CarRepository
import com.agcoding.cartrackingapp.domain.repository.ExpenseRepository
import com.agcoding.cartrackingapp.util.parseLocalizedDouble
import com.agcoding.cartrackingapp.util.parseLocalizedInt
import com.agcoding.cartrackingapp.util.sanitizeDecimalInput
import com.agcoding.cartrackingapp.util.sanitizeIntInput
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
    application: Application
) : AndroidViewModel(application) {

    private val _carId = MutableStateFlow(0L)

    private val _category = MutableStateFlow("")
    val category: StateFlow<String> = _category.asStateFlow()

    private val translatedCategories = ExpenseCategories.predefinedResIds.map { resId ->
        getApplication<Application>().getString(resId)
    }

    // Quick-pick categories (starred by user in Manage Categories)
    private val _quickPickCategories = MutableStateFlow<List<String>>(emptyList())
    val quickPickCategories: StateFlow<List<String>> = _quickPickCategories.asStateFlow()

    // Non-quick-pick categories for the dropdown
    private val _otherCategories = MutableStateFlow<List<String>>(emptyList())
    val otherCategories: StateFlow<List<String>> = _otherCategories.asStateFlow()

    private val _dropdownExpanded = MutableStateFlow(false)
    val dropdownExpanded: StateFlow<Boolean> = _dropdownExpanded.asStateFlow()

    init {
        loadAllCategories()
    }

    private fun loadAllCategories() {
        viewModelScope.launch {
            expenseCategoryDao.getAllCategories().collect { categoryEntities ->
                val quickPickNames = categoryEntities
                    .filter { it.isQuickPick }
                    .map { it.name }
                    .toSet()

                val customNames = categoryEntities
                    .filter { it.isCustom }
                    .map { it.name }

                val allNames = (translatedCategories + customNames).distinct().sorted()

                _quickPickCategories.value = allNames.filter { it in quickPickNames }
                _otherCategories.value = allNames.filter { it !in quickPickNames }
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

    private val _categoryError = MutableStateFlow<String?>(null)
    val categoryError: StateFlow<String?> = _categoryError.asStateFlow()

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

    /**
     * Selects a category, whether picked from the list or just typed into the
     * picker's search field.
     *
     * If the name already exists in a different casing the stored spelling wins,
     * so typing "parking" reuses "Parking" instead of creating a second category
     * that would split the same expenses in the statistics.
     */
    fun selectCategory(value: String) {
        val trimmed = value.trim()
        _category.value = existingCategoryNamed(trimmed) ?: trimmed
        _categoryError.value = null
        _dropdownExpanded.value = false
    }

    /** The stored category matching [name] ignoring case, if there is one. */
    private fun existingCategoryNamed(name: String): String? =
        (_quickPickCategories.value + _otherCategories.value)
            .firstOrNull { it.equals(name, ignoreCase = true) }

    fun toggleDropdown() {
        _dropdownExpanded.value = !_dropdownExpanded.value
    }

    fun dismissDropdown() {
        _dropdownExpanded.value = false
    }

    fun updateAmount(value: String) {
        val clean = sanitizeDecimalInput(value)
        _amount.value = clean
        val amountValue = clean.parseLocalizedDouble()
        _amountError.value = when {
            value.isBlank() -> null
            amountValue == null -> getApplication<Application>().getString(R.string.error_cost_invalid)
            amountValue < 0 -> getApplication<Application>().getString(R.string.error_amount_negative)
            amountValue == 0.0 -> getApplication<Application>().getString(R.string.error_cost_positive)
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
            _reminderDate.value = null
            _reminderMileage.value = ""
        }
    }

    fun updateReminderMileage(value: String) {
        _reminderMileage.value = sanitizeIntInput(value)
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
        val amountValue = amount.value.parseLocalizedDouble()
        val categoryValue = category.value.trim()

        if (categoryValue.isBlank()) {
            _categoryError.value = getApplication<Application>().getString(R.string.error_category_required)
            return
        }
        _categoryError.value = null

        if (amountValue == null || amountValue <= 0) {
            onError("Please enter a valid amount")
            return
        }

        viewModelScope.launch {
            try {
                _isSaving.value = true

                // Store a newly typed category, unless the name already exists in
                // another casing — in which case reuse the stored spelling.
                val existing = existingCategoryNamed(categoryValue)
                if (existing == null) {
                    expenseCategoryDao.insertCategory(
                        ExpenseCategoryEntity(name = categoryValue, isCustom = true)
                    )
                }

                val targetMileage = if (_serviceReminderEnabled.value && _reminderMileage.value.isNotBlank()) {
                    val additionalKm = _reminderMileage.value.parseLocalizedInt()
                    if (additionalKm != null && additionalKm > 0) {
                        val car = carRepository.getCarById(_carId.value).first()
                        val currentOdometer = car?.currentOdometer?.toInt() ?: 0
                        currentOdometer + additionalKm
                    } else null
                } else null

                val expense = Expense(
                    carId = _carId.value,
                    category = existing ?: categoryValue,
                    amount = amountValue,
                    timestamp = selectedDate.value,
                    notes = notes.value.ifBlank { null },
                    reminderDate = if (_serviceReminderEnabled.value) _reminderDate.value else null,
                    reminderMileage = targetMileage
                )

                expenseRepository.insertExpense(expense)
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
        _amount.value = ""
        _amountError.value = null
        _notes.value = ""
        _selectedDate.value = System.currentTimeMillis()
        _dropdownExpanded.value = false
        _serviceReminderEnabled.value = false
        _reminderDate.value = null
        _reminderMileage.value = ""
    }

    fun setCarIdAndType(carId: Long, expenseType: String) {
        _carId.value = carId
        _category.value = when (expenseType) {
            "SERVICE" -> "Service"
            "OTHER" -> ""
            else -> expenseType
        }
    }
}
