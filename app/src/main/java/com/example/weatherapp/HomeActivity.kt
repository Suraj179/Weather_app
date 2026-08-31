package com.example.weatherapp

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.text.format.DateFormat
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.weatherapp.databinding.ActivityHomeBinding
import com.example.weatherapp.db.DatabaseHelper
import com.example.weatherapp.model.WeatherRecord
import com.example.weatherapp.model.WeatherResponse
import com.example.weatherapp.network.RetrofitClient
import com.example.weatherapp.utils.LocationHelper
import com.example.weatherapp.utils.NetworkUtils
import com.example.weatherapp.utils.PrefsManager
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import java.util.Date
import kotlin.math.roundToInt

/**
 * Main screen of the app: fetches the current weather for the user's
 * location, displays it, and lets the user refresh, save, or share it.
 */
class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var prefsManager: PrefsManager
    private lateinit var locationHelper: LocationHelper

    // Keep the last successful response around so Save / Share / unit-toggle can reuse it.
    private var lastWeather: WeatherResponse? = null
    private var lastLocationLabel: String = ""

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                loadWeather()
            } else {
                showError("Location permission was denied. Weather can't be shown without your location.")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        dbHelper = DatabaseHelper(this)
        prefsManager = PrefsManager(this)
        locationHelper = LocationHelper(this)

        if (auth.currentUser == null) {
            goToLogin()
            return
        }

        binding.switchUnit.isChecked = prefsManager.useFahrenheit
        binding.switchUnit.setOnCheckedChangeListener { _, isChecked ->
            prefsManager.useFahrenheit = isChecked
            lastWeather?.let { renderWeather(it) }
        }

        binding.btnLogout.setOnClickListener { logout() }
        binding.btnSavedRecords.setOnClickListener {
            startActivity(Intent(this, SavedRecordsActivity::class.java))
        }
        binding.btnRetry.setOnClickListener { checkPermissionAndLoad() }
        binding.swipeRefresh.setOnRefreshListener { checkPermissionAndLoad(fromSwipe = true) }
        binding.btnSave.setOnClickListener { saveCurrentRecord() }
        binding.btnShare.setOnClickListener { shareCurrentWeather() }

        checkPermissionAndLoad()
    }

    private fun checkPermissionAndLoad(fromSwipe: Boolean = false) {
        val hasPermission = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            loadWeather(fromSwipe)
        } else if (!fromSwipe) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            binding.swipeRefresh.isRefreshing = false
            showError("Location permission is required to refresh the weather.")
        }
    }

    private fun loadWeather(fromSwipe: Boolean = false) {
        if (!NetworkUtils.isInternetAvailable(this)) {
            binding.swipeRefresh.isRefreshing = false
            showError("No internet connection. Please check your network and try again.")
            return
        }

        if (!fromSwipe) setLoading(true)
        hideError()

        lifecycleScope.launch {
            try {
                val location: Location = locationHelper.getCurrentLocation()
                val apiKey = BuildConfig.WEATHER_API_KEY
                val response = RetrofitClient.weatherApiService.getCurrentWeather(
                    lat = location.latitude,
                    lon = location.longitude,
                    apiKey = apiKey
                )
                lastWeather = response
                renderWeather(response)
                showContent()
            } catch (e: Exception) {
                showError(
                    when {
                        e.message?.contains("location", ignoreCase = true) == true ->
                            "Couldn't determine your current location. Make sure location services (GPS) are turned on."
                        else -> "Failed to fetch weather data: ${e.localizedMessage ?: "unknown error"}"
                    }
                )
            } finally {
                setLoading(false)
                binding.swipeRefresh.isRefreshing = false
            }
        }
    }

    private fun renderWeather(response: WeatherResponse) {
        val useFahrenheit = prefsManager.useFahrenheit
        val tempC = response.main.temp
        val feelsLikeC = response.main.feelsLike
        val displayTemp = if (useFahrenheit) celsiusToFahrenheit(tempC) else tempC
        val displayFeelsLike = if (useFahrenheit) celsiusToFahrenheit(feelsLikeC) else feelsLikeC
        val unitSymbol = if (useFahrenheit) "\u00B0F" else "\u00B0C"

        val locationLabel = buildString {
            append(response.cityName?.ifBlank { "Unknown location" } ?: "Unknown location")
            response.sys?.country?.let { append(", ").append(it) }
        }
        lastLocationLabel = locationLabel

        binding.tvLocation.text = locationLabel
        binding.tvUpdatedAt.text = "Updated ${DateFormat.format("HH:mm", Date())}"
        binding.tvTemperature.text = "${displayTemp.roundToInt()}\u00B0"
        binding.tvCondition.text = response.weather.firstOrNull()?.description
            ?.replaceFirstChar { it.uppercase() } ?: "N/A"
        binding.tvFeelsLike.text = "Feels like ${displayFeelsLike.roundToInt()}$unitSymbol"
        binding.tvHumidity.text = "${response.main.humidity}%"
        binding.tvWind.text = "${response.wind.speed} km/h"
    }

    private fun saveCurrentRecord() {
        val weather = lastWeather
        val userId = auth.currentUser?.uid
        if (weather == null || userId == null) {
            android.widget.Toast.makeText(this, "No weather data to save yet.", android.widget.Toast.LENGTH_SHORT).show()
            return
        }

        val record = WeatherRecord(
            userId = userId,
            locationName = lastLocationLabel,
            temperature = weather.main.temp,
            condition = weather.weather.firstOrNull()?.description?.replaceFirstChar { it.uppercase() } ?: "N/A",
            humidity = weather.main.humidity,
            windSpeed = weather.wind.speed
        )

        val id = dbHelper.insertRecord(record)
        val message = if (id != -1L) "Weather record saved." else "Failed to save record."
        android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_SHORT).show()
    }

    private fun shareCurrentWeather() {
        val weather = lastWeather
        if (weather == null) {
            android.widget.Toast.makeText(this, "No weather data to share yet.", android.widget.Toast.LENGTH_SHORT).show()
            return
        }
        val condition = weather.weather.firstOrNull()?.description ?: "N/A"
        val shareText = "Current weather in $lastLocationLabel: " +
            "${weather.main.temp.roundToInt()}\u00B0C, $condition. " +
            "Humidity ${weather.main.humidity}%, Wind ${weather.wind.speed} km/h."

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
            putExtra(Intent.EXTRA_SUBJECT, "Weather Update")
        }
        startActivity(Intent.createChooser(shareIntent, "Share weather via"))
    }

    private fun logout() {
        auth.signOut()
        goToLogin()
    }

    private fun goToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }

    private fun celsiusToFahrenheit(celsius: Double): Double = celsius * 9.0 / 5.0 + 32.0

    private fun setLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        if (loading) {
            binding.layoutContent.visibility = View.GONE
            binding.layoutError.visibility = View.GONE
        }
    }

    private fun showContent() {
        binding.layoutContent.visibility = View.VISIBLE
        binding.layoutError.visibility = View.GONE
    }

    private fun showError(message: String) {
        binding.layoutError.visibility = View.VISIBLE
        binding.layoutContent.visibility = View.GONE
        binding.tvErrorMessage.text = message
    }

    private fun hideError() {
        binding.layoutError.visibility = View.GONE
    }
}
