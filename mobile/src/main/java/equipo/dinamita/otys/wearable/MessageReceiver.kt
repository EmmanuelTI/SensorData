package equipo.dinamita.otys.wearable

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.google.android.gms.wearable.Wearable

class MessageReceiver(
    private val context: Context,
    private val onMessageReceived: (String, String) -> Unit
) : MessageClient.OnMessageReceivedListener {

    fun startListening() {
        Wearable.getMessageClient(context).addListener(this)
    }

    fun stopListening() {
        Wearable.getMessageClient(context).removeListener(this)
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        val path = messageEvent.path
        val data = String(messageEvent.data)
        Log.d("WearMessage", "📲 Mensaje recibido — Path: $path — Data: $data")
        onMessageReceived(path, data)
    }
}
