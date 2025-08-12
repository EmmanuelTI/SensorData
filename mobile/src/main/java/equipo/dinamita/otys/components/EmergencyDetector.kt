package equipo.dinamita.otys.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.math.sqrt

class EmergencyDetector(private val context: Context) {

    private var lastHeartRate = 0f
    private var lastHeartRateTime = 0L

    private val HEART_RATE_THRESHOLD = 130f
    private val HEART_RATE_JUMP = 40f
    private val MOVEMENT_THRESHOLD = 15f // Ajustar según pruebas

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    /**
     * Procesa el ritmo cardíaco y detecta subidas bruscas.
     */
    fun processHeartRate(currentRate: Float) {
        val currentTime = System.currentTimeMillis()

        if (lastHeartRate > 0 &&
            currentRate >= HEART_RATE_THRESHOLD &&
            (currentRate - lastHeartRate) >= HEART_RATE_JUMP &&
            (currentTime - lastHeartRateTime) < 5000) { // 5 segundos

            Log.d("EmergencyDetector", "Subida brusca de ritmo: $lastHeartRate -> $currentRate")
            triggerEmergency(currentRate, null) // Ubicación puede pasarse después
        }

        lastHeartRate = currentRate
        lastHeartRateTime = currentTime
    }

    /**
     * Procesa valores de acelerómetro y giroscopio para detectar movimientos bruscos.
     */
    fun processMovement(accValues: FloatArray, gyroValues: FloatArray) {
        val accMagnitude = sqrt(accValues.map { it * it }.sum())
        val gyroMagnitude = sqrt(gyroValues.map { it * it }.sum())

        if (accMagnitude > MOVEMENT_THRESHOLD || gyroMagnitude > MOVEMENT_THRESHOLD) {
            Log.d("EmergencyDetector", "Movimiento brusco detectado - Acc: $accMagnitude, Giro: $gyroMagnitude")
            // Aquí podrías llamar triggerEmergency o manejar patrones para evitar falsos positivos
        }
    }

    /**
     * Dispara la emergencia: obtiene el número de teléfono desde Firestore y envía mensaje WhatsApp.
     * @param currentHeartRate El ritmo cardíaco actual detectado.
     * @param location Par(latitud, longitud) o null si no disponible.
     */
    fun triggerEmergency(currentHeartRate: Float, location: Pair<Double, Double>?) {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Toast.makeText(context, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
            return
        }

        firestore.collection("usuarios").document(currentUser.uid).get()
            .addOnSuccessListener { doc ->
                val phoneNumber = doc.getString("numeroTelefono")
                if (!phoneNumber.isNullOrEmpty()) {
                    enviarMensajeWhatsApp(currentHeartRate, location, phoneNumber)
                } else {
                    Toast.makeText(context, "Número de emergencia no configurado", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(context, "Error al obtener número: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("EmergencyDetector", "Error Firestore al obtener teléfono", e)
            }
    }

    private fun enviarMensajeWhatsApp(currentHeartRate: Float, location: Pair<Double, Double>?, phoneNumber: String) {
        val locationText = location?.let { "Ubicación: https://maps.google.com/?q=${it.first},${it.second}" } ?: "Ubicación no disponible"
        val message = """
            🚨 ¡ESTOY EN PELIGRO!
            ❤️ Ritmo cardíaco: ${currentHeartRate.toInt()} bpm
            📍 $locationText
        """.trimIndent()

        val uri = Uri.parse("https://wa.me/$phoneNumber?text=" + Uri.encode(message))
        val intent = Intent(Intent.ACTION_VIEW, uri).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }

        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "No se pudo abrir WhatsApp", Toast.LENGTH_SHORT).show()
            Log.e("EmergencyDetector", "Error al abrir WhatsApp", e)
        }
    }
}
