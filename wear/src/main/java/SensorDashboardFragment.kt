package equipo.dinamita.otys.presentation

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.viewpager2.widget.ViewPager2
import equipo.dinamita.otys.R

class SensorDashboardFragment : Fragment(R.layout.fragment_sensor_dashboard) {

    private val viewModel: SensorViewModel by activityViewModels()
    private lateinit var adapter: SensorPagerAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val viewPager = view.findViewById<ViewPager2>(R.id.viewPager)
        adapter = SensorPagerAdapter(requireActivity(), emptyList())
        viewPager.adapter = adapter

        viewModel.sensorData.observe(viewLifecycleOwner, Observer { newList ->
            adapter.updateData(newList)
        })
    }
}
