package com.agcoding.cartrackingapp.widget

import android.content.Context
import android.util.Log
import androidx.core.content.edit
import com.agcoding.cartrackingapp.data.local.database.CarDatabase
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Widget Data Provider
 * Provides data for the home screen widget
 */
@Singleton
class WidgetDataProvider @Inject constructor(
    @ApplicationContext context: Context
) {

    companion object {
        private const val PREFS_NAME = "widget_prefs"
        private const val KEY_LAST_SELECTED_CAR_ID = "last_selected_car_id"

        /**
         * Check if any cars exist in the database
         */
        fun hasAnyCars(context: Context): Flow<Boolean> = flow {
            try {
                Log.d("WidgetDataProvider", "Checking if any cars exist")
                val database = CarDatabase.getInstance(context)
                val carDao = database.carDao()
                val cars = carDao.getAllCars().first()
                val hasCars = cars.isNotEmpty()
                Log.d("WidgetDataProvider", "Cars found: ${cars.size}, hasCars: $hasCars")
                emit(hasCars)
            } catch (e: Exception) {
                Log.e("WidgetDataProvider", "Error checking cars: ${e.message}", e)
                emit(false)
            }
        }

        /**
         * Get the last transaction (refill or expense) from all cars
         */
        fun getLastTransaction(context: Context): Flow<LastTransaction?> = flow {
            try {
                Log.d("WidgetDataProvider", "Fetching last transaction")
                val database = CarDatabase.getInstance(context)
                val carDao = database.carDao()
                val refillDao = database.fuelRefillDao()
                val expenseDao = database.expenseDao()

                // Get all refills and expenses
                val allRefills = refillDao.getAllRefills().first()
                val allExpenses = expenseDao.getAllExpenses().first()

                Log.d("WidgetDataProvider", "Found ${allRefills.size} refills, ${allExpenses.size} expenses")

                // Find the most recent transaction
                val lastRefill = allRefills.maxByOrNull { it.timestamp }
                val lastExpense = allExpenses.maxByOrNull { it.timestamp }

                val lastTransaction = when {
                    lastRefill != null && lastExpense != null -> {
                        if (lastRefill.timestamp > lastExpense.timestamp) {
                            // Last transaction is a refill
                            val car = carDao.getCarByIdSync(lastRefill.carId)
                            LastTransaction(
                                type = "Refill",
                                amount = lastRefill.amountPaid,
                                timestamp = lastRefill.timestamp,
                                carName = car?.name ?: ""
                            )
                        } else {
                            // Last transaction is an expense
                            val car = carDao.getCarByIdSync(lastExpense.carId)
                            LastTransaction(
                                type = lastExpense.category,
                                amount = lastExpense.amount,
                                timestamp = lastExpense.timestamp,
                                carName = car?.name ?: ""
                            )
                        }
                    }
                    lastRefill != null -> {
                        val car = carDao.getCarByIdSync(lastRefill.carId)
                        LastTransaction(
                            type = "Refill",
                            amount = lastRefill.amountPaid,
                            timestamp = lastRefill.timestamp,
                            carName = car?.name ?: ""
                        )
                    }
                    lastExpense != null -> {
                        val car = carDao.getCarByIdSync(lastExpense.carId)
                        LastTransaction(
                            type = lastExpense.category,
                            amount = lastExpense.amount,
                            timestamp = lastExpense.timestamp,
                            carName = car?.name ?: ""
                        )
                    }
                    else -> null
                }

                Log.d("WidgetDataProvider", "Last transaction: $lastTransaction")
                emit(lastTransaction)
            } catch (e: Exception) {
                Log.e("WidgetDataProvider", "Error getting last transaction: ${e.message}", e)
                emit(null)
            }
        }

        /**
         * Get widget data flow for a specific widget instance
         * This is a simplified version that doesn't require DI in the widget
         */
        fun getWidgetData(context: Context, widgetId: Int? = null): Flow<WidgetData> = flow {
            try {
                val database = CarDatabase.getInstance(context)
                val carDao = database.carDao()
                val refillDao = database.fuelRefillDao()
                val expenseDao = database.expenseDao()

                // Get all cars
                val cars = carDao.getAllCars().first()

                if (cars.isEmpty()) {
                    emit(WidgetData(noCarsAvailable = true))
                    return@flow
                }

                // Get car from widget configuration or last selected
                val selectedCar = if (widgetId != null && widgetId != 0) {
                    Log.d("WidgetDataProvider", "=== Widget $widgetId Car Selection ===")
                    // Try to get car from widget-specific configuration
                    val widgetCarId = QuickAddWidgetConfigActivity.getWidgetCarId(context, widgetId)
                    Log.d("WidgetDataProvider", "Widget $widgetId config car ID: $widgetCarId")
                    Log.d("WidgetDataProvider", "Available cars: ${cars.map { "${it.name} (ID: ${it.id})" }}")

                    if (widgetCarId != -1L) {
                        val car = cars.find { it.id == widgetCarId }
                        if (car != null) {
                            Log.d("WidgetDataProvider", "✓ Found car from widget config: ${car.name} (ID: ${car.id})")
                            car
                        } else {
                            Log.w("WidgetDataProvider", "⚠ Widget config car ID $widgetCarId not found, using first car")
                            val firstCar = cars.first()
                            Log.d("WidgetDataProvider", "Using fallback car: ${firstCar.name} (ID: ${firstCar.id})")
                            firstCar
                        }
                    } else {
                        // Fallback to last selected car
                        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                        val lastSelectedCarId = prefs.getLong(KEY_LAST_SELECTED_CAR_ID, -1L)
                        Log.d("WidgetDataProvider", "No widget config found, using last selected: $lastSelectedCarId")
                        if (lastSelectedCarId != -1L) {
                            val car = cars.find { it.id == lastSelectedCarId }
                            if (car != null) {
                                Log.d("WidgetDataProvider", "✓ Found last selected car: ${car.name} (ID: ${car.id})")
                                car
                            } else {
                                Log.w("WidgetDataProvider", "⚠ Last selected car $lastSelectedCarId not found, using first car")
                                val firstCar = cars.first()
                                Log.d("WidgetDataProvider", "Using fallback car: ${firstCar.name} (ID: ${firstCar.id})")
                                firstCar
                            }
                        } else {
                            val firstCar = cars.first()
                            Log.d("WidgetDataProvider", "No last selected car, using first car: ${firstCar.name} (ID: ${firstCar.id})")
                            firstCar
                        }
                    }
                } else {
                    Log.d("WidgetDataProvider", "Widget ID is null or 0, using fallback logic")
                    // No widget ID, use last selected car
                    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    val lastSelectedCarId = prefs.getLong(KEY_LAST_SELECTED_CAR_ID, -1L)
                    if (lastSelectedCarId != -1L) {
                        cars.find { it.id == lastSelectedCarId } ?: cars.first()
                    } else {
                        cars.first()
                    }
                }

                // Calculate monthly total
                val calendar = Calendar.getInstance()
                val currentMonth = calendar.get(Calendar.MONTH)
                val currentYear = calendar.get(Calendar.YEAR)

                val startOfMonth = Calendar.getInstance().apply {
                    set(Calendar.YEAR, currentYear)
                    set(Calendar.MONTH, currentMonth)
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis

                val endOfMonth = Calendar.getInstance().apply {
                    set(Calendar.YEAR, currentYear)
                    set(Calendar.MONTH, currentMonth + 1)
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                    add(Calendar.MILLISECOND, -1)
                }.timeInMillis

                val refills = refillDao.getRefillsByCarId(selectedCar.id).first()
                val expenses = expenseDao.getExpensesByCarId(selectedCar.id).first()

                val monthlyRefillTotal = refills
                    .filter { it.timestamp in startOfMonth..endOfMonth }
                    .sumOf { it.amountPaid }

                val monthlyExpenseTotal = expenses
                    .filter { it.timestamp in startOfMonth..endOfMonth }
                    .sumOf { it.amount }

                val monthlyTotal = monthlyRefillTotal + monthlyExpenseTotal

                emit(
                    WidgetData(
                        noCarsAvailable = false,
                        selectedCarName = selectedCar.name,
                        lastSelectedCarId = selectedCar.id,
                        monthlyTotal = if (monthlyTotal > 0) monthlyTotal else null
                    )
                )
            } catch (e: Exception) {
                e.printStackTrace()
                emit(WidgetData(noCarsAvailable = true))
            }
        }

        /**
         * Get widget data for a specific car ID directly
         * This bypasses the widget ID lookup and uses the car ID directly
         */
        fun getWidgetDataForCar(context: Context, carId: Long): Flow<WidgetData> = flow {
            try {
                Log.d("WidgetDataProvider", "=== Getting data for car ID: $carId ===")

                val database = CarDatabase.getInstance(context)
                val carDao = database.carDao()
                val refillDao = database.fuelRefillDao()
                val expenseDao = database.expenseDao()

                // Get all cars
                val cars = carDao.getAllCars().first()

                if (cars.isEmpty()) {
                    Log.d("WidgetDataProvider", "No cars available")
                    emit(WidgetData(noCarsAvailable = true))
                    return@flow
                }

                // Find the specific car or fall back to first car
                val selectedCar = if (carId != -1L) {
                    val car = cars.find { it.id == carId }
                    if (car != null) {
                        Log.d("WidgetDataProvider", "✓ Found car: ${car.name} (ID: ${car.id})")
                        car
                    } else {
                        Log.w("WidgetDataProvider", "⚠ Car ID $carId not found, using first car")
                        cars.first()
                    }
                } else {
                    Log.d("WidgetDataProvider", "No car ID provided, using first car")
                    cars.first()
                }

                // Calculate monthly total
                val calendar = Calendar.getInstance()
                val currentMonth = calendar.get(Calendar.MONTH)
                val currentYear = calendar.get(Calendar.YEAR)

                val startOfMonth = Calendar.getInstance().apply {
                    set(Calendar.YEAR, currentYear)
                    set(Calendar.MONTH, currentMonth)
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis

                val endOfMonth = Calendar.getInstance().apply {
                    set(Calendar.YEAR, currentYear)
                    set(Calendar.MONTH, currentMonth + 1)
                    set(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                    add(Calendar.MILLISECOND, -1)
                }.timeInMillis

                val refills = refillDao.getRefillsByCarId(selectedCar.id).first()
                val expenses = expenseDao.getExpensesByCarId(selectedCar.id).first()

                val monthlyRefillTotal = refills
                    .filter { it.timestamp in startOfMonth..endOfMonth }
                    .sumOf { it.amountPaid }

                val monthlyExpenseTotal = expenses
                    .filter { it.timestamp in startOfMonth..endOfMonth }
                    .sumOf { it.amount }

                val monthlyTotal = monthlyRefillTotal + monthlyExpenseTotal

                Log.d("WidgetDataProvider", "Emitting data for ${selectedCar.name} (ID: ${selectedCar.id})")
                emit(
                    WidgetData(
                        noCarsAvailable = false,
                        selectedCarName = selectedCar.name,
                        lastSelectedCarId = selectedCar.id,
                        monthlyTotal = if (monthlyTotal > 0) monthlyTotal else null
                    )
                )
            } catch (e: Exception) {
                Log.e("WidgetDataProvider", "Error getting widget data for car $carId: ${e.message}")
                e.printStackTrace()
                emit(WidgetData(noCarsAvailable = true))
            }
        }

        /**
         * Save last selected car ID
         */
        fun saveLastSelectedCarId(context: Context, carId: Long) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit {
                putLong(KEY_LAST_SELECTED_CAR_ID, carId)
            }
        }
    }
}

/**
 * Data class for the last transaction info
 */
data class LastTransaction(
    val type: String,
    val amount: Double,
    val timestamp: Long,
    val carName: String
)

