package equipo_dinamita.otys.firebase

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import equipo.dinamita.otys.dbsqlite.SensorDatabaseHelper
import equipo.dinamita.otys.dbsqlite.model.SensorRecord
import equipo_dinamita.otys.firebase.models.FirestoreSensorRecord

class FirestoreManager(private val context: Context) {
    private val db = FirebaseFirestore.getInstance()
    private val sqliteHelper = SensorDatabaseHelper(context)

    private val usersCollection = "SensorUser"
    private val sensorDataSubcollection = "sensor"

    fun uploadAllSensorDataToFirestore() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Log.w("FirestoreManager", "Usuario no autenticado, no se subirán datos")
            return
        }

        val displayName = user.displayName ?: "desconocido"

        try {
            val sensorRecords = sqliteHelper.getAllSensorData()

            sensorRecords.forEach { record ->
                val firestoreRecord = hashMapOf(
                    "sensorName" to record.sensorName,
                    "value" to record.value,
                    "timestamp" to record.timestamp,
                    "nombreUsuario" to displayName
                )

                db.collection(usersCollection)
                    .document(user.uid)
                    .collection(sensorDataSubcollection)
                    .add(firestoreRecord)
                    .addOnSuccessListener { documentReference ->
                        Log.d("FirestoreManager", "Documento añadido con ID: ${documentReference.id}")
                    }
                    .addOnFailureListener { e ->
                        Log.w("FirestoreManager", "Error añadiendo documento", e)
                    }
            }
        } catch (e: Exception) {
            Log.e("FirestoreManager", "Error al subir datos a Firestore", e)
        }
    }

    fun uploadSingleRecordToFirestore(record: SensorRecord) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Log.w("FirestoreManager", "Usuario no autenticado, no se guardará el dato")
            return
        }

        val displayName = user.displayName ?: "desconocido"

        val firestoreRecord = hashMapOf(
            "sensorName" to record.sensorName,
            "value" to record.value,
            "timestamp" to record.timestamp,
            "nombreUsuario" to displayName
        )

        db.collection(usersCollection)
            .document(user.uid)
            .collection(sensorDataSubcollection)
            .add(firestoreRecord)
            .addOnSuccessListener { documentReference ->
                Log.d("FirestoreManager", "Documento añadido con ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w("FirestoreManager", "Error añadiendo documento", e)
            }
    }

    fun uploadRecordWithLocation(
        record: SensorRecord,
        latitude: Double,
        longitude: Double
    ) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Log.w("FirestoreManager", "Usuario no autenticado, no se guardará el dato")
            return
        }

        val displayName = user.displayName ?: "desconocido"

        val dataMap = hashMapOf<String, Any>(
            "sensorName" to record.sensorName,
            "value" to record.value,
            "timestamp" to record.timestamp,
            "latitude" to latitude,
            "longitude" to longitude,
            "nombreUsuario" to displayName
        )

        db.collection(usersCollection)
            .document(user.uid)
            .collection(sensorDataSubcollection)
            .add(dataMap)
            .addOnSuccessListener { documentReference ->
                Log.d("FirestoreManager", "Documento con ubicación añadido ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w("FirestoreManager", "Error añadiendo documento con ubicación", e)
            }
    }
}
