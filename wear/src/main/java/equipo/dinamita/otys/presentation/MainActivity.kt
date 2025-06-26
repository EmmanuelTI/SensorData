package equipo.dinamita.otys.presentation

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import equipo.dinamita.otys.R

class MainActivity : AppCompatActivity() {

    companion object {
        val SENSOR_DATA = listOf(
            "Ritmo Cardíaco" to "-- bpm",
            "GPS" to "Lat: ---, Lon: ---",
            "Estrés" to "Nivel: --",
            "Giroscopio" to "X: ---, Y: ---, Z: ---",
            "Acelerómetro" to "X: ---, Y: ---, Z: ---",
            "Temperatura" to "-- °C"
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val viewPager = findViewById<ViewPager2>(R.id.viewPager)
        viewPager.adapter = SensorPagerAdapter(this, SENSOR_DATA)
    }
}

