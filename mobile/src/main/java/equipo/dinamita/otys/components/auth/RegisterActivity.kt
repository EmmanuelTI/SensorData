package equipo.dinamita.otys.components.auth

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FieldValue
import equipo.dinamita.otys.R
import java.util.*

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val edtUsername = findViewById<EditText>(R.id.edtUsernameRegister)
        val edtEmail = findViewById<EditText>(R.id.edtEmailRegister)
        val edtPassword = findViewById<EditText>(R.id.edtPasswordRegister)
        val edtDate = findViewById<EditText>(R.id.edtDateRegister)
        val edtEmergencyPhone = findViewById<EditText>(R.id.edtEmergencyPhone)
        val btnRegister = findViewById<Button>(R.id.btnRegister)

        // Selección de fecha
        edtDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            DatePickerDialog(this, { _, y, m, d ->
                val formatted = String.format("%02d/%02d/%04d", d, m + 1, y)
                edtDate.setText(formatted)
            }, year, month, day).show()
        }

        btnRegister.setOnClickListener {
            val username = edtUsername.text.toString().trim()
            val email = edtEmail.text.toString().trim()
            val password = edtPassword.text.toString().trim()
            val birthDate = edtDate.text.toString().trim()
            val phoneEmergency = edtEmergencyPhone.text.toString().trim() // NUEVO

            if (username.isEmpty() || email.isEmpty() || password.isEmpty() || birthDate.isEmpty() || phoneEmergency.isEmpty()) {
                Toast.makeText(this, "Completa todos los campos, incluido teléfono de emergencia", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(this, "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Crear usuario en Firebase Auth
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val userId = auth.currentUser?.uid ?: return@addOnCompleteListener

                        val userMap = hashMapOf(
                            "name" to username,
                            "email" to email,
                            "birthdate" to birthDate,
                            "numeroTelefono" to phoneEmergency,
                            "createdAt" to FieldValue.serverTimestamp()
                        )

                        firestore.collection("usuarios").document(userId)
                            .set(userMap)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show()
                                finish()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Error al guardar usuario: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                    } else {
                        Toast.makeText(this, "Error de autenticación: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }
}
