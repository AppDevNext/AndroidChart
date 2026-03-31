package info.appdev.chartexample.custom

import android.annotation.SuppressLint
import android.content.Context
import android.widget.TextView
import info.appdev.chartexample.R
import info.appdev.charting.components.MarkerView
import info.appdev.charting.data.BarEntryFloat
import info.appdev.charting.data.EntryFloat
import info.appdev.charting.highlight.Highlight
import info.appdev.charting.utils.PointF
import info.appdev.charting.utils.formatNumber

/**
 * Custom implementation of the MarkerView.
 */
@Suppress("unused")
@SuppressLint("ViewConstructor")
class StackedBarsMarkerView(context: Context?, layoutResource: Int) : MarkerView(context, layoutResource) {
    private val tvContent: TextView = findViewById(R.id.tvContent)

    // runs every time the MarkerView is redrawn, can be used to update the
    // content (user-interface)
    override fun refreshContent(entryFloat: EntryFloat, highlight: Highlight) {
        if (entryFloat is BarEntryFloat) {

            if (entryFloat.yVals != null) {
                // draw the stack value
                tvContent.text = entryFloat.yVals!![highlight.stackIndex].formatNumber(0, true)
            } else {
                tvContent.text = entryFloat.y.formatNumber(0, true)
            }
        } else {
            tvContent.text = entryFloat.y.formatNumber(0, true)
        }

        super.refreshContent(entryFloat, highlight)
    }

    override var offset: PointF = PointF()
        get() = PointF(-(width / 2).toFloat(), -height.toFloat())
}
