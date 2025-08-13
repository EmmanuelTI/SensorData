package equipo.dinamita.otys.Sincronizar
import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import equipo.dinamita.otys.R
import equipo_dinamita.otys.firebase.FirestoreManager

class SyncActivity : AppCompatActivity() {

    private lateinit var progressBar: ProgressBar
    private lateinit var tvProgress: TextView
    private lateinit var btnSync: Button

    private val firestoreManager = FirestoreManager(this) // tu clase con uploadAllSensorDataToFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.sync_layout) // el layout con LinearLayout que mostraste

        progressBar = findViewById(R.id.progressBar)
        tvProgress = findViewById(R.id.tvProgress)
        btnSync = findViewById(R.id.btnSync)

        btnSync.setOnClickListener {
            val user = FirebaseAuth.getInstance().currentUser
            if (user == null) {
                tvProgress.text = "Inicia sesión para sincronizar"
                return@setOnClickListener
            }

            btnSync.isEnabled = false
            progressBar.progress = 0
            tvProgress.text = "0%"

            firestoreManager.uploadAllSensorDataToFirestore(

                    onProgress = { current, total ->
                    val percent = (current * 100) / total
                    progressBar.progress = percent
                    tvProgress.text = "$percent%"
                },
                onComplete = {
                    btnSync.isEnabled = true
                    tvProgress.text = "Sincronización completa"
                }
            )
        }


    }
}
