package com.agcoding.cartrackingapp.widget

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.agcoding.cartrackingapp.domain.model.Expense
import com.agcoding.cartrackingapp.domain.model.FuelRefill
import com.agcoding.cartrackingapp.domain.repository.CarRepository
import com.agcoding.cartrackingapp.domain.repository.ExpenseRepository
import com.agcoding.cartrackingapp.domain.repository.RefillRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QuickEntryViewModel @Inject constructor(
    private val refillRepository: RefillRepository,
    private val carRepository: CarRepository,
    private val expenseRepository: ExpenseRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private var carId: Long = -1L

    private val _carName = MutableStateFlow<String?>(null)
    val carName: StateFlow<String?> = _carName.asStateFlow()

    private val _allCars = MutableStateFlow<List<com.agcoding.cartrackingapp.domain.model.Car>>(emptyList())
    val allCars: StateFlow<List<com.agcoding.cartrackingapp.domain.model.Car>> = _allCars.asStateFlow()

    private val _selectedCar = MutableStateFlow<com.agcoding.cartrackingapp.domain.model.Car?>(null)
    val selectedCar: StateFlow<com.agcoding.cartrackingapp.domain.model.Car?> = _selectedCar.asStateFlow()

    fun setCarId(id: Long) {
        carId = id
    }

    fun loadCarName() {
        viewModelScope.launch {
            carRepository.getCarById(carId).collect { car ->
                _carName.value = car?.name
            }
        }
    }

    fun loadAllCars() {
        viewModelScope.launch {
            carRepository.getAllCars().collect { cars ->
                _allCars.value = cars
                // Auto-select if only one car or if carId was provided
                if (cars.isNotEmpty()) {
                    if (carId != -1L) {
                        val car = cars.find { it.id == carId }
                        if (car != null) {
                            selectCar(car)
                        } else if (cars.size == 1) {
                            selectCar(cars.first())
                        }
                    } else if (cars.size == 1) {
                        selectCar(cars.first())
                    }
                }
            }
        }
    }

    fun selectCar(car: com.agcoding.cartrackingapp.domain.model.Car) {
        _selectedCar.value = car
        carId = car.id
        _carName.value = car.name
    }

    fun saveQuickRefill(
        liters: Double,
        cost: Double,
        distance: Double,
        timestamp: Long,
        onSuccess: () -> Unit,
        onError: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                val refill = FuelRefill(
                    carId = carId,
                    litersAdded = liters,
                    amountPaid = cost,
                    pricePerLiter = cost / liters,
                    tripDistance = distance,
                    odometerReading = 0.0,
                    fuelConsumption = if (distance > 0) (liters / distance) * 100 else 0.0,
                    timestamp = timestamp
                )
                refillRepository.insertRefill(refill)

                onSuccess()

                // Update widgets after a short delay to ensure UI is ready
                viewModelScope.launch {
                    kotlinx.coroutines.delay(300)
                    QuickAddWidgetReceiver.updateWidgets(context)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                onError()
            }
        }
    }

    fun saveQuickExpense(
        cost: Double,
        category: String,
        notes: String?,
        timestamp: Long,
        onSuccess: () -> Unit,
        onError: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                val expense = Expense(
                    carId = carId,
                    category = category,
                    amount = cost,
                    notes = notes,
                    timestamp = timestamp
                )
                expenseRepository.insertExpense(expense)

                onSuccess()

                // Update widgets after a short delay to ensure UI is ready
                viewModelScope.launch {
                    kotlinx.coroutines.delay(300)
                    QuickAddWidgetReceiver.updateWidgets(context)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                onError()
            }
        }
    }
}

