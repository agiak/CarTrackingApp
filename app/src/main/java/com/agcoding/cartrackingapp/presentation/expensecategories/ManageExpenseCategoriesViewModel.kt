package com.agcoding.cartrackingapp.presentation.expensecategories

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.data.local.database.dao.ExpenseCategoryDao
import com.agcoding.cartrackingapp.data.local.database.entity.ExpenseCategoryEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ManageExpenseCategoriesUiState {
    object Loading : ManageExpenseCategoriesUiState()
    data class Success(
        val predefinedCategories: List<String>,
        val customCategories: List<String>
    ) : ManageExpenseCategoriesUiState()
    data class Error(val message: String) : ManageExpenseCategoriesUiState()
}

@HiltViewModel
class ManageExpenseCategoriesViewModel @Inject constructor(
    private val expenseCategoryDao: ExpenseCategoryDao,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow<ManageExpenseCategoriesUiState>(
        ManageExpenseCategoriesUiState.Loading
    )
    val uiState: StateFlow<ManageExpenseCategoriesUiState> = _uiState.asStateFlow()

    // Predefined categories from resources
    private val predefinedCategoriesResIds = listOf(
        R.string.expense_category_tire_change,
        R.string.expense_category_oil_change,
        R.string.expense_category_small_service,
        R.string.expense_category_big_service,
        R.string.expense_category_repairs,
        R.string.expense_category_accessories,
        R.string.expense_category_insurance,
        R.string.expense_category_registration,
        R.string.expense_category_parking,
        R.string.expense_category_toll,
        R.string.expense_category_car_wash,
        R.string.expense_category_other
    )

    init {
        loadCategories()
        initializePredefinedCategories()
    }

    private fun initializePredefinedCategories() {
        viewModelScope.launch {
            try {
                // Insert predefined categories if they don't exist
                predefinedCategoriesResIds.forEach { resId ->
                    val categoryName = context.getString(resId)
                    if (!expenseCategoryDao.categoryExists(categoryName)) {
                        expenseCategoryDao.insertCategory(
                            ExpenseCategoryEntity(
                                name = categoryName,
                                isCustom = false
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                // Silently fail - categories will be loaded from database anyway
            }
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            try {
                expenseCategoryDao.getAllCategories().collect { categories ->
                    // Always get predefined categories from string resources for current locale
                    val predefined = predefinedCategoriesResIds.map { resId ->
                        context.getString(resId)
                    }.sorted()

                    val custom = categories
                        .filter { it.isCustom }
                        .map { it.name }
                        .sorted()

                    _uiState.value = ManageExpenseCategoriesUiState.Success(
                        predefinedCategories = predefined,
                        customCategories = custom
                    )
                }
            } catch (e: Exception) {
                _uiState.value = ManageExpenseCategoriesUiState.Error(
                    e.message ?: "Failed to load categories"
                )
            }
        }
    }

    fun addCategory(name: String) {
        viewModelScope.launch {
            try {
                val trimmedName = name.trim()

                // Check if category already exists
                if (expenseCategoryDao.categoryExists(trimmedName)) {
                    // Optionally show error - for now just ignore
                    return@launch
                }

                expenseCategoryDao.insertCategory(
                    ExpenseCategoryEntity(
                        name = trimmedName,
                        isCustom = true
                    )
                )
            } catch (e: Exception) {
                // Handle error - for now just log
            }
        }
    }

    fun deleteCategory(name: String) {
        viewModelScope.launch {
            try {
                expenseCategoryDao.deleteCategoryByName(name)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}

