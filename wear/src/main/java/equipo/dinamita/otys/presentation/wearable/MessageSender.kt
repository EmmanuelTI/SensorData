package equipo.dinamita.otys.presentation.wearable

import android.content.Context
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.tasks.await

class MessageSender(private val context: Context) {

    suspend fun sendMessage(path: String, message: String) {
        val nodes = Wearable.getNodeClient(context).connectedNodes.await()
        val nodeId = nodes.firstOrNull()?.id ?: return
        Wearable.getMessageClient(context)
            .sendMessage(nodeId, path, message.toByteArray())
            .await()
    }
}
