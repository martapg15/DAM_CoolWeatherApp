package dam_A51564.coolweatherapp.view

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import dam_A51564.coolweatherapp.databinding.ActivityMainBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import dam_A51564.coolweatherapp.R
import dam_A51564.coolweatherapp.viewmodel.WeatherViewModel

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: WeatherViewModel by viewModels()
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        setupObservers()

        if (savedInstanceState == null) {
            checkPermissionsAndGetLocation()
        }
    }

    private fun setupObservers() {
        viewModel.currentResource.observe(this) { resource ->
            resource?.let {
                val isDay = viewModel.isDaytime.value ?: true

                val wImage = when (it.code) {
                    0, 1, 2 -> if (isDay) it.image + "day" else it.image + "night"
                    else -> it.image
                }

                val resId = resources.getIdentifier(wImage, "drawable", packageName)
                if (resId != 0) {
                    binding.weatherImage.setImageResource(resId)
                }
            }
        }

        viewModel.isDaytime.observe(this) { isDay ->
            val isLandscape = resources.configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

            if (isDay) {
                if (isLandscape) {
                    binding.container.setBackgroundResource(R.drawable.background_land)
                } else {
                    binding.container.setBackgroundResource(R.drawable.background)
                }
            } else {
                binding.container.setBackgroundResource(R.drawable.night_background)
            }
        }

        viewModel.errorLat.observe(this) { errorMsg ->
            binding.textLat.error = errorMsg
        }

        viewModel.errorLong.observe(this) { errorMsg ->
            binding.textLong.error = errorMsg
        }
    }

    private fun checkPermissionsAndGetLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1000)
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                viewModel.updateCoordinates(it.latitude, it.longitude)
            }
        }
    }
}