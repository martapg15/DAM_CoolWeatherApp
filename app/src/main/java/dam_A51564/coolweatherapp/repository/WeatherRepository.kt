package dam_A51564.coolweatherapp.repository

import com.google.gson.Gson
import dam_A51564.coolweatherapp.model.WeatherData
import java.net.URL

class WeatherRepository {
    fun getWeatherData(lat: Float, lon: Float): WeatherData? {
        return try {
            val reqString = buildString {
                append("https://api.open-meteo.com/v1/forecast?")
                append("latitude=${lat}&longitude=${lon}&")
                append("current_weather=true&")
                append("hourly=temperature_2m,weathercode,pressure_msl,windspeed_10m&")
                append("daily=sunrise,sunset&timezone=auto")
            }
            val response = URL(reqString).readText()
            Gson().fromJson(response, WeatherData::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}