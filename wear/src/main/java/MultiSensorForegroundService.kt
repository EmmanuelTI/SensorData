package equipo.dinamita.otys.presentation.sensors

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat

class MultiSensorForegroundService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager

    private var heartRateSensor: Sensor? = null
    private var accelerometerSensor: Sensor? = null
    private var gyroscopeSensor: Sensor? = null
    private var gravitySensor: Sensor? = null

    override fun onCreate() {
        super.onCreate()
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

        heartRateSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        gyroscopeSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(NOTIFICATION_ID, createNotification())

        heartRateSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }

        accelerometerSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }

        gyroscopeSensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }

        gravitySensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }

        return START_STICKY
    }

    override fun onDestroy() {
        sensorManager.unregisterListener(this)
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            val sensorName = when (it.sensor.type) {
                Sensor.TYPE_HEART_RATE -> "Ritmo Cardíaco"
                Sensor.TYPE_ACCELEROMETER -> "Acelerómetro"
                Sensor.TYPE_GYROSCOPE -> "Giroscopio"
                Sensor.TYPE_GRAVITY -> "Gravedad"
                else -> "Desconocido"
            }

            val data = it.values.joinToString(", ") { v -> "%.2f".format(v) }
            Log.d("MultiSensorService", "$sensorName -> $data")
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun createNotification(): Notification {
        val channelId = "multi_sensor_channel"
        val channelName = "Sensores en Segundo Plano"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val chan = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_LOW)
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(chan)
        }

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Sensores activos")
            .setContentText("Recopilando datos de sensores en segundo plano")
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .setOngoing(true)
            .build()
    }

    companion object {
        const val NOTIFICATION_ID = 5678
    }
}
