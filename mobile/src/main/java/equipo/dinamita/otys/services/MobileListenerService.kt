package equipo.dinamita.otys.services

import android.content.Intent
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.WearableListenerService

class MobileListenerService : WearableListenerService() {

    override fun onMessageReceived(messageEvent: MessageEvent) {
        if (messageEvent.path == "/sensor_data_path") {
            val message = String(messageEvent.data)
            Log.d("MobileListener", "Mensaje recibido: $message")

            val intent = Intent("SENSOR_DATA_UPDATE")
            intent.putExtra("sensorData", message)
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
        }
    }
}
