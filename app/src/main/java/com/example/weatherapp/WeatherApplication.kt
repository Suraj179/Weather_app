package com.example.weatherapp

import android.app.Application
import com.google.firebase.FirebaseApp

/**
 * Simple Application subclass so Firebase is initialized once, on app startup.
 */
class WeatherApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}
