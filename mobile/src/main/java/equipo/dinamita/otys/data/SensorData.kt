package equipo.dinamita.otys.data

data class SensorData(
    val name: String,
    val value: Int = 0,
    val extra: String = ""  // Para datos no enteros, como coordenadas o vectores
) {
    // Coordenadas GPS: extra = "lat,lon"
    val coordinates: Pair<Double, Double>?
        get() = extra.split(",").mapNotNull { it.toDoubleOrNull() }
            .takeIf { it.size == 2 }
            ?.let { it[0] to it[1] }

    // Valores de sensores vectoriales: extra = "x,y,z"
    val vectorXYZ: Triple<Float, Float, Float>
        get() {
            val values = extra.split(",").mapNotNull { it.toFloatOrNull() }
            return Triple(
                values.getOrNull(0) ?: 0f,
                values.getOrNull(1) ?: 0f,
                values.getOrNull(2) ?: 0f
            )
        }
}
