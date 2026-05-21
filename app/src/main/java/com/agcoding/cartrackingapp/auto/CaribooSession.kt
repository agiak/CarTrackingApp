package com.agcoding.cartrackingapp.auto

import android.content.Intent
import androidx.car.app.Screen
import androidx.car.app.Session

/**
 * Represents a single Android Auto session — i.e., one connection between the phone app and
 * the car's head unit display.
 *
 * A new [Session] is created each time the user connects. [onCreateScreen] is the entry point
 * that returns the first [Screen] the user sees.
 */
class CaribooSession : Session() {

    /**
     * Called when Android Auto requests the first screen to display.
     * We show [CaribooMainScreen] as the home screen of our Auto app.
     *
     * @param intent The intent that started this session (may carry deep-link data).
     */
    override fun onCreateScreen(intent: Intent): Screen {
        return CaribooMainScreen(carContext)
    }
}

