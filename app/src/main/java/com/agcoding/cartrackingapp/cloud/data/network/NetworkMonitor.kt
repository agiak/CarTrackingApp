package com.agcoding.cartrackingapp.cloud.data.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Whether the phone can actually reach the internet right now.
 *
 * "Connected" is not enough: a captive-portal Wi-Fi reports a connection it
 * cannot route anywhere, and a sync started on it hangs until it times out.
 * [NetworkCapabilities.NET_CAPABILITY_VALIDATED] is Android confirming it has
 * genuinely reached the internet over that network, so both it and
 * NET_CAPABILITY_INTERNET are required.
 *
 * One process-wide callback feeds a StateFlow, so every collector sees the
 * same value and the callback is registered exactly once.
 */
@Singleton
class NetworkMonitor @Inject constructor(
    @ApplicationContext context: Context,
) {
    private val connectivity =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    private val _isOnline = MutableStateFlow(currentlyOnline())
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) = refresh()
        override fun onLost(network: Network) = refresh()
        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities,
        ) = refresh()

        private fun refresh() {
            _isOnline.value = currentlyOnline()
        }
    }

    init {
        // The app is the only consumer and lives as long as the process, so
        // this callback is never unregistered.
        connectivity.registerDefaultNetworkCallback(callback)
    }

    private fun currentlyOnline(): Boolean {
        val capabilities = connectivity.getNetworkCapabilities(connectivity.activeNetwork)
            ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
}
