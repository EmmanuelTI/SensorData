package equipo.dinamita.otys.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import equipo.dinamita.otys.R
import equipo.dinamita.otys.presentation.sensors.HeartRateSensorManager
import equipo.dinamita.otys.presentation.wearable.MessageSender

class MainActivity : ComponentActivity() {

    private lateinit var heartRateSensorManager: HeartRateSensorManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicializa el MessageSender para mandar datos a la app móvil
        val messageSender = MessageSender(context = this)

        // Inicializa el sensor manager y pásale el MessageSender
        heartRateSensorManager = HeartRateSensorManager(
            context = this,
            lifecycle = lifecycle,
            messageSender = messageSender
        )

        // Vincula el sensor al ciclo de vida de la Activity
        lifecycle.addObserver(heartRateSensorManager)
    }
}
