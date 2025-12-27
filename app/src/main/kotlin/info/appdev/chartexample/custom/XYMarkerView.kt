package info.appdev.chartexample.custom

import android.annotation.SuppressLint
import android.content.Context
import android.widget.TextView
import info.appdev.charting.components.MarkerView
import info.appdev.charting.data.Entry
import info.appdev.charting.formatter.IAxisValueFormatter
import info.appdev.charting.highlight.Highlight
import info.appdev.charting.utils.PointF
import info.appdev.chartexample.R
import java.text.DecimalFormat

/**
 * Custom implementation of the MarkerView.
 */
@SuppressLint("ViewConstructor")
class XYMarkerView(context: Context?, private val xAxisValueFormatter: IAxisValueFormatter) : MarkerView(context, R.layout.custom_marker_view) {
    private val tvContent: TextView = findViewById(R.id.tvContent)

    private val format: DecimalFormat = DecimalFormat("###.0")

    // runs every time the MarkerView is redrawn, can be used to update the content (user-interface)
    override fun refreshContent(entry: Entry, highlight: Highlight) {
        tvContent.text = String.format("x: %s, y: %s", xAxisValueFormatter.getFormattedValue(entry.x, null), format.format(entry.y.toDouble()))

        super.refreshContent(entry, highlight)
    }

    override var offset: PointF = PointF()
        get() = PointF(-(width / 2).toFloat(), -height.toFloat())
}
