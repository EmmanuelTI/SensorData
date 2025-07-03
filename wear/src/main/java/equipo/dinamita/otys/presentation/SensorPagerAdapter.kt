package equipo.dinamita.otys.presentation

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter

class SensorPagerAdapter(
    activity: FragmentActivity,
    private var data: List<Pair<String, String>>
) : FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = data.size

    override fun createFragment(position: Int): Fragment {
        val title = data[position].first
        return DataFragment.newInstance(title)
    }

    fun updateData(newData: List<Pair<String, String>>) {
        data = newData
        notifyDataSetChanged()
    }

    override fun getItemId(position: Int): Long {
        // Devuelve un ID único basado en el título del sensor
        return data[position].first.hashCode().toLong()
    }

    override fun containsItem(itemId: Long): Boolean {
        return data.any { it.first.hashCode().toLong() == itemId }
    }
}
