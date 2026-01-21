package com.agcoding.cartrackingapp.presentation.carlist

import android.util.Log
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
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
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

    companion object {
        private const val TAG = "CarListViewModel"
    }

    private val _showAddCarDialog = MutableStateFlow(false)
    val showAddCarDialog: StateFlow<Boolean> = _showAddCarDialog.asStateFlow()

    // Get reminders info with dismissal check
    private val remindersFlow = getTodayRemindersCountUseCase()
        .flatMapLatest { reminderInfo ->
            reminderBannerPreferences.isBannerDismissed(reminderInfo.totalCount).map { isDismissed ->
                Log.d(TAG, "Banner state check: " +
                        "totalCount=${reminderInfo.totalCount}, " +
                        "dateCount=${reminderInfo.dateBasedCount}, " +
                        "mileageCount=${reminderInfo.mileageBasedCount}, " +
                        "isDismissed=$isDismissed")

                val result = if (isDismissed || reminderInfo.totalCount == 0) null else reminderInfo
                Log.d(TAG, "Banner will show: ${result != null}")
                result
            }
        }

    // Combine cars and reminders into a single UI state
    val uiState: StateFlow<CarListUiState> = getAllCarsUseCase()
        .flatMapLatest { cars ->
            remindersFlow.map { reminderInfo ->
                when {
                    cars.isEmpty() -> CarListUiState.Empty
                    else -> CarListUiState.Success(cars, reminderInfo)
                }
            }
        }
        .catch { e ->
            emit(CarListUiState.Error(e.message ?: "Unknown error"))
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            CarListUiState.Loading
        )

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
            val currentCount = (uiState.value as? CarListUiState.Success)?.reminderInfo?.totalCount ?: 0
            Log.d(TAG, "Dismissing banner with count: $currentCount")
            reminderBannerPreferences.dismissBannerForToday(currentCount)
        }
    }
}

sealed class CarListUiState {
    object Loading : CarListUiState()
    object Empty : CarListUiState()
    data class Success(val cars: List<Car>, val reminderInfo: ReminderInfo?) : CarListUiState()
    data class Error(val message: String) : CarListUiState()
}

