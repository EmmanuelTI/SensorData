package equipo.dinamita.otys.presentation.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import equipo.dinamita.otys.presentation.SensorViewModel

class GyroscopeSensorManager(
    private val context: Context,
    private val lifecycle: Lifecycle,
    private val viewModel: SensorViewModel
) : DefaultLifecycleObserver, SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private var gyroscopeSensor: Sensor? = null

    override fun onResume(owner: LifecycleOwner) {
        gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        if (gyroscopeSensor == null) {
            Log.e("MAIN_ACTIVITY", "⚠️ Sensor de giroscopio no disponible")
            viewModel.updateSensor("Giroscopio", "No disponible")
            return
        }
        sensorManager.registerListener(this, gyroscopeSensor, SensorManager.SENSOR_DELAY_NORMAL)
        Log.d("MAIN_ACTIVITY", "✅ Giroscopio registrado")
    }

    override fun onPause(owner: LifecycleOwner) {
        sensorManager.unregisterListener(this)
        Log.d("MAIN_ACTIVITY", "⛔ Giroscopio desregistrado")
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (it.sensor.type == Sensor.TYPE_GYROSCOPE) {
                val x = it.values[0]
                val y = it.values[1]
                val z = it.values[2]
                val formatted = "x: %.2f, y: %.2f, z: %.2f rad/s".format(x, y, z)
                Log.d("MAIN_ACTIVITY", "📦 Giroscopio -> $formatted")
                viewModel.updateSensor("Giroscopio", formatted)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No necesario
    }
}
