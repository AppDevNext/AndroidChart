package info.appdev.chartexample.custom

import android.annotation.SuppressLint
import android.content.Context
import android.widget.TextView
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.BarEntry
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
@Suppress("unused")
@SuppressLint("ViewConstructor")
class StackedBarsMarkerView(context: Context, layoutResource: Int) : MarkerView(context, layoutResource) {
    private val tvContent = findViewById<TextView?>(R.id.tvContent)

    // runs every time the MarkerView is redrawn, can be used to update the
    // content (user-interface)
    override fun refreshContent(e: Entry, highlight: Highlight) {
        if (e is BarEntry) {
            val be = e

            if (be.yVals != null) {
                // draw the stack value

                tvContent?.text = formatNumber(be.yVals!![highlight.stackIndex], 0, true)
            } else {
                tvContent?.text = formatNumber(be.y, 0, true)
            }
        } else {
            tvContent?.text = formatNumber(e.y, 0, true)
        }

        super.refreshContent(e, highlight)
    }

    override val offset: MPPointF
        get() = MPPointF(-(width / 2).toFloat(), -height.toFloat())
}
