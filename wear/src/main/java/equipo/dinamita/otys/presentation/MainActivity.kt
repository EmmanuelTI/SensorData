package equipo.dinamita.otys.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import equipo.dinamita.otys.R
import equipo.dinamita.otys.presentation.sensors.HeartRateSensorManager

class MainActivity : AppCompatActivity() {

    private lateinit var heartRateSensorManager: HeartRateSensorManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (checkSelfPermission(Manifest.permission.BODY_SENSORS) == PackageManager.PERMISSION_GRANTED) {
            Log.d("PERMISO", "Permiso BODY_SENSORS concedido")
            iniciarSensor()
        } else {
            Log.d("PERMISO", "Permiso BODY_SENSORS NO concedido, solicitando...")
            requestPermissions(arrayOf(Manifest.permission.BODY_SENSORS), 100)
        }
    }

    private fun iniciarSensor() {
        heartRateSensorManager = HeartRateSensorManager(this, lifecycle)
        lifecycle.addObserver(heartRateSensorManager)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("PERMISO", "Permiso BODY_SENSORS concedido tras solicitud")
                iniciarSensor()
            } else {
                Log.e("PERMISO", "Permiso BODY_SENSORS denegado")
            }
        }
    }
}
