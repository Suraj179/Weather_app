package com.example.weatherapp.utils

import android.content.Context

/**
 * Thin wrapper around SharedPreferences that stores the user's preferred
 * temperature unit (Celsius vs Fahrenheit) — requirement #9 of the spec.
 */
class PrefsManager(context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var useFahrenheit: Boolean
        get() = prefs.getBoolean(KEY_USE_FAHRENHEIT, false)
        set(value) = prefs.edit().putBoolean(KEY_USE_FAHRENHEIT, value).apply()

    companion object {
        private const val PREFS_NAME = "weather_app_prefs"
        private const val KEY_USE_FAHRENHEIT = "use_fahrenheit"
    }
}
