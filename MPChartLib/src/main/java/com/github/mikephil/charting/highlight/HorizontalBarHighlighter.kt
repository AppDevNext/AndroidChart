package com.github.mikephil.charting.highlight

import com.github.mikephil.charting.data.DataSet
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.interfaces.dataprovider.BarDataProvider
import com.github.mikephil.charting.interfaces.datasets.IDataSet
import com.github.mikephil.charting.utils.MPPointD
import kotlin.math.abs

class HorizontalBarHighlighter(dataProvider: BarDataProvider) : BarHighlighter(dataProvider) {
    override fun getHighlight(x: Float, y: Float): Highlight? {
        val barData = provider.barData

        val pos = getValsForTouch(y, x)

        val high = getHighlightForX(pos.y.toFloat(), y, x) ?: return null

        val set = barData.getDataSetByIndex(high.dataSetIndex)
        if (set.isStacked()) {
            return getStackedHighlight(
                high,
                set,
                pos.y.toFloat(),
                pos.x.toFloat()
            )
        }

        MPPointD.recycleInstance(pos)

        return high
    }

    override fun buildHighlights(set: IDataSet<*>, dataSetIndex: Int, xVal: Float, rounding: DataSet.Rounding?): MutableList<Highlight> {
        val highlights = ArrayList<Highlight>()

        var entries = set.getEntriesForXValue(xVal)
        if (entries != null && entries.isEmpty()) {
            // Try to find closest x-value and take all entries for that x-value
            val closestEntry: Entry? = set.getEntryForXValue(xVal, Float.NaN, rounding)
            closestEntry?.let { closestE ->
                entries = set.getEntriesForXValue(closestE.x)
            }
        }

        if (entries != null && entries.isEmpty())
            return highlights

        if (entries != null)
            for (entry in entries) {
                val pixels = provider.getTransformer(set.axisDependency)!!.getPixelForValues(entry.y, entry.x)

                highlights.add(
                    Highlight(
                        entry.x, entry.y,
                        pixels.x.toFloat(), pixels.y.toFloat(),
                        dataSetIndex, set.axisDependency
                    )
                )
            }

        return highlights
    }

    override fun getDistance(x1: Float, y1: Float, x2: Float, y2: Float) = abs(y1 - y2)
}
