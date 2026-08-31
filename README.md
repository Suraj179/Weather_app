# Weather App (Kotlin + XML)

A native Android weather app built with Kotlin, XML layouts, Firebase Authentication,
the Fused Location Provider, the OpenWeatherMap REST API, and a local SQLite database.

## Features implemented

1. **Register / Log in / Log out** — Firebase Authentication (email + password), in
   `LoginActivity.kt` / `RegisterActivity.kt`.
2. **Current location** — requested via the runtime permission dialog and fetched with
   the Fused Location Provider (`utils/LocationHelper.kt`).
3. **Live weather data** — fetched from the OpenWeatherMap "Current Weather" REST API
   via Retrofit (`network/`).
4. **Weather display** — location name, temperature, condition, humidity, and wind
   speed, all shown on `HomeActivity` in a Material-styled card UI.
5. **Refresh** — pull-to-refresh (`SwipeRefreshLayout`) and a manual "Try Again" button.
6. **Local persistence** — selected weather snapshots are saved to a SQLite database
   (`db/DatabaseHelper.kt`), scoped per logged-in user.
7. **RecyclerView list** — saved records are listed in `SavedRecordsActivity` with a
   `RecordAdapter` (ListAdapter + DiffUtil).
8. **View / update / delete** — tap a saved record to edit its label/note, or delete it,
   from `SavedRecordsActivity`.
9. **SharedPreferences** — the Celsius/Fahrenheit unit toggle is persisted with
   `utils/PrefsManager.kt`.
10. **Share** — the current weather summary can be shared via an implicit
    `Intent.ACTION_SEND` chooser.

Loading states, input validation, "no internet", "location permission denied", and
"API request failed" messages are all handled explicitly on `HomeActivity`.

## Project structure

```
app/src/main/java/com/example/weatherapp/
├── WeatherApplication.kt        # Initializes Firebase
├── LoginActivity.kt
├── RegisterActivity.kt
├── HomeActivity.kt              # Main weather screen
├── SavedRecordsActivity.kt      # RecyclerView list + edit/delete
├── model/
│   ├── WeatherResponse.kt       # OpenWeatherMap API response models
│   └── WeatherRecord.kt         # Local SQLite record model
├── network/
│   ├── WeatherApiService.kt     # Retrofit interface
│   └── RetrofitClient.kt
├── db/
│   └── DatabaseHelper.kt        # SQLiteOpenHelper (CRUD)
├── adapter/
│   └── RecordAdapter.kt         # RecyclerView ListAdapter
└── utils/
    ├── PrefsManager.kt          # SharedPreferences wrapper
    ├── LocationHelper.kt        # Fused Location Provider wrapper
    └── NetworkUtils.kt          # Internet connectivity check
```

Layouts live in `app/src/main/res/layout/`, one per screen, plus
`item_weather_record.xml` (RecyclerView row) and `dialog_edit_record.xml` (edit dialog).

## Setup instructions

### 1. Requirements
- Android Studio (Koala/Ladybug or newer recommended)
- JDK 17 (the project targets JVM 17, which is inside the 15–20 range requested;
  compatible with any JDK 15–20 installed as your Gradle JDK)
- Gradle 8.9 (declared in `gradle/wrapper/gradle-wrapper.properties`), Android Gradle
  Plugin 8.7.2 (required because Firebase Auth pulls in `androidx.credentials`, which
  needs compileSdk 35 and AGP 8.6+)

### 2. Firebase setup
1. Go to the [Firebase console](https://console.firebase.google.com/) and create a
   project.
2. Add an Android app with package name `com.example.weatherapp`.
3. Download the generated `google-services.json` and **replace the placeholder file**
   at `app/google-services.json` with it.
4. In the Firebase console, go to **Authentication > Sign-in method** and enable the
   **Email/Password** provider.

### 3. Weather API key
1. Create a free account at [OpenWeatherMap](https://openweathermap.org/api) and
   generate an API key (the "Current Weather Data" API, on the free tier, is enough).
2. Open `app/build.gradle.kts` and replace `YOUR_OPENWEATHERMAP_API_KEY` in the
   `buildConfigField("String", "WEATHER_API_KEY", ...)` line with your real key.
   (It's exposed to the app at compile time as `BuildConfig.WEATHER_API_KEY`.)

### 4. Build & run
1. Open the project folder in Android Studio ("Open an existing project").
2. Let Gradle sync (it will download the dependencies listed in `app/build.gradle.kts`).
3. Run on a device or emulator with Google Play services (needed for the Fused
   Location Provider) and API 24+.
4. On first launch, register a new account, grant the location permission when
   prompted, and the current weather for your location will load automatically.

## Notes on design choices

- **UI**: Material Components (`MaterialButton`, `TextInputLayout`, `CardView`,
  `SwitchMaterial`) with a custom blue-to-purple gradient background, rounded cards,
  and a clean single-column layout for readability on any screen size.
- **Navigation**: simple Activity-per-screen structure (`Login → Register`,
  `Login → Home → SavedRecords`) using explicit Intents — easy to follow for a
  small app and easy to grade.
- **Weather API**: OpenWeatherMap was chosen because it has a generous free tier,
  needs only lat/lon + API key, and returns temperature, condition, humidity and
  wind speed in a single call.
- **Local storage**: plain `SQLiteOpenHelper` (no ORM) to keep the app dependency-light
  and to directly demonstrate SQL CRUD as requested by the task.
