package equipo.dinamita.otys.presentation

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import equipo.dinamita.otys.R

class DataFragment : Fragment() {

    private var title: String? = null
    private lateinit var valueView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            title = it.getString(ARG_TITLE)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_data, container, false)

        val titleView = view.findViewById<TextView>(R.id.tv_title)
        valueView = view.findViewById(R.id.tv_value)

        titleView.text = title

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
        valueView.setTextColor(resources.getColor(R.color.gray, null))

        animateValueView(valueView)

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewModel = ViewModelProvider(requireActivity())[SensorViewModel::class.java]

        viewModel.sensorData.observe(viewLifecycleOwner) { data ->
            val updatedValue = data.find { it.first == title }?.second ?: "--"
            valueView.text = updatedValue
        }
    }

    private fun animateValueView(view: View) {
        val scaleX = ObjectAnimator.ofFloat(view, View.SCALE_X, 1f, 1.3f).apply {
            duration = 1250
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }
        val scaleY = ObjectAnimator.ofFloat(view, View.SCALE_Y, 1f, 1.3f).apply {
            duration = 1250
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }
        scaleX.start()
        scaleY.start()
    }

    companion object {
        private const val ARG_TITLE = "title"

        fun newInstance(title: String) = DataFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_TITLE, title)
            }
        }
    }
}
