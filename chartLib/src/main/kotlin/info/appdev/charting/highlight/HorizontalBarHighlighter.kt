package info.appdev.charting.highlight

import android.annotation.SuppressLint
import android.util.Log
import info.appdev.charting.data.DataSet
import info.appdev.charting.data.EntryFloat
import info.appdev.charting.interfaces.dataprovider.BarDataProvider
import info.appdev.charting.interfaces.datasets.IDataSet
import info.appdev.charting.utils.PointD
import kotlin.math.abs

class HorizontalBarHighlighter(dataProvider: BarDataProvider) : BarHighlighter(dataProvider) {

    override fun getHighlight(x: Float, y: Float): Highlight? {
        provider.barData?.let { barData ->
            // In horizontal layouts, the touch engine passes inverted screen projection scales
            val pos = getValsForTouch(y, x)

            val high = getHighlightForX(pos.y.toFloat(), y, x)
            if (high == null) {
                Log.d("HIGHLIGHT", "high is null after getHighlightForX")
                return null
            }

            val set = barData.getDataSetByIndex(high.dataSetIndex) ?: return null

            if (set.isStacked) {
                val result = getStackedHighlight(
                    high,
                    set,
                    pos.y.toFloat(),
                    pos.x.toFloat()
                )
                Log.d("HIGHLIGHT", "stacked result: ${if (result == null) "NULL" else "x=${result.x} xPx=${result.xPx} yPx=${result.yPx}"}")
                PointD.recycleInstance(pos)
                return result
            }

            // ─── TRANSLATED DATA-BOUNDS FIX (NON-STACKED HORIZONTAL) ───
            val entry = set.getEntryForXValue(high.x, high.y)
            if (entry != null) {
                // Vertical Category Check: Ensure touch y is within the slot height bounds
                val barWidthHalf = barData.barWidth / 2f
                val barBottom = entry.x - barWidthHalf
                val barTop = entry.x + barWidthHalf

                if (pos.y < barBottom || pos.y > barTop) {
                    PointD.recycleInstance(pos)
                    return null
                }

                // Horizontal Value Length Check: Ensure touch x matches the bar length range
                val valY = entry.y
                if (valY >= 0) {
                    if (pos.x > valY || pos.x < 0) {
                        PointD.recycleInstance(pos)
                        return null
                    }
                } else {
                    if (pos.x < valY || pos.x > 0) {
                        PointD.recycleInstance(pos)
                        return null
                    }
                }
            }
            // ─── END OF FIX ───

            PointD.recycleInstance(pos)
            Log.d("HIGHLIGHT", "Returning high: x=${high.x} y=${high.y}")
            return high
        }
        return null
    }

    override fun buildHighlights(
        @SuppressLint("RawTypeDataSet") set: IDataSet<*>,
        dataSetIndex: Int,
        xVal: Float,
        rounding: DataSet.Rounding?
    ): MutableList<Highlight> {
        val highlights = ArrayList<Highlight>()

        var entries = set.getEntriesForXValue(xVal)?.map { it as EntryFloat }?.toMutableList()

        if (entries != null && entries.isEmpty()) {
            // Try to find closest x-value and take all entries for that x-value
            val closestEntryFloat = set.getEntryForXValue(xVal, Float.NaN, rounding) as? EntryFloat
            closestEntryFloat?.let { closestE ->
                entries = set.getEntriesForXValue(closestE.x)?.map { it as EntryFloat }?.toMutableList()
            }
        }

        if (entries == null || entries.isEmpty())
            return highlights

        for (entry in entries!!) {
            val pixels = provider.getTransformer(set.axisDependency)!!.getPixelForValues(entry.y, entry.x)

            highlights.add(
                Highlight(
                    x = entry.x,
                    y = entry.y,
                    xPx = pixels.x.toFloat(),
                    yPx = pixels.y.toFloat(),
                    dataSetIndex = dataSetIndex,
                    axis = set.axisDependency
                )
            )
        }

        return highlights
    }

    override fun getDistance(x1: Float, y1: Float, x2: Float, y2: Float) = abs(y1 - y2)
}