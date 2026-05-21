package com.agcoding.cartrackingapp.auto

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.ItemList
import androidx.car.app.model.ListTemplate
import androidx.car.app.model.Row
import androidx.car.app.model.Template
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.data.local.database.CarDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * The home/landing screen shown when the user opens Cariboo on Android Auto.
 *
 * Displays the list of all registered cars. Each car is shown as a tappable row that navigates
 * to [CarDetailScreen]. This is the root of the Auto navigation stack.
 *
 * UI contract (enforced by Android Auto safety rules):
 * - Maximum 6 visible items per list on the car display.
 * - No custom layouts — only Car App Library templates are allowed.
 * - All interactions must be simple taps (no complex gestures).
 */
class CaribooMainScreen(carContext: CarContext) : Screen(carContext) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val db = CarDatabase.getInstance(carContext)

    // Cars loaded from the database, stored so [onGetTemplate] can render them.
    private var cars: List<com.agcoding.cartrackingapp.data.local.database.entity.CarEntity> = emptyList()
    private var isLoading = true

    init {
        // Load cars once and invalidate to trigger a re-render when data arrives.
        scope.launch {
            db.carDao().getAllCars().collect { carList ->
                cars = carList
                isLoading = false
                invalidate() // Tell the Car App host that our template has changed.
            }
        }
    }

    override fun onGetTemplate(): Template {
        if (isLoading) {
            return ListTemplate.Builder()
                .setTitle(carContext.getString(R.string.app_name))
                .setLoading(true)
                .build()
        }

        val listBuilder = ItemList.Builder()

        if (cars.isEmpty()) {
            listBuilder.setNoItemsMessage(carContext.getString(R.string.auto_no_cars))
        } else {
            // Android Auto limits visible list items — we show up to the API limit.
            cars.take(6).forEach { car ->
                listBuilder.addItem(
                    Row.Builder()
                        .setTitle(car.name)
                        .addText(carContext.getString(R.string.auto_car_plate, car.licensePlate))
                        .setOnClickListener {
                            // Navigate to the detail screen for this car.
                            screenManager.push(CarDetailScreen(carContext, car.id, car.name))
                        }
                        .build()
                )
            }
        }

        return ListTemplate.Builder()
            .setTitle(carContext.getString(R.string.auto_home_title))
            .setHeaderAction(Action.APP_ICON)
            .setSingleList(listBuilder.build())
            .build()
    }
}



