package com.agcoding.cartrackingapp.auto

import androidx.car.app.CarAppService
import androidx.car.app.Session
import androidx.car.app.validation.HostValidator

/**
 * Entry point for the Android Auto integration.
 *
 * This service is declared in AndroidManifest.xml and is discovered by the Android Auto host
 * (the car's head unit or the Android Auto phone app). When the user opens Cariboo from the
 * car's launcher, Android Auto binds to this service and calls [onCreateSession].
 *
 * The Car App Library handles all rendering on the car's screen — we only provide data and
 * respond to user interactions through [Session] and [Screen] implementations.
 *
 * Architecture overview:
 * - [CaribooCarAppService] → creates [CaribooSession]
 * - [CaribooSession] → creates [CaribooMainScreen] (home screen)
 * - [CaribooMainScreen] → navigates to [CarListScreen], [CarDetailScreen], [AddRefillScreen]
 */
class CaribooCarAppService : CarAppService() {

    /**
     * Returns the host validator. Using [HostValidator.ALLOW_ALL_HOSTS_VALIDATOR] during
     * development for convenience. In production you should restrict to known hosts by
     * providing a known-good set of host certificates.
     */
    override fun createHostValidator(): HostValidator {
        return HostValidator.ALLOW_ALL_HOSTS_VALIDATOR
    }

    /**
     * Creates a new [Session] every time the user starts a new Auto session (e.g., plugs in
     * phone, or relaunches the app). Each session corresponds to one instance of the car display.
     */
    override fun onCreateSession(): Session {
        return CaribooSession()
    }
}

