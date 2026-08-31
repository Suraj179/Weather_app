package com.example.weatherapp.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.weatherapp.model.WeatherRecord

/**
 * SQLiteOpenHelper wrapper providing simple CRUD operations for saved weather
 * records. Each record is tied to the Firebase UID of the user who saved it,
 * so different accounts on the same device only see their own records.
 */
class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "weather_app.db"
        private const val DATABASE_VERSION = 1

        const val TABLE_NAME = "weather_records"
        const val COL_ID = "id"
        const val COL_USER_ID = "user_id"
        const val COL_LOCATION = "location_name"
        const val COL_TEMPERATURE = "temperature"
        const val COL_CONDITION = "condition_text"
        const val COL_HUMIDITY = "humidity"
        const val COL_WIND_SPEED = "wind_speed"
        const val COL_SAVED_AT = "saved_at"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TABLE_NAME (
                $COL_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COL_USER_ID TEXT NOT NULL,
                $COL_LOCATION TEXT NOT NULL,
                $COL_TEMPERATURE REAL NOT NULL,
                $COL_CONDITION TEXT NOT NULL,
                $COL_HUMIDITY INTEGER NOT NULL,
                $COL_WIND_SPEED REAL NOT NULL,
                $COL_SAVED_AT INTEGER NOT NULL
            )
        """.trimIndent()
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    /** Inserts a new record and returns its generated row id (-1 on failure). */
    fun insertRecord(record: WeatherRecord): Long {
        val db = writableDatabase
        val values = recordToContentValues(record)
        return db.insert(TABLE_NAME, null, values)
    }

    /** Returns all records saved by the given user, most recent first. */
    fun getRecordsForUser(userId: String): List<WeatherRecord> {
        val records = mutableListOf<WeatherRecord>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_NAME,
            null,
            "$COL_USER_ID = ?",
            arrayOf(userId),
            null, null,
            "$COL_SAVED_AT DESC"
        )
        cursor.use {
            while (it.moveToNext()) {
                records.add(cursorToRecord(it))
            }
        }
        return records
    }

    /** Updates the location name and condition text of an existing record. */
    fun updateRecord(record: WeatherRecord): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_LOCATION, record.locationName)
            put(COL_CONDITION, record.condition)
        }
        return db.update(TABLE_NAME, values, "$COL_ID = ?", arrayOf(record.id.toString()))
    }

    /** Deletes a record by id. Returns number of rows affected. */
    fun deleteRecord(id: Long): Int {
        val db = writableDatabase
        return db.delete(TABLE_NAME, "$COL_ID = ?", arrayOf(id.toString()))
    }

    private fun recordToContentValues(record: WeatherRecord): ContentValues {
        return ContentValues().apply {
            put(COL_USER_ID, record.userId)
            put(COL_LOCATION, record.locationName)
            put(COL_TEMPERATURE, record.temperature)
            put(COL_CONDITION, record.condition)
            put(COL_HUMIDITY, record.humidity)
            put(COL_WIND_SPEED, record.windSpeed)
            put(COL_SAVED_AT, record.savedAt)
        }
    }

    private fun cursorToRecord(cursor: android.database.Cursor): WeatherRecord {
        return WeatherRecord(
            id = cursor.getLong(cursor.getColumnIndexOrThrow(COL_ID)),
            userId = cursor.getString(cursor.getColumnIndexOrThrow(COL_USER_ID)),
            locationName = cursor.getString(cursor.getColumnIndexOrThrow(COL_LOCATION)),
            temperature = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_TEMPERATURE)),
            condition = cursor.getString(cursor.getColumnIndexOrThrow(COL_CONDITION)),
            humidity = cursor.getInt(cursor.getColumnIndexOrThrow(COL_HUMIDITY)),
            windSpeed = cursor.getDouble(cursor.getColumnIndexOrThrow(COL_WIND_SPEED)),
            savedAt = cursor.getLong(cursor.getColumnIndexOrThrow(COL_SAVED_AT))
        )
    }
}
