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

class AccelerometerSensorManager(
    private val context: Context,
    private val lifecycle: Lifecycle,
    private val viewModel: SensorViewModel
) : DefaultLifecycleObserver, SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private var accelerometerSensor: Sensor? = null

    override fun onResume(owner: LifecycleOwner) {
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        if (accelerometerSensor == null) {
            Log.e("MAIN_ACTIVITY", "⚠️ Sensor de acelerómetro no disponible")
            viewModel.updateSensor("Acelerómetro", "No disponible")
            return
        }
        sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL)
        Log.d("MAIN_ACTIVITY", "✅ Acelerómetro registrado")
    }

    override fun onPause(owner: LifecycleOwner) {
        sensorManager.unregisterListener(this)
        Log.d("MAIN_ACTIVITY", "⛔ Acelerómetro desregistrado")
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (it.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                val x = it.values[0]
                val y = it.values[1]
                val z = it.values[2]
                val formatted = "x: %.2f, y: %.2f, z: %.2f m/s²".format(x, y, z)
                Log.d("MAIN_ACTIVITY", "📦 Acelerómetro -> $formatted")
                viewModel.updateSensor("Acelerómetro", formatted)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No es necesario implementarlo en este caso
    }
}
