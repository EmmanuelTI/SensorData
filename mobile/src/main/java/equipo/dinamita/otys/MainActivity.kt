package equipo.dinamita.otys

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import equipo.dinamita.otys.data.SensorDatabaseHelper



class MainActivity : AppCompatActivity() {

    private val sensorsMutable = mutableListOf(
        SensorData("Ritmo Cardíaco", 0),
        SensorData("Giroscopio", 0),
        SensorData("Acelerómetro", 0),
        SensorData("GPS", 0)
    )

    private lateinit var databaseHelper: SensorDatabaseHelper

    private lateinit var adapter: SensorPagerAdapter
    private lateinit var viewPager: ViewPager2
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var mapContainer: FrameLayout
    private var mapView: MapView? = null

    private var currentLatitude = 0.0
    private var currentLongitude = 0.0

    private val sensorDataReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val data = intent?.getStringExtra("sensorData") ?: return

            val parts = data.split(":")
            if (parts.size != 2) return

            val sensorName = parts[0]
            val valueStr = parts[1]

            if (sensorName == "GPS") {
                val coords = valueStr.split(",")
                if (coords.size == 2) {
                    currentLatitude = coords[0].toDoubleOrNull() ?: currentLatitude
                    currentLongitude = coords[1].toDoubleOrNull() ?: currentLongitude
                    updateMapLocation()
                }
            }

            val firstValue = valueStr.split(",")[0].toFloatOrNull()?.toInt() ?: 0
            val index = sensorsMutable.indexOfFirst { it.name == sensorName }
            if (index != -1) {
                sensorsMutable[index] = sensorsMutable[index].copy(value = firstValue)
                adapter.notifyItemChanged(index)
            }

            // Guardar en base de datos
            databaseHelper.insertSensorData(sensorName, firstValue)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configura osmdroid (obligatorio)
        Configuration.getInstance()
            .load(this, androidx.preference.PreferenceManager.getDefaultSharedPreferences(this))

        setContentView(R.layout.activity_main)

        // Inicializar helper antes de usarlo
        databaseHelper = SensorDatabaseHelper(this)



        adapter = SensorPagerAdapter(sensorsMutable)
        viewPager = findViewById(R.id.viewPagerSensors)
        viewPager.adapter = adapter

        bottomNav = findViewById(R.id.bottomNavigationView)
        mapContainer = findViewById(R.id.mapContainer)

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_gps -> {
                    showMap()
                    true
                }
                else -> {
                    hideMap()
                    false
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(sensorDataReceiver, IntentFilter("SENSOR_DATA_UPDATE"))
        mapView?.onResume()
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(sensorDataReceiver)
        mapView?.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView?.onDetach()
    }

    private fun showMap() {
        viewPager.visibility = View.GONE
        mapContainer.visibility = View.VISIBLE

        if (mapView == null) {
            mapView = MapView(this)
            mapView!!.setTileSource(TileSourceFactory.MAPNIK)
            mapContainer.addView(mapView)

            mapView!!.controller.setZoom(15.0)
        }

        updateMapLocation()
    }

    private fun hideMap() {
        mapContainer.visibility = View.GONE
        viewPager.visibility = View.VISIBLE
    }

    private fun updateMapLocation() {
        mapView?.let { map ->
            val geoPoint = GeoPoint(currentLatitude, currentLongitude)
            map.controller.setCenter(geoPoint)

            // Borra marcadores previos
            map.overlays.clear()

            // Agrega marcador en la ubicación actual
            val marker = Marker(map)
            marker.position = geoPoint
            marker.title = "Última ubicación"
            map.overlays.add(marker)

            map.invalidate()
        }
    }
}
