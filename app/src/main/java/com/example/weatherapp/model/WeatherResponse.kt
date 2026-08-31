package com.example.weatherapp.model

import com.google.gson.annotations.SerializedName

/**
 * Data classes matching the OpenWeatherMap "Current Weather Data" API response.
 * See: https://openweathermap.org/current
 */
data class WeatherResponse(
    @SerializedName("name") val cityName: String?,
    @SerializedName("sys") val sys: Sys?,
    @SerializedName("main") val main: Main,
    @SerializedName("weather") val weather: List<WeatherCondition>,
    @SerializedName("wind") val wind: Wind
)

data class Sys(
    @SerializedName("country") val country: String?
)

data class Main(
    @SerializedName("temp") val temp: Double,
    @SerializedName("feels_like") val feelsLike: Double,
    @SerializedName("humidity") val humidity: Int
)

data class WeatherCondition(
    @SerializedName("main") val main: String,
    @SerializedName("description") val description: String,
    @SerializedName("icon") val icon: String
)

data class Wind(
    @SerializedName("speed") val speed: Double
)
