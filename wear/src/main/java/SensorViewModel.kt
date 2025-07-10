package equipo.dinamita.otys.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SensorViewModel : ViewModel() {

    private val _sensorData = MutableLiveData(
        listOf(
            "Ritmo Cardíaco" to "-- bpm",
            "GPS" to "Lat: ---, Lon: ---",
            "Giroscopio" to "X: ---, Y: ---, Z: ---",
            "Acelerómetro" to "X: ---, Y: ---, Z: ---",
        )
    )
    val sensorData: LiveData<List<Pair<String, String>>> = _sensorData

    fun updateSensor(title: String, newValue: String) {
        _sensorData.value = _sensorData.value?.map {
            if (it.first == title) title to newValue else it
        }
    }

    fun resetHeartRate() {
        updateSensor("Ritmo Cardíaco", "-- bpm")
    }

    // Nueva función para actualizar desde mensajes del wearable
    fun updateSensorDataFromWearable(rawMessage: String) {
        val parts = rawMessage.split(":", limit = 2)
        if (parts.size == 2) {
            val sensorName = parts[0]
            val value = parts[1]
            updateSensor(sensorName, value)
        }
    }
}
