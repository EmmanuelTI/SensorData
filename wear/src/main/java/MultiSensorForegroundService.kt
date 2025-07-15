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
import com.google.android.gms.wearable.Wearable

class MultiSensorForegroundService : Service(), SensorEventListener {

    private lateinit var sensorManager: SensorManager

    private var heartRateSensor: Sensor? = null
    private var accelerometerSensor: Sensor? = null
    private var gyroscopeSensor: Sensor? = null
    private var gravitySensor: Sensor? = null

    private val PATH_SENSOR_DATA = "/sensor_data_path"

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

    // Cambiado: guardamos el último tiempo enviado por cada tipo de sensor
    private val lastSentTimes = mutableMapOf<Int, Long>()
    private val sendIntervalMs = 1000L  // 1 segundo entre envíos por sensor

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            val sensorType = it.sensor.type
            val currentTime = System.currentTimeMillis()
            val lastTime = lastSentTimes[sensorType] ?: 0L

            // Solo enviamos si ha pasado el intervalo desde el último envío de este sensor
            if (currentTime - lastTime < sendIntervalMs) return

            val sensorName = when (sensorType) {
                Sensor.TYPE_HEART_RATE -> "Ritmo Cardíaco"
                Sensor.TYPE_ACCELEROMETER -> "Acelerómetro"
                Sensor.TYPE_GYROSCOPE -> "Giroscopio"
                Sensor.TYPE_GRAVITY -> "Gravedad"
                else -> "Desconocido"
            }

            val data = when (sensorType) {
                Sensor.TYPE_HEART_RATE -> "${it.values[0].toInt()} bpm"
                else -> it.values.joinToString(",") { v -> "%.2f".format(v) }
            }

            val message = "$sensorName:$data"
            Log.d("MultiSensorService", "Preparando para enviar: $message")

            sendDataToPhone(message)

            lastSentTimes[sensorType] = currentTime
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun sendDataToPhone(message: String) {
        val nodeListTask = Wearable.getNodeClient(this).connectedNodes
        nodeListTask.addOnSuccessListener { nodes ->
            for (node in nodes) {
                Wearable.getMessageClient(this).sendMessage(node.id, PATH_SENSOR_DATA, message.toByteArray())
                    .addOnSuccessListener {
                        Log.d("WearDataLayer", "Enviado $message a ${node.displayName}")
                    }
                    .addOnFailureListener { e ->
                        Log.e("WearDataLayer", "Error enviando mensaje: ${e.message}")
                    }
            }
        }
    }

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