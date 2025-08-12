package equipo.dinamita.otys.sensors

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class SensorDataReceiver(
    private val onSensorDataReceived: (sensorName: String, valueStr: String) -> Unit,
    private val onGpsDataReceived: (latitude: Double, longitude: Double) -> Unit
) : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val data = intent?.getStringExtra("sensorData") ?: return
        val parts = data.split(";")
        for (part in parts) {
            val (sensorName, valueStr) = part.split(":").takeIf { it.size == 2 } ?: continue

            if (sensorName == "GPS") {
                val coords = valueStr.split(",")
                if (coords.size == 2) {
                    val lat = coords[0].toDoubleOrNull()
                    val lon = coords[1].toDoubleOrNull()
                    if (lat != null && lon != null) {
                        onGpsDataReceived(lat, lon)
                    }
                }
            } else {
                onSensorDataReceived(sensorName, valueStr)
            }
        }
    }
}
