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


class HeartRateSensorManager(
    private val context: Context,
    private val lifecycle: Lifecycle,
    private val viewModel: SensorViewModel
) : DefaultLifecycleObserver, SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private var heartRateSensor: Sensor? = null

    override fun onResume(owner: LifecycleOwner) {
        heartRateSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)
        if (heartRateSensor == null) {
            Log.e("HEART_RATE", "Sensor de ritmo cardíaco no disponible")
            viewModel.resetHeartRate()  // Actualiza el valor a "-- bpm"
            return
        }
        sensorManager.registerListener(this, heartRateSensor, SensorManager.SENSOR_DELAY_NORMAL)
        Log.d("HEART_RATE", "Sensor registrado")
    }

    override fun onPause(owner: LifecycleOwner) {
        sensorManager.unregisterListener(this)
        Log.d("HEART_RATE", "Sensor desregistrado")
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (it.sensor.type == Sensor.TYPE_HEART_RATE) {
                val bpm = it.values[0].toInt()
                Log.d("HEART_RATE", "Valor: $bpm bpm")
                viewModel.updateSensor("Ritmo Cardíaco", "$bpm bpm")
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No es necesario implementar para este caso
    }
}
