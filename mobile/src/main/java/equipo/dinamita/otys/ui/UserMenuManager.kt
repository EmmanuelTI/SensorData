package equipo.dinamita.otys.ui

import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import com.google.firebase.auth.FirebaseAuth
import equipo.dinamita.otys.components.auth.LoginActivity
import equipo.dinamita.otys.components.auth.RegisterActivity
import equipo.dinamita.otys.MainActivity
import equipo.dinamita.otys.R

class UserMenuManager(private val context: Context, private val anchorView: View) {

    fun showMenu() {
        val popupMenu = PopupMenu(context, anchorView)
        popupMenu.menuInflater.inflate(R.menu.menu_user_actions, popupMenu.menu)

        val isLoggedIn = FirebaseAuth.getInstance().currentUser != null
        popupMenu.menu.findItem(R.id.menu_login).isVisible = !isLoggedIn
        popupMenu.menu.findItem(R.id.menu_register).isVisible = !isLoggedIn
        popupMenu.menu.findItem(R.id.menu_logout).isVisible = isLoggedIn

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_login -> {
                    context.startActivity(Intent(context, LoginActivity::class.java))
                    true
                }
                R.id.menu_register -> {
                    context.startActivity(Intent(context, RegisterActivity::class.java))
                    true
                }
                R.id.menu_logout -> {
                    FirebaseAuth.getInstance().signOut()
                    Toast.makeText(context, "Sesión cerrada", Toast.LENGTH_SHORT).show()
                    val intent = Intent(context, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    context.startActivity(intent)
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }
}
