package equipo.dinamita.otys.presentation

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import equipo.dinamita.otys.R

class DataFragment : Fragment() {

    private var title: String? = null
    private var value: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            title = it.getString(ARG_TITLE)
            value = it.getString(ARG_VALUE)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_data, container, false)

        val titleView = view.findViewById<TextView>(R.id.tv_title)
        val valueView = view.findViewById<TextView>(R.id.tv_value)

        // Asignar texto
        titleView.text = title
        valueView.text = value

        // Cambiar colores dinámicamente solo en el título
        val titleColor = when (title) {
            "Ritmo Cardíaco" -> resources.getColor(R.color.red, null)
            "GPS" -> resources.getColor(R.color.blue, null)
            "Estrés" -> resources.getColor(R.color.magenta, null)
            "Giroscopio" -> resources.getColor(R.color.green, null)
            "Acelerómetro" -> resources.getColor(R.color.cyan, null)
            "Temperatura" -> resources.getColor(R.color.yellow, null)
            else -> resources.getColor(R.color.white, null)
        }

        titleView.setTextColor(titleColor)

        // Mantener valores en gris o blanco
        valueView.setTextColor(resources.getColor(R.color.gray, null)) // Gris o blanco

        // Animación para todos los valores
        val scaleX = ObjectAnimator.ofFloat(valueView, View.SCALE_X, 1f, 1.3f).apply {
            duration = 1250
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }
        val scaleY = ObjectAnimator.ofFloat(valueView, View.SCALE_Y, 1f, 1.3f).apply {
            duration = 1250
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }
        scaleX.start()
        scaleY.start()

        return view
    }

    companion object {
        private const val ARG_TITLE = "title"
        private const val ARG_VALUE = "value"

        fun newInstance(title: String, value: String) = DataFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_TITLE, title)
                putString(ARG_VALUE, value)
            }
        }
    }
}
