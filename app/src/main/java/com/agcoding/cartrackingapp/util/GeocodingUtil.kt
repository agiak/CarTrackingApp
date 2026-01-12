package com.agcoding.cartrackingapp.util

import android.content.Context
import android.location.Geocoder
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

object GeocodingUtil {

    suspend fun getAddressFromLocation(
        context: Context,
        latitude: Double,
        longitude: Double
    ): String? = withContext(Dispatchers.IO) {
        try {
            val geocoder = Geocoder(context, Locale.getDefault())

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // Android 13+ API
                suspendCoroutine { continuation ->
                    geocoder.getFromLocation(latitude, longitude, 1) { addresses ->
                        val address = addresses.firstOrNull()
                        continuation.resume(formatAddress(address))
                    }
                }
            } else {
                // Legacy API for older Android versions
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                formatAddress(addresses?.firstOrNull())
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun formatAddress(address: android.location.Address?): String? {
        if (address == null) return null

        return buildString {
            // Try to get a meaningful address
            address.thoroughfare?.let { street ->
                append(street)
                address.subThoroughfare?.let { number ->
                    append(" $number")
                }
            } ?: address.featureName?.let { append(it) }

            if (isNotEmpty()) append(", ")

            address.locality?.let { city ->
                append(city)
            } ?: address.subAdminArea?.let { append(it) }
        }.takeIf { it.isNotBlank() }
            ?: address.getAddressLine(0)
            ?: "Unknown location"
    }
}

