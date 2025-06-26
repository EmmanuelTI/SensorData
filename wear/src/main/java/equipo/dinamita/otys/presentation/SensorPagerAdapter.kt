package equipo.dinamita.otys.presentation

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter



class SensorPagerAdapter(activity: FragmentActivity, private val data: List<Pair<String, String>>) :
    FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = data.size

    override fun createFragment(position: Int): Fragment {
        val (title, value) = data[position]
        return DataFragment.newInstance(title, value)
    }
}

