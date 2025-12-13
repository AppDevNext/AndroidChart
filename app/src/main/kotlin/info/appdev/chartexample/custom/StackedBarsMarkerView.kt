package info.appdev.chartexample.custom

import android.annotation.SuppressLint
import android.content.Context
import android.widget.TextView
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import com.github.mikephil.charting.utils.formatNumber
import info.appdev.chartexample.R

/**
 * Custom implementation of the MarkerView.
 */
@Suppress("unused")
@SuppressLint("ViewConstructor")
class StackedBarsMarkerView(context: Context?, layoutResource: Int) : MarkerView(context, layoutResource) {
    private val tvContent: TextView = findViewById(R.id.tvContent)

    // runs every time the MarkerView is redrawn, can be used to update the
    // content (user-interface)
    override fun refreshContent(entry: Entry, highlight: Highlight) {
        if (entry is BarEntry) {

            if (entry.yVals != null) {
                // draw the stack value
                tvContent.text = entry.yVals!![highlight.stackIndex].formatNumber(0, true)
            } else {
                tvContent.text = entry.y.formatNumber(0, true)
            }
        } else {
            tvContent.text = entry.y.formatNumber(0, true)
        }

        super.refreshContent(entry, highlight)
    }

    override var offset: MPPointF = MPPointF()
        get() = MPPointF(-(width / 2).toFloat(), -height.toFloat())
}
