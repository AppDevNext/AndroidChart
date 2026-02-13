package info.appdev.chartexample.custom

import android.annotation.SuppressLint
import android.content.Context
import android.widget.TextView
import info.appdev.chartexample.R
import info.appdev.charting.components.MarkerView
import info.appdev.charting.data.Entry
import info.appdev.charting.highlight.Highlight
import info.appdev.charting.interfaces.datasets.IDataSet
import info.appdev.charting.utils.PointF
import java.text.SimpleDateFormat
import java.util.Locale

@SuppressLint("ViewConstructor")
class TimeMarkerView(context: Context?, layoutResource: Int, val format: String = "yyyy-MM-dd'T'HH:mm:ss'Z'") : MarkerView(context, layoutResource) {

    private val simpleDateFormat = SimpleDateFormat(format, Locale.getDefault())
    private val tvContent: TextView = findViewById(R.id.tvContent)

    @SuppressLint("SetTextI18n")
    override fun refreshContent(entry: Entry, highlight: Highlight) {
        @Suppress("UNCHECKED_CAST")
        val dataset = this.chartView?.data?.dataSets[0] as? IDataSet<Entry>
        val myIndex = dataset?.getEntryIndex(entry)
        val nextEntry = myIndex?.let {
            if (it < dataset.entryCount - 1)
                dataset.getEntryForIndex(myIndex + 1)
            else
                null
        } ?: run { null }

        val duration = if (nextEntry != null) " - duration:${(nextEntry.x - entry.x)}" else ""
        tvContent.text = "${simpleDateFormat.format(entry.x)}$duration"
        super.refreshContent(entry, highlight)
    }

    override var offset: PointF = PointF()
        get() = PointF(-(width / 2).toFloat(), -height.toFloat())
}
