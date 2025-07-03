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
import equipo.dinamita.otys.presentation.sensors.HeartRateForegroundService
import equipo.dinamita.otys.presentation.sensors.HeartRateSensorManager

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: SensorViewModel
    private lateinit var adapter: SensorPagerAdapter
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

        viewModel = ViewModelProvider(this)[SensorViewModel::class.java]

        val viewPager = findViewById<ViewPager2>(R.id.viewPager)
        adapter = SensorPagerAdapter(this, emptyList())
        viewPager.adapter = adapter

        // Observamos todos los sensores y actualizamos el ViewPager
        viewModel.sensorData.observe(this) { newSensorData ->
            adapter.updateData(newSensorData)
        }

        // Manejo del sensor desde el lifecycle
        heartRateSensorManager = HeartRateSensorManager(this, lifecycle, viewModel)
        lifecycle.addObserver(heartRateSensorManager)

        // Verificar permisos antes de iniciar el servicio en segundo plano
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
