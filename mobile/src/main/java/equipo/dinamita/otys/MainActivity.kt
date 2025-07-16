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
import equipo.dinamita.otys.dbsqlite.SensorDatabaseHelper
import equipo_dinamita.otys.firebase.FirestoreManager

class MainActivity : AppCompatActivity() {

    private val sensorsMutable = mutableListOf(
        SensorData("Ritmo Cardíaco", 0),
        SensorData("Giroscopio", extra = ""),
        SensorData("Acelerómetro", extra = "")
    )

    private lateinit var databaseHelper: SensorDatabaseHelper

    private lateinit var firestoreManager: FirestoreManager

    private lateinit var adapter: SensorPagerAdapter
    private lateinit var viewPager: ViewPager2
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var mapContainer: FrameLayout
    private var mapView: MapView? = null

    private var currentLatitude = 19.4326
    private var currentLongitude = -99.1332

    private val handler = android.os.Handler()
    private val queryIntervalMs = 1 * 60 * 1000L // 5 minutos en milisegundos

    private val periodicQueryRunnable = object : Runnable {
        override fun run() {
            // Aquí haces la consulta periódica
            val allSensorData = databaseHelper.getAllSensorData()

            // Limitar a 20 últimos registros (ya que tu query ordena por timestamp DESC)
            val last20 = allSensorData.take(20)

            Log.d("MainActivity", "Mostrando los últimos ${last20.size} registros:")
            // Aquí puedes hacer algo con la lista, como actualizar UI, enviar a adapter, etc.

            for (record in last20) {
                Log.d("MainActivity", "ID: ${record.id}, Sensor: ${record.sensorName}, Valor: ${record.value}, Timestamp: ${record.timestamp}")
            }

            // Volver a ejecutar después del intervalo
            handler.postDelayed(this, queryIntervalMs)
        }
    }

    private val sensorDataReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val data = intent?.getStringExtra("sensorData") ?: return

            // Dividir el mensaje completo en partes por ";"
            val parts = data.split(";")
            for (part in parts) {
                val subParts = part.split(":")
                if (subParts.size != 2) continue

                val sensorName = subParts[0]
                val valueStr = subParts[1]

                if (sensorName == "GPS") {
                    val coords = valueStr.split(",")
                    if (coords.size == 2) {
                        currentLatitude = coords[0].toDoubleOrNull() ?: currentLatitude
                        currentLongitude = coords[1].toDoubleOrNull() ?: currentLongitude
                        updateMapLocation()
                    }
                    continue // GPS no va en ViewPager
                }

                when (sensorName) {
                    "Giroscopio", "Acelerómetro" -> {
                        val index = sensorsMutable.indexOfFirst { it.name == sensorName }
                        if (index != -1) {
                            sensorsMutable[index] = sensorsMutable[index].copy(extra = valueStr)
                            adapter.notifyItemChanged(index)
                        }
                    }
                    "Ritmo Cardíaco" -> {
                        val numberRegex = Regex("""\d+""")
                        val firstValue = numberRegex.find(valueStr)?.value?.toIntOrNull() ?: 0
                        val index = sensorsMutable.indexOfFirst { it.name == sensorName }
                        if (index != -1) {
                            sensorsMutable[index] = sensorsMutable[index].copy(value = firstValue)
                            adapter.notifyItemChanged(index)
                        }
                    }
                }
                // Guardar TODOS los sensores en la base de datos
                databaseHelper.insertSensorData(sensorName, valueStr)
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Mostrar bases de datos existentes
        val dbList = databaseList()
        for (dbName in dbList) {
            Log.d("DATABASES", "Base de datos encontrada: $dbName")
        }

        Configuration.getInstance()
            .load(this, androidx.preference.PreferenceManager.getDefaultSharedPreferences(this))

        setContentView(R.layout.activity_main)

        databaseHelper = SensorDatabaseHelper(this)
        //Elimina todas las db existentes con sus journals conservando solo la db usada
        databaseHelper.deleteOtherDatabasesAndJournals()

        //Inicializa FirestoreManager
        firestoreManager = FirestoreManager(this)
        // Iniciar la subida inicial a Firestore
        firestoreManager.uploadAllSensorDataToFirestore()

        // Iniciar la consulta periódica
        handler.post(periodicQueryRunnable)

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
                    true
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

            map.overlays.clear()

            val marker = Marker(map)
            marker.position = geoPoint
            marker.title = "Última ubicación"
            map.overlays.add(marker)

            map.invalidate()
        }
    }
}
