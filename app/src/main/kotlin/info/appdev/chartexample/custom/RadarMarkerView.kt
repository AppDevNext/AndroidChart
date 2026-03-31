package info.appdev.chartexample.custom

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Typeface
import android.widget.TextView
import info.appdev.chartexample.R
import info.appdev.charting.components.MarkerView
import info.appdev.charting.data.EntryFloat
import info.appdev.charting.highlight.Highlight
import info.appdev.charting.utils.PointF
import java.text.DecimalFormat

/**
 * Custom implementation of the MarkerView.
 */
@SuppressLint("ViewConstructor")
class RadarMarkerView(context: Context, layoutResource: Int) : MarkerView(context, layoutResource) {
    private val tvContent: TextView = findViewById(R.id.tvContent)
    private val format = DecimalFormat("##0")

    init {
        tvContent.setTypeface(Typeface.createFromAsset(context.assets, "OpenSans-Light.ttf"))
    }

    // runs every time the MarkerView is redrawn, can be used to update the
    // content (user-interface)
    override fun refreshContent(entryFloat: EntryFloat, highlight: Highlight) {
        tvContent.text = String.format("%s %%", format.format(entryFloat.y.toDouble()))

        super.refreshContent(entryFloat, highlight)
    }

    override var offset: PointF = PointF()
        get() = PointF(-(width / 2).toFloat(), (-height - 10).toFloat())
}
