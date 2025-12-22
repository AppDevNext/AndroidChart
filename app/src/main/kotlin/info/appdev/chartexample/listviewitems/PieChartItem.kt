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
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.ChartData
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import info.appdev.chartexample.R

class PieChartItem(cd: ChartData<*>, c: Context) : ChartItem(cd) {
    private val typeface: Typeface? = Typeface.createFromAsset(c.assets, "OpenSans-Regular.ttf")
    private val centerText: SpannableString

    init {
        centerText = generateCenterSpannableText()
    }

    override val itemType: Int
        get() = TYPE_PIECHART

    @SuppressLint("InflateParams")
    override fun getView(position: Int, convertView: View?, c: Context?): View {
        var convertView = convertView
        val holder: ViewHolder

        if (convertView == null) {
            holder = ViewHolder()

            convertView = LayoutInflater.from(c).inflate(
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
        holder.chart!!.setUsePercentValues(true)
        holder.chart!!.setExtraOffsets(5f, 10f, 50f, 10f)

        chartData.setValueFormatter(PercentFormatter())
        chartData.setValueTypeface(typeface)
        chartData.setValueTextSize(11f)
        chartData.setValueTextColor(Color.WHITE)
        // set data
        holder.chart!!.setData(chartData as PieData?)

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
