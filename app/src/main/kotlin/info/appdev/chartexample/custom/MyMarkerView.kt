package info.appdev.chartexample.custom

import android.annotation.SuppressLint
import android.content.Context
import android.widget.TextView
import info.appdev.chartexample.R
import info.appdev.charting.components.MarkerView
import info.appdev.charting.data.CandleEntryFloat
import info.appdev.charting.data.EntryFloat
import info.appdev.charting.highlight.Highlight
import info.appdev.charting.utils.PointF
import info.appdev.charting.utils.formatNumber

/**
 * Custom implementation of the MarkerView.
 */
@SuppressLint("ViewConstructor")
class MyMarkerView(context: Context?, layoutResource: Int) : MarkerView(context, layoutResource) {
    private val tvContent: TextView = findViewById(R.id.tvContent)

    // runs every time the MarkerView is redrawn, can be used to update the content (user-interface)

    override fun refreshContent(entryFloat: EntryFloat, highlight: Highlight) {
        if (entryFloat is CandleEntryFloat) {
            tvContent.text = entryFloat.high.formatNumber(0, true)
        } else {
            tvContent.text = entryFloat.y.formatNumber(0, true)
        }

        super.refreshContent(entryFloat, highlight)
    }

    override var offset: PointF = PointF()
        get() = PointF(-(width / 2).toFloat(), -height.toFloat())
}
