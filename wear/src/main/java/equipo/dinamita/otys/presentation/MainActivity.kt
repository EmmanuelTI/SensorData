package equipo.dinamita.otys.presentation

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import equipo.dinamita.otys.R
import equipo.dinamita.otys.presentation.sensors.AccelerometerSensorManager
import equipo.dinamita.otys.presentation.sensors.GpsLocationManager
import equipo.dinamita.otys.presentation.sensors.GyroscopeSensorManager
import equipo.dinamita.otys.presentation.sensors.MultiSensorForegroundService
import equipo.dinamita.otys.presentation.sensors.HeartRateSensorManager
import equipo.dinamita.otys.presentation.sensors.StressSensorManager


class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: SensorViewModel
    private lateinit var adapter: SensorPagerAdapter
    private lateinit var heartRateSensorManager: HeartRateSensorManager
    private lateinit var gpsLocationManager: GpsLocationManager  // 📌 Aquí lo declaramos
    private lateinit var gyroscopeSensorManager: GyroscopeSensorManager
    private lateinit var accelerometerSensorManager: AccelerometerSensorManager


    private lateinit var stressSensorManager: StressSensorManager

    private val REQUIRED_PERMISSIONS = mutableListOf(
        Manifest.permission.BODY_SENSORS,
        Manifest.permission.ACCESS_FINE_LOCATION  // 📌 Agregar permiso de ubicación
    ).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.FOREGROUND_SERVICE_HEALTH)
        }
    }.toTypedArray()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        viewModel = ViewModelProvider(this)[SensorViewModel::class.java]

        val viewPager = findViewById<ViewPager2>(R.id.viewPager)
        adapter = SensorPagerAdapter(this, emptyList())
        viewPager.adapter = adapter

        // Observamos los sensores para actualizar el ViewPager
        viewModel.sensorData.observe(this) { newSensorData ->
            adapter.updateData(newSensorData)
        }

        // Manejo de sensores: ritmo cardíaco y GPS
        heartRateSensorManager = HeartRateSensorManager(this, lifecycle, viewModel)
        lifecycle.addObserver(heartRateSensorManager)

        gpsLocationManager = GpsLocationManager(this, lifecycle, viewModel)
        lifecycle.addObserver(gpsLocationManager)

        gyroscopeSensorManager = GyroscopeSensorManager(this, lifecycle, viewModel)
        lifecycle.addObserver(gyroscopeSensorManager)

        accelerometerSensorManager = AccelerometerSensorManager(this, lifecycle, viewModel)
        lifecycle.addObserver(accelerometerSensorManager)

        stressSensorManager = StressSensorManager(this, lifecycle, viewModel)
        lifecycle.addObserver(stressSensorManager)


        // Verificar permisos antes de iniciar servicios
        if (REQUIRED_PERMISSIONS.all {
                ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
            }) {
            startSensorService()

        } else {
            requestPermissions(REQUIRED_PERMISSIONS, 100)
        }
    }

    private fun startSensorService() {
        val intent = Intent(this, MultiSensorForegroundService::class.java)
        ContextCompat.startForegroundService(this, intent)
    }


    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            startSensorService()

        } else {
            Log.e("PERMISOS", "Permisos requeridos denegados")
        }
    }
}
