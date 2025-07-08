package equipo.dinamita.otys.presentation.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import equipo.dinamita.otys.presentation.SensorViewModel
import kotlin.math.abs

class StressSensorManager(
    private val context: Context,
    private val lifecycle: Lifecycle,
    private val viewModel: SensorViewModel
) : DefaultLifecycleObserver, SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private var heartRateSensor: Sensor? = null

    private val heartRateValues = mutableListOf<Int>()
    private val samplingWindowMillis = 60000L  // 1 minuto para análisis
    private val handler = Handler(Looper.getMainLooper())
    private val samplingRunnable = Runnable { analyzeStress() }

    override fun onResume(owner: LifecycleOwner) {
        heartRateSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)
        if (heartRateSensor == null) {
            Log.e("MAIN_ACTIVITY", "⚠️ Sensor de ritmo cardíaco no disponible para estrés")
            viewModel.updateSensor("Estrés", "Sensor no disponible")
            return
        }
        sensorManager.registerListener(this, heartRateSensor, SensorManager.SENSOR_DELAY_NORMAL)
        handler.postDelayed(samplingRunnable, samplingWindowMillis)
        Log.d("MAIN_ACTIVITY", "✅ Sensor ritmo cardíaco registrado para estrés")
    }

    override fun onPause(owner: LifecycleOwner) {
        sensorManager.unregisterListener(this)
        handler.removeCallbacks(samplingRunnable)
        Log.d("MAIN_ACTIVITY", "⛔ Sensor de estrés desregistrado")
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (it.sensor.type == Sensor.TYPE_HEART_RATE) {
                val bpm = it.values[0].toInt()
                if (bpm > 0) {
                    heartRateValues.add(bpm)
                    Log.d("MAIN_ACTIVITY", "HR para estrés: $bpm bpm")
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No es necesario para este caso
    }

    private fun analyzeStress() {
        if (heartRateValues.size < 2) {
            viewModel.updateSensor("Estrés", "Datos insuficientes")
            scheduleNextAnalysis()
            return
        }

        // Calcula el RMSSD (Root Mean Square of Successive Differences), un indicador común de HRV
        var sumSquaredDiffs = 0.0
        for (i in 1 until heartRateValues.size) {
            val diff = heartRateValues[i] - heartRateValues[i - 1]
            sumSquaredDiffs += diff * diff
        }
        val rmssd = kotlin.math.sqrt(sumSquaredDiffs / (heartRateValues.size - 1))

        // Umbral básico para estrés (simplificado)
        val stressLevel = when {
            rmssd < 10 -> "Alto estrés"
            rmssd < 30 -> "Estrés moderado"
            else -> "Bajo estrés"
        }

        Log.d("MAIN_ACTIVITY", "RMSSD: $rmssd -> $stressLevel")
        viewModel.updateSensor("Estrés", "$stressLevel (RMSSD: %.1f)".format(rmssd))

        // Limpia datos y programa siguiente análisis
        heartRateValues.clear()
        scheduleNextAnalysis()
    }

    private fun scheduleNextAnalysis() {
        handler.postDelayed(samplingRunnable, samplingWindowMillis)
    }
}
