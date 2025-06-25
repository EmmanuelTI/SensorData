package equipo.dinamita.otys.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.lifecycle.LifecycleOwner
import equipo.dinamita.otys.presentation.sensors.HeartRateSensorManager
import equipo.dinamita.otys.presentation.wearable.MessageSender
import equipo.dinamita.otys.R


class MainActivity : ComponentActivity() {

    private lateinit var heartRateSensorManager: HeartRateSensorManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val messageSender = MessageSender(this)
        heartRateSensorManager = HeartRateSensorManager(this, lifecycle, messageSender)

        lifecycle.addObserver(heartRateSensorManager)
    }
}
