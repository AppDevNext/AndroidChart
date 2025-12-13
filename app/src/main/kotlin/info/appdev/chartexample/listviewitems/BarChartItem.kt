package info.appdev.chartexample.listviewitems

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis.XAxisPosition
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.ChartData
import info.appdev.chartexample.R

class BarChartItem(cd: ChartData<*>, c: Context) : ChartItem(cd) {
    private val typeface: Typeface? = Typeface.createFromAsset(c.assets, "OpenSans-Regular.ttf")

    override val itemType: Int
        get() = TYPE_BARCHART

    @SuppressLint("InflateParams")
    override fun getView(position: Int, convertView: View?, c: Context?): View? {
        var convertView = convertView
        val holder: ViewHolder

        if (convertView == null) {
            holder = ViewHolder()

            convertView = LayoutInflater.from(c).inflate(
                R.layout.list_item_barchart, null
            )
            holder.chart = convertView.findViewById(R.id.chart)

            convertView.tag = holder
        } else {
            holder = convertView.tag as ViewHolder
        }

        // apply styling
        holder.chart!!.description.isEnabled = false
        holder.chart!!.setDrawGridBackground(false)
        holder.chart!!.setDrawBarShadow(false)

        val xAxis = holder.chart!!.xAxis
        xAxis.position = XAxisPosition.BOTTOM
        xAxis.typeface = typeface
        xAxis.setDrawGridLines(false)
        xAxis.setDrawAxisLine(true)

        val leftAxis = holder.chart!!.axisLeft
        leftAxis.typeface = typeface
        leftAxis.setLabelCount(5, false)
        leftAxis.spaceTop = 20f
        leftAxis.axisMinimum = 0f // this replaces setStartAtZero(true)

        val rightAxis = holder.chart!!.axisRight
        rightAxis.typeface = typeface
        rightAxis.setLabelCount(5, false)
        rightAxis.spaceTop = 20f
        rightAxis.axisMinimum = 0f // this replaces setStartAtZero(true)

        chartData.setValueTypeface(typeface)

        // set data
        holder.chart!!.setData(chartData as BarData?)
        holder.chart!!.setFitBars(true)

        // do not forget to refresh the chart
//        holder.chart.invalidate();
        holder.chart!!.animateY(700)

        return convertView
    }

    private class ViewHolder {
        var chart: BarChart? = null
    }
}
