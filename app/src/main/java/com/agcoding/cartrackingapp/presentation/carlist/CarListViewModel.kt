package com.agcoding.cartrackingapp.presentation.carlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agcoding.cartrackingapp.data.preferences.ReminderBannerPreferences
import com.agcoding.cartrackingapp.domain.model.Car
import com.agcoding.cartrackingapp.domain.usecase.car.AddCarUseCase
import com.agcoding.cartrackingapp.domain.usecase.car.GetAllCarsUseCase
import com.agcoding.cartrackingapp.domain.usecase.expense.GetTodayRemindersCountUseCase
import com.agcoding.cartrackingapp.domain.usecase.expense.ReminderInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CarListViewModel @Inject constructor(
    private val getAllCarsUseCase: GetAllCarsUseCase,
    private val addCarUseCase: AddCarUseCase,
    getTodayRemindersCountUseCase: GetTodayRemindersCountUseCase,
    private val reminderBannerPreferences: ReminderBannerPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow<CarListUiState>(CarListUiState.Loading)
    val uiState: StateFlow<CarListUiState> = _uiState.asStateFlow()

    private val _showAddCarDialog = MutableStateFlow(false)
    val showAddCarDialog: StateFlow<Boolean> = _showAddCarDialog.asStateFlow()

    // Expose today's reminders info (only show banner if not dismissed today)
    val todayRemindersInfo: StateFlow<ReminderInfo?> = combine(
        getTodayRemindersCountUseCase(),
        reminderBannerPreferences.isBannerDismissedToday
    ) { reminderInfo, isDismissed ->
        if (isDismissed || reminderInfo.totalCount == 0) null else reminderInfo
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        loadCars()
    }

    private fun loadCars() {
        viewModelScope.launch {
            getAllCarsUseCase()
                .catch { e ->
                    _uiState.value = CarListUiState.Error(e.message ?: "Unknown error")
                }
                .collect { cars ->
                    _uiState.value = if (cars.isEmpty()) {
                        CarListUiState.Empty
                    } else {
                        CarListUiState.Success(cars)
                    }
                }
        }
    }

    fun showAddCarDialog() {
        _showAddCarDialog.value = true
    }

    fun hideAddCarDialog() {
        _showAddCarDialog.value = false
    }

    fun addCar(name: String, licensePlate: String, odometer: String) {
        viewModelScope.launch {
            val odometerValue = odometer.toDoubleOrNull() ?: 0.0
            addCarUseCase(
                name = name,
                licensePlate = licensePlate,
                currentOdometer = odometerValue
            ).onSuccess {
                hideAddCarDialog()
            }.onFailure { e ->
                // Handle error - could add a separate error state
            }
        }
    }

    fun dismissBannerForToday() {
        viewModelScope.launch {
            reminderBannerPreferences.dismissBannerForToday()
        }
    }
}

sealed class CarListUiState {
    object Loading : CarListUiState()
    object Empty : CarListUiState()
    data class Success(val cars: List<Car>) : CarListUiState()
    data class Error(val message: String) : CarListUiState()
}

