package equipo.dinamita.otys.presentation.sensors

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.android.gms.location.*
import equipo.dinamita.otys.presentation.SensorViewModel
import android.Manifest
import android.content.pm.PackageManager

class GpsLocationManager(
    private val context: Context,
    private val lifecycle: androidx.lifecycle.Lifecycle,
    private val viewModel: SensorViewModel
) : DefaultLifecycleObserver {

    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    override fun onResume(owner: LifecycleOwner) {
       // Log.d("GPS_LOCATION", "Intentando iniciar el servicio de ubicación...")
        startLocationUpdates()
    }

    override fun onPause(owner: LifecycleOwner) {
        // Log.d("GPS_LOCATION", "Deteniendo servicio de ubicación")
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
           // Log.e("GPS_LOCATION", "Permiso de ubicación no concedido")
            return
        }

        locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000L)
            .setMinUpdateIntervalMillis(3000L)
            .build()

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
               // Log.d("GPS_LOCATION", "Callback recibido")

                val location = locationResult.lastLocation
                if (location != null) {
                    val lat = location.latitude
                    val lon = location.longitude
                  //  Log.d("GPS_LOCATION", "Ubicación recibida: $lat, $lon")
                    viewModel.updateSensor("GPS", "Lat: $lat, Lon: $lon")
                } else {
                  //  Log.w("GPS_LOCATION", "Ubicación nula")
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
        //Log.d("GPS_LOCATION", "Request de ubicación iniciado")
    }
}
