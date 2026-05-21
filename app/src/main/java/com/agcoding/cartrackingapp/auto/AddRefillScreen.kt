package com.agcoding.cartrackingapp.auto

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.MessageTemplate
import androidx.car.app.model.Template
import com.agcoding.cartrackingapp.R
import com.agcoding.cartrackingapp.widget.QuickEntryActivity

/**
 * Android Auto screen that lets the user add a new fuel refill for a specific car.
 *
 * ## How it works
 *
 * Android Auto's safety guidelines forbid complex form UI on the car display (no keyboards,
 * no numeric pads with more than a few taps, etc.). Therefore we present a [MessageTemplate]
 * with two options:
 *
 * 1. **Voice Input** — launches [QuickEntryActivity] on the *phone* screen in voice mode
 *    (`ACTION_VOICE`). The user speaks the refill details ("40 liters, 60 euros, 350 km")
 *    and the existing voice parsing pipeline fills the form automatically.
 *
 * 2. **Manual Input** — launches [QuickEntryActivity] on the *phone* screen in normal refill
 *    mode (`ACTION_ADD_REFILL`). The user fills in the form on their phone while parked.
 *
 * Both paths reuse the existing [QuickEntryActivity] so there is zero duplication of business
 * logic — Android Auto just acts as a launcher.
 *
 * > **Note:** Launching a phone-side activity from an Auto screen is an explicitly permitted
 * > pattern called a "phone task" in the Car App Library documentation.
 *
 * @param carId   Database ID of the car the refill will be recorded against.
 * @param carName Human-readable name shown in the message header.
 */
class AddRefillScreen(
    carContext: CarContext,
    private val carId: Long,
    private val carName: String
) : Screen(carContext) {

    override fun onGetTemplate(): Template {
        val voiceAction = Action.Builder()
            .setTitle(carContext.getString(R.string.auto_voice_input))
            .setOnClickListener {
                // Launch the quick-entry overlay on the phone with voice mode active.
                // The carId pre-selects the right car so the user doesn't have to pick it.
                val intent = QuickEntryActivity.createVoiceIntent(carContext, carId)
                carContext.startActivity(intent)
                // Return to previous screen after launching — the user will interact on phone.
                screenManager.pop()
            }
            .build()

        val manualAction = Action.Builder()
            .setTitle(carContext.getString(R.string.auto_manual_input))
            .setOnClickListener {
                // Launch the standard quick-entry dialog on the phone.
                val intent = QuickEntryActivity.createRefillIntent(carContext, carId)
                carContext.startActivity(intent)
                screenManager.pop()
            }
            .build()

        return MessageTemplate.Builder(
            carContext.getString(R.string.auto_add_refill_message, carName)
        )
            .setTitle(carContext.getString(R.string.auto_add_refill))
            .setHeaderAction(Action.BACK)
            .addAction(voiceAction)
            .addAction(manualAction)
            .build()
    }
}

