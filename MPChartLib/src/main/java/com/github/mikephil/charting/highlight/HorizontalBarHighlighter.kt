package com.github.mikephil.charting.highlight

import com.github.mikephil.charting.data.DataSet.Rounding
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.interfaces.dataprovider.BarDataProvider
import com.github.mikephil.charting.interfaces.datasets.IDataSet
import com.github.mikephil.charting.utils.MPPointD
import kotlin.math.abs

/**
 * Created by Philipp Jahoda on 22/07/15.
 */
class HorizontalBarHighlighter(chart: BarDataProvider?) : BarHighlighter(chart) {
    override fun getHighlight(x: Float, y: Float): Highlight? {
        val barData = mChart?.barData ?: return null

        val pos = getValsForTouch(y, x) ?: return null

        val high = getHighlightForX(pos.y.toFloat(), y, x)
        if (high == null) return null

        val set = barData.getDataSetByIndex(high.dataSetIndex)
        if (set.isStacked) {
            return getStackedHighlight(
                high,
                set,
                pos.y.toFloat(),
                pos.x.toFloat()
            )
        }

        MPPointD.Companion.recycleInstance(pos)

        return high
    }

    override fun buildHighlights(set: IDataSet<*>, dataSetIndex: Int, xVal: Float, rounding: Rounding?): MutableList<Highlight> {
        val highlights = ArrayList<Highlight>()

        var entries = set.getEntriesForXValue(xVal)
        if (entries.isEmpty()) {
            // Try to find closest x-value and take all entries for that x-value
            val closest: Entry? = set.getEntryForXValue(xVal, Float.Companion.NaN, rounding)
            if (closest != null) {
                entries = set.getEntriesForXValue(closest.x)
            }
        }

        if (entries.isEmpty()) return highlights

        for (e in entries) {
            val pixels = mChart?.getTransformer(
                set.axisDependency
            )?.getPixelForValues(e.y, e.x)

            if (pixels != null) {
                highlights.add(
                    Highlight(
                        e.x, e.y,
                        pixels.x.toFloat(), pixels.y.toFloat(),
                        dataSetIndex, set.axisDependency
                    )
                )
            }
        }

        return highlights
    }

    override fun getDistance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        return abs(y1 - y2)
    }
}
