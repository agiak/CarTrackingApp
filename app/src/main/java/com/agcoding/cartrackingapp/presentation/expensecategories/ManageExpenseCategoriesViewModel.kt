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
import timber.log.Timber
import javax.inject.Inject

data class CategoryWithQuickPick(val name: String, val isQuickPick: Boolean)

sealed class ManageExpenseCategoriesUiState {
    object Loading : ManageExpenseCategoriesUiState()
    data class Success(
        val predefinedCategories: List<CategoryWithQuickPick>,
        val customCategories: List<CategoryWithQuickPick>
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
                    val categoryMap = categories.associateBy { it.name }

                    val predefined = predefinedCategoriesResIds.map { resId ->
                        val name = context.getString(resId)
                        CategoryWithQuickPick(name, categoryMap[name]?.isQuickPick ?: false)
                    }.sortedBy { it.name }

                    val custom = categories
                        .filter { it.isCustom }
                        .map { CategoryWithQuickPick(it.name, it.isQuickPick) }
                        .sortedBy { it.name }

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

    /**
     * The row that was last deleted, kept so that undo can put it back.
     *
     * This table has no `deletedAt` — a category is there or it is not — so
     * undo means re-inserting the entity rather than clearing a mark. Holding
     * the whole row keeps its id, so the category comes back as itself rather
     * than as a new one with the same name.
     */
    private var lastDeleted: ExpenseCategoryEntity? = null

    fun deleteCategory(name: String) {
        viewModelScope.launch {
            runCatching {
                lastDeleted = expenseCategoryDao.getCategoryByName(name)
                expenseCategoryDao.deleteCategoryByName(name)
            }.onFailure { Timber.e(it, "Could not delete category %s", name) }
        }
    }

    /** Puts the last deleted category back. Does nothing if there is none. */
    fun restoreLastDeletedCategory() {
        val category = lastDeleted ?: return
        lastDeleted = null
        viewModelScope.launch {
            runCatching { expenseCategoryDao.insertCategory(category) }
                .onFailure { Timber.e(it, "Could not restore category %s", category.name) }
        }
    }

    fun toggleQuickPick(name: String, currentValue: Boolean) {
        viewModelScope.launch {
            try {
                expenseCategoryDao.setQuickPick(name, !currentValue)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}

