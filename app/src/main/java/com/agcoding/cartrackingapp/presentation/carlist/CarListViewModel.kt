package com.agcoding.cartrackingapp.presentation.carlist

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agcoding.cartrackingapp.data.preferences.PermissionBannerPreferences
import com.agcoding.cartrackingapp.data.preferences.ReminderBannerPreferences
import com.agcoding.cartrackingapp.domain.usecase.car.AddCarUseCase
import com.agcoding.cartrackingapp.domain.usecase.car.GetAllCarsUseCase
import com.agcoding.cartrackingapp.domain.usecase.expense.GetTodayRemindersCountUseCase
import com.agcoding.cartrackingapp.shared.domain.result.Result
import com.agcoding.cartrackingapp.widget.QuickAddWidgetReceiver
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
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
    private val reminderBannerPreferences: ReminderBannerPreferences,
    private val permissionBannerPreferences: PermissionBannerPreferences,
    @ApplicationContext private val context: Context,
) : ViewModel() {

    private val _showAddCarDialog = MutableStateFlow(false)
    val showAddCarDialog: StateFlow<Boolean> = _showAddCarDialog.asStateFlow()

    private val _allPermissionsGranted = MutableStateFlow(checkAllPermissionsGranted())

    val showPermissionBanner: StateFlow<Boolean> =
        combine(_allPermissionsGranted, permissionBannerPreferences.isBannerDismissed) { granted, dismissed ->
            !granted && !dismissed
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // Get reminders info with dismissal check
    private val remindersFlow = getTodayRemindersCountUseCase()
        .flatMapLatest { reminderInfo ->
            reminderBannerPreferences.isBannerDismissed(reminderInfo.totalCount).map { isDismissed ->
                if (isDismissed || reminderInfo.totalCount == 0) null else reminderInfo
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

    fun refreshPermissionState() {
        _allPermissionsGranted.value = checkAllPermissionsGranted()
    }

    fun showAddCarDialog() {
        _showAddCarDialog.value = true
    }

    fun hideAddCarDialog() {
        _showAddCarDialog.value = false
    }

    fun addCar(name: String, licensePlate: String, odometer: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            val odometerValue = odometer.toDoubleOrNull() ?: 0.0
            when (addCarUseCase(name = name, licensePlate = licensePlate, currentOdometer = odometerValue)) {
                is Result.Success -> {
                    QuickAddWidgetReceiver.updateWidgets(context)
                    hideAddCarDialog()
                    onSuccess()
                }
                is Result.Error -> Unit
            }
        }
    }

    fun dismissBannerForToday() {
        viewModelScope.launch {
            val currentCount = (uiState.value as? CarListUiState.Success)?.reminderInfo?.totalCount ?: 0
            reminderBannerPreferences.dismissBannerForToday(currentCount)
        }
    }

    fun dismissPermissionBanner() {
        viewModelScope.launch {
            permissionBannerPreferences.dismissBanner()
        }
    }

    private fun checkAllPermissionsGranted(): Boolean {
        val permissions = buildList {
            add(Manifest.permission.ACCESS_FINE_LOCATION)
            add(Manifest.permission.RECORD_AUDIO)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        return permissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }
}
