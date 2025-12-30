package info.appdev.chartexample.listviewitems

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import info.appdev.charting.charts.LineChart
import info.appdev.charting.components.XAxis.XAxisPosition
import info.appdev.charting.data.ChartData
import info.appdev.charting.data.LineData
import info.appdev.chartexample.R

class LineChartItem(chartData: ChartData<*>, context: Context) : ChartItem(chartData) {
    private val typeface: Typeface? = Typeface.createFromAsset(context.assets, "OpenSans-Regular.ttf")

    override val itemType: Int
        get() = TYPE_LINECHART

    @SuppressLint("InflateParams")
    override fun getView(position: Int, convertView: View?, context: Context?): View {
        var convertView = convertView
        val holder: ViewHolder

        if (convertView == null) {
            holder = ViewHolder()

            convertView = LayoutInflater.from(context).inflate(
                R.layout.list_item_linechart, null
            )
            holder.chart = convertView.findViewById(R.id.chart)

            convertView.tag = holder
        } else {
            holder = convertView.tag as ViewHolder
        }

        // apply styling
        // holder.chart.setValueTypeface(mTf);
        holder.chart!!.description.isEnabled = false
        holder.chart!!.setDrawGridBackground(false)

        val xAxis = holder.chart!!.xAxis
        xAxis.position = XAxisPosition.BOTTOM
        xAxis.typeface = typeface
        xAxis.setDrawGridLines(false)
        xAxis.setDrawAxisLine(true)

        val leftAxis = holder.chart!!.axisLeft
        leftAxis.typeface = typeface
        leftAxis.setLabelCount(5, false)
        leftAxis.axisMinimum = 0f // this replaces setStartAtZero(true)

        val rightAxis = holder.chart!!.axisRight
        rightAxis.typeface = typeface
        rightAxis.setLabelCount(5, false)
        rightAxis.setDrawGridLines(false)
        rightAxis.axisMinimum = 0f // this replaces setStartAtZero(true)

        holder.chart!!.data = chartData

        // do not forget to refresh the chart
        // holder.chart.invalidate();
        holder.chart!!.animateX(750)

        return convertView
    }

    private class ViewHolder {
        var chart: LineChart? = null
    }
}
