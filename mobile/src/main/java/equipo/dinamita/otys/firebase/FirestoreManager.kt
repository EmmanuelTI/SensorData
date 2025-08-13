package equipo_dinamita.otys.firebase

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import equipo.dinamita.otys.dbsqlite.SensorDatabaseHelper
import equipo.dinamita.otys.dbsqlite.model.SensorRecord

class FirestoreManager(private val context: Context) {
    private val db = FirebaseFirestore.getInstance()
    private val sqliteHelper = SensorDatabaseHelper(context)

    private val usersCollection = "SensorUser"
    private val sensorDataSubcollection = "sensor"

    fun uploadAllSensorDataToFirestore(
        onProgress: ((current: Int, total: Int) -> Unit)? = null,
        onComplete: (() -> Unit)? = null
    ) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Log.w("FirestoreManager", "Usuario no autenticado, no se subirán datos")
            return
        }

        try {
            val sensorRecords = sqliteHelper.getAllSensorData()

            if (sensorRecords.isEmpty()) {
                Log.d("FirestoreManager", "No hay datos que subir")
                onComplete?.invoke()
                return
            }

            var successCount = 0
            val totalRecords = sensorRecords.size
            val lastValueBySensor = mutableMapOf<String, Any?>()

            sensorRecords.forEach { record ->
                val lastValue = lastValueBySensor[record.sensorName]
                if (lastValue != record.value) {
                    val firestoreRecord = hashMapOf(
                        "sensorName" to record.sensorName,
                        "value" to record.value,
                        "timestamp" to record.timestamp
                    )

                    db.collection(usersCollection)
                        .document(user.uid)
                        .collection(sensorDataSubcollection)
                        .add(firestoreRecord)
                        .addOnSuccessListener {
                            successCount++
                            onProgress?.invoke(successCount, totalRecords)

                            if (successCount == totalRecords) {
                                sqliteHelper.deleteAllSensorData()
                                onComplete?.invoke()
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.w("FirestoreManager", "Error añadiendo documento", e)
                            successCount++
                            onProgress?.invoke(successCount, totalRecords)
                            if (successCount == totalRecords) {
                                onComplete?.invoke()
                            }
                        }

                    lastValueBySensor[record.sensorName] = record.value
                } else {
                    Log.d("FirestoreManager", "Dato repetido para sensor ${record.sensorName}, no se sube")
                    successCount++
                    onProgress?.invoke(successCount, totalRecords)
                    if (successCount == totalRecords) {
                        sqliteHelper.deleteAllSensorData()
                        onComplete?.invoke()
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("FirestoreManager", "Error al subir datos a Firestore", e)
            onComplete?.invoke()
        }
    }

















    fun uploadSingleRecordToFirestore(record: SensorRecord) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null) {
            Log.w("FirestoreManager", "Usuario no autenticado, no se guardará el dato")
            return
        }

        val firestoreRecord = hashMapOf(
            "sensorName" to record.sensorName,
            "value" to record.value,
            "timestamp" to record.timestamp
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

        val dataMap = hashMapOf<String, Any>(
            "sensorName" to record.sensorName,
            "value" to record.value,
            "timestamp" to record.timestamp,
            "latitude" to latitude,
            "longitude" to longitude
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
