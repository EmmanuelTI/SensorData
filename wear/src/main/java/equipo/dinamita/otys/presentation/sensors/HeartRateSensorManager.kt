package equipo.dinamita.otys.presentation.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent

class HeartRateSensorManager(
    private val context: Context,
    private val lifecycle: Lifecycle
) : SensorEventListener, LifecycleObserver {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val heartRateSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun start() {
        heartRateSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
            Log.d("HEART_RATE", "Sensor registrado")
        } ?: Log.e("HEART_RATE", "Sensor de ritmo cardíaco no disponible")
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun stop() {
        sensorManager.unregisterListener(this)
        Log.d("HEART_RATE", "Sensor desregistrado")
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (it.sensor.type == Sensor.TYPE_HEART_RATE) {
                val heartRateValue = it.values[0]
                Log.d("HEART_RATE", "Valor: $heartRateValue bpm")
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Opcional
    }
}
