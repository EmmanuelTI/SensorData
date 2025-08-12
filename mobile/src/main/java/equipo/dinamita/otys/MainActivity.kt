package equipo.dinamita.otys

import android.content.IntentFilter
import android.os.Bundle
import android.util.Log
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import equipo.dinamita.otys.InternetConnection.InternetUtil
import equipo.dinamita.otys.adapters.SensorPagerAdapter
import equipo.dinamita.otys.auth.SessionManager
import equipo.dinamita.otys.components.EmergencyDetector
import equipo.dinamita.otys.data.SensorData
import equipo.dinamita.otys.dbsqlite.SensorDatabaseHelper
import equipo.dinamita.otys.dbsqlite.model.SensorRecord
import equipo.dinamita.otys.map.MapController
import equipo.dinamita.otys.sensors.SensorDataReceiver
import equipo.dinamita.otys.ui.UserMenuManager
import equipo_dinamita.otys.firebase.FirestoreManager
import org.osmdroid.config.Configuration

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
    private lateinit var mapController: MapController

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var topAppBar: MaterialToolbar
    private lateinit var emergencyDetector: EmergencyDetector

    private lateinit var sensorDataReceiver: SensorDataReceiver
    private lateinit var userMenuManager: UserMenuManager
    private lateinit var sessionManager: SessionManager

    private val handler = android.os.Handler()
    private val queryIntervalMs = 1 * 60 * 1000L

    private var currentLatitude = 19.4326
    private var currentLongitude = -99.1332

    private val periodicQueryRunnable = object : Runnable {
        override fun run() {
            val last20 = databaseHelper.getAllSensorData().take(20)
            val totalRecords = databaseHelper.getAllSensorData()
            Log.d("MainActivity", "Cantidad de registros en la db: ${totalRecords.size}")
            Log.d("MainActivity", "Últimos ${last20.size} registros:")
            last20.forEach {
                Log.d("MainActivity", "ID: ${it.id}, Sensor: ${it.sensorName}, Valor: ${it.value}, Timestamp: ${it.timestamp}")
            }
            handler.postDelayed(this, queryIntervalMs)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Configuration.getInstance()
            .load(this, androidx.preference.PreferenceManager.getDefaultSharedPreferences(this))

        setContentView(R.layout.activity_main)

        // Views
        topAppBar = findViewById(R.id.topAppBar)
        viewPager = findViewById(R.id.viewPagerSensors)
        bottomNav = findViewById(R.id.bottomNavigationView)
        mapContainer = findViewById(R.id.mapContainer)

        // Firebase
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Helpers and Managers
        databaseHelper = SensorDatabaseHelper(this)
        databaseHelper.deleteOtherDatabasesAndJournals()
        firestoreManager = FirestoreManager(this)
        emergencyDetector = EmergencyDetector(this)

        // Session and UI managers
        sessionManager = SessionManager(topAppBar)


        userMenuManager = UserMenuManager(this, topAppBar)



        // Adapter for sensors
        adapter = SensorPagerAdapter(sensorsMutable)
        viewPager.adapter = adapter

        // MapController (viewPager is the view to hide when map shows)
        mapController = MapController(this, mapContainer, viewPager)

        cargarNombreUsuario()

        if (sessionManager.isUserLoggedIn()) {
            firestoreManager.uploadAllSensorDataToFirestore()
        } else {
            Log.d("MainActivity", "Usuario no logueado: no se subirán datos a Firestore")
        }

        // SensorDataReceiver: define lambdas for handling sensor data and GPS updates
        sensorDataReceiver = SensorDataReceiver(
            onSensorDataReceived = { sensorName, valueStr ->
                when (sensorName) {
                    "Giroscopio", "Acelerómetro" -> {
                        val index = sensorsMutable.indexOfFirst { it.name == sensorName }
                        if (index != -1) {
                            sensorsMutable[index] = sensorsMutable[index].copy(extra = valueStr)
                            adapter.notifyItemChanged(index)
                        }
                    }
                    "Ritmo Cardíaco" -> {
                        val value = Regex("""\d+""").find(valueStr)?.value?.toIntOrNull() ?: 0
                        val index = sensorsMutable.indexOfFirst { it.name == sensorName }
                        if (index != -1) {
                            sensorsMutable[index] = sensorsMutable[index].copy(value = value)
                            adapter.notifyItemChanged(index)
                            emergencyDetector.processHeartRate(value.toFloat())
                        }
                    }
                }

                // Guardar en base de datos o subir a Firestore, enviando la ubicación actual
                if (InternetUtil.isInternetAvailable(this)) {
                    val record = SensorRecord(
                        id = 0,
                        sensorName = sensorName,
                        value = valueStr,
                        timestamp = System.currentTimeMillis().toString()
                    )
                    firestoreManager.uploadRecordWithLocation(record, currentLatitude, currentLongitude)
                } else {
                    databaseHelper.insertSensorData(sensorName, valueStr)
                }
            },
            onGpsDataReceived = { lat, lon ->
                currentLatitude = lat
                currentLongitude = lon
                emergencyDetector.currentLocation = Pair(lat, lon)
                mapController.updateMapLocation(lat, lon)

            }
        )

        // Bottom nav listener para mostrar/ocultar mapa
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_gps -> {
                    mapController.showMap(currentLatitude, currentLongitude)
                    true
                }
                else -> {
                    mapController.hideMap()
                    true
                }
            }
        }

        // Configurar menú usuario
        topAppBar.setNavigationOnClickListener {
            userMenuManager.showMenu()
        }

        // Iniciar tareas periódicas
        handler.post(periodicQueryRunnable)
    }

    private fun cargarNombreUsuario() {
        val currentUser = auth.currentUser
        topAppBar.title = if (currentUser != null) {
            "Hola, ${currentUser.displayName ?: "usuario"}"
        } else {
            "Bienvenido"
        }
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(this).registerReceiver(sensorDataReceiver, IntentFilter("SENSOR_DATA_UPDATE"))
        mapController.onResume()
    }

    override fun onPause() {
        super.onPause()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(sensorDataReceiver)
        mapController.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapController.onDestroy()
    }
}
