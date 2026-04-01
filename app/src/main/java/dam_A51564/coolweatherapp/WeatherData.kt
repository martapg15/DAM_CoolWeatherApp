package dam_A51564.coolweatherapp

import android.content.Context

data class WeatherData(
    var latitude: String,
    var longitude: String,
    var timezone: String,
    var current_weather: CurrentWeather,
    var hourly: Hourly,
    var daily: Daily
)

data class CurrentWeather(
    var temperature: Float,
    var windspeed: Float,
    var winddirection: Int,
    var weathercode: Int,
    var time: String
)

data class Hourly(
    var time: ArrayList<String>,
    var temperature_2m: ArrayList<Float>,
    var weathercode: ArrayList<Int>,
    var pressure_msl: ArrayList<Double>
)

data class Daily(
    var sunrise: ArrayList<String>,
    var sunset: ArrayList<String>
)

data class WeatherResource(
    val code: Int,
    val description: String,
    val image: String
)

fun getWeatherResources(context: Context): Map<Int, WeatherResource> {
    val weatherMap = HashMap<Int, WeatherResource>()
    val weatherCodes = context.resources.getStringArray(R.array.weather_codes)

    for (item in weatherCodes) {
        // Split the string "Code|Description|Image"
        val parts = item.split("|")
        if (parts.size == 3) {
            val code = parts[0].toInt()
            weatherMap[code] = WeatherResource(code, parts[1], parts[2])
        }
    }
    return weatherMap
}

// The commented section below represents the previous hardcoded Enum implementation.
// The new version retrieves weather data dynamically from XML resources (R.array.weather_codes)
// and caches it in onCreate() for better performance and resource management.
/*enum class WMOWeatherCode(var code: Int, var image: String) {
    CLEAR_SKY(0, "clear_"),
    MAINLY_CLEAR(1, "mostly_clear_"),
    PARTLY_CLOUDY(2, "partly_cloudy_"),
    OVERCAST(3, "cloudy"),
    FOG(45, "fog"),
    DEPOSITING_RIME_FOG(48, "fog"),
    DRIZZLE_LIGHT(51, "drizzle"),
    DRIZZLE_MODERATE(53, "drizzle"),
    DRIZZLE_DENSE(55, "drizzle"),
    FREEZING_DRIZZLE_LIGHT(56, "freezing_drizzle"),
    FREEZING_DRIZZLE_DENSE(57, "freezing_drizzle"),
    RAIN_SLIGHT(61, "rain_light"),
    RAIN_MODERATE(63, "rain"),
    RAIN_HEAVY(65, "rain_heavy"),
    FREEZING_RAIN_LIGHT(66, "freezing_rain_light"),
    FREEZING_RAIN_HEAVY(67, "freezing_rain_heavy"),
    SNOW_FALL_SLIGHT(71, "snow_light"),
    SNOW_FALL_MODERATE(73, "snow"),
    SNOW_FALL_HEAVY(75, "snow_heavy"),
    SNOW_GRAINS(77, "snow"),
    RAIN_SHOWERS_SLIGHT(80, "rain_light"),
    RAIN_SHOWERS_MODERATE(81, "rain"),
    RAIN_SHOWERS_VIOLENT(82, "rain_heavy"),
    SNOW_SHOWERS_SLIGHT(85, "snow_light"),
    SNOW_SHOWERS_HEAVY(86, "snow_heavy"),
    THUNDERSTORM_SLIGHT_MODERATE(95, "tstorm"),
    THUNDERSTORM_HAIL_SLIGHT(96, "tstorm"),
    THUNDERSTORM_HAIL_HEAVY(99, "tstorm")
}

fun getWeatherCodeMap(): Map<Int, WMOWeatherCode> {
    val weatherMap = HashMap<Int, WMOWeatherCode>()
    WMOWeatherCode.entries.forEach {
        weatherMap[it.code] = it
    }
    return weatherMap
}*/