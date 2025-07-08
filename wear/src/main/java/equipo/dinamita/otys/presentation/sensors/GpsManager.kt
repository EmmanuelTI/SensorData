package equipo.dinamita.otys.presentation.sensors

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.google.android.gms.location.*
import equipo.dinamita.otys.presentation.SensorViewModel

class GpsLocationManager(
    private val context: Context,
    private val lifecycle: Lifecycle,
    private val viewModel: SensorViewModel
) : DefaultLifecycleObserver {

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    override fun onResume(owner: LifecycleOwner) {
        startLocationUpdates()
        Log.d("GPS_LOCATION", "Servicio de ubicación iniciado")
    }

    override fun onPause(owner: LifecycleOwner) {
        fusedLocationClient.removeLocationUpdates(locationCallback)
        Log.d("GPS_LOCATION", "Servicio de ubicación detenido")
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, 1000L
        ).build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location = locationResult.lastLocation
                location?.let {
                    val lat = it.latitude
                    val lon = it.longitude
                    Log.d("GPS_LOCATION", "Ubicación: $lat, $lon")
                    viewModel.updateSensor("GPS", "Lat: $lat, Lon: $lon")
                }
            }
        }

        // Asegúrate de haber pedido permisos ACCESS_FINE_LOCATION antes de llamar este método
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }
}
