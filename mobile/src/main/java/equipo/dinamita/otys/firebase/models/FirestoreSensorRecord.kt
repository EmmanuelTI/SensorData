package equipo_dinamita.otys.firebase.models

data class FirestoreSensorRecord(
    val sensorName: String,
    val value: String,
    val timestamp: String
    // No incluimos el ID ya que Firestore genera su propio ID
)