package info.appdev.chartexample.custom

import android.annotation.SuppressLint
import android.content.Context
import android.widget.TextView
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.CandleEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import com.github.mikephil.charting.utils.Utils.formatNumber
import info.appdev.chartexample.R

/**
 * Custom implementation of the MarkerView.
 *
 * @author Philipp Jahoda
 */
@SuppressLint("ViewConstructor")
class MyMarkerView(context: Context, layoutResource: Int) : MarkerView(context, layoutResource) {
    private val tvContent = findViewById<TextView?>(R.id.tvContent)

    // runs every time the MarkerView is redrawn, can be used to update the
    // content (user-interface)
    override fun refreshContent(e: Entry, highlight: Highlight) {
        if (e is CandleEntry) {
            tvContent?.text = formatNumber(e.high, 0, true)
        } else {
            tvContent?.text = formatNumber(e.y, 0, true)
        }

        super.refreshContent(e, highlight)
    }

    override val offset: MPPointF
        get() = MPPointF(-(width / 2).toFloat(), -height.toFloat())
}
