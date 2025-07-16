package equipo.dinamita.otys.dbsqlite.model

data class SensorRecord(
    val id: Int,
    val sensorName: String,
    val value: String,
    val timestamp: String
)
