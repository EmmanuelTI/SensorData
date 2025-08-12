package equipo.dinamita.otys.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import equipo.dinamita.otys.R
import equipo.dinamita.otys.data.SensorData

class SensorPagerAdapter(
    private val sensors: List<SensorData>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_PIE = 0
    private val TYPE_BAR = 1

    override fun getItemViewType(position: Int): Int {
        val sensor = sensors[position]
        return when (sensor.name.lowercase()) {
            "ritmo cardíaco", "heart rate" -> TYPE_PIE
            else -> TYPE_BAR
        }
    }

    inner class PieChartViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val pieChart: PieChart = view.findViewById(R.id.pieChart)
        val tvSensorValue: TextView = view.findViewById(R.id.tvSensorValue)
        val tvSensorName: TextView = view.findViewById(R.id.tvSensorName)
    }

    inner class BarChartViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val barChart: BarChart = view.findViewById(R.id.barChart)
        val tvSensorXYZ: TextView = view.findViewById(R.id.tvSensorXYZ)
        val tvSensorName: TextView = view.findViewById(R.id.tvSensorName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_PIE) {
            val view = inflater.inflate(R.layout.item_sensor_chart, parent, false)
            PieChartViewHolder(view)
        } else {
            val view = inflater.inflate(R.layout.item_sensor_bar, parent, false)
            BarChartViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val sensor = sensors[position]

        when (holder) {
            is PieChartViewHolder -> {
                holder.tvSensorValue.text = sensor.value.toString()
                holder.tvSensorName.text = sensor.name
                setupPieChart(holder.pieChart, sensor.value)
            }

            is BarChartViewHolder -> {
                // Simulación de valores X/Y/Z si vienen en `extra` separados por coma, como "1.2,0.4,9.8"
                val (x, y, z) = sensor.extra.split(",").mapNotNull { it.toFloatOrNull() }
                    .let { it + List(3 - it.size) { 0f } }  // Rellenar con ceros si faltan

                holder.tvSensorXYZ.text = "X: %.2f  Y: %.2f  Z: %.2f".format(x, y, z)
                holder.tvSensorName.text = sensor.name
                setupBarChart(holder.barChart, x, y, z)
            }
        }
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

        val entries = listOf(
            PieEntry(value.toFloat()),
            PieEntry((100 - value).coerceAtLeast(0).toFloat())
        )

        val dataSet = PieDataSet(entries, "").apply {
            setDrawValues(false)
            colors = listOf("#3F51B5".toColorInt(), Color.LTGRAY)
        }

        chart.data = PieData(dataSet)
        chart.invalidate()
    }

    private fun setupBarChart(chart: BarChart, x: Float, y: Float, z: Float) {
        val entries = listOf(
            BarEntry(0f, x),
            BarEntry(1f, y),
            BarEntry(2f, z)
        )

        val dataSet = BarDataSet(entries, "Ejes XYZ").apply {
            colors = listOf(Color.RED, Color.GREEN, Color.BLUE)
            valueTextColor = Color.BLACK
            valueTextSize = 14f
        }

        chart.apply {
            data = BarData(dataSet)
            description.isEnabled = false
            legend.isEnabled = false
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                valueFormatter = IndexAxisValueFormatter(listOf("X", "Y", "Z"))
                granularity = 1f
                setDrawGridLines(false)
            }
            axisLeft.axisMinimum = -20f
            axisRight.isEnabled = false
            setTouchEnabled(false)
            invalidate()
        }
    }
}
