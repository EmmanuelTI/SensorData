package equipo.dinamita.otys.auth

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.android.material.appbar.MaterialToolbar

class SessionManager(private val toolbar: MaterialToolbar) {

    private val auth = FirebaseAuth.getInstance()

    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    fun loadUserName() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            Log.d("SessionManager", "UID actual: ${currentUser.uid}")
            toolbar.title = "Hola, ${currentUser.displayName ?: "usuario"}"
        } else {
            toolbar.title = "Bienvenido"
        }
    }
}
