package info.appdev.charting.highlight

import info.appdev.charting.interfaces.dataprovider.BarDataProvider
import info.appdev.charting.interfaces.datasets.IBarDataSet
import info.appdev.charting.utils.PointD
import kotlin.math.abs
import kotlin.math.max

open class BarHighlighter(barDataProvider: BarDataProvider) : ChartHighlighter<BarDataProvider>(barDataProvider) {
    override fun getHighlight(x: Float, y: Float): Highlight? {
        val high = super.getHighlight(x, y) ?: return null

        val pos = getValsForTouch(x, y)

        provider.barData?.let { barData ->
            barData.getDataSetByIndex(high.dataSetIndex)?.let { set ->
                if (set.isStacked) {
                    return getStackedHighlight(
                        high,
                        set,
                        pos.x.toFloat(),
                        pos.y.toFloat()
                    )
                }
            }
        }
        PointD.recycleInstance(pos)

        return high
    }

    /**
     * This method creates the Highlight object that also indicates which value of a stacked BarEntry has been
     * selected.
     *
     * @param high the Highlight to work with looking for stacked values
     */
    fun getStackedHighlight(high: Highlight, set: IBarDataSet, xVal: Float, yVal: Float): Highlight? {
        set.getEntryForXValue(xVal, yVal)?.let { entry ->
            // not stacked
            if (entry.yVals == null) {
                return high
            } else {
                val ranges: Array<Range> = entry.ranges

                if (ranges.isNotEmpty()) {
                    val stackIndex = getClosestStackIndex(ranges, yVal)

                    val pixels = provider.getTransformer(set.axisDependency)!!.getPixelForValues(high.x, ranges[stackIndex].to)

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
            }
        }
        return null
    }

    /**
     * Returns the index of the closest value inside the values array / ranges (stacked barchart) to the value given as a parameter.
     */
    protected fun getClosestStackIndex(ranges: Array<Range>?, value: Float): Int {
        if (ranges == null || ranges.isEmpty())
            return 0

        var stackIndex = 0

        for (range in ranges) {
            if (range.contains(value))
                return stackIndex
            else
                stackIndex++
        }

        val length = max(ranges.size - 1, 0)

        return if (value > ranges[length].to) length else 0
    }

    override fun getDistance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        return abs(x1 - x2)
    }

}
