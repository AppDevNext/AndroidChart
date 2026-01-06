package info.appdev.chartexample.listviewitems

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.view.View
import info.appdev.chartexample.R
import info.appdev.charting.charts.PieChart
import info.appdev.charting.components.Legend
import info.appdev.charting.data.PieData
import info.appdev.charting.formatter.PercentFormatter
import info.appdev.charting.utils.ColorTemplate

class PieChartItem(pieData: PieData, context: Context) : ChartItem<PieData>(pieData) {
    private val typeface: Typeface? = Typeface.createFromAsset(context.assets, "OpenSans-Regular.ttf")
    private val centerText: SpannableString

    init {
        centerText = generateCenterSpannableText()
    }

    override val itemType: Int
        get() = TYPE_PIECHART

    @SuppressLint("InflateParams")
    override fun getView(position: Int, convertView: View?, context: Context?): View {
        var convertView = convertView
        val holder: ViewHolder

        if (convertView == null) {
            holder = ViewHolder()

            convertView = LayoutInflater.from(context).inflate(
                R.layout.list_item_piechart, null
            )
            holder.chart = convertView.findViewById(R.id.chart)

            convertView.tag = holder
        } else {
            holder = convertView.tag as ViewHolder
        }

        // apply styling
        holder.chart!!.description.isEnabled = false
        holder.chart!!.holeRadius = 52f
        holder.chart!!.transparentCircleRadius = 57f
        holder.chart!!.centerText = centerText
        holder.chart!!.setCenterTextTypeface(typeface)
        holder.chart!!.setCenterTextSize(9f)
        holder.chart!!.isUsePercentValues = true
        holder.chart!!.setExtraOffsets(5f, 10f, 50f, 10f)

        chartData.setValueFormatter(PercentFormatter())
        chartData.setValueTypeface(typeface)
        chartData.setValueTextSize(11f)
        chartData.setValueTextColor(Color.WHITE)
        // set data
        holder.chart?.data = chartData

        holder.chart?.legend?.apply {
            verticalAlignment = Legend.LegendVerticalAlignment.TOP
            horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
            orientation = Legend.LegendOrientation.VERTICAL
            setDrawInside(false)
            yEntrySpace = 0f
            yOffset = 0f
        }

        // do not forget to refresh the chart
        // holder.chart.invalidate();
        holder.chart?.animateY(900)

        return convertView
    }

    private fun generateCenterSpannableText(): SpannableString {
        val spannable = SpannableString("AndroidChart\ndeveloped by AppDevNext")
        spannable.setSpan(RelativeSizeSpan(1.7f), 0, 14, 0)
        spannable.setSpan(StyleSpan(Typeface.NORMAL), 14, spannable.length - 15, 0)
        spannable.setSpan(ForegroundColorSpan(Color.GRAY), 14, spannable.length - 15, 0)
        spannable.setSpan(RelativeSizeSpan(.8f), 14, spannable.length - 15, 0)
        spannable.setSpan(StyleSpan(Typeface.ITALIC), spannable.length - 14, spannable.length, 0)
        spannable.setSpan(ForegroundColorSpan(ColorTemplate.holoBlue), spannable.length - 14, spannable.length, 0)
        return spannable
    }

    private class ViewHolder {
        var chart: PieChart? = null
    }
}
