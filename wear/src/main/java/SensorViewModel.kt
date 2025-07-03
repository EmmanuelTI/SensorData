package equipo.dinamita.otys.presentation

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SensorViewModel : ViewModel() {

    private val _sensorData = MutableLiveData(
        listOf(
            "Ritmo Cardíaco" to "-- bpm",
            "GPS" to "Lat: ---, Lon: ---",
            "Estrés" to "Nivel: --",
            "Giroscopio" to "X: ---, Y: ---, Z: ---",
            "Acelerómetro" to "X: ---, Y: ---, Z: ---",
            "Temperatura" to "-- °C"
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
}
