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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Shows an overview for a specific car and the 5 most recent refills.
 *
 * From here the user can:
 * - See recent refill history (read-only, safety-compliant list).
 * - Navigate to [AddRefillScreen] to log a new refill.
 *
 * Google's distracted-driver guidelines require keeping all information concise and tappable
 * targets large. Refill rows show only the most important data points.
 *
 * @param carId   Database ID of the car to display.
 * @param carName Human-readable name shown in the header.
 */
class CarDetailScreen(
    carContext: CarContext,
    private val carId: Long,
    private val carName: String
) : Screen(carContext) {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val db = CarDatabase.getInstance(carContext)
    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    private var refills: List<com.agcoding.cartrackingapp.data.local.database.entity.FuelRefillEntity> = emptyList()
    private var isLoading = true

    init {
        scope.launch {
            db.fuelRefillDao().getRefillsByCarId(carId).collect { list ->
                refills = list
                isLoading = false
                invalidate()
            }
        }
    }

    override fun onGetTemplate(): Template {
        if (isLoading) {
            return ListTemplate.Builder()
                .setTitle(carName)
                .setLoading(true)
                .build()
        }

        val listBuilder = ItemList.Builder()

        // "Add Refill" action row at the top
        listBuilder.addItem(
            Row.Builder()
                .setTitle(carContext.getString(R.string.auto_add_refill))
                .addText(carContext.getString(R.string.auto_add_refill_subtitle))
                .setOnClickListener {
                    screenManager.push(AddRefillScreen(carContext, carId, carName))
                }
                .build()
        )

        if (refills.isEmpty()) {
            listBuilder.addItem(
                Row.Builder()
                    .setTitle(carContext.getString(R.string.auto_no_refills))
                    .build()
            )
        } else {
            // Show the 5 most recent refills (list is already ordered DESC by timestamp)
            refills.take(5).forEach { refill ->
                val date = dateFormatter.format(Date(refill.timestamp))
                val liters = String.format(Locale.getDefault(), "%.1f L", refill.litersAdded)
                val cost = String.format(Locale.getDefault(), "€%.2f", refill.amountPaid)
                listBuilder.addItem(
                    Row.Builder()
                        .setTitle(carContext.getString(R.string.auto_refill_row_title, date))
                        .addText(carContext.getString(R.string.auto_refill_row_detail, liters, cost))
                        .build()
                )
            }
        }

        return ListTemplate.Builder()
            .setTitle(carName)
            .setHeaderAction(Action.BACK)
            .setSingleList(listBuilder.build())
            .build()
    }
}

