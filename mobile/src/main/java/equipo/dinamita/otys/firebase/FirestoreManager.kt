package equipo_dinamita.otys.firebase

import android.content.Context
import equipo.dinamita.otys.dbsqlite.SensorDatabaseHelper
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import equipo.dinamita.otys.dbsqlite.model.SensorRecord
import equipo_dinamita.otys.firebase.models.FirestoreSensorRecord

class FirestoreManager(private val context: Context) {
    private val db = FirebaseFirestore.getInstance()
    private val sqliteHelper = SensorDatabaseHelper(context)

    // Colección en Firestore donde se guardarán los datos
    private val collectionName = "sensor_records"

    /**
     * Sube todos los registros de SQLite a Firestore
     */
    fun uploadAllSensorDataToFirestore() {
        try {
            // Obtener todos los datos de SQLite
            val sensorRecords = sqliteHelper.getAllSensorData()

            // Convertir cada registro a formato Firestore y subirlo
            sensorRecords.forEach { record ->
                val firestoreRecord = FirestoreSensorRecord(
                    sensorName = record.sensorName,
                    value = record.value,
                    timestamp = record.timestamp
                )

                // Agregar documento a Firestore (Firestore generará automáticamente un ID)
                db.collection(collectionName)
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

    /**
     * Sube un solo registro a Firestore
     */
    fun uploadSingleRecordToFirestore(record: SensorRecord) {
        val firestoreRecord = FirestoreSensorRecord(
            sensorName = record.sensorName,
            value = record.value,
            timestamp = record.timestamp
        )

        db.collection(collectionName)
            .add(firestoreRecord)
            .addOnSuccessListener { documentReference ->
                Log.d("FirestoreManager", "Documento añadido con ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w("FirestoreManager", "Error añadiendo documento", e)
            }
    }
}