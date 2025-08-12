package equipo.dinamita.otys.components

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.math.sqrt

class EmergencyDetector(private val context: Context) {
    private var lastHeartRate = 0f
    private var lastHeartRateTime = 0L
    private var lastEmergencySentTime = 0L
    private val EMERGENCY_COOLDOWN_MS = 5 * 60 * 1000L // 5 minutos cooldown

    private val HEART_RATE_THRESHOLD = 80f
    private val HEART_RATE_JUMP = 10f
    private val MOVEMENT_THRESHOLD = 10f

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Última ubicación conocida (latitud, longitud)
    var currentLocation: Pair<Double, Double>? = null

    /**
     * Actualiza la ubicación usando LocationManager sin Google Play Services
     */
    @SuppressLint("MissingPermission")
    fun updateLocation() {
        if (!hasLocationPermission()) {
            Log.e("EmergencyDetector", "Permisos de ubicación no concedidos")
            return
        }

        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val providers = locationManager.getProviders(true)

        var bestLocation: Location? = null
        for (provider in providers) {
            val location = locationManager.getLastKnownLocation(provider)
            if (location != null) {
                if (bestLocation == null || location.accuracy < bestLocation.accuracy) {
                    bestLocation = location
                }
            }
        }

        if (bestLocation != null) {
            currentLocation = Pair(bestLocation.latitude, bestLocation.longitude)
            Log.d("EmergencyDetector", "Ubicación actualizada: $currentLocation")
        } else {
            Log.e("EmergencyDetector", "No se pudo obtener ubicación")
        }
    }

    private fun hasLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    fun processHeartRate(currentRate: Float) {
        val currentTime = System.currentTimeMillis()

        if (lastHeartRate > 0 &&
            currentRate >= HEART_RATE_THRESHOLD &&
            (currentRate - lastHeartRate) >= HEART_RATE_JUMP &&
            (currentTime - lastHeartRateTime) < 10000) {

            if (currentTime - lastEmergencySentTime > EMERGENCY_COOLDOWN_MS) {
                Log.d("EmergencyDetector", "Subida brusca de ritmo: $lastHeartRate -> $currentRate")
                updateLocation()  // Actualiza ubicación antes de enviar emergencia
                triggerEmergency(currentRate, currentLocation)
                lastEmergencySentTime = currentTime
            } else {
                Log.d("EmergencyDetector", "Alerta ignorada por cooldown")
            }
        }

        lastHeartRate = currentRate
        lastHeartRateTime = currentTime
    }

    fun processMovement(accValues: FloatArray, gyroValues: FloatArray) {
        val accMagnitude = sqrt(accValues.map { it * it }.sum())
        val gyroMagnitude = sqrt(gyroValues.map { it * it }.sum())

        if (accMagnitude > MOVEMENT_THRESHOLD || gyroMagnitude > MOVEMENT_THRESHOLD) {
            Log.d("EmergencyDetector", "Movimiento brusco detectado - Acc: $accMagnitude, Giro: $gyroMagnitude")
        }
    }

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
