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
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable
import equipo.dinamita.otys.R
import equipo.dinamita.otys.presentation.sensors.AccelerometerSensorManager
import equipo.dinamita.otys.presentation.sensors.GpsLocationManager
import equipo.dinamita.otys.presentation.sensors.GyroscopeSensorManager
import equipo.dinamita.otys.presentation.sensors.HeartRateSensorManager
import equipo.dinamita.otys.presentation.sensors.MultiSensorForegroundService

class MainActivity : AppCompatActivity(), MessageClient.OnMessageReceivedListener {

    private lateinit var viewModel: SensorViewModel
    private lateinit var adapter: SensorPagerAdapter
    private lateinit var heartRateSensorManager: HeartRateSensorManager
    private lateinit var gpsLocationManager: GpsLocationManager
    private lateinit var gyroscopeSensorManager: GyroscopeSensorManager
    private lateinit var accelerometerSensorManager: AccelerometerSensorManager

    private val REQUIRED_PERMISSIONS = mutableListOf(
        Manifest.permission.BODY_SENSORS,
        Manifest.permission.ACCESS_FINE_LOCATION
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

        // Observamos cambios para actualizar la UI
        viewModel.sensorData.observe(this) { newSensorData ->
            adapter.updateData(newSensorData)
        }

        // Inicializamos managers sensores locales
        heartRateSensorManager = HeartRateSensorManager(this, lifecycle, viewModel)
        lifecycle.addObserver(heartRateSensorManager)

        gpsLocationManager = GpsLocationManager(this, lifecycle, viewModel)
        lifecycle.addObserver(gpsLocationManager)

        gyroscopeSensorManager = GyroscopeSensorManager(this, lifecycle, viewModel)
        lifecycle.addObserver(gyroscopeSensorManager)

        accelerometerSensorManager = AccelerometerSensorManager(this, lifecycle, viewModel)
        lifecycle.addObserver(accelerometerSensorManager)

        // Pedir permisos y arrancar servicio en wearable
        if (REQUIRED_PERMISSIONS.all {
                ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
            }) {
            startSensorService()
        } else {
            requestPermissions(REQUIRED_PERMISSIONS, 100)
        }

        // Añadir listener para recibir mensajes del wearable
        Wearable.getMessageClient(this).addListener(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Remover listener para evitar leaks
        Wearable.getMessageClient(this).removeListener(this)
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

    // Aquí recibimos mensajes enviados desde el wearable
    override fun onMessageReceived(messageEvent: MessageEvent) {
        if (messageEvent.path == "/sensor_data_path") {
            val msg = String(messageEvent.data)
            Log.d("MobileListener", "Mensaje recibido: $msg")
            runOnUiThread {
                // Actualizar ViewModel para reflejar en UI
                viewModel.updateSensorDataFromWearable(msg)
            }
        }
    }
}
