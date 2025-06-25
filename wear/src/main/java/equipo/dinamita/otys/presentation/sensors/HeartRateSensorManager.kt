package equipo.dinamita.otys.presentation.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import equipo.dinamita.otys.presentation.wearable.MessageSender
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class HeartRateSensorManager(
    private val context: Context,
    private val lifecycle: Lifecycle,
    private val messageSender: MessageSender
) : SensorEventListener, LifecycleObserver {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val heartRateSensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun start() {
        heartRateSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun stop() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (it.sensor.type == Sensor.TYPE_HEART_RATE) {
                val heartRateValue = it.values[0]
                coroutineScope.launch {
                    messageSender.sendMessage("/sensor/heart_rate", heartRateValue.toString())
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No es necesario implementar si no quieres
    }
}
