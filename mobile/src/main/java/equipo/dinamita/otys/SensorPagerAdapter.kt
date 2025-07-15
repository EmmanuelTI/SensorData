package equipo.dinamita.otys

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import androidx.core.graphics.toColorInt



class SensorPagerAdapter(
    private val sensors: List<SensorData>
) : RecyclerView.Adapter<SensorPagerAdapter.SensorViewHolder>() {

    inner class SensorViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val pieChart: PieChart = view.findViewById(R.id.pieChart)
        val tvSensorValue: TextView = view.findViewById(R.id.tvSensorValue)
        val tvSensorName: TextView = view.findViewById(R.id.tvSensorName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SensorViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_sensor_chart, parent, false)
        return SensorViewHolder(view)
    }

    override fun onBindViewHolder(holder: SensorViewHolder, position: Int) {
        val sensor = sensors[position]

        if (sensor.name == "GPS") {
            holder.tvSensorValue.text = sensor.extra  // Mostrar coordenadas
            setupPieChart(holder.pieChart, 0)         // PieChart vacío o fijo
        } else {
            holder.tvSensorValue.text = sensor.value.toString()
            setupPieChart(holder.pieChart, sensor.value)
        }

        holder.tvSensorName.text = sensor.name
    }


    override fun getItemCount(): Int = sensors.size

    private fun setupPieChart(chart: PieChart, value: Int) {
        chart.setUsePercentValues(false)
        chart.description.isEnabled = false
        chart.isDrawHoleEnabled = true
        chart.setHoleColor(Color.WHITE)
        chart.holeRadius = 70f
        chart.transparentCircleRadius = 75f
        chart.setDrawCenterText(true)
        chart.centerText = "$value"
        chart.setCenterTextSize(36f)
        chart.setCenterTextColor(Color.BLACK)
        chart.legend.isEnabled = false
        chart.setTouchEnabled(false)


        val entries = ArrayList<PieEntry>()
        entries.add(PieEntry(value.toFloat()))
        entries.add(PieEntry((100 - value).toFloat()))

        val dataSet = PieDataSet(entries, "")
        dataSet.setDrawValues(false)
        dataSet.colors = listOf("#3F51B5".toColorInt(), Color.LTGRAY)

        val data = PieData(dataSet)
        chart.data = data
        chart.invalidate()

    }


}
