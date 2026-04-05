# Assignment TP2 - Cool Weather App
**Course:** Mobile Application Development (DAM)  
**Student:** Marta Garcia (nº 51564)  
**Date:** 2026-04-12  
**Repository URL:** https://github.com/martapg15/DAM_CoolWeatherApp  

---

## 1. Introduction
**CoolWeatherApp** is an Android mobile application developed in Kotlin as part of the Mobile Application Development (DAM) course. The goal of this assignment was to build a fully functional weather application while applying core Android development concepts and the MVVM architectural pattern.

The application allows users to check real-time weather conditions for any location in the world by entering geographic coordinates (latitude and longitude), or by using the device's GPS to automatically detect the current location. Weather data is retrieved from the **Open-Meteo API**, a free and open-source weather API that requires no authentication key.

## 2. System Overview
The Cool Weather Application provides users with up-to-date weather metrics based on their physical location. 

**Key Features:**

- **Real-time weather data:** Fetched from the [Open-Meteo API](https://open-meteo.com/)
- **Manual coordinate input:** Users can type any latitude and longitude
- **Automatic location detection:** Uses the device's GPS (fine location) to pre-fill coordinates
- **Dynamic weather icons:** 28+ WMO weather code icons with day and night variants
- **Dynamic backgrounds:** Background image changes based on time of day (day/night)
- **Responsive layouts:** Dedicated layouts for phones (portrait/landscape)
- **Multi-language support:** English and Portuguese translations
- **Input validation:** Latitude must be in range [−90, 90] and longitude in [−180, 180]

## 3. Architecture and Design

### 3.1 MVVM Pattern

The application follows the **Model-View-ViewModel (MVVM)** architectural pattern, which provides a clear separation of concerns and supports lifecycle-aware state management.

```
┌────────────────────────────────────────┐
│           MainActivity (View)          │
│  - Renders UI                          │
│  - Observes LiveData                   │
│  - Handles permissions & user input    │
└──────────────┬─────────────────────────┘
               │ observes / calls
       ┌───────▼────────────┐
       │  WeatherViewModel  │
       │  - Holds app state │
       │  - Validates input │
       │  - Calls Repository│
       └────────┬───────────┘
                │ requests data
   ┌────────────▼────────────────┐
   │     WeatherRepository       │
   │  - HTTP requests (OkHttp)   │
   │  - JSON parsing (Gson)      │
   └────────────┬────────────────┘
                │ deserialises
   ┌────────────▼────────────────┐
   │  WeatherData (Model)        │
   │  - Data classes             │
   │  - WMO code → icon mapping  │
   └─────────────────────────────┘
```

### 3.2 Key Design Decisions

- **LiveData** is used to expose weather state from the ViewModel to the View reactively. MutableLiveData instances are kept private and only immutable LiveData is exposed to the View, following encapsulation best practices.
- **Data Binding** (two-way) is enabled to bind ViewModel properties directly to XML layout fields, reducing boilerplate code in the Activity.
- **ViewModel** survives configuration changes (screen rotation), so weather data is not lost on device rotation.
- **Repository pattern** isolates all network logic from the ViewModel, making the code easier to test and maintain.
- **WMO weather codes** are mapped to drawable resources via an XML string array (`res/values/arrays.xml`), avoiding hardcoded values in Kotlin code.

### 3.3 Responsive UI

The app supports four layout variants to provide an optimal experience on all device types:

| Qualifier | Target |
|---|---|
| `layout/` | Portrait phone |
| `layout-land/` | Landscape phone |

Background images also adapt: a daytime and a nighttime background are used depending on the sunrise/sunset times returned by the API.

## 4. Implementation

### 4.1 Project Structure

```
app/src/main/java/dam_A51564/coolweatherapp/
├── view/
│   └── MainActivity.kt          # UI layer
├── viewmodel/
│   └── WeatherViewModel.kt      # Business logic & state
├── model/
│   └── WeatherData.kt           # Data classes & WMO mapping
└── repository/
    └── WeatherRepository.kt     # API communication
```

### 4.2 Source Files

| File | Responsibility |
|---|---|
| `MainActivity.kt` | Displays UI, requests location permission, observes ViewModel |
| `WeatherViewModel.kt` | Validates coordinates, calls repository, holds LiveData state |
| `WeatherData.kt` | Defines Kotlin data classes for Open-Meteo JSON; maps WMO codes to icons |
| `WeatherRepository.kt` | Executes HTTP GET request and deserialises JSON response using Gson |

### 4.3 API Integration

**Provider:** [Open-Meteo](https://open-meteo.com/) - free, open-source, no API key required.

**Endpoint:**
```
GET https://api.open-meteo.com/v1/forecast
```

**Query Parameters:**
```
latitude=<lat>
longitude=<lon>
current_weather=true
hourly=temperature_2m,weathercode,pressure_msl,windspeed_10m
daily=sunrise,sunset
timezone=auto
```

The JSON response is deserialised into Kotlin data classes using **Gson**. Error handling wraps the request in a try-catch block; on failure, `null` is returned and the View displays no data.

### 4.4 Weather Icon Mapping

The app maps **WMO weather interpretation codes** to vector drawable resources, with separate day and night variants where applicable:

| Code | Condition | Day Icon | Night Icon |
|---|---|---|---|
| 0 | Clear sky | `clear_day` | `clear_night` |
| 1 | Mainly clear | `mostly_clear_day` | `mostly_clear_night` |
| 2 | Partly cloudy | `partly_cloudy_day` | `partly_cloudy_night` |
| 3 | Overcast | `cloudy` | `cloudy` |
| 45 / 48 | Fog | `fog` | `fog_light` |
| 51-55 | Drizzle | `drizzle` | — |
| 61-67 | Rain | `rain`, `rain_light`, `rain_heavy` | — |
| 71-77 | Snow | `snow`, `snow_light`, `snow_heavy` | — |
| 80-86 | Showers | `rain_light`, `rain_heavy` | — |
| 95-99 | Thunderstorm | `tstorm` | — |

Day/night status is determined by comparing the current local time against the `sunrise` and `sunset` values returned by the API.

### 4.5 Dependencies

| Library | Version | Purpose |
|---|---|---|
| `androidx.activity` | 1.13.0 | Activity/ViewModel integration |
| `androidx.lifecycle:viewmodel-ktx` | 2.10.0 | ViewModel |
| `androidx.lifecycle:livedata-ktx` | 2.10.0 | LiveData |
| `androidx.appcompat` | 1.7.1 | AppCompat support |
| `androidx.constraintlayout` | 2.2.1 | Responsive layout |
| `androidx.core:core-ktx` | 1.18.0 | Kotlin Android extensions |
| `com.google.android.material` | 1.13.0 | Material Design UI components |
| `com.google.code.gson` | 2.13.2 | JSON deserialisation |
| `play-services-location` | 21.3.0 | Fused Location Provider (GPS) |
| `junit` | 4.13.2 | Unit testing |
| `androidx.test.espresso` | 3.7.0 | UI/instrumentation testing |

### 4.6 Permissions

The following permissions are declared in `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
```

`ACCESS_FINE_LOCATION` is requested at runtime (Android 6.0+). If the user denies the permission, manual coordinate entry is still available.

## 5. Testing and Validation
- **Testing Strategy:** The app was tested on both Android Studio Emulators and a physical Android device to validate performance differences (resolving heavy layout rendering lag observed in the emulator).
- **Edge Cases Addressed:** * Implemented boundary validation for Latitude (-90 to 90) and Longitude (-180 to 180) with dynamic error messages displayed on the `EditText` fields.
  - Verified thread-safety by using `postValue()` when updating `LiveData` from the background networking thread.
- **Known Limitations:** The app currently fetches the GPS location once upon startup (`onCreate`). If the user travels long distances while the app remains open, it requires a restart to fetch the new GPS location automatically.

## 6. Usage Instructions

### 6.1 Prerequisites

- Android Studio Panda 1 (2025.3.1) or later
- Android device or emulator running Android 7.0 (API 24) or higher
- Internet connection (for API calls)

### 6.2 Running the App

1. Clone the repository: `git clone https://github.com/martapg15/DAM_CoolWeatherApp`
2. Open the project in **Android Studio**.
3. Wait for Gradle to sync all dependencies.
4. Build and run the application on a physical device (recommended) or an emulator.
5. Click **Run** or press `Shift + F10`.

### 6.3 Using the App

1. **Allow location permission** when prompted, or skip to enter coordinates manually.
2. **Enter latitude and longitude** in the corresponding fields (e.g., `38.7169` and `-9.1399` for Lisbon).
3. Tap the **Update** button to fetch current weather data.
4. The app displays temperature, wind speed, wind direction, pressure, time, and a matching weather icon.
5. The background image and weather icon automatically reflect day or night conditions.

### 6.4 Changing Language

The app automatically uses the device's system language. Supported languages are **English** and **Portuguese**. To change, update the device language in system settings.

---

# Development Process

## 12. Version Control and Commit History
Version control was used iteratively to track the evolution of the Android application. The commit history reflects continuous work, separated logically by feature. Commits include the initial project setup, UI layout implementation (Portrait and Landscape XML files), integration of the Open-Meteo API with Gson parsing, the implementation of MVVM architecture with Data Binding, and final adjustments for GPS permissions and physical device testing.

## 13. Difficulties and Lessons Learned
**Difficulties encountered:**
- **Thread Management:** Understanding that network calls cannot run on the Main Thread and learning how to safely push data back to the UI using `postValue()`.
- **Hardware Simulation:** Discovering that the Android Studio emulator can struggle with heavy layouts or missing location mocks, whereas a physical device executes the code smoothly.
- **Data Binding:** Mastering the syntax for Two-Way Data Binding and writing conditional logic directly inside the XML (e.g., `viewModel.weatherData != null ? ...`).

**Lessons Learned:**
- The MVVM pattern greatly improves code maintainability and testability by keeping the View as passive as possible.
- Using an XML array resource for weather code mapping instead of hardcoded Kotlin enums makes the mapping easier to update and keeps the code cleaner.
- Using a free and open API like Open-Meteo simplifies development considerably by eliminating authentication concerns.

## 14. Future Improvements
- **Active Location Updates:** Implement a dedicated "Refresh Location" button to re-trigger the `FusedLocationProviderClient` without needing to restart the app.
- **Network Error Handling:** Add visual feedback (like a `Toast` or `Snackbar`) to inform the user if the network request fails due to lack of internet.
- **Hourly / daily forecast:** Extend the UI to show a multi-hour or multi-day forecast using the hourly/daily data already available from the Open-Meteo API.
- **Offline support:** Cache the last known weather data so the app remains useful when there is no internet connection.

## 15. AI Usage Disclosure (Mandatory)
In this part of the assignment, the use of AI tools for code generation was explicitly forbidden. Therefore, all code in this repository was implemented manually by the student.

However, AI tools were used only to assist in revising and improving the wording of the README report, as permitted by the assignment guidelines. I remain fully responsible for the accuracy and content of this documentation.
