package com.agcoding.cartrackingapp.widget

import android.content.Context
import androidx.core.content.edit
import com.agcoding.cartrackingapp.data.local.database.CarDatabase
import com.agcoding.cartrackingapp.data.local.database.entity.CarEntity
import com.agcoding.cartrackingapp.util.formatNumber
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Everything one widget instance needs to draw itself.
 *
 * The widget is tied to a single car, and until the redesign it showed none of
 * that: the 2×2 was an app logo and three unlabelled icons, identical on every
 * home screen whichever car it was configured for. These are the fields it now
 * puts on the face of it.
 */
data class WidgetData(
    val noCarsAvailable: Boolean = false,
    val carId: Long? = null,
    val carName: String? = null,
    val licensePlate: String? = null,
    /** Refills plus expenses for this car, this calendar month. */
    val monthlyTotal: Double = 0.0,
    /** The month the total covers, as a name: "October". */
    val monthLabel: String = "",
    /** "14 Oct · 35,9 L", or null when the car has never been refilled. */
    val lastRefill: String? = null,
)

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
        suspend fun hasAnyCars(context: Context): Boolean = try {
            CarDatabase.getInstance(context).carDao().getAllCars().first().isNotEmpty()
        } catch (e: Exception) {
            Timber.e(e, "Error checking cars")
            false
        }

        /**
         * Data for the car configured on [widgetId], falling back to the last
         * car the user picked and then to the first car on file.
         */
        suspend fun getWidgetData(context: Context, widgetId: Int? = null): WidgetData =
            load(context) { cars ->
                val configured = if (widgetId != null && widgetId != 0) {
                    QuickAddWidgetConfigActivity.getWidgetCarId(context, widgetId)
                } else {
                    -1L
                }
                cars.firstOrNull { it.id == configured } ?: lastSelectedOrFirst(context, cars)
            }

        /**
         * Data for one named car, skipping the widget lookup. Used after a save,
         * when the car is already known.
         */
        suspend fun getWidgetDataForCar(context: Context, carId: Long): WidgetData =
            load(context) { cars ->
                cars.firstOrNull { it.id == carId } ?: cars.first()
            }

        /**
         * Save last selected car ID
         */
        fun saveLastSelectedCarId(context: Context, carId: Long) {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit {
                putLong(KEY_LAST_SELECTED_CAR_ID, carId)
            }
        }

        // ── internals ────────────────────────────────────────────────────────

        /**
         * The one body both public readers share. They differed only in how
         * they chose the car, and keeping two copies of the month arithmetic
         * meant fixing any mistake in it twice.
         */
        private suspend fun load(
            context: Context,
            pickCar: (List<CarEntity>) -> CarEntity,
        ): WidgetData = try {
            val database = CarDatabase.getInstance(context)
            val cars = database.carDao().getAllCars().first()

            if (cars.isEmpty()) {
                WidgetData(noCarsAvailable = true)
            } else {
                val car = pickCar(cars)
                val refills = database.fuelRefillDao().getRefillsByCarId(car.id).first()
                val expenses = database.expenseDao().getExpensesByCarId(car.id).first()
                val month = currentMonth()

                val total = refills.filter { it.timestamp in month }.sumOf { it.amountPaid } +
                    expenses.filter { it.timestamp in month }.sumOf { it.amount }

                val latest = refills.maxByOrNull { it.timestamp }

                WidgetData(
                    carId = car.id,
                    carName = car.name,
                    licensePlate = car.licensePlate,
                    monthlyTotal = total,
                    monthLabel = monthName(),
                    lastRefill = latest?.let {
                        "${dayFormat().format(Date(it.timestamp))} · ${it.litersAdded.formatNumber(1)} L"
                    },
                )
            }
        } catch (e: Exception) {
            Timber.e(e, "Error loading widget data")
            WidgetData(noCarsAvailable = true)
        }

        private fun lastSelectedOrFirst(context: Context, cars: List<CarEntity>): CarEntity {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val lastSelected = prefs.getLong(KEY_LAST_SELECTED_CAR_ID, -1L)
            return cars.firstOrNull { it.id == lastSelected } ?: cars.first()
        }

        /** This calendar month, as a range of epoch millis. */
        private fun currentMonth(): LongRange {
            val start = Calendar.getInstance().apply {
                set(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val end = (start.clone() as Calendar).apply { add(Calendar.MONTH, 1) }
            return start.timeInMillis until end.timeInMillis
        }

        private fun monthName(): String =
            SimpleDateFormat("LLLL", Locale.getDefault()).format(Date())
                .replaceFirstChar { it.titlecase(Locale.getDefault()) }

        private fun dayFormat() = SimpleDateFormat("d MMM", Locale.getDefault())
    }
}
