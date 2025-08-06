package equipo.dinamita.otys

import android.content.*
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import equipo.dinamita.otys.dbsqlite.SensorDatabaseHelper
import equipo_dinamita.otys.firebase.FirestoreManager
import equipo.dinamita.otys.presentation.alert.EmergencyDetector
import android.widget.Toast


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
    private val queryIntervalMs = 1 * 60 * 1000L

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var topAppBar: MaterialToolbar

    private lateinit var emergencyDetector: EmergencyDetector

    private val periodicQueryRunnable = object : Runnable {
        override fun run() {
            val last20 = databaseHelper.getAllSensorData().take(20)
            Log.d("MainActivity", "Últimos ${last20.size} registros:")
            last20.forEach {
                Log.d("MainActivity", "ID: ${it.id}, Sensor: ${it.sensorName}, Valor: ${it.value}, Timestamp: ${it.timestamp}")
            }
            handler.postDelayed(this, queryIntervalMs)
        }
    }

    private val sensorDataReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val data = intent?.getStringExtra("sensorData") ?: return
            val parts = data.split(";")
            for (part in parts) {
                val (sensorName, valueStr) = part.split(":").takeIf { it.size == 2 } ?: continue
                if (sensorName == "GPS") {
                    val coords = valueStr.split(",")
                    if (coords.size == 2) {
                        currentLatitude = coords[0].toDoubleOrNull() ?: currentLatitude
                        currentLongitude = coords[1].toDoubleOrNull() ?: currentLongitude
                        updateMapLocation()
                    }
                    continue
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
                        val value = Regex("""\d+""").find(valueStr)?.value?.toIntOrNull() ?: 0
                        val index = sensorsMutable.indexOfFirst { it.name == sensorName }
                        if (index != -1) {
                            sensorsMutable[index] = sensorsMutable[index].copy(value = value)
                            adapter.notifyItemChanged(index)

                            emergencyDetector.processHeartRate(value.toFloat())
                        }
                    }
                }

                // Guarda solo en la base de datos local SQLite
                databaseHelper.insertSensorData(sensorName, valueStr)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Configuration.getInstance()
            .load(this, androidx.preference.PreferenceManager.getDefaultSharedPreferences(this))

        setContentView(R.layout.activity_main)

        topAppBar = findViewById(R.id.topAppBar)
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        cargarNombreUsuario()
        setupTopAppBar()

        databaseHelper = SensorDatabaseHelper(this)
        databaseHelper.deleteOtherDatabasesAndJournals()

        firestoreManager = FirestoreManager(this)

        emergencyDetector = EmergencyDetector(this)

        if (isUserLoggedIn()) {
            firestoreManager.uploadAllSensorDataToFirestore()
        } else {
            Log.d("MainActivity", "Usuario no logueado: no se subirán datos a Firestore")
        }

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

    private fun cargarNombreUsuario() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            firestore.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val name = document.getString("name")
                        topAppBar.title = if (!name.isNullOrEmpty()) "Hola, $name" else "Hola, usuario"
                    } else {
                        topAppBar.title = "Hola, usuario"
                    }
                }
                .addOnFailureListener {
                    topAppBar.title = "Hola, usuario"
                }
        } else {
            topAppBar.title = "Bienvenido"
        }
    }

    private fun setupTopAppBar() {
        topAppBar.setNavigationOnClickListener { view ->
            val popupMenu = androidx.appcompat.widget.PopupMenu(this, view)
            popupMenu.menuInflater.inflate(R.menu.menu_user_actions, popupMenu.menu)

            val isLoggedIn = FirebaseAuth.getInstance().currentUser != null
            popupMenu.menu.findItem(R.id.menu_login).isVisible = !isLoggedIn
            popupMenu.menu.findItem(R.id.menu_register).isVisible = !isLoggedIn
            popupMenu.menu.findItem(R.id.menu_logout).isVisible = isLoggedIn

            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.menu_login -> {
                        startActivity(Intent(this, LoginActivity::class.java))
                        true
                    }
                    R.id.menu_register -> {
                        startActivity(Intent(this, RegisterActivity::class.java))
                        true
                    }
                    R.id.menu_logout -> {
                        FirebaseAuth.getInstance().signOut()
                        updateUIForLoggedOutUser()
                        true
                    }


                    else -> false
                }
            }
            popupMenu.show()
        }
    }
    private fun updateUIForLoggedOutUser() {
        topAppBar.title = "Bienvenido"
        Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show()
        // Oculta botones o funcionalidades que requieran login, si es necesario
    }

    private fun isUserLoggedIn(): Boolean {
        return FirebaseAuth.getInstance().currentUser != null
    }

    override fun onResume() {
        super.onResume()
        LocalBroadcastManager.getInstance(this).registerReceiver(sensorDataReceiver, IntentFilter("SENSOR_DATA_UPDATE"))
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
            mapView = MapView(this).apply {
                setTileSource(TileSourceFactory.MAPNIK)
                controller.setZoom(15.0)
            }
            mapContainer.addView(mapView)
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

            val marker = Marker(map).apply {
                position = geoPoint
                title = "Última ubicación"
            }
            map.overlays.add(marker)
            map.invalidate()
        }
    }
}
