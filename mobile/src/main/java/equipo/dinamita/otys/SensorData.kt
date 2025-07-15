package equipo.dinamita.otys

data class SensorData(
    val name: String,
    val value: Int = 0,
    val extra: String = ""  // Para datos que no son enteros, como coordenadas GPS
)
