package dam_A51564.coolweatherapp

import android.content.res.Configuration
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.gson.Gson
import java.io.InputStreamReader
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private var day: Boolean = true

    // Keys for saving state
    private val DAY_KEY = "day"
    private val PRESSURE_KEY = "pressure"
    private val DIRECTION_KEY = "direction"
    private val SPEED_KEY = "speed"
    private val TEMP_KEY = "temperature"
    private val TIME_KEY = "time"
    private val WEATHERIMAGE_KEY = "weatherImage"

    // UI variables
    private lateinit var pressure: TextView
    private lateinit var direction: TextView
    private lateinit var speed: TextView
    private lateinit var temp: TextView
    private lateinit var time: TextView
    private lateinit var weatherImage: ImageView
    private lateinit var weatherImageName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        savedInstanceState?.let {
            day = it.getBoolean(DAY_KEY, true)
        }

        when (resources.configuration.orientation) {
            Configuration.ORIENTATION_PORTRAIT -> {
                if (day) setTheme(R.style.Theme_Day) else setTheme(R.style.Theme_Night)
            }
            Configuration.ORIENTATION_LANDSCAPE -> {
                if (day) setTheme(R.style.Theme_Day_Land) else setTheme(R.style.Theme_Night_Land)
            }
        }

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        pressure = findViewById(R.id.pressureValue)
        direction = findViewById(R.id.directionValue)
        speed = findViewById(R.id.speedValue)
        temp = findViewById(R.id.tempValue)
        time = findViewById(R.id.timeValue)
        weatherImage = findViewById(R.id.weatherImage)

        // Restore the text and images if the screen was recreated
        if (savedInstanceState != null) {
            restoreInstanceState(savedInstanceState)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.container)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val updateButton: Button = findViewById(R.id.btnUpdate)
        updateButton.setOnClickListener { onFetchWeatherButtonClick() }
    }

    private fun weatherAPICall(lat: Float, long: Float): WeatherData? {
        val reqString = buildString {
            append("https://api.open-meteo.com/v1/forecast?")
            append("latitude=${lat}&longitude=${long}&")
            append("current_weather=true&")
            append("hourly=temperature_2m,weathercode,pressure_msl,windspeed_10m&")
            append("daily=sunrise,sunset&timezone=auto")
        }
        val url = URL(reqString)
        url.openStream().use {
            val request = Gson().fromJson(InputStreamReader(it, "UTF-8"), WeatherData::class.java)
            return request
        }
    }

    private fun fetchWeatherData(lat: Float, long: Float): Thread {
        return Thread {
            val weather = weatherAPICall(lat, long)
            if (weather != null) {
                updateUI(weather)
            }
        }
    }

    private fun onFetchWeatherButtonClick() {
        val lat: EditText = findViewById(R.id.textLat)
        val long: EditText = findViewById(R.id.textLong)

        val latValue = lat.text.toString().toFloatOrNull()
        val longValue = long.text.toString().toFloatOrNull()

        if (latValue != null && longValue != null) {
            // Checks if latitude is between -90 and 90 and longitude is between -180 and 180
            if (latValue in -90.0..90.0) {
                if (longValue in -180.0..180.0) {
                    fetchWeatherData(latValue, longValue).start()
                } else {
                    long.error = "Longitude must be between -180 and 180"
                }
            } else {
                lat.error = "Latitude must be between -90 and 90"
            }
        }
    }

    private fun isDay(currentTime: String, sunrise: String, sunset: String): Boolean {
        val currentDate = SimpleDateFormat("HH:mm", Locale.getDefault())

        try {
            val currentDateTime = currentDate.parse(currentTime)
            val sunriseDateTime = currentDate.parse(sunrise)
            val sunsetDateTime = currentDate.parse(sunset)

            if (currentDateTime != null && sunriseDateTime != null && sunsetDateTime != null) {
                return currentDateTime.after(sunriseDateTime) && currentDateTime.before(sunsetDateTime)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return true
    }

    private fun updateUI(request: WeatherData) {
        runOnUiThread {
            pressure.text = buildString {
                append(request.hourly.pressure_msl[12].toString())
                append(" hPa")
            }

            direction.text = request.current_weather.winddirection.toString()

            speed.text = buildString {
                append(request.current_weather.windspeed.toString())
                append(" km/h")
            }

            temp.text = buildString {
                append(request.hourly.temperature_2m[12].toString())
                append("ºC")
            }

            time.text = request.current_weather.time

            val sunriseTime = request.daily.sunrise[0].substring(11, 16) // Extract time part
            val sunsetTime = request.daily.sunset[0].substring(11, 16) // Extract time part
            val currentTime = request.current_weather.time.substring(11, 16) // Extract time part

            val isDay = isDay(currentTime, sunriseTime, sunsetTime)

            val weatherCodeMap = getWeatherCodeMap()
            val weatherCode = weatherCodeMap[request.current_weather.weathercode]
            weatherImageName = when (weatherCode) {
                WMOWeatherCode.CLEAR_SKY,
                WMOWeatherCode.MAINLY_CLEAR,
                WMOWeatherCode.PARTLY_CLOUDY -> if (isDay) "${weatherCode.image}day" else "${weatherCode.image}night"
                else -> weatherCode?.image.toString()
            }

            val res = getResources()
            val resID = res.getIdentifier(weatherImageName, "drawable", packageName)
            val drawable = AppCompatResources.getDrawable(this, resID)
            weatherImage.setImageDrawable(drawable)

            // It will only use recreate if the day has actually changed, updating the global variable
            if (day != isDay) {
                day = isDay
                recreate()
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Save all current text values and the day status
        outState.putBoolean(DAY_KEY, day)
        outState.putString(PRESSURE_KEY, pressure.text.toString())
        outState.putString(DIRECTION_KEY, direction.text.toString())
        outState.putString(SPEED_KEY, speed.text.toString())
        outState.putString(TEMP_KEY, temp.text.toString())
        outState.putString(TIME_KEY, time.text.toString())

        if (::weatherImageName.isInitialized) {
            outState.putString(WEATHERIMAGE_KEY, weatherImageName)
        }
    }

    private fun restoreInstanceState(savedInstanceState: Bundle) {
        pressure.text = savedInstanceState.getString(PRESSURE_KEY)
        direction.text = savedInstanceState.getString(DIRECTION_KEY)
        speed.text = savedInstanceState.getString(SPEED_KEY)
        temp.text = savedInstanceState.getString(TEMP_KEY)
        time.text = savedInstanceState.getString(TIME_KEY)

        savedInstanceState.getString(WEATHERIMAGE_KEY)?.let { savedImageName ->
            weatherImageName = savedImageName
            val resID = resources.getIdentifier(weatherImageName, "drawable", packageName)
            if (resID != 0) {
                val drawable = AppCompatResources.getDrawable(this, resID)
                weatherImage.setImageDrawable(drawable)
            }
        }
    }
}