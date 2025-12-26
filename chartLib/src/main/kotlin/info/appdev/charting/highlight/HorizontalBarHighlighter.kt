package info.appdev.charting.highlight

import info.appdev.charting.data.DataSet
import info.appdev.charting.data.Entry
import info.appdev.charting.interfaces.dataprovider.BarDataProvider
import info.appdev.charting.interfaces.datasets.IDataSet
import info.appdev.charting.utils.MPPointD
import kotlin.math.abs

class HorizontalBarHighlighter(dataProvider: BarDataProvider) : BarHighlighter(dataProvider) {
    override fun getHighlight(x: Float, y: Float): Highlight? {
        provider.barData?.let { barData ->

            val pos = getValsForTouch(y, x)

            val high = getHighlightForX(pos.y.toFloat(), y, x) ?: return null

            val set = barData.getDataSetByIndex(high.dataSetIndex)
            if (set != null && set.isStacked) {
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
        return null
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
