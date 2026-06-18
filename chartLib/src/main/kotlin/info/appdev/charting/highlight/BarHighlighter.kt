package info.appdev.charting.highlight

import android.annotation.SuppressLint
import info.appdev.charting.components.YAxis.AxisDependency
import info.appdev.charting.data.BarData
import info.appdev.charting.data.BarEntryFloat
import info.appdev.charting.data.DataSet
import info.appdev.charting.interfaces.dataprovider.BarDataProvider
import info.appdev.charting.interfaces.datasets.IBarDataSet
import info.appdev.charting.interfaces.datasets.IDataSet
import info.appdev.charting.utils.PointD
import kotlin.math.abs

open class BarHighlighter(barDataProvider: BarDataProvider) : ChartHighlighter<BarDataProvider>(barDataProvider) {

    override fun getHighlight(x: Float, y: Float): Highlight? {
        val barData = provider.barData ?: return null

        // 1. Convert touch pixel to chart coordinates
        val pos = getValsForTouch(x, y)
        val xVal = pos.x.toFloat()

        // 2. Resolve the closest base highlight using our overridden buildHighlights loop
        val high = getHighlightForX(xVal, x, y) ?: run {
            PointD.recycleInstance(pos)
            return null
        }

        val set = barData.getDataSetByIndex(high.dataSetIndex) ?: run {
            PointD.recycleInstance(pos)
            return null
        }

        // 3. Stacked Chart processing
        if (set.isStacked) {
            val stackedHighlight = getStackedHighlight(
                high,
                set,
                pos.x.toFloat(),
                pos.y.toFloat()
            )
            PointD.recycleInstance(pos)
            return stackedHighlight
        }

        // 4. Non-Stacked Chart data bounds check
        val entry = set.getEntryForXValue(high.x, high.y) as? BarEntryFloat
        if (entry != null) {
            val barWidthHalf = barData.barWidth / 2f
            val barLeft = entry.x - barWidthHalf
            val barRight = entry.x + barWidthHalf

            if (pos.x < barLeft || pos.x > barRight) {
                PointD.recycleInstance(pos)
                return null
            }

            val valY = entry.y
            if (valY >= 0) {
                if (pos.y > valY || pos.y < 0) {
                    PointD.recycleInstance(pos)
                    return null
                }
            } else {
                if (pos.y < valY || pos.y > 0) {
                    PointD.recycleInstance(pos)
                    return null
                }
            }
        }

        PointD.recycleInstance(pos)
        return high
    }

    override fun buildHighlights(
        @SuppressLint("RawTypeDataSet") set: IDataSet<*>,
        dataSetIndex: Int,
        xVal: Float,
        rounding: DataSet.Rounding?
    ): MutableList<Highlight> {
        val highlights = ArrayList<Highlight>()

        // Cast safely to BarEntryFloat instead of breaking on EntryFloat mapping
        var entries = set.getEntriesForXValue(xVal)?.map { it as BarEntryFloat }?.toMutableList()

        if (entries.isNullOrEmpty()) {
            val closest = set.getEntryForXValue(xVal, Float.NaN, rounding) as? BarEntryFloat
            if (closest != null) {
                entries = set.getEntriesForXValue(closest.x)?.map { it as BarEntryFloat }?.toMutableList()
            }
        }

        if (entries.isNullOrEmpty()) return highlights

        for (e in entries) {
            val pixels = provider.getTransformer(set.axisDependency)!!.getPixelForValues(e.x, e.y)
            highlights.add(
                Highlight(
                    x = e.x,
                    y = e.y,
                    xPx = pixels.x.toFloat(),
                    yPx = pixels.y.toFloat(),
                    dataSetIndex = dataSetIndex,
                    axis = set.axisDependency
                )
            )
            PointD.recycleInstance(pixels)
        }

        return highlights
    }

    open fun getStackedHighlight(high: Highlight, set: IBarDataSet, xVal: Float, yVal: Float): Highlight? {
        val entry = set.getEntryForXValue(high.x, high.y) as? BarEntryFloat ?: return high
        if (entry.yVals == null) return high

        val ranges = entry.ranges
        if (ranges.isNullOrEmpty()) return null

        // FIX: Removed the artificial selection tolerance to prevent selections when clicking in-between gaps
        val barWidthHalf = (provider.barData?.barWidth ?: 1f) / 2f

        val isHorizontal = provider.javaClass.name.contains("HorizontalBarChart", ignoreCase = true)

        if (isHorizontal) {
            val barBottomEdge = entry.x - barWidthHalf
            val barTopEdge = entry.x + barWidthHalf
            if (xVal < barBottomEdge || xVal > barTopEdge) return null

            val stackMin = ranges[0].from
            val stackMax = ranges[ranges.size - 1].to
            if (yVal < stackMin || yVal > stackMax) return null
        } else {
            val barLeftEdge = entry.x - barWidthHalf
            val barRightEdge = entry.x + barWidthHalf
            if (xVal < barLeftEdge || xVal > barRightEdge) return null

            val stackBottom = ranges[0].from
            val stackTop = ranges[ranges.size - 1].to
            val valueOffset = 0.05f
            if (entry.y >= 0) {
                if (yVal > (stackTop + valueOffset) || yVal < -valueOffset) return null
            } else {
                if (yVal < (stackBottom - valueOffset) || yVal > valueOffset) return null
            }
        }

        // FIX: Uniformly look up the segments along the value scale axis (yVal) for accurate layer picking
        val stackIndex = getClosestStackIndex(ranges, yVal)
        val pixels = if (isHorizontal) {
            val highlightVal = if (ranges[stackIndex].from < 0) ranges[stackIndex].from else ranges[stackIndex].to
            provider.getTransformer(set.axisDependency)!!.getPixelForValues(highlightVal, high.x)
        } else {
            provider.getTransformer(set.axisDependency)!!.getPixelForValues(high.x, ranges[stackIndex].to)
        }

        val stackedHigh = Highlight(
            x = entry.x,
            y = entry.y,
            xPx = pixels.x.toFloat(),
            yPx = pixels.y.toFloat(),
            dataSetIndex = high.dataSetIndex,
            stackIndex = stackIndex,
            axis = high.axis
        )

        PointD.recycleInstance(pixels)
        return stackedHigh
    }

    protected fun getClosestStackIndex(ranges: Array<Range>?, value: Float): Int {
        if (ranges.isNullOrEmpty()) return 0

        var stackIndex = 0
        for (range in ranges) {
            if (range.contains(value)) return stackIndex
            else stackIndex++
        }

        var closest = 0
        var closestDist = Float.MAX_VALUE
        for (i in ranges.indices) {
            val mid = (ranges[i].from + ranges[i].to) / 2f
            val dist = abs(mid - value)
            if (dist < closestDist) {
                closestDist = dist
                closest = i
            }
        }
        return closest
    }

    override fun getDistance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        return abs(x1 - x2)
    }
}