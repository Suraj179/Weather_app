package com.example.weatherapp.model

/**
 * Represents a single saved weather record, persisted locally in SQLite.
 */
data class WeatherRecord(
    val id: Long = -1,
    val userId: String,
    val locationName: String,
    val temperature: Double,
    val condition: String,
    val humidity: Int,
    val windSpeed: Double,
    val savedAt: Long = System.currentTimeMillis()
)
