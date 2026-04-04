package dam_A51564.coolweatherapp.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dam_A51564.coolweatherapp.model.WeatherData
import dam_A51564.coolweatherapp.model.WeatherResource
import dam_A51564.coolweatherapp.model.getWeatherResources
import dam_A51564.coolweatherapp.repository.WeatherRepository

class WeatherViewModel(application: Application) : AndroidViewModel(application) {
    // The ViewModel doesn't care if the data comes from a Network API, a local database, or a cache,
    // it only knows the Repository will provide a WeatherData object.
    // So instantiating the repository here follows the 'Separation of Concerns' principle by keeping networking
    // logic out of the Business Logic layer, making the code easier to test and maintain.
    private val repository = WeatherRepository()

    val latitude = MutableLiveData<String>()
    val longitude = MutableLiveData<String>()

    // I decided to use a private MutableLiveData so it's only possible for the ViewModel to modify the data.
    // What will be shown to the View (MainActivity) is a public immutable LiveData to ensure
    // the UI cannot change the state directly, preserving data integrity.
    private val _weatherData = MutableLiveData<WeatherData?>()
    val weatherData: LiveData<WeatherData?> = _weatherData

    private val _currentResource = MutableLiveData<WeatherResource?>()
    val currentResource: LiveData<WeatherResource?> = _currentResource

    private val _isDaytime = MutableLiveData<Boolean>()
    val isDaytime: LiveData<Boolean> = _isDaytime

    val errorLat = MutableLiveData<String?>()
    val errorLong = MutableLiveData<String?>()

    fun fetchWeatherData() {
        val lat = latitude.value?.toFloatOrNull() ?: return
        val lon = longitude.value?.toFloatOrNull() ?: return

        var hasError = false
        if (lat !in -90.0..90.0) {
            errorLat.value = "Latitude must be between -90 and 90"
            hasError = true
        } else {
            errorLat.value = null
        }

        if (lon !in -180.0..180.0) {
            errorLong.value = "Longitude must be between -180 and 180"
            hasError = true
        } else {
            errorLong.value = null
        }

        if (hasError) return

        Thread {
            val data = repository.getWeatherData(lat, lon)

            if (data != null) {
                val currentTime = data.current_weather.time
                val sunrise = data.daily.sunrise[0]
                val sunset = data.daily.sunset[0]
                val isDay = currentTime in sunrise..<sunset

                _isDaytime.postValue(isDay)
                _weatherData.postValue(data)

                val resources = getWeatherResources(getApplication())
                _currentResource.postValue(resources[data.current_weather.weathercode])
            }
        }.start()
    }

    fun updateCoordinates(lat: Double, lon: Double) {
        latitude.value = lat.toString()
        longitude.value = lon.toString()
        fetchWeatherData()
    }
}