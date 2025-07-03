package equipo.dinamita.otys.presentation

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import equipo.dinamita.otys.R
import equipo.dinamita.otys.presentation.sensors.HeartRateForegroundService
import equipo.dinamita.otys.presentation.sensors.HeartRateSensorManager

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: SensorViewModel
    private lateinit var adapter: SensorPagerAdapter
    private lateinit var tvHeartRate: TextView
    private lateinit var heartRateSensorManager: HeartRateSensorManager

    private val REQUIRED_PERMISSIONS = mutableListOf(
        Manifest.permission.BODY_SENSORS
    ).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.FOREGROUND_SERVICE_HEALTH)
        }
    }.toTypedArray()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tvHeartRate = findViewById(R.id.tvHeartRate)
        viewModel = ViewModelProvider(this)[SensorViewModel::class.java]

        val viewPager = findViewById<ViewPager2>(R.id.viewPager)
        adapter = SensorPagerAdapter(this, emptyList())
        viewPager.adapter = adapter

        // Observamos cambios en los datos de sensores para actualizar UI
        viewModel.sensorData.observe(this) { newSensorData ->
            adapter.updateData(newSensorData)

            val heartRate = newSensorData.find { it.first == "Ritmo Cardíaco" }?.second ?: "-- bpm"
            tvHeartRate.text = "Ritmo Cardíaco: $heartRate"
        }

        // Crear e iniciar HeartRateSensorManager para registrar sensor con lifecycle
        heartRateSensorManager = HeartRateSensorManager(this, lifecycle, viewModel)
        lifecycle.addObserver(heartRateSensorManager)

        // Verificar permisos y arrancar servicio foreground si están dados
        if (REQUIRED_PERMISSIONS.all {
                ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
            }) {
            startHeartRateService()
        } else {
            requestPermissions(REQUIRED_PERMISSIONS, 100)
        }
    }

    private fun startHeartRateService() {
        val serviceIntent = Intent(this, HeartRateForegroundService::class.java)
        ContextCompat.startForegroundService(this, serviceIntent)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
            startHeartRateService()
        } else {
            Log.e("PERMISOS", "Permisos requeridos denegados")
        }
    }
}
