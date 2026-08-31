package com.example.weatherapp.utils

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Wraps the Fused Location Provider to fetch a single, up-to-date location
 * fix as a suspend function. Callers are responsible for checking/requesting
 * the ACCESS_FINE_LOCATION / ACCESS_COARSE_LOCATION runtime permission first.
 */
class LocationHelper(private val context: Context) {

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): Location = suspendCancellableCoroutine { continuation ->
        val request = CurrentLocationRequest.Builder()
            .setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY)
            .build()

        fusedLocationClient.getCurrentLocation(request, null)
            .addOnSuccessListener { location ->
                if (location != null) {
                    continuation.resume(location)
                } else {
                    continuation.resumeWithException(
                        IllegalStateException("Unable to determine current location. Make sure location services are turned on.")
                    )
                }
            }
            .addOnFailureListener { e ->
                continuation.resumeWithException(e)
            }
    }
}
