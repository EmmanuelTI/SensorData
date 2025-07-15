package equipo.dinamita.otys

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.viewpager2.widget.ViewPager2

class MainActivity : AppCompatActivity() {

    private val sensorsMutable = mutableListOf(
        SensorData("Ritmo Cardíaco", 0),
        SensorData("Giroscopio", 0),
        SensorData("Acelerómetro", 0),
        SensorData("GPS",0)
    )

    private lateinit var adapter: SensorPagerAdapter
    private lateinit var viewPager: ViewPager2

    private val sensorDataReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val data = intent?.getStringExtra("sensorData") ?: return

            // Formato esperado: "sensorName:value1,value2,..."
            val parts = data.split(":")
            if (parts.size != 2) return

            val sensorName = parts[0]
            val valueStr = parts[1]

            // Tomamos solo el primer valor numérico y convertimos a Int para mostrar
            val firstValue = valueStr.split(",")[0].toFloatOrNull()?.toInt() ?: 0

            val index = sensorsMutable.indexOfFirst { it.name == sensorName }
            if (index != -1) {
                sensorsMutable[index] = sensorsMutable[index].copy(value = firstValue)
                adapter.notifyItemChanged(index)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        adapter = SensorPagerAdapter(sensorsMutable)
        viewPager = findViewById(R.id.viewPagerSensors)
        viewPager.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(sensorDataReceiver, IntentFilter("SENSOR_DATA_UPDATE"))
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(sensorDataReceiver)
    }
}
